package tfg.uab.jga.appopencv;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.LoginFilter;
import android.util.Log;
import java.text.DecimalFormat;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
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
    private static String TAG = "ProcessImage";
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


    public Mat surroundModulation(Mat inputImage){

        long time1 = SystemClock.currentThreadTimeMillis();
        Log.d(TAG,"start surrond Modulation");
        //Log.d(TAG,"rows: "+ String.valueOf(rows) + "columns: " + String.valueOf(columns) + "channels: " + String.valueOf(channels));
        double maxVal = 255.0;
        Mat src = new Mat();
        inputImage.convertTo(src, CvType.CV_64FC3); //convert MAT to double precision with 3 channels

        Scalar scalar = new Scalar(maxVal,maxVal,maxVal);
        Core.divide(src,scalar,src); //divide de Mat
        final List<Mat> listRGB = new ArrayList<>();
        Core.split(src,listRGB);

        final Mat[] arrayMat = new Mat[3];

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Mat dorg = applyOneChannel(listRGB.get(2));//red
                arrayMat[2] = dorg;
            }
        });
        t.start();
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                Mat doyb = applyOneChannel(listRGB.get(1));//green
                arrayMat[1] = doyb;
            }
        });
        t2.start();
        Thread t3 = new Thread(new Runnable() {
            @Override
            public void run() {
                Mat dowb = applyOneChannel(listRGB.get(0));//blue
                arrayMat[0] = dowb;
            }
        });
        t3.start();
        /*
        Log.d(TAG,"onechanel red");
        Mat dorg = applyOneChannel(listRGB.get(2));//red
        Log.d(TAG,"onechanel green");
        Mat doyb = applyOneChannel(listRGB.get(1));//green
        Log.d(TAG,"onechanel blue");
        Mat dowb = applyOneChannel(listRGB.get(0));//blue*/


        try {
            t.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //merge the 3 maps in 1 with 3 channels
        //List<Mat> listMat = Arrays.asList(dowb,doyb,dorg); //BGR
        List<Mat> listMat = Arrays.asList(arrayMat[0],arrayMat[1],arrayMat[2]);
        //Log.d(TAG,"doresponse");
        Mat dorespons = new Mat();
        /*Log.d(TAG,"red channel");
        Log.d(TAG,arrayMat[2].dump());
        Log.d(TAG,arrayMat[2].row(12).dump());
        Log.d(TAG,arrayMat[2].row(13).dump());
        Log.d(TAG,arrayMat[2].row(14).dump());
        Log.d(TAG,arrayMat[2].row(15).dump());
        Log.d(TAG,"green channel");
        Log.d(TAG,arrayMat[1].dump());
        Log.d(TAG,arrayMat[1].row(12).dump());
        Log.d(TAG,arrayMat[1].row(13).dump());
        Log.d(TAG,arrayMat[1].row(14).dump());
        Log.d(TAG,arrayMat[1].row(15).dump());
        Log.d(TAG,"blue channel");
        Log.d(TAG,arrayMat[0].dump());
        Log.d(TAG,arrayMat[0].row(12).dump());
        Log.d(TAG,arrayMat[0].row(13).dump());
        Log.d(TAG,arrayMat[0].row(14).dump());
        Log.d(TAG,arrayMat[0].row(15).dump());*/
        Core.merge(listMat,dorespons);

        Log.d(TAG,"calulate luminance");
        Scalar luminance = calculateLuminanceSingle(dorespons,inputImage);
        //luminance = new Scalar(214.6582,126.1673,103.2881);
        Log.d(TAG,"luminance: " + String.valueOf(luminance.val[0])+" "+ String.valueOf(luminance.val[1])+" "+ String.valueOf(luminance.val[2]));
        double sumluminance = luminance.val[2] +luminance.val[1]+luminance.val[0]; //sum of the values
        //divide each value per sum of all values
        Scalar luminancedouble = new Scalar(luminance.val[0]/sumluminance,luminance.val[1]/sumluminance,luminance.val[2]/sumluminance);

        for(int i = 0;i<3;i++){
            luminancedouble.val[i] = 1/luminancedouble.val[i];
        }
        //Log.d(TAG,"luminancedouble: " + String.valueOf(luminancedouble.val[0])+" "+ String.valueOf(luminancedouble.val[1])+" "+ String.valueOf(luminancedouble.val[2]));


        //Mat colourConstantImage = matChansMulK(src,luminancedouble); //mutiplicamos cada canal por su luminance
        //Log.d(TAG,"multiply");
        Mat colourConstantImage = new Mat();
        src.convertTo(src,CvType.CV_32FC3);
        //Log.d(TAG,"src");
        //Log.d(TAG,src.dump());


        Core.multiply(src,luminancedouble,colourConstantImage);

        //Log.d(TAG,"coloutConstantImage after marchansmulk");
        Mat temp = colourConstantImage;
        List<Mat> lis = new ArrayList<>();
        Core.split(temp,lis);

        //Log.d(TAG,"red");
        //Log.d(TAG,lis.get(2).dump());
        //Log.d(TAG,"green");
        //Log.d(TAG,lis.get(1).dump());
        //Log.d(TAG,"blue");
        //Log.d(TAG,lis.get(0).dump());
        //cogemos el max de la matrix

        ArrayList<Mat> list = new ArrayList<>();
        Core.split(colourConstantImage,list);
        //Log.d(TAG,list.get(0).dump());
        //Log.d(TAG,list.get(1).dump());
        //Log.d(TAG,list.get(2).dump());
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
        Core.multiply(colourConstantImage,scalar,colourConstantImage);
        //Mat t = colourConstantImage;
       // List<Mat> li = new ArrayList<>();
        //Core.split(t,li);

        /*Log.d(TAG,"blue");
        Log.d(TAG,li.get(0).dump());
        Log.d(TAG,"green");
        Log.d(TAG,li.get(1).dump());
        Log.d(TAG,"red");
        Log.d(TAG,li.get(2).dump());*/


        colourConstantImage.convertTo(colourConstantImage,CvType.CV_8UC3);



        //Log.d(TAG,"end");
       // Log.d(TAG,colourConstantImage.dump());
        long time2 = SystemClock.currentThreadTimeMillis();
        Log.d(TAG,"time mili; " + String.valueOf(time2-time1));
        return colourConstantImage;
    }


    private Mat applyOneChannel(Mat isignal){
        //Log.d(TAG,"start applyOneChannel");
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
        //Log.d(TAG,"rgc");
        //Log.d(TAG,rgc.dump());

        Mat rgs = st.getSigmaSurround();
        //Log.d(TAG,"rgs");
        //Log.d(TAG,rgs.dump());

        Scalar mrgc = Core.mean(rgc);
        Scalar mrgs = Core.mean(rgs);

        c1 = c1 + mrgc.val[0];
        c4 = c4 + mrgs.val[0];
        Mat ab = SingleContrast(isignal,gaussianSigma,contrastEnlarge,nk);
        //Log.d(TAG,"ab");
        //Log.d(TAG,ab.dump());
        Mat ba = SingleGaussian(isignal,gaussianSigma*surroundEnlarge);
        //Log.d(TAG,"ba");
        //Log.d(TAG,ba.dump());

        double[] ss = linspace(s1,s4,nk);
        //Log.d(TAG,"ss");
        //Log.d(TAG,Arrays.toString(ss));

        double[] cs = linspace(c1,c4,nk);
        //Log.d(TAG,"cs");
        //Log.d(TAG,Arrays.toString(cs));

        Mat dorg = applyNeighbourInpact(isignal,ab,ba,ss,cs);
        /*Log.d(TAG,"dorg");
        Log.d(TAG,dorg.dump());
        Log.d(TAG,dorg.row(13).dump());
        Log.d(TAG,dorg.row(14).dump());
        Log.d(TAG,dorg.row(15).dump());*/
        //Log.d(TAG,"end applyOneChannel");
        return dorg;

    }

    private SigmaTemplate relativePixelContrast(Mat inputImage, double centreSize, double surroundSize){

        //Log.d(TAG,"Start relativePixelContrast");
        Scalar oneScalar = new Scalar(1);
        Mat hc = new Mat((int)centreSize,(int)centreSize,CvType.CV_64FC1,oneScalar);
        Mat hs = new Mat ((int)surroundSize,(int) surroundSize,CvType.CV_64FC1,oneScalar);
        //double hcx = hc.rows();
        //double hcy = hc.cols();
        double d[] = new double[2];

        d[0] = 1;
        d[1] = 1;
        double hsx = hs.rows();
        double hsy = hs.cols();
        double[] m = new double[2];
        m[0] = (hsx+1)/2;
        m[1] = (hsy+1)/2;
        double zero = 0;
        for(int i = 6;i<9;i++){
            for(int j = 6;j<9;j++){
                hs.put(i,j,zero);
            }
        }

        //Log.d(TAG,"hc");
        //Log.d(TAG, hc.dump());

        //Log.d(TAG,"hs");
        ///Log.d(TAG,hs.dump());


        Mat SigmaCentre = stdfilt(inputImage,hc);
        Mat SigmaSurround = stdfilt(inputImage,hs);

        //Log.d(TAG,"sigmacentre");
        //Log.d(TAG,SigmaCentre.dump());
        //Log.d(TAG,"sigmasurrund");
        //Log.d(TAG,SigmaSurround.dump());

        SigmaTemplate st = new SigmaTemplate(SigmaCentre,SigmaSurround);
        //Log.d(TAG,"end relativepixelcontrast");
        return st;


    }

    private Mat stdfilt(Mat image, Mat h){
        Point anchor = new Point(-1,-1);
        double delta = 0;

        Mat G1 = new Mat();
        Mat G2 = new Mat();
        Mat I2 = new Mat();
        Scalar n = Core.sumElems(h);
        Scalar n2 = new Scalar(n.val[0]-1);
        Core.pow(image,2.0,I2);
        Mat h2 = new Mat();
        Core.divide(h,n2,h2);
        Imgproc.filter2D(I2,G2,-1,h2,anchor,delta,Core.BORDER_REFLECT);
        //Log.d(TAG,"G2");
        //Log.d(TAG,G2.dump());

        //Core.divide(G2,n2,G2);
        Mat h3 = new Mat();
        double sh = n.val[0]*n2.val[0];
        double shq = Math.sqrt(sh);
        Scalar shqS = new Scalar(shq);
        //Log.d(TAG,"sqrt: " + String.valueOf(shqS.val[0]));
        Core.divide(h,shqS,h3);
        //Log.d(TAG,"h3");
        //Log.d(TAG,h3.dump());

        Imgproc.filter2D(image,G1,-1,h3,anchor,delta,Core.BORDER_REFLECT);

        //n.val[0] = n.val[0] * n2.val[0];
        //Core.divide(G1,n,G1);
        Core.pow(G1,2.0,G1);
        //Log.d(TAG,"G1");
        //Log.d(TAG,G1.dump());
        Core.absdiff(G2,G1,G2);
        Core.max(G2,new Scalar(0),G2);
        Core.sqrt(G2,G2);
        //Log.d(TAG,"return");
        //Log.d(TAG,G2.dump());
        return G2;

    }


    private Mat SingleContrast(Mat isignal, double startingsigma, double contrastenlarge, double nContrastLevels){
        //Log.d(TAG,"Start SingleContrast");
        double rows = isignal.rows();
        double cols = isignal.cols();
        int[] arg1 = new int[2];
        arg1[0] = 17;
        arg1[1] = 1;
        int[] arg2 = new int[2];
        arg2[0] = 1;
        arg2[1] = 17;
        Mat contrastImx = getContrastImage(isignal, arg1);
       /* Log.d(TAG,"contrastImx");
        Log.d(TAG,contrastImx.dump());
        Log.d(TAG,contrastImx.row(13).dump());
        Log.d(TAG,contrastImx.row(14).dump());
        Log.d(TAG,contrastImx.row(15).dump());*/

        Mat contrastImy = getContrastImage(isignal,arg2);
        /*Log.d(TAG,"contrastImy");
        Log.d(TAG,contrastImy.dump());
        Log.d(TAG,contrastImy.row(13).dump());
        Log.d(TAG,contrastImy.row(14).dump());
        Log.d(TAG,contrastImy.row(15).dump());
*/
        double finishSigma = startingsigma * contrastenlarge;
        double[] sigmas = linspace(startingsigma,finishSigma,nContrastLevels);

        Mat contrastLevelX = getContrastLevels(contrastImx,nContrastLevels);
        contrastLevelX.convertTo(contrastLevelX,CvType.CV_32S);

        Mat contrastLevelY = getContrastLevels(contrastImy,nContrastLevels);
        contrastLevelY.convertTo(contrastLevelY,CvType.CV_32S);

        /*Log.d(TAG,"contrastLevelX");
        Log.d(TAG,contrastLevelX.dump());
        Log.d(TAG,"contrastLevelY");
        Log.d(TAG,contrastLevelY.dump());*/

        double[] nContrastLevelsX = new double[4];
        nContrastLevelsX[0] = 1.0;
        nContrastLevelsX[1] = 2.0;
        nContrastLevelsX[2] = 3.0;
        nContrastLevelsX[3] = 4.0;

        double[] nContrastLevelY = new double[4];
        nContrastLevelY[0] = 1.0;
        nContrastLevelY[1] = 2.0;
        nContrastLevelY[2] = 3.0;
        nContrastLevelY[3] = 4.0;

        Mat rfresponse = new Mat((int)rows,(int)cols,CvType.CV_64FC1,new Scalar(0));

        double lambdaxi;
        double lambdayi;

        Point anchor = new Point(-1,-1);
        double delta = 0;

        Scalar iS = new Scalar(0);
        Scalar jS = new Scalar(0);
        Mat maskX = new Mat();
        Mat maskY = new Mat();
        Mat mask = new Mat();
        Scalar norm = new Scalar(255);
        for (double i: nContrastLevelsX) {
            lambdaxi = sigmas[(int)i-1];
            iS.val[0] = i;
            Core.compare(contrastLevelX,iS,maskX,Core.CMP_EQ);
            Core.divide(maskX,norm,maskX);
            for (double j:nContrastLevelY) {
                lambdayi = sigmas[(int)i-1];

                Mat rfi = GaussianFilter2(lambdaxi,lambdayi,0,0);
                //Log.d(TAG,"rfi");
                //Log.d(TAG,rfi.dump());
                Mat fresponsei = new Mat();
                Imgproc.filter2D(isignal,fresponsei,-1,rfi,anchor,delta,Core.BORDER_REPLICATE );
                //Log.d(TAG,"i: " + String.valueOf(i)+" j: "+String.valueOf(j));
                //Log.d(TAG,"fresponsei");
                //Log.d(TAG,fresponsei.dump());


                jS.val[0] = j;
                Core.compare(contrastLevelY,jS,maskY,Core.CMP_EQ);
                Core.divide(maskY,norm,maskY);
                Core.multiply(maskX,maskY,mask);
                //Log.d(TAG,"maskX");
                //Log.d(TAG,maskX.dump());
                //Log.d(TAG,"maskY");
                //Log.d(TAG,maskY.dump());
                //Log.d(TAG,"mask");
                //Log.d(TAG,mask.dump());
                fresponsei.copyTo(rfresponse,mask);
                //Log.d(TAG,"rfresponse");
                //Log.d(TAG,rfresponse.dump());
             ;

            }
            
        }
        /*Log.d(TAG,"rfresponse");
        Log.d(TAG,rfresponse.dump());
        Log.d(TAG,rfresponse.row(13).dump());
        Log.d(TAG,rfresponse.row(14).dump());
        Log.d(TAG,rfresponse.row(15).dump());*/

        //Log.d(TAG,"end singlecontrast");
        return rfresponse;

    }

    public double[] linspace(double d1,double d2, double n){
        //Log.d(TAG,"Start linspace");
        n = Math.floor(n);
        double n1 = n -1;
        double step = d2-d1;
        step = step/n1;
        double[] y = new double[(int)n];


        for(int i = 0; i<n;i++){

            y[i] = d1+step*i;

        }
        //Log.d(TAG,"end linspace");
        return y;
    }



    private Mat getContrastLevels(Mat contrastIm, double nContrastLevels){
        //Log.d(TAG,"Start getContrastlevel");
        Core.MinMaxLocResult minMaxLocResult =Core.minMaxLoc(contrastIm);
        double min = minMaxLocResult.minVal;

        double max = minMaxLocResult.maxVal;

        double step = ((max-min)/nContrastLevels);

        ArrayList<Double> levels = new ArrayList<>();
        double i = min;

        for(int k = 1;k<nContrastLevels;k++){
            i = i+step;
            levels.add(i);
        }
        //Log.d(TAG,"contrastIm");
        //Log.d(TAG,contrastIm.dump());
        //Log.d(TAG,"levels: " + levels);
        //int size = levels.size();

        Mat resum = imQuantize2(contrastIm,levels);
        return resum;

    }



    private Mat getContrastImage(Mat isignal, int[] surroundSize){
        Log.d(TAG,"start getContrastImage");

        //Log.d(TAG,"surroundSize: " + Arrays.toString(surroundSize));

        Mat contrastStd = LocalstdContrast(isignal,surroundSize);
        //Log.d(TAG,"contrastStd");
        //Log.d(TAG,contrastStd.dump());
        Mat contrastImg = new Mat(contrastStd.rows(),contrastStd.cols(),contrastStd.type(),new Scalar(1.0));

        Mat out = new Mat(contrastStd.rows(),contrastStd.cols(),CvType.CV_64F);
        Core.subtract(contrastImg,contrastStd,out);
        //Log.d(TAG,"contrast subtract");
        //Log.d(TAG,out.dump());
        //Log.d(TAG,"end getcontrastimage");
        return  out;


    }

    private double[] unique(Mat src){
        //Log.d(TAG,"Start unique");
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
        Arrays.sort(out);
        //Log.d(TAG,"end unique");
        return out;
    }



    private Mat matChansMulK(Mat inputImage, double[] k){
        //Log.d(TAG,"Strat matChansMulK");

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
        //Log.d(TAG,"end matCHans");
        return out;

    }



    private Mat SingleGaussian(Mat isignal,double startingSigma){

        //Log.d(TAG,"start singlGaussian");
        Point anchor = new Point(-1,-1);
        double delta = 0;
        //Mat rf = Imgproc.getGaussianKernel(1,startingSigma);
        Mat rf = GaussianFilter2(startingSigma,startingSigma,0,0);

        Mat response = new Mat();
        Imgproc.filter2D(isignal,response,-1,rf,anchor,delta,Core.BORDER_REPLICATE);
        //Log.d(TAG,"result");
        //Log.d(TAG,response.dump());
        //Log.d(TAG,"end singlegaussian");
        return response;
    }

    private Mat applyNeighbourInpact(Mat isignal, Mat ab, Mat ba, double[] surroundImpacts, double[] centreImpacts){
        //Log.d(TAG,"start applyNeighbourInpact");
        //Log.d(TAG,"surroundImpacts: " + Arrays.toString(surroundImpacts));
        //Log.d(TAG,"centreImpats: " + Arrays.toString(centreImpacts));

        /*Log.d(TAG,"ab");
        Log.d(TAG,ab.dump());
        Log.d(TAG,ab.row(13).dump());
        Log.d(TAG,ab.row(14).dump());
        Log.d(TAG,ab.row(15).dump());
        Log.d(TAG,"ba");
        Log.d(TAG,ba.dump());
        Log.d(TAG,ba.row(13).dump());
        Log.d(TAG,ba.row(14).dump());
        Log.d(TAG,ba.row(15).dump());*/



        int nContrastLevels = surroundImpacts.length;
        Log.d(TAG,"nContrastLeveks: " + String.valueOf(nContrastLevels));
        int[] surroundSize = {17,17};
        Mat contrastImage = getContrastImage(isignal,surroundSize);
        //Log.d(TAG,"contarstImage");
        //Log.d(TAG,contrastImage.dump());
        Mat contrastLevels = getContrastLevels(contrastImage,nContrastLevels);
        //Log.d(TAG,"contrastLevels");
        //Log.d(TAG,contrastLevels.dump());
        //double[] nconLevels = unique(contrastLevels);
        double[] nconLevels = new double[4];
        nconLevels[0] = 1.0;
        nconLevels[1] = 2.0;
        nconLevels[2] = 3.0;
        nconLevels[3] = 4.0;
        //og.d(TAG,"nconLevels; " + Arrays.toString(nconLevels));
        Scalar zero = new Scalar(0);
        Mat osignal = new Mat(isignal.rows(),isignal.cols(),isignal.type(),zero);

        Scalar maskScalar;
        Mat mask = new Mat();
        Mat newab = new Mat();
        Mat newba = new Mat();
        Mat abba = new Mat();
        double j;
        double k;
        for (double i: nconLevels) {

            //Log.d(TAG,"i: " + String.valueOf(i));
            maskScalar = new Scalar(i);
            Core.compare(contrastLevels,maskScalar,mask,Core.CMP_EQ);
            //Log.d(TAG,"mask");
            //Log.d(TAG,mask.dump());

            ab.copyTo(newab,mask);
            ba.copyTo(newba,mask);

            //Log.d(TAG,"newab");
            //Log.d(TAG,newab.dump());
            //Log.d(TAG,"newba");
            //Log.d(TAG,newba.dump());

            k = surroundImpacts[(int)i-1];
            j = centreImpacts[(int)i-1];
            //Log.d(TAG,"surroundImoact "+String.valueOf(k) + " centreImpact: " + String.valueOf(j));
            Core.multiply(newab,new Scalar(j),newab);
            //Log.d(TAG,"mul newAb");
            //Log.d(TAG,newab.dump());
            Core.multiply(newba,new Scalar(k),newba);
            //Log.d(TAG,"mul newba");
            //Log.d(TAG,newba.dump());
            Core.add(newab,newba,abba);
            //Log.d(TAG,"sum abba");
            //Log.d(TAG,abba.dump());

             abba.copyTo(osignal,mask);
             /*Log.d(TAG,"result");
             Log.d(TAG,osignal.dump());
             Log.d(TAG,osignal.row(13).dump());
             Log.d(TAG,osignal.row(14).dump());
             Log.d(TAG,osignal.row(15).dump());*/



        }
        /*
        Log.d(TAG,"osignal");
        Log.d(TAG,osignal.dump());
        Log.d(TAG,"last rows");
        Log.d(TAG,osignal.row(13).dump());
        Log.d(TAG,osignal.row(14).dump());
        Log.d(TAG,osignal.row(15).dump());
        Log.d(TAG,"end applyneighbourinpact");*/
        return osignal;

    }



    private Mat imQuantize2(Mat in, ArrayList<Double> levels){
        //Log.d(TAG,"start Imquantize");
        Mat out = new Mat(in.rows(),in.cols(),in.type());
        Mat aux = new Mat(in.rows(),in.cols(),in.type());

        int size = levels.size();

        Scalar rangmen = new Scalar(0);
        Scalar rangmaj = new Scalar(0);
        Scalar rang = new Scalar(0);

        Scalar value = new Scalar(1);
        for(int i = 0;i<size+1;i++){
            if(i == 0){
                rang.val[0] = levels.get(0);
                Core.compare(in,rang,aux,Core.CMP_LE);

                out.setTo(value,aux);

            }else if(i == size){
                rang.val[0] = levels.get(size-1);

                Core.compare(in,rang,aux,Core.CMP_GE);

                value.val[0] = size+1;

                out.setTo(value,aux);


            }else{
                rangmen.val[0] = levels.get(i-1);
                rangmaj.val[0] = levels.get(i);
                Core.inRange(in,rangmen,rangmaj,aux);

                value.val[0] = i+1;

                out.setTo(value,aux);

            }
        }

        //Log.d(TAG,"end imquantize");
        return out;
    }

    private Mat LocalstdContrast(Mat inputImage, int[] windowSize){
        Log.d(TAG,"strat LocalstdContrast");

        Mat kernel = new Mat(windowSize[0],windowSize[1],CvType.CV_64F);
        if(windowSize[0] == 17 && windowSize[1] == 17){
            kernel.setTo(new Scalar(0.0035));
        }else if(windowSize[0] == 3){
            kernel.setTo(new Scalar(0.1111));
        }else{
            kernel.setTo(new Scalar(0.0588));
        }

        //Log.d(TAG,"kernel");
        //Log.d(TAG,kernel.dump());
        Point anchor = new Point(-1,-1);
        //Log.d(TAG,"imputImage");
        //Log.d(TAG,inputImage.dump());

        //Log.d(TAG,"centreSize: " + String.valueOf(centreSize[0])+ " "+String.valueOf(centreSize[1]));
        Mat meanCentre = new Mat(inputImage.rows(),inputImage.cols(),CvType.CV_64FC1);

        Imgproc.filter2D(inputImage,meanCentre,-1,kernel,anchor,0,Core.BORDER_REFLECT);
        /*Log.d(TAG,"meanCentre");
        Log.d(TAG,meanCentre.dump());
        Log.d(TAG,meanCentre.row(13).dump());
        Log.d(TAG,meanCentre.row(14).dump());
        Log.d(TAG,meanCentre.row(15).dump());*/
        Mat stdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Core.subtract(inputImage,meanCentre,stdv);
        /*Log.d(TAG,"stdv");
        Log.d(TAG,stdv.dump());
        Log.d(TAG,stdv.row(13).dump());
        Log.d(TAG,stdv.row(14).dump());
        Log.d(TAG,stdv.row(15).dump());*/

        Core.pow(stdv,2,stdv);
        /*Log.d(TAG,"stdv pow");
        Log.d(TAG,stdv.dump());
        Log.d(TAG,stdv.row(13).dump());
        Log.d(TAG,stdv.row(14).dump());
        Log.d(TAG,stdv.row(15).dump());*/


        Mat meanstdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Imgproc.filter2D(stdv,meanstdv,-1,kernel,anchor,0,Core.BORDER_REFLECT);
        /*Log.d(TAG,"meanstdv reflect");
        Log.d(TAG,meanstdv.dump());
        Log.d(TAG,meanstdv.row(13).dump());
        Log.d(TAG,meanstdv.row(14).dump());
        Log.d(TAG,meanstdv.row(15).dump());*/
        Core.sqrt(meanstdv,meanstdv);


        //Log.d(TAG,"end localstdcontrast");
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
        Log.d(TAG,"ST: " + String.valueOf(SaturationThreshold) + " DT: " + String.valueOf(DarkThreshold));

        Mat maxImage = new Mat(image.rows(),image.cols(),CvType.CV_64FC1);
        Mat minImage = new Mat(image.rows(),image.cols(),CvType.CV_64FC1);


        Core.max(listMat.get(0),listMat.get(1),maxImage);
        Core.max(maxImage,listMat.get(2),maxImage);
        //Log.d(TAG,"maxImage");
       // Log.d(TAG,maxImage.dump());


        Core.min(listMat.get(0),listMat.get(1),minImage);
        Core.min (minImage,listMat.get(2),minImage);
        //Log.d(TAG,"minImage");
       // Log.d(TAG,minImage.dump());

        Mat maskMax = new Mat();
        Imgproc.threshold(maxImage,maskMax,SaturationThreshold-1,1.0,Imgproc.THRESH_BINARY);

        Mat minMask = new Mat();
        Imgproc.threshold(minImage,minMask,DarkThreshold,1.0,Imgproc.THRESH_BINARY_INV);

        Mat dilMat = new Mat(image.rows(),image.cols(),CvType.CV_8U,new Scalar(0));

        Scalar v = new Scalar(1);

        dilMat.setTo(v,minMask);
        dilMat.setTo(v,maskMax);
        //Log.d(TAG,"dilMat");
       // Log.d(TAG,dilMat.dump());

        //Log.d(TAG,"start dilation33");
        Mat saturatedPixels = dilation33(dilMat);
        //Log.d(TAG,"end dilation33");
       // Log.d(TAG,"saturedPixels");
       // Log.d(TAG,saturatedPixels.dump());

        Mat masks = new Mat();
        Core.compare(saturatedPixels,new Scalar(0),masks,Core.CMP_EQ);

        Mat s = new Mat(saturatedPixels.rows(),saturatedPixels.cols(),saturatedPixels.type(),new Scalar(0));
        s.setTo(v,masks);


        double sigma = 2;
        Log.d(TAG,"start set_border");
        saturatedPixels = set_border(s,sigma +1);
        Log.d(TAG,"setborder");
       // Log.d(TAG,saturatedPixels.dump());
        List<Mat> modelList = new ArrayList<Mat>();

        Core.split(modelResponse,modelList);
        saturatedPixels.convertTo(saturatedPixels,CvType.CV_32F);
       // Log.d(TAG,"setborder to 32f");
        //Log.d(TAG,saturatedPixels.dump());

        //primer canal
        Mat src = modelList.get(0);
        src.convertTo(src,CvType.CV_32F);


        Mat mask = new Mat(src.rows(),src.cols(),src.type());
        Imgproc.threshold(src,mask,0.0,1.0,Imgproc.THRESH_BINARY);
        Core.multiply(src,mask,src);

        Core.multiply(src,saturatedPixels,src);
        Log.d(TAG,"src 1");
        Log.d(TAG,src.dump());
        modelList.set(0,src);

        //segon canal
        src = modelList.get(1);
        src.convertTo(src,CvType.CV_32F);
        mask = new Mat(src.rows(),src.cols(),src.type());
        Imgproc.threshold(src,mask,0.0,1.0,Imgproc.THRESH_BINARY);

        Core.multiply(src,mask,src);

        Core.multiply(src,saturatedPixels,src);
        Log.d(TAG,"src 2");
        Log.d(TAG,src.dump());
        modelList.set(1,src);


        //tecer canal
        src = modelList.get(2);
        src.convertTo(src,CvType.CV_32F);
        mask = new Mat(src.rows(),src.cols(),src.type());
        Imgproc.threshold(src,mask,0.0,1.0,Imgproc.THRESH_BINARY);

        Core.multiply(src,mask,src);

        Core.multiply(src,saturatedPixels,src);
        Log.d(TAG,"src 3");
        Log.d(TAG,src.dump());
        modelList.set(2,src);


        double centreSize = Math.floor(Math.min(image.rows(),image.cols()) * 0.01);
        if((centreSize % 2) == 0){
            centreSize --;
        }

        if(centreSize<3){
            centreSize = 3;
        }
        //Log.d(TAG,"centresize: " + String.valueOf(centreSize));

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
        Log.d(TAG,"max modelResponse: " + String.valueOf(maxModelResponse));




        Scalar scalmaxmr = new Scalar(maxModelResponse,maxModelResponse,maxModelResponse);
        Core.merge(modelList,modelResponse);
        Core.divide(modelResponse,scalmaxmr,modelResponse);

        List<Mat> listLocalstdCentre = new ArrayList<Mat>();
        Core.split(modelResponse,listLocalstdCentre);
        int[] cs = {(int)centreSize,(int)centreSize};
        //Log.d(TAG,"centreSize " + Arrays.toString(cs));
        Log.d(TAG,"modelresponse divide for maxmodelresponse");
        Log.d(TAG,"1");
        Log.d(TAG,listLocalstdCentre.get(2).dump());
        Log.d(TAG,"2");
        Log.d(TAG,listLocalstdCentre.get(1).dump());
        Log.d(TAG,"3");
        Log.d(TAG,listLocalstdCentre.get(0).dump());

        Mat stdImg = LocalstdContrast(listLocalstdCentre.get(0),cs);
        listLocalstdCentre.set(0,stdImg);
        Log.d(TAG,"stdImg blue");
        Log.d(TAG,stdImg.dump());


        stdImg = LocalstdContrast(listLocalstdCentre.get(1),cs);
        listLocalstdCentre.set(1,stdImg);
        Log.d(TAG,"stdImg green");
        Log.d(TAG,stdImg.dump());

        stdImg = LocalstdContrast(listLocalstdCentre.get(2),cs);
        listLocalstdCentre.set(2,stdImg);
        Log.d(TAG,"stdImg red");
        Log.d(TAG,stdImg.dump());

        Mat stdmodel = new Mat();
        Core.merge(listLocalstdCentre,stdmodel);




        Scalar Cutoff = Core.mean(stdmodel);
        Log.d(TAG,"Cutoff; " + String.valueOf(Cutoff.val[0])+" " + String.valueOf(Cutoff.val[1])+" "+String.valueOf(Cutoff.val[2]));
        double meanCutOff = (Cutoff.val[0]+Cutoff.val[1]+Cutoff.val[2])/3;
        Log.d(TAG,"meanCutOff " + String.valueOf(meanCutOff));
        Scalar normRGB = new Scalar(255,255,255);
        Core.multiply(modelResponse,normRGB,modelResponse);


        //Mat tmp = new Mat(modelResponse.rows(),modelResponse.cols(),CvType.CV_32F);
        double[] maxValors = new double[3];
        double valor;
        List<Mat> listTmp = new ArrayList<>();
        Core.split(modelResponse,listTmp);

        Log.d(TAG,"model response");
        Log.d(TAG,listTmp.get(0).dump());
        Log.d(TAG,listTmp.get(1).dump());
        Log.d(TAG,listTmp.get(2).dump());


        for(int i = 0;i<3;i++){
           // Log.d(TAG,"s pooling");
            valor = PoolingHistMax(listTmp.get(i),saturatedPixels,meanCutOff);
            //Log.d(TAG,"e pooling");
            //Log.d(TAG,"valor Pooling: " + String.valueOf(valor));
            maxValors[i] = valor;

        }



        Scalar luminance = new Scalar(maxValors[0],maxValors[1],maxValors[2]);
        //Log.d(TAG,"end calculateLumiannceSingle");

        return luminance;
    }

    private Mat dilation33(Mat in){
        //Log.d(TAG,"start dilation33");
        int hh = in.rows();
        int ll = in.cols();
        Scalar zero = new Scalar(0);
        Scalar one = new Scalar(1);
        Mat out1chanel = new Mat(in.rows(),in.cols(),in.type(),zero);
        //menys primera fila, ultima fila igual que la ultima
        //Log.d(TAG,"in");
        //Log.d(TAG,in.dump());

        for (int i = 0;i<hh;i++){
            if(i == hh-1){
                in.row(i).copyTo(out1chanel.row(i));
            }else{
                in.row(i+1).copyTo(out1chanel.row(i));
            }
        }
        //Log.d(TAG,"out1channel");
        //Log.d(TAG,out1chanel.dump());


        Mat out12chanel = in; //igual al in

        Mat out13chanel = new Mat(in.rows(),in.cols(),in.type(),zero);


        //menys ultima fila, afegir primera fila igual que primera

       for(int i = 0;i<hh;i++){
           if(i == 0){
               in.row(i).copyTo(out13chanel.row(i));
           }else{
               in.row(i-1).copyTo(out13chanel.row(i));
           }
       }
        //Log.d(TAG,"out13channel");
        //Log.d(TAG,out13chanel.dump());

        List<Mat> listout = new ArrayList();
        listout.add(out1chanel);
        listout.add(out12chanel);
        listout.add(out13chanel);
        Mat out = new Mat();
        Core.merge(listout,out);

        Mat out2 = new Mat(in.rows(),in.cols(),in.type(),zero);
        //get max dels tres canals
        Core.max(listout.get(0),listout.get(1),out2);
        Core.max(out2,listout.get(2),out2);

        out1chanel.setTo(zero);
        out13chanel.setTo(zero);

        //Log.d(TAG,"ou2");
        //Log.d(TAG,out2.dump());

        //eliminar primer fila, ultima fila igual que ultima
        for(int i = 0;i<ll;i++){
            if(i == ll-1){
                out2.col(i).copyTo(out1chanel.col(i));
            }else{
                out2.col(i+1).copyTo(out1chanel.col(i));
            }

        }
        //Log.d(TAG,"out1channel");
        //Log.d(TAG,out1chanel.dump());

        //eliminar ultima fila, primera fila igual primera

        for(int i = 0;i<ll;i++){
            if(i == 0){
                out2.col(i).copyTo(out13chanel.col(i));
            }else{
                out2.col(i-1).copyTo(out13chanel.col(i));
            }
        }
        //Log.d(TAG,"out13channel");
        //Log.d(TAG,out13chanel.dump());

        listout.clear();
        listout.add(out1chanel);
        listout.add(out2);
        listout.add(out13chanel);

        Mat out3channels = new Mat();
        Core.merge(listout,out3channels);

        Mat outDilation = new Mat(in.rows(),in.cols(),in.type());
        Core.max(listout.get(0),listout.get(1),outDilation);
        Core.max(outDilation,listout.get(2),outDilation);
        //Log.d(TAG,"out Dilation");
        //Log.d(TAG,outDilation.dump());
        //Log.d(TAG,"end dilation33");

        return outDilation;
    }

    private Mat set_border(Mat in, double width){
        //Log.d(TAG,"start set_border");
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
        //Log.d(TAG,"end set_border");

        return out;

    }

    private double PoolingHistMax(Mat in,Mat mask,double cutoof){
        //Log.d(TAG,"start PoolingHistMax");
        mask.convertTo(mask,CvType.CV_8U);
        int numNonzero = Core.countNonZero(in);
        Log.d(TAG,"nonzero: " + String.valueOf(numNonzero));
        Log.d(TAG,in.dump());
        Mat t = new Mat();
        in.copyTo(t,mask);
        Log.d(TAG,"t");
        Log.d(TAG,t.dump());
        double[] h = new double[numNonzero];
        double[] pixels;
        int contador = 0;

        for(int i = 0; i<in.rows();i++){
            for(int j = 0;j<in.cols();j++){
                pixels = in.get(i,j);
                if(pixels[0] != 0){
                    h[contador] = pixels[0];
                    contador++;
                }
            }
        }
        Arrays.sort(h);

        Mat temp = new Mat(numNonzero,1,in.type());
        for (int i = 0;i<numNonzero;i++){
            temp.put(i,1,h[i]);
        }





        //Log.d(TAG,"h");
        //Log.d(TAG,Arrays.toString(h));
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(in);
        double maxValor = minMaxLocResult.maxVal;
        double minValor = minMaxLocResult.minVal;
        double lowerMaxPixels = cutoof * numNonzero;
        double upperMaxPixels = lowerMaxPixels * 1.5;
        //Log.d(TAG,"maxValor: "+String.valueOf(maxValor) + " minValor: " + String.valueOf(minValor));
        //Log.d(TAG,"lower: " + String.valueOf(lowerMaxPixels) + " upper: "+String.valueOf(upperMaxPixels));
        int nbins = 0;
        if(maxValor < 256){
            nbins = 256;
        }else if(maxValor < 65536){
            nbins = 65536;
        }
        //Log.d(TAG,"nbins: " + String.valueOf(nbins));

        Mat histogram = new Mat();

        MatOfFloat range = new MatOfFloat(0,256);
        List<Mat> arrayHist = new ArrayList<>();
        MatOfInt channels = new MatOfInt(0);



        MatOfInt hintsize = new MatOfInt(nbins);


        arrayHist.add(in);
        //Log.d(TAG,in.dump());
        Imgproc.calcHist(arrayHist,channels,mask,histogram,hintsize,range,false);

        double[] values = linspace(h[0],maxValor,nbins);
        //Log.d(TAG,"values");
        //Log.d(TAG,Arrays.toString(values));
        //Log.d(TAG,String.valueOf(values[255]));
        //Log.d(TAG,"histogram");
        //Log.d(TAG,histogram.dump());


        double jpixels = 0;
        double[] hisValor;
        int j = nbins-2;

        boolean finish = false;

        while (j >-1 && !finish){
            //Log.d(TAG,"j: " + String.valueOf(j));
            hisValor = histogram.get(j,0);
            //Log.d(TAG,"hisvalor: " + String.valueOf(hisValor[0]));
            jpixels = hisValor[0] + jpixels;
            //Log.d(TAG,"jpixel: " +String.valueOf(jpixels));
            if(jpixels > lowerMaxPixels){
                if (jpixels > upperMaxPixels){
                    maxValor = values[j+1];
                    //maxValor = MeanBiggerPixels(values,j+1);
                }else{
                    maxValor = values[j];
                    //maxValor = MeanBiggerPixels(values,j);
                }

                finish = true;
            }
            j--;
        }


        //Log.d(TAG,"end PoolingHistMAx");
        //Log.d(TAG,"maxValor");
        //Log.d(TAG,String.valueOf(maxValor));
        return maxValor;

    }
    private double MeanBiggerPixels(double[] pixels,int cut){
        int size = pixels.length;
        int rows = size-cut;
        Mat m = new Mat(rows,1,CvType.CV_64F);
        for(int i = 0;i<rows;i++){
            m.put(i,0,pixels[cut + i]);

        }
        Scalar mean = Core.mean(m);
        return mean.val[0];
    }

    private Mat GaussianFilter2(double sigmax, double sigmay, double meanx,double meany){
        //Log.d(TAG,"start GaussianFilter2");
        double sizex = CalculateGaussianWidth(sigmax);
        double sizey = CalculateGaussianWidth(sigmay);

        double centrex = (sizex +1)/2;
        double centrey = (sizey + 1)/2;

        centrex = centrex + (meanx*centrex);
        centrey = centrey + (meany*centrey);

        double[] xs = linspace(1,sizex,sizex);
        int rows = xs.length;
        Mat xsMat = new Mat(rows,1,CvType.CV_64F);

        for(int i = 0;i<rows;i++){
            xsMat.put(i,0,xs[i]);
        }

        double[] ys = linspace(1,sizey,sizey);
        int rowys = ys.length;
        Mat ysMat = new Mat(1,rowys,CvType.CV_64F);
        for(int i = 0;i<rowys;i++){
            ysMat.put(0,i,ys[i]);
        }


        Mat onesxs = new Mat(1,rowys,CvType.CV_64F,new Scalar(1.0));
        Mat onesys = new Mat(rows,1,CvType.CV_64F,new Scalar(1.0));

        Mat resultxs = new Mat(rows,rowys,CvType.CV_64F);
        Mat resultys = new Mat(rows,rowys,CvType.CV_64F);

        Core.gemm(xsMat,onesxs,1,new Mat(),0,resultxs);
        Core.gemm(onesys,ysMat,1,new Mat(),0,resultys);

        Core.subtract(resultxs,new Scalar(centrex),resultxs);
        Core.subtract(resultys,new Scalar(centrey),resultys);

        Core.divide(resultxs,new Scalar(sigmax),resultxs);
        Core.divide(resultys,new Scalar(sigmay),resultys);

        Core.pow(resultxs,2,resultxs);
        Core.pow(resultys,2,resultys);


        Mat h = new Mat();

        Core.add(resultxs,resultys,h);

        Core.multiply(h,new Scalar(-0.5),h);

        Core.exp(h,h);

        double aux = 2 * 3.1416 * sigmax *  sigmay;
        Core.divide(h,new Scalar(aux),h);


        Scalar sum = Core.sumElems(h);


        Core.divide(h,sum,h);
        //Log.d(TAG,"end gaussianFilter2");

        return h;
    }

    private double CalculateGaussianWidth(double sigma){
        //Log.d(TAG,"start calculate Gauusian Width");
        double filterWidth;
        if(sigma == 1.5){
            filterWidth = 13;
        }else if(sigma == 2.0){
            filterWidth = 17;
        }else if (sigma == 2.5){
            filterWidth = 21;
        }else if(sigma == 3.0){
            filterWidth = 25;
        }else if(sigma == 7.5){
            filterWidth = 65;
        }
        else{
            filterWidth = 0;
        }


        return filterWidth;
    }
}
