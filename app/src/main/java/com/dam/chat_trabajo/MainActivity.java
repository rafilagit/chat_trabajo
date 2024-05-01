package com.dam.chat_trabajo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser usuario;
    private FirebaseFirestore db;
    private SalasAdapter salasAdapter;
    private List<Sala> salasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        usuario = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

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

            // Inicializar la lista de salas y el adaptador
            salasList = new ArrayList<>();
            salasAdapter = new SalasAdapter(this, salasList);

            // Configurar la ListView
            ListView listViewSalas = findViewById(R.id.listViewSalas);
            listViewSalas.setAdapter(salasAdapter);

            // Obtener y mostrar las salas existentes
            mostrarSalas();
            suscribirListenerSalas();

        }

        Button botonCerrarSesion = findViewById(R.id.logout);
        Button botonMensajes = findViewById(R.id.mensajes);
        Button botonCrearSala = findViewById(R.id.crearSala);

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

        botonCrearSala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mostrar el diálogo flotante para crear la sala
                mostrarDialogoCrearSala();
            }
        });
    }

    private void mostrarSalas() {
        db.collection("chat").document("salas_aux").collection("salas").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                String idSala = document.getId();
                                String nombreSala = document.getString("nombre");
                                String participantesString = (String) document.get("usuarios");
                                List<String> participantes = null;
                                if (participantesString != null && !participantesString.isEmpty()) {
                                    // Eliminar la última coma si existe
                                    if (participantesString.charAt(participantesString.length() - 1) == ',') {
                                        participantesString = participantesString.substring(0, participantesString.length() - 1);
                                    }
                                    participantes = Arrays.asList(participantesString.split(","));
                                    // Ahora tienes la lista de participantes sin la última coma
                                }
                                // Verificar si el usuario actual está entre los participantes
                                if (participantes != null && participantes.contains(obtenerNombreUsuario(usuario.getEmail()))) {
                                    // Crear objeto Sala y añadirlo a la lista
                                    Sala sala = new Sala(idSala, nombreSala, participantes);
                                    salasList.add(sala);
                                }
                            }
                            // Notificar al adaptador que los datos han cambiado
                            salasAdapter.notifyDataSetChanged();
                        } else {
                            Log.d("MainActivity", "No existen salas en la base de datos.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MainActivity", "Error al obtener las salas: " + e.getMessage());
                    }
                });
    }

    private void mostrarDialogoCrearSala() {
        // Verificar si usuario no es null antes de usarlo
        if (usuario != null) {
            String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
            obtenerNombresUsuarios(nombresUsuarios -> {
                Log.d("MainActivity", "Usuarios obtenidos: " + nombresUsuarios.size());

                nombresUsuarios.remove(nombreUsuario);

                // Inflar el diseño XML del diálogo flotante
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogLayout = inflater.inflate(R.layout.dialog_crear_sala, null);

                // EditText para el nombre de la sala (ya existe)
                EditText editTextNombreSala = dialogLayout.findViewById(R.id.editTextNombreSala);

                // RecyclerView para mostrar la lista de usuarios con casillas de verificación
                RecyclerView recyclerViewUsuarios = dialogLayout.findViewById(R.id.recyclerViewUsuarios);
                recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(this));

                // Adaptador para la lista de usuarios
                UsuariosAdapter usuariosAdapter = new UsuariosAdapter(nombresUsuarios);
                recyclerViewUsuarios.setAdapter(usuariosAdapter);

                // Construir el diálogo flotante (ya existe)
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(dialogLayout);
                builder.setTitle("Crear Sala");
                builder.setPositiveButton("Crear", (dialog, which) -> {
                    // Obtener el nombre de la sala
                    String nombreSala = editTextNombreSala.getText().toString().trim();

                    // Validar que se ingresó un nombre de sala
                    if (nombreSala.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Por favor, ingresa un nombre para la sala", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Obtener los usuarios seleccionados
                    List<String> usuariosSeleccionados = usuariosAdapter.getUsuariosSeleccionados();

                    // Añadir tu nombre de usuario a la lista si aún no está presente
                    if (!usuariosSeleccionados.contains(nombreUsuario)) {
                        usuariosSeleccionados.add(nombreUsuario);
                    }

                    // Construir el string con los usuarios seleccionados separados por comas
                    StringBuilder usuariosString = new StringBuilder();
                    for (String usuario : usuariosSeleccionados) {
                        usuariosString.append(usuario).append(",");
                    }

                    // Generar un identificador único de 5 caracteres para la nueva sala
                    String idSala = Utils.generateUniqueID();

                    // Crear un mapa con los datos de la sala
                    Map<String, Object> sala = new HashMap<>();
                    sala.put("nombre", nombreSala);
                    sala.put("usuarios", usuariosString.toString());
                    sala.put("salaId", idSala); // Agregar el ID de la sala al mapa

                    // Añadir la sala al Firestore con el identificador único como nombre de la colección
                    db.collection("chat")
                            .document("salas_aux")
                            .collection("salas")
                            .document(idSala)
                            .set(sala)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this, "Sala creada correctamente", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Error al crear la sala: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
                builder.setNegativeButton("Cancelar", (dialog, which) -> {
                    // Cerrar el diálogo si se presiona el botón "Cancelar"
                    dialog.cancel();
                });

                // Mostrar el diálogo flotante (ya existe)
                AlertDialog dialog = builder.create();
                dialog.show();
            });
        } else {
            Log.e("MainActivity", "El objeto usuario es null");
        }
    }

    private void obtenerNombresUsuarios(OnUsuariosObtenidosListener listener) {
        Log.d("Mio", "Obteniendo nombres de usuarios");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("messages")
                .document("usuarios")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> nombresUsuarios = new ArrayList<>(documentSnapshot.getData().keySet());
                        listener.onUsuariosObtenidos(nombresUsuarios);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Error", "Error al obtener los usuarios", e);
                });
    }

    private String obtenerNombreUsuario(String email) {
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return null;
    }


    private void suscribirListenerSalas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Suscribirse a los cambios en la colección de salas
        db.collection("chat")
                .document("salas_aux")
                .collection("salas")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("Error", "Error al obtener las salas", error);
                        return;
                    }

                    // Llamar al método mostrarSalas() para actualizar la lista de salas
                    mostrarSalas();
                });
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
                                    Toast.makeText(MainActivity.this, "Es tu primera vez en el chat?"+nombreUsuario, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar el campo de usuario
                                    Toast.makeText(MainActivity.this, "Error al actualizar el nombre de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Ya existe un campo con el nombre de usuario
                        Toast.makeText(MainActivity.this, "Bienvenido "+nombreUsuario, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Error al verificar el documento de usuarios
                    Toast.makeText(MainActivity.this, "Error al verificar el documento de usuarios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    interface OnUsuariosObtenidosListener {
        void onUsuariosObtenidos(List<String> nombresUsuarios);
    }
}
