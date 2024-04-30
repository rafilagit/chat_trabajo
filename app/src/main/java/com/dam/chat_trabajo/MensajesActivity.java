package com.dam.chat_trabajo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dam.chat_trabajo.Mensaje;
import com.dam.chat_trabajo.MensajeAdapter;
import com.dam.chat_trabajo.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MensajesActivity extends AppCompatActivity {

    private ListView listView;
    private MensajeAdapter adapter;
    private EditText editTextMensaje;
    private Button buttonEnviar;
    private FirebaseAuth auth;
    private FirebaseUser usuario;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);
        obtenerMensajes();
        listView = findViewById(R.id.listView);
        editTextMensaje = findViewById(R.id.editTextMensaje);
        buttonEnviar = findViewById(R.id.buttonEnviar);
        auth = FirebaseAuth.getInstance();
        usuario = auth.getCurrentUser();

        List<Mensaje> mensajes = new ArrayList<>();
        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
        adapter = new MensajeAdapter(this, R.layout.item_mensaje, mensajes, nombreUsuario);
        listView.setAdapter(adapter);

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje();
            }
        });

    }

    private void obtenerMensajes() {
        if (usuario != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
            if (nombreUsuario != null) {
                // Obtener los mensajes del usuario actual
                db.collection("messages")
                        .document("Mensajes")
                        .collection(nombreUsuario)
                        .orderBy("fechaHora", Query.Direction.ASCENDING)
                        .addSnapshotListener((queryDocumentSnapshots, e) -> {
                            if (e != null) {
                                Log.e("Error", "Error al obtener los mensajes del usuario actual", e);
                                return;
                            }

                            List<Mensaje> mensajesUsuario = new ArrayList<>();
                            for (DocumentSnapshot mensajeDoc : queryDocumentSnapshots) {
                                String mensajeTexto = mensajeDoc.getString("mensaje");
                                String fechaHora = mensajeDoc.getString("fechaHora");
                                if (mensajeTexto != null && fechaHora != null) {
                                    Mensaje mensaje = new Mensaje(nombreUsuario, mensajeTexto, fechaHora);
                                    mensajesUsuario.add(mensaje);
                                }
                            }
                            adapter.clear();
                            adapter.addAll(mensajesUsuario);
                        });

                // Obtener los mensajes de los otros usuarios
                obtenerNombresUsuarios(nombresUsuarios -> {
                    nombresUsuarios.remove(nombreUsuario);
                    for (String nombre : nombresUsuarios) {
                        db.collection("messages")
                                .document("Mensajes")
                                .collection(nombre)
                                .orderBy("fechaHora", Query.Direction.ASCENDING)
                                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                                    if (e != null) {
                                        Log.e("Error", "Error al obtener los mensajes de otros usuarios", e);
                                        return;
                                    }

                                    List<Mensaje> mensajesOtrosUsuarios = new ArrayList<>();
                                    for (DocumentSnapshot mensajeDoc : queryDocumentSnapshots) {
                                        String mensajeTexto = mensajeDoc.getString("mensaje");
                                        String fechaHora = mensajeDoc.getString("fechaHora");
                                        if (mensajeTexto != null && fechaHora != null) {
                                            Mensaje mensaje = new Mensaje(nombre, mensajeTexto, fechaHora);
                                            mensajesOtrosUsuarios.add(mensaje);
                                        }
                                    }
                                    adapter.addAll(mensajesOtrosUsuarios);
                                });
                    }
                });
            }
        }
    }

    private void enviarMensaje() {
        String mensaje = editTextMensaje.getText().toString().trim();
        if (!mensaje.isEmpty()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
            if (usuario != null) {
                String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
                if (nombreUsuario != null) {
                    // Obtener la fecha y hora actual
                    String fechaHora = obtenerFechaHoraActual();

                    // Guardar el mensaje junto con la fecha y hora en Firestore
                    Map<String, Object> mensajeMap = new HashMap<>();
                    mensajeMap.put("mensaje", mensaje);
                    mensajeMap.put("fechaHora", fechaHora);

                    // Añadir el mensaje al documento correspondiente del usuario actual
                    db.collection("messages")
                            .document("Mensajes")
                            .collection(nombreUsuario)
                            .add(mensajeMap)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(MensajesActivity.this, "Mensaje enviado correctamente", Toast.LENGTH_SHORT).show();
                                // Después de enviar el mensaje, obtener y mostrar todos los mensajes
                                obtenerMensajes();
                            })
                            .addOnFailureListener(e -> Toast.makeText(MensajesActivity.this, "Error al enviar el mensaje: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
            // Limpiar el campo de texto después de enviar el mensaje
            editTextMensaje.setText("");
        } else {
            Toast.makeText(this, "Por favor, escribe un mensaje", Toast.LENGTH_SHORT).show();
        }
    }


    private String obtenerNombreUsuario(String email) {
        if (email != null && email.contains("@")) {
            return email.split("@")[0];
        }
        return null;
    }

    private void obtenerNombresUsuarios(OnUsuariosObtenidosListener listener) {
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

    interface OnUsuariosObtenidosListener {
        void onUsuariosObtenidos(List<String> nombresUsuarios);
    }


    private String obtenerFechaHoraActual() {
        // Obtener la fecha y hora actual
        Date fechaHoraActual = new Date();

        // Formatear la fecha y hora en el formato deseado
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Devolver la fecha y hora formateadas como una cadena
        return sdf.format(fechaHoraActual);
    }

}
