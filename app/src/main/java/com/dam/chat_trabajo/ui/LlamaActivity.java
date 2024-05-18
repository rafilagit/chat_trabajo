package com.dam.chat_trabajo.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.dam.chat_trabajo.databinding.ActivityLlamaBinding;
import com.dam.chat_trabajo.repository.MainRepository;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.permissionx.guolindev.PermissionX;
import java.util.ArrayList;
import java.util.Objects;

public class LlamaActivity extends AppCompatActivity {

    private ActivityLlamaBinding views;
    private MainRepository mainRepository;
    private String nombreSala;
    private String idSala;
    private ArrayList<String> participantesSala;
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityLlamaBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        if (intent != null) {
            nombreSala = intent.getStringExtra("nombreSala");
            idSala = intent.getStringExtra("idSala");
            participantesSala = intent.getStringArrayListExtra("participantesSala");
            nombreUsuario = intent.getStringExtra("nombreUsuario");
        }
        databaseReference.child("disponibilidad").child(Objects.requireNonNull(nombreUsuario)).setValue(true);
        init();
    }

    private void init() {
        if (nombreUsuario != null) {
            mainRepository = MainRepository.getInstance();
            PermissionX.init(this)
                    .permissions(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            // Login a Firebase aquí
                            mainRepository.login(nombreUsuario, getApplicationContext(), () -> {
                                // Pasar los datos adicionales en el intent
                                Intent intent = new Intent(LlamaActivity.this, CallActivity.class);
                                intent.putExtra("nombreSala", nombreSala);
                                intent.putExtra("idSala", idSala);
                                intent.putStringArrayListExtra("participantesSala", participantesSala);
                                intent.putExtra("nombreUsuario", nombreUsuario);
                                startActivity(intent);
                            });
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "El nombre es nulo", Toast.LENGTH_SHORT).show();
        }
    }

}
