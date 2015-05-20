package com.shenghangao.myprofile;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class SecondActivity extends ActionBarActivity {

    private ListView lv;
    private static final String[] name = new String[] {"Acer", "Asus", "Sony", "Toshiba"};
    private static final Integer[] pic = new Integer[] {R.drawable.acer, R.drawable.asus, R.drawable.sony, R.drawable.toshiba};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        lv = (ListView) findViewById(R.id.listview);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();

        for (int i = 0; i <= 3; ++i) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("image", pic[i]);
            map.put("picname", name[i]);
            listItem.add(map);
        }

        SimpleAdapter mSimpleAdapter = new SimpleAdapter(this, listItem, R.layout.listview, new String [] {"image", "picname"}, new int[] {R.id.image,R.id.picname});
        lv.setAdapter(mSimpleAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setTitle("You clicked No."+(position+1)+" logo.");

                Intent intent = new Intent(SecondActivity.this, FirstActivity.class);

                intent.putExtra("picno", pic[position]);
                intent.putExtra("chosenbrand", name[position]);
                startActivity(intent);
            }
        });
    }
}
