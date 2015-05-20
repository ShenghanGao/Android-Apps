package com.shenghangao.rollingdice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.Random;

public class OpenGLES20Complete extends Activity {

    private MyGLSurfaceView mGLView;
    private SensorManagerHelper sensorHelper;
    private Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGLView = new MyGLSurfaceView(this);

        setContentView(mGLView);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Button buttonRolling = new Button(this);
        buttonRolling.setText("Rolling");

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width/3, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP | Gravity.END;

        addContentView(buttonRolling, params);

        Button buttonChange = new Button(this);
        buttonChange.setText("3D Face");
        FrameLayout.LayoutParams params2  = new FrameLayout.LayoutParams(width/3, FrameLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.BOTTOM | Gravity.END;

        addContentView(buttonChange, params2);

        intent = new Intent (this, FaceDrawing.class);

        SensorManagerHelper sensorHelper = new SensorManagerHelper(this);
        buttonRolling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int max = 3;
                Random random = new Random();
                mGLView.mRenderer.turn1 = random.nextInt(max+1);
                mGLView.mRenderer.turn2 = random.nextInt(max+1);
                mGLView.mRenderer.turn3 = random.nextInt(max+1);
                Log.d("Debug", "turn1: " + mGLView.mRenderer.turn1 + " turn2: " + mGLView.mRenderer.turn2 + " turn3: " + mGLView.mRenderer.turn3);
            }
        });

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });

        sensorHelper.setOnShakeListener(new SensorManagerHelper.OnShakeListener() {
            @Override
            public void onShake() {
                int max = 3;
                Random random = new Random();
                mGLView.mRenderer.turn1 = random.nextInt(max+1);
                mGLView.mRenderer.turn2 = random.nextInt(max+1);
                mGLView.mRenderer.turn3 = random.nextInt(max+1);
                Log.d("Debug", "turn1: " + mGLView.mRenderer.turn1 + " turn2: " + mGLView.mRenderer.turn2 + " turn3: " + mGLView.mRenderer.turn3);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView.onResume();
    }
}

class MyGLSurfaceView extends GLSurfaceView {

    public final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


    }

    public final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    public float mPreviousX;
    public float mPreviousY;


}

class SensorManagerHelper implements SensorEventListener {

    private static final int SPEED_THRESHOLD = 5000;
    private static final int UPDATE_INTERVAL_TIME = 50;
    private SensorManager sensorManager;
    private Sensor sensor;
    private OnShakeListener onShakeListener;
    private Context context;
    private float lastX;
    private float lastY;
    private float lastZ;
    private long lastUpdateTime;

    public SensorManagerHelper(Context context) {
        this.context = context;
        start();
    }

    public void start() {
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (sensor != null) {
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public interface OnShakeListener {
        public void onShake();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        onShakeListener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long currentUpdateTime = System.currentTimeMillis();
        long timeInterval = currentUpdateTime - lastUpdateTime;
        if (timeInterval < UPDATE_INTERVAL_TIME) return;
        lastUpdateTime = currentUpdateTime;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float deltaX = x - lastX;
        float deltaY = y - lastY;
        float deltaZ = z - lastZ;

        lastX = x;
        lastY = y;
        lastZ = z;
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeInterval * 10000;

        if (speed >= SPEED_THRESHOLD)
            onShakeListener.onShake();
    }
}
