package tfg.uab.jga.appopencv;


import java.io.Serializable;
import java.util.ArrayList;

public class Luminance implements Serializable{
    private int id;
    private String name;
    private int red, green, blue, alpha;


    public Luminance(){
        super();
    }

    public Luminance(int id, String name, int red, int green, int blue, int alpha){
        super();
        this.blue = blue;
        this.green = green;
        this.red = red;
        this.id = id;
        this.name = name;
        this.alpha = alpha;

    }

    public int getId(){
        return this.id;
    }
    public int getAlpha(){ return this.alpha; }
    public int getRed(){
        return this.red;
    }
    public int getBlue(){
        return this.blue;
    }
    public int getGreen(){
        return this.green;
    }
    public String getName(){
        return this.name;
    }

    public void setName(String n){
        this.name = n;
    }
    public void setRed(int red){
        this.red = red;
    }
    public void setId(int id){
        this.id = id;
    }
    public void setGreen(int green){
        this.green = green;
    }
    public void setAlpha(int alpha) {this.alpha = alpha;}
    public void setBlue(int blue){
        this.blue = blue;
    }


    public ArrayList getRGBA(){
        ArrayList a = new ArrayList();
        a.add(this.red);
        a.add(this.green);
        a.add(this.blue);
        a.add(this.alpha);
        return a;
    }


}
