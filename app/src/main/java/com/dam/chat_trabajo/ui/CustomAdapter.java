package com.dam.chat_trabajo.ui;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dam.chat_trabajo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CustomAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private Map<String, Boolean> disponibilidadMap; // Mapa para almacenar el estado de disponibilidad de los participantes
    private String nombreUsuario;
    private DatabaseReference mDatabaseReference;

    public CustomAdapter(Context context, ArrayList<String> participantes, String nombreUsuario) {
        super(context, 0, participantes);
        mContext = context;
        this.nombreUsuario = nombreUsuario;
        disponibilidadMap = new HashMap<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(); // Inicializa mDatabaseReference aquí
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(mContext).inflate(R.layout.item_usuario_llamada, parent, false);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Obtener referencias de vistas en el layout del elemento de la lista
        TextView textView = listItemView.findViewById(R.id.Usuario_Llamada);
        ImageButton noDisponibleButton = listItemView.findViewById(R.id.no_disponible);
        ImageButton disponibleButton = listItemView.findViewById(R.id.disponible);

        // Obtener el nombre del participante en esta posición
        String participante = getItem(position);


        // Si el participante no es igual al nombre de usuario y no es una cadena vacía, lo mostramos
        if (participante != null && !participante.equals(nombreUsuario) && !participante.isEmpty()) {
            // Establecer el texto del TextView
            textView.setText(participante);
        }

        DatabaseReference userStatusRef = mDatabaseReference.child("disponibilidad").child(Objects.requireNonNull(participante));
        final View finalListItemView = listItemView;

        userStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Obtener el valor del estado del participante
                Boolean disponible = dataSnapshot.getValue(Boolean.class);

                // Actualizar la interfaz de usuario según el estado del participante
                if (disponible != null && disponible) {
                    noDisponibleButton.setVisibility(View.GONE);
                    disponibleButton.setVisibility(View.VISIBLE);
                } else {
                    noDisponibleButton.setVisibility(View.VISIBLE);
                    disponibleButton.setVisibility(View.GONE);
                }

                // Si el participante es igual al nombre de usuario o es una cadena vacía, ocultarlo
                if (participante.isEmpty() || participante.equals(nombreUsuario)) {
                    finalListItemView.setVisibility(View.GONE);
                    finalListItemView.setLayoutParams(new AbsListView.LayoutParams(0, 0));
                } else {
                    finalListItemView.setVisibility(View.VISIBLE);
                    finalListItemView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar el error si se produce al leer desde la base de datos
                Log.e(TAG, "Error al obtener el estado del participante " + participante, error.toException());
            }
        });



        return listItemView;
    }
}
