package com.bodega.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/** Reproductor simple para musica de fondo y efectos cortos desde resources. */
public class MusicPlayer {

    private static final String DEFAULT_BACKGROUND_MUSIC = "/music/musicaFondo.mp3";
    private static MusicPlayer backgroundMusicPlayer;

    private final MediaPlayer mediaPlayer;
    private double volume = 0.8;

    public MusicPlayer(String resourcePath) {
        this(resourcePath, true);
    }

    public MusicPlayer(String resourcePath, boolean loop) {
        this.mediaPlayer = crearMediaPlayer(resourcePath);
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
            mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        }
    }

    public static MusicPlayer cargarDesdeResources(String resourcePath) {
        return new MusicPlayer(resourcePath);
    }

    public static synchronized MusicPlayer musicaFondoCompartida() {
        if (backgroundMusicPlayer == null) {
            backgroundMusicPlayer = new MusicPlayer(DEFAULT_BACKGROUND_MUSIC, true);
        }
        return backgroundMusicPlayer;
    }

    public static synchronized void reproducirMusicaFondo() {
        musicaFondoCompartida().play();
    }

    public static synchronized void pausarMusicaFondo() {
        musicaFondoCompartida().pause();
    }

    public static synchronized void reanudarMusicaFondo() {
        musicaFondoCompartida().resume();
    }

    public static synchronized void detenerMusicaFondo() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0, Math.min(volume, 1));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
    }

    public double getVolume() {
        return volume;
    }

    private MediaPlayer crearMediaPlayer(String resourcePath) {
        try {
            String normalized = normalizarRuta(resourcePath);
            URL resource = MusicPlayer.class.getResource(normalized);
            if (resource == null) {
                System.err.println("No se encontro el recurso de audio: " + normalized);
                return null;
            }

            Media media = new Media(resource.toExternalForm());
            return new MediaPlayer(media);
        } catch (Exception exception) {
            System.err.println("Error al cargar el audio " + resourcePath + ": " + exception.getMessage());
            return null;
        }
    }

    private String normalizarRuta(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("La ruta del audio es obligatoria.");
        }
        return resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
    }
}
