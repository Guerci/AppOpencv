package tfg.uab.jga.appopencv;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.core.Mat;

import java.util.ArrayList;

/**
 * Created by jordi on 26/04/2017.
 */

public class ProcessImage {
    Mat matInput;
    Bitmap bitmapInput;
    ArrayList<Integer> rgbArray;

    public ProcessImage(Mat src){
        matInput = src;
    }

    public static ArrayList<Integer> getLumFromImage(Bitmap image){

        //inicalitzacio de variable
        int width = image.getWidth(); //agafem aplada bitmap
        int height = image.getHeight(); //agafem alçada bitmap
        int[] redP = new int[width*height];
        int[] greenP = new int[width*height];
        int[] blueP = new int[width*height];
        int[] alphaP = new int[width*height];
        int[] pixels = new int[width*height];

        int R,G,B,A;
        int index;
        int color;

        ArrayList<Integer> RGBA = new ArrayList<>();

        image.getPixels(pixels,0,width,0,0,width,height); //posem a pixels els valors RGBA de cada pixel de la imatge


       //Saparem cada color en un array diferent
        for(int y = 0; y<height;y++){
            for(int x = 0; x<width;x++){
                index = y *width + x;
                color = pixels[index];
                redP[index] = Color.red(color);
                greenP[index] = Color.green(color);
                blueP[index] = Color.blue(color);
                alphaP[index] = Color.alpha(color);
            }
        }

        //Agafem la mitjana de cada color
        R = (int)average(redP);
        G = (int)average(greenP);
        B = (int)average(blueP);
        A = (int)average(alphaP);

        RGBA.add(R);
        RGBA.add(G);
        RGBA.add(B);
        RGBA.add(A);

        return RGBA;
    }



    public static double average(int[] data){
        int sum = 0;
        double average;
        for(int i = 0; i<data.length;i++){
            sum = sum + data[i];

        }
        average = (double) sum/data.length;
        return average;
    }
}
