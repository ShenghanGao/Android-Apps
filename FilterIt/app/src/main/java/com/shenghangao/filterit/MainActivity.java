package com.shenghangao.filterit;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;



public class MainActivity extends Activity {
    private SurfaceView surfaceView = null;
    private SurfaceHolder surfaceHolder = null;
    private SeekBar amplitudeSeek;
    private SeekBar frequencySeek;
    private SeekBar factorSeek;
    private Switch waveForm;
    private Thread thread;
    private boolean flag = true;
    private boolean wave = false;
    private double seekBarAmplitude;
    private double seekBarFrequency;
    private double seekBarFactor;

    private double surfaceView_width;
    private double surfaceView_height;
    private double [] sine;
    private double [] cosine;
    private double [] lpfout;
    private static final String TAG = "FilterIt";

    public native double [] sineWave(double width, double height,double amplitude, double frequency);
    public native double [] cosineWave(double width, double height,double amplitude, double frequency);
    public native double [] LPF(double [] input, double factor);

    static {
        try {
            System.loadLibrary("Jnilib");
        }
        catch (UnsatisfiedLinkError ule)
        {
            Log.e(TAG, "Could not load native lib.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        amplitudeSeek = (SeekBar) findViewById(R.id.amplitudeSeek);
        frequencySeek = (SeekBar) findViewById(R.id.frequencySeek);
        factorSeek = (SeekBar) findViewById(R.id.factorSeek);
        waveForm = (Switch) findViewById(R.id.wave_switch);
        surfaceHolder = surfaceView.getHolder();


        amplitudeSeek.setOnSeekBarChangeListener(new amplitudeSeekBar());
        frequencySeek.setOnSeekBarChangeListener(new frequencySeekBar());
        factorSeek.setOnSeekBarChangeListener(new factorSeekBar());

        waveForm.setOnCheckedChangeListener(new waveChange());

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                thread = new Thread(new Runnable() {
                    public void run() {
                        while (flag) {
                            surfaceView_width = surfaceView.getWidth();
                            surfaceView_height = surfaceView.getHeight();

                            double interval = surfaceView_width / 1000;
                            double centre = surfaceView_height / 2;
                            sine = sineWave(surfaceView_width, surfaceView_height, seekBarAmplitude, seekBarFrequency);
                            cosine = cosineWave(surfaceView_width, surfaceView_height, seekBarAmplitude, seekBarFrequency);
                            Canvas canvas = surfaceHolder.lockCanvas();

                            Paint paint = new Paint();
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                            canvas.drawPaint(paint);
                            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                            paint.setColor(Color.WHITE);
                            paint.setStrokeWidth(2);
                            canvas.drawLine((float) (surfaceView_width/2), 0, (float) (surfaceView_width/2), (float) (surfaceView_height), paint);

                            if (!wave) {
                                lpfout = LPF(sine, seekBarFactor);
                                for (int i = 0; i < 1004; ++i) {
                                    paint.setStrokeWidth(2);
                                    paint.setColor(getResources().getColor(R.color.pink));
                                    canvas.drawLine((float) (i * interval), (float) (centre - sine[i]), (float) ((i + 1) * interval), (float) (centre - sine[i + 1]), paint);
                                    paint.setColor(getResources().getColor(R.color.green));
                                    canvas.drawLine((float) (i * interval), (float) (centre - lpfout[i]), (float) ((i + 1) * interval), (float) (centre - lpfout[i + 1]), paint);
                                }
                            }

                            else {
                                lpfout = LPF(cosine, seekBarFactor);
                                for (int i = 0; i < 1004; ++i) {
                                    paint.setStrokeWidth(2);
                                    paint.setColor(getResources().getColor(R.color.pink));
                                    canvas.drawLine((float) (i * interval), (float) (centre - cosine[i]), (float) ((i + 1) * interval), (float) (centre - cosine[i + 1]), paint);
                                    paint.setColor(getResources().getColor(R.color.green));
                                    canvas.drawLine((float) (i * interval), (float) (centre - lpfout[i]), (float) ((i + 1) * interval), (float) (centre - lpfout[i + 1]), paint);
                                }
                            }

                            surfaceHolder.unlockCanvasAndPost(canvas);

                        }
                    }

                });
                thread.start();

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }

        });
    }

    class amplitudeSeekBar implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            seekBarAmplitude = (double)progress/1000.0;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class frequencySeekBar implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            seekBarFrequency = (double)progress/10000.0;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class factorSeekBar implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            seekBarFactor = (double)progress/100000.0;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class waveChange implements Switch.OnCheckedChangeListener
    {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            wave = isChecked;
        }
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        flag = true;
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        flag = false;
    }

    @Override
    protected void onDestroy ()
    {
        super.onDestroy();
        flag = false;
    }

}
