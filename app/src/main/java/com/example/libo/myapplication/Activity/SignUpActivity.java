package com.example.libo.myapplication.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.libo.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressBar progressBar;

    EditText editTextUsername,editTextPassword,editTextEmail;

    private FirebaseAuth mAuth;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up);

        editTextUsername = (EditText) findViewById(R.id.editTextUsername);

        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);





        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.buttonSignUp).setOnClickListener(this);

        findViewById(R.id.buttonCancel).setOnClickListener(this);

    }



    private void RegisterUser(){

        String username = editTextUsername.getText().toString().trim();

        String password = editTextPassword.getText().toString().trim();

        String email = editTextEmail.getText().toString().trim();



        if (username.isEmpty()){

            editTextUsername.setError("Username is required");

            editTextUsername.requestFocus();

        }

        if (password.isEmpty()){

            editTextPassword.setError("Password is required");

            editTextPassword.requestFocus();

        }

        if (email.isEmpty()){

            editTextEmail.setError("Email is required");

            editTextEmail.requestFocus();

        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            editTextEmail.setError("Please enter a valid email");

            editTextEmail.requestFocus();

            return;

        }



        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override

            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(getApplicationContext(),"User Registered Successfull",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, BasicActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);

                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException){

                        Toast.makeText(getApplicationContext(),"User email already registered",Toast.LENGTH_SHORT).show();

                    }

                    else {

                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }

                }

            }

        });

    }



    @Override

    public void onClick (View view){

        switch (view.getId()){

            case R.id.buttonSignUp:

                RegisterUser();

                break;



            case R.id.buttonCancel:

                startActivity(new Intent(this, MainActivity.class));

        }

    }

}