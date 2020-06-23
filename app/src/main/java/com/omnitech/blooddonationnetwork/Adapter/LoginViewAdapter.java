package com.omnitech.blooddonationnetwork.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class LoginViewAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragList = new ArrayList<>();
    ArrayList<String> titleList = new ArrayList<>();

    public LoginViewAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragList.get(position);
    }

    @Override
    public int getCount() {
        return fragList.size();
    }

    public void addFragment(Fragment fragment, String title){
        fragList.add(fragment);
        titleList.add(title);
    }

    @Override
    public String getPageTitle(int position) {
        return titleList.get(position);
    }
}
