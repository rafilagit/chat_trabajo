package com.dam.chat_trabajo;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        TextView tvNombreSala = convertView.findViewById(R.id.btnSala); //No es un boton es un textview se quedo el id de boton
        TextView tvParticipantes = convertView.findViewById(R.id.tvParticipantes);
        TextView tvIdSala = convertView.findViewById(R.id.tvIdSala);
        Button btnEliminarSala = convertView.findViewById(R.id.btnEliminarSala); // Botón para eliminar sala

        tvNombreSala.setText(sala.getNombre());

        // Reemplazar el nombre real del usuario por "tu" si coincide con el nombre de usuario actual
        List<String> participantes = sala.getParticipantes();
        for (int i = 0; i < participantes.size(); i++) {
            if (participantes.get(i).equals(nombreUsuario)) {
                participantes.set(i, "Tú");

            }
        }
        String participantesString = TextUtils.join(", ", participantes);
        tvParticipantes.setText("Participantes: " + participantesString);
        tvIdSala.setText("ID de la Sala: " + sala.getId());

        // Mostrar el botón de eliminar sala si el usuario actual es el administrador
        // Mostrar el botón de eliminar sala si el usuario actual es el administrador
        if (sala != null && sala.getAdmin() != null && sala.getAdmin().equals(nombreUsuario)) {
            btnEliminarSala.setVisibility(View.VISIBLE);
            btnEliminarSala.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eliminarSala(sala.getId());
                }
            });
        } else {
            btnEliminarSala.setVisibility(View.GONE);
        }

        //Gestiona los Clicks en los items de cada sala y le pasa los parametros a MensajesActivity
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MensajesActivity.class);
                intent.putExtra("nombreSala", sala.getNombre());
                intent.putExtra("idSala", sala.getId());
                intent.putStringArrayListExtra("participantesSala", new ArrayList<>(sala.getParticipantes()));
                intent.putExtra("admin", sala.getAdmin()); // Agregar el administrador como extra
                intent.putExtra("imagen", sala.getImagen()); // Agregar la imagen como extra
                getContext().startActivity(intent);
            }

        });

        return convertView;
    }
    private void eliminarSala(String idSala) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference salaRef = db.collection("chat")
                .document("salas_aux")
                .collection("salas")
                .document(idSala);

        salaRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Eliminar la sala de la lista de salas
                        for (Sala s : salas) {
                            if (s.getId().equals(idSala)) {
                                salas.remove(s);
                                break;
                            }
                        }
                        // Notificar al adaptador que los datos han cambiado
                        notifyDataSetChanged();
                        Toast.makeText(context, "Sala eliminada correctamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Error al eliminar la sala", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
    }
}


