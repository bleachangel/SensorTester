package com.madgaze.sensortester;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AccelerometerActivity extends Activity implements SensorEventListener, View.OnClickListener{
    private Button btnNext;
    private Button btnSave;
    private Button btnStop;
    private Button btnListRecord;
    private TextView txtviewAccelHint;
    private TextView txtviewCoordinate;
    private int coordinate_index;
    public static final int RECORD_NOT_START = 0;
    public static final int RECORD_RECORDING = 1;
    public static final int RECORD_STOPED = 2;
    private int status; //0为未开始，1为录制状态，2为停止
    public int mOrientation;
    private SensorManager mSensorManager;
    private AlbumOrientationEventListener orientationListener;

    public class AccelerometerCoordinate{
        public float x;
        public float y;
        public float z;
    }

    public static final int MAX_COORDINATE = 10;
    public List<AccelerometerCoordinate> accel = new ArrayList<>();
    public List<RecordBean> record_list = new ArrayList<>();
    public List<RecordBean> saved_list = new ArrayList<>();
    public String record_path;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        btnNext = (Button)findViewById(R.id.btn_next);
        btnSave = (Button)findViewById(R.id.btn_save);
        btnStop = (Button)findViewById(R.id.btn_stop);
        btnListRecord = (Button)findViewById(R.id.btn_recode_list);
        txtviewAccelHint = (TextView)findViewById(R.id.accel_hint_textview);
        txtviewAccelHint.setTextColor(this.getResources().getColor(R.color.black,null));
        txtviewCoordinate = (TextView)findViewById(R.id.accel_coordinate_textview);
        txtviewCoordinate.setTextColor(this.getResources().getColor(R.color.red, null));

        btnNext.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnListRecord.setOnClickListener(this);

        coordinate_index = 0;
        mOrientation = 0;

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
        record_path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "accelerometer_records" + ".csv";
        //record_path = this.getFilesDir().getAbsolutePath() + File.separator + "accelerometer_records" + ".csv";
        status = RECORD_NOT_START;
        orientationListener = new AlbumOrientationEventListener(this);
        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable();
        } else {
            orientationListener.disable();
        }
    }
    protected  void onPause(){
        super.onPause();

        status = RECORD_STOPED;
        orientationListener.disable();
    }
    @Override
    protected void onResume(){
        super.onResume();

        orientationListener.enable();
    }

    protected void onDestory(){
        super.onDestroy();
        if (mSensorManager != null){
            mSensorManager.unregisterListener(this);
        }

        orientationListener.disable();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //没有开始，则不进行录制
        if(status != RECORD_RECORDING){
            return ;
        }
        //判断传感器类别
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (coordinate_index < MAX_COORDINATE){
                    AccelerometerCoordinate cord = new AccelerometerCoordinate();
                    cord.x = event.values[0];
                    cord.y = event.values[1];
                    cord.z = event.values[2];
                    accel.add(cord);
                    coordinate_index ++;
                } else {
                    float x,y,z;
                    float av_x,av_y,av_z;
                    int size;
                    x = accel.get(0).x;
                    y = accel.get(0).y;
                    z = accel.get(0).z;
                    if(accel.size() < MAX_COORDINATE){
                        size = accel.size();
                    } else {
                        size = MAX_COORDINATE;
                    }
                    for(int i = 1; i < accel.size() && i < MAX_COORDINATE; i++){
                        x += accel.get(i).x;
                        y += accel.get(i).y;
                        z += accel.get(i).z;
                    }

                    av_x = x/size;
                    av_y = y/size;
                    av_z = z/size;

                    String coordinateTxt = "Orientation: "+mOrientation+", X: "+av_x+", Y: "+av_y+", Z: "+av_z;
                    txtviewCoordinate.setText(coordinateTxt);
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

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_next:
                if(status == RECORD_RECORDING){
                    Toast.makeText(AccelerometerActivity.this, R.string.accel_recording,Toast.LENGTH_SHORT).show();
                } else {
                    coordinate_index = 0;
                    status = RECORD_RECORDING;
                    accel.clear();
                    String coordinateTxt = "";
                    txtviewCoordinate.setText(coordinateTxt);
                }
                break;
            case R.id.btn_stop:
                status = RECORD_STOPED;
                break;
            case R.id.btn_save:
                CsvOperator.writeCsv(record_list, record_path);
                break;
            case R.id.btn_recode_list:
                CsvOperator.readCsv(saved_list, record_path);
                break;
        }
    }

    private class AlbumOrientationEventListener extends OrientationEventListener {
        public AlbumOrientationEventListener(Context context) {
            super(context);
        }

        public AlbumOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }
            //mOrientation = orientation;

            //保证只返回四个方向
            /*int newOrientation = ((orientation + 45) / 90 * 90) % 360
            if (newOrientation != mOrientation) {
                mOrientation = newOrientation;

                //返回的mOrientation就是手机方向，为0°、90°、180°和270°中的一个
            }*/
        }
    }
}
