package com.example.userhold;

import java.lang.Math;

public class MotionVector extends Vector {

    private int threshold = 0;
    public MotionVector(int _threshold){
        super("Motion");
        this.threshold = _threshold;
    }

    public double[] get_info(boolean standard){
        return(super.get_info(true));
    }

    public void detect_significant_motion(double var_x, double var_y, double var_z){ //parameters should be standard deviation of ground
        this.extract_variations(false);
        double v = Math.pow(var_x, 2) + Math.pow(var_y, 2) + Math.pow(var_z, 2);
        int i = -1;
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
                i = -1;
            }
        }

        assert (i != -1) : "No motion detected";

        num = 0;
        int j = size;
        int ind = j - 1;
        while(ind > i){
            double[] vals = this.gets(ind);
            double var = Math.pow(vals[0], 2) + Math.pow(vals[1], 2) + Math.pow(vals[2], 2);
            if(var > v){
                if(num == 0){
                    j = ind + 1;
                }
                num += 1;
                if(num == threshold) break;
            } else {
                num = 0;
                j = size;
            }
            ind -= 1;
        }
        x.subset(i, j);
        y.subset(i, j);
        z.subset(i, j);

    }

    public String toString(){
        String val = IDENTIFIER + '\n';
        val += "Vector type: " + "Motion" + '\n';
        val += "Vector size: " + x.size() + '\n';
        val += "Vector X:\n";
        val += x.toString();
        val += "Vector Y:\n";
        val += y.toString();
        val += "Vector Z:\n";
        val += z.toString();
        val += "Threshold: " + this.threshold + '\n';
        val += '\n' + ENDTEXT + '\n';
        return(val);
    }

}
