package com.example.unictrides;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private User user;
    private Veicolo veicolo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadData();
        } else {
            Log.d("Main Activity onCreate", "setting content view");
            setContentView(R.layout.activity_main);
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        }
    }

    public void loginClick(View v) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void registerClick(View v) {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
    }

    private void loadData() {
        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference().child("veicoli").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                user = dataSnapshot.getValue(User.class);

                vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        veicolo = dataSnapshot.getValue(Veicolo.class);

                        onDataLoaded();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FETCH_DATA", "error fetching data");
                        mAuth.signOut();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onDataLoaded() {
        Intent intent = new Intent(MainActivity.this, Homepage.class);
        intent.putExtra("user", user);
        intent.putExtra("veicolo", veicolo);
        startActivity(intent);
        finish();
    }
}