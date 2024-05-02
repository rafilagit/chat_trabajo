package com.dam.chat_trabajo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.widget.AbsListView;

public class MensajesActivity extends AppCompatActivity {

    private ListView listView;
    private MensajeAdapter adapter;
    private EditText editTextMensaje;
    private Button buttonEnviar;
    private ImageButton botonScrollAbajo; // Declarar el botón de scroll hacia abajo
    private FirebaseAuth auth;
    private FirebaseUser usuario;
    private ListenerRegistration mensajesListener;
    private String nombreSala;
    private String idSala;
    private ArrayList<String> participantesSala;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensajes);

        // Recuperar los parámetros del Intent
        Intent intent = getIntent();
        if (intent != null) {
             nombreSala = intent.getStringExtra("nombreSala");
             idSala = intent.getStringExtra("idSala");
             participantesSala = intent.getStringArrayListExtra("participantesSala");

            // Ahora puedes usar estos datos como desees
            Log.d("MensajesActivity", "Nombre de la sala: " + nombreSala);
            Log.d("MensajesActivity", "ID de la sala: " + idSala);
            Log.d("MensajesActivity", "Participantes de la sala: " + participantesSala);
        }



        listView = findViewById(R.id.listView);
        editTextMensaje = findViewById(R.id.editTextMensaje);
        buttonEnviar = findViewById(R.id.buttonEnviar);
        botonScrollAbajo = findViewById(R.id.botonScrollAbajo); // Obtener referencia al botón de scroll hacia abajo
        auth = FirebaseAuth.getInstance();
        usuario = auth.getCurrentUser();

        List<Mensaje> mensajes = new ArrayList<>();
        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
        adapter = new MensajeAdapter(this, R.layout.item_mensaje, mensajes, nombreUsuario);
        listView.setAdapter(adapter);

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje(nombreSala, idSala, participantesSala);
            }
        });

        // Suscribir al listener en tiempo real para obtener mensajes
        suscribirListenerMensajes(nombreSala, idSala, participantesSala);

        // Agregar un Listener al botón de scroll hacia abajo
        botonScrollAbajo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Desplazar la lista hasta la parte inferior
                listView.smoothScrollToPosition(adapter.getCount() - 1);
            }
        });

        // Ocultar el botón de scroll hacia abajo al inicio
        botonScrollAbajo.setVisibility(View.GONE);

        // Agregar un listener de desplazamiento al ListView
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // No es necesario implementar este método, pero es obligatorio
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (listView.getChildAt(0) != null) {
                    // Verificar si el primer elemento visible es el primero en la lista
                    boolean canScrollDown = listView.getLastVisiblePosition() != listView.getAdapter().getCount() - 1 || listView.getChildAt(listView.getChildCount() - 1).getBottom() > listView.getHeight();

                    // Si se puede hacer scroll hacia abajo, mostrar el botón, de lo contrario, ocultarlo
                    botonScrollAbajo.setVisibility(canScrollDown ? View.VISIBLE : View.GONE);
                }
            }
        });


    }

    // Método para verificar si es posible hacer scroll hacia abajo
    private boolean canScrollDown(ListView listView) {
        final int lastItemPosition = listView.getLastVisiblePosition();
        final int lastVisiblePosition = listView.getChildCount() - 1;
        return lastItemPosition >= lastVisiblePosition && lastVisiblePosition > 0;
    }



    private void suscribirListenerMensajes(String nombreSala, String idSala, ArrayList<String> participantesSala) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Obtener los nombres de todos los usuarios
            // Iterar sobre cada nombre de usuario
            for (String nombre : participantesSala) {
                // Suscribirse a los cambios en la colección de mensajes de cada usuario
                db.collection("chat")
                        .document("salas")
                        .collection(idSala)
                        .document(nombreSala)
                        .collection(nombre)
                        .addSnapshotListener((queryDocumentSnapshots, error) -> {
                            if (error != null) {
                                Log.e("Error", "Error al obtener los mensajes de " + nombre, error);
                                return;
                            }

                            // Llamar al método obtenerMensajes() para actualizar la lista de mensajes
                            obtenerMensajes(nombreSala, idSala, participantesSala);
                        });
            }
    }

    private void obtenerMensajes(String nombreSala, String idSala, ArrayList<String> participantesSala) {
        if (usuario != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
            if (nombreUsuario != null) {
                List<Mensaje> todosMensajes = new ArrayList<>(); // Lista para contener todos los mensajes

                // Obtener los mensajes del usuario actual
                db.collection("chat")
                        .document("salas")
                        .collection(idSala)
                        .document(nombreSala)
                        .collection(nombreUsuario)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot mensajeDoc : task.getResult()) {
                                    String mensajeTexto = mensajeDoc.getString("contenidoMensaje");
                                    String fechaHora = mensajeDoc.getString("fechaHoraOriginal");
                                    if (mensajeTexto != null && fechaHora != null) {
                                        Mensaje mensaje = new Mensaje(nombreUsuario, mensajeTexto, fechaHora);
                                        todosMensajes.add(mensaje);
                                    }
                                }
                                // Obtener los mensajes de los otros usuarios
                                participantesSala.remove(nombreUsuario);
                                for (String nombre : participantesSala) {
                                    db.collection("chat")
                                            .document("salas")
                                            .collection(idSala)
                                            .document(nombreSala)
                                            .collection(nombre)
                                            .get()
                                            .addOnCompleteListener(otherTask -> {
                                                if (otherTask.isSuccessful()) {
                                                    for (DocumentSnapshot mensajeDoc : otherTask.getResult()) {
                                                        String mensajeTexto = mensajeDoc.getString("contenidoMensaje");
                                                        String fechaHora = mensajeDoc.getString("fechaHoraOriginal");
                                                        if (mensajeTexto != null && fechaHora != null) {
                                                            Mensaje mensaje = new Mensaje(nombre, mensajeTexto, fechaHora);
                                                            todosMensajes.add(mensaje);
                                                        }
                                                    }
                                                    // Ordenar todos los mensajes por fecha y hora
                                                    Collections.sort(todosMensajes, new Comparator<Mensaje>() {
                                                        @Override
                                                        public int compare(Mensaje m1, Mensaje m2) {
                                                            // Comparar por año
                                                            int yearComparison = Integer.compare(m1.getYear(), m2.getYear());
                                                            if (yearComparison != 0) {
                                                                return yearComparison;
                                                            }

                                                            // Comparar por mes
                                                            int monthComparison = Integer.compare(m1.getMonth(), m2.getMonth());
                                                            if (monthComparison != 0) {
                                                                return monthComparison;
                                                            }

                                                            // Comparar por día
                                                            int dayComparison = Integer.compare(m1.getDay(), m2.getDay());
                                                            if (dayComparison != 0) {
                                                                return dayComparison;
                                                            }

                                                            // Comparar por hora
                                                            int hourComparison = Integer.compare(m1.getHour(), m2.getHour());
                                                            if (hourComparison != 0) {
                                                                return hourComparison;
                                                            }

                                                            // Comparar por minuto
                                                            return Integer.compare(m1.getMinute(), m2.getMinute());
                                                        }
                                                    });
                                                    // Limpiar el adaptador y agregar los mensajes ordenados
                                                    adapter.clear();
                                                    adapter.addAll(todosMensajes);
                                                    if (canScrollDown(listView)) {
                                                        botonScrollAbajo.setVisibility(View.VISIBLE);
                                                    } else {
                                                        botonScrollAbajo.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    Log.e("Error", "Error al obtener los mensajes de otros usuarios", otherTask.getException());
                                                }
                                            });
                                }
                            } else {
                                Log.e("Error", "Error al obtener los mensajes del usuario actual", task.getException());
                            }
                        });
            }
        }
    }


    private void enviarMensaje(String nombreSala, String idSala, ArrayList<String> participantesSala) {
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
                    Mensaje nuevoMensaje = new Mensaje(nombreUsuario, mensaje, fechaHora);
                    db.collection("chat")
                            .document("salas")
                            .collection(idSala)
                            .document(nombreSala)
                            .collection(nombreUsuario)
                            .add(nuevoMensaje.toMap())
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(MensajesActivity.this, "Mensaje enviado correctamente", Toast.LENGTH_SHORT).show();
                                // Después de enviar el mensaje, actualizar la lista
                                obtenerMensajes(nombreSala, idSala, participantesSala);
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


    private String obtenerFechaHoraActual() {
        // Obtener la fecha y hora actual
        Date fechaHoraActual = new Date();

        // Formatear la fecha y hora en el formato deseado
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Devolver la fecha y hora formateadas como una cadena
        return sdf.format(fechaHoraActual);
    }
}
