package tfg.uab.jga.appopencv;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by JordiPC on 15/04/2017.
 */

public class ListLuminanceAdapter extends ArrayAdapter<Luminance> {

    private Context context;
    List<Luminance> luminanceList;
    SharedPref sharedPref;


    public ListLuminanceAdapter (Context context, List<Luminance> luminanceList){
        super(context,R.layout.luminance_list_adapter,luminanceList);
        this.context = context;
        this.luminanceList = luminanceList;
        this.sharedPref = new SharedPref();
    }

    private class ViewHolder{
        TextView textRed;
        TextView textBlue;
        TextView textGreen;
        TextView textName;

    }

    @Override
    public int getCount(){
        return luminanceList.size();
    }

    @Override
    public Luminance getItem(int position){
        return luminanceList.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = null;
        if (convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.luminance_list_adapter,null);
            holder = new ViewHolder();
            holder.textBlue = (TextView) convertView.findViewById(R.id.txt_blue);
            holder.textGreen = (TextView) convertView.findViewById(R.id.txt_green);
            holder.textName = (TextView) convertView.findViewById(R.id.txt_name);
            holder.textRed = (TextView) convertView.findViewById(R.id.txt_red);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        Luminance lum = (Luminance) getItem(position);
        holder.textBlue.setText(lum.getBlue());
        holder.textRed.setText(lum.getRed());
        holder.textGreen.setText(lum.getGreen());
        holder.textName.setText(lum.getName());
        return convertView;
    }

    @Override
    public void add(Luminance luminance) {
        super.add(luminance);
        luminanceList.add(luminance);
        notifyDataSetChanged();
    }

    @Override
    public void remove(Luminance luminance) {
        super.remove(luminance);
        luminanceList.remove(luminance);
        notifyDataSetChanged();
    }


}
