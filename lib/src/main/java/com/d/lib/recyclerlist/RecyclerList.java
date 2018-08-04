package com.d.lib.recyclerlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.d.lib.recyclerlist.adapter.CommonAdapter;
import com.d.lib.recyclerlist.adapter.CommonHolder;

/**
 * RecyclerList
 * Created by D on 2018/7/11.
 */
public class RecyclerList extends ViewGroup {
    static final String TAG = "RecyclerList";

    private int mWidth, mHeight, mItemHeight;
    private int mWidthMeasureSpec, mHeightMeasureSpec;

    private final float mLoadFactor = 0.36f;
    private final int mDuration = 1000;

    private Context mContext;
    private Scroller mScroller;
    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    private CommonAdapter mAdapter;
    private RecycledViewPool mRecyclerPool;

    private int mTopBorder, mBottomBorder;
    private int mPositon, mOffset;
    private int mSize;
    private boolean mBlockLayoutRequests;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mOverscrollDistance;
    private int mOverflingDistance;

    private boolean mIsMoveValid;

    // TouchEvent_ACTION_DOWN coordinates (mDx, mDy)
    private float mDx, mDy;
    private float mLastX, mLastY;

    private int mActivePointerId;
    private boolean isFirst = true;

    public RecyclerList(Context context) {
        super(context);
        init(context);
    }

