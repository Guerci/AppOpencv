package tfg.uab.jga.appopencv;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.loadLibrary;

public class MainActivity extends AppCompatActivity {
    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;
    static final int GALLERY_REQUEST = 20;
    static final int CAM_REQUEST = 1;
    static final int ADD_LUM = 10;
    static final int LIST_LUM = 2;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    private static String TAG = "MainActivity";
    static {
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV initialize success");
        } else {
            Log.i(TAG, "OpenCV initialize failed");
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "main activity layout");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onImageCamera(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void onImageGallery(View v) {
        //invoke the image gallery with an implicit intent
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        //directori de la galeria
        File picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String picturesDirPath = picturesDir.getPath();
        Uri data = Uri.parse(picturesDirPath);
        //set data type
        photoPickerIntent.setDataAndType(data, "image/*");

        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);


    }


    public void onListLuminance(View v){
        Intent loadListLuminance = new Intent(this,ListLuminance.class);
        loadListLuminance.putExtra("Code",LIST_LUM);
        startActivity(loadListLuminance);
    }

    public void onAddLuminance(View v){
        /*
      //  Intent loadAddLuminance = new Intent(this,AddLuminance.class);
        Intent loadAddLum = new Intent(this,AddLuminance.class);
        loadAddLum.putExtra("code",ADD_LUM);
        startActivity(loadAddLum);
       // startActivityForResult(loadAddLuminance,ADD_LUM);
        //startActivity(loadAddLuminance);*/

        testMat();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case GALLERY_REQUEST:
                if (resultCode == RESULT_OK) {
                    Uri imageUri = data.getData(); //adress of the image
                    //declare a stream to read the image data

                    Intent loadDisplayActivity = new Intent(this, DetailPicture.class);
                    loadDisplayActivity.putExtra("uri", imageUri);
                    loadDisplayActivity.putExtra("code", GALLERY_REQUEST);
                    startActivity(loadDisplayActivity);


                }

        }
    }


    public void testMat(){

        Mat image = new Mat(5,5,CvType.CV_64FC3,new Scalar(5));
        List<Mat> list = new ArrayList<>();
        Core.split(image,list);
        Core.add(list.get(1),new Scalar(2),list.get(1));
        Core.add(list.get(2),new Scalar(1.5),list.get(2));
        Core.merge(list,image);
        Log.d(TAG,"Imatge inicial");
        Log.d(TAG,image.dump());
        Scalar divide = new Scalar(255,255,255);
        Core.divide(image,divide,image);
        //Core.normalize(image,image,0,1,Core.NORM_MINMAX);

        Log.d(TAG,"Imatge normalitzada");
        Log.d(TAG,image.dump());
        Scalar rgb = new Scalar(150/255.0,90/255.0,95/255.0);
        Log.d(TAG,"Scalar rgb");
        Log.d(TAG,String.valueOf(rgb));
        Core.multiply(image,rgb,image);
        Log.d(TAG,"Imatge multiplicada per rgb");
        Log.d(TAG,image.dump());

        //Core.normalize(image,image,0,255,Core.NORM_MINMAX);


        List<Mat> listMat = new ArrayList<>();
        Core.split(image,listMat);
        Core.MinMaxLocResult maxBlue = Core.minMaxLoc(listMat.get(0));
        Core.MinMaxLocResult maxGreen = Core.minMaxLoc(listMat.get(1));
        Core.MinMaxLocResult maxRed = Core.minMaxLoc(listMat.get(2));

        double maxValor;
        if(maxBlue.maxVal >= maxGreen.maxVal && maxBlue.maxVal >= maxRed.maxVal){
            maxValor = maxBlue.maxVal;
        }else if(maxGreen.maxVal >= maxRed.maxVal){
            maxValor = maxGreen.maxVal;
        }else{
            maxValor = maxRed.maxVal;
        }
        Log.d(TAG,"Valor max normalitzat");
        Log.d(TAG,String.valueOf(maxValor));


        Core.merge(listMat,image);

        Scalar normalize = new Scalar(255/maxValor,255/maxValor,255/maxValor);
        Log.d(TAG,"scalar normalize");
        Log.d(TAG,String.valueOf(normalize));
        Core.multiply(image,normalize,image);
        Log.d(TAG,"Imatge aa punt per pasar a bitmap");
        Log.d(TAG,image.dump());

    }
}
