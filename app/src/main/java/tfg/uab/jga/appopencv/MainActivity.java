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
import java.util.Vector;

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
        Mat t = new Mat(10,10,CvType.CV_64F);
        for(int i = 0;i<10;i++){
            for(int j= 0;j<10;j++){
                t.put(i,j,0.1);
            }
        }
        ProcessImage pi = new ProcessImage();
        int[] a = new int[2];
        a[0] = 0;
        a[1] = 4;
       // pi.getContrastImage(t,a);

    }
}
