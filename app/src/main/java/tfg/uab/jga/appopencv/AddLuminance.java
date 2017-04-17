package tfg.uab.jga.appopencv;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Random;


public class AddLuminance extends AppCompatActivity {
    EditText blue,green,red,name;
    SharedPref sp;
    Luminance luminance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_luminance);

        blue = (EditText) findViewById(R.id.blueColor);
        green = (EditText) findViewById(R.id.greenColor);
        red = (EditText) findViewById(R.id.redColor);
        name = (EditText) findViewById(R.id.nameLuminance);
        sp = new SharedPref();


    }

    //save luminance

    public void onSaveLum(View v){
        Random random = new Random();
        int redNumber = Integer.valueOf(red.getText().toString()).intValue();;
        int blueNumber = Integer.valueOf(blue.getText().toString()).intValue();;
        int greenNumber = Integer.valueOf(green.getText().toString()).intValue();
        luminance = new Luminance(random.nextInt(),name.getText().toString(),redNumber,blueNumber,greenNumber);
        sp.addLuminance(this,luminance);

        Toast.makeText(this, "Luminance added", Toast.LENGTH_SHORT).show();



    }
}
