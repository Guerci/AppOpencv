package tfg.uab.jga.appopencv;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;


import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;


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
import java.util.Arrays;
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
    static final int STORAGE_PERMISSION_REQUEST = 5;
    static final int CAMERA_PERMISSION_REQUEST = 7;
    static final int BOTH_PERMISSIONS_REQUEST = 9;
    static Uri imageUri;

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
        Button i =(Button) findViewById(R.id.btn_lenguage);
        i.setVisibility(View.INVISIBLE);

        if(!isPermissionAllowed(STORAGE_PERMISSION_REQUEST )&& !isPermissionAllowed(CAMERA_PERMISSION_REQUEST)){
            requestPermission(BOTH_PERMISSIONS_REQUEST);
        }
        else if(!isPermissionAllowed(STORAGE_PERMISSION_REQUEST)){
            requestPermission(STORAGE_PERMISSION_REQUEST);
        }else if(!isPermissionAllowed(CAMERA_PERMISSION_REQUEST)){
            requestPermission(CAMERA_PERMISSION_REQUEST);
        }


    }

    private void requestPermission(int code){
        if(code == BOTH_PERMISSIONS_REQUEST){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this,"you need these permissions to use the app!",Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},BOTH_PERMISSIONS_REQUEST);
        }
        if(code == STORAGE_PERMISSION_REQUEST){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                Toast.makeText(this,"you need this permission to save your image!",Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_REQUEST);
        }

        if(code == CAMERA_PERMISSION_REQUEST){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){
                Toast.makeText(this,"you need this permission to use the camera!",Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case STORAGE_PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"permission granted!",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,"Oops you denied the permission",Toast.LENGTH_LONG).show();
                }
                break;
            case CAMERA_PERMISSION_REQUEST:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"permission granted!",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,"Oops you denied the permission",Toast.LENGTH_LONG).show();
                }
                break;
            case BOTH_PERMISSIONS_REQUEST:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"permissions granted",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this,"Oops you denied the permissions",Toast.LENGTH_LONG).show();
                }
                break;

        }

    }



    private boolean isPermissionAllowed(int code){
        int result = 0;
        //check permission
        if(code == STORAGE_PERMISSION_REQUEST){
            result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else if(code == CAMERA_PERMISSION_REQUEST){
            result =  ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        }

        //check if its granted or denied
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }




    public void onImageCamera(View view) {
        /*
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);*/

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"fname_"+
                                                String.valueOf(SystemClock.currentThreadTimeMillis())+".jpg"));
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,CAM_REQUEST);

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
                    Uri imageUriG = data.getData(); //adress of the image
                    //declare a stream to read the image data

                    Intent loadDisplayActivity = new Intent(this, DetailPicture.class);
                    loadDisplayActivity.putExtra("uri", imageUriG);
                    loadDisplayActivity.putExtra("code", GALLERY_REQUEST);
                    startActivity(loadDisplayActivity);


                }
                break;
            case CAM_REQUEST:
                if(resultCode == RESULT_OK){
                    //Bundle extras = data.getExtras();
                    Intent loadDisplayActivity = new Intent(this, DetailPicture.class);
                    loadDisplayActivity.putExtra("uri",imageUri);
                    loadDisplayActivity.putExtra("code",CAM_REQUEST);
                    startActivity(loadDisplayActivity);

                }

        }
    }


    public void testMat(){
        /*
        ProcessImage pi = new ProcessImage();
        Long time = SystemClock.currentThreadTimeMillis();
        double[] test = pi.linspace2(0.234,0.820,1000000);
        Long time2 = SystemClock.currentThreadTimeMillis();
        //Log.d(TAG, Arrays.toString(test));
        Log.d(TAG,"Time: " + String.valueOf(time2-time));

        time = SystemClock.currentThreadTimeMillis();
        Mat t = pi.linspace(0.234,500,1000000);
        time2 = SystemClock.currentThreadTimeMillis();
        //Log.d(TAG,t.dump());
        Log.d(TAG,"Time: " + String.valueOf(time2-time));
        */


    }
}
