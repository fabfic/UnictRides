package com.example.unictrides;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.fragment.app.FragmentTransaction;

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

public class PassaggiFragment extends Fragment {
    LinearLayout cardContainer;
    private String uid;
    private DatabaseReference viaggiRef, offerteRef, richiesteRef, notificheRef;
    private List<Percorso> viaggi;
    private LayoutInflater inflater;
    private boolean currentPercorso;

    public PassaggiFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viaggiRef = FirebaseDatabase.getInstance().getReference().child("viaggi");
        offerteRef = FirebaseDatabase.getInstance().getReference().child("offerte");
        richiesteRef = FirebaseDatabase.getInstance().getReference().child("richieste");
        notificheRef = FirebaseDatabase.getInstance().getReference().child("notifiche");
        viaggi = new ArrayList<>();

        viaggiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentPercorso = false;
                viaggi.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Percorso p = dataSnapshot.getValue(Percorso.class);

                    if (checkPercorso(p)) {
                        if (p.getOfferta().getUid().equals(uid)) {
                            viaggi.add(p);
                            if (p.isRunning()) {
                                currentPercorso = true;
                            }
                        } else {
                            for (Passeggero pass : p.getPasseggeri()) {
                                if (pass.getUid().equals(uid)) {
                                    viaggi.add(p);
                                    break;
                                }
                            }
                        }
                    }
                }
                refreshViaggi();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_passaggi, container, false);
        this.inflater = LayoutInflater.from(getContext());

        cardContainer = v.findViewById(R.id.card_container);
        return v;
    }

    private void refreshViaggi() {

        for (Percorso viaggio : viaggi) {
            if (viaggio.isRunning()) {
                continue;
            } else if (viaggio.getOfferta().getUid().equals(uid)) {
                View cardView = inflater.inflate(R.layout.card_viaggio, cardContainer, false);

                TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
                TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
                TextView poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
                TextView poiSorgOfferta = cardView.findViewById(R.id.poi_sorgente_offerta);
                TextView posti = cardView.findViewById(R.id.posti_offerta);
                TextView numPassaggi = cardView.findViewById(R.id.num_passaggi);
                Button annullaButton = cardView.findViewById(R.id.annulla_button);
                Button startViaggio = cardView.findViewById(R.id.start_viaggio);

                Offerta offerta = viaggio.getOfferta();

                dataOfferta.setText(offerta.getData());
                oraOfferta.setText(offerta.getOra());
                poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
                poiSorgOfferta.setText(offerta.getPoiSorgente().getNome());
                posti.setText(String.valueOf(offerta.getPosti()));
                numPassaggi.setText(String.valueOf(viaggio.getPasseggeri().size()));

                if (currentPercorso) {
                    startViaggio.setEnabled(false);
                    startViaggio.setAlpha(0.4f);
                }


                annullaButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //dialog sei sicuro
                        Dialog dialog = new Dialog(requireContext());
                        dialog.setContentView(R.layout.dialog_remove_viaggio);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        Button eliminaButton = dialog.findViewById(R.id.eliminaButton);
                        Button cancelButton = dialog.findViewById(R.id.cancelButton);

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        eliminaButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //rimuovere offerta, rimuovere viaggio,rimuovere le richieste collegate, informare utenti richiedenti,toast
                                //rimuovere offerta
                                offerteRef.child(viaggio.getOfferta().getKey()).removeValue();
                                //rimuovere viaggio
                                viaggiRef.child(viaggio.getKey()).removeValue();
                                //rimuovere le richiesta
                                richiesteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            Richiesta r = ds.getValue(Richiesta.class);
                                            if (r.getOfferta().getKey().equals(viaggio.getOfferta().getKey())) {
                                                richiesteRef.child(r.getKey()).removeValue();
                                                //manda notifiche agli utenti richiedenti
                                                String notKey = notificheRef.push().getKey();
                                                Notifica notifica = new Notifica(notKey, 3, r);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Passaggio rimosso", Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show();
                    }
                });
                //start viaggio
                startViaggio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //mostra dialog
                        Dialog dialog = new Dialog(requireContext());
                        dialog.setContentView(R.layout.dialog_start_viaggio);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        TextView dataOfferta = dialog.findViewById(R.id.data_offerta);
                        TextView oraOfferta = dialog.findViewById(R.id.ora_offerta);
                        TextView poiDestOfferta = dialog.findViewById(R.id.poi_end);
                        TextView poiSorgOfferta = dialog.findViewById(R.id.poi_start);
                        LinearLayout poi1 = dialog.findViewById(R.id.poi1);
                        LinearLayout poi2 = dialog.findViewById(R.id.poi2);
                        LinearLayout poi3 = dialog.findViewById(R.id.poi3);
                        TextView tv1 = dialog.findViewById(R.id.tv1);
                        TextView tv2 = dialog.findViewById(R.id.tv2);
                        TextView tv3 = dialog.findViewById(R.id.tv3);

                        dataOfferta.setText(offerta.getData());
                        oraOfferta.setText(offerta.getOra());
                        poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
                        poiSorgOfferta.setText(offerta.getPoiSorgente().getNome());

                        int i = 1;
                        for (Passeggero p : viaggio.getPasseggeri()) {
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

                        Button backButton = dialog.findViewById(R.id.backButton);
                        Button confermaButton = dialog.findViewById(R.id.confermaButton);

                        backButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        confermaButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("CONFERMA VIAGGIO", "BOTTONE CLICCATO");
                                //set isrunning, cambia fragment e manda viaggio, -> rimuovi offerta e richieste
                                //setisRunning
                                viaggio.setRunning(true);
                                viaggiRef.child(viaggio.getKey()).setValue(viaggio);
                                //rimuovi richieste
                                richiesteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            Richiesta r = dataSnapshot.getValue(Richiesta.class);
                                            if (r.getOfferta().getKey().equals(offerta.getKey())) {
                                                richiesteRef.child(r.getKey()).removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                //rimuovi l'offerta
                                offerteRef.child(offerta.getKey()).removeValue();
                                //mando notifica ai partecipanti
                                for (Passeggero passeggero : viaggio.getPasseggeri()) {
                                    String notKey = notificheRef.push().getKey();
                                    Notifica notifica = new Notifica(notKey, 4, passeggero);
                                    notificheRef.child(notKey).setValue(notifica);
                                }
                                //avvio maps
                                openMaps(viaggio);

                                //cambia fragment
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("state", 1);
                                Fragment fragment = new TravelFragment();
                                fragment.setArguments(bundle);
                                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                                transaction.replace(R.id.travel_frame_layout, fragment);
                                Log.d("VIAGGIO", "sfragment transaction commit");
                                transaction.commit();
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                });
                cardContainer.addView(cardView);
            } else {
                Log.d("Viaggi Fragment ", "PASSEGGERO");
                View cardView = inflater.inflate(R.layout.card_passaggi, cardContainer, false);

                TextView nomeUtente = cardView.findViewById(R.id.nome_utente);
                TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
                TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
                TextView poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
                TextView poiSorgOfferta = cardView.findViewById(R.id.poi_sorgente_offerta);
                Button annullaButton = cardView.findViewById(R.id.annulla_button);

                Offerta offerta = viaggio.getOfferta();
                for (Passeggero p : viaggio.getPasseggeri()) {
                    if (p.getUid().equals(uid)) {
                        poiSorgOfferta.setText(p.getPosizione().getNome());
                        break;
                    }
                }

                nomeUtente.setText(offerta.getNomeUtente());
                dataOfferta.setText(offerta.getData());
                oraOfferta.setText(offerta.getOra());
                poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());

                annullaButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //openDialog
                        Dialog dialog = new Dialog(requireContext());
                        dialog.setContentView(R.layout.dialog_annulla_passaggio);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        Button cancelButton = dialog.findViewById(R.id.cancelButton);
                        Button annullaPassaggio = dialog.findViewById(R.id.annulla_passaggio);

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        annullaPassaggio.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //modifica viaggio: rimuovi passeggero ,aumentare posti -> reupload viaggio modificato nel db
                                for (Passeggero p : viaggio.getPasseggeri()) {
                                    if (p.getUid().equals(uid)) {
                                        viaggio.getPasseggeri().remove(p);
                                        break;
                                    }
                                }
                                viaggio.getOfferta().setPosti(viaggio.getOfferta().getPosti() + 1);
                                offerteRef.child(viaggio.getOfferta().getKey()).setValue(viaggio.getOfferta());

                                if (viaggio.getPasseggeri().isEmpty()) {
                                    viaggiRef.child(viaggio.getKey()).removeValue();
                                } else {
                                    viaggiRef.child(viaggio.getKey()).setValue(viaggio);
                                }

                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                });
                cardContainer.addView(cardView);
            }


        }
    }

    private void openMaps(Percorso percorso) {
        Uri gmmIntentUri = createDirectionsUri(percorso, percorso.getPasseggeri());

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Ensure it opens in Google Maps

        Log.d("MAPS", "starting activity intent");
        startActivity(mapIntent);


    }

    private Uri createDirectionsUri(Percorso percorso, List<Passeggero> passeggeri) {
        Uri.Builder builder = new Uri.Builder();

        // Add the base URL
        builder.scheme("https")
                .authority("www.google.com")
                .appendPath("maps")
                .appendPath("dir")
                .appendPath("")
                .appendQueryParameter("api", "1");

        builder.appendQueryParameter("origin", percorso.getOfferta().getPoiSorgente().getLatitude() + "," + percorso.getOfferta().getPoiSorgente().getLongitude());

        builder.appendQueryParameter("destination", percorso.getOfferta().getPoiDestinazione().getLatitude() + "," + percorso.getOfferta().getPoiDestinazione().getLongitude());

        StringBuilder waypointsBuilder = new StringBuilder();
        for (Passeggero p : passeggeri) {
            waypointsBuilder.append(p.getPosizione().getLatitude()).append(",").append(p.getPosizione().getLongitude()).append("|");
        }
        if (waypointsBuilder.length() > 0) {
            waypointsBuilder.setLength(waypointsBuilder.length() - 1);
        }
        builder.appendQueryParameter("waypoints", waypointsBuilder.toString());
        // Specify the travel mode (e.g., driving)
        builder.appendQueryParameter("travelmode", "driving");

        return builder.build();
    }

    private boolean checkPercorso(Percorso p) {
        if (p.isRunning()) {

            try {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                Date targetTime = dateFormat.parse(p.getOfferta().getData() + " " + p.getOfferta().getOra());
                Date currentTime = new Date();

                if (targetTime.getTime() < (currentTime.getTime() - 2 * 60 * 60 * 1000)) {
                    Log.d("Check Viaggio", "Passate piu' di 3 ore.");
                    //terminare viaggio->eliminare viaggio->aggiungere viaggio nella cronologia
                    //aggiungo alla cronologia
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference cronologiaRef = rootRef.child("cronologia");

                    String key0 = cronologiaRef.push().getKey();
                    IstanzaViaggio iv = new IstanzaViaggio(key0, p, p.getOfferta().getUid(), true, System.currentTimeMillis());
                    cronologiaRef.child(key0).setValue(iv);

                    for (Passeggero pa : p.getPasseggeri()) {
                        String key = cronologiaRef.push().getKey();
                        IstanzaViaggio istanzaViaggio = new IstanzaViaggio(key, p, pa.getUid(), false, System.currentTimeMillis());
                        cronologiaRef.child(key).setValue(istanzaViaggio);

                        //mando notifica recensione (6)
                        String notKey = rootRef.child("notifiche").push().getKey();
                        Notifica notifica = new Notifica(notKey, 6, pa.getUid());
                        rootRef.child("notifiche").child(notKey).setValue(notifica);
                    }

                    //elimino oggetto viaggio/percorso
                    viaggiRef.child(p.getKey()).removeValue();
                    return false;
                } else {
                    Log.d("Check Notifica 0", "Passate meno di 3 ore.");
                    return true;
                }

            } catch (Exception e) {
                Toast.makeText(getContext(), "Errore nel caricamento dei viaggi", Toast.LENGTH_SHORT).show();
                return false;
            }

        } else {

            try {

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                Date targetTime = dateFormat.parse(p.getOfferta().getData() + " " + p.getOfferta().getOra());
                Date currentTime = new Date();

                if (targetTime.getTime() < currentTime.getTime()) {
                    Log.d("Check Viaggio", "Passata piu' di un'ora.");
                    viaggiRef.child(p.getKey()).removeValue(); //le offerte e richieste saranno eliminate nella home
                    return false;
                } else {
                    Log.d("Check Notifica 0", "Passata meno di un'ora.");
                    return true;
                }

            } catch (Exception e) {
                Toast.makeText(getContext(), "Errore nel caricamento dei viaggi", Toast.LENGTH_SHORT).show();
                return false;
            }

        }
    }
}
