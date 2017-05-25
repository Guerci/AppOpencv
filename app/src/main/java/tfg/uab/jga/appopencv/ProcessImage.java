package tfg.uab.jga.appopencv;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import java.text.DecimalFormat;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jordi on 26/04/2017.
 */

public class ProcessImage {
    Mat matInput;
    Bitmap bitmapInput;
    ArrayList<Integer> rgbArray;
    static String TAG = "ProcessImage";
    public ProcessImage(Mat src){
        matInput = src;
    }
    public ProcessImage(){}

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
    public ArrayList<Integer> getLumFromImage(Mat image){
        if(image.type() != CvType.CV_64FC3){
            image.convertTo(image,CvType.CV_64FC3);
        }
        int rows = image.rows();
        int cols = image.cols();
        double[] redP = new double[rows*cols];
        double[] greenP = new double[rows*cols];
        double[] blueP = new double[rows*cols];
        int index = 0;
        for(int i = 0;i<rows;i++){
            for(int j = 0; j<cols;j++){
                double[] BGR;
                BGR = image.get(i,j);
                redP[index] = BGR[2];
                greenP[index] =  BGR[1];
                blueP[index] =  BGR[0];
                index++;
            }
        }
        int R,G,B;
        R = (int)average(redP);
        G = (int)average(greenP);
        B = (int)average(blueP);
        ArrayList<Integer> RGBA = new ArrayList<>();
        RGBA.add(R);
        RGBA.add(G);
        RGBA.add(B);
        RGBA.add(5);
        return RGBA;
    }

    public void test(){
        Scalar scalar = new Scalar(123);
        Mat testMat = new Mat(5,5,CvType.CV_64FC1);
        double[] doubles = {143};
        for(int i = 0; i<5;i++){
            for(int j = 0; j<5;j++){

                testMat.put(i,j,doubles[0]);
            }
        }
        for(int i = 0; i<5;i++){
            for(int j = 0; j<5;j++){
                double[] pixel = testMat.get(i,j);
                String pixString = String.valueOf(pixel[0]);
                Log.d(TAG,pixString);
            }
        }
    }

    public static double average(int[] data){
        int sum = 0;
        double average;
        for(int i = 0; i<data.length;i++){
            sum = sum + data[i];

        }
        average = sum/data.length;
        return average;
    }

    public static double average(double[] data){
        double sum = 0;
        double average;
        for(int i = 0; i<data.length;i++){
            sum = sum + data[i];

        }
        average = sum/data.length;
        return average;
    }


