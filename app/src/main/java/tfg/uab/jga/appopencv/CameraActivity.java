package tfg.uab.jga.appopencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.SurfaceView;
import android.view.View;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static String TAG = "CameraActivity";
    JavaCameraView javaCameraView;
    Mat mRgba;
    Mat mRgbaT;
    String imagePath;
    String filename;
    private int CAM = 1;
    BaseLoaderCallback mLoaderCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        javaCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        javaCameraView.setVisibility(SurfaceView.VISIBLE);

        javaCameraView.setCvCameraViewListener(this);

    }
    @Override
    protected void onPause(){
        super.onPause();
        if(javaCameraView != null){
            javaCameraView.disableView();
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(javaCameraView != null){
            javaCameraView.disableView();
        }

    }

    @Override
    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"opencv loaded");
            mLoaderCallBack.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }else{
            Log.d(TAG,"Opencv failed to load");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallBack);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8SC4);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mRgbaT = mRgba.t();
        Core.flip(mRgba.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
        return mRgbaT;
    }

    public void onClickSave(View view){
        takePicture(mRgbaT);
        Intent i = new Intent(this,DetailPicture.class);
        //Uri imgUri = Uri.parse(imagePath);
        i.putExtra("filename",filename);
        i.putExtra("code",CAM);
        startActivity(i);
    }

    private void takePicture(Mat image){
        Bitmap bmp = null;
        try{
            bmp =   Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(image,bmp);

        }catch (CvException e){
            Log.d(TAG,e.getMessage());
        }
        image.release();
        FileOutputStream out = null;
        filename = getFileName();

        File sd = new File(Environment.getExternalStorageDirectory() + "/frames");
        boolean succes = true;
        if(!sd.exists()){
            succes = sd.mkdir();
            Log.d(TAG,"crear directori " + succes);
        }
        if(succes){

            File dest = new File(sd,filename);

            try{
                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG,100,out);

            }catch(Exception e){
                e.printStackTrace();
                Log.d(TAG,e.getMessage());
            }finally {
                try{
                    if(out != null){
                        out.close();
                        Log.d(TAG,"Succes in saving the image");

                    }
                } catch (IOException e){
                    Log.d(TAG,e.getMessage() + "Error");
                    e.printStackTrace();
                }
            }
        }else{
            Log.d(TAG,"error al guardar la imatge");
        }



    }

    private String getFileName()
    {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + ".png";


        return imageFileName;
    }

}
