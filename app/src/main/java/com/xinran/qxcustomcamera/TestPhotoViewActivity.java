package com.xinran.qxcustomcamera;

import android.app.Activity;
import android.os.Bundle;

import uk.co.senab.photoview.PhotoView;

/**
 *
 *  * // https://github.com/chrisbanes/PhotoView
 * PhotoView aims to help produce an easily usable implementation of a zooming Android ImageView.
 *
 * Out of the box zooming, using multi-touch and double-tap.
 Scrolling, with smooth scrolling fling.
 Works perfectly when used in a scrolling parent (such as ViewPager).
 Allows the application to be notified when the displayed Matrix has changed. Useful for when you need to update your UI based on the current zoom/scroll position.
 Allows the application to be notified when the user taps on the Photo.

 使用详情可以看本项目中com.xinran.qxcustomcamera.photoview.LauncherActivity
 * Created by qixinh on 16/6/13.
 */
public class TestPhotoViewActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview_activity_simple);
        PhotoView photoView= (PhotoView) findViewById(R.id.iv_photo);
        photoView.setImageResource(R.drawable.ic_launcher);
    }
}
