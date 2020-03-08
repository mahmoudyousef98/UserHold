package com.example.userhold;

import java.util.HashMap;
import java.util.LinkedList;

/*
 * TODO:
 */

class Vector {
    protected ValueList x = null;
    protected ValueList y = null;
    protected ValueList z = null;

    protected double[] times;

    protected double[] means = new double[3];
    protected double[] variations = new double[3];

    protected int size = 0;

    private String type = "Abstract";

    static final String IDENTIFIER = "+";
    static final String ENDTEXT = "=";

    protected Vector(String _type){
        x = new ValueList();
        y = new ValueList();
        z = new ValueList();
        type = _type;
        times = null;
    }

    protected Vector(LinkedList<HashMap> vals, String _type){
        this(_type);
        times = new double[vals.size()];
        int i = 0;
        for(HashMap val : vals){
            double timestamp = (double)val.get("Timestamp");
            double x_val = (double)val.get("X-value");
            double y_val = (double)val.get("Y-value");
            double z_val = (double)val.get("Z-value");

            x.append(x_val);
            y.append(y_val);
            z.append(z_val);
            times[i] = timestamp;
            i++;
            size += 1;
        }
    }

    public void subset(double start_time, double end_time){
        int i = 0, j = size - 1;
        for(; i < size; i++){
            if(times[i] >= start_time) break;
        }
        if(i == size) return;
        while(j > (i + 1)){
            if(times[j] <= end_time) break;
        }
        x.subset(i, j);
        y.subset(i, j);
        z.subset(i, j);

        int num = j - i;
        double[] temp = new double[num];
        for(int t = i; t < j; t++){
            temp[t] = times[i+t];
        }
        times = temp;
        size = num;

    }

    private void add_x(double val){
        x.append(val);
    }

    private void add_y(double val){
        y.append(val);
    }

    private void add_z(double val){
        z.append(val);
    }

    void add(double val_x, double val_y, double val_z){
        return; //buggy implementation rn, trying to avoid it completely
        /*this.add_x(val_x);
        this.add_y(val_y);
        this.add_z(val_z);
        size += 1; */
    }

    double[] gets(int i){
        double[] result = new double[3];
        result[0] = x.get(i);
        result[1] = y.get(i);
        result[2] = z.get(i);
        return(result);
    }

    double get_time(int i){
        return times[i];
    }

    int size(){
        return(size);
    }

    void extract_means(){
        means[0] = x.mean();
        means[1] = y.mean();
        means[2] = z.mean();
    }

    void extract_variations(boolean standard){
        variations[0] = x.variation(standard);
        variations[1] = y.variation(standard);
        variations[2] = z.variation(standard);
    }

    public double[] get_info(boolean standard){
        this.extract_means();
        this.extract_variations(standard);

        double results[] = new double[6];
        results[0] = means[0];
        results[1] = means[1];
        results[2] = means[2];
        results[3] = variations[0];
        results[4] = variations[1];
        results[5] = variations[2];
        return(results);
    }

    public String toString(){
        String val = IDENTIFIER + '\n';
        val += "Vector type: " + type + '\n';
        val += "Vector size: " + x.size() + '\n';
        val += "Vector X:\n";
        val += x.toString();
        val += "Vector Y:\n";
        val += y.toString();
        val += "Vector Z:\n";
        val += z.toString();
        val += '\n' + ENDTEXT + '\n';
        return(val);
    }


}
