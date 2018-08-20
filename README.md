# DialView自定义刻度盘
### 效果图
![效果图1](https://raw.githubusercontent.com/lwh7819/source/master/image/dialview1.gif)
![效果图2](https://raw.githubusercontent.com/lwh7819/source/master/image/dialview2.gif)
![效果图3](https://raw.githubusercontent.com/lwh7819/source/master/image/dialview3.gif)
![效果图4](https://raw.githubusercontent.com/lwh7819/source/master/image/dialview4.gif)
### 使用
```
添加依赖：
compile 'com.lwh.dialView:dialview:1.0.3'
```
```
<com.example.lvweihao.dialdemo.DialView
        android:id="@+id/dialView"
        android:layout_marginTop="100dp"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:startStr="0"
        app:midStr="50"
        app:endStr="100"
        app:unitStr="%"
        app:lineLong="100"/>
```
布局里引用一下,添加自定义属性即可
```
 //事件回调
        dialView.setOnDialViewTouchListener(new OnDialViewTouch() {
            @Override
            public void onTouched(int value) { //百分比0-100
                Log.e("lwa", "value:" + value);
                //TODO 业务逻辑
            }
        });
```
在java代码里添加触摸事件回调监听，返回的value为当前百分比（0-100）
