package tfg.uab.jga.appopencv;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by JordiPC on 15/04/2017.
 */

public class ListLuminanceContainer extends FragmentActivity {
    private Fragment contentFragment;
    ListLuminance listLuminance;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_luminance);

        FragmentManager fragmentManager = getSupportFragmentManager();

        listLuminance = new ListLuminance();
        //setFragmentTitle()
        setTitle("llista ilu");
//        getActionBar().setTitle("llista ilu");

    }

    protected void setFragmentTitle(int resourceId){
        setTitle(resourceId);
        //getActionBar().setTitle(resourceId);
    }

    @Override
    public void onBackPressed(){
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0){
            super.onBackPressed();
        }else if (contentFragment instanceof ListLuminance || fm.getBackStackEntryCount() == 0){
            finish();
        }
    }
}
