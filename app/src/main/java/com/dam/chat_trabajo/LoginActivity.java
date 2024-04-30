package com.dam.chat_trabajo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText editTextemail , editTextPassword;
    Button botonlogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textview;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        editTextemail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);
        botonlogin=findViewById(R.id.btn_login);
        progressBar=findViewById(R.id.progress_bar);
        textview=findViewById(R.id.RegisterNow);
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        botonlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email,password;
                email=String.valueOf(editTextemail.getText());
                password=String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    // El campo de correo electrónico está vacío
                    // Aquí puedes mostrar un mensaje de error al usuario o realizar otra acción
                    Toast.makeText(LoginActivity.this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    // El campo de correo electrónico está vacío
                    // Aquí puedes mostrar un mensaje de error al usuario o realizar otra acción
                    Toast.makeText(LoginActivity.this, "Por favor, ingrese una contraseña", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(getApplicationContext(),"Login Sucessful",Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {

                                    Toast.makeText(LoginActivity.this, "Error Login.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

            }
        });


    }
}