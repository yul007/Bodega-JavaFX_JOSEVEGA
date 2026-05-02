package com.bodega.util;

import java.math.BigDecimal;

/** Validaciones comunes para formularios y operaciones de inventario. */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static String requerido(String texto, String campo) {
        if (estaVacio(texto)) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return texto.trim();
    }

    public static BigDecimal requeridoPositivo(BigDecimal valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser mayor que cero.");
        }
        return valor;
    }

    public static BigDecimal requeridoNoNegativo(BigDecimal valor, String campo) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser negativo.");
        }
        return valor;
    }

    public static String opcional(String texto) {
        return estaVacio(texto) ? null : texto.trim();
    }
}
