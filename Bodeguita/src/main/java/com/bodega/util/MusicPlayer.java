package com.bodega.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

public class MusicPlayer {

    private MediaPlayer mediaPlayer;
    private double volume = 0.5; // Default volume (50%)

    public MusicPlayer(String audioFilePath) {
        try {
            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                Media media = new Media(audioFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setVolume(volume);
            } else {
                System.err.println("El archivo de audio no existe: " + audioFilePath);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el archivo de música: " + e.getMessage());
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else {
            System.err.println("MediaPlayer no está inicializado.");
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
        if (mediaPlayer != null) {
            this.volume = Math.max(0, Math.min(volume, 1)); // Ensure volume is between 0 and 1
            mediaPlayer.setVolume(this.volume);
        }
    }

    public double getVolume() {
        return volume;
    }
}