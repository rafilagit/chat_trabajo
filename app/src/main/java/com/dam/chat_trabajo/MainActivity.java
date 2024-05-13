package com.dam.chat_trabajo;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser usuario;
    private FirebaseFirestore db;
    private SalasAdapter salasAdapter;
    private List<Sala> salasList;
    private ListView listViewSalas;

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

            // Crear un campo en el documento usuarios de la coleccion chat--> chat/usuarios
            crearUsuarioDDBB(nombreUsuario);

            // Inicializar la lista de salas y el adaptador
            salasList = new ArrayList<>();
            salasAdapter = new SalasAdapter(this, salasList, nombreUsuario);

            // Configurar la ListView
            listViewSalas = findViewById(R.id.listViewSalas);
            listViewSalas.setAdapter(salasAdapter);

            // Configurar el OnItemClickListener
            listViewSalas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Obtener los datos de la sala seleccionada
                    Sala salaSeleccionada = salasList.get(position);
                    Log.d("sala", "Sala seleccionada: " + salaSeleccionada.getNombre());

                    // Crear un intent para la actividad de mensajes
                    Intent intent = new Intent(MainActivity.this, MensajesActivity.class);

                    // Agregar parámetros extra al intent
                    intent.putExtra("nombreSala", salaSeleccionada.getNombre());
                    intent.putExtra("idSala", salaSeleccionada.getId());
                    intent.putExtra("imagen", salaSeleccionada.getImagen());
                    intent.putStringArrayListExtra("participantesSala", new ArrayList<>(salaSeleccionada.getParticipantes()));

                    // Iniciar la actividad de mensajes
                    startActivity(intent);
                    Log.d("sala", "Iniciando MensajesActivity...");
                }
            });

            // Obtener y mostrar las salas existentes
            mostrarSalas();
            suscribirListenerSalas();
        }

        Button botonCerrarSesion = findViewById(R.id.logout);
        Button botonCrearSala = findViewById(R.id.crearSala);
        //Button botonEliminarSala = findViewById(R.id.boEliminarSala);

        botonCerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
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
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    salasList.clear(); // Limpiar la lista existente antes de agregar las nuevas salas
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        // Obtener datos de la sala
                        String idSala = document.getId();
                        String nombreSala = document.getString("nombre");
                        String participantesString = document.getString("usuarios");
                        String admin = document.getString("admin");
                        Long imagenLong = document.getLong("imagen");
                        int imagen = (imagenLong != null) ? imagenLong.intValue() : 0;

                        List<String> participantes = new ArrayList<>();
                        if (participantesString != null && !participantesString.isEmpty()) {
                            participantes = Arrays.asList(participantesString.split(","));
                        }

                        // Crear objeto Sala y añadirlo a la lista existente
                        Sala sala = new Sala(idSala, nombreSala, participantes, admin, imagen);
                        salasList.add(sala);
                    }
                    // Notificar al adaptador que los datos han cambiado
                    salasAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al obtener las salas: " + e.getMessage());
                });
    }


    private void mostrarDialogoCrearSala() {
        if (usuario != null) {
            String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
            obtenerNombresUsuarios(nombresUsuarios -> {
                nombresUsuarios.remove(nombreUsuario);

                // Inflar el layout del diálogo
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogLayout = inflater.inflate(R.layout.dialog_crear_sala, null);

                // Obtener referencias a las vistas del diálogo
                EditText editTextNombreSala = dialogLayout.findViewById(R.id.editTextNombreSala);
                RecyclerView recyclerViewUsuarios = dialogLayout.findViewById(R.id.recyclerViewUsuarios);
                recyclerViewUsuarios.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                // Configurar RecyclerView para mostrar la lista de usuarios
                UsuariosAdapter usuariosAdapter = new UsuariosAdapter(nombresUsuarios);
                recyclerViewUsuarios.setAdapter(usuariosAdapter);

                Spinner spinnerBackgroundImages = dialogLayout.findViewById(R.id.spinnerBackgroundImages);
                // Configurar Spinner con las opciones de imágenes de fondo
                String[] nombresImagenes = {"Fondo Default", "Fondo Agua y Luz", "Fondo Árboles", "Fondo Atardecer", "Fondo Nubes Lilas", "Fondo Neón"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, nombresImagenes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBackgroundImages.setAdapter(adapter);

               /* spinnerBackgroundImages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedImageName = (String) parent.getItemAtPosition(position);
                        actualizarImagenDeFondoChat(selectedImageName);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // Manejar el caso en el que no se haya seleccionado nada (opcional)
                    }
                });
                */


                // Construir el diálogo de creación de sala
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialogLayout)
                        .setTitle("Crear Sala")
                        .setPositiveButton("Crear", (dialog, which) -> {
                            String selectedBackground = (String) spinnerBackgroundImages.getSelectedItem();
                            String nombreSala = editTextNombreSala.getText().toString().trim();
                            List<String> usuariosSeleccionados = usuariosAdapter.getUsuariosSeleccionados();

                            if (nombreSala.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Por favor, ingresa un nombre para la sala", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Verificar si la sala ya existe en la lista local
                            boolean salaExistente = false;
                            for (Sala existingSala : salasList) {
                                if (existingSala.getNombre().equals(nombreSala)) {
                                    salaExistente = true;
                                    break;
                                }
                            }

                            if (salaExistente) {
                                Toast.makeText(MainActivity.this, "La sala ya existe", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!usuariosSeleccionados.contains(nombreUsuario)) {
                                    usuariosSeleccionados.add(nombreUsuario);
                                }

                                StringBuilder usuariosString = new StringBuilder();
                                for (String usuario : usuariosSeleccionados) {
                                    usuariosString.append(usuario).append(",");
                                }

                                String idSala = Utils.generateUniqueID();
                                guardarSala(nombreSala, idSala, selectedBackground, usuariosSeleccionados);
                                mostrarSalas();
                            }
                        })
                        .show(); // Mostrar el diálogo después de configurarlo

            });
        } else {
            Log.e("MainActivity", "El objeto usuario es null");
        }
    }







    private void guardarSala(String nombreSala, String idSala, String imagen, List<String> usuariosSeleccionados) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Verificar si la sala ya existe localmente
        boolean salaExistente = false;
        for (Sala existingSala : salasList) {
            if (existingSala.getId().equals(idSala)) {
                salaExistente = true;
                break;
            }
        }

        if (!salaExistente) {
            // Crear un mapa con los datos de la sala a guardar en Firestore
            Map<String, Object> salaMapa = new HashMap<>();
            salaMapa.put("nombre", nombreSala);
            salaMapa.put("usuarios", usuariosSeleccionados.toString()); // Convertir lista a String separada por comas
            salaMapa.put("salaId", idSala);
            salaMapa.put("admin", obtenerNombreUsuario(FirebaseAuth.getInstance().getCurrentUser().getEmail())); // Obtener el nombre del usuario actual como admin
            salaMapa.put("imagen", obtenerImagenId(imagen)); // Guardar la ID de la imagen de fondo

            // Guardar la sala en Firestore
            db.collection("chat")
                    .document("salas_aux")
                    .collection("salas")
                    .document(idSala)
                    .set(salaMapa)
                    .addOnSuccessListener(aVoid -> {
                        // Mostrar mensaje de éxito
                        Toast.makeText(MainActivity.this, "Sala creada correctamente", Toast.LENGTH_SHORT).show();
                        // Agregar la sala a la lista local solo si no existe
                        Sala sala = new Sala(idSala, nombreSala, usuariosSeleccionados, obtenerNombreUsuario(FirebaseAuth.getInstance().getCurrentUser().getEmail()), obtenerImagenId(imagen));
                        salasList.add(sala);

                        // Notificar al adaptador que los datos han cambiado
                        //salasAdapter.notifyDataSetChanged();
                        //suscribirListenerSalas();

                    })
                    .addOnFailureListener(e -> {
                        // Mostrar mensaje de error en caso de falla
                        Toast.makeText(MainActivity.this, "Error al crear la sala: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(MainActivity.this, "La sala ya existe localmente", Toast.LENGTH_SHORT).show();
        }
    }

    private int obtenerImagenId(String nombreImagen) {
        int imagenId = 0; // Valor predeterminado en caso de que no se encuentre la imagen

        // Mapa que relaciona los nombres de las imágenes con sus IDs en /res/drawable
        HashMap<String, Integer> mapaImagenes = new HashMap<>();
        mapaImagenes.put("Fondo Default", R.drawable.fondo_default);
        mapaImagenes.put("Fondo Agua y Luz", R.drawable.fondo_agua_luz);
        mapaImagenes.put("Fondo Árboles", R.drawable.fondo_arboles);
        mapaImagenes.put("Fondo Atardecer", R.drawable.fondo_atardecer);
        mapaImagenes.put("Fondo Nubes Lilas", R.drawable.fondo_nubes_lilas);
        mapaImagenes.put("Fondo Neón", R.drawable.fondo_neon);

        // Verificar si el nombre de la imagen existe en el mapa
        if (mapaImagenes.containsKey(nombreImagen)) {
            imagenId = mapaImagenes.get(nombreImagen);
        } else {
            // Si el nombre de la imagen no se encuentra, podemos manejarlo según las necesidades
            // Por ejemplo, mostrar una imagen predeterminada o lanzar una excepción
            // También puedes retornar un valor predeterminado o null
            // Aquí, se asigna 0 como valor predeterminado si no se encuentra la imagen
            // Pero podemos modificar esto según se necesite en la aplicación
            Log.e("MainActivity", "Nombre de imagen no válido: " + nombreImagen);
        }

        return imagenId;
    }




    private void obtenerNombresUsuarios(OnUsuariosObtenidosListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chat")  //chat/usuarios, todos los campos y te los mete en una Lista
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


    private Set<String> salaIdsSet = new HashSet<>();

    private void suscribirListenerSalas() {
        db.collection("chat")
                .document("salas_aux")
                .collection("salas")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("MainActivity", "Error al escuchar las salas", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        for (DocumentChange change : queryDocumentSnapshots.getDocumentChanges()) {
                            DocumentSnapshot document = change.getDocument();
                            String idSala = document.getId();

                            switch (change.getType()) {
                                case ADDED:
                                    if (!salaIdsSet.contains(idSala)) {
                                        // Sala no existe en el conjunto, procesarla
                                        String nombreSala = document.getString("nombre");
                                        String participantesString = document.getString("usuarios");
                                        String admin = document.getString("admin");
                                        Long imagenLong = document.getLong("imagen");
                                        int imagen = (imagenLong != null) ? imagenLong.intValue() : 0;

                                        List<String> participantes = new ArrayList<>();
                                        if (participantesString != null && !participantesString.isEmpty()) {
                                            participantes = Arrays.asList(participantesString.split(","));
                                        }

                                        // Agregar la ID al conjunto
                                        salaIdsSet.add(idSala);

                                        // Crear objeto Sala y agregarlo a la lista solo si no existe
                                        boolean salaExistente = false;
                                        for (Sala sala : salasList) {
                                            if (sala.getId().equals(idSala)) {
                                                salaExistente = true;
                                                break;
                                            }
                                        }
                                        if (!salaExistente) {
                                            Sala nuevaSala = new Sala(idSala, nombreSala, participantes, admin, imagen);
                                            salasList.add(nuevaSala);
                                        }
                                    }
                                    break;

                                case REMOVED:
                                    // Eliminar la sala del conjunto y de la lista
                                    salaIdsSet.remove(idSala);
                                    salasList.removeIf(sala -> sala.getId().equals(idSala));
                                    break;

                                // Otros casos (MODIFIED)
                            }
                        }

                        // Notificar al adaptador que los datos han cambiado
                        salasAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void crearUsuarioDDBB(String nombreUsuario) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference usuarioDocRef = db.collection("chat").document("usuarios");

        // Verificar si ya existe un campo con el nombre de usuario
        usuarioDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists() || !documentSnapshot.contains(nombreUsuario)) {
                        // El documento no existe o no contiene el nombre de usuario, actualizar el campo
                        usuarioDocRef
                                .update(nombreUsuario, true)
                                .addOnSuccessListener(aVoid -> {
                                    // Campo de usuario actualizado exitosamente
                                    Toast.makeText(MainActivity.this, "¡Es tu primera vez en el chat! Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar el campo de usuario
                                    Toast.makeText(MainActivity.this, "Error al actualizar el nombre de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Ya existe un campo con el nombre de usuario
                        Toast.makeText(MainActivity.this, "Bienvenido " + nombreUsuario, Toast.LENGTH_SHORT).show();
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





