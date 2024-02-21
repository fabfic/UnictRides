package com.example.unictrides;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class HistoryFragment extends Fragment {
    private String uid;
    private DatabaseReference cronologiaRef, userRef;
    private List<IstanzaViaggio> listOfIstanze;
    private LayoutInflater inflater;
    private LinearLayout cardContainer;
    private float previousRating;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        cronologiaRef = FirebaseDatabase.getInstance().getReference().child("cronologia");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        listOfIstanze = new ArrayList<>();
        cronologiaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOfIstanze.clear();
                long currentTimestamp = System.currentTimeMillis();
                long thresholdMillis = 30L * 24 * 60 * 60 * 1000;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    IstanzaViaggio iv = dataSnapshot.getValue(IstanzaViaggio.class);

                    if ((currentTimestamp - iv.getTimestamp()) >= thresholdMillis) {//se son passati più di 30 giorni
                        cronologiaRef.child(iv.getKey()).removeValue();
                        continue;
                    }

                    if (iv.getUid().equals(uid)) {
                        listOfIstanze.add(iv);
                    }
                }
                refreshHistory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Firebase", error.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        cardContainer = v.findViewById(R.id.card_container);
        this.inflater = LayoutInflater.from(getContext());

        previousRating = 0f;

        return v;
    }

    private void refreshHistory() {
        cardContainer.removeAllViews();
        for (IstanzaViaggio istanzaViaggio : listOfIstanze) {

            if (istanzaViaggio.isGuidatore()) {
                View cardView = inflater.inflate(R.layout.card_cronologia_guidatore, cardContainer, false);

                TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
                TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
                TextView poiDestOfferta = cardView.findViewById(R.id.poi_end);
                TextView poiSorgOfferta = cardView.findViewById(R.id.poi_start);
                LinearLayout poi1 = cardView.findViewById(R.id.poi1);
                LinearLayout poi2 = cardView.findViewById(R.id.poi2);
                LinearLayout poi3 = cardView.findViewById(R.id.poi3);
                TextView tv1 = cardView.findViewById(R.id.tv1);
                TextView tv2 = cardView.findViewById(R.id.tv2);
                TextView tv3 = cardView.findViewById(R.id.tv3);
                MaterialRatingBar mrbGuidatore = cardView.findViewById(R.id.mrbGuidatore);

                Offerta offerta = istanzaViaggio.getPercorso().getOfferta();

                dataOfferta.setText(offerta.getData());
                oraOfferta.setText(offerta.getOra());
                poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
                poiSorgOfferta.setText(offerta.getPoiSorgente().getNome());

                int i = 1;
                for (Passeggero p : istanzaViaggio.getPercorso().getPasseggeri()) {
                    switch (i) {
                        case 1:
                            poi1.setVisibility(View.VISIBLE);
                            String s1 = p.getPosizione().getNome() + " (" + p.getNome() + ")";
                            tv1.setText(s1);
                            break;
                        case 2:
                            poi2.setVisibility(View.VISIBLE);
                            String s2 = p.getPosizione().getNome() + " (" + p.getNome() + ")";
                            tv2.setText(s2);
                            break;
                        case 3:
                            poi3.setVisibility(View.VISIBLE);
                            String s3 = p.getPosizione().getNome() + " (" + p.getNome() + ")";
                            tv3.setText(s3);
                    }
                    i++;
                }


                cronologiaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int numRating = 0;
                        float sumRating = 0f;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            IstanzaViaggio istanza = dataSnapshot.getValue(IstanzaViaggio.class);

                            if (istanza.getPercorso().getKey().equals(istanzaViaggio.getPercorso().getKey())) {

                                if (istanza.isGuidatore()) {
                                    continue;
                                }

                                if (istanza.getRating() != null) {
                                    numRating++;
                                    sumRating += istanza.getRating();
                                }

                            }
                        }
                        if (numRating > 0) {
                            mrbGuidatore.setRating(sumRating / numRating);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Firebase", error.getMessage());
                    }
                });

                cardContainer.addView(cardView);

            } else {
                View cardView = inflater.inflate(R.layout.card_cronologia_passaggio, cardContainer, false);

                TextView nomeUtente = cardView.findViewById(R.id.nome_utente);
                TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
                TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
                TextView poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
                TextView poiSorgRichiesta = cardView.findViewById(R.id.poi_sorgente);
                MaterialRatingBar mrb = cardView.findViewById(R.id.mrb);

                Offerta offerta = istanzaViaggio.getPercorso().getOfferta();
                for (Passeggero p : istanzaViaggio.getPercorso().getPasseggeri()) {
                    if (p.getUid().equals(uid)) {
                        poiSorgRichiesta.setText(p.getPosizione().getNome());
                        break;
                    }
                }
                nomeUtente.setText(offerta.getNomeUtente());
                dataOfferta.setText(offerta.getData());
                oraOfferta.setText(offerta.getOra());
                poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());

                if (istanzaViaggio.getRating() != null) {
                    previousRating = istanzaViaggio.getRating();
                    mrb.setRating(istanzaViaggio.getRating());
                }

                mrb.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
                    @Override
                    public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                        String guidatoreKey = istanzaViaggio.getPercorso().getOfferta().getUid();
                        userRef.child(guidatoreKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User guidatore = snapshot.getValue(User.class);
                                guidatore.setRatingSum(guidatore.getRatingSum() + rating - previousRating);
                                Log.d("ISFIRSTRATING", "isFirstRAting:" + istanzaViaggio.isFirstRating());
                                if (istanzaViaggio.isFirstRating()) {
                                    Log.d("NUM RATING", String.valueOf(guidatore.getNumRating()));
                                    guidatore.setNumRating(guidatore.getNumRating() + 1);
                                    Log.d("NUM RATING", String.valueOf(guidatore.getNumRating()));
                                }

                                istanzaViaggio.setRating(rating);
                                cronologiaRef.child(istanzaViaggio.getKey()).setValue(istanzaViaggio);

                                userRef.child(guidatoreKey).setValue(guidatore);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d("Firebase", error.getMessage());
                            }
                        });
                    }
                });
                cardContainer.addView(cardView);
            }

        }
    }

}