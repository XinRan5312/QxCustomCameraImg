package com.xinran.qxcustomcamera.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.File;
/**
 * Created by qixinh on 16/6/8.
 */
public class Utils {

    public static DisplayMetrics getScreenWH(Context context) {

        return context.getResources().getDisplayMetrics();
    }

    /**
     * 计算焦点及测光区域
     *
     * @param focusWidth
     * @param focusHeight
     * @param areaMultiple
     * @param x
     * @param y
     * @param previewleft
     * @param previewRight
     * @param previewTop
     * @param previewBottom
     * @return Rect(left,top,right,bottom) : left、top、right、bottom是以显示区域中心为原点的坐标
     */
    public static Rect calculateTapArea(int focusWidth, int focusHeight,
                                 float areaMultiple, float x, float y, int previewleft,
                                 int previewRight, int previewTop, int previewBottom) {
        int areaWidth = (int) (focusWidth * areaMultiple);
        int areaHeight = (int) (focusHeight * areaMultiple);
        int centerX = (previewleft + previewRight) / 2;
        int centerY = (previewTop + previewBottom) / 2;
        double unitx = ((double) previewRight - (double) previewleft) / 2000;
        double unity = ((double) previewBottom - (double) previewTop) / 2000;
        int left = clamp((int) (((x - areaWidth / 2) - centerX) / unitx),
                -1000, 1000);
        int top = clamp((int) (((y - areaHeight / 2) - centerY) / unity),
                -1000, 1000);
        int right = clamp((int) (left + areaWidth / unitx), -1000, 1000);
        int bottom = clamp((int) (top + areaHeight / unity), -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    public static int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    /**
     * 检测摄像头设备是否可用
     * Check if this device has a camera
     * @param context
     * @return
     */
    public static boolean checkCameraHardware(Context context) {
        if (context != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * @param context
     * @return app_cache_path/dirName
     */
    public static String getDBDir(Context context) {
        String path = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + "bbk" + File.separator + "cloudteacher" + File.separator + "db";
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                path = externalCacheDir.getPath();
            }
        }
        if (path == null) {
            File cacheDir = context.getCacheDir();
            if (cacheDir != null && cacheDir.exists()) {
                path = cacheDir.getPath();
            }
        }
        return path;
    }

    /**
     * bitmap旋转
     * @param b
     * @param degrees
     * @return
     */
    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();  //Android开发网再次提示Bitmap操作完应该显示的释放
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                return b;
            }
        }
        return b;
    }

    public static final int getHeightInPx(Context context) {
       return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static final int getWidthInPx(Context context) {

        return context.getResources().getDisplayMetrics().widthPixels;
    }

}
