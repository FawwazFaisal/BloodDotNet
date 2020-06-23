package com.omnitech.blooddonationnetwork.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.omnitech.blooddonationnetwork.R;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    View mWindow;
    Context mContext;
    public InfoWindowAdapter(Context context){
        mContext = context;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.info_window_layout, null);
    }
    public View renderView(Marker marker){
        String title = marker.getTitle().split(",")[0];
        String snippet = marker.getSnippet();
        LinearLayout parent = mWindow.findViewById(R.id.parent);
        TextView titleView = parent.findViewById(R.id.title);
        TextView snippetView = parent.findViewById(R.id.snippet);


        titleView.setText(title);
        snippetView.setText(snippet);
        return mWindow;
    }
    @Override
    public View getInfoWindow(Marker marker) {
        return renderView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return renderView(marker);
    }
}
