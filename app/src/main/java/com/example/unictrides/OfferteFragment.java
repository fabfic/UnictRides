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

public class OfferteFragment extends Fragment {
    private User user;
    private DatabaseReference notificheRef, rootRef, offerteRef, richiesteRef, viaggiRef;
    private String uid;
    private POI POI_destinazione, POI_sorgente, filtroPoi;
    private String nome, data, ora, dataFiltro;
    private int posti;
    private List<Offerta> listOfOfferte;
    private List<Richiesta> userRequests;
    private List<Percorso> viaggiList;
    private LinearLayout cardContainer;
    private LayoutInflater inflater;
    private boolean filterON = false;

    public OfferteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        Log.d("DEBUG ON CREATE", "Arguments: " + args);
        if (args != null) {
            user = (User) args.getSerializable("user");
            if (user != null) {
                Log.d("DEBUG ON CREATE", "User: " + user);
            } else {
                Log.e("ProfileFragment", "User object is null");
            }
        } else {
            Log.e("ProfileFragment", "No arguments passed to OfferteFragment");
        }
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        notificheRef = rootRef.child("notifiche");
        offerteRef = FirebaseDatabase.getInstance().getReference().child("offerte");
        richiesteRef = FirebaseDatabase.getInstance().getReference().child("richieste");
        viaggiRef = rootRef.child("viaggi");
        Log.d("DEBUG", "user:" + user.toString());
        POI_sorgente = user.getPoiSorgete();
        POI_destinazione = user.getPoiDestinazione();
        nome = user.getNome() + " " + user.getCognome();
        posti = 2;
        listOfOfferte = new ArrayList<>();
        userRequests = new ArrayList<>();
        viaggiList = new ArrayList<>();

