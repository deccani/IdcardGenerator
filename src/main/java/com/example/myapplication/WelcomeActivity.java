package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class WelcomeActivity extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public FirebaseUser currentUser;
    Button signUpButton;
    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mAuth = FirebaseAuth.getInstance();
        signUpButton = findViewById(R.id.welcomeSignUpButton);
        signInButton = findViewById(R.id.welcomeSignInButton);

        signInButton.setVisibility(INVISIBLE);
        signUpButton.setVisibility(INVISIBLE);

        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().reload().addOnSuccessListener( aVoid -> {
                currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                }
            });
        } else {
            signInButton.setVisibility(VISIBLE);
            signUpButton.setVisibility(VISIBLE);
        }

        signUpButton.setOnClickListener(view -> {
            Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        });

        signInButton.setOnClickListener(view -> {
            Intent signInIntent = new Intent(WelcomeActivity.this, SignInActivity.class);
            startActivityForResult(signInIntent, 9);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAuth.getCurrentUser() != null) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 9 && resultCode == RESULT_OK) {
            finish();
        }
    }
}
