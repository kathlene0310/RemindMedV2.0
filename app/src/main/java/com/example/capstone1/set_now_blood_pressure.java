package com.example.capstone1;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class set_now_blood_pressure extends AppCompatActivity implements fragment_bp.BloodPressuretxt,fragment_bp.BloodPressuretxtNoPulse{

    EditText bloodpressure;
    Button saveBPNow, bpDialog, cancelBP;
    FirebaseAuth rootAuthen;
    FirebaseFirestore fstore;
    String userId, dateToday, timeToday, time, startdate, enddate, sysString, diaString;
    int alarmYear, alarmMonth, alarmDay, alarmHour,alarmMin, id ,freq;
    Date myDate;
    Calendar myAlarmDate = Calendar.getInstance();
    static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
    int sysInt, diaInt, pulseInt;
    private measurement_info_today measurement_info_today;

    Date c = Calendar.getInstance().getTime();
    int choice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_now_blood_pressure);

        measurement_info_today = (measurement_info_today) getIntent().getSerializableExtra("measuremy_info_today");

        saveBPNow = findViewById(R.id.save_set_now_bp);
        bpDialog = findViewById(R.id.bp_set_now);
        rootAuthen = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userId = rootAuthen.getCurrentUser().getUid();
        cancelBP = findViewById(R.id.cancel_set_now_bp);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel
                    ("abnormalbp", "abnormalbp", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }



        bpDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBPDialog();
            }
        });


        SimpleDateFormat df = new SimpleDateFormat("M/dd/yyyy", Locale.getDefault());
        dateToday = df.format(c);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        timeToday = timeFormat.format(c);


        saveBPNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Object> user =new HashMap<>();
                String Record = bpDialog.getText().toString().trim();
                getData();
                if (bpDialog.getText().toString().equals("Set BP"))
                {
                    Toast.makeText(set_now_blood_pressure.this, "Please input a blood pressure measurement", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (choice == 1 && freq ==1)
                {
                    user.put("Name", "Bloodpressure");
                    user.put("Record",Record);
                    user.put("Date", startdate);
                    user.put("Time", time);
                    moveStartDate();
                    if(!startdate.equals(enddate))
                    {
                        startAlarm(myAlarmDate);
                    }

                }
                else if(choice == 1 && freq == 2)
                {
                    user.put("Name", "Bloodpressure");
                    user.put("Record",Record);
                    user.put("Date", startdate);
                    user.put("Time", time);
                    moveStartDateWeek();
                    if(!startdate.equals(enddate))
                    {
                        startAlarm(myAlarmDate);
                    }
                }
                else
                {
                    user.put("Name", "Bloodpressure");
                    user.put("Record",Record);
                    user.put("Date", dateToday);
                    user.put("Time", timeToday);
                }

                if(sysInt >= 140 && diaInt >= 90 || sysInt <= 90 && diaInt <= 60)
                {
                    NotificationCompat.Builder mBuilder = (NotificationCompat.Builder)
                            new NotificationCompat.Builder(set_now_blood_pressure.this, "abnormalbp");
                    mBuilder.setSmallIcon(R.drawable.logoicon);
                    mBuilder.setContentTitle("Abnormal Measurement");
                    mBuilder.setContentText("You have recently recorded an abnormal measurement");
                    mBuilder.setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(set_now_blood_pressure.this);
                    notificationManager.notify(7, mBuilder.build());

                    AlertDialog.Builder aBuilder = new AlertDialog.Builder(set_now_blood_pressure.this);
                    aBuilder.setCancelable(true);
                    aBuilder.setTitle("Abnormal Measurement");
                    aBuilder.setMessage("You have recently recorded an abnormal measurement for your blood pressure click ok to see some recommendations to normalize it");

                    aBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(set_now_blood_pressure.this, home_page.class);
                        intent.putExtra("description", "Bloodpressure");
                        startActivity(intent);

                    }
                });

                    aBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(set_now_blood_pressure.this, recommendations.class);
                            intent.putExtra("description", "Bloodpressure");
                            startActivity(intent);
                        }
                    });

                    aBuilder.show();


                }
                else{
                    startActivity(new Intent(set_now_blood_pressure.this, home_page.class));

                }

                fstore.collection("users").document(userId).collection("New Health Measurements")
                        .document("Bloodpressure").collection("Bloodpressure")
                        .add(user)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(set_now_blood_pressure.this, "New Bloodpressure measurement added", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Log.d(TAG,"onSuccess: failed");
                            }
                        });
            }
        });

        cancelBP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(set_now_blood_pressure.this);
                aBuilder.setCancelable(true);
                aBuilder.setTitle("Cancel");
                aBuilder.setMessage("Are you sure you want to cancel");

                aBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                });

                aBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(set_now_blood_pressure.this, home_page.class);
                        startActivity(intent);
                    }
                });

                aBuilder.show();
            }
        });
    }

    private void getData() {
        if (getIntent().hasExtra("Time") && getIntent().hasExtra("Date")) {
            time = getIntent().getStringExtra("Time");
            startdate = getIntent().getStringExtra("Date");
            enddate = getIntent().getStringExtra("EndDate");
            choice = getIntent().getIntExtra("fromToday", 1);
            freq = getIntent().getIntExtra("Frequency", 1 );
            alarmHour = getIntent().getIntExtra("Hour", 0);
            alarmMin = getIntent().getIntExtra("Minute", 0);

        }
    }

    public void openBPDialog()
    {
        fragment_bp fragment_bp = new fragment_bp();
        fragment_bp.show(getSupportFragmentManager(), "BP Dialog");
    }

    private void moveStartDate(){
        getData();
        myDate = getDateFromString(startdate);
        if (!startdate.equals(enddate))
        {
            myDate = DateUtil.addDays(myDate, 1);
        }

        String month = (String) DateFormat.format("MM", myDate);
        String day = (String) DateFormat.format("dd", myDate);
        String year = (String) DateFormat.format("yyyy", myDate);

        alarmMonth = Integer.parseInt(month);
        alarmDay = Integer.parseInt(day);
        alarmYear = Integer.parseInt(year);

        if (startdate.equals(enddate)) {
            fstore.collection("users").document(userId).collection("Health Measurement Alarm").document(measurement_info_today.getId()).delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(set_now_blood_pressure.this, "Deleted Alarm", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        } else {
            fstore.collection("users").document(userId).collection("Health Measurement Alarm")
                    .document(measurement_info_today.getId()).update("StartDate", myDate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void avoid) {
                            Toast.makeText(set_now_blood_pressure.this, "Confirmed Intake", Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    private void moveStartDateWeek() {
        getData();
        myDate = getDateFromString(startdate);
        if (!startdate.equals(enddate) && daysBetween(dateToCalendar(getDateFromString(startdate)), dateToCalendar(getDateFromString(enddate))) > 7)
        {
            myDate = DateUtil.addDays(myDate, 7);
        }
        else
        {
            myDate = getDateFromString(enddate);
        }
        String month = (String) DateFormat.format("MM", myDate);
        String day = (String) DateFormat.format("dd", myDate);
        String year = (String) DateFormat.format("yyyy", myDate);

        alarmMonth = Integer.parseInt(month);
        alarmDay = Integer.parseInt(day);
        alarmYear = Integer.parseInt(year);

        if (startdate.equals(enddate)) {
            fstore.collection("users").document(userId).collection("Health Measurement Alarm").document(measurement_info_today.getId()).delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(set_now_blood_pressure.this, "Deleted Alarm", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        } else {
            fstore.collection("users").document(userId).collection("Health Measurement Alarm")
                    .document(measurement_info_today.getId()).update("StartDate", myDate)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void avoid) {
                            Toast.makeText(set_now_blood_pressure.this, "Confirmed Intake", Toast.LENGTH_LONG).show();
                        }
                    });
        }

    }

    private void startAlarm(Calendar c)
    {
        getData();
        String month = (String) DateFormat.format("MM", myDate);
        String day = (String) DateFormat.format("dd", myDate);
        String year = (String) DateFormat.format("yyyy", myDate);

        alarmMonth = Integer.parseInt(month);
        alarmDay = Integer.parseInt(day);
        alarmYear = Integer.parseInt(year);
        myAlarmDate.setTimeInMillis(System.currentTimeMillis());
        myAlarmDate.set(alarmYear, alarmMonth-1, alarmDay, alarmHour, alarmMin);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, alarmreceivermeasurement.class);
        Intent i = new Intent(this, alarm_notification.class);
        id = new Random().nextInt(1000000);
        i.putExtra("userID", id);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, myAlarmDate.getTimeInMillis(), pendingIntent);
        //  alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, myAlarmDate.getTimeInMillis(), 24*60*60*1000, pendingIntent);
    }


    @Override
    public void applyBPtext(String bp, int sys, int dia, int pulse) {
        bpDialog.setText(bp);
        sysInt = sys;
        diaInt = dia;
        pulseInt = pulse;

    }
    public void nowbp_To_hm(View view) {
        Intent intent = new Intent(set_now_blood_pressure.this, health_measurements.class);
        startActivity(intent);
    }
    public void nowbp_To_hm1(View view) {
        Intent intent = new Intent(set_now_blood_pressure.this, health_measurements.class);
        startActivity(intent);
    }

    public static long daysBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        long daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private Calendar dateToCalendar(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;

    }

    public Date getDateFromString(String dateToSave) {
        try {
            Date date = dateFormat.parse(dateToSave);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public void applyBPtextnoP(String bp, int sys, int dia) {
        bpDialog.setText(bp);
        sysInt = sys;
        diaInt = dia;
    }
}



