package com.shenghangao.myprofile;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class FirstActivity extends ActionBarActivity {

    private ImageView image;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        Intent intent = getIntent();
        int picchosen = intent.getIntExtra("picno", R.drawable.android);
        String chosenbrand = intent.getStringExtra("chosenbrand");
        image = (ImageView) findViewById(R.id.pic);
        image.setImageResource(picchosen);
        if (chosenbrand == null) chosenbrand = "Select Image";
        name = (TextView) findViewById(R.id.brand);
        name.setText(chosenbrand);
    }

    public void clickPic (View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
}
