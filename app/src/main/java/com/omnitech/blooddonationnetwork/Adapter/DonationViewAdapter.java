package com.omnitech.blooddonationnetwork.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class DonationViewAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragList = new ArrayList<>();
    ArrayList<String> titleList = new ArrayList<>();

    public DonationViewAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }

    public void addFragment(Fragment fragment, String title){
        fragList.add(fragment);
        titleList.add(title);
    }

    @Override
    public int getCount() {
        return fragList.size();
    }
    @Override
    public String getPageTitle(int position) {
        return titleList.get(position);
    }
}