        setupListeners();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offerte, container, false);

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

        setupFilter(v);

        cardContainer = v.findViewById(R.id.card_container);
        this.inflater = LayoutInflater.from(getContext());

        return v;
    }

    private void setupFilter(View v) {
        TextView resetFiltro = v.findViewById(R.id.reset_filtro);

        EditText dateField = v.findViewById(R.id.date_input);
        dateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Seleziona un giorno")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                    @Override
                    public void onPositiveButtonClick(Long selection) {
                        dataFiltro = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(selection));
                        dateField.setText(dataFiltro);
                    }
                });
                materialDatePicker.show(getActivity().getSupportFragmentManager(), "tag");
            }
        });

        Spinner spinnerPoiDest = v.findViewById(R.id.spinner_poi_dest);
        ArrayList<POI> unictPoi = new ArrayList<>(POIs.getUNICT_POI());
        unictPoi.add(0, new POI("Destinazione"));
        ArrayAdapter<POI> spinnerPoiDestAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, unictPoi);
        spinnerPoiDestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoiDest.setAdapter(spinnerPoiDestAdapter);
        spinnerPoiDest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    filtroPoi = null;
                } else {
                    filtroPoi = (POI) parent.getItemAtPosition(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Button filtroButton = v.findViewById(R.id.filtro_button);
        filtroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterON = true;
                refreshOfferteFiltered();
            }
        });
        resetFiltro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterON = false;

                spinnerPoiDest.setSelection(0);
                dataFiltro = null;
                dateField.setText("");

                resetFiltro.setVisibility(View.GONE);
                Log.d("RESET FILTRO CLICK", "VADO A REFRESHARE IL FRAGMENT");
                setupListeners();
            }
        });
    }

    private void refreshOfferteFiltered() {
        ArrayList<Offerta> temp = new ArrayList<>(listOfOfferte);
        listOfOfferte.clear();
        offerteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean noSelection = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Offerta offerta = dataSnapshot.getValue(Offerta.class);

                    if (dataFiltro != null) {
                        if (filtroPoi != null) {
                            if (offerta.getData().equals(dataFiltro) && offerta.getPoiDestinazione().equals(filtroPoi)) {
                                listOfOfferte.add(offerta);
                            }
                        } else {
                            if (offerta.getData().equals(dataFiltro)) {
                                listOfOfferte.add(offerta);
                            }
                        }
                    } else if (filtroPoi != null) {
                        if (offerta.getPoiDestinazione().equals(filtroPoi)) {
                            listOfOfferte.add(offerta);
                        }
                    } else {
                        noSelection = true;
                        listOfOfferte = temp;
                    }
                }
                if (noSelection) {
                    getView().findViewById(R.id.reset_filtro).setVisibility(View.GONE);
                } else {
                    getView().findViewById(R.id.reset_filtro).setVisibility(View.VISIBLE);
                }
                refreshOfferte();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void refreshOfferte() {

        cardContainer.removeAllViews();
        outerLoop:
        for (Offerta offerta : listOfOfferte) {
            if (offerta.getPosti() == 0 || offerta.getUid().equals(uid)) {
                continue;
            }
            for (Richiesta richiesta : userRequests) {
                if (richiesta.getOfferta().getKey().equals(offerta.getKey())) {
                    continue outerLoop;
                }
            }
            for (Percorso percorso : viaggiList) {
                if (percorso.getOfferta().getKey().equals(offerta.getKey())) {
                    for (Passeggero p : percorso.getPasseggeri()) {
                        if (p.getUid().equals(uid)) {
                            continue outerLoop;
                        }
                    }
                }

            }

            View cardView = inflater.inflate(R.layout.card_offerta, cardContainer, false);

            TextView nomeUtente = cardView.findViewById(R.id.nome_utente);
            TextView dataOfferta = cardView.findViewById(R.id.data_offerta);
            TextView oraOfferta = cardView.findViewById(R.id.ora_offerta);
            TextView poiDestOfferta = cardView.findViewById(R.id.poi_dest_offerta);
            TextView postiOfferta = cardView.findViewById(R.id.posti_offerta);
            Button richiestaButton = cardView.findViewById(R.id.richiesta_button);

            nomeUtente.setText(offerta.getNomeUtente());
            dataOfferta.setText(offerta.getData());
            oraOfferta.setText(offerta.getOra());
            poiDestOfferta.setText(offerta.getPoiDestinazione().getNome());
            postiOfferta.setText(String.valueOf(offerta.getPosti()));

            richiestaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startRichiestaDialog(offerta);
                }
            });
            cardContainer.addView(cardView);
        }
    }

    private void startRichiestaDialog(Offerta offerta) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_start_richiesta);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView destinazione = dialog.findViewById(R.id.richiesta_dest);
        Spinner spinnerPoiSorgente = dialog.findViewById(R.id.dialogPOIsorgente);
        Button continuaButton = dialog.findViewById(R.id.continue_button);
        Button annullaButton = dialog.findViewById(R.id.annulla_button);

        destinazione.setText(offerta.getPoiDestinazione().getNome());

        List<POI> ctPoi = POIs.getCT_POI();
        ArrayAdapter<POI> spinnerPoiSorgenteAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, ctPoi);
        spinnerPoiSorgenteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoiSorgente.setAdapter(spinnerPoiSorgenteAdapter);

        int defaultSorgenteIndex = findIndexOfItem(ctPoi, POI_sorgente);
        spinnerPoiSorgente.setSelection(defaultSorgenteIndex);

        spinnerPoiSorgente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                POI_sorgente = (POI) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        annullaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                POI_sorgente = user.getPoiSorgete();
                dialog.dismiss();
            }
        });

        continuaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmRichiestaDialog(offerta);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void confirmRichiestaDialog(Offerta offerta) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_confirm_richiesta);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView row1 = dialog.findViewById(R.id.row1);
        TextView row2 = dialog.findViewById(R.id.row2);
        TextView row3 = dialog.findViewById(R.id.row3);
        TextView row4 = dialog.findViewById(R.id.row4);
        Button confermaButton = dialog.findViewById(R.id.confermaButton);
        Button backButton = dialog.findViewById(R.id.backButton);

        String s_row1 = "A '" + offerta.getNomeUtente() + "'";
        String s_row2 = "Da " + POI_sorgente.getNome();
        String s_row3 = "A " + offerta.getPoiDestinazione().getNome();
        String s_row4 = "Giorno " + offerta.getData() + " alle " + offerta.getOra();

        row1.setText(s_row1);
        row2.setText(s_row2);
        row3.setText(s_row3);
        row4.setText(s_row4);

        confermaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String key = rootRef.child("richieste").push().getKey();

                Richiesta richiesta = new Richiesta(key, uid, user.getNome() + " " + user.getCognome(), offerta, POI_sorgente);

                rootRef.child("richieste").child(key).setValue(richiesta);

                //notifica DB
                String notKey = notificheRef.push().getKey();
                Notifica notifica = new Notifica(notKey, 0, richiesta);
                notificheRef.child(notKey).setValue(notifica);

                //reset dati
                POI_sorgente = user.getPoiSorgete();

                Toast.makeText(getContext(), "Richiesta inviata con successo!", Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRichiestaDialog(offerta);
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

                //mandare notifiche ai "subscriber" del POI DEST
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

    private void sendNot(Offerta offerta) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        //trova tutti utenti con poi dest uguale e manda notifica
        Query query = rootRef.child("users").orderByChild("poiDestinazione/nome").equalTo(offerta.getPoiDestinazione().getNome());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    //Log.d("Firebase","user corrente:"+userSnapshot);
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

    private int findIndexOfItem(List<POI> list, POI poi) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(poi)) {
                return i;
            }
        }
        return -1;
    }

    private void setupListeners() {
        offerteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listOfOfferte.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Offerta offerta = dataSnapshot.getValue(Offerta.class);

                    if (checkDateOfferta(offerta)) {
                        listOfOfferte.add(offerta);
                    }


                }
                if (!filterON) {
                    Log.d("OFFERTEValueEventListener", "FILTER IS OFF -> refresh offerte");
                    refreshOfferte();
                } else {
                    Log.d("ValueEventListener", "FILTER IS ON -> NO refresh offerte");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        richiesteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userRequests.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Richiesta richiesta = dataSnapshot.getValue(Richiesta.class);
                    if (richiesta.getUidRichiedente().equals(uid)) {
                        if (checkRichiesta(richiesta)) {
                            userRequests.add(richiesta);
                        }
                    }
                }
                if (!filterON) {
                    Log.d("RICHIESTEValueEventListener", "FILTER IS OFF -> refresh offerte");
                    refreshOfferte();
                } else {
                    Log.d("OFFERTEValueEventListener", "FILTER IS ON -> NON refresh offerte");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Firebase", error.getMessage());
            }
        });
        viaggiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viaggiList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Percorso percorso = dataSnapshot.getValue(Percorso.class);
                    if (!percorso.getOfferta().getUid().equals(uid)) {
                        viaggiList.add(percorso);
                    }
                }
                if (!filterON) {
                    Log.d("VIAGGIListerner", "FILTER IS OFF -> refresh offerte");
                    refreshOfferte();
                } else {
                    Log.d("VIAGGIListener", "FILTER IS ON -> NON refresh offerte");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Firebase", error.getMessage());
            }
        });
    }

    private boolean checkDateOfferta(Offerta offerta) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date targetTime = dateFormat.parse(offerta.getData() + " " + offerta.getOra());
            Date currentTime = new Date();

            if (targetTime.getTime() < currentTime.getTime()) {
                Log.d("Check Offerta", "Passata piu' di un'ora.");
                offerteRef.child(offerta.getKey()).removeValue();
                return false;
            } else {
                Log.d("Check Offerta", "Passata meno di un'ora.");
                return true;
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Errore nel caricamento delle offerte", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean checkRichiesta(Richiesta richiesta) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date targetTime = dateFormat.parse(richiesta.getOfferta().getData() + " " + richiesta.getOfferta().getOra());
            Date currentTime = new Date();

            if (targetTime.getTime() < currentTime.getTime()) {
                Log.d("Check Richiesta", "Passata piu' di un'ora.");
                richiesteRef.child(richiesta.getKey()).removeValue();
                return false;
            } else {
                Log.d("Check Richiesta", "Passata meno di un'ora.");
                return true;
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Errore nel caricamento delle offerte", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
