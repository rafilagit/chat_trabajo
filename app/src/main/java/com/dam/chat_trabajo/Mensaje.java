package com.dam.chat_trabajo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Mensaje {
    private String nombreUsuario;
    private String contenidoMensaje;
    private String fechaHora; // Cambiado a String

    public Mensaje(String nombreUsuario, String contenidoMensaje, String fechaHora) { // Ajustado el constructor
        this.nombreUsuario = nombreUsuario;
        this.contenidoMensaje = contenidoMensaje;
        this.fechaHora = fechaHora;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getContenidoMensaje() {
        return contenidoMensaje;
    }

    public String getFechaHora() {
        return fechaHora; // Retornamos la fecha y hora original
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nombreUsuario", nombreUsuario);
        map.put("contenidoMensaje", contenidoMensaje);
        map.put("fechaHoraOriginal", fechaHora); // Actualizado para incluir la fecha y hora original
        return map;
    }
}
