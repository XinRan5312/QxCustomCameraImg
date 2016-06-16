package com.xinran.qxcustomcamera.customcamare;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.xinran.qxcustomcamera.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Class: CameraPreview
 * @Description: �Զ������
 * Created by qixinh on 16/6/8.
 */
public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback, AutoFocusCallback {
	private static final String TAG = "CameraPreview";

	private int viewWidth = 0;
	private int viewHeight = 0;

	/** �����ӿ� */
	private OnCameraStatusListener listener;

	private SurfaceHolder holder;
	private Camera camera;
	private FocusView mFocusView;

	//����һ��PictureCallback���󣬲�ʵ�����е�onPictureTaken����
	private PictureCallback pictureCallback = new PictureCallback() {

		// �÷������ڴ�����������Ƭ����
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// ֹͣ��Ƭ����
			try {
				camera.stopPreview();
			} catch (Exception e) {
			}
			// ���ý����¼�
			if (null != listener) {
				listener.onCameraStopped(data);
			}
		}
	};

	// Preview��Ĺ��췽��
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// ���SurfaceHolder����
		holder = getHolder();
		// ָ�����ڲ�׽�����¼���SurfaceHolder.Callback����
		holder.addCallback(this);
		// ����SurfaceHolder���������
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		setOnTouchListener(onTouchListener);
	}

	// ��surface����ʱ����
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "==surfaceCreated==");
		if(!Utils.checkCameraHardware(getContext())) {
			Toast.makeText(getContext(), "����ͷ��ʧ�ܣ�", Toast.LENGTH_SHORT).show();
			return;
		}
		// ���Camera����
		camera = getCameraInstance();
		try {
			// ����������ʾ���������SurfaceHolder����
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
			// �ͷ��ֻ�����ͷ
			camera.release();
			camera = null;
		}
		updateCameraParameters();
		if (camera != null) {
			camera.startPreview();
		}
		setFocus();
	}

	// ��surface����ʱ����
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "==surfaceDestroyed==");
		// �ͷ��ֻ�����ͷ
		camera.release();
		camera = null;
	}

	// ��surface�Ĵ�С�����ı�ʱ����
	public void surfaceChanged(final SurfaceHolder holder, int format, int w,
							   int h) {
		// stop preview before making changes
		try {
			camera.stopPreview();
		} catch (Exception e){
			// ignore: tried to stop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		updateCameraParameters();
		// start preview with new settings
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();

		} catch (Exception e){
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
		setFocus();
	}

	/**
	 * �����ʾ��������
	 */
	OnTouchListener onTouchListener = new OnTouchListener() {
		@SuppressWarnings("deprecation")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int width = mFocusView.getWidth();
				int height = mFocusView.getHeight();
				mFocusView.setX(event.getX() - (width / 2));
				mFocusView.setY(event.getY() - (height / 2));
				mFocusView.beginFocus();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				focusOnTouch(event);
			}
			return true;
		}
	};

	/**
	 * ��ȡ����ͷʵ��
	 * @return
	 */
	private Camera getCameraInstance() {
		Camera c = null;
		try {
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras(); // get cameras number

			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
				// ��������ͷ�ķ�λ��Ŀǰ�ж���ֵ�����ֱ�ΪCAMERA_FACING_FRONTǰ�ú�CAMERA_FACING_BACK����
				if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					try {
						c = Camera.open(camIdx);   //�򿪺�������ͷ
					} catch (RuntimeException e) {
						Toast.makeText(getContext(), "����ͷ��ʧ�ܣ�", Toast.LENGTH_SHORT).show();
					}
				}
			}
			if (c == null) {
				c = Camera.open(0); // attempt to get a Camera instance
			}
		} catch (Exception e) {
			Toast.makeText(getContext(), "����ͷ��ʧ�ܣ�", Toast.LENGTH_SHORT).show();
		}
		return c;
	}

	private void updateCameraParameters() {
		if (camera != null) {
			Camera.Parameters p = camera.getParameters();

			setParameters(p);

			try {
				camera.setParameters(p);
			} catch (Exception e) {
				Camera.Size previewSize = findBestPreviewSize(p);
				p.setPreviewSize(previewSize.width, previewSize.height);
				p.setPictureSize(previewSize.width, previewSize.height);
				camera.setParameters(p);
			}
		}
	}

	/**
	 * @param p
	 */
	private void setParameters(Camera.Parameters p) {
		List<String> focusModes = p.getSupportedFocusModes();
		if (focusModes
				.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}

		long time = new Date().getTime();
		p.setGpsTimestamp(time);
		// ������Ƭ��ʽ
		p.setPictureFormat(PixelFormat.JPEG);
		Camera.Size previewSize = findPreviewSizeByScreen(p);
		p.setPreviewSize(previewSize.width, previewSize.height);
		p.setPictureSize(previewSize.width, previewSize.height);
		p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		if (getContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			camera.setDisplayOrientation(90);
			p.setRotation(90);
		}
	}

	// �������գ������������Ƭ����PictureCallback�ӿڵ�onPictureTaken����
	public void takePicture() {
		if (camera != null) {
			try {
				camera.takePicture(null, null, pictureCallback);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ���ü����¼�
	public void setOnCameraStatusListener(OnCameraStatusListener listener) {
		this.listener = listener;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {

	}

	public void start() {
		if (camera != null) {
			camera.startPreview();
		}
	}

	public void stop() {
		if (camera != null) {
			camera.stopPreview();
		}
	}

	/**
	 * ������ռ����ӿ�
	 */
	public interface OnCameraStatusListener {
		// ������ս����¼�
		void onCameraStopped(byte[] data);
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		viewWidth = MeasureSpec.getSize(widthSpec);
		viewHeight = MeasureSpec.getSize(heightSpec);
		super.onMeasure(
				MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
	}

	/**
	 * ��Ԥ����С����Ϊ��Ļ��С
	 * @param parameters
	 * @return
	 */
	private Camera.Size findPreviewSizeByScreen(Camera.Parameters parameters) {
		if (viewWidth != 0 && viewHeight != 0) {
			return camera.new Size(Math.max(viewWidth, viewHeight),
					Math.min(viewWidth, viewHeight));
		} else {
			return camera.new Size(Utils.getScreenWH(getContext()).heightPixels,
					Utils.getScreenWH(getContext()).widthPixels);
		}
	}

	/**
	 * �ҵ�����ʵ���ʾ�ֱ��� ����ֹԤ��ͼ����Σ�
	 * @param parameters
	 * @return
	 */
	private Camera.Size findBestPreviewSize(Camera.Parameters parameters) {

		// ϵͳ֧�ֵ�����Ԥ���ֱ���
		String previewSizeValueString = null;
		previewSizeValueString = parameters.get("preview-size-values");

		if (previewSizeValueString == null) {
			previewSizeValueString = parameters.get("preview-size-value");
		}

		if (previewSizeValueString == null) { // ��Щ�ֻ�����m9��ȡ����֧�ֵ�Ԥ����С ��ֱ�ӷ�����Ļ��С
			return camera.new Size(Utils.getScreenWH(getContext()).widthPixels,
					Utils.getScreenWH(getContext()).heightPixels);
		}
		float bestX = 0;
		float bestY = 0;

		float tmpRadio = 0;
		float viewRadio = 0;

		if (viewWidth != 0 && viewHeight != 0) {
			viewRadio = Math.min((float) viewWidth, (float) viewHeight)
					/ Math.max((float) viewWidth, (float) viewHeight);
		}

		String[] COMMA_PATTERN = previewSizeValueString.split(",");
		for (String prewsizeString : COMMA_PATTERN) {
			prewsizeString = prewsizeString.trim();

			int dimPosition = prewsizeString.indexOf('x');
			if (dimPosition == -1) {
				continue;
			}

			float newX = 0;
			float newY = 0;

			try {
				newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
				newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
			} catch (NumberFormatException e) {
				continue;
			}

			float radio = Math.min(newX, newY) / Math.max(newX, newY);
			if (tmpRadio == 0) {
				tmpRadio = radio;
				bestX = newX;
				bestY = newY;
			} else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
				tmpRadio = radio;
				bestX = newX;
				bestY = newY;
			}
		}

		if (bestX > 0 && bestY > 0) {
			return camera.new Size((int) bestX, (int) bestY);
		}
		return null;
	}

	/**
	 * ���ý���Ͳ������
	 *
	 * @param event
	 */
	public void focusOnTouch(MotionEvent event) {

		int[] location = new int[2];
		RelativeLayout relativeLayout = (RelativeLayout)getParent();
		relativeLayout.getLocationOnScreen(location);

		Rect focusRect = Utils.calculateTapArea(mFocusView.getWidth(),
				mFocusView.getHeight(), 1f, event.getRawX(), event.getRawY(),
				location[0], location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());
		Rect meteringRect = Utils.calculateTapArea(mFocusView.getWidth(),
				mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
				location[0], location[0] + relativeLayout.getWidth(), location[1],
				location[1] + relativeLayout.getHeight());

		Camera.Parameters parameters = camera.getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 1000));

			parameters.setFocusAreas(focusAreas);
		}

		if (parameters.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 1000));

			parameters.setMeteringAreas(meteringAreas);
		}

		try {
			camera.setParameters(parameters);
		} catch (Exception e) {
		}
		camera.autoFocus(this);
	}

	/**
	 * ���þ۽���ͼƬ
	 * @param focusView
	 */
	public void setFocusView(FocusView focusView) {
		this.mFocusView = focusView;
	}

	/**
	 * �����Զ��۽������Ҿ۽���ȦȦ��ʾ����Ļ�м�λ��
	 */
	public void setFocus() {
		if(!mFocusView.isFocusing()) {
			try {
				camera.autoFocus(this);
				mFocusView.setX((Utils.getWidthInPx(getContext())-mFocusView.getWidth()) / 2);
				mFocusView.setY((Utils.getHeightInPx(getContext())-mFocusView.getHeight()) / 2);
				mFocusView.beginFocus();
			} catch (Exception e) {
			}
		}
	}


}