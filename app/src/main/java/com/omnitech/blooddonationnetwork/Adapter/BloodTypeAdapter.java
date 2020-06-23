package com.omnitech.blooddonationnetwork.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.omnitech.blooddonationnetwork.R;

import java.util.ArrayList;

public class BloodTypeAdapter extends ArrayAdapter<BloodType> {
    Context mContext;
    int mResource;
    String bloodtype;
    public BloodTypeAdapter(@NonNull Context context, int resource, ArrayList<BloodType> pojo) {
        super(context, R.layout.spinner_item_layout, pojo);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return (initView(position, convertView, parent));
    }
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if(convertView==null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }
        BloodType currentItem = getItem(position);
        if(currentItem!=null) {
            bloodtype = currentItem.getBloodType();
            ConstraintLayout constraintLayout = convertView.findViewById(R.id.spinner_contraint_layout);
            TextView BloodType = constraintLayout.findViewById(R.id.textView);
            BloodType.setText(bloodtype);
        }
        return convertView;
    }
}
