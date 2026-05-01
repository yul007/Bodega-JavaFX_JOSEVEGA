package com.bodega.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/** Controla la musica de fondo de la aplicacion sin bloquear la interfaz. */
public final class MusicPlayer {

    private static final double DEFAULT_VOLUME = 0.25;
    private static MediaPlayer mediaPlayer;

    private MusicPlayer() {
    }

    public static void play(String resourcePath) {
        try {
            if (mediaPlayer == null) {
                var resource = MusicPlayer.class.getResource(resourcePath);
                if (resource == null) {
                    return;
                }

                Media media = new Media(resource.toExternalForm());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(DEFAULT_VOLUME);
            }
            mediaPlayer.play();
        } catch (RuntimeException exception) {
            stop();
        }
    }

    public static void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }
}
