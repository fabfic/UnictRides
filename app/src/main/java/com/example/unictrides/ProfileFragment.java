package com.example.unictrides;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ProfileFragment extends Fragment {

    private ImageView profilePic;
    private TextView profile_nome, profile_email, profile_facolta, profile_poi_sorgente, profile_poi_dest, targaVeicolo, modelloVeicolo, coloreVeicolo;
    private User user;
    private Veicolo veicolo;
    private DatabaseReference userRef, vehicleRef;
    private String uid;
    private POI POI_destinazione, POI_sorgente;
    private String facolta;
    private ActivityResultLauncher<Intent> resultLauncher;
    private StorageReference imagesRef;
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Log.d("PermissionDebug", "Permission Result: " + isGranted);
                if (isGranted) {
                    Intent getImageIntent = new Intent(Intent.ACTION_PICK);
                    getImageIntent.setType("image/*");
                    resultLauncher.launch(getImageIntent);
                } else {
                    Toast.makeText(getContext(), "Permesso non concesso -> non potrai cambiare immagine profilo", Toast.LENGTH_LONG).show();
                }
            });

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            user = (User) args.getSerializable("user");
            veicolo = (Veicolo) args.getSerializable("veicolo");
        } else {
            Log.e("ProfileFragment", "Nessuno argomento passato al profile fragment");
        }

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        vehicleRef = FirebaseDatabase.getInstance().getReference().child("veicoli").child(uid);

        imagesRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    Uri imageUri = result.getData().getData();
                    //salvo in fragment
                    Log.d("URI LOCAL", imageUri.toString());
                    profilePic.setImageURI(imageUri);
                    //salvo in storage
                    imagesRef.child(uid).putFile(imageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                Toast.makeText(requireContext(), "Immagine caricata correttamente", Toast.LENGTH_SHORT).show();
                                //aggiorno utente
                                imagesRef.child(uid).getDownloadUrl().addOnSuccessListener(uri -> {
                                    Log.d("DOWNLOAD URI", uri.toString());
                                    user.setProfilePic(uri.toString());
                                    Log.d("SAVED URL", user.getProfilePic());
                                    userRef.setValue(user);
                                });
                            })
                            .addOnFailureListener(taskSnapshot -> {
                                Toast.makeText(getContext(), "Errore caricamento immagine", Toast.LENGTH_SHORT).show();
                            });
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Nessuna immagine selezionata", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        getFields(view);
        setUserFields();

        linkButtons(view);

        updateRating(view);

        return view;
    }

    private void linkButtons(View v) {
        Button singoutButton = v.findViewById(R.id.signoutButton);
        Button modificaPOIbutton = v.findViewById(R.id.modificaPOIbutton);
        Button aggiungiVeicolo = v.findViewById(R.id.aggiungi_veicolo);
        Button modificaVeicolo = v.findViewById(R.id.modifica_veicolo);
        Button rimuoviVeicolo = v.findViewById(R.id.rimuovi_veicolo);

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePic();
            }
        });
        singoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignout();
            }
        });
        modificaPOIbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserDialog();
            }
        });
        aggiungiVeicolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVeicoloDialog();
            }
        });
        modificaVeicolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateVeicoloDialog();
            }
        });
        rimuoviVeicolo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rimuoviVeicoloDialog();
            }
        });
    }

    private void getFields(View v) {
        profilePic = v.findViewById(R.id.profile_image_view);
        profile_nome = v.findViewById(R.id.profile_nome);
        profile_email = v.findViewById(R.id.profile_email);
        profile_facolta = v.findViewById(R.id.profile_facolta);
        profile_poi_sorgente = v.findViewById(R.id.profile_poi_sorgente);
        profile_poi_dest = v.findViewById(R.id.profile_poi_dest);

        CardView cdVeicolo = v.findViewById(R.id.cardView_Veicolo);
        CardView cdNoVeicolo = v.findViewById(R.id.cardView_noVeicolo);

        if (user.isGuidatore()) {
            cdNoVeicolo.setVisibility(View.GONE);
            cdVeicolo.setVisibility(View.VISIBLE);

            targaVeicolo = v.findViewById(R.id.profile_targa);
            modelloVeicolo = v.findViewById(R.id.profile_modello);
            coloreVeicolo = v.findViewById(R.id.profile_coloreVeicolo);

            setVehicleFields();

        } else {
            cdVeicolo.setVisibility(View.GONE);
            cdNoVeicolo.setVisibility(View.VISIBLE);
        }
    }

    private void setUserFields() {
        profile_nome.setText(user.getNome() + " " + user.getCognome());
        profile_email.setText(user.getEmail());
        profile_facolta.setText(user.getFacolta());
        profile_poi_sorgente.setText(user.getPoiSorgete().getNome());
        profile_poi_dest.setText(user.getPoiDestinazione().getNome());

        if (user.getProfilePic() != null) {
            Glide.with(requireContext()).load(user.getProfilePic()).into(profilePic);
        }
    }

    private void setVehicleFields() {
        targaVeicolo.setText(veicolo.getTarga());
        modelloVeicolo.setText(veicolo.getModello());
        coloreVeicolo.setText(veicolo.getColore());
    }

    private void updatePic() {
        Intent getImageIntent = new Intent(Intent.ACTION_PICK);
        getImageIntent.setType("image/*");
        //controllare se si hanno permessi
        if (ContextCompat.checkSelfPermission(getContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            //granted
            resultLauncher.launch(getImageIntent);
        } else {
            //senza permesso->chiedi permesso
            int currentApiVersion = android.os.Build.VERSION.SDK_INT;

            if (currentApiVersion >= Build.VERSION_CODES.TIRAMISU) {
                //api lvl >=33
                requestPermissionLauncher.launch(READ_MEDIA_IMAGES);
            } else {
                //api lvl <33
                requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void rimuoviVeicoloDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_rimuovi_veicolo);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button removeButton = dialog.findViewById(R.id.removeButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                veicolo = null;
                user.setGuidatore(false);
                getFields(getView());
                userRef.child("guidatore").setValue(false);
                vehicleRef.removeValue();
                vehicleRef = null;

                Toast.makeText(getContext(), "Veicolo rimosso con successo", Toast.LENGTH_SHORT).show();
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

    private void updateVeicoloDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_update_veicolo);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText targaInput = dialog.findViewById(R.id.dialog_targa_input);
        EditText modelloInput = dialog.findViewById(R.id.dialog_modello_input);
        EditText coloreInput = dialog.findViewById(R.id.dialog_colore_input);
        targaInput.setText(veicolo.getTarga());
        modelloInput.setText(veicolo.getModello());
        coloreInput.setText(veicolo.getColore());
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targa = targaInput.getText().toString().trim().toUpperCase();
                String modello = modelloInput.getText().toString().trim();
                String colore = coloreInput.getText().toString().trim();

                if (targa.isEmpty() || modello.isEmpty() || colore.isEmpty()) {
                    Toast.makeText(getContext(), "Riempi tutti i campi", Toast.LENGTH_SHORT).show();
                } else if (targa.equals(veicolo.getTarga()) && modello.equals(veicolo.getModello()) && colore.equals(veicolo.getColore())) {
                    Log.d("VEICOLO NON MODIFICATO", "modello=" + modello + ", " + veicolo.getModello() + "\n targa=" + targa + ", " + veicolo.getTarga() + "\n colore=" + colore + ", " + veicolo.getColore());
                    dialog.dismiss();
                } else if (targa.length() != 7) {
                    Toast.makeText(getContext(), "Targa NON valida", Toast.LENGTH_SHORT).show();
                } else {
                    veicolo = new Veicolo(modello, targa, colore, uid);
                    vehicleRef.setValue(veicolo);
                    setVehicleFields();

                    Toast.makeText(getContext(), "Veicolo aggiornato con successo!", Toast.LENGTH_SHORT).show();
                    Log.d("VEICOLO MODIFICATO", "modello=" + modello + ", " + veicolo.getModello() + "\n targa=" + targa + ", " + veicolo.getTarga() + "\n colore=" + colore + ", " + veicolo.getColore());
                    dialog.dismiss();
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

    private void openUserDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_upate_user);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        facolta = user.getFacolta();
        POI_sorgente = user.getPoiSorgete();
        POI_destinazione = user.getPoiDestinazione();

        Spinner spinner_facolta = dialog.findViewById(R.id.dialogFacoltaInput);
        Spinner spinnerPoiSorgente = dialog.findViewById(R.id.dialogPOIsorgenteInput);
        Spinner spinnerPoiDestinazione = dialog.findViewById(R.id.dialogPOIdestInput);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        ArrayAdapter<CharSequence> adapter_facolta = ArrayAdapter.createFromResource(getContext(), R.array.facolta, android.R.layout.simple_spinner_item);
        adapter_facolta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_facolta.setAdapter(adapter_facolta);

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

        String[] faculties = getResources().getStringArray(R.array.facolta);
        int defaultFacoltaIndex = Arrays.asList(faculties).indexOf(facolta);
        spinner_facolta.setSelection(defaultFacoltaIndex);

        spinner_facolta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                facolta = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setFacolta(facolta);
                user.setPoiSorgete(POI_sorgente);
                user.setPoiDestinazione(POI_destinazione);
                setUserFields();
                userRef.setValue(user);

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facolta = null;
                POI_sorgente = null;
                POI_destinazione = null;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void addVeicoloDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_add_veicolo);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        EditText targaInput = dialog.findViewById(R.id.dialog_targa_input);
        EditText modelloInput = dialog.findViewById(R.id.dialog_modello_input);
        EditText coloreInput = dialog.findViewById(R.id.dialog_colore_input);
        Button saveButton = dialog.findViewById(R.id.saveButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);


        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targa = targaInput.getText().toString().trim().toUpperCase();
                String modello = modelloInput.getText().toString().trim();
                String colore = coloreInput.getText().toString().trim();

                if (targa.isEmpty() || modello.isEmpty() || colore.isEmpty()) {
                    Toast.makeText(getContext(), "Riempi tutti i campi", Toast.LENGTH_SHORT).show();
                } else if (targa.length() != 7) {
                    Toast.makeText(getContext(), "Targa NON valida", Toast.LENGTH_SHORT).show();
                } else {
                    user.setGuidatore(true);
                    veicolo = new Veicolo(modello, targa, colore, uid);
                    vehicleRef.setValue(veicolo);
                    userRef.child("guidatore").setValue(true);

                    getFields(getView());

                    Toast.makeText(getContext(), "Veicolo aggiunto con successo!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
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

    private void onSignout() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_signout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button signoutButton = dialog.findViewById(R.id.signoutButton);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);

        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), MainActivity.class));
                getActivity().finish();
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

    private int findIndexOfItem(List<POI> list, POI poi) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(poi)) {
                return i;
            }
        }
        return -1;
    }

    private void updateRating(View v) {
        MaterialRatingBar mrb = v.findViewById(R.id.ratingBar);
        TextView ratingText = v.findViewById(R.id.rating);

        if (user.getNumRating() == 0) {
            ratingText.setText("N/A");
        } else {
            float rating = user.getRatingSum() / user.getNumRating();
            mrb.setRating(rating);
            ratingText.setText(String.format("%.1f", rating) + "stelle");
        }
    }

}