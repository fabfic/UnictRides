package com.example.unictrides;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TueOfferteFragment extends Fragment {
    private String uid;
    private DatabaseReference rootRef, offerteRef, richiesteRef, notificheRef;
    private List<Offerta> listOfOfferte;
    private LayoutInflater inflater;
    private LinearLayout cardContainer;
    private User user;
    private POI POI_destinazione, POI_sorgente;
    private String nome, data, ora;
    private int posti;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            user = (User) args.getSerializable("user");
        } else {
            Log.e("ProfileFragment", "Nessun argomento passato a offerte_fragment");
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        offerteRef = FirebaseDatabase.getInstance().getReference().child("offerte");
        richiesteRef = rootRef.child("richieste");
        notificheRef = rootRef.child("notifiche");

        listOfOfferte = new ArrayList<>();
        offerteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOfOfferte.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Offerta offerta = dataSnapshot.getValue(Offerta.class);
                    listOfOfferte.add(offerta);
                }
                refreshOfferte();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        POI_sorgente = user.getPoiSorgete();
        POI_destinazione = user.getPoiDestinazione();
        posti = 2;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tue_offerte, container, false);

        cardContainer = v.findViewById(R.id.card_container);
        this.inflater = LayoutInflater.from(getContext());

        FloatingActionButton fab = v.findViewById(R.id.floating_action_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isGuidatore()) {
                    openDateDialog();
                } else {
                    Toast.makeText(getContext(), "Non puoi offrire un passaggio senza un veicolo", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return v;
    }

    private void refreshOfferte() {
        cardContainer.removeAllViews();

        for (Offerta offerta : listOfOfferte) {
            if (!offerta.getUid().equals(uid)) {
                continue;
            }

            View cardView = inflater.inflate(R.layout.card_tua_offerta, cardContainer, false);

            TextView nomeUtente = cardView.findViewById(R.id.nome_utente);
            TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
            TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
            TextView poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
            TextView postiOfferta = cardView.findViewById(R.id.posti_offerta);
            Button annullaButton = cardView.findViewById(R.id.annulla_button);

            nomeUtente.setText(offerta.getNomeUtente());
            dataOfferta.setText(offerta.getData());
            oraOfferta.setText(offerta.getOra());
            poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
            postiOfferta.setText(String.valueOf(offerta.getPosti()));

            annullaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rimuoviOffertaDialog(offerta);
                }
            });

            cardContainer.addView(cardView);
        }
    }

    private void rimuoviOffertaDialog(Offerta offerta) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_cancella_offerta);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button removeButton = dialog.findViewById(R.id.removeButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = offerta.getKey();
                offerteRef.child(key).removeValue();

                //cerca richieste collegate all'offerta e eliminale->manda notifiche rimozione richiesta
                richiesteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Richiesta richiesta = dataSnapshot.getValue(Richiesta.class);
                            if (richiesta.getOfferta().getKey().equals(key)) {
                                String richiestaKey = richiesta.getKey();
                                richiesteRef.child(richiestaKey).removeValue();

                                String notKey = notificheRef.push().getKey();
                                Notifica notifica = new Notifica(notKey, 3, richiesta);
                                notificheRef.child(notKey).setValue(notifica);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                Toast.makeText(getContext(), "Offerta rimossa con successo", Toast.LENGTH_SHORT).show();
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


    private void openDateDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_pickdate);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText dateField = dialog.findViewById(R.id.dialog_date_input);
        EditText timeField = dialog.findViewById(R.id.dialog_time_input);
        Button continuaButton = dialog.findViewById(R.id.continueButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        if (ora != null && data != null) {
            dateField.setText(data);
            timeField.setText(ora);
        }


        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar today = Calendar.getInstance();

                CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
                constraintsBuilder.setStart(today.getTimeInMillis()); // Set the start date to tomorrow (in milliseconds)

                // Build the constraints
                CalendarConstraints constraints = constraintsBuilder.build();

                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Seleziona un giorno")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .setCalendarConstraints(constraints)
                        .build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        data = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection));
                        dateField.setText(data);
                    }
                });
                materialDatePicker.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });

        timeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(00)
                        .setTitleText("Seleziona un orario")
                        .build();
                timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (timePicker.getHour() < 10) {
                            if (timePicker.getMinute() < 10) {
                                ora = timePicker.getHour() + ":0" + timePicker.getMinute();
                                timeField.setText(ora);
                            } else {
                                ora = timePicker.getHour() + ":" + timePicker.getMinute();
                                timeField.setText(ora);
                            }
                        } else if (timePicker.getMinute() < 10) {
                            ora = timePicker.getHour() + ":0" + timePicker.getMinute();
                            timeField.setText(ora);
                        } else {
                            ora = timePicker.getHour() + ":" + timePicker.getMinute();
                            timeField.setText(ora);
                        }
                    }
                });
                timePicker.show(getParentFragmentManager(), "tag");
            }
        });

        continuaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ora != null && data != null) {
                    openPoiDialog();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Riempi i campi", Toast.LENGTH_SHORT).show();
                }
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

    private void openPoiDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_pick_poi);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button continuaButton = dialog.findViewById(R.id.continueButton);
        Button backButton = dialog.findViewById(R.id.backButton);
        Spinner spinnerPoiSorgente = dialog.findViewById(R.id.dialogPOIsorgente);
        Spinner spinnerPoiDestinazione = dialog.findViewById(R.id.dialogPOIdest);

        List<POI> ctPoi = POIs.getCT_POI();
        ArrayAdapter<POI> spinnerPoiSorgenteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ctPoi);
        spinnerPoiSorgenteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoiSorgente.setAdapter(spinnerPoiSorgenteAdapter);

        List<POI> unictPoi = POIs.getUNICT_POI();
        ArrayAdapter<POI> spinnerPoiDestAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, unictPoi);
        spinnerPoiDestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoiDestinazione.setAdapter(spinnerPoiDestAdapter);

        int defaultSorgenteIndex = findIndexOfItem(ctPoi, POI_sorgente);
        spinnerPoiSorgente.setSelection(defaultSorgenteIndex);

        int defaultDestIndex = findIndexOfItem(unictPoi, POI_destinazione);
        spinnerPoiDestinazione.setSelection(defaultDestIndex);

        spinnerPoiSorgente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                POI_sorgente = (POI) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPoiDestinazione.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                POI_destinazione = (POI) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDateDialog();
                dialog.dismiss();
            }
        });

        continuaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPostiDialog();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openPostiDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_pick_posti);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView num = dialog.findViewById(R.id.n_posti);
        ImageView leftArrow = dialog.findViewById(R.id.leftArrow);
        ImageView rightArrow = dialog.findViewById(R.id.rightArrow);
        Button continuaButton = dialog.findViewById(R.id.continueButton);
        Button backButton = dialog.findViewById(R.id.backButton);

        aggiornaPostiPicker(leftArrow, rightArrow, posti);
        num.setText(String.valueOf(posti));

        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posti--;
                aggiornaPostiPicker(leftArrow, rightArrow, posti);
                num.setText(String.valueOf(posti));

            }
        });

        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                posti++;
                aggiornaPostiPicker(leftArrow, rightArrow, posti);
                num.setText(String.valueOf(posti));
            }
        });

        continuaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeOfferDialog();
                dialog.dismiss();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPoiDialog();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void aggiornaPostiPicker(ImageView leftArrow, ImageView rightArrow, int num) {
        if (num == 1) {
            leftArrow.setClickable(false);
            leftArrow.setAlpha(0.4f);
        } else if (num == 2) {
            leftArrow.setClickable(true);
            leftArrow.setAlpha(0.9f);

            rightArrow.setClickable(true);
            rightArrow.setAlpha(0.9f);
        } else if (num == 3) {
            rightArrow.setClickable(false);
            rightArrow.setAlpha(0.4f);
        }
    }

    private void completeOfferDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_complete_offer);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView row1 = dialog.findViewById(R.id.row1);
        TextView row2 = dialog.findViewById(R.id.row2);
        TextView row3 = dialog.findViewById(R.id.row3);
        TextView row4 = dialog.findViewById(R.id.row4);
        TextView row5 = dialog.findViewById(R.id.row5);
        Button confermaButton = dialog.findViewById(R.id.confermaButton);
        Button backButton = dialog.findViewById(R.id.backButton);

        String s_row1 = "Offerta di passaggio da '" + nome + "'";
        String s_row2 = "Giorno " + data + " alle ore " + ora;
        String s_row3 = "Da " + POI_sorgente.getNome();
        String s_row4 = "A " + POI_destinazione.getNome();
        String s_row5 = "Posti disponibili: " + posti;

        row1.setText(s_row1);
        row2.setText(s_row2);
        row3.setText(s_row3);
        row4.setText(s_row4);
        row5.setText(s_row5);

        confermaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String key = rootRef.child("offerte").push().getKey();

                Offerta offerta = new Offerta(key, uid, nome, data, ora, POI_sorgente, POI_destinazione, posti);

                rootRef.child("offerte").child(key).setValue(offerta);

                //reset dati
                data = null;
                ora = null;
                POI_sorgente = user.getPoiSorgete();
                POI_destinazione = user.getPoiDestinazione();
                posti = 2;

                Toast.makeText(getContext(), "Offerta pubblicata con successo!", Toast.LENGTH_SHORT).show();

                //mandare notifica ai sbscriber di poi dest
                dialog.dismiss();
                sendNot(offerta);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPostiDialog();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private int findIndexOfItem(List<POI> list, POI poi) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(poi)) {
                return i;
            }
        }
        return -1;
    }

    private void sendNot(Offerta offerta) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        //trova tutti utenti con poi dest uguale e manda notifica
        List<User> users = new ArrayList<>();
        Query query = rootRef.child("users").orderByChild("poiDestinazione/nome").equalTo(offerta.getPoiDestinazione().getNome());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                    //manda notifica
                    if (!userSnapshot.getKey().equals(uid)) {
                        String notKey = notificheRef.push().getKey();
                        Notifica notifica = new Notifica(notKey, 5, offerta, userSnapshot.getKey());
                        notificheRef.child(notKey).setValue(notifica);
                    }
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
                dialog.dismiss();
            }
        });
    }

}