    public Mat surroundModulation(Mat src){

        int rows = src.rows();
        int columns = src.cols();
        int channels = src.channels();

        int maxVal = 255;

        src.convertTo(src, CvType.CV_64FC3); //convert MAT to double precision with 3 channels

        Scalar scalar = new Scalar(1.0/maxVal);
        Core.divide(src,scalar,src); //divide de Mat
        Mat rcMat = new Mat(rows,columns,CvType.CV_64FC1);
        Mat gcMat = new Mat(rows,columns,CvType.CV_64FC1);
        Mat bcMat = new Mat(rows,columns,CvType.CV_64FC1);


        //put
        for(int j = 0; j<rows;j++){
            for(int i = 0; i<columns; i++){
                double[] rgb = src.get(j,i);
                rcMat.put(j,i,rgb[2]);
                gcMat.put(j,i,rgb[1]);
                bcMat.put(j,i,rgb[0]);

            }
        }
        Log.d(TAG,"rcMat");
        Log.d(TAG,rcMat.dump());
        Log.d(TAG,"gcMat");
        Log.d(TAG,gcMat.dump());
        Log.d(TAG,"bcMat");
        Log.d(TAG,bcMat.dump());

        Mat dorg = applyOneChannel(rcMat);
        Mat doyb = applyOneChannel(gcMat);
        Mat dowb = applyOneChannel(bcMat);

        Mat red = new Mat(rows,columns,CvType.CV_64F); //create and fill mat red
        for(int i = 0;i<rows;i++){
            for(int j = 0;j<columns;j++){
                red.put(i,j,dorg.get(i,j));
            }
        }

        Mat green = new Mat(rows,columns,CvType.CV_64F); //create and fill mat green
        for(int i = 0;i<rows;i++){
            for(int j = 0;j<columns;j++){
                green.put(i,j,doyb.get(i,j));
            }
        }

        Mat blue = new Mat(rows,columns,CvType.CV_64F); //create and fill mat blue
        for(int i = 0;i<rows;i++){
            for(int j = 0;j<columns;j++){
                blue.put(i,j,dowb.get(i,j));
            }
        }

        //merge the 3 maps in 1 with 3 channels
        List<Mat> listMat = Arrays.asList(blue,green,red);
        Mat dorespons = new Mat();
        Core.merge(listMat,dorespons);
        double[][] luminance = calculateLuminanceSingle(dorespons,src);
        luminance = reshape(luminance,1,3); //reshape luminance in a matrix of 1 row and 3 cols

        double sumluminance = luminance[0][1]+luminance[0][2]+luminance[0][0]; //sum of the values
        //divide each value per sum of all values
        for(int i = 0; i<3;i++){
            luminance[0][i] = luminance[0][i]/sumluminance;

        }

        Mat colourConstantImage = matChansMulK(src,luminance); //mutiplicamos cada canal por su luminance

        //cogemos el max de la matrix
        Core.MinMaxLocResult minMaxLocResult;
        minMaxLocResult =Core.minMaxLoc(colourConstantImage);
        double max = minMaxLocResult.maxVal;
        Scalar maxScal = new Scalar(max);
        Core.divide(colourConstantImage,maxScal,colourConstantImage);
        colourConstantImage.convertTo(colourConstantImage,CvType.CV_8UC3);



        return colourConstantImage;
    }


    private Mat applyOneChannel(Mat isignal){
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
        Log.d(TAG,"rgc getSigmaCentre");
        Log.d(TAG,rgc.dump());
        Mat rgs = st.getSigmaSurround();
        Log.d(TAG,"rgs getSigmaSurround");
        Log.d(TAG,rgs.dump());
        Scalar mrgc = Core.mean(rgc);
        Scalar mrgs = Core.mean(rgs);
        Log.d(TAG,"rgc mean i rgs mean");
        Log.d(TAG,mrgc.toString());
        Log.d(TAG,mrgs.toString());
        c1 = c1 + mrgc.val[0];
        c4 = c4 + mrgs.val[0];
        Mat ab = SingleContrast(isignal,gaussianSigma,contrastEnlarge,nk);
        Log.d(TAG,"Sigle Contrast result");
        Log.d(TAG,ab.dump());
        Mat ba = SingleGaussian(isignal,gaussianSigma*surroundEnlarge);
        Log.d(TAG,"single Gaussian");
        Log.d(TAG,ba.dump());
        Mat ss = linspace(s1,s4,nk);
        Mat cs = linspace(c1,c4,nk);

        Mat dorg = applyNeighbourInpact(isignal,ab,ba,ss,cs);
        return dorg;

    }

