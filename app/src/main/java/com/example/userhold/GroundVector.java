package com.example.userhold;

public class GroundVector extends Vector {

    public GroundVector(){
        super("Ground");
    }

    public double[] get_info(boolean standard){
        return(super.get_info(false));
    }

    public double[] get_ground_info(){
        return(super.get_info(false));
    }

}
