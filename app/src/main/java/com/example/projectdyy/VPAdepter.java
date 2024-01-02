package com.example.projectdyy;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Collections;

public class VPAdepter extends FragmentStatePagerAdapter {
    private ArrayList<Fragment> items;
    private ArrayList<String> itext = new ArrayList<String>();

    private boolean hasMQTTInfo = false;

    public VPAdepter(FragmentManager fm){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        items = new ArrayList<>();
        items.add(new Fragment1());
        items.add(new Fragment2());

        itext.add("1");
        itext.add("+");


    }

    // 예시: MQTT 정보를 Fragment에 전달하는 메서드
    public void setMQTTInfo(int position, String mqttBroker, String mqttUsername, String mqttPassword) {
        //position에 해당하는 Fragment에 정보를 전달
        Fragment fragment = items.get(position);
        if (fragment instanceof Fragment1) {
            ((Fragment1) fragment).setMQTTInfo(mqttBroker, mqttUsername, mqttPassword);
        }
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(int position){
        return itext.get(position);
    }
    @Override
    public Fragment getItem(int position) {
        return items.get(position);
    }
    @Override
    public int getCount() { return items.size(); }
}