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
        String nombreArchivo = CARPETA_REPORTES + "/kardex_producto_" + producto.getNombre() + "_" + LocalDate.now() + ".csv";
        generarCSV(nombreArchivo, new String[]{"Fecha", "Tipo", "Cantidad", "Costo Unitario", "Saldo Cantidad", "Saldo Valor"}, datos);
    }

    public static void generarReporteNotasPorPeriodo(LocalDate desde, LocalDate hasta, List<String[]> datos) throws IOException {
        String nombreArchivo = CARPETA_REPORTES + "/notas_salida_" + desde + "_" + hasta + ".csv";
        generarCSV(nombreArchivo, new String[]{"Número", "Cliente", "Fecha", "Subtotal", "IVA", "Total"}, datos);
    }

    public static void generarReporteComprasPorProveedor(Proveedor proveedor, List<String[]> datos) throws IOException {
        String nombreArchivo = CARPETA_REPORTES + "/compras_proveedor_" + proveedor.getNombre() + "_" + LocalDate.now() + ".csv";
        generarCSV(nombreArchivo, new String[]{"Fecha", "Producto", "Cantidad", "Costo Total"}, datos);
    }

    public static void generarReporteProductosConStockBajo(List<String[]> datos) throws IOException {
        String nombreArchivo = CARPETA_REPORTES + "/productos_stock_bajo_" + LocalDate.now() + ".csv";
        generarCSV(nombreArchivo, new String[]{"Nombre", "Stock Actual", "Stock Mínimo"}, datos);
    }

    public static void generarReporteValorInventario(BigDecimal valorTotal, List<String[]> datos) throws IOException {
        String nombreArchivo = CARPETA_REPORTES + "/valor_inventario_" + LocalDate.now() + ".csv";
        generarCSV(nombreArchivo, new String[]{"Producto", "Cantidad", "Valor Total"}, datos);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nombreArchivo.replace(".csv", ".txt")))) {
            writer.write("REPORTE DE VALOR DE INVENTARIO\n");
            writer.write("Valor Total: $" + valorTotal + "\n\n");
            writer.write(String.format("%-20s %-10s %-10s\n", "Producto", "Cantidad", "Valor Total"));
            datos.forEach(fila -> {
                try {
                    writer.write(String.format("%-20s %-10s %-10s\n", fila));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
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
}