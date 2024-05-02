
package com.dam.chat_trabajo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    TextInputEditText editTextemail , editTextPassword,editTextPassword2;
    Button botonreg;
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
        setContentView(R.layout.activity_register);
        mAuth=FirebaseAuth.getInstance();
        editTextemail=findViewById(R.id.email);
        editTextPassword=findViewById(R.id.password);
        editTextPassword2=findViewById(R.id.password2);
        botonreg=findViewById(R.id.btn_register);
        progressBar=findViewById(R.id.progress_bar);
        textview=findViewById(R.id.loginNow);


        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


        botonreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String email,password,password2;
                email=String.valueOf(editTextemail.getText());
                password=String.valueOf(editTextPassword.getText());
                password2=String.valueOf(editTextPassword2.getText());

                if (TextUtils.isEmpty(email)) {
                    // El campo de correo electrónico está vacío
                    // Aquí puedes mostrar un mensaje de error al usuario o realizar otra acción
                    Toast.makeText(RegisterActivity.this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(password)||TextUtils.isEmpty(password2)) {
                    // El campo de correo electrónico está vacío
                    // Aquí puedes mostrar un mensaje de error al usuario o realizar otra acción
                    Toast.makeText(RegisterActivity.this, "Por favor, ingrese una contraseña en los 2 apartados", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!password.equals(password2)) {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no son iguales, por favor introduzca la contraseña correctamente", Toast.LENGTH_LONG).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Account created.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });



            }
        });





    }
}
