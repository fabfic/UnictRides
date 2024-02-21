package com.example.unictrides;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Homepage extends AppCompatActivity {
    private User user;
    private Veicolo veicolo;
    private BottomNavigationView menu;
    private boolean isUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        user = (User) getIntent().getSerializableExtra("user");
        veicolo = (Veicolo) getIntent().getSerializableExtra("veicolo");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference().child("veicoli").child(uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Homepage fetching", "Errore nel fetching data valueventlistener");
            }
        });
        vehicleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                veicolo = snapshot.getValue(Veicolo.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Homepage fetching", "Errore nel fetching data valueventlistener");
            }
        });

        setContentView(R.layout.activity_homepage);
        menu = findViewById(R.id.bottomNavigation);
        menu.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            } else if (itemId == R.id.nav_notification) {
                loadFragment(new NotificationFragment());
            } else if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (itemId == R.id.nav_travel) {
                loadTravelFragment(new TravelFragment());
            } else if (itemId == R.id.nav_history) {
                replaceFragment(new HistoryFragment());
            }
            return true;
        });
        menu.setSelectedItemId(R.id.nav_home);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void loadFragment(Fragment fragment) {

        Bundle bundle = new Bundle();
        bundle.putSerializable("user", user);
        bundle.putSerializable("veicolo", veicolo);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();

    }

    private void loadTravelFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("state", 0);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    public void changeBottomNavigationSelection(int itemId) {
        menu.setSelectedItemId(itemId);
    }
}