package tfg.uab.jga.appopencv;


import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;


public class Luminance implements Serializable{
    private int id;
    private String name;
    private int red, green, blue;



    public Luminance(ArrayList<Integer> rgba, Context context){
        this.red = rgba.get(0);
        this.green = rgba.get(1);
        this.blue = rgba.get(2);

        Random r = new Random();
        this.id = r.nextInt();
        this.name = context.getString(R.string.lum_from_image);
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





}
