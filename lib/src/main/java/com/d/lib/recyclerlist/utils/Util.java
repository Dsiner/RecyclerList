package com.d.lib.recyclerlist.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

/**
 * Util集合
 * Created by D on 2017/4/27.
 */
public class Util {
    private static int SCREEN_WIDTH;//屏幕宽度
    private static int SCREEN_HEIGHT;//屏幕宽度

    /**
     * Toast提示
     *
     * @param c：   Context
     * @param msg： String
     */
    public static void toast(Context c, String msg) {
        if (c == null || TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Toast提示
     *
     * @param c：   Context
     * @param msg： String
     */
    public static void toastLong(Context c, String msg) {
        if (c == null || TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(c, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Toast提示
     *
     * @param c：     Context
     * @param resId： resource id
     */
    public static void toast(Context c, int resId) {
        if (c == null) {
            return;
        }
        toast(c, c.getString(resId));
    }

    /**
     * 打印当前代码所在线程信息
     */
    public static void printThread(String tag) {
        Log.d("Current thread", tag + Thread.currentThread().getId() + "--NAME--" + Thread.currentThread().getName());
    }

    /**
     * 获取屏幕宽度和高度
     *
     * @return int[]{SCREEN_WIDTH, SCREEN_HEIGHT}
     */
    public static int[] getScreenSize(Activity activity) {
        if (SCREEN_WIDTH > 0 && SCREEN_HEIGHT > 0) {
            return new int[]{SCREEN_WIDTH, SCREEN_HEIGHT};
        }
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        if (metric.widthPixels != SCREEN_WIDTH) {
            SCREEN_WIDTH = metric.widthPixels;
            SCREEN_HEIGHT = metric.heightPixels;
        }
        return new int[]{SCREEN_WIDTH, SCREEN_HEIGHT};
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (dpValue * (metrics.densityDpi / 160f));
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context:context
     * @param spValue:DisplayMetrics类中属性scaledDensity）
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取字体高度
     */
    public static float getTextHeight(Paint p) {
        Paint.FontMetrics fm = p.getFontMetrics();// 获取字体高度
        return (float) ((Math.ceil(fm.descent - fm.top) + 2) / 2);
    }

    public static int getTextWidth(String str, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(str, 0, str.length(), bounds);
        return bounds.width();
    }

    public static int getTextWidth(String str, TextView tvText) {
        Rect bounds = new Rect();
        TextPaint paint = tvText.getPaint();
        paint.getTextBounds(str, 0, str.length(), bounds);
        return bounds.width();
    }

    /**
     * format a number properly with the given number of digits
     *
     * @param number the number to format
     * @param digits the number of digits
     * @return
     */
    public static String formatDecimal(double number, int digits) {
        number = roundNumber((float) number, digits);
        StringBuffer a = new StringBuffer();
        for (int i = 0; i < digits; i++) {
            if (i == 0)
                a.append(".");
            a.append("0");
        }
        DecimalFormat nf = new DecimalFormat("###,###,###,##0" + a.toString());
        String formatted = nf.format(number);
        return formatted;
    }

    /**
     * Math.pow(...) is very expensive, so avoid calling it and create it
     * yourself.
     */
    private static final int POW_10[] = {
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    };

    public static float roundNumber(float number, int digits) {
        try {
            if (digits == 0) {
                int r0 = (int) Math.round(number);
                return r0;
            } else if (digits > 0) {
                if (digits > 9)
                    digits = 9;
                StringBuffer a = new StringBuffer();
                for (int i = 0; i < digits; i++) {
                    if (i == 0)
                        a.append(".");
                    a.append("0");
                }
                DecimalFormat nf = new DecimalFormat("#" + a.toString());
                String formatted = nf.format(number);
                return Float.valueOf(formatted);
            } else {
                digits = -digits;
                if (digits > 9)
                    digits = 9;
                int r2 = (int) (number / POW_10[digits] + 0.5);
                return r2 * POW_10[digits];
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return number;
        }
    }
}
