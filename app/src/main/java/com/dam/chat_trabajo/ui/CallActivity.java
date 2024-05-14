package com.dam.chat_trabajo.ui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dam.chat_trabajo.Mensajes.MensajesActivity;
import com.dam.chat_trabajo.R;
import com.dam.chat_trabajo.databinding.ActivityCallBinding;
import com.dam.chat_trabajo.repository.MainRepository;
import com.dam.chat_trabajo.utils.DataModelType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.activity.OnBackPressedCallback;

import java.util.ArrayList;
import java.util.Objects;

public class CallActivity extends AppCompatActivity implements MainRepository.Listener {

    private ActivityCallBinding views;
    private MainRepository mainRepository;
    private Boolean isCameraMuted = false;
    private Boolean isMicrophoneMuted = false;
    private String nombreSala;
    private String idSala;
    private ArrayList<String> participantesSala;
    private String nombreUsuario;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        views = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(views.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        iniciarListenerBaseDatos();

        Intent intent = getIntent();
        if (intent != null) {
            nombreSala = intent.getStringExtra("nombreSala");
            idSala = intent.getStringExtra("idSala");
            participantesSala = intent.getStringArrayListExtra("participantesSala");
            nombreUsuario = intent.getStringExtra("nombreUsuario");
        }

        init();
        mostrarListaParticipantes(participantesSala, nombreUsuario);

        // Manejo del botón de retroceso
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Iniciar la actividad de MensajesActivity cuando se presione el botón de retroceso
                Intent intent = new Intent(CallActivity.this, MensajesActivity.class);
                intent.putExtra("nombreSala", nombreSala);
                intent.putExtra("idSala", idSala);
                intent.putStringArrayListExtra("participantesSala", participantesSala);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void init() {
        mainRepository = MainRepository.getInstance();
        views.callBtn.setOnClickListener(v -> {
            mainRepository.sendCallRequest(views.targetUserNameEt.getText().toString(), () -> {
                Toast.makeText(this, "couldnt find the target", Toast.LENGTH_SHORT).show();
            });

        });
        mainRepository.initLocalView(views.localView);
        mainRepository.initRemoteView(views.remoteView);
        mainRepository.listener = this;

        mainRepository.subscribeForLatestEvent(data -> {
            if (data.getType() == DataModelType.StartCall) {
                runOnUiThread(() -> {
                    views.incomingNameTV.setText(data.getSender() + " Te está llamando");
                    views.incomingCallLayout.setVisibility(View.VISIBLE);
                    views.acceptButton.setOnClickListener(v -> {
                        mainRepository.startCall(data.getSender());
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                    views.rejectButton.setOnClickListener(v -> {
                        views.incomingCallLayout.setVisibility(View.GONE);
                    });
                });
            }
        });

        views.switchCameraButton.setOnClickListener(v -> {
            mainRepository.switchCamera();
        });

        views.micButton.setOnClickListener(v -> {
            if (isMicrophoneMuted) {
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_off_24);
            } else {
                views.micButton.setImageResource(R.drawable.ic_baseline_mic_24);
            }
            mainRepository.toggleAudio(isMicrophoneMuted);
            isMicrophoneMuted = !isMicrophoneMuted;
        });

        views.videoButton.setOnClickListener(v -> {
            if (isCameraMuted) {
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24);
            } else {
                views.videoButton.setImageResource(R.drawable.ic_baseline_videocam_24);
            }
            mainRepository.toggleVideo(isCameraMuted);
            isCameraMuted = !isCameraMuted;
        });

        views.endCallButton.setOnClickListener(v -> {
            mainRepository.endCall();
            finish();
        });
    }

    @Override
    public void webrtcConnected() {
        runOnUiThread(() -> {
            views.incomingCallLayout.setVisibility(View.GONE);
            views.whoToCallLayout.setVisibility(View.GONE);
            views.callLayout.setVisibility(View.VISIBLE);
        });
    }





    @Override
    public void webrtcClosed() {
        runOnUiThread(() -> {
            Intent intent = new Intent(CallActivity.this, CallActivity.class);
            intent.putExtra("nombreSala", nombreSala);
            intent.putExtra("idSala", idSala);
            intent.putExtra("nombreUsuario", nombreUsuario);
            intent.putStringArrayListExtra("participantesSala", participantesSala);
            startActivity(intent);
            finish();
        });
    }

    private void mostrarListaParticipantes(ArrayList<String> participantesSala, String nombreUsuario) {
        if (participantesSala != null && nombreUsuario != null && !nombreUsuario.isEmpty()) {
            ArrayList<String> participantesFiltrados = new ArrayList<>(participantesSala);
            participantesFiltrados.remove(nombreUsuario);
            participantesFiltrados.remove("tu");

            CustomAdapter adapter = new CustomAdapter(this, participantesFiltrados, nombreUsuario);
            views.listViewLlamada.setAdapter(adapter);
        }
    }

    private void iniciarListenerBaseDatos() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mostrarListaParticipantes(participantesSala, nombreUsuario);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error en ValueEventListener", databaseError.toException());
            }
        });
    }
}
