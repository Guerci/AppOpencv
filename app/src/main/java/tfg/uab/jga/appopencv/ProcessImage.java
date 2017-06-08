package tfg.uab.jga.appopencv;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import java.text.DecimalFormat;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
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



    public ArrayList<Integer> getLuminanceFromMat(Mat src){
        if(src.type() != CvType.CV_64FC3){
            src.convertTo(src,CvType.CV_64FC3);
        }
        Scalar scalAverage = Core.mean(src);
        ArrayList<Integer> rgb = new ArrayList<>();
        rgb.add((int)scalAverage.val[2]);
        rgb.add((int)scalAverage.val[1]);
        rgb.add((int)scalAverage.val[0]);
        rgb.add(5);
        return rgb;
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
        Log.d(TAG,"start surrond Modulation");
        Log.d(TAG,"rows: "+ String.valueOf(rows) + "columns: " + String.valueOf(columns) + "channels: " + String.valueOf(channels));
        double maxVal = 255.0;

        src.convertTo(src, CvType.CV_64FC3); //convert MAT to double precision with 3 channels

        Scalar scalar = new Scalar(maxVal,maxVal,maxVal);
        Core.divide(src,scalar,src); //divide de Mat
        List<Mat> listRGB = new ArrayList<>();
        Core.split(src,listRGB);





        Mat dorg = applyOneChannel(listRGB.get(2));//red
        Mat doyb = applyOneChannel(listRGB.get(1));//green
        Mat dowb = applyOneChannel(listRGB.get(0));//blue


        //merge the 3 maps in 1 with 3 channels
        List<Mat> listMat = Arrays.asList(dowb,doyb,dorg); //BGR
        Mat dorespons = new Mat();
        Core.merge(listMat,dorespons);
        Scalar luminance = calculateLuminanceSingle(dorespons,src);

        double sumluminance = luminance.val[2] +luminance.val[1]+luminance.val[0]; //sum of the values
        //divide each value per sum of all values
        double[] luminancedouble = {0.0,0.0,0.0};
        for(int i = 0; i<3;i++){
            luminancedouble[i] = luminance.val[i]/sumluminance;

        }

        Mat colourConstantImage = matChansMulK(src,luminancedouble); //mutiplicamos cada canal por su luminance

        //cogemos el max de la matrix

        ArrayList<Mat> list = new ArrayList<>();
        Core.split(colourConstantImage,list);
        Core.MinMaxLocResult maxBlue = Core.minMaxLoc(list.get(0));
        Core.MinMaxLocResult maxGreen = Core.minMaxLoc(list.get(1));
        Core.MinMaxLocResult maxRed = Core.minMaxLoc(list.get(2));

        double maxValor;
        if(maxBlue.maxVal >= maxGreen.maxVal && maxBlue.maxVal >= maxRed.maxVal){
            maxValor = maxBlue.maxVal;
        }else if(maxGreen.maxVal >= maxRed.maxVal){
            maxValor = maxGreen.maxVal;
        }else{
            maxValor = maxRed.maxVal;
        }


        Scalar maxScal = new Scalar(maxValor,maxValor,maxValor);
        Core.divide(colourConstantImage,maxScal,colourConstantImage);
        colourConstantImage.convertTo(colourConstantImage,CvType.CV_8UC3);



        return colourConstantImage;
    }


    private Mat applyOneChannel(Mat isignal){
        Log.d(TAG,"start applyOneChannel");
        double centreSize = 3.0;
        double gaussianSigma = 1.5;
        double contrastEnlarge = 2.0;
        double surroundEnlarge = 5.0;
        double s1 = -0.77;
        double s4 = -0.67;
        double c1 = 1.0;
        double c4 = 1.0;
        double nk = 4.0;

        SigmaTemplate st = relativePixelContrast(isignal,centreSize,surroundEnlarge*centreSize);
        Mat rgc = st.getSigmaCentre();

        Mat rgs = st.getSigmaSurround();

        Scalar mrgc = Core.mean(rgc);
        Scalar mrgs = Core.mean(rgs);

        c1 = c1 + mrgc.val[0];
        c4 = c4 + mrgs.val[0];
        Mat ab = SingleContrast(isignal,gaussianSigma,contrastEnlarge,nk);

        Mat ba = SingleGaussian(isignal,gaussianSigma*surroundEnlarge);

        double[] ss = linspace(s1,s4,nk);
        double[] cs = linspace(c1,c4,nk);

        Mat dorg = applyNeighbourInpact(isignal,ab,ba,ss,cs);
        return dorg;

    }

    private SigmaTemplate relativePixelContrast(Mat inputImage, double centreSize, double surroundSize){

        Log.d(TAG,"Start relativePixelContrast");
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
        Log.d(TAG,"Start stdFilt");
        Mat out;
        Point anchor;

        Scalar noZero = new Scalar(Core.countNonZero(h));
        Log.d(TAG,"noZero");
        Log.d(TAG,String.valueOf(noZero));
        Core.divide(h,noZero,h);
        double delta = 0;
        anchor = new Point(-1,-1);
        out = image.clone();

        Imgproc.filter2D(image,out,-1,h,anchor,delta,Core.BORDER_DEFAULT);
        Core.absdiff(out,image,out);
        Core.pow(out,2.0,out);

        return out;
    }

    private Mat SingleContrast(Mat isignal,double startingsigma, double contrastenlarge, double nContrastLevels){
        Log.d(TAG,"Start SingleContrast");
        double rows = isignal.rows();
        double cols = isignal.cols();
        int[] arg1 = new int[2];
        arg1[0] = 17;
        arg1[1] = 1;
        int[] arg2 = new int[2];
        arg2[0] = 1;
        arg2[1] = 17;
        Mat contrastImx = getContrastImage(isignal, arg1);
        Mat contrastImy = getContrastImage(isignal,arg2);
        //Log.d(TAG,"contrastImx");
        //Log.d(TAG,contrastImx.dump());
        //Log.d(TAG,"contrastimY");
        //Log.d(TAG,contrastImy.dump());

        double finishSigma = startingsigma * contrastenlarge;
        double[] sigmas = linspace(startingsigma,finishSigma,nContrastLevels);
        Log.d(TAG,"Sigmas");
        Log.d(TAG,String.valueOf(sigmas[0])+ " " + String.valueOf(sigmas[1])+" " + String.valueOf(sigmas[2])+" " + String.valueOf(sigmas[3]));
        Mat contrastLevelX = getContrastLevels(contrastImx,nContrastLevels);
        Mat contrastLevelY = getContrastLevels(contrastImy,nContrastLevels);
        Log.d(TAG,"contrastLevelX");
        Log.d(TAG,contrastLevelX.dump());
        Log.d(TAG,"contrastLevelY");
        Log.d(TAG,contrastLevelY.dump());
        double[] nContrastLevelsX = unique(contrastLevelX);
        Log.d(TAG,"nContrastLevelX" + String.valueOf(nContrastLevelsX[0]));
        double[] nContrastLevelY = unique(contrastLevelY);
        Log.d(TAG,"nContrastLevelY" + String.valueOf(nContrastLevelY[0]));
        Mat rfresponse = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));

        double lambdaxi;

        double lambdayi;
        for (double i: nContrastLevelsX) {
            lambdaxi = sigmas[(int)i];
            for (double j:nContrastLevelY) {
                lambdayi = sigmas[(int)j];
                Size size = new Size(lambdaxi,lambdayi);
                Mat rfi = new Mat();
                Imgproc.GaussianBlur(rfresponse,rfi,size,0,0); //no es un gaussianblur comprovar
                Mat fresponsei = new Mat();
                Imgproc.filter2D(isignal,fresponsei,0,rfi);
                for(int x=0;x<rfresponse.rows();x++){
                    for(int y=0;y<rfresponse.cols();y++){
                        double[] cx = new double[1];
                        contrastLevelX.get(x,y,cx);
                        double[] cy = new double[1];
                        contrastLevelY.get(x,y,cy);
                        if(i == cx[0] && j == cy[0]){
                            double[] fr = new double[1];
                            fresponsei.get(x,y,fr);
                            rfresponse.put(x,y,fr);
                        }
                    }

                }

            }
            
        }

        return rfresponse;

    }

    private double[] linspace(double d1,double d2,double n){
        Log.d(TAG,"Start linspace");
        n = Math.floor(n);
        double n1 = n -1;
        double step = d2-d1;
        step = step/n1;
        //Log.d(TAG,"startP: " + String.valueOf(startP) + "endP: " + String.valueOf(endP) + "interval: " + String.valueOf(interval));
        double[] y = new double[(int)n];
        //double c = (d2-d1)*(n1-1);

        for(int i = 0; i<n;i++){
            if(y[0] != d1){
                y[0] = d1;
            }else{
                y[i] = d1+step*i;
            }
        }
        return y;

    }

    private Mat getContrastLevels(Mat contrastIm, double nContrastLevels){
        Log.d(TAG,"Start getContrastlevel");
        Core.MinMaxLocResult minMaxLocResult =Core.minMaxLoc(contrastIm);
        double min = minMaxLocResult.minVal;
        Log.d(TAG,"min: " + min);
        double max = minMaxLocResult.maxVal;
        Log.d(TAG,"max: " + max);
        double step = ((max-min)/nContrastLevels);
        Log.d(TAG,"step: " + step);
        ArrayList<Double> levels = new ArrayList<>();
        double i = min;
        //levels.add(i);
        for(int k = 1;k<nContrastLevels;k++){
            i = i+step;
            levels.add(i);
        }

        int size = levels.size();
        //Log.d(TAG,"size gCL: " + String.valueOf(size));
        //Log.d(TAG,"levels: " + levels);
        //levels.remove(0);

        //Log.d(TAG,"size gCL: " + String.valueOf(size));
        //Log.d(TAG,"levels: " + levels);

        return imQuantize(contrastIm,levels);

    }

    //private Mat imQuantize(Mat contrastIm,ArrayList<Double> levels){}

    public Mat getContrastImage(Mat isignal, int[] surroundSize){
        Log.d(TAG,"start getContrastImage");
        double[] centreSize = new double[2];
        centreSize[0] = 0;
        centreSize[1] = 0;
        //Log.d(TAG,"isignal");
        //Log.d(TAG,isignal.dump());
        Mat contrastStd = LocalstdContrast(isignal,surroundSize,centreSize);
        //Log.d(TAG,"contrastStd");
        //Log.d(TAG,contrastStd.dump());
        //Log.d(TAG,String.valueOf(contrastStd.cols()) + " " + String.valueOf(contrastStd.rows()));
        Mat contrastImg = new Mat(contrastStd.rows(),contrastStd.cols(),contrastStd.type(),new Scalar(1.0));

        Mat out = new Mat(contrastStd.rows(),contrastStd.cols(),CvType.CV_64F);
        Core.subtract(contrastImg,contrastStd,out);
        //Log.d(TAG,"out");
        String dumo = out.dump();
        //Log.d(TAG,dumo);

        return  out;


    }

    private double[] unique(Mat src){
        Log.d(TAG,"Start unique");
        ArrayList<Double> o = new ArrayList<>();
        if(src.type() != CvType.CV_32FC1){
            src.convertTo(src,CvType.CV_32FC1);
        }
        for(int i = 0;i<src.rows();i++){
            for(int j = 0; j<src.cols();j++){
                if(o.isEmpty()){
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

    private Mat matChansMulK(Mat inputImage, double[] k){
        Log.d(TAG,"Strat matChansMulK");

        List<Mat> listMat = new ArrayList<>();
        Core.split(inputImage,listMat);
        Mat blue = new Mat();
        Mat green = new Mat();
        Mat red = new Mat();

        Scalar scalBlue = new Scalar(k[2]);
        Scalar scalGreen = new Scalar(k[1]);
        Scalar scalRed = new Scalar(k[0]);

        Core.multiply(listMat.get(0),scalBlue,blue);
        Core.multiply(listMat.get(1),scalGreen,green);
        Core.multiply(listMat.get(2),scalRed,red);

        List<Mat> list = Arrays.asList(blue,green,red);
        Mat out = new Mat();

        Core.merge(list,out);

        return out;

    }



    private Mat SingleGaussian(Mat isignal,double startingSigma){

        Log.d(TAG,"start singlGaussian");

        Mat rf = Imgproc.getGaussianKernel(1,startingSigma);
        Imgproc.filter2D(isignal,isignal,0,rf);

        return isignal;
    }

    private Mat applyNeighbourInpact(Mat isignal, Mat ab, Mat ba, double[] surroundImpacts, double[] centreImpacts){
        Log.d(TAG,"start applyNeighbourInpact");
        int nContrastLevels = surroundImpacts.length;
        int[] surroundSize = {17,17};
        Mat contrastImage = getContrastImage(isignal,surroundSize);
        Mat contrastLevels = getContrastLevels(contrastImage,nContrastLevels);
        double[] nconLevels = unique(contrastLevels);

        Scalar zero = new Scalar(0);
        Mat osignal = new Mat(isignal.rows(),isignal.cols(),CvType.CV_64FC1,zero);
        for (double i: nconLevels) {
            //demanar arash
        }
        return osignal;

    }

    private Mat imQuantize(Mat in, ArrayList<Double> levels){
        Log.d(TAG,"start imQuantize");

        Mat index = new Mat(in.rows(),in.cols(),CvType.CV_64FC1,new Scalar(1));
        int size = levels.size();
        Log.d(TAG,"size; " + size);
        Log.d(TAG,"levels: " + levels);
        double[] pixel;
        for(int i = 0; i<in.cols();i++){
            for(int j = 0; j<in.rows();j++){
                pixel = in.get(j,i);
                if(pixel[0] <= levels.get(0)){
                    index.put(j,i,1);
                }
                else if(pixel[0] >= levels.get(levels.size()-1)){
                    index.put(j,i,size+1);
                    //Log.d(TAG,"mes gran");
                    //Log.d(TAG,"pixel: " + String.valueOf(pixel[0]) + " valor: "+ String.valueOf(size+1));
                    //Log.d(TAG,"level: " + String.valueOf(levels.get(size-1)) + " " + String.valueOf(levels.get(size-1)));

                }
                else{
                    for(int k = 0;k<size-1;k++){
                        if(levels.get(k)< pixel[0] && pixel[0] <= levels.get(k+1)){

                            index.put(j,i,k+2);
                            //Log.d(TAG,"pixel: " + String.valueOf(pixel[0]) + " k: " + k + " valor: "+ String.valueOf(k+1));
                            //Log.d(TAG,"level: " + String.valueOf(levels.get(k)) + " " + String.valueOf(levels.get(k+1)));
                        }
                    }

                }

            }
        }

        return index;
    }

    private Mat LocalstdContrast(Mat inputImage, int[] windowSize, double[] centreSize){
        Log.d(TAG,"strat LocalstdContrast");

        Mat kernel = new Mat(windowSize[0],windowSize[1],CvType.CV_64F);
        kernel.setTo(new Scalar(0.0588));
        Point anchor = new Point(centreSize[0],centreSize[1]);
        Mat dest = new Mat(inputImage.rows(),inputImage.cols(),CvType.CV_64FC1);
        //Imgproc.filter2D(inputImage,dest,inputImage.depth(),kernel);
        Imgproc.filter2D(inputImage,dest,-1,kernel,anchor,0,Core.BORDER_DEFAULT);
        //Log.d(TAG,"dest");
        //Log.d(TAG,dest.dump());
        Mat stdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Core.subtract(inputImage,dest,stdv);
        //Log.d(TAG,"stdv");
        //Log.d(TAG,stdv.dump());
        Core.pow(stdv,2,stdv);
        //Log.d(TAG,"stdv pow");
        //Log.d(TAG,stdv.dump());
        Mat meanstdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Imgproc.filter2D(stdv,meanstdv,-1,kernel,anchor,0,Core.BORDER_DEFAULT);
        Core.sqrt(meanstdv,meanstdv);
        //Log.d(TAG,"meanstdv");
        //Log.d(TAG,meanstdv.dump());
        return meanstdv;
    }

    private Scalar calculateLuminanceSingle(Mat modelResponse, Mat image){
        Log.d(TAG,"strat calculateLuminanceSingle");
        ArrayList<Mat> listMat = new ArrayList<>();
        Core.split(image,listMat);
        Core.MinMaxLocResult maxBlue = Core.minMaxLoc(listMat.get(0));
        Core.MinMaxLocResult maxGreen = Core.minMaxLoc(listMat.get(1));
        Core.MinMaxLocResult maxRed = Core.minMaxLoc(listMat.get(2));

        double SaturationThreshold;
        if(maxBlue.maxVal >= maxGreen.maxVal && maxBlue.maxVal >= maxRed.maxVal){
            SaturationThreshold = maxBlue.maxVal;
        }else if(maxGreen.maxVal >= maxRed.maxVal){
            SaturationThreshold = maxGreen.maxVal;
        }else{
            SaturationThreshold = maxRed.maxVal;
        }

        double DarkThreshold;
        if(maxBlue.minVal <= maxGreen.minVal && maxBlue.minVal <= maxRed.minVal){
            DarkThreshold = maxBlue.minVal;
        }else if(maxGreen.minVal <= maxRed.minVal){
            DarkThreshold = maxGreen.minVal;
        }else{
            DarkThreshold = maxRed.minVal;
        }

        Mat maxImage = new Mat(image.rows(),image.cols(),CvType.CV_64FC1);
        Mat minImage = new Mat(image.rows(),image.cols(),CvType.CV_64FC1);

        for (int i = 0; i<image.rows();i++){
            for (int j = 0;j<image.cols();j++){
                double[] pixels = image.get(i,j);
                if (pixels[0] > pixels[1] && pixels[0] > pixels[1]){
                    maxImage.put(i,j,pixels[0]);
                    if(pixels[1] <= pixels[2]){
                        minImage.put(i,j,pixels[1]);
                    }else {
                        minImage.put(i,j,pixels[2]);
                    }
                }else if (pixels[1] >= pixels[2]){
                    maxImage.put(i,j,pixels[1]);
                    if(pixels[0] <= pixels[2]){
                        minImage.put(i,j,pixels[0]);
                    }else{
                        minImage.put(i,j,pixels[2]);
                    }
                }else{
                    maxImage.put(i,j,pixels[2]);
                    if(pixels[0] <= pixels[1]){
                        minImage.put(i,j,pixels[0]);
                    }else{
                        minImage.put(i,j,pixels[1]);
                    }
                }
            }
        }
        Mat dilationMat = new Mat(maxImage.rows(),maxImage.cols(),CvType.CV_8U,new Scalar(0));
        double[] pixelMax = new double[1];
        double[] pixelMin = new double[1];
        for(int i = 0;i<maxImage.rows();i++){
            for(int j = 0;j<maxImage.cols();j++){
                maxImage.get(i,j,pixelMax);
                minImage.get(i,j,pixelMin);
                if(pixelMax[0] >= SaturationThreshold || pixelMin[0] <= DarkThreshold){
                    dilationMat.put(i,j,1);
                }else{
                    dilationMat.put(i,j,0);
                }
            }
        }

        Mat saturatedPixels = dilation33(dilationMat);
        double sigma = 2;
        saturatedPixels = set_border(saturatedPixels,sigma +1);
        List<Mat> modelList = new ArrayList<Mat>();
        Core.split(modelResponse,modelList);
        Core.multiply(modelList.get(0),modelList.get(0),modelList.get(0));
        Core.multiply(modelList.get(1),modelList.get(1),modelList.get(1));
        Core.multiply(modelList.get(2),modelList.get(2),modelList.get(2));
        Core.multiply(modelList.get(0),saturatedPixels,modelList.get(0));
        Core.multiply(modelList.get(1),saturatedPixels,modelList.get(1));
        Core.multiply(modelList.get(2),saturatedPixels,modelList.get(2));


        double centreSize = Math.floor(Math.min(image.rows(),image.cols()) * 0.01);
        if((centreSize % 2) == 0){
            centreSize --;
        }

        Core.MinMaxLocResult modelResponseblue = Core.minMaxLoc(modelList.get(0));
        Core.MinMaxLocResult modelResponsegreen = Core.minMaxLoc(modelList.get(1));
        Core.MinMaxLocResult modelResponsered = Core.minMaxLoc(modelList.get(2));
        double maxModelResponse;
        if(modelResponseblue.maxVal > modelResponsegreen.maxVal && modelResponseblue.maxVal > modelResponsered.maxVal){
            maxModelResponse = modelResponseblue.maxVal;
        }else if(modelResponsegreen.maxVal >= modelResponsered.maxVal){
            maxModelResponse = modelResponseblue.maxVal;
        }else{
            maxModelResponse = modelResponsered.maxVal;
        }
        Scalar scalmaxmr = new Scalar(maxModelResponse,maxModelResponse,maxModelResponse);
        Core.merge(listMat,modelResponse);
        Core.divide(modelResponse,scalmaxmr,modelResponse);
        double[] cs = {centreSize,centreSize};
        int[] ws = {5,5};
        Mat stdImg = LocalstdContrast(modelResponse,ws, cs);
        Scalar Cutoff = Core.mean(stdImg);
        double meanCutOff = (Cutoff.val[0]+Cutoff.val[1]+Cutoff.val[2])/3;
        Scalar normRGB = new Scalar(255,255,255);
        Core.multiply(modelResponse,normRGB,modelResponse);

        Mat tmp = new Mat(modelResponse.rows(),modelResponse.cols(),CvType.CV_64FC1);
        for(int i = 0; i<tmp.rows();i++){
            for(int j = 0; j<tmp.cols();j++){
                double[] values = saturatedPixels.get(i,j);
                if(values[0] == 1){
                    tmp.put(i,j,modelResponse.get(i,j));
                }
            }
        }
        List<Mat> listTmp = new ArrayList<>();
        Core.split(tmp,listTmp);
        double valor1 = PoolingHistMax(listTmp.get(0),meanCutOff,false);
        double valor2 = PoolingHistMax(listTmp.get(0),meanCutOff,false);
        double valor3 = PoolingHistMax(listTmp.get(0),meanCutOff,false);
        Scalar luminance = new Scalar(valor1,valor2,valor3);
        return luminance;
    }

    private Mat dilation33(Mat in){
        Log.d(TAG,"start dilation33");
        double hh = in.rows();
        double ll = in.cols();
        Mat t = new Mat();
        return t;
    }

    private Mat set_border(Mat in, double width){
        Log.d(TAG,"start set_border");
        Mat temp = new Mat(in.rows(),in.cols(),in.type(),new Scalar(1));
        for(int i = 0;i<temp.rows();i++){
            for(int j = 0;j<temp.cols();j++){
                if( i<width){
                    temp.put(i,j,0);
                }else if(i>= temp.rows() -width){
                    temp.put(i,j,0);
                }else if(j < width){
                    temp.put(i,j,0);
                }else if(j>= temp.cols() -width){
                    temp.put(i,j,0);
                }
            }
        }
        Mat out = new Mat();
        Core.multiply(temp,in,out);
        return out;

    }

    private double PoolingHistMax(Mat in,double cutoof,boolean useaAveragePixels){
        Log.d(TAG,"start PoolingHistMax");
        double nPixels = in.cols() * in.rows();
        Core.MinMaxLocResult maxR = Core.minMaxLoc(in);
        double max = maxR.maxVal;
        if(max == 0){
            return 0;
        }
        int nbins = 0;
        if(max < 256){
            nbins = 256;
        }else if(max < 65536){
            nbins = 65536;
        }
        double lowerMaxPixels = cutoof * nPixels;
        double upperMaxPixels = lowerMaxPixels * 1.5;
        Mat ichan = in;
        Mat his = new Mat();
        Mat mas = null;
        MatOfInt size = new MatOfInt(nbins);
        MatOfFloat range = new MatOfFloat(0,256);
        List<Mat> listMat = new ArrayList<>();
        listMat.add(ichan);
        MatOfInt c = new MatOfInt(0);
        Imgproc.calcHist(listMat,c,mas,his,size,range);
        return 0.0;

    }
}
