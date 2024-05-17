package com.dam.chat_trabajo.Salas;
import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dam.chat_trabajo.Mensajes.MensajesActivity;
import com.dam.chat_trabajo.R;
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

        TextView tvNombreSala = convertView.findViewById(R.id.btnSala);
        TextView tvParticipantes = convertView.findViewById(R.id.tvParticipantes);
        Button btnEliminarSala = convertView.findViewById(R.id.btnEliminarSala);
        View colorView = convertView.findViewById(R.id.colorView); // Obtener el View para el color de fondo

        tvNombreSala.setText(sala.getNombre());

        // Reemplazar el nombre real del usuario por "Tú" si coincide con el nombre de usuario actual
        List<String> participantes = sala.getParticipantes();
        for (int i = 0; i < participantes.size(); i++) {
            if (participantes.get(i).equals(nombreUsuario)) {
                participantes.set(i, "tu");
            }
        }
        String participantesString = TextUtils.join(", ", participantes);
        tvParticipantes.setText("Participantes: " + participantesString);

        // Establecer el color de fondo dinámicamente
        Drawable drawable = ContextCompat.getDrawable(context, sala.getImagen());

        // Establecer la imagen como fondo de la vista colorView
        if (drawable != null) {
            colorView.setBackground(drawable);
        } else {
            // Si la imagen es nula, establece el color de fondo predeterminado
            int defaultColor = ContextCompat.getColor(context, R.color.celeste);
            colorView.setBackgroundColor(defaultColor);
        }


        // Mostrar u ocultar el botón de eliminar sala según el administrador
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

        // Gestiona los clics en los elementos de cada sala
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MensajesActivity.class);
                intent.putExtra("nombreSala", sala.getNombre());
                intent.putExtra("idSala", sala.getId());
                intent.putStringArrayListExtra("participantesSala", new ArrayList<>(sala.getParticipantes()));
                intent.putExtra("admin", sala.getAdmin());
                intent.putExtra("imagen", sala.getImagen());
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


