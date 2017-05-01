package tfg.uab.jga.appopencv;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;

public class ListLuminance extends AppCompatActivity {

    public static final String ARG_LUM_ID = "luminance_list";


    ListView luminanceListView;
    List<Luminance> luminance;
    ListLuminanceAdapter listLuminanceAdapter;
    static final int ADD_LUM = 10;
    static final int EDIT_LUM = 15;
    static final int USE_EFFECT = 35;
    static final int LIST_LUM = 2;
    static final String TAG = "ListLuminance";
    SharedPref sharedPref;
    int codeIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_luminance_fragment);
        /*
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_list);
        setSupportActionBar(myToolbar);
*/
        ActionBar ab = getActionBar();

        sharedPref = new SharedPref();
        populateLuminanceList();

        codeIntent = getIntent().getExtras().getInt("Code");
        Log.i(TAG, "Codi: " + codeIntent);
        if (luminance != null){
            luminanceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    //Luminance selItem = (Luminance) adapter.getItem(position);
                    Luminance lum = luminance.get(position);

                    if (codeIntent == LIST_LUM) {
                        startAddLuminance(lum);
                    } else if (codeIntent == USE_EFFECT) {
                        Log.i(TAG, "dintre seleccionar effecte");
                        getLuminance(lum);

                    }


                }
            });
        }


    }

    private void populateLuminanceList() {
        luminance = sharedPref.getLuminance(this);
        if (luminance != null) {
            listLuminanceAdapter = new ListLuminanceAdapter(this, luminance);
            luminanceListView = (ListView) findViewById(R.id.list_luminance);
            luminanceListView.setAdapter(listLuminanceAdapter);
        }

    }


    public void getLuminance(Luminance luminance) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", luminance);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void startAddLuminance(Luminance luminance) {
        Intent editLum = new Intent(this, AddLuminance.class);
        editLum.putExtra("luminance", luminance);
        editLum.putExtra("code", EDIT_LUM);
        startActivity(editLum);
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here
        setContentView(R.layout.list_luminance_fragment);
        sharedPref = new SharedPref();
        populateLuminanceList();
        codeIntent = getIntent().getExtras().getInt("Code");
        if(luminance != null){
            luminanceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                    //Luminance selItem = (Luminance) adapter.getItem(position);
                    Luminance lum = luminance.get(position);
                    if (codeIntent == LIST_LUM) {
                        startAddLuminance(lum);
                    } else if (codeIntent == USE_EFFECT) {
                        Log.i(TAG, "dintre seleccionar effecte");
                        getLuminance(lum);

                    }

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_add_lum:
                // User chose the add lum
                Intent intent = new Intent(this, AddLuminance.class);
                intent.putExtra("code", ADD_LUM);
                startActivity(intent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }


    }
}