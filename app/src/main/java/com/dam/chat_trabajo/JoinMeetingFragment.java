package com.dam.chat_trabajo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class JoinMeetingFragment extends Fragment {

    private EditText etMeetingId;
    private Button btnJoinMeeting;

    public JoinMeetingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_join_meeting, container, false);

        etMeetingId = rootView.findViewById(R.id.etMeetingId);
        btnJoinMeeting = rootView.findViewById(R.id.btnJoinMeeting);

        btnJoinMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String meetingId = etMeetingId.getText().toString().trim();
                if (!meetingId.isEmpty()) {
                    // Aquí deberías implementar la lógica para unirte a la videollamada con el ID proporcionado
                    // Puedes usar Intent para abrir otra actividad o comunicarte con tu servidor
                }
            }
        });

        return rootView;
    }
}
