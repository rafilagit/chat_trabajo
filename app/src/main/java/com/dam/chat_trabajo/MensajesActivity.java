package com.dam.chat_trabajo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.widget.AbsListView;

public class MensajesActivity extends AppCompatActivity implements MensajeAdapter.OnImageMessageLongClickListener {

    private ListView listView;
    private MensajeAdapter adapter;
    private EditText editTextMensaje;
    private ImageButton buttonEnviar;
    private ImageButton botonScrollAbajo; // Declarar el botón de scroll hacia abajo
    private FirebaseAuth auth;
    private FirebaseUser usuario;
    private String nombreSala;
    private String idSala;
    private ArrayList<String> participantesSala;
    private AlertDialog alertDialog;
    private MapView mapView;
    private EditText editTextDireccion;
    private GoogleMap googleMap;
    private AutocompleteSupportFragment autocompleteFragment;
    private Marker previousMarker;
    private Address lastSelectedAddress;
    private ImageButton buttonAudio;
    Button startStop;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String AudioSavePath;
    private File audio;

    private Vibrator vibrator;
    private String Ruta;







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
        buttonAudio = findViewById(R.id.buttonAudio);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        botonScrollAbajo = findViewById(R.id.botonScrollAbajo); // Obtener referencia al botón de scroll hacia abajo
        auth = FirebaseAuth.getInstance();
        usuario = auth.getCurrentUser();
        List<Mensaje> mensajes = new ArrayList<>();
        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
        adapter = new MensajeAdapter(this, R.layout.item_mensaje, mensajes, nombreUsuario);
        listView.setAdapter(adapter);
        // Inicializar el servicio de Places
        Places.initialize(getApplicationContext(), "AIzaSyDXgkZ7WoIg0hW6X-9MReKQTozYqn3yZ_s");

        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje(nombreSala, idSala, participantesSala, null, null);
            }
        });


        buttonAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startVibration();
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        // Llamar a performClick para manejar la acción de clic
                        v.performClick();
                        return true;
                }
                return false;
            }
        });

        adapter.setOnAudioPlayClickListener(new MensajeAdapter.OnAudioPlayClickListener() {
            @Override
            public void onAudioPlayClick(int position) {
                adapter.startPlaying(position);
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


        adapter.setOnImageMessageLongClickListener(this);


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
                                    String downloadUrl = mensajeDoc.getString("imagen");
                                    String downloadUrlAudio = mensajeDoc.getString("audio");

                                    if (fechaHora != null) {
                                        Mensaje mensaje = new Mensaje(nombreUsuario, mensajeTexto, fechaHora, downloadUrl, downloadUrlAudio);
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
                                                        String downloadUrl = mensajeDoc.getString("imagen");
                                                        String downloadUrlAudio = mensajeDoc.getString("audio");
                                                        if (fechaHora != null) {
                                                            Mensaje mensaje = new Mensaje(nombre, mensajeTexto, fechaHora, downloadUrl, downloadUrlAudio);
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
                                                            int minuteComparison = Integer.compare(m1.getMinute(), m2.getMinute());
                                                            if (minuteComparison != 0) {
                                                                return minuteComparison;
                                                            }

                                                            // Comparar por segundo
                                                            return Integer.compare(m1.getSecond(), m2.getSecond());
                                                        }
                                                    });
                                                    // Iterar sobre todos los mensajes
                                                    for (Mensaje mensaje : todosMensajes) {
                                                        // Eliminar los segundos del campo de fecha y hora
                                                        String fechaHoraSinSegundos = eliminarSegundos(mensaje.getFechaHora());
                                                        mensaje.setFechaHora(fechaHoraSinSegundos);
                                                    }

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


    private void enviarMensaje(String nombreSala, String idSala, ArrayList<String> participantesSala, String downloadURL, String downloadUrlAudio) {

        if (downloadURL == null && downloadUrlAudio==null) {   //Si no hay una imagen ni hay un audio
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
                        Mensaje nuevoMensaje = new Mensaje(nombreUsuario, mensaje, fechaHora, null, null);
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
        //Si envio una imagen
        else if(downloadUrlAudio==null){
            if (!downloadURL.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
                    if (nombreUsuario != null) {
                        // Obtener la fecha y hora actual
                        String fechaHora = obtenerFechaHoraActual();

                        // Guardar el mensaje junto con la fecha y hora en Firestore
                        Mensaje nuevoMensaje = new Mensaje(nombreUsuario, null, fechaHora, downloadURL, null);
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
            } else {
                Toast.makeText(this, "Por favor, escribe un mensaje", Toast.LENGTH_SHORT).show();
            }
        }
        //Si envio una imagen
        else {
            if (!downloadUrlAudio.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
                    if (nombreUsuario != null) {
                        // Obtener la fecha y hora actual
                        String fechaHora = obtenerFechaHoraActual();
                        // Guardar el mensaje junto con la fecha y hora en Firestore
                        Mensaje nuevoMensaje = new Mensaje(nombreUsuario, null, fechaHora, null, downloadUrlAudio);
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
            } else {
                Toast.makeText(this, "Por favor, escribe un mensaje", Toast.LENGTH_SHORT).show();
            }
        }
    }






    public void openCameraOrGallery() {
        // Crear un diálogo para mostrar las opciones de cámara y galería
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una opción")
                .setItems(new CharSequence[]{"Cámara", "Galería"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                // Abrir la cámara
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                    someActivityResultLauncher.launch(takePictureIntent);
                                }
                                break;
                            case 1:
                                // Abrir la galería de imágenes
                                Intent pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
                                pickPhoto.setType("image/*"); // Solo selecciona imágenes
                                pickPhoto.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png"}); // Tipos MIME permitidos
                                someActivityResultLauncher.launch(pickPhoto);
                                break;
                        }
                    }
                });
        builder.create().show();
    }








    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            if (data.getExtras() != null && data.getExtras().get("data") != null) {
                                // La imagen fue capturada con la cámara
                                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                                // Llama al método para enviar la imagen capturada con la cámara
                                envioImagenesPrevio(imageBitmap);
                            } else {
                                // La imagen fue seleccionada desde la galería
                                Uri selectedImageUri = data.getData();
                                if (selectedImageUri != null) {
                                    // Mostrar la vista previa de la imagen utilizando la URI
                                    showImagePreviewDialog(selectedImageUri);
                                }
                            }
                        }
                    }
                }
            });


    private void showImagePreviewDialog(Uri imageUri) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_image_preview, null);
        dialogBuilder.setView(dialogView);

        ImageView imageViewPreview = dialogView.findViewById(R.id.imageViewPreview);
        Button buttonEnviar = dialogView.findViewById(R.id.buttonEnviar);
        Button buttonCancelar = dialogView.findViewById(R.id.buttonCancelar);

        // Mostrar la imagen en el ImageView
        Glide.with(MensajesActivity.this).load(imageUri).into(imageViewPreview);

        // Configurar OnClickListener para el botón "Enviar"
        buttonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Llama al método para enviar la imagen seleccionada desde la galería
                envioImagenesPrevio(imageUri);
                // Cierra el cuadro de diálogo
                alertDialog.dismiss();
            }
        });

        // Configurar OnClickListener para el botón "Cancelar"
        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cierra el cuadro de diálogo
                alertDialog.dismiss();
            }
        });

        // Crear y mostrar el cuadro de diálogo
        alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


    private void envioImagenesPrevio(Bitmap imageBitmap) {
        // Obtener el nombre de usuario y la fecha/hora actual
        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
        String fechaHora = obtenerFechaHoraActual();

        // Generar un nombre único para el archivo
        String nombreArchivo = nombreUsuario + "_" + fechaHora + "_" + System.currentTimeMillis() + ".jpg";

        // Crear una referencia al Storage de Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Crear una referencia al archivo en el Storage con el nombre único
        StorageReference imageRef = storageRef.child(nombreArchivo);

        // Convertir el Bitmap en un byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Subir el byte array al Storage de Firebase
        UploadTask uploadTask = imageRef.putBytes(imageData);

        // Manejar el resultado de la carga
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // La imagen se ha cargado correctamente
                    // Obtener la URL de descarga del archivo cargado
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Aquí puedes utilizar la URL de descarga para mostrar la imagen
                            String downloadUrl = uri.toString();
                            Log.d("URIDESCARGA", downloadUrl);
                            //Aqui llamo a enviarmensaje
                            enviarMensaje(nombreSala, idSala, participantesSala, downloadUrl, null);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Manejar cualquier error al obtener la URL de descarga
                        }
                    });
                } else {
                    // La carga de la imagen ha fallado
                    // Manejar el error
                }
            }
        });
    }




    private void envioImagenesPrevio(Uri imageUri) {

        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
        String fechaHora = obtenerFechaHoraActual();
        String nombreArchivo = obtenerNombreArchivo(imageUri);


        // Obtener una referencia al Storage de Firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Crear una referencia al archivo en el Storage con un nombre único
        String fileName = nombreUsuario + "_" + fechaHora + "_" + nombreArchivo;; // Cambia "nombre_de_tu_imagen" según prefieras
        StorageReference imageRef = storageRef.child(fileName);

        // Subir la imagen al Storage de Firebase
        UploadTask uploadTask = imageRef.putFile(imageUri);

        // Manejar el resultado de la carga
        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    // La imagen se ha cargado correctamente
                    // Obtener la URL de descarga del archivo cargado
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Aquí puedes utilizar la URL de descarga para mostrar la imagen
                            String downloadUrl = uri.toString();
                            Log.d("URIDESCARGA", downloadUrl);
                            //Aqui llamo a enviarmensaje
                            enviarMensaje(nombreSala, idSala, participantesSala, downloadUrl, null);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle any errors
                        }
                    });
                } else {
                    // La carga de la imagen ha fallado
                    // Manejar el error
                }
            }
        });
    }

    @SuppressLint("Range")
    private String obtenerNombreArchivo(Uri uri) {
        String nombreArchivo = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    // Comprobar si la columna DISPLAY_NAME existe en el cursor
                    if (cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) != -1) {
                        // La columna existe, obtener el nombre del archivo
                        nombreArchivo = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    } else {
                        // La columna no existe, utilizar el último segmento de la URI como nombre de archivo
                        nombreArchivo = obtenerNombreArchivoDesdeUri(uri);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return nombreArchivo;
    }
    private String obtenerNombreArchivoDesdeUri(Uri uri) {
        // Utilizar el último segmento de la URI como nombre de archivo
        String path = uri.getLastPathSegment();
        if (path != null) {
            return path.substring(path.lastIndexOf('/') + 1);
        }
        return null;
    }







    public void mostrarMenuFlotante(View view) {
        // Infla el diseño del menú flotante
        View popupView = getLayoutInflater().inflate(R.layout.layout_menu_flotante, null);

        // Encuentra los botones dentro del layout inflado del menú flotante
        ImageButton botonAdjuntarImagen = popupView.findViewById(R.id.botonAdjuntarImagen);
        ImageButton botonMaps = popupView.findViewById(R.id.botonMapa);

        // Agrega un OnClickListener al botón
        botonAdjuntarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraOrGallery();
            }
        });

        botonMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarMapaEnDialogo();
            }
        });

        // Crea el PopupWindow y configura la animación
        PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        // Calcula la posición en la que se mostrará el menú flotante
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0] -250;
        int y = location[1] - popupView.getHeight() -650; // Mueve el menú hacia arriba

        // Muestra el menú flotante en la posición calculada
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, x, y);

        // Configura un OnTouchListener para detectar toques fuera del menú flotante y cerrarlo
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Si el usuario toca fuera del menú flotante, ciérralo
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
    }







    private void mostrarMapaEnDialogo() {
        // Inflar el diseño del diálogo que contiene el MapView
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mapa, null);

        // Obtener una referencia al fragmento AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.places_autocomplete_fragment);
        assert autocompleteFragment != null;

        // Configurar el fragmento de autocompletado
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Realizar la búsqueda y mostrar en el mapa
                buscarDireccionYMostrarEnMapa(place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("TAG", "An error occurred: " + status);
            }
        });

        // Inicializar el MapView desde el diseño del diálogo
        mapView = dialogView.findViewById(R.id.mapView);
        mapView.onCreate(null); // No pasamos un Bundle de estado guardado

        // Obtener una referencia al botón de búsqueda
        Button botonBuscar = dialogView.findViewById(R.id.boton_buscar);

        // Configurar el ciclo de vida del MapView en el diálogo
        mapView.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(GoogleMap map) {
                googleMap = map;

                // Configurar el tipo de mapa
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                // Obtener la ubicación actual
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(MensajesActivity.this);
                fusedLocationClient.getLastLocation().addOnSuccessListener(MensajesActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Obtener las coordenadas actuales
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            // Crear un marcador personalizado con el icono azul distintivo de Google Maps
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(currentLocation);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)); // Utiliza el icono azul distintivo
                            markerOptions.title("¡Estás aquí!"); // Título del marcador

                            // Agregar el marcador al mapa
                            googleMap.addMarker(markerOptions);

                            // Ajustar el nivel de zoom para que se vea más ampliado
                            float zoomLevel = 20.0f; // Puedes ajustar este valor según lo necesites
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel));
                        } else {
                            Toast.makeText(MensajesActivity.this, "No se puede obtener la ubicación actual", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

// Configurar un listener para el clic en el mapa
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        // Recoger las coordenadas de la ubicación donde se hizo clic
                        double latitud = latLng.latitude;
                        double longitud = latLng.longitude;

                        // Eliminar el marcador anterior si existe
                        if (previousMarker != null) {
                            previousMarker.remove();
                        }

                        // Crear un marcador en la ubicación del clic
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);

                        // Agregar el marcador al mapa y guardar una referencia a él
                        previousMarker = googleMap.addMarker(markerOptions);
                        previousMarker.setTag(latLng); // Asignar la posición como etiqueta del marcador

                        // Obtener la dirección de la ubicación y mostrarla en un InfoWindow personalizado
                        Geocoder geocoder = new Geocoder(MensajesActivity.this, Locale.getDefault());

                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String direccion = address.getAddressLine(0);

                                // Crear un InfoWindow personalizado con la dirección
                                previousMarker.setSnippet(direccion);
                                previousMarker.showInfoWindow();

                                // Abrir un diálogo con la opción de enviar la ubicación o cancelar
                                AlertDialog.Builder builder = new AlertDialog.Builder(MensajesActivity.this);
                                builder.setTitle("Enviar ubicación")
                                        .setMessage("¿Desea enviar esta ubicación?\nUbicación: " + direccion) // Agregar la dirección al mensaje del diálogo
                                        .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // Aquí puedes hacer lo que necesites con la dirección, como enviarla a través de un intent
                                                // Por ejemplo, aquí puedes enviar la ubicación con latLng
                                                //enviarUbicacion(latLng);
                                            }
                                        })
                                        .setNegativeButton("Cancelar", null)
                                        .show();
                            } else {
                                // Si no se encuentra ninguna dirección, mostrar un mensaje de error
                                Toast.makeText(MensajesActivity.this, "No se encontró ninguna dirección para esta ubicación", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MensajesActivity.this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        LatLng latLng = marker.getPosition();
                        double latitud = latLng.latitude;
                        double longitud = latLng.longitude;

                        // Obtener la dirección de la ubicación y mostrarla en un InfoWindow personalizado
                        Geocoder geocoder = new Geocoder(MensajesActivity.this, Locale.getDefault());

                        try {
                            List<Address> addresses = geocoder.getFromLocation(latitud, longitud, 1);
                            if (!addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                String direccion = address.getAddressLine(0);

                                // Crear un InfoWindow personalizado con la dirección
                                marker.setSnippet(direccion);
                                marker.showInfoWindow();
                            } else {
                                // Si no se encuentra ninguna dirección, mostrar un mensaje de error
                                Toast.makeText(MensajesActivity.this, "No se encontró ninguna dirección para esta ubicación", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(MensajesActivity.this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                });

                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        // Obtener la dirección de la ubicación seleccionada
                        String direccion = marker.getSnippet();

                        // Abrir un diálogo con la opción de enviar la ubicación o cancelar
                        AlertDialog.Builder builder = new AlertDialog.Builder(MensajesActivity.this);
                        builder.setTitle("Enviar ubicación")
                                .setMessage("¿Desea enviar esta ubicación?")
                                .setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Aquí puedes hacer lo que necesites con la dirección, como enviarla a través de un intent
                                    }
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }
                });







            }
        });

        // Configurar el botón de búsqueda
        botonBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String direccion = autocompleteFragment.getView().toString();
                if (!direccion.isEmpty()) {
                    buscarDireccionYMostrarEnMapa(direccion);
                } else {
                    Toast.makeText(MensajesActivity.this, "Por favor, ingrese una dirección", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Crear un diálogo y establecer su contenido como el diseño inflado
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogView);

        // Configurar el evento OnDismissListener para el diálogo
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mapView != null) {
                    mapView.onDestroy();
                }
                // Quitar el fragmento cuando se cierra el diálogo
                getSupportFragmentManager().beginTransaction().remove(autocompleteFragment).commit();
            }
        });

        // Mostrar el diálogo
        dialog.show();
    }




    // Método para buscar una dirección y mover la cámara del mapa a esa ubicación
    private void buscarDireccionYMostrarEnMapa(String direccion) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(direccion, 1);
            assert addresses != null;
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                float zoomLevel = 20.0f; // Puedes ajustar este valor según lo necesites
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
            } else {
                Toast.makeText(this, "No se encontraron resultados para la dirección ingresada", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar la dirección", Toast.LENGTH_SHORT).show();
        }
    }











    private boolean checkPermissions(){
        int first= ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.RECORD_AUDIO);
        int second=ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.READ_MEDIA_AUDIO);

        return first == PackageManager.PERMISSION_GRANTED &&
                second==PackageManager.PERMISSION_GRANTED;
    }
    private void startRecording() {

        String nombreUsuario = obtenerNombreUsuario(usuario.getEmail()).toString();
        String fechaHora = obtenerFechaHoraActual().toString();
        fechaHora = quitarBarrasFechaYHora(fechaHora);
        if(checkPermissions()==true)
        {
            String fichero_audio=nombreUsuario+fechaHora+".mp3";
            File carpeta= getFilesDir();
            audio=new File(carpeta,fichero_audio);
            AudioSavePath=audio.getAbsolutePath();
            Toast.makeText(MensajesActivity.this, AudioSavePath, Toast.LENGTH_LONG).show();


            mediaRecorder=new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audio.getAbsolutePath());

            try {
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(MensajesActivity.this, "habla", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(MensajesActivity.this, "error", Toast.LENGTH_SHORT).show();

                throw new RuntimeException(e);

            }


        }else {
            Toast.makeText(MensajesActivity.this, "permisos", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(MensajesActivity.this, new String[]{
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_MEDIA_AUDIO
            },1);
        }

    }
    private void stopRecording() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null; // Marcar el MediaRecorder como nulo después de liberar los recursos
                Toast.makeText(MensajesActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                String nombreUsuario = obtenerNombreUsuario(usuario.getEmail()).toString();
                String fechaHora = obtenerFechaHoraActual().toString();
                fechaHora = quitarBarrasFechaYHora(fechaHora);


                AudioSavePath=audio.getAbsolutePath();


                //File archivo = new File(AudioSavePath);
                //Ruta = archivo.getParent();
                //Toast.makeText(MensajesActivity.this, Ruta, Toast.LENGTH_LONG).show();
                String fichero_audio=nombreUsuario+fechaHora+".mp3";

                // Obtener una referencia al Storage de Firebase
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();

                // Crear una referencia al archivo en el Storage con un nombre único
                //String fileName = nombreUsuario + "_" + fechaHora;;
                StorageReference audioRef = storageRef.child(fichero_audio);

                // Subir la imagen al Storage de Firebase
                UploadTask uploadTask = audioRef.putFile(Uri.fromFile(new File(AudioSavePath)));

                // Manejar el resultado de la carga
                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            // El archivo de audio se ha cargado correctamente
                            // Obtener la URL de descarga del archivo cargado
                            audioRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Aquí puedes utilizar la URL de descarga para el archivo de audio
                                    String downloadUrlAudio = uri.toString();
                                    Log.d("URL_DESCARGA_AUDIO", downloadUrlAudio);


                                    // Aquí puedes llamar a enviarMensaje
                                    enviarMensaje(nombreSala, idSala, participantesSala, null, downloadUrlAudio);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Manejar cualquier error al obtener la URL de descarga
                                }
                            });
                        } else {
                            // La carga del archivo de audio ha fallado
                            // Manejar el error
                        }
                    }
                });



            } catch (IllegalStateException e) {
                // Manejar la excepción de estado ilegal
                e.printStackTrace();
            }
        } else {
            // Manejar el caso en el que mediaRecorder es nulo
            Toast.makeText(MensajesActivity.this, "El MediaRecorder no está inicializado", Toast.LENGTH_SHORT).show();
        }
    }


