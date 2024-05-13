package com.dam.chat_trabajo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import android.graphics.drawable.Drawable; // Agrega esta importación
import android.widget.Toast;

import com.squareup.picasso.Target; // Agrega esta importación


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MensajeAdapter extends ArrayAdapter<Mensaje> {

    private Context context;
    private int resource;
    private List<Mensaje> mensajes;
    private boolean isPlaying;
    private int currentPosition = 0;
    private MediaPlayer mediaPlayer;
    private String nombreUsuarioActual; // Nuevo campo para almacenar el nombre de usuario actual

    public MensajeAdapter(Context context, int resource, List<Mensaje> mensajes, String nombreUsuarioActual) {
        super(context, resource, mensajes);
        this.context = context;
        this.resource = resource;
        this.mensajes = mensajes;
        this.nombreUsuarioActual = nombreUsuarioActual; // Almacenar el nombre de usuario actual
    }

    // Interfaz para notificar al MensajesActivity cuando se hace long click en un mensaje con imagen
    public interface OnImageMessageLongClickListener {
        void onImageMessageLongClick(String imageUrl);
    }
    private OnImageMessageLongClickListener mOnImageMessageLongClickListener;

    public void setOnImageMessageLongClickListener(OnImageMessageLongClickListener listener) {
        mOnImageMessageLongClickListener = listener;
    }



    // Interfaz para notificar al MensajesActivity cuando se hace clic en un mensaje de ubicación
    public interface OnLocationClickListener {
        void onLocationClick(String ubicacion);
    }
    private OnLocationClickListener mOnLocationClickListener;

    public void setOnLocationClickListener(OnLocationClickListener listener) {
        mOnLocationClickListener = listener;
    }




    public interface OnAudioPlayClickListener {
        void onAudioPlayClick(int position);
    }

    private OnAudioPlayClickListener mOnAudioPlayClickListener;

    public void setOnAudioPlayClickListener(OnAudioPlayClickListener listener) {
        mOnAudioPlayClickListener = listener;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Mensaje mensaje = mensajes.get(position);

        TextView textViewNombreUsuario = convertView.findViewById(R.id.textViewNombreUsuario);
        TextView textViewMensaje = convertView.findViewById(R.id.textViewMensaje);
        TextView textViewFechaHora = convertView.findViewById(R.id.textViewFechaHora);
        ImageButton btnReproducirAudio = convertView.findViewById(R.id.buttonReproducir);
        ImageView barra = convertView.findViewById(R.id.barra);
        ImageView onda = convertView.findViewById(R.id.onda);


        MapView mapView = convertView.findViewById(R.id.mapView);


        ImageView imageViewImagen = convertView.findViewById(R.id.imageViewImagen); // Referencia al ImageView para la imagen

        // Verificar si el mensaje es del usuario actual y reemplazar el nombre de usuario
        String nombreUsuario = mensaje.getNombreUsuario().equals(nombreUsuarioActual) ? "Tú" : mensaje.getNombreUsuario();
        textViewNombreUsuario.setText(nombreUsuario);

        // Manejar el clic en el botón de reproducción de audio
        btnReproducirAudio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!isPlaying) {
                            // Si no está reproduciendo, iniciar la reproducción
                            startPlaying(position);
                            btnReproducirAudio.setImageResource(R.drawable.pausa);
                        } else {
                            // Si está reproduciendo, detener la reproducción
                            stopPlaying(position);
                            btnReproducirAudio.setImageResource(R.drawable.reproducir);
                        }
                        return true;
                }
                return false;
            }
        });

        // Verificar si el mensaje es una imagen o un mensaje de texto
        if (mensaje.getImagen() != null) {
            // Si es una imagen, ocultar el TextView del mensaje y cargar la imagen en el ImageView
            textViewMensaje.setVisibility(View.GONE);
            mapView.setVisibility(View.GONE);
            btnReproducirAudio.setVisibility(View.GONE);
            barra.setVisibility(View.GONE);
            onda.setVisibility(View.GONE);
            imageViewImagen.setVisibility(View.VISIBLE);


            // Cargar la imagen utilizando Picasso
            Picasso.get().load(mensaje.getImagen()).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // Obtener las dimensiones de la imagen
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    // Decidir si la imagen es vertical u horizontal
                    boolean isVertical = height > width;

                    // Establecer el tamaño de la imagen en consecuencia
                    if (isVertical) {
                        // Si es vertical, establecer el alto como 800 y ajustar el ancho según la relación de aspecto
                        int newWidth = 800 * width / height;
                        imageViewImagen.getLayoutParams().width = newWidth;
                        imageViewImagen.getLayoutParams().height = 800;
                        // Cargar la imagen en el ImageView con Picasso
                        Picasso.get()
                                .load(mensaje.getImagen())
                                .resize(newWidth, 800)
                                .centerInside()
                                .into(imageViewImagen);
                    } else {
                        // Si es horizontal, establecer el ancho como 800 y ajustar la altura según la relación de aspecto
                        int newHeight = 800 * height / width;
                        imageViewImagen.getLayoutParams().width = 800;
                        imageViewImagen.getLayoutParams().height = newHeight;
                        // Cargar la imagen en el ImageView con Picasso
                        Picasso.get()
                                .load(mensaje.getImagen())
                                .resize(800, newHeight)
                                .centerInside()
                                .into(imageViewImagen);
                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    // Manejar la falla en la carga de la imagen
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // No es necesario hacer nada aquí
                }
            });


        } else if(mensaje.getContenidoMensaje() != null) {
            // Si es un mensaje de texto, mostrar el TextView del mensaje y ocultar el ImageView
            mapView.setVisibility(View.GONE);
            imageViewImagen.setVisibility(View.GONE);
            btnReproducirAudio.setVisibility(View.GONE);
            onda.setVisibility(View.GONE);
            barra.setVisibility(View.GONE);
            textViewMensaje.setVisibility(View.VISIBLE);


            textViewMensaje.setText(mensaje.getContenidoMensaje());
        }

        //SI ME HA LLEGADO UNA UBICACION
        else if (mensaje.getUbicacion() != null) {
            // Ocultar el TextView del mensaje y mostrar el MapView
            textViewMensaje.setVisibility(View.GONE);
            imageViewImagen.setVisibility(View.GONE);
            btnReproducirAudio.setVisibility(View.GONE);
            mapView.setVisibility(View.VISIBLE);
            btnReproducirAudio.setVisibility(View.GONE);
            onda.setVisibility(View.GONE);
            barra.setVisibility(View.GONE);

            // Inicializar el MapView
            mapView.onCreate(null); // No pasamos un Bundle de estado guardado
            mapView.onResume(); // Necesario para que el mapa sea visible

            // Configurar el mapa
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    // Extraer la latitud y longitud de la ubicación del mensaje
                    String ubicacionString = mensaje.getUbicacion();
                    String[] ubicacionParts = ubicacionString.split(",");
                    double latitud = Double.parseDouble(ubicacionParts[0].substring(9).trim());
                    double longitud = Double.parseDouble(ubicacionParts[1].substring(10).trim());

                    // Agregar un marcador en la ubicación
                    LatLng ubicacion = new LatLng(latitud, longitud);
                    googleMap.addMarker(new MarkerOptions().position(ubicacion));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 17)); // Zoom en la ubicación
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mensaje.getUbicacion() != null && mOnLocationClickListener != null) {
                        mOnLocationClickListener.onLocationClick(mensaje.getUbicacion());
                    }
                }
            });
        }


        else {
            // Si es un mensaje de audio, ocultar el TextView del mensaje y cargar el botón de reproducción del audio
            textViewMensaje.setVisibility(View.GONE);
            imageViewImagen.setVisibility(View.GONE); // Ocultar el ImageView
            mapView.setVisibility(View.GONE);
            btnReproducirAudio.setVisibility(View.VISIBLE);
            onda.setVisibility(View.VISIBLE);
            barra.setVisibility(View.VISIBLE);


            // Mostrar el botón de reproducción de audio en su lugar
        }







        textViewFechaHora.setText(mensaje.getFechaHora());



        // Cambiar el fondo del elemento de lista para los mensajes del usuario actual
        if (mensaje.getNombreUsuario().equals(nombreUsuarioActual)) {
            convertView.setBackgroundResource(R.drawable.rounded_corner_mios);
        } else {
            convertView.setBackgroundResource(R.drawable.rounded_corner);        }


        // Agregar el OnLongClickListener al elemento de la lista
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Mensaje mensaje = getItem(position);

                // Verificar si el mensaje tiene una imagen
                if (mensaje != null && mensaje.getImagen() != null && !mensaje.getImagen().isEmpty()) {
                    // Si el mensaje tiene una imagen, notificar al MensajesActivity
                    if (mOnImageMessageLongClickListener != null) {
                        mOnImageMessageLongClickListener.onImageMessageLongClick(mensaje.getImagen());
                    }
                    return true;
                }
                return false;
            }
        });

        return convertView;


    }

    private Map<Integer, Integer> currentPositionMap = new HashMap<>();

    public void startPlaying(int position) {
        Mensaje mensaje = getItem(position);
        if (mensaje != null && mensaje.getAudio() != null) {
            String audioUrl = mensaje.getAudio();

            mediaPlayer=new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.prepare();
                // Obtener la posición de reproducción guardada asociada a esta posición del item
                int savedPosition = currentPositionMap.getOrDefault(position, 0);
                mediaPlayer.seekTo(savedPosition); // Establecer la posición de reproducción guardada
                mediaPlayer.start();
                isPlaying = true;
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // La reproducción ha finalizado, restablecer el estado de reproducción
                        isPlaying = false;
                        currentPositionMap.put(position, 0); // Reiniciar la posición de reproducción asociada a esta posición del item
                    }
                });

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public void stopPlaying(int position) {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause(); // Pausar la reproducción
            int currentPosition = mediaPlayer.getCurrentPosition(); // Obtener la posición actual
            currentPositionMap.put(position, currentPosition); // Guardar la posición actual asociada a esta posición del item
            isPlaying = false;
        }
    }
}
