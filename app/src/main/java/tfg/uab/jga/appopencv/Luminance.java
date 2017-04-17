package tfg.uab.jga.appopencv;


import java.util.ArrayList;

public class Luminance {
    private int id;
    private String name;
    private int red, green, blue;


    public Luminance(){
        super();
    }

    public Luminance(int id, String name, int red, int green, int blue){
        super();
        this.blue = blue;
        this.green = green;
        this.red = red;
        this.id = id;
        this.name = name;

    }

    public int getId(){
        return this.id;
    }

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

    public void setBlue(int blue){
        this.blue = blue;
    }


    public ArrayList getRGB(){
        ArrayList a = new ArrayList();
        a.add(this.red);
        a.add(this.green);
        a.add(this.blue);
        return a;
    }


}
