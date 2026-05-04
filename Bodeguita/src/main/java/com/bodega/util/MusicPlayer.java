package com.bodega.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

/** Reproductor simple para música de fondo y efectos cortos desde resources. */
public class MusicPlayer {

    private static final String DEFAULT_BACKGROUND_MUSIC = "/music/musicaFondo.mp3";
    private static MusicPlayer backgroundMusicPlayer;     /// Instancia única para el reproductor de fondo (Singleton)    // solo existe un objeto MusicPlayer para toda la aplicación que maneja la música ambiental.

    private final MediaPlayer mediaPlayer;     /// Reproductor de JavaFX que maneja la reproducción real // MediaPlayer es la clase de JavaFX que realmente reproduce el audio (controla la reproducción, volumen, pausa, etc// stop, play, resume, pause etc

    public MusicPlayer(String resourcePath, boolean loop) {
        this.mediaPlayer = crearMediaPlayer(resourcePath); // Carga el audio desde resources
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(0.8);
            mediaPlayer.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        }
    }

    // ── Música de fondo compartida ──────────────────────────────────────────

    public static synchronized void reproducirMusicaFondo() {
        if (backgroundMusicPlayer == null)
            backgroundMusicPlayer = new MusicPlayer(DEFAULT_BACKGROUND_MUSIC, true);
        backgroundMusicPlayer.play(); // es lo mismo que decir backgroundMusicPlayer.mediaPlayer.play();
    }

    public static synchronized void pausarMusicaFondo() {
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.pause();
    }

    public static synchronized void reanudarMusicaFondo() {
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.resume();
    }

    public static synchronized void detenerMusicaFondo() {
        if (backgroundMusicPlayer != null) backgroundMusicPlayer.stop();
    }

    // ── Control de instancia ────────────────────────────────────────────────

    public void play() {
        if (mediaPlayer != null) mediaPlayer.play();
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING)
            mediaPlayer.pause();
    }

    public void resume() {
        if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED)
            mediaPlayer.play();
    }

    public void stop() {
        if (mediaPlayer != null) mediaPlayer.stop();
    }

    public void setVolume(double v) {
        if (mediaPlayer != null) mediaPlayer.setVolume(Math.max(0, Math.min(v, 1)));
    }

    // ── Interno ─────────────────────────────────────────────────────────────

    private MediaPlayer crearMediaPlayer(String resourcePath) { //guarda en el MediaPlayer el archivo que se va a reproducir
        if (resourcePath == null || resourcePath.isBlank())
            throw new IllegalArgumentException("La ruta del audio es obligatoria.");
        try {
            String ruta = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath; // Asegura que la ruta comience con "/"
            URL resource = MusicPlayer.class.getResource(ruta); // Busca el recurso en el classpath (resources)
            if (resource == null) {
                System.err.println("No se encontró el recurso: " + ruta);
                return null;
            }
            return new MediaPlayer(new Media(resource.toExternalForm())); // Carga el audio desde resources
        } catch (Exception e) {
            System.err.println("Error al cargar el audio " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    public MediaPlayer getMediaPlayer() {
    return mediaPlayer;
}
}


