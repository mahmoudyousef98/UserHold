package com.example.userhold;

import static java.lang.Math.sqrt;

public class Model {
    private GroundVector ground = null;
    private MotionVector pass = null;

    private double x0 = 0, y0 = 0, z0 = 0;
    private double std_x = 0, std_y = 0, std_z = 0;
    private double sensitivity = 0;

    private double cutoff_percent = 0.50;

    private double variation_threshold = 0;

    public static final String IDENTIFIER = "?";
    public static final String ENDTEXT = "!";

    public Model(GroundVector g, double _sensitivity){
        ground = g;
        initialize_ground();
        if((_sensitivity <= 1) && (_sensitivity > 0)){
            sensitivity = _sensitivity;
        }
    }

    private void initialize_ground(){
        double[] info = ground.get_ground_info();
        x0 = info[0];
        y0 = info[1];
        z0 = info[2];

        std_x = info[3];
        std_y = info[4];
        std_z = info[5];
    }

    public void record_passcode(MotionVector m){
        pass = m;
        pass.detect_significant_motion(std_x, std_y, std_z);
        double[] info = m.get_info(true);
        double thresh = Math.pow(info[3], 2) + Math.pow(info[4], 2) + Math.pow(info[5], 2);
        variation_threshold = sqrt(thresh) / sensitivity;
    }

    private double get_score_of_match(double i_x, double i_y, double i_z, double s_x, double s_y, double s_z){
        double score = 0;
        score += Math.pow(i_x - s_x, 2);
        score += Math.pow(i_y - s_y, 2);
        score += Math.pow(i_z - s_z, 2);
        return(sqrt(score));
    }

    private Tuple<Integer, Double> optimize_start(MotionVector bigger, MotionVector smaller, int cur_i, int cur_j){
        double best = Double.MAX_VALUE;
        int besti = cur_i;
        int diff = bigger.size() - smaller.size() - cur_i - cur_j;

        double[] smaller_values = smaller.gets(cur_j);

        for(int i = 0; i < diff; i++){
            double[] larger_values = bigger.gets(i+cur_i);
            double score = this.get_score_of_match(smaller_values[0], smaller_values[1], smaller_values[2], larger_values[0], larger_values[1], larger_values[2]);
            if(score < best){
                besti = i + cur_i;
                best = score;
            }
        }
        return(new Tuple<Integer, Double>(besti, best));
    }

    private Tuple<Double, Double> compare(MotionVector input){
        int size_pass = pass.size();
        int size_inp = input.size();
        MotionVector bigger;
        MotionVector smaller;
        if(size_inp > size_pass){
            bigger = input;
            smaller = pass;

        } else{
            bigger = pass;
            smaller = input;
        }
        int size_big = bigger.size();
        int size_small = smaller.size();

        double[] boundary_variations = this.pass.get_info(true);

        int match_score = 0;
        double total_variation = 0;
        int index = 0;
        for(int i = 0; i < size_small; i++){
            Tuple<Integer, Double> match = this.optimize_start(bigger, smaller, index, i);
            index += match.x;
            double var = match.y;
            total_variation += var;

            double[] small_vals = smaller.gets(i);
            double[] bigger_vals = bigger.gets(index);

            if(Math.abs(small_vals[0] - bigger_vals[0]) <= (boundary_variations[0] / sensitivity)) match_score += 1;
            if(Math.abs(small_vals[1] - bigger_vals[1]) <= (boundary_variations[1] / sensitivity)) match_score += 1;
            if(Math.abs(small_vals[2] - bigger_vals[2]) <= (boundary_variations[2] / sensitivity)) match_score += 1;

            index += 1;
        }

        double asize = (size_big + size_small) / 2;
        double percent_match = (match_score / (3 * asize));
        double unit_variation = total_variation / size_small;
        return(new Tuple<Double, Double>(percent_match, unit_variation));
    }

    public boolean authenticate(MotionVector input){
        input.detect_significant_motion(std_x, std_y, std_z);
        Tuple<Double, Double> analysis = compare(input);
        if(analysis.x >= cutoff_percent){
            return(true);
        }
        return analysis.x >= cutoff_percent * sensitivity && analysis.y <= variation_threshold;
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
