package com.dam.chat_trabajo;

import java.util.List;

public class Sala {
    private String id;
    private String nombre;
    private List<String> participantes;

    public Sala(String id, String nombre, List<String> participantes) {
        this.id = id;
        this.nombre = nombre;
        this.participantes = participantes;
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
}
