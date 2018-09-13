package com.madgaze.sensortester;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MagneticActivity extends Activity implements View.OnClickListener,SensorEventListener {
    private Button btnNext;
    private Button btnSave;
    private Button btnStop;
    private Button btnListRecord;
    private TextView txtviewMagHint;
    private TextView txtviewMagCoordinate;
    private int coordinate_index;
    public static final int RECORD_NOT_START = 0;
    public static final int RECORD_RECORDING = 1;
    public static final int RECORD_STOPED = 2;
    private int status; //0为未开始，1为录制状态，2为停止
    public int mOrientation;
    private SensorManager mSensorManager;

    public class MagneticCoordinate{
        public float x;
        public float y;
        public float z;
    }

    public static final int MAX_COORDINATE = 10;
    public List<MagneticCoordinate> mag = new ArrayList<>();
    public List<RecordBean> record_list = new ArrayList<>();
    public List<RecordBean> saved_list = new ArrayList<>();
    public String record_path;

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_mag_next:
                if(status == RECORD_RECORDING){
                    Toast.makeText(this, R.string.mag_recording,Toast.LENGTH_SHORT).show();
                } else {
                    coordinate_index = 0;
                    status = RECORD_RECORDING;
                    mag.clear();
                    String coordinateTxt = "";
                    txtviewMagCoordinate.setText(coordinateTxt);
                }
                break;
            case R.id.btn_mag_stop:
                status = RECORD_STOPED;
                break;
            case R.id.btn_mag_save:
                CsvOperator.writeCsv(record_list, record_path);
                break;
            case R.id.btn_mag_list_records:
                CsvOperator.readCsv(saved_list, record_path);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetic);

        btnNext = (Button)findViewById(R.id.btn_mag_next);
        btnSave = (Button)findViewById(R.id.btn_mag_save);
        btnStop = (Button)findViewById(R.id.btn_mag_stop);
        btnListRecord = (Button)findViewById(R.id.btn_mag_list_records);
        txtviewMagHint = (TextView)findViewById(R.id.mag_hint_textview);
        txtviewMagHint.setTextColor(this.getResources().getColor(R.color.black,null));
        txtviewMagCoordinate = (TextView)findViewById(R.id.mag_coordinate_textview);
        txtviewMagCoordinate.setTextColor(this.getResources().getColor(R.color.red, null));

        btnNext.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnListRecord.setOnClickListener(this);

        coordinate_index = 0;
        mOrientation = 0;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
        record_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "mag_records" + ".csv";

        status = RECORD_NOT_START;
    }
    @Override
    protected  void onPause(){
        super.onPause();

        status = RECORD_STOPED;
    }
    @Override
    protected void onResume(){
        super.onResume();
    }

    protected void onDestory(){
        super.onDestroy();
        if (mSensorManager != null){
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //没有开始，则不进行录制
        if(status != RECORD_RECORDING){
            return ;
        }
        //判断传感器类别
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                if (coordinate_index < MAX_COORDINATE){
                    MagneticCoordinate cord = new MagneticCoordinate();
                    cord.x = event.values[0];
                    cord.y = event.values[1];
                    cord.z = event.values[2];
                    mag.add(cord);
                    coordinate_index ++;
                } else {
                    float x,y,z;
                    float av_x,av_y,av_z;
                    int size;
                    x = mag.get(0).x;
                    y = mag.get(0).y;
                    z = mag.get(0).z;
                    if(mag.size() < MAX_COORDINATE){
                        size = mag.size();
                    } else {
                        size = MAX_COORDINATE;
                    }
                    for(int i = 1; i < mag.size() && i < MAX_COORDINATE; i++){
                        x += mag.get(i).x;
                        y += mag.get(i).y;
                        z += mag.get(i).z;
                    }

                    av_x = x/size;
                    av_y = y/size;
                    av_z = z/size;

                    String coordinateTxt = "Orientation: "+mOrientation+", X: "+av_x+", Y: "+av_y+", Z: "+av_z;
                    txtviewMagCoordinate.setText(coordinateTxt);

                    status = RECORD_STOPED;

                    RecordBean record = new RecordBean();
                    record.orientation = mOrientation;
                    record.x = av_x;
                    record.y = av_y;
                    record.z = av_z;
                    record_list.add(record);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
