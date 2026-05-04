//yuli
package com.bodega.util;

import java.math.BigDecimal;

/** Validaciones comunes para formularios y operaciones de inventario. */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static String requerido(String texto, String campo) { //se usa para validar campos de texto que no pueden estar vacíos, como el nombre del producto o la categoría. Si el campo está vacío, se lanza una excepción con un mensaje específico.
        if (estaVacio(texto)) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        return texto.trim();
    }

    public static BigDecimal requeridoPositivo(BigDecimal valor, String campo) { //se usa para validar campos numéricos que deben ser mayores que cero, como el precio o la cantidad. Si el valor es nulo o no es positivo, se lanza una excepción con un mensaje específico.
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser mayor que cero.");
        }
        return valor;
    }

    public static BigDecimal requeridoNoNegativo(BigDecimal valor, String campo) { //se usa para validar campos numéricos que no pueden ser negativos, como el stock mínimo o el stock actual. Si el valor es nulo o es negativo, se lanza una excepción con un mensaje específico.
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + campo + " es obligatorio.");
        }
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El campo " + campo + " no puede ser negativo.");
        }
        return valor;
    }

    public static String opcional(String texto) { //se usa para validar campos de texto que son opcionales, como la descripción del producto. Si el campo está vacío, se devuelve null; de lo contrario, se devuelve el texto sin espacios al principio o al final.
        return estaVacio(texto) ? null : texto.trim();
    }
}
