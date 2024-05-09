package com.dam.chat_trabajo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private Button buttonEnviar;
    private ImageButton buttonAdjuntarImagen;
    private ImageButton botonScrollAbajo; // Declarar el botón de scroll hacia abajo
    private FirebaseAuth auth;
    private FirebaseUser usuario;
    private ListenerRegistration mensajesListener;
    private String nombreSala;
    private String idSala;
    private ArrayList<String> participantesSala;
    private AlertDialog alertDialog;




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
        buttonAdjuntarImagen = findViewById(R.id.botonAdjuntarImagen);
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
                enviarMensaje(nombreSala, idSala, participantesSala, null);
            }
        });

        buttonAdjuntarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraOrGallery();
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
                                    if (fechaHora != null) {
                                        Mensaje mensaje = new Mensaje(nombreUsuario, mensajeTexto, fechaHora, downloadUrl);
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
                                                        if (fechaHora != null) {
                                                            Mensaje mensaje = new Mensaje(nombre, mensajeTexto, fechaHora, downloadUrl);
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


    private void enviarMensaje(String nombreSala, String idSala, ArrayList<String> participantesSala, String downloadURL) {

        if (downloadURL == null) {   //Si no hay una imagen
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
                        Mensaje nuevoMensaje = new Mensaje(nombreUsuario, mensaje, fechaHora, null);
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
        else{
            if (!downloadURL.isEmpty()) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                if (usuario != null) {
                    String nombreUsuario = obtenerNombreUsuario(usuario.getEmail());
                    if (nombreUsuario != null) {
                        // Obtener la fecha y hora actual
                        String fechaHora = obtenerFechaHoraActual();

                        // Guardar el mensaje junto con la fecha y hora en Firestore
                        Mensaje nuevoMensaje = new Mensaje(nombreUsuario, null, fechaHora, downloadURL);
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
                            enviarMensaje(nombreSala, idSala, participantesSala, downloadUrl);

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
                            enviarMensaje(nombreSala, idSala, participantesSala, downloadUrl);
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

