package com.example.unictrides;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
    private User user;
    private Veicolo veicolo;

    private POI POI_sorgente, POI_destinazione;
    private EditText emailInput, passwordInput, nomeInput, cognomeInput, telInput, targa_input, modello_input, colore_input;
    private TextView targa_text, modello_text, colore_text;
    private String email, password, nome, cognome, tel, facolta, targa, modello, colore;
    private FirebaseAuth mAuth;
    private boolean guidatore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);


        //facoltà spinner
        Spinner spinner_facolta = (Spinner) findViewById(R.id.facolta_input);
        ArrayAdapter<CharSequence> adapter_facolta = ArrayAdapter.createFromResource(this, R.array.facolta, android.R.layout.simple_spinner_item);
        adapter_facolta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_facolta.setAdapter(adapter_facolta);
        spinner_facolta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#FAFAFA"));
                facolta = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //poi sorgente spinner
        Spinner spinnerPoiSorgente = findViewById(R.id.poi_sorgente_input);
        List<POI> ctPoi = POIs.getCT_POI();
        ArrayAdapter<POI> spinnerPoiSorgenteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ctPoi);
        spinnerPoiSorgenteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoiSorgente.setAdapter(spinnerPoiSorgenteAdapter);
        spinnerPoiSorgente.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#FAFAFA"));

                POI_sorgente = (POI) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //poi destinazione spinner
        Spinner spinnerPoiDestinazione = findViewById(R.id.poi_destinazione_input);
        List<POI> unictPoi = POIs.getUNICT_POI();
        ArrayAdapter<POI> spinnerPoiDestAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unictPoi);
        spinnerPoiDestAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPoiDestinazione.setAdapter(spinnerPoiDestAdapter);
        spinnerPoiDestinazione.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.parseColor("#FAFAFA"));

                POI_destinazione = (POI) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //salvo gli input
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        nomeInput = findViewById(R.id.nome_input);
        cognomeInput = findViewById(R.id.cognome_input);
        telInput = findViewById(R.id.numero_input);
        targa_input = findViewById(R.id.targa_input);
        modello_input = findViewById(R.id.modello_input);
        colore_input = findViewById(R.id.colore_input);
        targa_text = findViewById(R.id.targa_text);
        modello_text = findViewById(R.id.modello_text);
        colore_text = findViewById(R.id.colore_text);

        //firebase setup
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(RegisterActivity.this, Homepage.class));
        }
    }

    public void onRegister(View v) {
        Log.d("D", "Bottone Register cliccato");

        getCampi();

        if (guidatore == false) {
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(nome) || TextUtils.isEmpty(cognome) || TextUtils.isEmpty(tel)) {
                Toast.makeText(RegisterActivity.this, "Riempi tutti i campi", Toast.LENGTH_SHORT).show();
                Log.d("debug", "guidatore false, riempire i campi");
                return;
            }
        } else if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(nome) || TextUtils.isEmpty(cognome) || TextUtils.isEmpty(tel) || TextUtils.isEmpty(targa) || TextUtils.isEmpty(modello) || TextUtils.isEmpty(colore)) {
            Toast.makeText(RegisterActivity.this, "Riempi tutti i campi", Toast.LENGTH_SHORT).show();
            Log.d("debug", "guidatore true, riempire i campi");
            return;
        }
        if (guidatore) {
            if (!checkGuidatore()) return;
        }
        if (!nomeValidation()) return;
        if (!emailValidation()) return;
        if (!passwordValidation()) return;

        phoneValidation(new PhoneNumberCheckCallback() {
            @Override
            public void onPhoneNumberCheckComplete(boolean isPhoneNumberValid) {
                if (isPhoneNumberValid) {
                    register();
                } else {
                    Log.d("D", "numero esistente, mostro toast");
                    Toast.makeText(RegisterActivity.this, "Numero di telefono già esistente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean nomeValidation() {
        if (nome.length() > 32) {
            Toast.makeText(RegisterActivity.this, "Il nome non deve contenere più di 32 caratteri", Toast.LENGTH_SHORT).show();
            return false;
        } else if (cognome.length() > 32) {
            Toast.makeText(RegisterActivity.this, "Il cognome non deve contenere più di 32 caratteri", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean emailValidation() {
        String dominio = "@studium.unict.it";
        if (!email.contains(dominio)) {
            Toast.makeText(RegisterActivity.this, "È necessaria una mail istituzionale (@studium.unict.it)", Toast.LENGTH_SHORT).show();
            return false;
        } else if (email.length() <= 17) {
            Toast.makeText(RegisterActivity.this, "Email non valida", Toast.LENGTH_SHORT).show();
            return false;
        } else if (email.length() > 81) {
            Toast.makeText(RegisterActivity.this, "Eccesso di caratteri nella mail", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean passwordValidation() {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?!.*\\s).+$";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(password);

        if ((password.length() < 8)) {
            Toast.makeText(RegisterActivity.this, "La password deve contenere almeno 8 caratteri", Toast.LENGTH_SHORT).show();
            return false;
        } else if (password.length() > 32) {
            Toast.makeText(RegisterActivity.this, "La password non può contenere più di 32 caratteri", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!m.matches()) {
            Toast.makeText(RegisterActivity.this, "La password deve contenere almeno una lettera maiuscola,minuscola e un numero senza spazi", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void guidatoreClicked(View v) {
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            guidatore = true;

            targa_text.setVisibility(View.VISIBLE);
            targa_input.setVisibility(View.VISIBLE);
            modello_text.setVisibility(View.VISIBLE);
            modello_input.setVisibility(View.VISIBLE);
            colore_text.setVisibility(View.VISIBLE);
            colore_input.setVisibility(View.VISIBLE);

        } else {
            guidatore = false;

            targa_text.setVisibility(View.GONE);
            targa_input.setVisibility(View.GONE);
            modello_text.setVisibility(View.GONE);
            modello_input.setVisibility(View.GONE);
            colore_text.setVisibility(View.GONE);
            colore_input.setVisibility(View.GONE);
        }
    }

    private void phoneValidation(final PhoneNumberCheckCallback callback) {
        Log.d("D", "controllo la lunghezza del num di telefono");
        if (tel.length() != 10) {
            Toast.makeText(RegisterActivity.this, "Numero di telefono non valido", Toast.LENGTH_LONG).show();
            callback.onPhoneNumberCheckComplete(false);
        } else {
            Log.d("D", "effettuo una query per cercare il numero di telefono");
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
            Query phoneNumberQuery = usersRef.orderByChild("telefono").equalTo(tel);
            phoneNumberQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("D", "controllo se il numero è in uso");
                    boolean isPhoneNumberValid = !(dataSnapshot.exists());
                    callback.onPhoneNumberCheckComplete(isPhoneNumberValid);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("D", "Errore nella query del numero di telefono");
                    callback.onPhoneNumberCheckComplete(false);
                }
            });
        }
    }


    private void saveUserData(String uid) {
        user = new User(email, nome, cognome, facolta, tel, guidatore, POI_sorgente, POI_destinazione);
        rootRef.child("users").child(uid).setValue(user);
        if (guidatore) {
            veicolo = new Veicolo(modello, targa, colore, uid);
            rootRef.child("veicoli").child(uid).setValue(veicolo);
        }
    }

    private void getCampi() {
        email = emailInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();
        nome = nomeInput.getText().toString().trim();
        cognome = cognomeInput.getText().toString().trim();
        tel = telInput.getText().toString().trim();
        targa = targa_input.getText().toString().trim().toUpperCase();
        modello = modello_input.getText().toString().trim();
        colore = colore_input.getText().toString().trim();
    }

    private void register() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, save data in the database, update UI with the signed-in user's information

                            String uid = mAuth.getCurrentUser().getUid();
                            saveUserData(uid);

                            Intent intent = new Intent(RegisterActivity.this, Homepage.class);
                            intent.putExtra("user", user);
                            intent.putExtra("veicolo", veicolo);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(RegisterActivity.this, "Email già in uso", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }

    private boolean checkGuidatore() {
        if (targa.length() != 7) {
            Toast.makeText(RegisterActivity.this, "Targa non conforme", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}