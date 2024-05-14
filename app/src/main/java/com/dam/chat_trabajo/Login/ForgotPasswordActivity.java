package com.dam.chat_trabajo.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dam.chat_trabajo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    ProgressBar progressBar;
    EditText mail;
    Button userPass;


    ImageView flechaVolver;

    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpass);

        progressBar=findViewById(R.id.progress_bar_rec);
        mail=findViewById(R.id.email_rec);
        userPass=findViewById(R.id.mailsend);
        flechaVolver=findViewById(R.id.botonBolver);

        firebaseAuth = FirebaseAuth.getInstance();

        flechaVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        userPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mail.getText().toString().isEmpty())
                {
                    Toast.makeText(ForgotPasswordActivity.this, "Introduzca su Email", Toast.LENGTH_SHORT).show();
                    return;

                }
                else {
                    firebaseAuth.sendPasswordResetEmail(mail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, "Revise su Email", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ForgotPasswordActivity.this, "Ha ocurrido un error", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
            }
        });



    }
}