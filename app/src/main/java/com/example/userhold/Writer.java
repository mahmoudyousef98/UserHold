package com.example.userhold;

import android.content.Context;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* TODO: find a way to overwrite file when we need to delete the model.
*  Also, initial check to see if file exists.
*  NOTE: THIS FILE IS CURRENTLY NONFUNCTIONAL AND BUGGY.
* */
public class Writer {

    private static String filename = "local_model.txt";
    private static FileOutputStream fos = null;
    private static FileInputStream fis = null;

    public static void write_model(Model m){
        try {
            String to_write = m.toString();
            byte[] b = to_write.getBytes(StandardCharsets.UTF_8);
            for (int x = 0; x < b.length; x++) fos.write(b[x]);
        } catch(IOException e){
            return;
        }
    }

    /*private static ValueList read_ValueList(String vl){
        return new ValueList();
    } */
/*
    private static GroundVector read_GroundVector(String gv){
        String[] fields = gv.split("[\\r\\n]+");
        String fieldx = fields[4];
        String fieldy = fields[6];
        String fieldz = fields[8];

        int size = Integer.parseInt(fields[2].split(": ")[1]);

        GroundVector grv = new GroundVector();

        String[] x = fieldx.split("\\t");
        String[] y = fieldy.split("\\t");
        String[] z = fieldz.split("\\t");

        for(int index = 0; index < size; index++){
            grv.add(Double.parseDouble(x[index]), Double.parseDouble(y[index]), Double.parseDouble(z[index]));
        }

        return(grv);

    }

    private static MotionVector read_MotionVector(String mv){
        //read threshold
        int threshold = 0;

        String[] fields = mv.split("[\\r\\n]+");
        String fieldx = fields[4];
        String fieldy = fields[6];
        String fieldz = fields[8];

        int size = Integer.parseInt(fields[2].split(": ")[1]);
        int thresh = Integer.parseInt(fields[9].split(": ")[1]);

        MotionVector mov = new MotionVector(thresh);

        String[] x = fieldx.split("\\t");
        String[] y = fieldy.split("\\t");
        String[] z = fieldz.split("\\t");

        for(int index = 0; index < size; index++){
            mov.add(Double.parseDouble(x[index]), Double.parseDouble(y[index]), Double.parseDouble(z[index]));
        }
        return(mov);
    }

    public static Tuple<Model, Integer> parse_model(String m){
        String[] lines = m.split("[\\r\\n]+");
        if(lines[0].equals((Model.IDENTIFIER + '\n'))) return new Tuple<Model, Integer>(null, 2);
        double sensitivity = Double.parseDouble(lines[1].split(": ")[1]);

        GroundVector grv = null;
        MotionVector mov = null;
        int i = 2;

        String vals = "";
        while(!lines[i].equals(Vector.ENDTEXT + '\n')){
            i += 1;
            vals += lines[i];
        }
        grv = read_GroundVector(vals);
        if(grv == null) return(new Tuple<Model, Integer>(null, 3));
        Model mod = new Model(grv, sensitivity);
        vals = "";
        i += 2;
        while(!lines[i].equals(Vector.ENDTEXT + '\n')){
            i+=1;
            vals += lines[i];
        }
        mov = read_MotionVector(vals);
        if(mov == null){
            return(new Tuple<Model, Integer>(mod, 4));
        }
        mod.record_passcode(mov);
        return(new Tuple<Model, Integer>(mod, 0));

    }

    public static Tuple<Model, Integer> read_model(){
        //check if file exists, else return null model
        //check if file contains legitimate model else return null model
        //read each element of the model
        String value = "";
        try{
            int c = 0;
            while((c = fis.read()) != -1) {
                byte[] ch = {(byte)c};
                value += new String(ch, StandardCharsets.UTF_8);
            }
        }
        catch (IOException e){
            return new Tuple<Model, Integer>(null, 1);
        }
        return new Tuple<Model, Integer>(null, 1);
    }

    public static boolean open_io(Context context){
        try {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fis = context.openFileInput(filename);
            return true;
        }
        catch (IOException e){
            return false;
        }
    }

    public static boolean close_io(Context context){
        try{
            fos.close();
            fis.close();
            return true;
        }
        catch (IOException e){
            return false;
        }
    }

    public static String get_error(int code){
        switch(code) {
            case 1:
                return ("Could not read model from stored data");
            case 2:
                return ("Saved model corrupted");
            case 3:
                return ("Model calibration corrupted");
            case 4:
                return ("Passcode corrupted");
            default:
                return("Success");
        }
    }
    */
}