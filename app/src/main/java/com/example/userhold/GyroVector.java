package com.example.userhold;

import java.util.HashMap;
import java.util.LinkedList;

public class GyroVector extends Vector {

    public GyroVector(){
        super("Gyro");
    }

    public GyroVector(LinkedList<HashMap> vals){
        super(vals, "Gyro");
    }

    public Tuple<Double, Double> detect_significant_motion(double var_x, double var_y, double var_z, int threshold){ //parameters should be standard deviation of ground
        this.extract_variations(true);
        double v = Math.pow(var_x, 2) + Math.pow(var_y, 2) + Math.pow(var_z, 2);
        int i = 0;
        int num = 0;

        for(int ind = 0; ind < size; ind++){
            double[] vals = this.gets(ind);
            double var = Math.pow(vals[0], 2) + Math.pow(vals[1], 2) + Math.pow(vals[2], 2);
            if(var > v){
                if(num == 0){
                    i = ind;
                }
                num += 1;
                if(num == threshold) break;
            } else {
                num = 0;
                i = 0;
            }
        }

        System.out.println("Significant motion minimum: " + i);

        num = 0;
        int j = size  - 1;
        int ind = j;
        while(ind > i){
            double[] vals = this.gets(ind);
            double var = Math.pow(vals[0], 2) + Math.pow(vals[1], 2) + Math.pow(vals[2], 2);
            if(var > v){
                if(num == 0){
                    j = ind;
                }
                num += 1;
                if(num == threshold) break;
            } else {
                num = 0;
                j = size - 1;
            }
            ind--;
        }
        //x.subset(i, j);
        //y.subset(i, j);
        //z.subset(i, j);
        System.out.println("Significant motion maximum: " + j);

        return new Tuple(times[i], times[j]);

    }

    public double[] get_values(double time) {
        int i;
        for (i = 0; i < this.size - 1; i++) {
            if(times[i] <= time && times[i+1] >= time) break;
        }
        return this.gets(i);
    }


}
