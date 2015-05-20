package com.shenghangao.maskme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
//import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends Activity {
    private  static final String TAG = "MainAct";
    private SurfaceView surfaceView;
    private Camera camera;
    private boolean preview;
    private Button button;
    private Bitmap bitmap;
    private int buttonClickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //Resources resources = getResources();
        //int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");

        //Window window = getWindow();
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //View decorView = getWindow().getDecorView();

        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        //                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        //decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);

        //Intent intent = new Intent();
        //intent.setAction("android.media.action.STILL_IMAGE_CAMERA");
        //startActivity(intent);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        button = (Button) findViewById(R.id.button);

        //Integer height = 0;
        //if (resourceId > 0) height = resources.getDimensionPixelSize(resourceId);
        //button.setText(height.toString());
    }


    private final class SurfaceCallback implements SurfaceHolder.Callback{

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open();
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                //Display display = wm.getDefaultDisplay();
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size size = sizes.get(0);
                parameters.setPreviewSize(size.width, size.height);
                //setPreviewSize(display.getWidth(), display.getHeight());
                //parameters.setPreviewFrameRate(3);
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.set("jpeg-quality", 100);
                parameters.setPictureSize(size.width, size.height);
                camera.setDisplayOrientation(90);
                camera.setParameters(parameters);
                camera.setPreviewDisplay(surfaceView.getHolder());
                camera.startPreview();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null)
                if (preview) camera.stopPreview();
                camera.release();
        }
    }
        public void pressButton(View v) {
            ++buttonClickCount;
            if (buttonClickCount == 1) {
                if (camera != null) {
                    camera.autoFocus(null);
                    camera.takePicture(null, null, new TakePictureCallback());
                }
            }
            if (buttonClickCount == 2) {
                Intent intent = new Intent(this, SecondActivity.class);
                ByteArrayOutputStream bm = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bm);
                byte [] bitmapByte = bm.toByteArray();
                intent.putExtra("bitmap", bitmapByte);
                startActivity(intent);
                finish();
            }
    }

    private final class TakePictureCallback implements Camera.PictureCallback{
        @Override
        public void onPictureTaken(byte [] data, Camera camera) {
            try {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                File file = new File(Environment.getExternalStorageDirectory(), "test.jpg");
                FileOutputStream outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Picture_From_Mask_Me", "Description");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            button.setText("Add mask");
        }
    }

}
