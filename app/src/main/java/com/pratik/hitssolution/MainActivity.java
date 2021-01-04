package com.pratik.hitssolution;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity {
    private EditText edtUsername,edtUserPassword;
    private Button btnLogin,btnSignUp;
    private TextView btnSkip,tvSL,tvLS;
    private RadioGroup radioGroup;
    private RadioButton rbD,rbP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        if (ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")) {
                transitionPassengerActivity();
            } else if (ParseUser.getCurrentUser().get("as").equals("Driver")) {
                transitionDriverReqListActivity();
            }
        }

        edtUsername = findViewById(R.id.edt_username);
        edtUserPassword = findViewById(R.id.edt_user_password);
        btnLogin = findViewById(R.id.btn_login);
        radioGroup = findViewById(R.id.radioGroup);
        rbD = findViewById(R.id.rb_driver);
        rbP = findViewById(R.id.rb_passenger);
        btnSignUp = findViewById(R.id.btn_signup);
        btnSkip = findViewById(R.id.btn_skip_login);
        tvLS = findViewById(R.id.tv_ls);
        tvSL = findViewById(R.id.tv_sl);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtUsername.getText().toString().equals("") && edtUserPassword.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this,"Enter your username and password",Toast.LENGTH_SHORT).show();
                } else {
                ParseUser.logInInBackground(edtUsername.getText().toString(), edtUserPassword.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null) {
                            if (user.get("as").equals("Passenger")){
                               transitionPassengerActivity();
                            }
                            else if (user.get("as").equals("Driver")){
                                transitionDriverReqListActivity();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                }
            }
        });


       btnSignUp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (rbD.isChecked() == false && rbP.isChecked() == false){
                   Toast.makeText(MainActivity.this,"Are you a driver or passenger",Toast.LENGTH_SHORT).show();
                   return;
               }

               ParseUser appUser = new ParseUser();
               appUser.setUsername(edtUsername.getText().toString());
               appUser.setPassword(edtUserPassword.getText().toString());
               if (rbD.isChecked()){
                   appUser.put("as","Driver");
               }else{
                   appUser.put("as","Passenger");
               }

               appUser.signUpInBackground(new SignUpCallback() {
                   @Override
                   public void done(ParseException e) {
                       if (e == null) {
                           if (appUser.get("as").equals("Passenger")) {

                               transitionPassengerActivity();
                           }else if (appUser.get("as").equals("Driver")){
                               transitionDriverReqListActivity();
                           }
                       }else {
                           Toast.makeText(MainActivity.this,e + "",Toast.LENGTH_SHORT).show();
                       }
                   }
               });
           }
       });


        tvLS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtUserPassword.setText(null);
                edtUsername.setText(null);
                btnLogin.setVisibility(View.GONE);
                btnSignUp.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.VISIBLE);
                tvLS.setVisibility(View.GONE);
                tvSL.setVisibility(View.VISIBLE);
            }


        });


        tvSL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtUserPassword.setText(null);
                edtUsername.setText(null);
                btnLogin.setVisibility(View.VISIBLE);
                btnSignUp.setVisibility(View.GONE);
                radioGroup.setVisibility(View.GONE);
                tvLS.setVisibility(View.VISIBLE);
                tvSL.setVisibility(View.GONE);
            }


        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Hits Solution")
                        .setMessage("Join as")
                        .setIcon(getDrawable(R.drawable.ic_skip_dialog_foreground))
                        .setPositiveButton("Passenger", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    if (ParseUser.getCurrentUser() == null){
                                        ParseAnonymousUtils.logIn(new LogInCallback() {
                                            @Override
                                            public void done(ParseUser user, ParseException e) {
                                                if (user != null && e == null){
                                                    user.put("as","Passenger");
                                                    user.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            transitionPassengerActivity();
                                                        }
                                                    });

                                                }
                                            }
                                        });
                                    }

                            }
                        }).setNegativeButton("Driver", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if (ParseUser.getCurrentUser() == null) {
                                    ParseAnonymousUtils.logIn(new LogInCallback() {
                                        @Override
                                        public void done(ParseUser user, ParseException e) {
                                            if (user != null && e == null) {
                                                user.put("as", "Driver");
                                                user.saveInBackground();
                                                transitionDriverReqListActivity();
                                            }
                                        }
                                    });
                                }

                            }
                        });
             alertDialog.show();
            }
        });
    }

    public void rootLayoutTapped(View v){
        try{
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void transitionPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(MainActivity.this,PassengerActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void transitionDriverReqListActivity(){

        if(ParseUser.getCurrentUser() != null){
            if (ParseUser.getCurrentUser().get("as").equals("Driver")){
                startActivity(new Intent(MainActivity.this,DriverRequestList.class));
                finish();
            }
        }
    }


}