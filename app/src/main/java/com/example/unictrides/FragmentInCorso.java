package com.example.unictrides;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentInCorso extends Fragment {
    private DatabaseReference viaggiRef;
    private Percorso percorso;
    private View v;
    private String uid;
    private LinearLayout ll;
    private TextView emptyText;
    private ProgressBar progressBar;

    public FragmentInCorso() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viaggiRef = FirebaseDatabase.getInstance().getReference().child("viaggi");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_in_corso, container, false);

        ll = v.findViewById(R.id.layout_percorso);
        progressBar = v.findViewById(R.id.progress_bar);
        emptyText = v.findViewById(R.id.empty_text);

        ll.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        viaggiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean condition = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (condition) {
                        break;
                    }
                    Percorso p = dataSnapshot.getValue(Percorso.class);
                    if (p.isRunning()) {
                        if (p.getOfferta().getUid().equals(uid)) {    //if guidatore
                            condition = true;
                            percorso = p;
                            break;
                        }
                        for (Passeggero pa : p.getPasseggeri()) {  //if passeggero
                            if (pa.getUid().equals(uid)) {
                                condition = true;
                                percorso = p;
                                break;
                            }
                        }
                    }
                }
                if (condition) {
                    updateLayout();
                    ll.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.GONE);
                } else {
                    ll.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return v;
    }


    private void updateLayout() {

        if (!isAdded()) {
            // Fragment is not attached, do not proceed
            return;
        }

        TextView poiStart = v.findViewById(R.id.poi_start);
        TextView poiEnd = v.findViewById(R.id.poi_end);
        LinearLayout ll = v.findViewById(R.id.ll);
        Button terminaButton = v.findViewById(R.id.terminabutton);
        if (!uid.equals(percorso.getOfferta().getUid())) {
            terminaButton.setVisibility(View.GONE); //per i passeggeri
        }

        poiStart.setText(percorso.getOfferta().getPoiSorgente().getNome());
        poiEnd.setText(percorso.getOfferta().getPoiDestinazione().getNome());

        for (Passeggero p : percorso.getPasseggeri()) {

            if (!isAdded()) {
                // Fragment is not attached, do not proceed
                break;
            }

            LinearLayout linearLayout = new LinearLayout(requireContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            imageView.setImageResource(R.drawable.subdirectory_arrow_right_24);
            int colorAttr;
            TypedArray ta = getContext().obtainStyledAttributes(new int[]{R.attr.colorSecondaryInverse});
            colorAttr = ta.getColor(0, 0); // 0 is the default color
            ta.recycle();
            imageView.setColorFilter(colorAttr, PorterDuff.Mode.SRC_IN);

            TextView textView = new TextView(getContext());
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setTextSize(18);
            textView.setText(p.getPosizione().getNome() + " (" + p.getNome() + ")");

            // Add the ImageView and TextView to the LinearLayout
            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            ll.addView(linearLayout);
        }
        terminaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isAdded()) {
                    // Fragment is not attached, do not proceed
                    return;
                }

                //terminare viaggio->eliminare viaggio->aggiungere viaggio nella cronologia
                //aggiungo alla cronologia
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference cronologiaRef = rootRef.child("cronologia");

                String key0 = cronologiaRef.push().getKey();
                IstanzaViaggio iv = new IstanzaViaggio(key0, percorso, percorso.getOfferta().getUid(), true, System.currentTimeMillis());
                cronologiaRef.child(key0).setValue(iv);

                for (Passeggero p : percorso.getPasseggeri()) {
                    String key = cronologiaRef.push().getKey();
                    IstanzaViaggio istanzaViaggio = new IstanzaViaggio(key, percorso, p.getUid(), false, System.currentTimeMillis());
                    istanzaViaggio.setFirstRating(true);
                    cronologiaRef.child(key).setValue(istanzaViaggio);

                    //mando notifica recensione (6)
                    String notKey = rootRef.child("notifiche").push().getKey();
                    Notifica notifica = new Notifica(notKey, 6, p.getUid());
                    rootRef.child("notifiche").child(notKey).setValue(notifica);
                }

                //elimino oggetto viaggio/percorso
                rootRef.child("viaggi").child(percorso.getKey()).removeValue();

            }
        });
    }


}
