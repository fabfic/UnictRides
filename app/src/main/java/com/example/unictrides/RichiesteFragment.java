package com.example.unictrides;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class RichiesteFragment extends Fragment {
    private String uid;
    private DatabaseReference richiesteRef, rootRef;
    private List<Richiesta> listOfRichieste;
    private LayoutInflater inflater;
    private LinearLayout cardContainer;

    public RichiesteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        richiesteRef = FirebaseDatabase.getInstance().getReference().child("richieste");
        rootRef = FirebaseDatabase.getInstance().getReference();

        listOfRichieste = new ArrayList<>();
        richiesteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOfRichieste.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Richiesta richiesta = dataSnapshot.getValue(Richiesta.class);
                    listOfRichieste.add(richiesta);
                }
                refreshRichieste();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_richieste, container, false);

        cardContainer = v.findViewById(R.id.card_container_richieste);
        this.inflater = LayoutInflater.from(getContext());

        return v;
    }

    private void refreshRichieste() {
        cardContainer.removeAllViews();

        for (Richiesta richiesta : listOfRichieste) {
            if (!richiesta.getUidRichiedente().equals(uid)) {
                continue;
            }

            View cardView = inflater.inflate(R.layout.card_richiesta, cardContainer, false);

            TextView nomeUtente = cardView.findViewById(R.id.nome_utente);
            TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
            TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
            TextView poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
            TextView poiSorgRichiesta = cardView.findViewById(R.id.poi_sorgente_richiesta);
            Button annullaButton = cardView.findViewById(R.id.annulla_button);

            Offerta offerta = richiesta.getOfferta();

            nomeUtente.setText(offerta.getNomeUtente());
            dataOfferta.setText(offerta.getData());
            oraOfferta.setText(offerta.getOra());
            poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
            poiSorgRichiesta.setText(richiesta.getPoiSorgente().getNome());

            annullaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rimuoviRichiestaDialog(richiesta);
                }
            });

            cardContainer.addView(cardView);
        }
    }

    private void rimuoviRichiestaDialog(Richiesta richiesta) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_cancella_richiesta);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button removeButton = dialog.findViewById(R.id.removeButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = richiesta.getKey();
                richiesteRef.child(key).removeValue();
                //rimuovi notifica richesta
                rootRef.child(richiesta.getKey()).removeValue();

                Toast.makeText(getContext(), "Richiesta rimossa con successo", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
