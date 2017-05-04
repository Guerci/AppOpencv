package tfg.uab.jga.appopencv;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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


    public static Mat surroundModulation(Mat src){

        int rows = src.rows();
        int columns = src.cols();
        int channels = src.channels();

        int maxVal = 255;

        src.convertTo(src, CvType.CV_64FC3);

        Scalar scalar = new Scalar(1.0/maxVal);
        Core.divide(src,scalar,src);
        double[] rc = new double[rows*columns];
        double[] gc = new double[rows*columns];
        double[] bc= new double[rows*columns];

        for(int j = 0; j<rows;j++){
            for(int i = 0; i<columns; i++){
                double[] rgb = src.get(j,i);
                rc[j+i] = rgb[0];
                gc[j+i] = rgb[1];
                bc[j+i] = rgb[2];
            }
        }



        return src;
    }


    public void applyOneChannel(Mat isignal){
        double centreSize = 3;
        double gaussianSigma = 1.5;
        double contrastEnlarge = 2;
        double surroundEnlarge = 5;
        double s1 = -0.77;
        double s4 = -0.67;
        double c1 = 1;
        double c4 = 1;
        double nk = 4;

        SigmaTemplate st = relativePixelContrast(isignal,centreSize,surroundEnlarge*centreSize);
        Mat rgc = st.getSigmaCentre();
        Mat rgs = st.getSigmaSurround();
        Scalar mrgc = Core.mean(rgc);
        Scalar mrgs = Core.mean(rgs);
        c1 = c1 + mrgc.val[0];
        c4 = c4 + mrgs.val[0];
        Mat ab = SingleContrast(isignal,gaussianSigma,contrastEnlarge,nk);
        Mat ba = SingleGaussian(isignal,gaussianSigma*surroundEnlarge);


    }

    public SigmaTemplate relativePixelContrast(Mat inputImage, double centreSize, double surroundSize){
        double[] centreSizeArray = new double[2];
        double[] surroundSizeArray = new double[2];
        centreSizeArray[0] = centreSize;
        centreSizeArray[1] = centreSize;
        surroundSizeArray[0] = surroundSize;
        surroundSizeArray[1] = surroundSize;
        Scalar centerSizeScalar = new Scalar(centreSize);
        Scalar surroundSizeScalar = new Scalar(surroundSize);
        Mat hc = new Mat((int)centreSize,(int)centreSize,CvType.CV_64FC1,centerSizeScalar);
        Mat hs = new Mat ((int)surroundSize,(int) surroundSize,CvType.CV_64FC1,surroundSizeScalar);
        double hcx = hc.rows();
        double hcy = hc.cols();
        double d[] = new double[2];
        d[0] = (hcx/2)-1;
        d[1] = (hcy/2)-1;
        double hsx = hs.rows();
        double hsy = hs.cols();
        double[] m = new double[2];
        m[0] = (hsx+1)/2;
        m[1] = (hsy+1)/2;
        double zero = 0;
        for(int i = (int)m[0]-(int)d[0];i<=m[0]+d[0];i++){
            for(int j = (int)m[1]-(int)m[1];i<=m[1]+m[1];i++){
                hs.put(i,j,zero);
            }
        }

        double rows = inputImage.rows();
        double cols = inputImage.cols();
        double chnl = inputImage.channels();
        Mat SigmaCentre = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));
        Mat SigmaSurround = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));
        //Pregunutar aquesta part a arash o alejandro

        SigmaCentre = stdfilt(inputImage,hc);
        SigmaSurround = stdfilt(inputImage,hs);

        SigmaTemplate st = new SigmaTemplate(SigmaCentre,SigmaSurround);
        return st;


    }

    private Mat stdfilt(Mat image, Mat h){
        Mat out;
        Point anchor;

        Scalar noZero = new Scalar(Core.countNonZero(h));
        Core.divide(h,noZero,h);
        double delta = 0;
        anchor = new Point(-1,-1);
        out = image.clone();

        Imgproc.filter2D(image,out,-1,h,anchor,delta,Core.BORDER_REFLECT_101);
        Core.absdiff(out,image,out);
        Core.pow(out,2.0,out);

        return out;
    }

    private Mat SingleContrast(Mat isignal,double startingsigma, double contrastenlarge, double nContrastLevels){
        double rows = isignal.rows();
        double cols = isignal.cols();
        double[] arg1 = new double[2];
        arg1[0] = 17;
        arg1[1] = 1;
        double[] arg2 = new double[2];
        arg1[0] = 1;
        arg1[1] = 17;
        Mat contrastImx = GetContrastImage(isignal, arg1);
        Mat contrastImy = GetContrastImage(isignal,arg2);
        double finishSigma = startingsigma * contrastenlarge;
        Mat sigmas = linspace(startingsigma,finishSigma,nContrastLevels);

        Mat contrastLevelX = GetContrastLevels(contrastImx,nContrastLevels);
        Mat contrastLevelY = GetContrastLevels(contrastImy,nContrastLevels);

        Mat nContrastLevelsX = unique(contrastLevelX);
        //nConstrastLevelX = nContrastLevelX'

        Mat nContrastLevelY = unique(contrastLevelY);
        //nConstrastLevelX = nContrastLevelX'
        Mat fresponse = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));
        for(int )


    }

    private Mat linspace(double startP,double endP,double interval){
        double spacing = interval -1;

        Mat y = new Mat((int)spacing,1,CvType.CV_64FC1);
        for(int i = 0;i<y.rows();i++){
            double data = startP + i*(endP-startP)/spacing;
            y.put(i,1,data);
        }
        return y;
    }
}
