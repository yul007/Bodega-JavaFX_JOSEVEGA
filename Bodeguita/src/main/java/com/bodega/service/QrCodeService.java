package com.bodega.service;

import com.bodega.model.Producto;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.util.Map;

public class QrCodeService {

    private static final int QR_SIZE = 150;

    /**
     * Genera y muestra el QR directamente en el ImageView, sin red.
     */
    public void generarQRCode(Producto producto, ImageView imageView) {
        if (producto == null || imageView == null) return;

        String contenido = obtenerContenido(producto);

        // Generar en hilo secundario para no bloquear la UI
        Thread thread = new Thread(() -> {
            try {
                Image qrImage = generarImagen(contenido);
                Platform.runLater(() -> {
                    imageView.setImage(qrImage);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> imageView.setImage(null));
                System.err.println("Error generando QR: " + e.getMessage());
            }
        }, "qr-generator");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Genera el QR y lo retorna como Image de JavaFX.
     */
    public Image generarImagen(String contenido) throws WriterException {
        Map<EncodeHintType, Object> hints = Map.of(
            EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN, 1
        );

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(contenido, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return SwingFXUtils.toFXImage(bufferedImage, null);
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