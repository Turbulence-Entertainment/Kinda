package com.qboxus.musictok.ActivitesFragment.Profile;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.qboxus.musictok.ActivitesFragment.Profile.Favourite_F.Favourite_videos_F;
import com.qboxus.musictok.ActivitesFragment.Search.Search_HashTags_F;
import com.qboxus.musictok.ActivitesFragment.SoundLists.Favourite_Sound_F;
import com.qboxus.musictok.Adapters.ViewPagerAdapter;
import com.qboxus.musictok.R;


public class Favourite_Main_F extends Fragment {

    View view;
    Context context;

    public Favourite_Main_F() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_favourite_main, container, false);
        context=getContext();

        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Set_tabs();
        return view;
    }



    protected TabLayout tabLayout;
    protected ViewPager menu_pager;
    ViewPagerAdapter adapter;
    public void Set_tabs(){

        adapter = new ViewPagerAdapter(getChildFragmentManager());
        menu_pager = (ViewPager) view.findViewById(R.id.viewpager);
        menu_pager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);



        adapter.addFrag(new Favourite_videos_F(),"Videos");
        adapter.addFrag(new Favourite_Sound_F(),"Sounds");
        adapter.addFrag(new Search_HashTags_F("favourite"),"HashTag");


        menu_pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(menu_pager);

    }



}
