package com.example.userhold;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ModelsActivity extends AppCompatActivity implements SensorEventListener {

    private int threshold;
    private double sensitivity;

    private Motion ground, pass, input;
    private Model model;

    private int state = 0;
    private boolean ready = false;

    private int level = 0;

    private LinkedList<HashMap> gyr_vals;
    private LinkedList<HashMap> accel_vals;

    private SensorManager sMg;
    public Sensor accel, gyr;

    private TextView text;
    private TextView prompt;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_models);
        text = findViewById(R.id.fullscreen_content);
        text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(ready){
                    if(state == 0){
                        startRecording();
                    }
                    else if(state == 1){
                        stopRecording();
                    }
                    return true;
                }
                return false;
            }
        });
        prompt = findViewById(R.id.prompt);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        Intent intent = getIntent();
        threshold = intent.getIntExtra("threshold", 3);
        sensitivity = intent.getDoubleExtra("sensitivity", 0.8);

        initializeSensors();
    }

    @Override
    protected void onStart(){
        super.onStart();
        measure("Place the phone on the table to begin calibration");
    }


    public void initializeSensors(){
        sMg = (SensorManager)this.getSystemService(SENSOR_SERVICE);

        accel = sMg.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        assert accel != null : "No accelerometer on device";

        gyr = sMg.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        assert gyr != null : "No gyroscope on device";

        //this.onPause();
    }

    private void startRecording(){
        sMg.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        sMg.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL);
        state = 1;
        text.setText("Tap the screen to stop recording");
    }

    private void stopRecording(){
        sMg.unregisterListener(this);
        state = 0;
        ready = false;
        prompt.setVisibility(View.INVISIBLE);
        text.setText("Loading");

        try {
            switch (level) {
                case 0:
                    stage0();
                    break;
                case 1:
                    stage1();
                    break;
                case 2:
                    stage2();
                    break;
                case 3:
                    stage3();
                    break;
                case 4:
                    stage4();
                    break;
                case 5:
                    stage5();
                    break;
                case 6:
                    stage6();
                    break;
                case 7:
                    stage7();
                    break;
                default:
                    stage7();
                    break;
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void measure(String toSay){
        gyr_vals = new LinkedList<HashMap>();
        accel_vals = new LinkedList<HashMap>();
        text.setText(toSay);
        prompt.setVisibility(View.VISIBLE);
        ready = true;
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void stage0() throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        ground =  new Motion(groundv, gyrov, threshold, true);
        model = new Model(ground, sensitivity);
        text.setText("Calibrated");
        TimeUnit.SECONDS.sleep(2);
        level++;
        measure("Hold your phone comfortably in your hand");
    }

    private void stage1() throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        pass = new Motion(groundv, gyrov, threshold, true);
        model.record_passcode(pass);
        text.setText("Recorded");
        TimeUnit.SECONDS.sleep(2);
        level++;
        measure("Hold your phone again (authentication)");
    }

    private void stage2() throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        input = new Motion(groundv, gyrov, threshold, true);
        boolean output = model.authenticate(input);
        text.setText("We got: " + output);
        TimeUnit.SECONDS.sleep(3);
        level++;
        text.setText("Wait 3 minutes");
        TimeUnit.MINUTES.sleep(3);
        measure("Hold your phone again (authentication)");
    }

    private void stage3() throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        input = new Motion(groundv, gyrov, threshold, false);
        boolean output = model.authenticate(input);
        text.setText("We got: " + output);
        level++;
        TimeUnit.SECONDS.sleep(3);
        measure("Choose a motion and get ready to input it");
    }

    private void stage4() throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        pass = new Motion(groundv, gyrov, threshold, false);
        model.record_passcode(pass);
        text.setText("Recorded");
        TimeUnit.SECONDS.sleep(2);
        level++;
        measure("Do the motion again (authentication)");
    }

    private void stage5()throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        input = new Motion(groundv, gyrov, threshold, false);
        boolean output = model.authenticate(input);
        text.setText("We got: " + output);
        TimeUnit.SECONDS.sleep(3);
        level++;
        text.setText("Wait 3 minutes");
        TimeUnit.MINUTES.sleep(3);
        measure("Hold your phone again (authentication)");
    }

    private void stage6()throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        input = new Motion(groundv, gyrov, threshold, false);
        boolean output = model.authenticate(input);
        text.setText("We got: " + output);
        level++;
        TimeUnit.SECONDS.sleep(3);
        measure("It's cracking time");
    }

    private void stage7()throws InterruptedException{
        TimeUnit.SECONDS.sleep(1);
        MotionVector groundv = new MotionVector(accel_vals);
        GyroVector gyrov = new GyroVector(gyr_vals);
        input = new Motion(groundv, gyrov, threshold, false);
        boolean output = model.authenticate(input);
        text.setText("We got: " + output);
        level = 4;
        TimeUnit.SECONDS.sleep(3);
        measure("Choose a motion and get ready to input it");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy){
        //do nothing
    }

    public void onSensorChanged(SensorEvent event) {
        int acc = 0;
        String sens;

        long time = event.timestamp;

        float[] vals = event.values;

        HashMap map = new HashMap();
        map.put("Timestamp", time);
        map.put("X-value", (double) vals[0]);
        map.put("Y-value", (double) vals[1]);
        map.put("Z-value", (double) vals[2]);

        if (event.sensor == gyr) {
            sens = "Gyroscope: ";
            acc = event.accuracy;
            map.put("Accuracy", acc);
            gyr_vals.add(map);
        } else {
            sens = "Accelerometer: ";
            acc = event.accuracy;
            map.put("Accuracy", acc);
            accel_vals.add(map);
        }
    }

    }
