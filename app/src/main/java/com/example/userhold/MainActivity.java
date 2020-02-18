package com.example.userhold;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Context;

//import android.widget.TextView;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import java.util.HashMap;
import java.util.LinkedList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sMg;
    public Sensor accel, gyr;

    private int accel_acc = 0;
    private int gyr_acc = 0;

    private int state = 0; //where 0 is off and 1 is on

    private LinkedList<HashMap> gyr_vals = new LinkedList();
    private LinkedList<HashMap> accel_vals = new LinkedList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeSensors();

        setContentView(R.layout.activity_main);

        //add start and stop button
        //save values to dictionary-like structure
        //maybe add a save values button
        //maybe add a see values tab which works only when stopped
        //find a way to write to a file

        FloatingActionButton start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state == 0) {
                    Snackbar.make(view, "Currently recording sensor values", Snackbar.LENGTH_LONG);
                    //No activity set yet
                    //Context c = view.getContext();
                    //c.onResume();
                    state = 1;
                    onResume();
                } else {
                    Snackbar.make(view, "Already recording sensor values", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });


        FloatingActionButton stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state == 1) {
                    Snackbar.make(view, "Stopping reading", Snackbar.LENGTH_LONG);
                    //No activity set yet
                    onPause();
                    state = 0;
                    Snackbar.make(view, "Stopped reading", Snackbar.LENGTH_LONG);
                } else {
                    Snackbar.make(view, "Currently not recording sensor values", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state == 0) {
                    Snackbar.make(view, "Saving reads", Snackbar.LENGTH_LONG);
                    Context c = view.getContext();
                    File path = c.getExternalFilesDir(null);
                    System.out.println(path.toString());
                    long t = currentTimeMillis();
                    try {
                        File gyr_file = new File(path, "gyroscope-reads-" + t + ".json");

                        FileOutputStream stream = new FileOutputStream(gyr_file);
                        String v = convert_list_to_Json(gyr_vals);

                        stream.write(v.getBytes());
                        stream.close();
                        System.out.println(gyr_file.toString());
                    } catch (IOException e) {
                        System.out.println(e.toString());
                    }


                    try{
                        File accel_file = new File(path, "accelerometer-reads-" + t + ".json");
                        FileOutputStream stream = new FileOutputStream(accel_file);
                        String v = convert_list_to_Json(accel_vals);

                        stream.write(v.getBytes());
                        stream.close();
                        System.out.println(accel_file.toString());
                    } catch (IOException e) {
                        System.out.println(e.toString());
                    }

                    Snackbar.make(view, "Reads saved on external SD card", Snackbar.LENGTH_LONG);
                    System.out.println("Reads saved on external SD card");

                } else {
                    Snackbar.make(view, "Cannot save while reading, please stop reading first", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    public void initializeSensors(){
        sMg = (SensorManager)this.getSystemService(SENSOR_SERVICE);

        accel = sMg.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        assert accel != null : "No accelerometer on device";
        sMg.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);

        gyr = sMg.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        assert gyr != null : "No gyroscope on device";
        sMg.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);

        this.onPause();
    }

    protected void onResume(){
        super.onResume();
        if(state == 1) {
            sMg.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            sMg.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);
            System.out.println("Resumed");
        }
    }

    protected void onPause() {
        super.onPause();
        sMg.unregisterListener(this);
        System.out.println("Paused");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
        if(sensor == gyr){
            gyr_acc = accuracy;
        }
        else{
            accel_acc = accuracy;
        }
    }

    public void onSensorChanged(SensorEvent event) {
        int acc = 0;
        String sens;

        long time = event.timestamp;

        float[] vals = event.values;

        HashMap map = new HashMap();
        map.put("Timestamp", time);
        map.put("X-value", vals[0]);
        map.put("Y-value", vals[1]);
        map.put("Z-value", vals[2]);
        map.put("Accuracy", acc);

        if(event.sensor == gyr){
            sens = "Gyroscope: ";
            acc = gyr_acc;
            gyr_vals.add(map);
        } else {
            sens = "Accelerometer: ";
            acc = accel_acc;
            accel_vals.add(map);
        }

        System.out.print(sens);
        for(int i = 0; i < vals.length; i++){
            System.out.print(vals[i] + ";_ ");
        }
        System.out.print("\tAccuracy: ");
        System.out.println(acc);
        System.out.println(time);
        System.out.println();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private String convert_list_to_Json(LinkedList<HashMap> list){
        String value = "";
        for(int i = 0; i < list.size(); i++){
            value += convert_map_to_JSON(list.get(i));
        }

        return value;
    }

    private String convert_map_to_JSON(HashMap map){
        String val = "{\n";
        val += "x-value : " + map.get("X-value") + ",\n";
        val += "y-value : " + map.get("Y-value") + ",\n";
        val += "z-value : " + map.get("Z-value") + ",\n";
        val += "accuracy : " + map.get("Accuracy") + ",\n";
        val += "timestamp : " + map.get("Timestamp") + "\n";
        val += "}\n";
        return val;
    }
}
