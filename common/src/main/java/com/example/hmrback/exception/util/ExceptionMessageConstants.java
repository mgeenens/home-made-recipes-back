package com.example.hmrback.exception.util;

public class ExceptionMessageConstants {

    private ExceptionMessageConstants() {
    }

    public static final String EXCEPTION_BASE_MESSAGE = "HMRGenericException {}";

    public static final String ACCESS_DENIED_EXCEPTION_MESSAGE = "Vous n'avez pas la permission de réaliser cette action.";
    public static final String USER_ALREADY_EXISTS_MESSAGE = "L'utilisateur avec l'email %s existe déjà.";
    public static final String USER_NOT_FOUND_EMAIL_MESSAGE = "L'utilisateur avec l'email %s est introuvable.";
    public static final String ROLE_NOT_FOUND_MESSAGE = "Le rôle '%s' est introuvable.";

    // Recipe
    public static final String RECIPE_NOT_FOUND_EXCEPTION_MESSAGE = "La recette avec l'id %s est introuvable.";
}
