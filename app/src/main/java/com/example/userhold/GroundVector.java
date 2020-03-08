package com.example.userhold;

import java.util.HashMap;
import java.util.LinkedList;

public class GroundVector extends Vector {

    public GroundVector(){
        super("Ground");
    }

    public GroundVector(LinkedList<HashMap> vals){
        super(vals, "Ground");
    }

    public double[] get_info(boolean standard){
        return(super.get_info(false));
    }

    public double[] get_ground_info(){
        return(super.get_info(false));
    }

}