/*
    private void startPlaying() {
        mediaPlayer=new MediaPlayer();
        try {
            mediaPlayer.setDataSource(AudioSavePath);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(currentPosition); // Establecer la posición de reproducción guardada
            mediaPlayer.start();
            Toast.makeText(MensajesActivity.this, "Escuchando", Toast.LENGTH_SHORT).show();
            isPlaying = true;

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // La reproducción ha finalizado, restablecer el estado de reproducción
                    isPlaying = false;
                    currentPosition = 0; // Reiniciar la posición de reproducción
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void stopPlaying() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause(); // Pausar la reproducción
            currentPosition = mediaPlayer.getCurrentPosition(); // Guardar la posición actual
            Toast.makeText(MensajesActivity.this, "Parar escuchar", Toast.LENGTH_SHORT).show();
            isPlaying = false;
        }
    }
*/
    private void startVibration() {
        // Verificar si la API de Vibrator está disponible
        if (vibrator.hasVibrator()) {
            VibrationEffect vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(vibrationEffect);
        } else {
            Toast.makeText(MensajesActivity.this, "El dispositivo no tiene capacidad de vibración", Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        // Devolver la fecha y hora formateadas como una cadena
        return sdf.format(fechaHoraActual);
    }

    private String quitarBarrasFechaYHora(String fechaHora) {
        // Reemplazar las barras por guiones bajos
        return fechaHora.replace("/", "_").replace(":", "_");
    }


    // Método para eliminar los segundos de la fecha y hora
    private String eliminarSegundos(String fechaHora) {
        // Dividir la fecha y hora en sus componentes
        String[] partes = fechaHora.split(" ");

        // Obtener la hora con minutos
        String horaConMinutos = partes[1];

        // Dividir la hora en horas, minutos y segundos
        String[] partesHora = horaConMinutos.split(":");
        String horaSinSegundos = partesHora[0] + ":" + partesHora[1]; // Hora sin los segundos

        // Reconstruir la fecha y hora sin los segundos
        return partes[0] + " " + horaSinSegundos;
    }




    @Override
    public void onImageMessageLongClick(String imageUrl) {
        // Crear un intent para mostrar la imagen flotante
        Intent intent = new Intent(this, ImagenFlotante.class);
        intent.putExtra("imageUrl", imageUrl);
        startActivity(intent);
    }
}

