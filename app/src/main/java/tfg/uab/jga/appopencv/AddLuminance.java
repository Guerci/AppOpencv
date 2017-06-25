package tfg.uab.jga.appopencv;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Random;


public class AddLuminance extends AppCompatActivity {
    EditText blue,green,red,name, alpha;
    SharedPref sp;
    Luminance luminance;
    int code;
    static final int ADD_LUM_AUTO = 20;
    static final int ADD_LUM = 10;
    static final int EDIT_LUM = 15;
    Luminance lum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_luminance);

        Button del = (Button) findViewById(R.id.remove_luminance);


        blue = (EditText) findViewById(R.id.blueColor);
        green = (EditText) findViewById(R.id.greenColor);
        red = (EditText) findViewById(R.id.redColor);
        name = (EditText) findViewById(R.id.nameLuminance);

        sp = new SharedPref();

        code = getIntent().getExtras().getInt("code");
        if(code == EDIT_LUM){
            lum = (Luminance) getIntent().getExtras().getSerializable("luminance");
            editLuminance();
        }else if(code == ADD_LUM_AUTO){
            lum = (Luminance) getIntent().getExtras().getSerializable("Lum");
            editLuminance();

        }else if (code == ADD_LUM){
            del.setVisibility(View.INVISIBLE);
        }

    }

    //save luminance

    public void onSaveLum(View v){
        Random random = new Random();
        int redNumber = Integer.valueOf(red.getText().toString()).intValue();
        int blueNumber = Integer.valueOf(blue.getText().toString()).intValue();
        int greenNumber = Integer.valueOf(green.getText().toString()).intValue();

        String newName = name.getText().toString();
        if(redNumber <=255 && blueNumber <=255 && greenNumber <= 255){
            if(code == EDIT_LUM) {
                sp.editLuminance(this, lum, redNumber, greenNumber, blueNumber, newName);
                Toast.makeText(this, this.getString(R.string.lum_edited), Toast.LENGTH_SHORT).show();
            }else if(code == ADD_LUM_AUTO){
                luminance = new Luminance(random.nextInt(),newName,redNumber,blueNumber,greenNumber);
                Intent backToDetail = new Intent();
                backToDetail.putExtra("result",luminance);
                setResult(Activity.RESULT_OK,backToDetail);

            }else{
                luminance = new Luminance(random.nextInt(),newName,redNumber,blueNumber,greenNumber);
                sp.addLuminance(this,luminance);
                Toast.makeText(this, this.getString(R.string.lum_added), Toast.LENGTH_SHORT).show();
            }
            finish();
        }else{
            Toast.makeText(this,this.getString(R.string.valors_RGB),Toast.LENGTH_LONG).show();
        }




    }
    public void editLuminance(){

        blue.setText(Integer.toString(lum.getBlue()));
        red.setText(Integer.toString(lum.getRed()));
        green.setText(Integer.toString(lum.getGreen()));

        name.setText(lum.getName());


    }

    public void onRemoveLum(View v){
        sp.removeLuminance(this,lum);
        Toast.makeText(this,this.getString(R.string.lum_removed),Toast.LENGTH_LONG).show();
        finish();
    }
}
