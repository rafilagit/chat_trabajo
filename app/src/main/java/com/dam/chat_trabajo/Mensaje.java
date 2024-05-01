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

    // Métodos para obtener el año, mes, día, hora y minuto de la fecha y hora del mensaje
    public int getYear() {
        return Integer.parseInt(fechaHora.substring(6, 10));
    }

    public int getMonth() {
        return Integer.parseInt(fechaHora.substring(3, 5));
    }

    public int getDay() {
        return Integer.parseInt(fechaHora.substring(0, 2));
    }

    public int getHour() {
        return Integer.parseInt(fechaHora.substring(11, 13));
    }

    public int getMinute() {
        return Integer.parseInt(fechaHora.substring(14, 16));
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("nombreUsuario", nombreUsuario);
        map.put("contenidoMensaje", contenidoMensaje);
        map.put("fechaHoraOriginal", fechaHora); // Actualizado para incluir la fecha y hora original
        return map;
    }
}
