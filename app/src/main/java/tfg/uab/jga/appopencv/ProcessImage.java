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

        int maxVal = 255;

        src.convertTo(src, CvType.CV_64FC3); //convert MAT to double precision with 3 channels

        Scalar scalar = new Scalar(maxVal);
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

        Mat ss = linspace(s1,s4,nk);
        Mat cs = linspace(c1,c4,nk);

        Mat dorg = applyNeighbourInpact(isignal,ab,ba,ss,cs);
        return dorg;

    }

    private SigmaTemplate relativePixelContrast(Mat inputImage, double centreSize, double surroundSize){


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

        Imgproc.filter2D(image,out,-1,h,anchor,delta,Core.BORDER_DEFAULT);
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
        for (double i: nContrastLevelsX) {
            double[] lambdaxiA = sigmas.get((int)i,1);
            double lambdaxi = lambdaxiA[0];
            for (double j:nContrastLevelY) {
                double[] lambdayA = sigmas.get((int)j,1);
                double lambdayi = lambdayA[0];
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

    private Mat matChansMulK(Mat inputImage, double[] k){


        List<Mat> listMat = null;
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


        Mat rf = new Mat();

        rf = Imgproc.getGaussianKernel(1,startingSigma);
        Imgproc.filter2D(isignal,isignal,0,rf);

        return isignal;
    }

    private Mat applyNeighbourInpact(Mat isignal, Mat ab, Mat ba, Mat surroundImpacts, Mat centreImpacts){
        int nContrastLevels = surroundImpacts.cols();
        double[] surroundSize = {17,17};
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
        Imgproc.filter2D(inputImage,dest,-1,kernel,anchor,delta,Core.BORDER_DEFAULT);
        Mat stdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Core.subtract(inputImage,dest,stdv);
        Core.pow(stdv,2,stdv);
        Mat meanstdv = new Mat(inputImage.rows(),inputImage.cols(),inputImage.type());
        Imgproc.filter2D(stdv,meanstdv,-1,kernel,anchor,delta,Core.BORDER_DEFAULT);
        Core.sqrt(meanstdv,meanstdv);
        return meanstdv;
    }

    private Scalar calculateLuminanceSingle(Mat modelResponse, Mat image){
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

        Mat saturatedPixels = dilation33(maxImage);
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
        double[] ws = {5.0,5.0};
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
        double hh = in.rows();
        double ll = in.cols();
        Mat t = new Mat();
        return t;
    }

    private Mat set_border(Mat in, double width){

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
