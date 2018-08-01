# RecyclerList for Android

[![License](https://img.shields.io/badge/license-Apache%202-green.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![API](https://img.shields.io/badge/API-9%2B-green.svg?style=flat)](https://android-arsenal.com/api?level=9)

> Handwritten recyclable list, like `ListView`, `RecyclerView`. For testing only.

## Demo
<p>
   <img src="https://github.com/Dsiner/Resouce/blob/master/lib/RecyclerList/recyclerlist.gif" width="320" alt="Screenshot"/>
</p>

## Support list
- [x] Support recycling
- [x] Support simple or multiple adapter
- [ ] Support notifyDataSetChanged

### Use
```java
    RecyclerList list = (RecyclerList) findViewById(R.id.list);
    Adapter adapter = new Adapter(this, getDatas(200), R.layout.adapter_item);
    list.setAdapter(adapter)
```

More usage see [Demo](app/src/main/java/com/d/recyclerlist/MainActivity.java)

## Licence

```txt
Copyright 2018 D

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
