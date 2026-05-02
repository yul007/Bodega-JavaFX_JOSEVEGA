package com.bodega.service;

import com.bodega.model.Producto;
import com.bodega.model.Proveedor;
import javafx.collections.ObservableList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteService {

    private static final String CARPETA_REPORTES = "reportes";

    public static void generarReporteKardexPorProducto(Producto producto, List<String[]> datos) throws IOException {
        String base = CARPETA_REPORTES + "/kardex_producto_" + sanitizar(producto.getNombre()) + "_" + LocalDate.now();
        generarCSV(base + ".csv", new String[]{"Fecha", "Tipo", "Producto", "Cantidad", "Costo Unitario", "Saldo Cantidad", "Saldo Valor", "Referencia"}, datos);
        generarTXT(base + ".txt", "REPORTE DE KARDEX", datos);
    }

    public static void generarReporteNotasPorPeriodo(LocalDate desde, LocalDate hasta, List<String[]> datos) throws IOException {
        String base = CARPETA_REPORTES + "/notas_salida_" + desde + "_" + hasta;
        generarCSV(base + ".csv", new String[]{"Número", "Cliente", "Fecha", "Subtotal", "IVA", "Total"}, datos);
        generarTXT(base + ".txt", "REPORTE DE NOTAS DE SALIDA", datos);
    }

    public static void generarReporteComprasPorProveedor(Proveedor proveedor, List<String[]> datos) throws IOException {
        String base = CARPETA_REPORTES + "/compras_proveedor_" + sanitizar(proveedor.getNombre()) + "_" + LocalDate.now();
        generarCSV(base + ".csv", new String[]{"Fecha", "Producto", "Cantidad", "Costo Unitario"}, datos);
        generarTXT(base + ".txt", "REPORTE DE COMPRAS POR PROVEEDOR: " + proveedor.getNombre(), datos);
    }

    public static void generarReporteProductosConStockBajo(List<String[]> datos) throws IOException {
        String base = CARPETA_REPORTES + "/productos_stock_bajo_" + LocalDate.now();
        generarCSV(base + ".csv", new String[]{"Nombre", "Stock Actual", "Stock Mínimo"}, datos);
        generarTXT(base + ".txt", "REPORTE DE STOCK BAJO", datos);
    }


    private static void generarCSV(String ruta, String[] cabecera, List<String[]> datos) throws IOException {
        File carpeta = new File(CARPETA_REPORTES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
            System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));
System.out.println("Ruta completa del reporte: " + new File(ruta).getAbsolutePath());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            System.out.println("Directorio de trabajo: " + System.getProperty("user.dir"));
System.out.println("Ruta completa del reporte: " + new File(ruta).getAbsolutePath());
            writer.write(String.join(",", cabecera) + "\n");
            for (String[] fila : datos) {
                writer.write(String.join(",", fila) + "\n");
            }
        }
    }

    private static void generarTXT(String ruta, String titulo, List<String[]> datos) throws IOException {
        File carpeta = new File(CARPETA_REPORTES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            writer.write(titulo + "\n\n");
            for (String[] fila : datos) {
                writer.write(String.join(" | ", fila) + "\n");
            }
        }
    }

    private static String sanitizar(String texto) {
        return texto == null ? "sin_nombre" : texto.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private static String safe(String[] fila, int index) {
        return fila.length > index && fila[index] != null ? fila[index] : "";
    }
}
