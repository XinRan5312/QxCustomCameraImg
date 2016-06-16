package com.xinran.qxcustomcamera.photoview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.xinran.qxcustomcamera.R;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


public class PicassoSampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photoview_activity_simple);

        PhotoView photoView = (PhotoView) findViewById(R.id.iv_photo);

        final PhotoViewAttacher attacher = new PhotoViewAttacher(photoView);

        Picasso.with(this)
                .load("http://pbs.twimg.com/media/Bist9mvIYAAeAyQ.jpg")
                .into(photoView, new Callback() {
                    @Override
                    public void onSuccess() {

                        /**
                         * 下面这个跟在初始化photoView后photoView.setImageResource(R.drawable.wallpaper)一个道理
                         */
                        attacher.update();
                    }

                    @Override
                    public void onError() {
                    }
                });
    }
}
