package com.example.capstone1;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
public class new_measurements_pulserate extends AppCompatActivity {
    EditText pulserate;
    Button buttonsavespulserate;
    FirebaseAuth rootAuthen;
    FirebaseFirestore fstore;
    String userId;
    Spinner spinnerpr;

    Button timeButtonpulse;
    int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_measurements_pulserate);

        pulserate = findViewById(R.id.pulse_rate_box);
        buttonsavespulserate = findViewById(R.id.btnsavepulserate);
        spinnerpr = findViewById(R.id.frequency_spinner_four);

        rootAuthen = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();

        userId = rootAuthen.getCurrentUser().getUid();

        //Spinner my_spinner = (Spinner) findViewById(R.id.frequency_spinner_eight);

        ArrayAdapter<String> myAdapter2 = new ArrayAdapter<String>(new_measurements_pulserate.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.frequency));
        myAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerpr.setAdapter(myAdapter2);

        timeButtonpulse = findViewById(R.id.time_btn_six);

        timeButtonpulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(new_measurements_pulserate.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                                hour = i;
                                minute = i1;
                                String time = hour + ":" + minute;
                                SimpleDateFormat f24Hours = new SimpleDateFormat("HH:mm"
                                );
                                try {
                                    Date date = f24Hours.parse(time);
                                    SimpleDateFormat f12Hours = new SimpleDateFormat("hh:mm aa"
                                    );
                                    timeButtonpulse.setText(f12Hours.format(date));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 12, 0, false
                );
                timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                timePickerDialog.updateTime(hour, minute);
                timePickerDialog.show();
            }
        });

        buttonsavespulserate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Pulserate = pulserate.getText().toString().trim();
                //String Frequency = spinnerpr.getSelectedItem().toString().trim();
                String TimePulseRate = timeButtonpulse.getText().toString().trim();


                Map<String, Object> user = new HashMap<>();
                user.put("Time",TimePulseRate);
                user.put("Record", Pulserate);
                user.put("Name", "Pulserate");

                //user.put("FrequencyPulseR", Frequency );

                if (TextUtils.isEmpty(Pulserate)) {
                    pulserate.setError("This field is required");
                    return;
                }
                if(timeButtonpulse.getText().toString().equals("Set Time")){
                    Toast.makeText(getApplicationContext(), "Please select Time", Toast.LENGTH_SHORT).show();
                    return;
                }

                fstore.collection("users").document(userId).collection("New Health Measurements").document("Pulserate").collection("Pulserate")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(new_measurements_pulserate.this, "New Pulserate measurement added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Log.d(TAG,"onSuccess: failed");
                            }
                        });
/*
                fstore.collection("users").document(userId).collection("New Health Measurements").document("Pulserate").set(user, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(new_measurements_pulserate.this, "New pulserate measurement added", Toast.LENGTH_SHORT).show();
                    }
                });

 */
            }
        });
/*
        DocumentReference documentReference = fstore.collection("users").document(userId).collection("New Health Measurements").document("Pulserate");
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                pulserate.setText(value.getString("Pulserate"));

            }
        });

 */
    }
    //added spinner and timePicker
    public void Pulserate_To_Health(View view) {
        Intent intent = new Intent(new_measurements_pulserate.this, health_measurements.class);
        startActivity(intent);

    }
}