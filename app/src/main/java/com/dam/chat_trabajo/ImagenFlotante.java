package com.dam.chat_trabajo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImagenFlotante extends AppCompatActivity {

    private ScaleGestureDetector scaleGestureDetector;
    private float scale = 1f;
    private float lastTouchX;
    private float lastTouchY;
    private float posX;
    private float posY;
    private float imageWidth;
    private float imageHeight;
    private ImageView imageView; // Definir imageView como variable de instancia

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_pulsar_imagen);

        // Obtener la URL de la imagen del Intent
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        Log.d("Flotante", "1");

        // Referenciar la ImageView en el diseño del diálogo
        imageView = findViewById(R.id.imageView); // Inicializar imageView

        // Cargar la imagen desde la URL utilizando Picasso
        Picasso.get().load(imageUrl).into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                // Guardar el tamaño original de la imagen después de cargarla
                imageWidth = imageView.getDrawable().getIntrinsicWidth();
                imageHeight = imageView.getDrawable().getIntrinsicHeight();
            }

            @Override
            public void onError(Exception e) {
                // Manejar cualquier error que ocurra durante la carga de la imagen
                Log.e("Picasso", "Error loading image", e);
            }
        });

        // Crea un ScaleGestureDetector para detectar los gestos de pellizco
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener(imageView));
        Log.d("Flotante", "2");

        // Botón para salir y volver a la actividad de mensajes
        findViewById(R.id.cruz_salir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Cierra la actividad actual
            }
        });

        // Botón para rotar la imagen hacia la derecha
        findViewById(R.id.rotar_derecha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setRotation(imageView.getRotation() + 90); // Rota la imagen 90 grados hacia la derecha
            }
        });

        // Botón para rotar la imagen hacia la izquierda
        findViewById(R.id.rotar_izquierda).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setRotation(imageView.getRotation() - 90); // Rota la imagen 90 grados hacia la izquierda
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

        // Implementa un ScaleGestureListener para detectar gestos de pellizco
        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            private final ImageView imageView;
            private float initialSpan;
            private float initialFocusX;
            private float initialFocusY;

            public ScaleListener(ImageView imageView) {
                this.imageView = imageView;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                // Guarda la distancia inicial entre los dedos
                initialSpan = detector.getCurrentSpan();
                // Guarda la posición inicial del centro de escala
                initialFocusX = detector.getFocusX();
                initialFocusY = detector.getFocusY();
                posX = imageView.getTranslationX();
                posY = imageView.getTranslationY();
                Log.d("ScaleListener", "onScaleBegin");
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // Verifica si la distancia entre los dedos ha cambiado significativamente
                float spanDifference = Math.abs(detector.getCurrentSpan() - initialSpan);
                if (spanDifference > 20) { // Ajusta este valor según sea necesario
                    // Escala la imagen solo si el cambio en la distancia entre los dedos es significativo
                    float scaleFactor = detector.getScaleFactor();
                    float newScale = scale * scaleFactor;
                    Log.d("ScaleListener", "onScale");

                    // Limita la ampliación al 200% del tamaño original de la imagen
                    float maxScale = 2.0f;
                    // Limita la desampliación al tamaño original de la imagen
                    float minScale = Math.min((float) imageView.getWidth() / imageWidth, (float) imageView.getHeight() / imageHeight);
                    if (newScale <= maxScale && newScale * imageWidth >= imageView.getWidth() && newScale * imageHeight >= imageView.getHeight()) {
                        scale = newScale;

                        // Calcula el desplazamiento del centro de escala
                        float focusShiftX = (initialFocusX - imageView.getPivotX()) * (1 - 1 / scaleFactor);
                        float focusShiftY = (initialFocusY - imageView.getPivotY()) * (1 - 1 / scaleFactor);

                        // Aplica la escala y el desplazamiento del centro de escala
                        imageView.setScaleX(scale);
                        imageView.setScaleY(scale);
                        imageView.setPivotX(initialFocusX + focusShiftX);
                        imageView.setPivotY(initialFocusY + focusShiftY);
                    } else if (newScale < minScale) {
                        // Limita la desampliación al tamaño original de la imagen
                        scale = minScale;
                        imageView.setScaleX(scale);
                        imageView.setScaleY(scale);
                    }
                }
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // Realiza alguna acción cuando termina el gesto de pellizco
                Log.d("ScaleListener", "onScaleEnd");

            }
        }
    }