package com.example.unictrides;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationFragment extends Fragment {
    private String uid;
    private DatabaseReference notificheRef, richiesteRef, viaggiRef, offerteRef;
    private List<Notifica> notificationList;
    private LayoutInflater inflater;
    private LinearLayout cardContainer;
    private User user;
    private Veicolo veicolo;

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        notificheRef = FirebaseDatabase.getInstance().getReference().child("notifiche");
        richiesteRef = FirebaseDatabase.getInstance().getReference().child("richieste");
        viaggiRef = FirebaseDatabase.getInstance().getReference().child("viaggi");
        offerteRef = FirebaseDatabase.getInstance().getReference().child("offerte");
        Bundle args = getArguments();
        if (args != null) {
            user = (User) args.getSerializable("user");
            veicolo = (Veicolo) args.getSerializable("veicolo");
        } else {
            Log.e("NotificationFragment", "Nessuno argomento passato al notification fragment");
        }

        notificationList = new ArrayList<>();

        notificheRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notifica notifica = dataSnapshot.getValue(Notifica.class);

                    switch (notifica.getType()) {
                        case 0:
                            //richiesta passaggio
                            if (notifica.getRichiesta().getOfferta().getUid().equals(uid)) {
                                if (checkNotifica0(notifica)) {
                                    notificationList.add(notifica);
                                }
                            }
                            break;
                        case 1:
                        case 2:
                        case 3:
                            //una richiesta è stata rimossa
                            //accetto richesta di passaggio
                            //rifiuto richiesta di passaggio
                            if (notifica.getRichiesta().getUidRichiedente().equals(uid)) {
                                notificationList.add(notifica);
                            }
                            break;
                        case 4:
                            //avvio viaggio
                            if (notifica.getPasseggero().getUid().equals(uid)) {
                                notificationList.add(notifica);
                            }
                            break;

                        case 5:
                            //notifica nuova offerta poi dest
                            if (!notifica.getOfferta().getUid().equals(uid) && notifica.getUid().equals(uid)) {
                                if (checkNotifica5(notifica)) {
                                    notificationList.add(notifica);
                                }
                            }
                            break;

                        case 6:
                            //notifica recensione viaggio
                            if (notifica.getUid().equals(uid)) {
                                notificationList.add(notifica);
                            }
                            break;
                    }
                }
                refreshNotifiche();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notification, container, false);

        cardContainer = v.findViewById(R.id.card_container);
        this.inflater = LayoutInflater.from(getContext());

        return v;
    }

    private void refreshNotifiche() {
        View cardView;
        TextView nomeUtente;
        TextView dataOfferta;
        TextView oraOfferta;
        TextView poiSorgOfferta;
        TextView poiDestOfferta;
        TextView posizione;
        TextView numPosti;

        Button refuseButton;
        Button okButton;
        Button vaiButton;
        Button acceptButton;

        Richiesta richiesta;
        Offerta offerta;

        cardContainer.removeAllViews();
        for (Notifica notifica : notificationList) {
            switch (notifica.getType()) {
                case 0:
                    //richiesta passaggio
                    cardView = inflater.inflate(R.layout.card_notifica_0, cardContainer, false);

                    nomeUtente = cardView.findViewById(R.id.nome_utente);
                    dataOfferta = cardView.findViewById(R.id.data_offerta);
                    oraOfferta = cardView.findViewById(R.id.ora_offerta);
                    poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
                    poiSorgOfferta = cardView.findViewById(R.id.poi_sorgente_offerta);
                    posizione = cardView.findViewById(R.id.posizione_richiesta);
                    numPosti = cardView.findViewById(R.id.posti_offerta);
                    refuseButton = cardView.findViewById(R.id.refuse_button);
                    acceptButton = cardView.findViewById(R.id.accept_button);

                    richiesta = notifica.getRichiesta();
                    offerta = richiesta.getOfferta();

                    nomeUtente.setText(richiesta.getNomeRichiedente());
                    dataOfferta.setText(offerta.getData());
                    oraOfferta.setText(offerta.getOra());
                    poiSorgOfferta.setText(offerta.getPoiSorgente().getNome());
                    poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
                    posizione.setText(richiesta.getPoiSorgente().getNome());
                    numPosti.setText(String.valueOf(offerta.getPosti()));

                    refuseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovi richiesta
                            String key = notifica.getRichiesta().getKey();
                            richiesteRef.child(key).removeValue();

                            //send notifica che hai rimosso richiesta
                            String notKey = notificheRef.push().getKey();
                            Notifica newNotifica = new Notifica(notKey, 1, notifica.getRichiesta());
                            notificheRef.child(notKey).setValue(newNotifica);

                            //rimuovi notifica
                            String thisKey = notifica.getKey();
                            notificheRef.child(thisKey).removeValue();

                            Toast.makeText(getContext(), "Richiesta di passaggio rifiutata", Toast.LENGTH_SHORT).show();
                        }
                    });
                    acceptButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovo richiesta
                            richiesteRef.child(notifica.getRichiesta().getKey()).removeValue();

                            //diminuire i posti, controllare se il percorso esiste, se si aggiungere apsseggero se no creare viaggio e aggiungere passeggero
                            String offertaKey = notifica.getRichiesta().getOfferta().getKey();
                            DatabaseReference percorsoRef = viaggiRef.child(offertaKey);
                            //diminuisco posti
                            Offerta offerta = notifica.getRichiesta().getOfferta();
                            offerta.setPosti(offerta.getPosti() - 1);
                            offerteRef.child(offerta.getKey()).setValue(offerta);
                            percorsoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        //aggiungere passeggero
                                        Passeggero p = new Passeggero(notifica.getRichiesta().getUidRichiedente(), notifica.getRichiesta().getNomeRichiedente(), notifica.getRichiesta().getPoiSorgente());
                                        Percorso percorso = snapshot.getValue(Percorso.class);
                                        percorso.addPasseggero(p);
                                        viaggiRef.child(percorso.getKey()).setValue(percorso);
                                    } else {
                                        //creare viaggio e aggiungere passeggero
                                        String key = notifica.getRichiesta().getOfferta().getKey();
                                        Percorso newPercorso = new Percorso(key, notifica.getRichiesta().getOfferta(), new ArrayList<>());
                                        Passeggero p = new Passeggero(notifica.getRichiesta().getUidRichiedente(), notifica.getRichiesta().getNomeRichiedente(), notifica.getRichiesta().getPoiSorgente());
                                        newPercorso.addPasseggero(p);
                                        //salvo nuovo percorso
                                        viaggiRef.child(key).setValue(newPercorso);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //manda notifica accettazione richiesta
                            String notKey = notificheRef.push().getKey();
                            Notifica newNotifica = new Notifica(notKey, 2, notifica.getRichiesta());
                            notificheRef.child(notKey).setValue(newNotifica);

                            //rimuovere attuale notifica
                            notificheRef.child(notifica.getKey()).removeValue();

                            refreshNotifiche();
                        }
                    });
                    cardContainer.addView(cardView);
                    break;

                case 1:
                    //rifiuto richiesta di passaggio
                    cardView = inflater.inflate(R.layout.card_notifica_1, cardContainer, false);

                    nomeUtente = cardView.findViewById(R.id.nome_utente);
                    dataOfferta = cardView.findViewById(R.id.data_offerta);
                    oraOfferta = cardView.findViewById(R.id.ora_offerta);
                    poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
                    posizione = cardView.findViewById(R.id.posizione_richiesta);
                    okButton = cardView.findViewById(R.id.okButton);

                    richiesta = notifica.getRichiesta();
                    offerta = richiesta.getOfferta();

                    nomeUtente.setText(offerta.getNomeUtente());
                    dataOfferta.setText(offerta.getData());
                    oraOfferta.setText(offerta.getOra());
                    poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
                    posizione.setText(richiesta.getPoiSorgente().getNome());

                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovi notifica
                            String key = notifica.getKey();
                            notificheRef.child(key).removeValue();
                        }
                    });
                    cardContainer.addView(cardView);
                    break;

                case 2:
                    //richiesta di passaggio accettata
                    cardView = inflater.inflate(R.layout.card_notifica_2, cardContainer, false);

                    nomeUtente = cardView.findViewById(R.id.nome_utente);
                    dataOfferta = cardView.findViewById(R.id.data_offerta);
                    oraOfferta = cardView.findViewById(R.id.ora_offerta);
                    poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
                    posizione = cardView.findViewById(R.id.posizione_richiesta);
                    okButton = cardView.findViewById(R.id.okButton);

                    richiesta = notifica.getRichiesta();
                    offerta = richiesta.getOfferta();

                    nomeUtente.setText(offerta.getNomeUtente());
                    dataOfferta.setText(offerta.getData());
                    oraOfferta.setText(offerta.getOra());
                    poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
                    posizione.setText(richiesta.getPoiSorgente().getNome());

                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovi notifica
                            notificheRef.child(notifica.getKey()).removeValue();
                        }
                    });
                    cardContainer.addView(cardView);
                    break;

                case 3:
                    //una richiesta è stata rimossa->conducente ha rimosso offerta
                    cardView = inflater.inflate(R.layout.card_notifica_3, cardContainer, false);

                    okButton = cardView.findViewById(R.id.okButton);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovi notifica
                            notificheRef.child(notifica.getKey()).removeValue();
                        }
                    });
                    cardContainer.addView(cardView);
                    break;

                case 4:
                    //notifica di avvio percorso
                    cardView = inflater.inflate(R.layout.card_notifica_4, cardContainer, false);

                    TextView poi = cardView.findViewById(R.id.posizione);
                    poi.setText(notifica.getPasseggero().getPosizione().getNome());
                    okButton = cardView.findViewById(R.id.okButton);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovi notifica
                            notificheRef.child(notifica.getKey()).removeValue();
                        }
                    });
                    cardContainer.addView(cardView);
                    break;

                case 5:
                    cardView = inflater.inflate(R.layout.card_notifica_5, cardContainer, false);

                    TextView destinazione = cardView.findViewById(R.id.posizione);
                    destinazione.setText(notifica.getOfferta().getPoiDestinazione().getNome());

                    okButton = cardView.findViewById(R.id.okButton);
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //rimuovi notifica
                            notificheRef.child(notifica.getKey()).removeValue();

                            Intent intent = new Intent(getActivity(), Homepage.class);
                            intent.putExtra("user", user);
                            intent.putExtra("veicolo", veicolo);
                            startActivity(intent);
                        }
                    });
                    cardContainer.addView(cardView);
                    break;

                case 6:
                    cardView = inflater.inflate(R.layout.card_notifica_6, cardContainer, false);

                    vaiButton = cardView.findViewById(R.id.vaiButton);

                    vaiButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //vai su cronologia
                            Homepage homepageActivity = (Homepage) requireActivity();
                            homepageActivity.changeBottomNavigationSelection(R.id.nav_history);

                            //rimuovi notifica
                            notificheRef.child(notifica.getKey()).removeValue();
                        }
                    });

                    cardContainer.addView(cardView);
                    break;
            }
        }
    }

    private boolean checkNotifica5(Notifica notifica) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date targetTime = dateFormat.parse(notifica.getOfferta().getData() + " " + notifica.getOfferta().getOra());
            Date currentTime = new Date();

            if (targetTime.getTime() < currentTime.getTime()) {
                Log.d("Check Notifica 5", "Passata piu' di un'ora.");
                notificheRef.child(notifica.getKey()).removeValue();
                return false;
            } else {
                Log.d("Check Notifica 5", "Passata meno di un'ora.");
                return true;
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Errore nel caricamento delle notifiche", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private boolean checkNotifica0(Notifica notifica) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date targetTime = dateFormat.parse(notifica.getRichiesta().getOfferta().getData() + " " + notifica.getRichiesta().getOfferta().getOra());
            Date currentTime = new Date();

            if (targetTime.getTime() < currentTime.getTime()) {
                Log.d("Check Notifica 0", "Passata piu' di un'ora.");
                notificheRef.child(notifica.getKey()).removeValue();
                return false;
            } else {
                Log.d("Check Notifica 0", "Passata meno di un'ora.");
                return true;
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Errore nel caricamento delle notifiche", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

}