package com.dam.chat_trabajo;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class SalasAdapter extends ArrayAdapter<Sala> {
    private Context context;
    private List<Sala> salas;
    private String nombreUsuario;

    public SalasAdapter(Context context, List<Sala> salas, String nombreUsuario) {
        super(context, 0, salas);
        this.context = context;
        this.salas = salas;
        this.nombreUsuario = nombreUsuario;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_sala, parent, false);
        }

        Sala sala = getItem(position);

        TextView tvNombreSala = convertView.findViewById(R.id.btnSala);
        TextView tvParticipantes = convertView.findViewById(R.id.tvParticipantes);
        TextView tvIdSala = convertView.findViewById(R.id.tvIdSala);

        tvNombreSala.setText(sala.getNombre());

        // Reemplazar el nombre real del usuario por "tu" si coincide con el nombre de usuario actual
        List<String> participantes = sala.getParticipantes();
        for (int i = 0; i < participantes.size(); i++) {
            if (participantes.get(i).equals(nombreUsuario)) {
                participantes.set(i, "tu");
            }
        }
        String participantesString = TextUtils.join(", ", participantes);
        tvParticipantes.setText("Participantes: " + participantesString);
        tvIdSala.setText("ID de la Sala: " + sala.getId());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MensajesActivity.class);
                intent.putExtra("nombreSala", sala.getNombre());
                intent.putExtra("idSala", sala.getId());
                intent.putStringArrayListExtra("participantesSala", new ArrayList<>(sala.getParticipantes()));
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
