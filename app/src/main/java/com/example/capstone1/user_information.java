package com.example.capstone1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class user_information extends AppCompatActivity {
    public static final String TAG = "TAG";
    EditText birthyr, height, weight;
    int weightChoice, heightChoice;
    Button buttonSave, buttonLogout, buttonDeleteAcc;
    TextView email, firstname, lastname, faq;
    FirebaseAuth rootAuthen;
    FirebaseFirestore fstore;
    FirebaseUser firebaseUser;
    String userId, genderDB, weightName, heightName;
    Spinner spnWeight, spnHeight;
    Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);


        email = (TextView) findViewById(R.id.emailview);

        spinner = findViewById(R.id.gender_spinner);
        birthyr = findViewById(R.id.editTextbirth);
        height = findViewById(R.id.editTextheight);
        weight = findViewById(R.id.editTextweight);
        buttonSave = findViewById(R.id.btnSave);
        buttonLogout = findViewById(R.id.btnLogout);
        buttonDeleteAcc = findViewById(R.id.btnDeleteAcc);
        rootAuthen = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        firebaseUser = rootAuthen.getCurrentUser();
        faq = (TextView)findViewById(R.id.FAQ);
        spnHeight = findViewById(R.id.spinnerHeight);
        spnWeight = findViewById(R.id.spinnerWeight);
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(user_information.this, Faq.class);
                startActivity(intent);
            }
        });

        ArrayAdapter<String> adapterWeight = new ArrayAdapter<String>(user_information.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array
                .weight));
        adapterWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnWeight.setAdapter(adapterWeight);

        ArrayAdapter<String> adapterHeight = new ArrayAdapter<String>(user_information.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.height));

        adapterWeight.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnHeight.setAdapter(adapterHeight);

        spnWeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weightChoice = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnHeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                heightChoice = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });




        userId = rootAuthen.getCurrentUser().getUid();

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(user_information.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.gender));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(myAdapter);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Gender = spinner.getSelectedItem().toString().trim();
                String Birthyr = birthyr.getText().toString().trim();
                String Height = height.getText().toString().trim();
                String Weight = weight.getText().toString().trim();
                heightName = spnHeight.getSelectedItem().toString();
                weightName = spnWeight.getSelectedItem().toString();

                if (spinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(getApplicationContext(), "Please select gender", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> user = new HashMap<>();
                user.put("gender", Gender);
                user.put("birthyr", Birthyr);
                user.put("height", Height);
                user.put("weight", Weight);
                user.put("weightChoice", weightChoice);
                user.put("weightName", weightName);

                user.put("heightChoice", heightChoice);
                user.put("heightName", heightName);


                fstore.collection("users").document(userId).set(user, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(user_information.this, "User information added", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        DocumentReference documentReference = fstore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "listen:error", error);
                    return;
                }
                heightName = value.getString("heightName");
                weightName = value.getString("weightName");
                if(spnHeight!=null)
                {
                    int pos = adapterHeight.getPosition(heightName);
                    spnHeight.setSelection(pos);
                }

                if(spnWeight!=null)
                {
                    int pos = adapterWeight.getPosition(weightName);
                    spnWeight.setSelection(pos);
                }



                email.setText(value.getString("email"));
                //firstname.setText(value.getString("firstname"));
                //lastname.setText(value.getString("lastname"));
                genderDB = (value.getString("gender"));
                if (genderDB != null) {
                    int pos = myAdapter.getPosition(genderDB);
                    spinner.setSelection(pos);
                }
                birthyr.setText(value.getString("birthyr"));
                height.setText(value.getString("height"));
                weight.setText(value.getString("weight"));
            }
        });

        //logout
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(user_information.this, main_page.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        //delete account
        buttonDeleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(user_information.this);
                dialog.setTitle("Are you sure?");
                dialog.setMessage("Deleting this account will permanently remove your account from the system");
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(user_information.this, "Account Deleted", Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent(user_information.this, main_page.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(user_information.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    }
                });

                dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = dialog.create();
                alertDialog.show();
            }
        });
    }

    public void User_To_Account(View view) {
        Intent intent = new Intent(user_information.this, change_name.class);
        startActivity(intent);
    }

    /*
        public void Logout (View view){
            Intent intent = new Intent(user_information.this, main_page.class);
            startActivity(intent);

        }

     */
    public void User_To_Home(View view) {
        Intent intent = new Intent(user_information.this, home_page.class);
        startActivity(intent);
    }
}