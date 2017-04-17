package tfg.uab.jga.appopencv;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ListLuminance extends Fragment {

    public static final String ARG_LUM_ID = "luminance_list";

    Activity activity;
    ListView luminanceListView;
    List<Luminance> luminances;
    ListLuminanceAdapter listLuminanceAdapter;

    SharedPref sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        sharedPref = new SharedPref();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savesInstaceState){
        View view = inflater.inflate(R.layout.list_luminance_fragment,container,false);
        findViewsById(view);
        luminances = sharedPref.getLuminance(this.getContext());

        listLuminanceAdapter = new ListLuminanceAdapter(activity,luminances);
        luminanceListView.setAdapter(listLuminanceAdapter);
        return view;


    }

    private void findViewsById(View view) {
        luminanceListView = (ListView) view.findViewById(R.id.list_luminance);
    }


    @Override
    public void onResume(){
        getActivity().setTitle("onResume bitch");
        getActivity().getActionBar().setTitle("Same sht");
        super.onResume();
    }

}
