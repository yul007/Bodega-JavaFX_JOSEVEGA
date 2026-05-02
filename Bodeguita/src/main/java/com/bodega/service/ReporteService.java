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
        generarTXT(base + ".txt", "REPORTE DE COMPRAS POR PROVEEDOR", datos);
    }

    public static void generarReporteProductosConStockBajo(List<String[]> datos) throws IOException {
        String base = CARPETA_REPORTES + "/productos_stock_bajo_" + LocalDate.now();
        generarCSV(base + ".csv", new String[]{"Nombre", "Stock Actual", "Stock Mínimo"}, datos);
        generarTXT(base + ".txt", "REPORTE DE STOCK BAJO", datos);
    }

    public static void generarReporteValorInventario(BigDecimal valorTotal, List<String[]> datos) throws IOException {
        String base = CARPETA_REPORTES + "/valor_inventario_" + LocalDate.now();
        generarCSV(base + ".csv", new String[]{"Producto", "Cantidad", "Valor Total"}, datos);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(base + ".txt"))) {
            writer.write("REPORTE DE VALOR DE INVENTARIO\n");
            writer.write("Valor Total: $" + valorTotal + "\n\n");
            writer.write(String.format("%-24s %-12s %-12s\n", "Producto", "Cantidad", "Valor Total"));
            for (String[] fila : datos) {
                writer.write(String.format("%-24s %-12s %-12s\n",
                        safe(fila, 0), safe(fila, 1), safe(fila, 2)));
            }
        }
    }

    private static void generarCSV(String ruta, String[] cabecera, List<String[]> datos) throws IOException {
        File carpeta = new File(CARPETA_REPORTES);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
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
