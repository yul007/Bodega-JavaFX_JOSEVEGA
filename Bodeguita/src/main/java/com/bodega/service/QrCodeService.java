package com.bodega.service;

import com.bodega.model.Producto;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class QrCodeService {

    private static final String QR_API_BASE_URL = "https://chart.googleapis.com/chart?chs=150x150&cht=qr&chl=";

    public String construirUrlQr(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo.");
        }

        String contenido = producto.getSku();
        if (contenido == null || contenido.isBlank()) {
            contenido = String.valueOf(producto.getIdProducto());
        } else {
            contenido = producto.getIdProducto() + " | " + contenido;
        }

        String encoded = URLEncoder.encode(contenido, StandardCharsets.UTF_8);
        return QR_API_BASE_URL + encoded;
    }

    public void generarQRCode(Producto producto, ImageView imageView) {
        if (imageView == null) {
            throw new IllegalArgumentException("El ImageView no puede ser nulo.");
        }

        try {
            String qrUrl = construirUrlQr(producto);
            Image qrImage = new Image(qrUrl, true);
            imageView.setImage(qrImage);
        } catch (Exception exception) {
            imageView.setImage(null);
        }
    }
}
