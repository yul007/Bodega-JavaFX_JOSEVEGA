//yuli
package com.bodega.service;

import com.bodega.model.Producto;
// Importaciones de ZXing para generación de QR
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
// Importaciones de JavaFX para manejo de hilos e imágenes
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.util.Map;

public class QrCodeService {

    private static final int QR_SIZE = 150; //tam fijo plx 

    /**
     * Genera y muestra el QR directamente en el ImageView, sin red.
     */
    public void generarQRCode(Producto producto, ImageView imageView) {
        if (producto == null || imageView == null) return;

        String contenido = obtenerContenido(producto);  //getsSKU del producto para poner en QR

        // Generar en hilo secundario para no bloquear la UI
        Thread thread = new Thread(() -> {
            try {
                Image qrImage = generarImagen(contenido);
                Platform.runLater(() -> {             //hace que el hilo principal ejecute lo sig. cuando se pueda 
                    imageView.setImage(qrImage);
                    imageView.setPreserveRatio(true); //redimensiona manteniendo proporciones
                    imageView.setSmooth(true);        //suaviza los bordes
                });
            } catch (Exception e) {
                Platform.runLater(() -> imageView.setImage(null));
                System.err.println("Error generando QR: " + e.getMessage());
            }
        }, "qr-generator");     //nombre del hilo para debug
        thread.setDaemon(true); //permite que la app cierre aunque el hilo siga corriendo
        thread.start();         //inicia el hilo para generar el QR sin bloquear la interfaz
    }

    /**
     * Genera el QR y lo retorna como Image de JavaFX.
     */
    public Image generarImagen(String contenido) throws WriterException {
        Map<EncodeHintType, Object> hints = Map.of(
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, //legibilidad del codigo 15% de daño IRL
            EncodeHintType.MARGIN, 1                                 //margen
        );

        QRCodeWriter writer = new QRCodeWriter(); //obj escritor del qr (math -> drawing)
        BitMatrix bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints); //generamos la mat de bits (b&w)// .encode decide segun world standard de qrs cual debe ser black y cual white

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix); //burfferedimage es un tipo de imagen de java clasico, y MatrixToImageWritter tiene el metodo para pasar la matriz a image
        return SwingFXUtils.toFXImage(bufferedImage, null); //convierte bufferedImage a Image de JavaFX
    }

    /**
     * Determina el texto que irá codificado en el QR.
     */
    public String obtenerContenido(Producto producto) {
        String sku = producto.getSku();
        if (sku != null && !sku.isBlank()) {
            return sku;
        }
        return String.valueOf(producto.getIdProducto());
    }
}