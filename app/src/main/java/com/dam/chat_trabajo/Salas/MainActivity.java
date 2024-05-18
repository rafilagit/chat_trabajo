package com.dam.chat_trabajo.Salas;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.chat_trabajo.Login.LoginActivity;
import com.dam.chat_trabajo.Mensajes.MensajesActivity;
import com.dam.chat_trabajo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser usuario;
    private FirebaseFirestore db;
    private SalasAdapter salasAdapter;
    private List<Sala> salasList;
    private ListView listViewSalas;


    @Override
    protected void onStart() {
        super.onStart();
        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
        usuario = auth.getCurrentUser();
        // Obtener la referencia a la base de datos
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        // Establecer el valor en la base de datos para el nombre de usuario como false
        databaseReference.child("disponibilidad").child(Objects.requireNonNull(nombreUsuario)).setValue(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        usuario = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");  // Establecer el título como una cadena vacía
        }
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
            assert nombreUsuario != null;
            databaseReference.child(nombreUsuario).setValue("");
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

        ImageButton botonCerrarSesion = findViewById(R.id.logout);
        botonCerrarSesion.setImageResource(R.drawable.logout_ic);
        ImageButton botonCrearSala = findViewById(R.id.crearSala);
        botonCrearSala.setImageResource(R.drawable.crearsala_ic);

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
        db.collection("chat").document("salas_aux").collection("salas").get()  //chat/salas_aux/salas
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) { //queryDocumentSnapshots.getDocuments() lista de todos los documentos de la sala
                                // Obtener datos de la sala                                           //DocumentSnapshot document variable de cada documento
                                String idSala = document.getId();
                                String nombreSala = document.getString("nombre");
                                String participantesString = (String) document.get("usuarios");
                                List<String> participantes = null;
                                String admin = document.getString("admin");
                                Long imagenLong = document.getLong("imagen");
                                int imagen = (imagenLong != null) ? imagenLong.intValue() : 0;

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
                                    // Verificar si la sala ya se ha mostrado, si se ha mostrado no se añade y por tanto no se muestra de nuevo
                                    boolean salaExistente = false;
                                    for (Sala sala : salasList) {
                                        if (sala.getId().equals(idSala)) {
                                            salaExistente = true;
                                            break;
                                        }
                                    }
                                    if (!salaExistente) {
                                        // Crear objeto Sala y añadirlo a la lista
                                        Sala sala = new Sala(idSala, nombreSala, participantes, admin, imagen);                                        salasList.add(sala);
                                    }
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

                Spinner spinnerBackgroundImages = dialogLayout.findViewById(R.id.spinnerBackgroundImages);
                // Configurar Spinner con las opciones de imágenes de fondo
                String[] nombresImagenes = {"Fondo Default", "Fondo Agua", "Fondo Árboles", "Fondo Atardecer", "Fondo Nubes", "Fondo Noche", "Fondo Burbujas"};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, nombresImagenes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerBackgroundImages.setAdapter(adapter);


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
                    String selectedBackground = (String) spinnerBackgroundImages.getSelectedItem();


                    // Añadir tu nombre de usuario a la lista
                    if (!usuariosSeleccionados.contains(nombreUsuario)) {
                        usuariosSeleccionados.add(nombreUsuario);
                    }

                    // Construir el string con los usuarios seleccionados separados por comas
                    StringBuilder usuariosString = new StringBuilder();
                    for (String usuario : usuariosSeleccionados) {
                        usuariosString.append(usuario).append(",");
                    }

                    // Generar un identificador único de 10 caracteres para la nueva sala
                    String idSala = Utils.generateUniqueID();

                    // Crear un mapa con los datos de la sala
                    Map<String, Object> sala = new HashMap<>();
                    sala.put("nombre", nombreSala);
                    sala.put("usuarios", usuariosString.toString());
                    sala.put("salaId", idSala); // Agregar el ID de la sala al mapa
                    sala.put("admin", obtenerNombreUsuario(FirebaseAuth.getInstance().getCurrentUser().getEmail())); // Obtener el nombre del usuario actual como admin
                    sala.put("imagen", obtenerImagenId(selectedBackground)); // Guardar la ID de la imagen de fondo

                    // Añadir la sala al Firestore con el identificador único como nombre de la colección
                    db.collection("chat")
                            .document("salas_aux")
                            .collection("salas")
                            .document(idSala)
                            .set(sala)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MainActivity.this, "Sala creada correctamente", Toast.LENGTH_SHORT).show();

                                // Crear colecciones dentro de la sala para cada participante
                                for (String participante : usuariosSeleccionados) {
                                    db.collection("chat")
                                            .document("salas")
                                            .collection(idSala)
                                            .document(nombreSala)
                                            .collection(participante)
                                            .add(new HashMap<>())
                                            .addOnSuccessListener(aVoid1 -> {
                                                Log.d("MainActivity", "Colección creada para participante: " + participante);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("MainActivity", "Error al crear la colección para participante: " + participante, e);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error al crear la sala: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private int obtenerImagenId(String nombreImagen) {
        int imagenId = 0; // Valor predeterminado en caso de que no se encuentre la imagen

        // Mapa que relaciona los nombres de las imágenes con sus IDs en /res/drawable
        HashMap<String, Integer> mapaImagenes = new HashMap<>();
        mapaImagenes.put("Fondo Default", R.drawable.fondo_default);
        mapaImagenes.put("Fondo Agua", R.drawable.fondo_agua);
        mapaImagenes.put("Fondo Árboles", R.drawable.fondo_arboles);
        mapaImagenes.put("Fondo Atardecer", R.drawable.fondo_atardecer);
        mapaImagenes.put("Fondo Nubes", R.drawable.fondo_nubes);
        mapaImagenes.put("Fondo Noche", R.drawable.fondo_noche);
        mapaImagenes.put("Fondo Burbujas", R.drawable.fondo_burbujas);

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
                                })
                                .addOnFailureListener(e -> {
                                    // Error al actualizar el campo de usuario
                                    Toast.makeText(MainActivity.this, "Error al actualizar el nombre de usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
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


