package com.example.unictrides;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

public class TravelFragment extends Fragment {
    private int state;

    public TravelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            Log.d("HELP", "args not null");
            state = (int) args.getSerializable("state");
        } else {
            Log.e("Travel fragment", "Nessun argomento passato a travel_fragment");
        }
        Log.d("sdfsf", "travelFragment running");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_travel, container, false);

        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        openTab(new PassaggiFragment());
                        break;
                    case 1:
                        openTab(new FragmentInCorso());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (state == 0) {
            Log.d("TRAVEL FRAGMENT", "STATE = 0");
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            openTab(new PassaggiFragment());
        } else if (state == 1) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            openTab(new FragmentInCorso());
        }
        return v;
    }

    private void openTab(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.travel_frame_layout, fragment);
        transaction.commit();
    }
}