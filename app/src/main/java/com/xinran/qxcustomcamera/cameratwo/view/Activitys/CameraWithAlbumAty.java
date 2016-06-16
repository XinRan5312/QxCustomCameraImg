package com.xinran.qxcustomcamera.cameratwo.view.Activitys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.xinran.qxcustomcamera.R;

/**
 * Created by qixinh on 16/6/12.
 */
public class CameraWithAlbumAty extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_with_album);
    }

    public void goCamera(View view) {
        startActivity(new Intent(this, CameraAty.class));
    }
}
