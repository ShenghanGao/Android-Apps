package com.shenghangao.maskme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
//import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
//import android.graphics.Shader;
//import android.graphics.drawable.ShapeDrawable;
//import android.graphics.drawable.shapes.RectShape;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class SecondActivity extends Activity {
    private Button button;
    private ImageView imageView;
    private Bitmap bitmap;
    private Face_Detection_View faceDetectionView;
    private TextView textView;
    private TextView centre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        //decorView.setSystemUiVisibility(uiOptions);
        //View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        //                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        //decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_second);

        imageView = (ImageView)findViewById(R.id.imageView);
        textView = (TextView)findViewById(R.id.confidence);
        centre = (TextView)findViewById(R.id.noface);
        Intent intent = getIntent();
        if(intent != null)
        {
            byte [] bm = intent.getByteArrayExtra("bitmap");
            bitmap = BitmapFactory.decodeByteArray(bm, 0, bm.length);
            //bitmap=intent.getParcelableExtra("bitmap");
            imageView.setImageBitmap(bitmap);
            //imageView.setImageResource(R.drawable.ic_launcher);
        }
        button = (Button) findViewById(R.id.button2);
        faceDetectionView = new Face_Detection_View(this);
    }

    public void pressButton2(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public class Face_Detection_View extends View {
        private Bitmap background_image;
        private static final int MAX_FACES = 10;
        private FaceDetector.Face[] faces;
        private int face_count;
        private Canvas canvas;

        private PointF tmp_point = new PointF();
        private Paint tmp_paint = new Paint();

        private PointF leftEye = new PointF();
        private PointF rightEye = new PointF();

        private float [] confidenceFactor;

        String confidenceFactorLine = "";

        public Face_Detection_View(Context context) {
            super(context);
            updateImage(bitmap);
        }

        public void updateImage(Bitmap bitmap_image) {
            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
            //background_image = bitmap_image;
            background_image = bitmap_image.copy(Bitmap.Config.RGB_565, true);
            FaceDetector face_detector = new FaceDetector(
                    background_image.getWidth(), background_image.getHeight(), MAX_FACES);
            faces = new FaceDetector.Face[MAX_FACES];
            face_count = face_detector.findFaces(background_image, faces);
            //button.setText("Face Count: " + String.valueOf(face_count));
            canvas = new Canvas(background_image);
            onDraw(canvas);
            imageView.setImageBitmap(background_image);
            textView.setText(confidenceFactorLine);
            if (face_count == 0) centre.setText("No face detected, try again!");
        }

        public void onDraw(Canvas canvas) {
            canvas.drawBitmap(background_image,0, 0, null);
            confidenceFactor = new float[face_count];
            for (int i=0; i<face_count; ++i) {
                FaceDetector.Face face = faces[i];
                tmp_paint.setColor(getResources().getColor(R.color.yellow));
                tmp_paint.setAlpha(100);
                tmp_paint.setStyle(Paint.Style.STROKE);
                tmp_paint.setStrokeWidth(6);
                face.getMidPoint(tmp_point);
                canvas.drawCircle(tmp_point.x, tmp_point.y, face.eyesDistance(), tmp_paint);
                tmp_paint.setColor(getResources().getColor(R.color.lightblue));
                leftEye.x = tmp_point.x - face.eyesDistance()/2; leftEye.y = tmp_point.y;
                rightEye.x = tmp_point.x + face.eyesDistance()/2; rightEye.y = tmp_point.y;
                //canvas.drawCircle(leftEye.x, leftEye.y, face.eyesDistance()/2, tmp_paint);
                //canvas.drawCircle(rightEye.x, rightEye.y, face.eyesDistance()/2, tmp_paint);
                //Shader mShader = new LinearGradient(0,0,100,100, new int[] {Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW}, null, Shader.TileMode.REPEAT);
                //ShapeDrawable sd;
                //sd = new ShapeDrawable(new RectShape());
                //sd.setBounds((int) (leftEye.x-face.eyesDistance()/3), (int) (leftEye.y-face.eyesDistance()/3), (int) (leftEye.x+face.eyesDistance()/3), (int) (leftEye.y+face.eyesDistance()/3));
                //sd.draw(canvas);
                canvas.drawRect(leftEye.x-face.eyesDistance()/3, leftEye.y-face.eyesDistance()/3, leftEye.x+face.eyesDistance()/3, leftEye.y+face.eyesDistance()/3, tmp_paint);
                canvas.drawRect(rightEye.x-face.eyesDistance()/3, rightEye.y-face.eyesDistance()/3, rightEye.x+face.eyesDistance()/3, rightEye.y+face.eyesDistance()/3, tmp_paint);
                tmp_paint.setColor(Color.GREEN);
                canvas.drawLine(tmp_point.x, tmp_point.y, tmp_point.x-face.eyesDistance()/3, (float) (tmp_point.y+face.eyesDistance()/1.5), tmp_paint);
                canvas.drawLine(tmp_point.x, tmp_point.y, tmp_point.x+face.eyesDistance()/3, (float) (tmp_point.y+face.eyesDistance()/1.5), tmp_paint);
                canvas.drawLine(tmp_point.x-face.eyesDistance()/3, (float) (tmp_point.y+face.eyesDistance()/1.5), tmp_point.x+face.eyesDistance()/3, (float) (tmp_point.y+face.eyesDistance()/1.5), tmp_paint);

                confidenceFactor[i] = face.confidence();
                confidenceFactorLine += "C" + (i+1) + ": " + confidenceFactor[i] + "\n";
            }
        }
    }
}
