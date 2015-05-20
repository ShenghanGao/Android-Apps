package com.shenghangao.rollingdice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class FaceDrawing extends Activity {

    private MyGLSurfaceView fGLView;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fGLView = new MyGLSurfaceView(this);
        fGLView.mRenderer.mode = 2;

        setContentView(fGLView);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        Button buttonChange = new Button(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width / 3, FrameLayout.LayoutParams.WRAP_CONTENT);
        buttonChange.setText("Dice");
        params.gravity = Gravity.BOTTOM | Gravity.END;
        addContentView(buttonChange, params);

        intent = new Intent(this, OpenGLES20Complete.class);

        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        fGLView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fGLView.onResume();
    }
}
