package com.dam.chat_trabajo.Salas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dam.chat_trabajo.R;

import java.util.ArrayList;
import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.UsuarioViewHolder> {

    private List<String> usuarios;
    private List<String> usuariosSeleccionados;

    public UsuariosAdapter(List<String> usuarios) {
        this.usuarios = usuarios;
        usuariosSeleccionados = new ArrayList<>();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        String usuario = usuarios.get(position);
        holder.bindUsuario(usuario);
    }

    @Override
    public int getItemCount() {
        return usuarios.size();
    }

    public List<String> getUsuariosSeleccionados() {
        return usuariosSeleccionados;
    }

    public class UsuarioViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkBoxUsuario;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxUsuario = itemView.findViewById(R.id.checkBoxUsuario);
        }

        public void bindUsuario(String usuario) {
            checkBoxUsuario.setText(usuario);
            checkBoxUsuario.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        usuariosSeleccionados.add(usuario);
                    } else {
                        usuariosSeleccionados.remove(usuario);
                    }
                }
            });
        }
    }
}
