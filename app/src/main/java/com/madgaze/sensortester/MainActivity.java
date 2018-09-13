package com.madgaze.sensortester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private ListView sensor_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensor_list = (ListView) findViewById(R.id.sensors_list);
        sensor_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView)view;
                Toast.makeText(MainActivity.this, tv.getText(),Toast.LENGTH_SHORT).show();
                System.out.println("############## position = "+position);
                System.out.println("############## id = "+id);
                if (tv.getText().equals("Accelerometer")){
                    //启动记录加速
                    Intent intent = new Intent(MainActivity.this, AccelerometerActivity.class);
                    startActivity(intent);
                } else if (tv.getText().equals("Gyroscope")){
                    //启动记录陀螺仪
                }  else if (tv.getText().equals("Magentic")){
                    //启动记录地磁
                    Intent intent = new Intent(MainActivity.this, MagneticActivity.class);
                    startActivity(intent);
                }
            }
        });

        PermisionUtils.verifyStoragePermissions(this);
    }
}
