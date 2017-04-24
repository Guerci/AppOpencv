package tfg.uab.jga.appopencv;

import android.content.SharedPreferences;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
    static final int ADD_LUM = 10;
    static final int EDIT_LUM = 15;
    Luminance lum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_luminance);

        blue = (EditText) findViewById(R.id.blueColor);
        green = (EditText) findViewById(R.id.greenColor);
        red = (EditText) findViewById(R.id.redColor);
        name = (EditText) findViewById(R.id.nameLuminance);
        alpha = (EditText) findViewById(R.id.alphaDepth);
        sp = new SharedPref();

        code = getIntent().getExtras().getInt("code");
        if(code == EDIT_LUM){
            lum = (Luminance) getIntent().getExtras().getSerializable("luminance");
            editLuminance();
        }

    }

    //save luminance

    public void onSaveLum(View v){
        Random random = new Random();
        int redNumber = Integer.valueOf(red.getText().toString()).intValue();
        int blueNumber = Integer.valueOf(blue.getText().toString()).intValue();
        int greenNumber = Integer.valueOf(green.getText().toString()).intValue();
        int alphaDepth = Integer.valueOf(alpha.getText().toString()).intValue();
        String newName = name.getText().toString();

        if(code == EDIT_LUM){
            sp.editLuminance(this,lum,redNumber,greenNumber,blueNumber,newName,alphaDepth);
            Toast.makeText(this, "Luminance edited", Toast.LENGTH_SHORT).show();

        }else{
            luminance = new Luminance(random.nextInt(),newName,redNumber,blueNumber,greenNumber,alphaDepth);
            sp.addLuminance(this,luminance);
            Toast.makeText(this, "Luminance added", Toast.LENGTH_SHORT).show();
        }


    }
    public void editLuminance(){

        blue.setText(Integer.toString(lum.getBlue()));
        red.setText(Integer.toString(lum.getRed()));
        green.setText(Integer.toString(lum.getGreen()));
        alpha.setText(Integer.toString(lum.getAlpha()));
        name.setText(lum.getName());


    }

    public void onRemoveLum(View v){
        sp.removeLuminance(this,lum);
        Toast.makeText(this,"Lumincace removed",Toast.LENGTH_LONG).show();
    }
}
