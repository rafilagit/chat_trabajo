package com.dam.chat_trabajo.Salas;

import java.util.List;

public class Sala {
    private String id;
    private String nombre;
    private List<String> participantes;
    private String admin;

    private int imagen;

    public Sala(String id, String nombre, List<String> participantes, String admin, int imagen) {
        this.id = id;
        this.nombre = nombre;
        this.participantes = participantes;
        this.admin = admin;
        this.imagen = imagen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(List<String> participantes) {
        this.participantes = participantes;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public void setImagen(int imagen) {
        this.imagen = imagen;
    }

    public int getImagen() {
        return imagen;
    }
}


