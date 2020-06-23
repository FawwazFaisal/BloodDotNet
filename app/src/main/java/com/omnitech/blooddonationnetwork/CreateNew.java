package com.omnitech.blooddonationnetwork;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.omnitech.blooddonationnetwork.Adapter.DonationViewAdapter;
import com.omnitech.blooddonationnetwork.Fragments.DonationFragment;
import com.omnitech.blooddonationnetwork.Fragments.RequestFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CreateNew extends AppCompatActivity {

    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    private Double Latitude;
    private Double Longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new);

        Intent callingIntent = getIntent();
        Latitude = callingIntent.getDoubleExtra("Latitude",0);
        Longitude = callingIntent.getDoubleExtra("Longitude",0);

        toolbar = findViewById(R.id.toolbar_donation);
        viewPager = findViewById(R.id.view_pager_create_new);
        tabLayout = findViewById(R.id.tab_layout_create_new);
        setSupportActionBar(toolbar);
        viewPager.setAdapter(setAdapter());
        tabLayout.setupWithViewPager(viewPager);

    }

    private PagerAdapter setAdapter() {
        DonationViewAdapter  adapter = new DonationViewAdapter(getSupportFragmentManager());
        adapter.addFragment(new DonationFragment(), "Donation");
        adapter.addFragment(new RequestFragment(), "Request");
        return adapter;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setTime(final TextView time){
        final View dialogView = View.inflate(this, R.layout.timepicker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

                // Create a calendar object that will convert the date and time value in milliseconds to date.
                calendar.setTimeInMillis(calendar.getTimeInMillis());
                time.setText((String.valueOf(formatter.format(calendar.getTime()))));
                alertDialog.dismiss();
            }});
        alertDialog.setView(dialogView);
        alertDialog.show();
    }
}
