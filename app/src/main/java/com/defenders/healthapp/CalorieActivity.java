package com.defenders.healthapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

@RequiresApi(api = Build.VERSION_CODES.O)
public class CalorieActivity extends AppCompatActivity {

    CircleImageView profile_pic;
    private GoogleSignInAccount account;

    GoogleSignInClient mGoogleSignInClient;

    private boolean authInProgress = false;
    private static final int REQUEST_OAUTH = 1;

    String personName,personGivenName,weight,height;
    String height_in_cm;
    Uri personPhoto;

    TextView t1,t2,t3,t4;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_calorie);


        t1 = findViewById(R.id.t1);
        t2 = findViewById(R.id.t2);
        t3 = findViewById(R.id.t3);
        t4 = findViewById(R.id.t4);



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        account = GoogleSignIn.getLastSignedInAccount(this);


        profile_pic = findViewById(R.id.profile_pic);
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =  new Intent(CalorieActivity.this,ProfileActivity.class);
                i.putExtra("name",personGivenName);
                i.putExtra("photo",personPhoto.toString());
                i.putExtra("weight",weight);
                i.putExtra("height",height_in_cm);
                startActivity(i);
            }
        });
        FitnessOptions fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_DELTA).addDataType(DataType.TYPE_CALORIES_EXPENDED).addDataType(DataType.TYPE_DISTANCE_DELTA).build();

        account = GoogleSignIn.getAccountForExtension(this,fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    REQUEST_OAUTH, // e.g. 1
                    account,
                    fitnessOptions);
        } else {
            stepsGoogle();
            totalCalorieGoogle();
            distanceGoogle();
            signIn();
            height_weightGoogle();



            CalorieInterval(9,12,0,0,t1);
            CalorieInterval(12,16,10,0,t2);
            CalorieInterval(16,21,0,0,t3);
            CalorieInterval(21,23,0,59,t4);

        }




    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_OAUTH);
    }

    void stepsGoogle(){
        Fitness.getHistoryClient(this,account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA).addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        TextView steps = findViewById(R.id.steps);

                        if(dataSet.getDataPoints().isEmpty()){
                            steps.setText("0");

                        }else {
                            String value  = dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).toString() ;
                            Log.e("Steps Count:",value);

                            steps.setText(value);
                        }}
                });

    }

    DataReadRequest height_weightGoogle(){
        Calendar calendar = Calendar.getInstance();
        Date date = new Date();
        calendar.setTime(date);
        long currentTime = calendar.getTimeInMillis();
        calendar.add(Calendar.YEAR, -1);
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .read(DataType.TYPE_HEIGHT)
                .setLimit(1)
                .setTimeRange(1, currentTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this,account)
                .readData(readRequest)
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        weight = dataReadResponse.getDataSet(DataType.TYPE_WEIGHT).getDataPoints().get(0).getValue(Field.FIELD_WEIGHT).toString();

                        height = dataReadResponse.getDataSet(DataType.TYPE_HEIGHT).getDataPoints().get(0).getValue(Field.FIELD_HEIGHT).toString();

                        Double www = Double.parseDouble(height);
                        Double ttt = www * 100;
                        height_in_cm = ttt.toString();

                        Log.e(" Weight:",weight);
                        Log.e(" Height:",height_in_cm);

                    }


                });


        return readRequest;

    }

    void distanceGoogle(){
        Fitness.getHistoryClient(this,account)
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA).addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        TextView distance = findViewById(R.id.distance);

                        if(dataSet.getDataPoints().isEmpty()){
                            distance.setText("0");
                        }else {
                            String value  = (dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).toString()) ;
                            float i = Float.parseFloat(value)/1000;
                            String s = String.format("%.1f", i);
                            Log.e("distance:",s);

                            distance.setText(s+" km");
                        }

                    }
                });

    }

    void totalCalorieGoogle(){
        Fitness.getHistoryClient(this,account)
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {

                        TextView tcalorie = findViewById(R.id.tcalorie);

                        if(dataSet.getDataPoints().isEmpty()){
                            tcalorie.setText("0");

                        }else {
                            String value = dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).toString();
                            int i = Math.round(Float.parseFloat(value));
                            Log.e(" Total Calorie:", String.valueOf(i));
                            tcalorie.setText(String.valueOf(i));

                        }
                    }
                });

    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OAUTH) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            authInProgress = false;
            if (resultCode == RESULT_OK) {
                stepsGoogle();
            } else if (resultCode == RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }

        stepsGoogle();
        totalCalorieGoogle();
        distanceGoogle();
        height_weightGoogle();

        CalorieInterval(9,12,0,0,t1);
        CalorieInterval(12,16,0,0,t2);
        CalorieInterval(16,21,0,0,t3);
        CalorieInterval(21,23,0,59,t4);





    }


    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            personName = acct.getGivenName();
            TextView name = findViewById(R.id.name);
            name.setText(personName);
//            Log.e("name:",personName);
            personGivenName = acct.getDisplayName();
            CircleImageView profilepic = findViewById(R.id.profile_pic);
            personPhoto = acct.getPhotoUrl();
            Picasso.get()
                    .load(personPhoto)
                    .into(profilepic);
        }

        // Signed in successfully, show authenticated UI.
    }


    public void CalorieInterval(int shour, int ehour,int smin, int emin, TextView text){


        ZonedDateTime startTime = ZonedDateTime.of( ZonedDateTime.now().getYear(),
                ZonedDateTime.now().getMonthValue(),
                ZonedDateTime.now().getDayOfMonth(),
                shour, smin, 00, 00,ZoneId.systemDefault() );


        ZonedDateTime endTime = ZonedDateTime.of( ZonedDateTime.now().getYear(),
                ZonedDateTime.now().getMonthValue(),
                ZonedDateTime.now().getDayOfMonth(),
                ehour, emin, 00, 00, ZoneId.systemDefault() );

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByActivityType(1, TimeUnit.SECONDS)
                .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build();

        Fitness.getHistoryClient(this,account)
                .readData(readRequest)
                .addOnSuccessListener (response -> {
                    // The aggregate query puts datasets into buckets, so convert to a
                    // single list of datasets
                    for (Bucket bucket : response.getBuckets()) {
                        for (DataSet dataSet : bucket.getDataSets()) {
                            if(dataSet.getDataPoints().isEmpty()){
                                text.setText("0");
                            }else {
                                String result = dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).toString();
                                int i = Math.round(Float.parseFloat(result));
                                Log.e("data:", String.valueOf(i));
                                String r = String.valueOf(i);
                                text.setText(r + " kcl");
                            }

                        }
                    }
                });


    }





}
