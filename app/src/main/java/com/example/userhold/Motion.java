package com.example.userhold;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Motion {

    private MotionVector mv;
    private GyroVector gv;

    private double[] accl_values;
    private double[] gyro_values;
    private int threshold;

    boolean ground;


    public Motion(MotionVector m, GyroVector v, int threshold, boolean ground){
        mv = m;
        gv = v;
        this.ground = ground;

        accl_values = mv.get_info(!ground);
        gyro_values = gv.get_info(!ground);

        this.threshold = threshold;

    }

    public void detect_significant_motion(double var_xa, double var_ya, double var_za, double var_xg, double var_yg, double var_zg){
        if(this.ground) return;
        Tuple<Double, Double> mot = mv.detect_significant_motion(var_xa, var_ya, var_za, this.threshold);
        Tuple<Double, Double> gyr = gv.detect_significant_motion(var_xg, var_yg, var_zg, this.threshold);

        double start = min(mot.x, gyr.x);
        double end = max(mot.y, gyr.y);

        mv.subset(start, end);
        gv.subset(start, end);
    }

    public double[] get_accelerometer_info(){
        return accl_values;
    }

    public double[] gets_accel(int i){
        return mv.gets(i);
    }

    public double[] get_gyroscope_info(){
        return gyro_values;
    }

    public double[] gets_gyro(int i){
        return gv.gets(i);
    }

    public int size(){
        return mv.size();
    }

    public double[] get_corresponding_gyro_values(int i){
        return(gv.get_values(mv.get_time(i)));
    }
}
