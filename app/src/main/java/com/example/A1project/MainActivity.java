package com.example.A1project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView text;
    SQLiteDatabase db;
    String heartratetosave;
    String respratetosave;
    SensorManager sensorManager;
    Sensor accelerometer;
    CountDownTimer timer;
    List<Double> accelValuesZ = new ArrayList<>();
    int per = 30;
    MoveAvg sma;
    private final Connection_Camera Connection_Camera = new Connection_Camera(this);
    public static final String BREATH_RATE = "BREATH_RATE";
    public static final String HEART_RATE = "HEART_RATE";
    public static final String NAUSEA = "NAUSEA";
    public static final String HEAD_ACHE = "HEAD_ACHE";
    public static final String DIARRHEA = "DIARRHEA";
    public static final String SOAR_THROAT = "SOAR_THROAT";
    public static final String FEVER = "FEVER";
    public static final String MUSCLE_ACHE = "MUSCLE_ACHE";
    public static final String LOSS_OF_SMELL_TASTE = "LOSS_OF_SMELL_TASTE";
    public static final String COUGH = "COUGH";
    public static final String SHORT_BREATH = "SHORT_BREATH";
    public static final String FEEL_TIRED = "FEEL_TIRED";

    @SuppressLint("HandlerLeak")
    private final Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_HEART_RATE_FINAL) {
                ((TextView) findViewById(R.id.heartRate)).setText(msg.obj.toString());
                heartratetosave = msg.obj.toString();
            }
        }
    };
    private Heart_rate analyzer;
    public static final int MESSAGE_HEART_RATE_FINAL = 1;
    private final int REQUEST_CODE_CAMERA = 0;
    public static final int MESSAGE_TIMER_VALUE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE_CAMERA);
        analyzer  = new Heart_rate(this, mainHandler);
        Button heartRateButton = (Button)findViewById(R.id.button);
        heartRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                TextureView cameraTextureView = findViewById(R.id.textureView2);
                SurfaceTexture previewSurfaceTexture = cameraTextureView.getSurfaceTexture();
                Surface previewSurface = new Surface(previewSurfaceTexture);
                Connection_Camera.start(previewSurface);
                analyzer.measureHeartRate(cameraTextureView, Connection_Camera);
            }
        });
        Button upload = (Button)findViewById(R.id.button4);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                try{
                    db = openOrCreateDatabase("myDB.db", Context.MODE_PRIVATE, null);
                    db.execSQL("DROP TABLE IF EXISTS ram;");
                    db.beginTransaction();
                    try {
                        db.execSQL("create table ram ("

                                + HEART_RATE + " FLOAT, "
                                + BREATH_RATE + " FLOAT, "
                                + NAUSEA + " FLOAT, "
                                + HEAD_ACHE + " FLOAT, "
                                + DIARRHEA + " FLOAT, "
                                + SOAR_THROAT + " FLOAT, "
                                + FEVER + " FLOAT, "
                                + MUSCLE_ACHE + " FLOAT, "
                                + LOSS_OF_SMELL_TASTE + " FLOAT, "
                                + COUGH + " FLOAT, "
                                + SHORT_BREATH + " FLOAT, "
                                + FEEL_TIRED + " FLOAT );" );
                        db.setTransactionSuccessful();
                    }
                    catch (SQLiteException e) {
                    }
                    finally {
                        db.endTransaction();
                    }
                }catch (SQLException e){
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                try {
                    db.execSQL( "INSERT OR REPLACE into ram(HEART_RATE, BREATH_RATE) values ( '"+heartratetosave+"', '"+respratetosave+"' );" );
                }
                catch (SQLiteException e) {
                }
                finally {
                }
            }
        });
        int i = 0;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        timer = new CountDownTimer(45000, 1000) {
            int i = 0;
            public void onTick(long millisUntilFinished) {
                i++;
            }
            public void onFinish() {
                i++;
                sensorManager.unregisterListener(accelListener);
                float breathRate =  sma.getPeakCount();
                breathRate *= 4f / 3f;
                System.out.println(breathRate);
                text = (TextView)findViewById(R.id.b_rate);
                text.setText(String.valueOf(breathRate));
                respratetosave = String.valueOf(breathRate);
                sensorManager.unregisterListener(accelListener);
            }
        };
        Button btn1 = (Button) findViewById(R.id.button2);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.start();
                sma = new MoveAvg(per);
                sensorManager.registerListener(accelListener, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
        });
                Button saveSymptomsButton = (Button) findViewById(R.id.button3);
                saveSymptomsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("test", "success");
                        Intent int1 = new Intent(getApplicationContext(), ratings.class);
                        int1.putExtra("h", heartratetosave);
                        int1.putExtra("r", respratetosave);
                        startActivity(int1);
                    }
                });
   }
    public void onResume() {
        super.onResume();
    }
    SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }
        public void onSensorChanged(SensorEvent event) {
            double z = event.values[2];
            accelValuesZ.add(z);
            sma.addData((float) z);
            System.out.println(accelValuesZ.size());
        }
    };
}