package com.restaurant.config;

public class UnitConverter {

    public static double kilogramToPieces(String ingredientName, double quantityInKilogram) {
        return switch (ingredientName) {
            case "tomate", "chocolat" -> quantityInKilogram * 10;
            case "laitue" -> quantityInKilogram * 2;
            case "poulet" -> quantityInKilogram * 8;
            case "beurre" -> quantityInKilogram * 4;
            default -> throw new IllegalArgumentException("Conversion KG -> PCS impossible pour: " + ingredientName);
        };
    }

    public static double piecesToLiters(String ingredientName, double quantityInPieces) {
        return switch (ingredientName) {
            case "chocolat" -> quantityInPieces * 0.25;
            case "beurre" -> quantityInPieces * 1.25;
            default -> throw new IllegalArgumentException("Conversion PCS -> L impossible pour: " + ingredientName);
        };
    }

    public static double kilogramToLiters(String ingredientName, double quantityInKilogram) {
        return switch (ingredientName) {
            case "chocolat" -> quantityInKilogram * 2.5;  // 1 KG = 2.5 L
            case "beurre" -> quantityInKilogram * 5.0;    // 1 KG = 5 L
            default -> throw new IllegalArgumentException("Conversion KG -> L impossible pour: " + ingredientName);
        };
    }

    public static double piecesToKilogram(String ingredientName, double quantityInPieces) {
        return switch (ingredientName) {
            case "tomate", "chocolat" -> quantityInPieces / 10;
            case "laitue" -> quantityInPieces / 2;
            case "poulet" -> quantityInPieces / 8;
            case "beurre" -> quantityInPieces / 4;
            default -> throw new IllegalArgumentException("Conversion PCS -> KG impossible pour: " + ingredientName);
        };
    }

    public static double litresToKilogram(String ingredientName, double quantityInLitres) {
        return switch (ingredientName) {
            case "chocolat" -> quantityInLitres / 2.5;
            case "beurre" -> quantityInLitres / 5.0;
            default -> throw new IllegalArgumentException("Conversion L -> KG impossible pour: " + ingredientName);
        };
    }
}