package tfg.uab.jga.appopencv;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by JordiPC on 13/04/2017.
 */

public class SharedPref {

    public static final String LUM_NAME = "LUMINANCE";

    public SharedPref(){
        super();

    }

    public void saveLuminance(Context context, List<Luminance> listLumi){
        SharedPreferences sharedPref = context.getSharedPreferences(LUM_NAME,context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonLumi = gson.toJson(listLumi);
        editor.putString(LUM_NAME,jsonLumi);
        editor.commit();
    }

    public void addLuminance(Context context,Luminance luminance){
        List<Luminance> listLumi = getLuminance(context);
        if(listLumi == null){
            listLumi = new ArrayList<Luminance>();
        }
        listLumi.add(luminance);
        saveLuminance(context,listLumi);
    }

    public void removeLuminance(Context context, Luminance luminance){
        ArrayList<Luminance> listLumi = getLuminance(context);
        if(listLumi != null){
            listLumi.remove(luminance);
            saveLuminance(context,listLumi);
        }
    }

    public ArrayList<Luminance> getLuminance(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(LUM_NAME,context.MODE_PRIVATE);
        List<Luminance> listLumi;

        if(sharedPref.contains(LUM_NAME)){
            String jsonLumi = sharedPref.getString(LUM_NAME,null);
            Gson gson = new Gson();
            Luminance[] luminaceItems = gson.fromJson(jsonLumi,Luminance[].class);

            listLumi = Arrays.asList(luminaceItems);
            listLumi = new ArrayList<Luminance>(listLumi);
        }else{
            return null;
        }
        return (ArrayList<Luminance>) listLumi;
    }




}
