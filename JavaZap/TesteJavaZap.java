package JavaZap;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class TesteJavaZap {
    public static void main(String[] args) {
        // Inicializa o servidor em uma thread separada
        new Thread(() -> {
            try {
                new JavaZap.Servidor(); // Usando a classe ConfigConexao
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Aguarda o servidor iniciar
        try {
            Thread.sleep(1000); // Aguarda 1 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Inicializa o cliente em uma thread separada
        new Thread(() -> {
            try {
                new JavaZap.TelaJavaZap(); // Usando ConfigConexao
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        //  música de fundo (Java Sound)
        new Thread(() -> {
            try {
                playMusic("src/music/TronSoundWav.wav"); //Caminho da música
            } catch (Exception e) {
                System.err.println("Erro a iniciar a música: " + e.getMessage());
            }
        }).start();
    }

    // Função  música(Java Sound)
    private static void playMusic(String musicFilePath) {
        try {
            // Prepara a música
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(musicFilePath));

            // Gera o Clip para tocar o áudio/música
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            //  Inicia a música em loop
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Erro ao tocar a música: " + e.getMessage());
        }
    }
}