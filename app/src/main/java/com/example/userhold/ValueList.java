package com.example.userhold;

import static java.lang.Math.sqrt;

public class ValueList {

    private int amount = 0;
    private int cur_size = 0;
    private double[] arr = null;
    private double total = 0;
    private double maximum = Double.MAX_VALUE; //int min
    private double minimum = Double.MIN_VALUE; //int max


    static final String IDENTIFIER = "#";
    static final String ENDTEXT = "~";

    protected ValueList(){
    }

    void append(double x){
        if(cur_size == amount) {
            double[] temp = new double[cur_size + 25];
            cur_size += 25;
            if (amount > 0) System.arraycopy(arr, 0, temp, 0, amount);
            arr = temp;
        }

        if(x > maximum) {
            maximum = x;
        }
        if(x < minimum) {
            minimum = x;
        }

        arr[amount] = x;
        total += x;
        amount += 1;

    }

    void fix(){
        double avg = this.mean();
        for(int i = 0; i < amount; i++){
            arr[i] -= avg;
        }
    }

    double get(int i){
        if(i < 0 || i >= amount){
            return Double.MIN_VALUE; //error condition
        }
        return arr[i];
    }

    int size(){
        return(amount);
    }

    double sum(){
        return(total);
    }

    double mean(){
        if(amount == 0){
            return(0);
        }
        return(total / amount);
    }

    double variation(boolean standard){
        if(standard){
            //standard deviation
            double avg = this.mean();
            double runsum = 0;
            for(int j = 0; j < amount; j++){
                runsum += ((avg - arr[j]) * (avg - arr[j]));
            }
            return(sqrt(runsum / amount));
        }
        return(maximum - minimum); //abs?
    }

    boolean subset(int i, int j){
        if(i >= j || i < 0 || j >= amount) return(false);

        int num = j - i;
        double[] temp = new double[num];
        for(int t = i; t < j; t++){
            temp[t] = arr[i+t];
        }
        arr = temp;
        amount = num;
        cur_size = (int) ((num + 5) / 5);
        return(true);
    }

    public String toString(){
        String value = IDENTIFIER + "\n";
        value += "Amount: " + amount + '\n';
        for(int i = 0; i < amount; i++) value += arr[i] + '\t';
        value += ENDTEXT + '\n';
        return value;
    }
}
