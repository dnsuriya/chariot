package com.sidedrive.chariot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiderLoginActivity extends AppCompatActivity {

    private final static String debugRiderEmail = "rider@rider.com";
    private final static String debugPassword = "123456";


    private EditText m_email, m_passowrd;
    private Button m_login, m_register;
    private FirebaseAuth m_auth;
    private FirebaseAuth.AuthStateListener m_authListner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_login);

        m_auth = FirebaseAuth.getInstance();

        m_authListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if(user != null)
                {
                    Intent driverLoginIntent = new Intent(RiderLoginActivity.this, RiderMapsActivity.class);
                    startActivity(driverLoginIntent);
                    finish();
                    return;
                }
            }
        };

        m_email = (EditText) findViewById(R.id.email);
        m_passowrd = (EditText) findViewById(R.id.password);

        m_login = (Button) findViewById(R.id.login);
        m_register = (Button) findViewById(R.id.register);

        m_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = debugRiderEmail;//m_email.getText().toString();
                String password = debugPassword;//m_passowrd.getText().toString();

                m_auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(RiderLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isComplete())
                        {
                            Toast.makeText(RiderLoginActivity.this, "Login Error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        m_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = m_email.getText().toString();
                String password = m_passowrd.getText().toString();

                final Task<AuthResult> authResultTask = m_auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RiderLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isComplete())
                        {
                            Toast.makeText(RiderLoginActivity.this, "Registration Error", Toast.LENGTH_LONG).show();
                        }
                        else if (task.isSuccessful())
                        {
                            String userID = m_auth.getCurrentUser().getUid();
                            DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("Users").child("Riders").child(userID);
                            databaseref.setValue(true);
                        }
                        else
                        {
                            Toast.makeText(RiderLoginActivity.this, "Error", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        m_auth.addAuthStateListener(m_authListner);
    }

    @Override
    protected void onStop() {
        super.onStop();
        m_auth.removeAuthStateListener(m_authListner);
    }
}
