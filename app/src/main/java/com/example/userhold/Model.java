package com.example.userhold;

import static java.lang.Math.sqrt;

public class Model {
    private Motion ground;
    private Motion pass = null;

    private double x0a = 0, y0a = 0, z0a = 0, x0g = 0, y0g = 0, z0g = 0;
    private double std_xa = 0, std_ya = 0, std_za = 0, std_xg = 0, std_yg = 0, std_zg = 0;
    private double sensitivity = 0.0000001;

    private double cutoff_percent = 0.50;

    private double accel_variation_threshold = 0;
    private double gyro_variation_threshold = 0;

    public static final String IDENTIFIER = "?";
    public static final String ENDTEXT = "!";

    public Model(Motion g, double _sensitivity){
        ground = g;
        initialize_ground();
        if((_sensitivity <= 1) && (_sensitivity > 0)){
            sensitivity = _sensitivity;
        }
    }

    private void initialize_ground(){
        double[] info = ground.get_accelerometer_info();
        x0a = info[0];
        y0a = info[1];
        z0a = info[2];

        std_xa = info[3];
        std_ya = info[4];
        std_za = info[5];

        info = ground.get_gyroscope_info();
        x0g = info[0];
        y0g = info[1];
        z0g = info[2];

        std_xg = info[3];
        std_yg = info[4];
        std_zg = info[5];
    }

    public void record_passcode(Motion m){
        pass = m;
        pass.detect_significant_motion(std_xa, std_ya, std_za, std_xg, std_yg, std_zg);

        double[] info = m.get_accelerometer_info();
        double thresh = Math.pow(info[3], 2) + Math.pow(info[4], 2) + Math.pow(info[5], 2);
        accel_variation_threshold = sqrt(thresh) / sensitivity;

        info = m.get_gyroscope_info();
        thresh = Math.pow(info[3], 2) + Math.pow(info[4], 2) + Math.pow(info[5], 2);
        gyro_variation_threshold = sqrt(thresh) / sensitivity;

    }

    private double get_score_of_match(double i_x, double i_y, double i_z, double s_x, double s_y, double s_z){
        double score = 0;
        score += Math.pow(i_x - s_x, 2);
        score += Math.pow(i_y - s_y, 2);
        score += Math.pow(i_z - s_z, 2);
        return(sqrt(score));
    }

    private Tuple<Integer, Double> optimize_start(Motion bigger, Motion smaller, int cur_i, int cur_j){
        double best = Double.MAX_VALUE;
        int besti = cur_i;
        int diff = bigger.size() - smaller.size() - cur_i - cur_j;

        double[] smaller_values = smaller.gets_accel(cur_j);

        for(int i = 0; i < diff; i++){
            double[] larger_values = bigger.gets_accel(i+cur_i);
            double score = this.get_score_of_match(smaller_values[0], smaller_values[1], smaller_values[2], larger_values[0], larger_values[1], larger_values[2]);
            if(score < best){
                besti = i + cur_i;
                best = score;
            }
        }
        return(new Tuple<Integer, Double>(besti, best));
    }

    private Tuple<Double, Double> compare(Motion input){
        System.out.println("Initializing - sensitivity: " + sensitivity);
        int size_pass = pass.size();
        int size_inp = input.size();
        Motion bigger;
        Motion smaller;
        if(size_inp > size_pass){
            bigger = input;
            smaller = pass;

        } else{
            bigger = pass;
            smaller = input;
        }
        int size_big = bigger.size();
        int size_small = smaller.size();

        double[] boundary_variations = this.pass.get_accelerometer_info();
        double[] gyro_variations = this.pass.get_gyroscope_info();

        int match_score = 0;
        double total_accel_variation = 0, total_gyro_variation = 0;
        int index = 0;
        for(int i = 0; i < size_small; i++){
            Tuple<Integer, Double> match = this.optimize_start(bigger, smaller, index, i);
            index = match.x;
            double var = match.y;
            total_accel_variation += var;

            double[] small_vals = smaller.gets_accel(i);
            double[] bigger_vals = bigger.gets_accel(index);

            System.out.println(small_vals[0] + ", " + small_vals[1] + ", " + small_vals[2]);
            System.out.println(bigger_vals[0] + ", " + bigger_vals[1] + ", " + bigger_vals[2]);
            System.out.println(boundary_variations[0] + ", " + boundary_variations[1] + ", " + boundary_variations[2]);
            if(Math.abs(Math.abs(small_vals[0]) - Math.abs(bigger_vals[0])) <= Math.abs(boundary_variations[0] / sensitivity)) match_score += 1;
            if(Math.abs(Math.abs(small_vals[1]) - Math.abs(bigger_vals[1])) <= Math.abs(boundary_variations[1] / sensitivity)) match_score += 1;
            if(Math.abs(Math.abs(small_vals[2]) - Math.abs(bigger_vals[2])) <= Math.abs(boundary_variations[2] / sensitivity)) match_score += 1;

            small_vals = smaller.get_corresponding_gyro_values(i);
            bigger_vals = bigger.get_corresponding_gyro_values(index);
            total_gyro_variation += this.get_score_of_match(small_vals[0], small_vals[1], small_vals[2], bigger_vals[0], bigger_vals[1], bigger_vals[2]);

            //System.out.println(small_vals[0] + ", " + small_vals[1] + ", " + small_vals[2]);
            //System.out.println(bigger_vals[0] + ", " + bigger_vals[1] + ", " + bigger_vals[2]);
            //System.out.println(gyro_variations[0] + ", " + gyro_variations[1] + ", " + gyro_variations[2]);

            if(Math.abs(Math.abs(small_vals[0]) - Math.abs(bigger_vals[0])) <= (gyro_variations[0] / sensitivity)) match_score += 1;
            if(Math.abs(Math.abs(small_vals[1]) - Math.abs(bigger_vals[1])) <= (gyro_variations[1] / sensitivity)) match_score += 1;
            if(Math.abs(Math.abs(small_vals[2]) - Math.abs(bigger_vals[2])) <= (gyro_variations[2] / sensitivity)) match_score += 1;

            index += 1;
        }

        double asize = (size_big + size_small) / 2;
        System.out.println("Match score: " + match_score);
        double percent_match = ((double) match_score) / (6 * size_small);
        double unit_variation = (total_accel_variation + total_gyro_variation) / (2 * size_small);
        return(new Tuple<Double, Double>(percent_match, unit_variation));
    }

    public boolean authenticate(Motion input){
        input.detect_significant_motion(std_xa, std_ya, std_za, std_xg, std_yg, std_zg);
        Tuple<Double, Double> analysis = compare(input);
        System.out.println("Percentage: " + analysis.x);
        if(analysis.x >= cutoff_percent){
            return(true);
        }
        //return analysis.x >= cutoff_percent * sensitivity && analysis.y <= variation_threshold;
        return false;
    }

    public String toString(){
        String val = IDENTIFIER + "\n";
        val += "Sensitivity: " + sensitivity + '\n';
        val += "Ground:\n";
        if(ground == null){
            val += Vector.IDENTIFIER + "\nnull\n" + Vector.ENDTEXT + '\n';
        } else
            val += ground.toString();
        val += "Password:\n";
        if(pass == null){
            val += Vector.IDENTIFIER + "\nnull\n" + Vector.ENDTEXT + '\n';
        } else
            val += pass.toString();
        val += ENDTEXT +'\n';
        return val;
    }
}
