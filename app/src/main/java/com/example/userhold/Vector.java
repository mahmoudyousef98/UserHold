package com.example.userhold;

class Vector {
    protected ValueList x = null;
    protected ValueList y = null;
    protected ValueList z = null;

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
        this.add_x(val_x);
        this.add_y(val_y);
        this.add_z(val_z);
        size += 1;
    }

    double[] gets(int i){
        double[] result = new double[3];
        result[0] = x.get(i);
        result[1] = y.get(i);
        result[2] = z.get(i);
        return(result);
    }

    int size(){
        return(x.size());
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