    private SigmaTemplate relativePixelContrast(Mat inputImage, double centreSize, double surroundSize){

       /* double[] centreSizeArray = new double[2];
        double[] surroundSizeArray = new double[2];
        centreSizeArray[0] = centreSize;
        centreSizeArray[1] = centreSize;
        surroundSizeArray[0] = surroundSize;
        surroundSizeArray[1] = surroundSize;*/
        Scalar oneScalar = new Scalar(1);
        Mat hc = new Mat((int)centreSize,(int)centreSize,CvType.CV_64FC1,oneScalar);
        Mat hs = new Mat ((int)surroundSize,(int) surroundSize,CvType.CV_64FC1,oneScalar);
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

        /*
        double rows = inputImage.rows();
        double cols = inputImage.cols();
        double chnl = inputImage.channels();*/
        //Mat SigmaCentre = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));
        //Mat SigmaSurround = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));
        //Pregunutar aquesta part a arash o alejandro

        Mat SigmaCentre = stdfilt(inputImage,hc);
        Mat SigmaSurround = stdfilt(inputImage,hs);

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
        Mat contrastImx = getContrastImage(isignal, arg1);
        Mat contrastImy = getContrastImage(isignal,arg2);
        double finishSigma = startingsigma * contrastenlarge;
        Mat sigmas = linspace(startingsigma,finishSigma,nContrastLevels);

        Mat contrastLevelX = getContrastLevels(contrastImx,nContrastLevels);
        Mat contrastLevelY = getContrastLevels(contrastImy,nContrastLevels);

        double[] nContrastLevelsX = unique(contrastLevelX);

        double[] nContrastLevelY = unique(contrastLevelY);

        Mat rfresponse = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));
        for(int i = 0; i < nContrastLevelsX.length; i++ ){

            double[] lambdaxiA = sigmas.get((int)nContrastLevelsX[i],1);
            double lambdaxi = lambdaxiA[0];
            for(int j = 0; j<nContrastLevelY.length;j++){
                double[] lambdayA = sigmas.get((int)nContrastLevelY[j],1);
                double lambdayi = lambdayA[0];
                Size size = new Size(lambdaxi,lambdayi);

                Imgproc.GaussianBlur(rfresponse,rfresponse,size,0,0);
                //falta una linia que nose que fa, demanar a Arash
            }
        }
        return rfresponse;

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

    private Mat getContrastLevels(Mat contrastIm, double nContrastLevels){

        Core.MinMaxLocResult minMaxLocResult =Core.minMaxLoc(contrastIm);
        double min = minMaxLocResult.minVal;
        double max = minMaxLocResult.maxVal;
        double step = ((max-min)/nContrastLevels);
        ArrayList<Double> levels = new ArrayList<>();
        double i = min;
        while(i < max){
            levels.add(i);
            i= i+step;
        }
        int size = levels.size();
        levels.remove(size-1);
        levels.remove(0);

        return imQuantize(contrastIm,levels);

    }

    //private Mat imQuantize(Mat contrastIm,ArrayList<Double> levels){}

    private Mat getContrastImage(Mat isignal, double[] surroundSize){
        double[] centreSize = new double[2];
        centreSize[0] = 0;
        centreSize[1] = 0;

        Mat contrastStd = LocalstdContrast(isignal,surroundSize,centreSize);
        Mat contrastImg = new Mat(contrastStd.rows(),contrastStd.cols(),contrastStd.type(),new Scalar(1));
        Core.subtract(contrastImg,contrastStd,contrastImg);

        return  contrastImg;


    }

    private double[] unique(Mat src){
        ArrayList<Double> o = new ArrayList<>();
        if(src.type() != CvType.CV_32FC1){
            src.convertTo(src,CvType.CV_32FC1);
        }
        for(int i = 0;i<src.rows();i++){
            for(int j = 0; j<src.cols();j++){
                if(o == null){
                    double[] data =src.get(i,j);
                    o.add(data[0]);
                }else{
                    double[] data = src.get(i,j);
                    if(!o.contains(data[0])){
                        o.add(data[0]);
                    }
                }
            }
        }
        double[] out = new double[o.size()];
        for(int i = 0; i<o.size();i++){
            out[i] = o.get(i);
        }
        return out;
    }

    private static double[][] reshape(double[][] A, int m, int n) {
        int origM = A.length;
        int origN = A[0].length;
        if(origM*origN != m*n){
            throw new IllegalArgumentException("New matrix must be of same area as matix A");
        }
        double[][] B = new double[m][n];
        double[] A1D = new double[A.length * A[0].length];

        int index = 0;
        for(int i = 0;i<A.length;i++){
            for(int j = 0;j<A[0].length;j++){
                A1D[index++] = A[i][j];
            }
        }

        index = 0;
        for(int i = 0;i<n;i++){
            for(int j = 0;j<m;j++){
                B[j][i] = A1D[index++];
            }

        }
        return B;
    }

    private Mat matChansMulK(Mat inputImage, double[][] k){

        for(int i = 0; i <3;i++){
            k[0][i] = 1 / k[0][i];
        }

        List<Mat> listMat = null;
        Core.split(inputImage,listMat);
        Mat blue = new Mat();
        Mat green = new Mat();
        Mat red = new Mat();

        Scalar scalBlue = new Scalar(k[0][2]);
        Scalar scalGreen = new Scalar(k[0][1]);
        Scalar scalRed = new Scalar(k[0][0]);

        Core.multiply(listMat.get(0),scalBlue,blue);
        Core.multiply(listMat.get(1),scalGreen,green);
        Core.multiply(listMat.get(2),scalRed,red);

        List<Mat> list = Arrays.asList(blue,green,red);
        Mat out = new Mat();

        Core.merge(list,out);

        return out;

    }

    private double[][] calculateLuminanceSingle(Mat ModelRespons,Mat InputImage){
        Core.MinMaxLocResult minMaxLocResult;
        minMaxLocResult =Core.minMaxLoc(InputImage);
        double max = minMaxLocResult.maxVal;
        double min = minMaxLocResult.minVal;
        double[][] ret = null; //pel retun no fa res
        return ret;

    }

    private Mat SingleGaussian(Mat isignal,double startingSigma){

        Size kernerl = new Size(startingSigma,startingSigma);

        Imgproc.GaussianBlur(isignal,isignal,kernerl,0,0);
        return isignal;
    }

    private Mat applyNeighbourInpact(Mat isignal, Mat ab, Mat ba, Mat surroundImpacts, Mat centreImpacts){
        int nContrastLevels = surroundImpacts.cols();
        double[] surroundSize = {17,17};
        Mat contrastImage = getContrastImage(isignal,surroundSize);
        Mat contrastLevels = getContrastLevels(contrastImage,nContrastLevels);
        double[] nconLevels = unique(contrastLevels);
        //nconLevels ha de ser 1x4 fer el canvi si es al reves
        Scalar zero = new Scalar(0);
        Mat osignal = new Mat(isignal.rows(),isignal.cols(),CvType.CV_64FC1,zero);
        for(int i = 0; i<nconLevels.length;i++){

        }
        return osignal;

    }

    private Mat imQuantize(Mat in, ArrayList<Double> levels){

        Mat index = new Mat(in.rows(),in.cols(),CvType.CV_64FC1,new Scalar(1));

        for(int i = 0; i<in.cols();i++){
            for(int j = 0; j<in.rows();j++){
                for(int k = 0; k<levels.size();k++){
                    double[] pixel = in.get(j,i);
                    if(pixel[0] <= levels.get(0)){
                        index.put(j,i,0);
                    }
                    else if(pixel[0] >= levels.get(levels.size()-1)){
                        index.put(j,i,255);
                    }
                    else{
                        if(levels.get(k)< pixel[0] && pixel[0] <= levels.get(k+1)){
                            double valor = ((k+1)*255)/(levels.size());
                            index.put(j,i,valor);
                        }
                    }

                }
            }
        }
        return index;
    }

    private Mat LocalstdContrast(Mat inputImage, double[] windowSize, double[] centreSize){
        Mat kernel = new Mat(1,2,CvType.CV_64FC1);
        kernel.put(0,0,windowSize[0]);
        kernel.put(0,1,windowSize[1]);
        double delta = 0;
        Point anchor = new Point(centreSize[0],centreSize[1]);
        Mat dest = new Mat(inputImage.rows(),inputImage.cols(),CvType.CV_64FC1);
        Imgproc.filter2D(inputImage,dest,-1,kernel,anchor,delta,Core.BORDER_CONSTANT);
        Mat stdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Core.subtract(inputImage,dest,stdv);
        Core.pow(stdv,2,stdv);
        Mat meanstdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Imgproc.filter2D(stdv,meanstdv,-1,kernel,anchor,delta,Core.BORDER_CONSTANT);
        Core.sqrt(meanstdv,meanstdv);
        return meanstdv;
    }
}
