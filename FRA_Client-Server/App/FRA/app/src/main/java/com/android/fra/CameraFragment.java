package com.android.fra;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.longsh.optionframelibrary.OptionMaterialDialog;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CameraFragment extends Fragment implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";
    private String currentUid;
    private boolean hasCaptured = false;
    private int captureMode;
    private int ScreenHeight;
    private int ScreenWidth;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;
    private static final int MAX_PREVIEW_WIDTH = 1080;
    private static final int MAX_PREVIEW_HEIGHT = 1920;
    private String mCameraId;
    private AutoFitTextureView mTextureView;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private ImageReader mImageReader;
    private File mFile;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private int mState = STATE_PREVIEW;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private boolean mFlashSupported;
    private int mSensorOrientation;
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static Integer mFaceDetectMode;
    private static Rect cRect;
    private static Size cPixelSize;
    private int FrameCount = 0;
    private int FrameInterval = 50;
    private List<com.android.fra.db.Face> faces;
    private String attendanceTime;
    private boolean isSetTime;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;
    private String currentPid;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
        }

    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    FrameCount++;
                    Face face[] = result.get(result.STATISTICS_FACES);
                    if (face.length > 0 && hasCaptured == false && !((CameraActivity) getActivity()).getOpenDrawerLayout()) {
                        int[] face_rec = drawRectangle(face[0]);
                        if (FrameCount % FrameInterval == 0 && face_rec[0] != face_rec[2]) {
                            Bitmap bitmap_get = mTextureView.getBitmap();
                            bitmap_get = Bitmap.createBitmap(bitmap_get, face_rec[0], face_rec[1], face_rec[2] - face_rec[0], face_rec[3] - face_rec[1]);
                            String feature = new LBP().getFeature(bitmap_get, 8, 8);
                            if (captureMode == 0) {
                                Calendar calendar = Calendar.getInstance();
                                String captureUid = new LBP().getFaceOwner(feature, currentPid, 120);
                                hasCaptured = true;
                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH) + 1;
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                int minute = calendar.get(Calendar.MINUTE);
                                int second = calendar.get(Calendar.SECOND);
                                final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(getActivity());
                                if (isSetTime && (hour < startHour || hour > endHour || (hour == startHour && minute < startMinute) || (hour == endHour && minute > endMinute))) {
                                    mMaterialDialog.setTitle("签到失败").setTitleTextColor(R.color.noFaceOwner).setTitleTextSize((float) 22.5)
                                            .setMessage("当前时间不在签到时间区间内").setMessageTextSize((float) 16.5)
                                            .setPositiveButton("确定", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mMaterialDialog.dismiss();
                                                    hasCaptured = false;

                                                }
                                            })
                                            .setPositiveButtonTextColor(R.color.noFaceOwner)
                                            .show();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mMaterialDialog.dismiss();
                                            hasCaptured = false;
                                        }
                                    }, 3000);
                                } else {
                                    if (!captureUid.equals("NoFaceOwner")) {
                                        faces = LitePal.where("uid = ? and pid = ?", captureUid, currentPid).find(com.android.fra.db.Face.class);
                                        String showMonth;
                                        if (month < 10) {
                                            showMonth = "0" + String.valueOf(month);
                                        } else {
                                            showMonth = String.valueOf(month);
                                        }
                                        String showDay;
                                        if (day < 10) {
                                            showDay = "0" + String.valueOf(day);
                                        } else {
                                            showDay = String.valueOf(day);
                                        }
                                        String showHour;
                                        if (hour < 10) {
                                            showHour = "0" + String.valueOf(hour);
                                        } else {
                                            showHour = String.valueOf(hour);
                                        }
                                        String showMinute;
                                        if (minute < 10) {
                                            showMinute = "0" + String.valueOf(minute);
                                        } else {
                                            showMinute = String.valueOf(minute);
                                        }
                                        String showSecond;
                                        if (second < 10) {
                                            showSecond = "0" + String.valueOf(second);
                                        } else {
                                            showSecond = String.valueOf(second);
                                        }
                                        attendanceTime = year + "." + showMonth + "." + showDay + " " + showHour + ":" + showMinute + ":" + showSecond;
                                        detectInBackground();
                                    } else {
                                        mMaterialDialog.setTitle("签到失败").setTitleTextColor(R.color.noFaceOwner).setTitleTextSize((float) 22.5)
                                                .setMessage("您尚未注册").setMessageTextSize((float) 16.5)
                                                .setPositiveButton("确定", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        mMaterialDialog.dismiss();
                                                        hasCaptured = false;
                                                    }
                                                })
                                                .setPositiveButtonTextColor(R.color.noFaceOwner)
                                                .show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                mMaterialDialog.dismiss();
                                                hasCaptured = false;
                                            }
                                        }, 3000);
                                    }
                                }
                            } else if (captureMode == 1) {
                                hasCaptured = true;
                                com.android.fra.db.Face updateFace = new com.android.fra.db.Face();
                                updateFace.setFeature(feature);
                                updateFace.updateAll("uid = ? and pid = ?", currentUid, currentPid);
                                registerInBackground();
                            }
                        }
                    } else {
                        clearRectangle();
                    }
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);

                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    public int[] drawRectangle(Face face) {

        boolean catchFlag = false;
        Rect bounds = face.getBounds();
        Paint mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.faceDetector));
        mPaint.setStrokeWidth(6f);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5f);
        Canvas canvas = new Canvas();
        canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        float scaleWidth = canvas.getHeight() * 1.0f / cPixelSize.getWidth();
        float scaleHeight = canvas.getWidth() * 1.0f / cPixelSize.getHeight();

        int l = (int) (bounds.left * scaleWidth);
        int t = (int) (bounds.top * scaleHeight);
        int r = (int) (bounds.right * scaleWidth);
        int b = (int) (bounds.bottom * scaleHeight);

        int left = canvas.getWidth() - b - 90;
        int top = mTextureView.getHeight() - r - 90;
        int right = canvas.getWidth() - t - 70;
        int bottom = mTextureView.getHeight() - l - 50;

        double[] Location = getLocation();
        if (left > Location[0] && top > Location[1] && right < Location[2] && bottom < Location[3]) {
            canvas.drawLine(left, top, left + (int) ((right - left) * 0.1), top, mPaint);
            canvas.drawLine(left + 2, top, left + 2, top + (int) ((bottom - top) * 0.1), mPaint);
            canvas.drawLine(right, top, right - (int) ((right - left) * 0.1), top, mPaint);
            canvas.drawLine(right - 3, top, right - 3, top + (int) ((bottom - top) * 0.1), mPaint);
            canvas.drawLine(left + 2, bottom, left + 2, bottom - (int) ((bottom - top) * 0.1), mPaint);
            canvas.drawLine(left, bottom, left + (int) ((right - left) * 0.1), bottom, mPaint);
            canvas.drawLine(right, bottom, right - (int) ((right - left) * 0.1), bottom, mPaint);
            canvas.drawLine(right - 3, bottom, right - 3, bottom - (int) ((bottom - top) * 0.1), mPaint);
            catchFlag = true;
        }
        surfaceHolder.unlockCanvasAndPost(canvas);
        int[] rec_coordinate;
        if (catchFlag == true) {
            rec_coordinate = new int[]{left, top, right, bottom};
        } else {
            rec_coordinate = new int[]{0, 0, 0, 0};
        }
        return rec_coordinate;
    }

    public void clearRectangle() {
        Canvas canvas = new Canvas();
        canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private double[] getLocation() {
        double diameter = ScreenHeight * 0.5;
        int Diameter;
        if ((int) diameter / 2 != 0) {
            Diameter = (int) diameter - 1;
        } else {
            Diameter = (int) diameter;
        }
        double px = ScreenWidth / 2;
        double py = ScreenHeight * 0.06 + Diameter / 2;
        double location[] = new double[4];
        location[0] = px - Diameter * 0.5 * 0.7;
        location[1] = ScreenHeight * 0.06 + Diameter * 0.5 * 0.3;
        location[2] = px + Diameter * 0.5 * 0.7;
        location[3] = py + Diameter * 0.5 * 0.7;
        return location;
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<>();
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }


    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        view.findViewById(R.id.surfaceView_show_rectangle).setOnClickListener(this);
        surfaceview = (SurfaceView) view.findViewById(R.id.surfaceView_show_rectangle);
        surfaceview.setZOrderOnTop(true);
        surfaceview.getHolder().setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder = surfaceview.getHolder();

        CameraActivity cameraActivity = (CameraActivity) getActivity();
        currentUid = cameraActivity.getCurrentUid();
        captureMode = cameraActivity.getCaptureMode();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        ScreenHeight = dm.heightPixels;
        ScreenWidth = dm.widthPixels;

        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        isSetTime = pref.getBoolean("is_set_time", false);
        if (isSetTime) {
            startHour = pref.getInt("startHour", 0);
            startMinute = pref.getInt("startMinute", 0);
            endHour = pref.getInt("endHour", 0);
            endMinute = pref.getInt("endMinute", 0);
        }
        currentPid = pref.getString("currentPid", "");

        Button button = (Button) view.findViewById(R.id.cancel_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity != null) {
                    LitePal.deleteAll(com.android.fra.db.Face.class, "uid = ? and pid = ?", currentUid, currentPid);
                    if (captureMode == 0) {
                        activity.finish();
                    } else {
                        Intent intent = new Intent(activity, RegisterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            }
        });
        TextView textView = (TextView) view.findViewById(R.id.hint);
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        textView.measure(spec, spec);
        int measuredWidthTicketNum = textView.getMeasuredWidth();

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins((int) ((ScreenWidth - measuredWidthTicketNum) / 2), (int) (ScreenHeight * 0.811), ScreenWidth, (int) (ScreenHeight * 0.85));
        textView.setLayoutParams(layoutParams);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new PermissionDeniedDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                new PermissionDeniedDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
                return;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                        ImageFormat.JPEG, 2);
                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);

                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraId = cameraId;

                cRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                cPixelSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);

                int[] FD = characteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
                int maxFD = characteristics.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);

                if (FD.length > 0) {
                    List<Integer> fdList = new ArrayList<>();
                    for (int FaceD : FD) {
                        fdList.add(FaceD);
                    }
                    if (maxFD > 0) {
                        mFaceDetectMode = Collections.max(fdList);
                    }
                }
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera("1", mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            Surface surface = new Surface(texture);

            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, mFaceDetectMode);

            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                return;
                            }

                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                setAutoFlash(mPreviewRequestBuilder);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private void runPrecaptureSequence() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillPicture() {
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            setAutoFlash(captureBuilder);

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {

                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void unlockFocus() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    public static class PermissionDeniedDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.denied_permission)
                    .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .create();
        }
    }

    Handler registerHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    hasCaptured = true;
                    final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(getActivity());
                    mMaterialDialog.setTitle("操作成功").setTitleTextColor(R.color.colorPrimary).setMessage("已成功添加面孔").setMessageTextSize((float) 16.5)
                            .setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMaterialDialog.dismiss();
                                    Intent intent = new Intent(getActivity(), RegisterActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            })
                            .setPositiveButtonTextColor(R.color.colorPrimary)
                            .show();
                    break;
                case 1:
                    hasCaptured = true;
                    final OptionMaterialDialog errorDialog = new OptionMaterialDialog(getActivity());
                    errorDialog.setTitle("操作失败").setTitleTextColor(R.color.noFaceOwner).setMessage("无法连接至服务器").setMessageTextSize((float) 16.5)
                            .setPositiveButton("重试", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    errorDialog.dismiss();
                                    registerInBackground();
                                }
                            })
                            .setPositiveButtonTextColor(R.color.noFaceOwner)
                            .setNegativeButton("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        LitePal.deleteAll(com.android.fra.db.Face.class, "uid = ? and pid = ?", currentUid, currentPid);
                                        Intent intent = new Intent(activity, RegisterActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButtonTextColor(R.color.noFaceOwner)
                            .show();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    Handler detectHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(getActivity());
            switch (msg.what) {
                case 0:
                    postInBackground();
                    break;
                case 1:
                    mMaterialDialog.setTitle("签到失败").setTitleTextColor(R.color.noFaceOwner).setTitleTextSize((float) 22.5)
                            .setMessage("您尚未注册").setMessageTextSize((float) 16.5)
                            .setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMaterialDialog.dismiss();
                                    hasCaptured = false;
                                }
                            })
                            .setPositiveButtonTextColor(R.color.noFaceOwner)
                            .show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMaterialDialog.dismiss();
                            hasCaptured = false;
                        }
                    }, 3000);
                    break;
                case 2:
                    faces = LitePal.where("uid = ? and pid = ?", faces.get(0).getUid(), currentPid).find(com.android.fra.db.Face.class);
                    mMaterialDialog.setTitle("您已签到").setTitleTextColor(R.color.noFaceOwner).setTitleTextSize((float) 22.5)
                            .setMessage("工号: " + faces.get(0).getUid() + "\n" + "姓名: " + faces.get(0).getName() + "\n" + "所属部门: " + faces.get(0).getDepartment()
                                    + "\n" + "上次签到: " + faces.get(0).getCurrentCheckTime()).setMessageTextSize((float) 16.5)
                            .setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMaterialDialog.dismiss();
                                    hasCaptured = false;
                                }
                            })
                            .setPositiveButtonTextColor(R.color.noFaceOwner)
                            .show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMaterialDialog.dismiss();
                            hasCaptured = false;
                        }
                    }, 3000);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    Handler postHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            final OptionMaterialDialog mMaterialDialog = new OptionMaterialDialog(getActivity());
            switch (msg.what) {
                case 0:
                    mMaterialDialog.setTitle("签到成功").setTitleTextColor(R.color.colorPrimary).setTitleTextSize((float) 22.5)
                            .setMessage("工号: " + faces.get(0).getUid() + "\n" + "姓名: " + faces.get(0).getName() + "\n" + "所属部门: " + faces.get(0).getDepartment()
                                    + "\n" + "签到时间: " + attendanceTime).setMessageTextSize((float) 16.5)
                            .setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMaterialDialog.dismiss();
                                    hasCaptured = false;

                                }
                            })
                            .setPositiveButtonTextColor(R.color.colorPrimary)
                            .show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMaterialDialog.dismiss();
                            hasCaptured = false;
                        }
                    }, 3000);
                    break;
                case 1:
                    mMaterialDialog.setTitle("签到失败").setTitleTextColor(R.color.noFaceOwner).setTitleTextSize((float) 22.5)
                            .setMessage("未连接至服务器").setMessageTextSize((float) 16.5)
                            .setPositiveButton("确定", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMaterialDialog.dismiss();
                                    hasCaptured = false;
                                }
                            })
                            .setPositiveButtonTextColor(R.color.noFaceOwner)
                            .show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMaterialDialog.dismiss();
                            hasCaptured = false;
                        }
                    }, 3000);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void registerInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<com.android.fra.db.Face> face = LitePal.where("uid = ? and pid = ?", currentUid, currentPid).find(com.android.fra.db.Face.class);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(8, TimeUnit.SECONDS)
                        .build();
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                String modTime = String.valueOf(year) + "." + String.valueOf(month) + "." + String.valueOf(day) + " " + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                com.android.fra.db.Face updateFace = new com.android.fra.db.Face();
                updateFace.setModTime(modTime);
                updateFace.updateAll("uid = ? and pid = ?", currentUid, currentPid);
                RequestBody requestBody = new FormBody.Builder()
                        .add("pid", currentPid)
                        .add("uid", currentUid)
                        .add("name", face.get(0).getName())
                        .add("gender", face.get(0).getGender())
                        .add("phone", face.get(0).getPhone())
                        .add("department", face.get(0).getDepartment())
                        .add("post", face.get(0).getPost())
                        .add("email", face.get(0).getEmail())
                        .add("modTime", modTime)
                        .build();
                Request request = new Request.Builder()
                        .url("http://10.10.19.134:3000/app/add")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Message msg = new Message();
                    if (responseData.equals("ok")) {
                        msg.what = 0;
                    } else {
                        msg.what = 1;
                    }
                    registerHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Message msg = new Message();
                        msg.what = 1;
                        registerHandler.sendMessage(msg);
                    }
                    if (e instanceof ConnectException) {
                        Message msg = new Message();
                        msg.what = 1;
                        registerHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    private void detectInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("pid", currentPid)
                        .build();
                Request request = new Request.Builder()
                        .url("http://10.10.19.134:3000/app/query")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Message msg = new Message();
                    if (!responseData.equals("error")) {
                        Gson gson = new Gson();
                        List<com.android.fra.db.Face> serverFaceList = gson.fromJson(responseData, new TypeToken<List<com.android.fra.db.Face>>() {
                        }.getType());
                        int existence = 0;
                        for (com.android.fra.db.Face serverFace : serverFaceList) {
                            if (faces.get(0).getUid().equals(serverFace.getUid())) {
                                if (!faces.get(0).getModTime().equals(serverFace.getModTime())) {
                                    serverFace.updateAll("uid = ? and pid = ?", serverFace.getUid(), currentPid);
                                }
                                if (serverFace.getCheckStatus().equals("1")) {
                                    serverFace.updateAll("uid = ? and pid = ?", serverFace.getUid(), currentPid);
                                    existence = 1;
                                    msg.what = 2;
                                    break;
                                }
                                existence = 1;
                            }
                            msg.what = 0;
                        }
                        if (existence == 0) {
                            LitePal.deleteAll(com.android.fra.db.Face.class, "uid = ? and pid = ?", faces.get(0).getUid(), currentPid);
                            msg.what = 1;
                        }
                    } else {
                        msg.what = 3;
                    }
                    detectHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Message msg = new Message();
                        msg.what = 2;
                        detectHandler.sendMessage(msg);
                    }
                    if (e instanceof ConnectException) {
                        Message msg = new Message();
                        msg.what = 2;
                        detectHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    private void postInBackground() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(8, TimeUnit.SECONDS)
                        .readTimeout(8, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBody = new FormBody.Builder()
                        .add("pid", currentPid)
                        .add("uid", faces.get(0).getUid())
                        .add("check_time", attendanceTime)
                        .build();
                Request request = new Request.Builder()
                        .url("http://10.10.19.134:3000/app/check")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    Message msg = new Message();
                    if (responseData.equals("ok")) {
                        msg.what = 0;
                    } else {
                        msg.what = 1;
                    }
                    postHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        Message msg = new Message();
                        msg.what = 1;
                        postHandler.sendMessage(msg);
                    }
                    if (e instanceof ConnectException) {
                        Message msg = new Message();
                        msg.what = 1;
                        postHandler.sendMessage(msg);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {

    }

}