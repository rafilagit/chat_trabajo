package com.dam.chat_trabajo;

import java.util.Random;

import java.security.SecureRandom;
import java.util.Locale;

public class Utils {

    // Método para generar un identificador único de 10 caracteres alfanuméricos
    public static String generateUniqueID() {
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder generatedID = new StringBuilder(10);

        SecureRandom random = new SecureRandom();

        // Usamos un bucle for para generar un identificador de 10 caracteres
        for (int i = 0; i < 10; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            generatedID.append(CHARACTERS.charAt(randomIndex));
        }

        return generatedID.toString();
    }
}
