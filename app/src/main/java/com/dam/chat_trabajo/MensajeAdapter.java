package com.dam.chat_trabajo;// En la clase MensajeAdapter.java

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dam.chat_trabajo.Mensaje;
import com.dam.chat_trabajo.R;

import java.util.List;

public class MensajeAdapter extends ArrayAdapter<Mensaje> {

    private Context context;
    private int resource;
    private List<Mensaje> mensajes;
    private String nombreUsuarioActual; // Nuevo campo para almacenar el nombre de usuario actual

    public MensajeAdapter(Context context, int resource, List<Mensaje> mensajes, String nombreUsuarioActual) {
        super(context, resource, mensajes);
        this.context = context;
        this.resource = resource;
        this.mensajes = mensajes;
        this.nombreUsuarioActual = nombreUsuarioActual; // Almacenar el nombre de usuario actual
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        Mensaje mensaje = mensajes.get(position);

        TextView textViewNombreUsuario = convertView.findViewById(R.id.textViewNombreUsuario);
        TextView textViewMensaje = convertView.findViewById(R.id.textViewMensaje);
        TextView textViewFechaHora = convertView.findViewById(R.id.textViewFechaHora);

        // Verificar si el mensaje es del usuario actual y reemplazar el nombre de usuario
        String nombreUsuario = mensaje.getNombreUsuario().equals(nombreUsuarioActual) ? "Tú" : mensaje.getNombreUsuario();
        textViewNombreUsuario.setText(nombreUsuario);

        textViewMensaje.setText(mensaje.getContenidoMensaje());
        textViewFechaHora.setText(mensaje.getFechaHora());

        // Cambiar el fondo del elemento de lista para los mensajes del usuario actual
        if (mensaje.getNombreUsuario().equals(nombreUsuarioActual)) {
            convertView.setBackgroundResource(R.color.celeste); // Ajusta este color según tu paleta de colores
        } else {
            convertView.setBackgroundResource(android.R.color.transparent); // Restablecer a fondo transparente para otros mensajes
        }

        return convertView;
    }
}
