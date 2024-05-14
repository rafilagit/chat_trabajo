package com.dam.chat_trabajo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Llamadas extends AppCompatActivity {

    private FirebaseClient firebaseClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videollamada);

        firebaseClient = new FirebaseClient(); // Crear una instancia de FirebaseClient

        // Aquí puedes inicializar cualquier otra cosa que necesites para tu actividad

        firebaseClient.writeToDatabase(); // Llamar al método writeToDatabase() de FirebaseClient
    }
}