    public RecyclerList(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RecyclerList(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = (int) (configuration.getScaledMaximumFlingVelocity() * 0.36f);
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        mOverflingDistance = configuration.getScaledOverflingDistance();

        mScroller = new Scroller(context);
        mRecyclerPool = new RecycledViewPool();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mWidthMeasureSpec = widthMeasureSpec;
        mHeightMeasureSpec = heightMeasureSpec;

        final int count = getChildCount();

        final boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;

        if (count <= 0 || measureMatchParentChildren) {
            // Not support mode
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    private void measureChild(View child) {
        LayoutParams lp = child.getLayoutParams();
        // Measure size for each child view in the ViewGroup
        final int childWidthMeasureSpec;
        if (lp.width == LayoutParams.MATCH_PARENT) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, mWidth), MeasureSpec.EXACTLY);
        } else {
            childWidthMeasureSpec = getChildMeasureSpec(mHeightMeasureSpec, 0, lp.width);
        }

        final int childHeightMeasureSpec;
        if (lp.height == LayoutParams.MATCH_PARENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.max(0, mHeight), MeasureSpec.EXACTLY);
        } else {
            childHeightMeasureSpec = getChildMeasureSpec(mHeightMeasureSpec, 0, lp.height);
        }

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int childCount = getChildCount();
        if (changed) {
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
        }
        layoutChildren();
    }

    private void layoutChildren() {
        if (mBlockLayoutRequests) {
            return;
        }

        // Clear out old views
        detachAllViewsFromParent();

        final int recyclers = mRecyclerPool.size();
        final int size = mAdapter != null ? mSize : 0;
        if (size <= 0) {
            mRecyclerPool.clear();
        }

        if (mAdapter != null) {
            for (int i = mPositon, totalHeight = 0; i < size && totalHeight < mHeight * (1 + mLoadFactor); i++) {
                View child = getViewForPosition(i);
                addView(child);
                measureChild(child);
                totalHeight += child.getMeasuredHeight();
            }
        }

        final int childCount = getChildCount();
        int top = getChildAt(0).getMeasuredHeight() * mPositon + mOffset;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();
            mItemHeight = height;

            // Layout horizontally for each child view in the ViewGroup
            child.layout(0, top, width, top + height);
            top += height;
        }
        // Initialize left and right boundary values
        mTopBorder = getChildAt(0).getTop();
        mBottomBorder = getChildAt(childCount - 1).getBottom();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            final float eX = ev.getX();
            final float eY = ev.getY();
            mLastY = mDy = eY;
            mDx = eX;
            super.dispatchTouchEvent(ev);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            final float eX = ev.getX();
            final float eY = ev.getY();
            // Intercept child event when horizontal ACTION_MOVE value is greater than TouchSlop
            if (Math.abs(eY - mDy) > mTouchSlop || Math.abs(eX - mDx) > mTouchSlop) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float eX = event.getX();
        final float eY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!mIsMoveValid && Math.abs(eY - mDy) > mTouchSlop && Math.abs(eY - mDy) > Math.abs(eX - mDx)) {
                    // Disable parent view interception events
                    requestDisallowInterceptTouchEvent(true);
                    mActivePointerId = event.getPointerId(0);
                    initOrResetVelocityTracker();
                    mVelocityTracker.addMovement(event);
                    mLastY = eY;
                    mIsMoveValid = true;
                }
                if (mIsMoveValid) {
                    final int offset = (int) (mLastY - eY);
                    mLastY = eY;
                    if (getScrollY() + offset <= 0) {
                        scrollTo(0, 0);
                    } else if (isBottom(offset)) {
                        return true;
                    } else {
                        scrollBy(0, offset);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsMoveValid) {
                    mIsMoveValid = false;
                    initVelocityTrackerIfNotExists();
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    final int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                    final boolean flingVelocity = Math.abs(initialVelocity) > mMinimumVelocity;
                    final int initialY = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
                    if (flingVelocity) {
                        mScroller.fling(0, getScrollY(), 0, -initialVelocity,
                                0, 0, 0, Integer.MAX_VALUE);
                    }
                    recycleVelocityTracker();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.d(TAG, "dsiner onScrollChanged: ChildCount: " + getChildCount()
                + " left: " + l + " top: " + t + " oldl: " + oldl + " oldt: " + oldt);
        if (getChildCount() <= 0) {
            return;
        }
        mBlockLayoutRequests = true;
        boolean down = t - oldt > 0;
        fillTop(t, down);
        fillDown(t, down);
        if (!mIsMoveValid) {
            isBottom(mTouchSlop);
        }
    }

    private void fillTop(int scrollY, boolean down) {
        if (getChildCount() <= 0) {
            return;
        }
        View first = getChildAt(0);
        CommonHolder holderFirst = (CommonHolder) first.getTag();
        mPositon = holderFirst.getPosition();
        mOffset = first.getTop() - scrollY;
        if (!down && first.getTop() - scrollY >= -mHeight * mLoadFactor) {
            mPositon -= 1;
            if (mPositon < 0) {
                mPositon = 0;
                return;
            }
            View child = getViewForPosition(mPositon);
            // Respect layout params that are already in the view. Otherwise make
            // some up...
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = (LayoutParams) generateDefaultLayoutParams();
            }
            addViewInLayout(child, 0, p);
            measureChild(child);
            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();
            final int childTop = first.getTop() - height;
            child.layout(0, childTop, width, childTop + height);
            mOffset -= height;
        } else if (down && first.getBottom() - scrollY < -mHeight * mLoadFactor) {
            mPositon += 1;
            mOffset = first.getBottom() - scrollY;
            detachViewFromParent(first);
            mRecyclerPool.putRecycledView(holderFirst);
        }
    }

    private void fillDown(int scrollY, boolean down) {
        if (getChildCount() <= 0) {
            return;
        }
        View last = getChildAt(getChildCount() - 1);
        CommonHolder holderLast = (CommonHolder) last.getTag();
        if (down && last.getBottom() - mHeight - scrollY <= mHeight * mLoadFactor) {
            int position = holderLast.getPosition() + 1;
            if (position > mSize - 1) {
                return;
            }
            View child = getViewForPosition(position);
            // Respect layout params that are already in the view. Otherwise make
            // some up...
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = (LayoutParams) generateDefaultLayoutParams();
            }
            addViewInLayout(child, -1, p);
            measureChild(child);
            final int width = child.getMeasuredWidth();
            final int height = child.getMeasuredHeight();
            final int childTop = last.getBottom();
            child.layout(0, childTop, width, childTop + height);
        } else if (!down && last.getTop() - mHeight - scrollY > mHeight * mLoadFactor) {
            detachViewFromParent(last);
            mRecyclerPool.putRecycledView(holderLast);
        }
    }

    public void smoothScrollTo(int position, int offset) {
        if (position < 0 || position > (mAdapter != null ? mSize - 1 : 0)) {
            return;
        }
        int dstY = mItemHeight * position + offset;
        int dValue = dstY - getScrollY();
        mScroller.startScroll(0, getScrollY(), 0, dValue, mDuration);
        invalidate();
    }

    private View getViewForPosition(int position) {
        CommonHolder holder = obtain(position);
        return bindHolder(position, holder);
    }

    private CommonHolder obtain(int position) {
        return mRecyclerPool.getRecycledView(mAdapter.getItemViewType(position));
    }

    private View bindHolder(int position, CommonHolder holder) {
        return mAdapter.getView(position, holder != null ? holder.getConvertView() : null, this);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public boolean isTop() {
        return getChildCount() <= 0 || getScrollY() <= 0;
    }

    public boolean isBottom(int offset) {
        int bottomY = getBottomBorder();
        if (bottomY < 0) {
            return false;
        }
        if (getScrollY() + offset >= bottomY - mHeight) {
            if (!mIsMoveValid) {
                mScroller.setFinalY(bottomY - mHeight);
                mScroller.abortAnimation();
            }
            scrollTo(0, bottomY - mHeight);
            return true;
        }
        return false;
    }

    public int getBottomBorder() {
        if (mAdapter == null || getChildCount() <= 0) {
            return -1;
        }
        View last = getChildAt(getChildCount() - 1);
        CommonHolder holderLast = (CommonHolder) last.getTag();
        if (holderLast.getPosition() >= mSize - 1) {
            return last.getBottom();
        }
        return -1;
    }

    private void reset() {
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }
        mSize = mAdapter.getCount();
        mBlockLayoutRequests = false;
        mPositon = 0;
        mOffset = 0;
        requestLayout();
        scrollTo(0, 0);
    }

    public void setRecycledViewPool(@NonNull RecycledViewPool pool) {
        mRecyclerPool = pool;
    }

    public void setAdapter(CommonAdapter adapter) {
        this.mAdapter = adapter;
        this.mAdapter.registerDataSetObserver(new CommonAdapter.DataSetObserver() {
            @Override
            public void notifyChanged() {
                reset();
            }
        });
        reset();
    }
}
