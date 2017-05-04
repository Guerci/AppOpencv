package tfg.uab.jga.appopencv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
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
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class DetailPicture extends AppCompatActivity {
    ImageView imageView;
    Button btnProcess, btnAddEff, btnSelectEff;
    SharedPref sp;
    Bitmap bmpInput, bmpOutput;
    Mat matInput, matOutput;
    Mat matProcess;
    Luminance effect;
    static final int SELECT_EFFECT = 100;
    static final int USE_EFFECT = 35;
    static{System.loadLibrary("opencv_java3"); }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_picture);

        imageView = (ImageView) findViewById(R.id.imageView);
        btnProcess = (Button) findViewById(R.id.btnProcess);
        btnAddEff = (Button) findViewById(R.id.add_effect);
        btnSelectEff = (Button) findViewById(R.id.select_effect);
        btnAddEff.setVisibility(View.INVISIBLE);
        //put button in invisibe for testing
        btnAddEff.setVisibility(View.INVISIBLE);
        btnProcess.setVisibility(View.INVISIBLE);
        btnSelectEff.setVisibility(View.INVISIBLE);

        sp = new SharedPref();

        //Bundle bd = getIntent().getExtras();
        int code = getIntent().getExtras().getInt("code");
        if(code == 1){
            String filename = getIntent().getExtras().getString("filename");
            getImageFromCamera(filename);
        }else{
            Bundle bd = getIntent().getExtras();
            Uri uri = bd.getParcelable("uri");
            getContentResolver().notifyChange(uri, null);
            getImageFromGallery(uri);
        }




    }


    public void onClickProcess(View v){

        Toast.makeText(DetailPicture.this,"Yey",Toast.LENGTH_LONG);
        matProcess  = convertBitmap2Mat(bmpInput);
        matProcess = getProcess(matProcess);
        bmpInput = convertMat2Bitmap(matProcess);
        imageView.setImageBitmap(bmpInput);

    }
    public void onClickProcess(){

        Toast.makeText(DetailPicture.this,"Yey",Toast.LENGTH_LONG);
        matProcess  = convertBitmap2Mat(bmpInput);
        matProcess = getProcess(matProcess);
        bmpInput = convertMat2Bitmap(matProcess);
        imageView.setImageBitmap(bmpInput);

    }

    public void onSelectEffect(View v){
        Intent getEffect = new Intent(this,ListLuminance.class);
        getEffect.putExtra("Code",USE_EFFECT);
        startActivityForResult(getEffect,SELECT_EFFECT);
    }
    public void onSelectEffect(){
        Intent getEffect = new Intent(this,ListLuminance.class);
        getEffect.putExtra("Code",USE_EFFECT);
        startActivityForResult(getEffect,SELECT_EFFECT);
    }
    public void onAddEffect(View v){
        int width = bmpInput.getWidth();
        int height = bmpInput.getHeight();

        Bitmap finalBitmap = Bitmap.createBitmap(width,height,bmpInput.getConfig());

        final double grayScale_Red = 0.3;
        final double grayScale_Green = 0.59;
        final double grayScale_Blue = 0.11;

        int red = effect.getRed();
        int green = effect.getGreen();
        int blue = effect.getBlue();
        int depth = effect.getAlpha();


        int channel_aplha, channel_red, channel_green, channel_blue;
        int pixel;

        for(int x = 0;x<width;x++){
            for(int y = 0;y<height;y++){
                pixel = bmpInput.getPixel(x,y);
                channel_aplha = Color.alpha(pixel);
                channel_red = Color.red(pixel);
                channel_blue = Color.blue(pixel);
                channel_green = Color.green(pixel);


                channel_blue = channel_green = channel_red = (int)(grayScale_Red * channel_red + grayScale_Green *
                channel_green + grayScale_Blue * channel_blue);

                channel_red += (depth * red);
                if(channel_red> 255){
                    channel_red = 255;
                }
                channel_blue += (depth * blue);
                if(channel_blue> 255){
                    channel_blue = 255;
                }
                channel_green += (depth * green);
                if(channel_green> 255){
                    channel_green = 255;
                }

                finalBitmap.setPixel(x,y,Color.argb(channel_aplha,channel_red,channel_green,channel_blue));

            }
        }


        imageView.setImageBitmap(finalBitmap);

    }

    public void onAddEffect(){
        int width = bmpInput.getWidth();
        int height = bmpInput.getHeight();

        Bitmap finalBitmap = Bitmap.createBitmap(width,height,bmpInput.getConfig());

        final double grayScale_Red = 0.3;
        final double grayScale_Green = 0.59;
        final double grayScale_Blue = 0.11;

        int red = effect.getRed();
        int green = effect.getGreen();
        int blue = effect.getBlue();
        int depth = effect.getAlpha();


        int channel_aplha, channel_red, channel_green, channel_blue;
        int pixel;

        for(int x = 0;x<width;x++){
            for(int y = 0;y<height;y++){
                pixel = bmpInput.getPixel(x,y);
                channel_aplha = Color.alpha(pixel);
                channel_red = Color.red(pixel);
                channel_blue = Color.blue(pixel);
                channel_green = Color.green(pixel);


                channel_blue = channel_green = channel_red = (int)(grayScale_Red * channel_red + grayScale_Green *
                        channel_green + grayScale_Blue * channel_blue);

                channel_red += (depth * red);
                if(channel_red> 255){
                    channel_red = 255;
                }
                channel_blue += (depth * blue);
                if(channel_blue> 255){
                    channel_blue = 255;
                }
                channel_green += (depth * green);
                if(channel_green> 255){
                    channel_green = 255;
                }

                finalBitmap.setPixel(x,y,Color.argb(channel_aplha,channel_red,channel_green,channel_blue));

            }
        }


        imageView.setImageBitmap(finalBitmap);

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

    private void getImageFromCamera(String filename){

        // Uri uri = bd.getParcelable("uri");
        String picturePath = Environment.getExternalStorageDirectory()+"/frames/" + filename; //arreglar amb un path absolut

        // bmpInput = getBitmap(uri);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmpInput = BitmapFactory.decodeFile(picturePath,opt);
        imageView.setImageBitmap(bmpInput);
        //matInput = convertBitmap2Mat(bmpInput);
        //matOutput = new Mat(matInput.rows(),matInput.cols(),CvType.CV_8UC3);
    }

    private void getImageFromGallery(Uri uri){


        try{

            bmpInput = getBitmap(uri);


            if(bmpInput != null){
                imageView.setImageBitmap(bmpInput);
            }else{
                Toast.makeText(this, "Error al capturar la imatge", Toast.LENGTH_LONG).show();
            }




        } catch (Exception e) {
            Toast.makeText(this, "No s'ha pogut mostrar la imatge", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public Bitmap getBitmap(Uri path){

        InputStream is = null;
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
            Log.d("Info scale", "scale = " + scale + ", orig-with; " + bmfOptions.outWidth + ", orig-height: "+ bmfOptions.outHeight);

            Bitmap b = null;
            is = getContentResolver().openInputStream(path);
            if(scale > 1){
                scale--;

                bmfOptions = new BitmapFactory.Options();
                bmfOptions.inSampleSize = scale;
                b = BitmapFactory.decodeStream(is, null, bmfOptions);

                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("sheet", "1th scale operation dimensions- width: " + width + ", height: " + height);
                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double)width)/height));
                double x = (y/height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,(int)x,(int)y, true);

                b.recycle();
                b = scaledBitmap;
                imageView.setRotation(90);


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

    private Mat getProcess(Mat image){
        int height = image.height();
        int width = image.width();

        Mat imgGrey = new Mat(height,width,CvType.CV_8UC1);
        Imgproc.cvtColor(image,imgGrey,Imgproc.COLOR_RGB2GRAY);
        return imgGrey;
    }



    public void onGetLumFromImage(View v){
        ArrayList<Integer> rgba = new ArrayList<>();
        Bitmap src = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        rgba = ProcessImage.getLumFromImage(src);
        Luminance lum = new Luminance(rgba);
        sp.addLuminance(this,lum);
        Toast.makeText(this,"lum added",Toast.LENGTH_LONG).show();
    }

    public void onGetLumFromImage(){
        ArrayList<Integer> rgba = new ArrayList<>();
        Bitmap src = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        rgba = ProcessImage.getLumFromImage(src);
        Luminance lum = new Luminance(rgba);
        sp.addLuminance(this,lum);
        Toast.makeText(this,"lum added",Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SELECT_EFFECT) {
            if(resultCode == Activity.RESULT_OK){
                effect= (Luminance) data.getSerializableExtra("result");
                btnAddEff.setVisibility(View.VISIBLE);

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,"no s'ha pogut seleccionar l'effecte", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_picture, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_proces_image:
                onClickProcess();
                return true;
            case R.id.action_add_luminance:
                onAddEffect();
                return true;
            case R.id.action_select_lum:
                onSelectEffect();
                return true;

            case R.id.action_save_image:
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
