package com.dam.chat_trabajo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SalasAdapter extends ArrayAdapter<Sala> {
    private Context context;
    private List<Sala> salas;

    public SalasAdapter(Context context, List<Sala> salas) {
        super(context, 0, salas);
        this.context = context;
        this.salas = salas;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Sala sala = salas.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sala, parent, false);
        }

        Button btnSala = convertView.findViewById(R.id.btnSala);
        TextView tvParticipantes = convertView.findViewById(R.id.tvParticipantes);
        TextView tvIdSala = convertView.findViewById(R.id.tvIdSala); // Nuevo TextView para mostrar el ID de la sala

        // Configurar el botón con el nombre de la sala
        btnSala.setText(sala.getNombre());

        // Configurar el TextView para mostrar los participantes de la sala
        tvParticipantes.setText("Participantes: " + sala.getParticipantes());

        // Mostrar el ID de la sala en el TextView correspondiente
        tvIdSala.setText("ID de la Sala: " + sala.getId());

        return convertView;
    }
}
