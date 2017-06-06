package tfg.uab.jga.appopencv;

import android.app.Activity;
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
import java.util.List;

import static org.opencv.imgproc.Imgproc.cvtColor;

public class DetailPicture extends AppCompatActivity {
    ImageView imageView;

    SharedPref sp;
    Bitmap bmpInput;

    Mat matProcess;
    Luminance effect;
    String TAG = "DetailPicture";
    static final int SELECT_EFFECT = 100;
    static final int USE_EFFECT = 35;
    static final int ADD_LUM = 10;
    static{System.loadLibrary("opencv_java3"); }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_picture);

        imageView = (ImageView) findViewById(R.id.imageView);


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



    public void onClickProcess(){

        Toast.makeText(DetailPicture.this,"Yey",Toast.LENGTH_LONG);
        matProcess  = convertBitmap2Mat(bmpInput);
        matProcess = getProcess(matProcess);
        bmpInput = convertMat2Bitmap(matProcess);
        imageView.setImageBitmap(bmpInput);

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
        File f = new File(Environment.getExternalStorageDirectory()+"/frames/" + filename);
        String path = f.getAbsolutePath();
        // bmpInput = getBitmap(uri);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmpInput = BitmapFactory.decodeFile(picturePath,opt);
        rotateBitmap(path);
        imageView.setImageBitmap(bmpInput);
        //matInput = convertBitmap2Mat(bmpInput);
        //matOutput = new Mat(matInput.rows(),matInput.cols(),CvType.CV_8UC3);
    }

    private void getImageFromGallery(Uri uri){
        try{
            bmpInput = getBitmap(uri);

            String path = getRealPathFromURI(uri);
            rotateBitmap(path);
            if(bmpInput != null){
                imageView.setImageBitmap(bmpInput);
            }else{
                Toast.makeText(this, "Error al capturar la imatge", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, this.getString(R.string.error_show_image), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        Log.d(TAG,result);
        cursor.close();
        return result;
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
            Log.d("InfoScale", "scale = " + scale + ", orig-with; " + bmfOptions.outWidth + ", orig-height: "+ bmfOptions.outHeight);

            Bitmap b;
            is = getContentResolver().openInputStream(path);
            if(scale > 1){
                scale--;

                bmfOptions = new BitmapFactory.Options();
                bmfOptions.inSampleSize = scale;
                b = BitmapFactory.decodeStream(is, null, bmfOptions);

                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("InfoScale", "1th scale operation dimensions- width: " + width + ", height: " + height);
                double y = Math.sqrt(IMAGE_MAX_SIZE / (((double)width)/height));
                double x = (y/height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b,(int)x,(int)y, true);

                b.recycle();
                b = scaledBitmap;
                //imageView.setRotation(90);


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
        /*
        Mat imgGrey = new Mat(height,width,CvType.CV_8UC1);
        Imgproc.cvtColor(image,imgGrey,Imgproc.COLOR_RGB2GRAY);*/

        ProcessImage pi = new ProcessImage();
        pi.surroundModulation(image);


        return image;
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
        Bitmap src = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Mat imageMat = convertBitmap2Mat(src);
        rgba = processImage.getLuminanceFromMat(imageMat);
        Luminance lum = new Luminance(rgba,this);
        Intent addLum = new Intent(this,AddLuminance.class);
        addLum.putExtra("code",20);
        addLum.putExtra("Lum",lum);
        startActivityForResult(addLum,ADD_LUM);
       /* sp.addLuminance(this,lum);
        Toast.makeText(this,"lum added",Toast.LENGTH_LONG).show();*/
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
        File sd = new File(Environment.getExternalStorageDirectory() + "/SMApp");
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
                        Toast.makeText(this,this.getString(R.string.save_image_detail_picture),Toast.LENGTH_LONG).show();

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
