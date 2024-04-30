package com.dam.chat_trabajo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        usuario = auth.getCurrentUser();

        if (usuario == null) {
            // Si el usuario no está autenticado, redirigir a la pantalla de inicio de sesión
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Obtener el nombre de usuario a partir del correo electrónico
            String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());

            // Crear un documento para el usuario en la colección "usuarios"
            crearUsuarioDDBB(nombreUsuario);
        }

        Button botonCerrarSesion = findViewById(R.id.logout);
        Button botonMensajes = findViewById(R.id.mensajes);

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        botonMensajes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MensajesActivity.class);
                startActivity(intent);
            }
        });
    }

    private String obtenerNombreUsuario(String email) {
        // Obtener el nombre de usuario a partir del correo electrónico
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return null;
    }

    private void crearUsuarioDDBB(String nombreUsuario) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usuarioDocRef = db.collection("messages").document("usuarios");

        // Verificar si ya existe un campo con el nombre de usuario
        usuarioDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains(nombreUsuario)) {
                        // El documento no existe o no contiene el nombre de usuario, actualizar el campo
                        usuarioDocRef
                                .update(nombreUsuario, true)
                                .addOnSuccessListener(aVoid -> {
                                    // Campo de usuario actualizado exitosamente
                                    Toast.makeText(MainActivity.this, "Nombre de usuario Añadido", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar el campo de usuario
                                    Toast.makeText(MainActivity.this, "Error al actualizar el nombre de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Ya existe un campo con el nombre de usuario
                        Toast.makeText(MainActivity.this, "El nombre de usuario ya existe", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al verificar el documento de usuarios
                    Toast.makeText(MainActivity.this, "Error al verificar el documento de usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}

