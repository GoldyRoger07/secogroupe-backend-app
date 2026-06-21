package com.secogroupe.app.exception;

import lombok.Getter;

/**
 * Levée lorsqu'un lien de vérification email a expiré.
 * Porte l'email du compte concerné afin de permettre le renvoi d'un nouveau lien.
 */
@Getter
public class VerificationExpiredException extends RuntimeException {

    private final String email;

    public VerificationExpiredException(String email) {
        super("Lien de vérification expiré. Veuillez demander un nouveau code.");
        this.email = email;
    }
}
