package com.dam.chat_trabajo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 123;


    TextInputEditText editTextemail , editTextPassword;
    Button botonlogin;

    TextView forget;
    FirebaseAuth mAuth;

    GoogleSignInClient googleSignInClient;
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode()==RESULT_OK){
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try{
                    GoogleSignInAccount signInAccount=accountTask.getResult(ApiException.class);
                    AuthCredential authCredential= GoogleAuthProvider.getCredential(signInAccount.getIdToken(),null);
                    mAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                mAuth = FirebaseAuth.getInstance();
                                Toast.makeText(LoginActivity.this,"Sesion Iniciada",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Toast.makeText(LoginActivity.this,"Error Al iniciar con Google",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }catch(ApiException e ){
                    e.printStackTrace();
                }
            }
        }
    });
    private GoogleSignInClient mGoogleSignInClient;

    ProgressBar progressBar;
    TextView textview;

    SignInButton botongoogle;


    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }
    private boolean checkPermissions() {
        int vibrate=ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE);
        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionContacts = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO);
        int permissionVideo = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionImages = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES);
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionRecordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO); // Verificar permiso del micrófono

        return permissionLocation == PackageManager.PERMISSION_GRANTED &&
                vibrate==PackageManager.PERMISSION_GRANTED &&
                permissionContacts == PackageManager.PERMISSION_GRANTED &&
                permissionStorage == PackageManager.PERMISSION_GRANTED &&
                permissionCamera == PackageManager.PERMISSION_GRANTED &&
                permissionAudio == PackageManager.PERMISSION_GRANTED &&
                permissionVideo == PackageManager.PERMISSION_GRANTED &&
                permissionImages== PackageManager.PERMISSION_GRANTED &&
                permissionRecordAudio == PackageManager.PERMISSION_GRANTED &&
                permissionWriteStorage == PackageManager.PERMISSION_GRANTED;
        }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Verificar si se otorgaron los permisos
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado
                // Puedes iniciar tu lógica aquí
            } else {
                // Permiso denegado
                // Puedes mostrar un mensaje al usuario o tomar alguna otra acción
                Toast.makeText(this, "Se necesitan permisos para la aplicación.", Toast.LENGTH_SHORT).show();
            }
        }
    }


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
        mAuth = FirebaseAuth.getInstance();
        //
        GoogleSignInOptions options=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.cliente_is))
                .requestEmail()
                .build();
        googleSignInClient=GoogleSignIn.getClient(LoginActivity.this,options);
        mAuth=FirebaseAuth.getInstance();
        //

        editTextemail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        botonlogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        textview = findViewById(R.id.RegisterNow);
        forget = findViewById(R.id.passwordForget);
        botongoogle=findViewById(R.id.btn_login_Google);
        // Verificar si los permisos están otorgados
        if (checkPermissions()) {
            // Los permisos ya están otorgados
            // Puedes iniciar tu lógica aquí
        } else {
            // Solicitar permisos al usuario
            requestPermissions();
            //
            botongoogle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=googleSignInClient.getSignInIntent();
                    activityResultLauncher.launch(intent);
                }
            });
            textview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            forget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            botonlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    progressBar.setVisibility(View.VISIBLE);
                    String email, password;
                    email = String.valueOf(editTextemail.getText());
                    password = String.valueOf(editTextPassword.getText());

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
                                Toast.makeText(getApplicationContext(), "Login Sucessful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
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
}