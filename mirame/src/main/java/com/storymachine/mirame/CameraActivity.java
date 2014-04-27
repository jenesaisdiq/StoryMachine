package com.storymachine.mirame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private String mCurrentPhotoPath = null;
    private File pictureFile = null;
    //private TextView textBox = (TextView)findViewById(R.id.textbox);
    private int scannedFlag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        safeCameraOpen(1);
//        try {
//            mCamera = getCameraInstance();
//        }
//        catch(Exception e){}
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        final RelativeLayout preview = (RelativeLayout) findViewById(R.id.camera_preview);
        //ImageView preview = (ImageView) findViewById(R.id.photoReview);
        preview.addView(mPreview,0);
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // get an image from the camera
                    final TextView textBox = (TextView)findViewById(R.id.textbox);
                    textBox.setText("3");
                    new CountDownTimer(4000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            textBox.setText(""+millisUntilFinished / 1000);
                            preview.invalidate();
                        }
                        public void onFinish() {
                            textBox.setText("");
                            Log.i("TAG", "Taking picture...");
                            mCamera.takePicture(null, null, mPicture);
                        }
                    }.start();
                    new CountDownTimer(6000, 6000) {
                        public void onTick(long millisUntilFinished) {
                            //textBox.setText(""+millisUntilFinished / 1000);
                        }
                        public void onFinish() {
//                            setContentView(R.layout.activity_main);
//                            ImageView jpgView = (ImageView)findViewById(R.id.photoReview);
//                            jpgView.setVisibility(View.VISIBLE);
//                            showLastPhoto();
                        }
                    }.start();
                    new CountDownTimer(9000, 9000) {
                        public void onTick(long millisUntilFinished) {
                            //textBox.setText(""+millisUntilFinished / 1000);
                        }
                        public void onFinish() {
//                            setContentView(R.layout.activity_main);
//                            ImageView jpgView = (ImageView)findViewById(R.id.photoReview);
//                            jpgView.setVisibility(View.INVISIBLE);
//                            jpgView.invalidate();

                            mCamera.startPreview();
                        }
                    }.start();
                    //textBox.setText("1");
                    //mCamera.startPreview();
                    //AsyncTaskRunner runner = new AsyncTaskRunner();
                    //runner.execute("1000");

                }
            }
        );
        if(scannedFlag == 1){
            Log.i("TAG", "Trying showLast...");
            showLastPhoto();
            scannedFlag = 0;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

        if(keyCode == KeyEvent.KEYCODE_R){
            mCamera.takePicture(null, null, mPicture);
            try{Thread.sleep(1000);} catch (Exception e){}
            showLastPhoto();
        }
        else if(keyCode == KeyEvent.KEYCODE_B) {}
        else if(keyCode == KeyEvent.KEYCODE_Y) {}

        return super.onKeyDown(keyCode, msg);
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            try {
                pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.i(getString(R.string.app_name), "pictureFile was null");
                    return;
                }
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                mCurrentPhotoPath = pictureFile.getAbsolutePath();
                scanFile(mCurrentPhotoPath);
                Log.i(getString(R.string.app_name), "File path: " + mCurrentPhotoPath);
            } catch (FileNotFoundException e) {
                Log.e(getString(R.string.app_name), "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e(getString(R.string.app_name), "Error accessing file: " + e.getMessage());
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "Check permissions?: " + e.getMessage());
            }

            //mCamera.startPreview();

        }
    };

    private void showLastPhoto(){
        //try{Thread.sleep(1000);} catch (Exception e){Log.i("TAG", "Didn't Sleep.");}
        try {
            setContentView(R.layout.activity_main);
            ImageView jpgView = (ImageView)findViewById(R.id.photoReview);
            //Uri external = Uri.fromFile(pictureFile);
            //jpgView.setImageURI(null);
            //jpgView.setImageURI(external);
            jpgView.setRotation(180);

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath) ;  ///-----------------------------
            jpgView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "Thrown in showLastPhoto" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(CameraActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                        scannedFlag = 1;

                    }
                });
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance-----------------------------hardcoded to second one!

        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            Camera.Parameters param;
            param = mCamera.getParameters();
            //modify parameter

            //param.setRotation(90);
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(param);

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.e(getString(R.string.app_name), "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            Camera.Parameters param;
            param = mCamera.getParameters();
            //modify parameter

//            param.setRotation(0);
//            mCamera.setParameters(param);

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.e(getString(R.string.app_name), "Error starting camera preview: " + e.getMessage());
            }
        }
    }


    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e("StoryMachine", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }


    private void safeCameraOpen(int id) {

        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

   /* @Override
    protected void onPause() {
        super.onPause();
        mCamera.release();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera.startPreview();
    }*/


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/



}

