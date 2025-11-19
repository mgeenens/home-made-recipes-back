package com.example.hmrback.exception.util;

public class ExceptionMessageConstants {

    private ExceptionMessageConstants() {
    }

    public static final String EXCEPTION_BASE_MESSAGE = "HMRGenericException {}";

    public static final String ACCESS_DENIED_EXCEPTION_MESSAGE = "Vous n'avez pas la permission de réaliser cette action.";

    // Recipe
    public static final String RECIPE_NOT_FOUND_EXCEPTION_MESSAGE = "La recette avec l'id %s est introuvable.";
}
