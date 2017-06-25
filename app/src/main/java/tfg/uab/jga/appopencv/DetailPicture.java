package tfg.uab.jga.appopencv;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;



import static org.opencv.imgproc.Imgproc.cvtColor;

public class DetailPicture extends AppCompatActivity {
    ImageView imageView;

    SharedPref sp;
    Bitmap bmpInput;


    Luminance effect;
    String TAG = "DetailPicture";
    static final int SELECT_EFFECT = 100;
    static final int USE_EFFECT = 35;
    static final int ADD_LUM = 10;
    static final int CAM_REQUEST = 1;
    static{System.loadLibrary("opencv_java3"); }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_picture);

        imageView = (ImageView) findViewById(R.id.imageView);


        sp = new SharedPref();

        //Bundle bd = getIntent().getExtras();
        int code = getIntent().getExtras().getInt("code");
        if(code == CAM_REQUEST){
            //String filename = getIntent().getExtras().getString("filename");
            //getImageFromCamera(filename);
            Bundle bd = getIntent().getExtras();
            Uri uri = bd.getParcelable("uri");
            getContentResolver().notifyChange(uri, null);
            getImageFromCamera2(uri);

        }else{
            Bundle bd = getIntent().getExtras();
            Uri uri = bd.getParcelable("uri");
            getContentResolver().notifyChange(uri, null);
            getImageFromGallery(uri);
        }




    }

    public void onClickProcess(){
        final Bitmap out[] = new Bitmap[1];
        final ProgressDialog progressDialog = new ProgressDialog(DetailPicture.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle(getString(R.string.process_image_title));
        progressDialog.setMessage(getString(R.string.Process_image_body));
        progressDialog.setCancelable(false);
        Mat image;

        progressDialog.show();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ProcessImage pi = new ProcessImage();
                //Mat image = convertBitmap2Mat(bmpInput);
                Bitmap bmp = bmpInput;
                Mat image = convertBitmap2Mat(bmp);
                image = pi.surroundModulation(image);
                out[0] = convertMat2Bitmap(image);
                bmpInput = out[0];
                progressDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(out[0]);
                    }
                });

            }
        });



        t.start();






    }



    public void onSelectEffect(){
        Intent getEffect = new Intent(this,ListLuminance.class);
        getEffect.putExtra("Code",USE_EFFECT);
        startActivityForResult(getEffect,SELECT_EFFECT);
    }

    public void onAddEffectMat(){


        Bitmap imageBmp = bmpInput;
        Mat image = convertBitmap2Mat(imageBmp);

        if(image.type() != CvType.CV_64FC3){
            image.convertTo(image,CvType.CV_64FC3);
        }



        Scalar divide = new Scalar(255,255,255);
        Core.divide(image,divide,image);

        Scalar rgb = new Scalar(effect.getBlue()/255.0,effect.getGreen()/255.0,effect.getRed()/255.0);
        Core.multiply(image,rgb,image);




        ArrayList<Mat> listMat = new ArrayList<>();
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

        Log.d(TAG,String.valueOf(maxValor));
        Core.merge(listMat,image);
        Scalar normalize = new Scalar(255.0/maxValor,255.0/maxValor,255.0/maxValor);
        Log.d(TAG,"valor per normalitzar a 255: " + String.valueOf(255.0/maxValor));
        Core.multiply(image,normalize,image);
        image.convertTo(image,CvType.CV_8UC3);
        Bitmap out = convertMat2Bitmap(image);


        bmpInput = out;
        imageView.setImageBitmap(out);




    }


    Mat convertBitmap2Mat(Bitmap img){

        Mat rgbaMat = new Mat(img.getHeight(),img.getWidth(), CvType.CV_8UC4);
        Bitmap bmp = img.copy(Bitmap.Config.ARGB_8888,true);
        Utils.bitmapToMat(bmp, rgbaMat);

        Mat rgbMat = new Mat(img.getHeight(),img.getWidth(),CvType.CV_8SC3);
        cvtColor(rgbaMat,rgbMat, Imgproc.COLOR_RGBA2BGR,3);
        return rgbMat;

    }

    Bitmap convertMat2Bitmap(Mat img){
        int width = img.width();
        int height = img.height();

        Bitmap bmp = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);


        Mat tmp;
        tmp = img.channels() == 1 ? new Mat (width,height, CvType.CV_8UC1,new Scalar(1)) : new Mat(width,height,CvType.CV_8UC3,new Scalar(3));
        try{
            if(img.channels() == 3)
                cvtColor(img,tmp,Imgproc.COLOR_RGB2BGRA);
            else if (img.channels() == 1){
                cvtColor(img,tmp,Imgproc.COLOR_GRAY2RGBA);
            }
            Utils.matToBitmap(tmp,bmp);
        }catch(CvException e){
            Log.d("Exception",e.getMessage());
        }
        return bmp;

    }


    private void getImageFromCamera2(Uri uri){
        try {
            bmpInput = getBitmap(uri);
            String path = uri.getPath();
            rotateBitmap(path);
            if (bmpInput != null) {
                imageView.setImageBitmap(bmpInput);

            } else {
                Toast.makeText(this, this.getString(R.string.error_get_image), Toast.LENGTH_LONG).show();
            }

        }catch (Exception e) {
            Toast.makeText(this, this.getString(R.string.error_show_image), Toast.LENGTH_LONG).show();
            Log.d(TAG,e.toString());
        }

    }


    private void getImageFromGallery(Uri uri){
        try{
            Log.d(TAG,"uri: " + uri.toString());
            bmpInput = getBitmap(uri);
            String path = getRealPathFromURI(uri);
            rotateBitmap(path);
            if(bmpInput != null){
                imageView.setImageBitmap(bmpInput);

            }else{
                Toast.makeText(this, this.getString(R.string.error_get_image), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, this.getString(R.string.error_show_image), Toast.LENGTH_LONG).show();
            Log.d(TAG,e.toString());
        }
    }
    private String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);

        cursor.close();
        return result;
    }

    public Bitmap scaleBitmap(Bitmap bmp){
        Bitmap b = bmp;
        final int IMAGE_MAX_SIZE = 1200000;
        int height = b.getHeight();
        int width = b.getWidth();
        //Log.d(TAG, "1th scale operation dimensions- width: " + width + ", height: " + height);
        double y = Math.sqrt(IMAGE_MAX_SIZE / (((double)width)/height));
        double x = (y/height) * width;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,(int)x,(int)y, true);

        b.recycle();
        b = scaledBitmap;
        return b;

    }


    public Bitmap getBitmap(Uri path){

        InputStream is;
        try{
            final int IMAGE_MAX_SIZE = 1200000;
            is = getContentResolver().openInputStream(path);

            BitmapFactory.Options bmfOptions = new BitmapFactory.Options();
            bmfOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is,null, bmfOptions);
            is.close();

            int scale = 1;
            while((bmfOptions.outWidth * bmfOptions.outHeight)*(1/Math.pow(scale, 2)) > IMAGE_MAX_SIZE){
                scale ++;
            }
            //Log.d(TAG, "scale = " + scale + ", orig-with; " + bmfOptions.outWidth + ", orig-height: "+ bmfOptions.outHeight);

            Bitmap b;
            is = getContentResolver().openInputStream(path);
            if(scale > 1){
                scale--;

                bmfOptions = new BitmapFactory.Options();
                bmfOptions.inSampleSize = scale;
                b = BitmapFactory.decodeStream(is, null, bmfOptions);

                int height = b.getHeight();
                int width = b.getWidth();
                //Log.d(TAG, "1th scale operation dimensions- width: " + width + ", height: " + height);
                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double)width)/height));
                double x = (y/height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,(int)x,(int)y, true);

                b.recycle();
                b = scaledBitmap;



            }else{
                b = BitmapFactory.decodeStream(is);


            }
            is.close();
            return b;


        }catch(IOException e){
            Log.e("IOEXCEPTOPM", e.getMessage(), e);
            return null;
        }

    }



    public void rotateBitmap(String path){
        try{
            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            Log.d(TAG,String.valueOf(rotation));
            int rotationInDegrees = exifToDegrees(rotation);
            Log.d(TAG,String.valueOf(rotationInDegrees));
            Matrix m = new Matrix();
            if(rotation != 0f){
                m.preRotate(rotationInDegrees);
                bmpInput = Bitmap.createBitmap(bmpInput,0,0,bmpInput.getWidth(),bmpInput.getHeight(),m,true);
            }
        }catch(Exception e){
            Log.d(TAG,"Error en rotar el bitmap");
        }
    }
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }



    public void onGetLumFromImage(){
        ProcessImage processImage = new ProcessImage();
        ArrayList<Integer> rgba;
        //Bitmap src = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Bitmap src = bmpInput;
        Mat imageMat = convertBitmap2Mat(src);
        rgba = processImage.getLuminanceFromMat(imageMat);
        Luminance lum = new Luminance(rgba,this);
        Intent addLum = new Intent(this,AddLuminance.class);
        addLum.putExtra("code",20);
        addLum.putExtra("Lum",lum);
        startActivityForResult(addLum,ADD_LUM);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_EFFECT) {
            if(resultCode == Activity.RESULT_OK){
                effect= (Luminance) data.getSerializableExtra("result");
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,this.getString(R.string.error_selec_effect), Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == ADD_LUM){
            Luminance lum = (Luminance) data.getSerializableExtra("result");
            sp.addLuminance(this,lum);
            Toast.makeText(this,this.getString(R.string.lum_added),Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_picture, menu);
        return true;
    }

    private void onSaveImage(){

        FileOutputStream out = null;
        String filename = getFileName();
        Bitmap bmp = bmpInput;
        File sd = new File(Environment.getExternalStorageDirectory() + "/appASM");
        boolean succes = true;
        if(!sd.exists()){
            succes = sd.mkdir();

        }
        if(succes){


            File dest = new File(sd.getPath() + File.separator + filename);

            try{

                out = new FileOutputStream(dest);
                bmp.compress(Bitmap.CompressFormat.PNG,100,out);


            }catch(Exception e){
                e.printStackTrace();

            }finally {

                try{
                    if(out != null){
                        out.close();

                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(dest);
                        mediaScanIntent.setData(contentUri);
                        this.sendBroadcast(mediaScanIntent);

                        Toast.makeText(this,this.getString(R.string.save_image_detail_picture),Toast.LENGTH_LONG).show();

                    }
                } catch (IOException e){

                    e.printStackTrace();
                }
            }
        }else{
            Toast.makeText(this,this.getString(R.string.error_save_image),Toast.LENGTH_LONG).show();

        }

    }
    private String getFileName()
    {
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + ".png";


        return imageFileName;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_proces_image:
                onClickProcess();
                return true;
            case R.id.action_add_luminance:
                if(effect == null){
                    Toast.makeText(this,this.getString(R.string.select_lum_first),Toast.LENGTH_LONG).show();
                }else{
                    onAddEffectMat();
                }

                return true;
            case R.id.action_select_lum:
                onSelectEffect();
                return true;

            case R.id.action_save_image:
                onSaveImage();
                return true;
            case R.id.action_get_luminance:
                onGetLumFromImage();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }


    }
}
