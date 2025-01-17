package sounds;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class PlaySound {
    // Enum for different sound types
    public enum Sound {
        CLICK("click.wav"),
        CONFIRMATION("confirmation.wav"),
        PURCHASE("purchase.wav"),
        ERROR("error.wav");

        private final String fileName;

        Sound(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    // Static method to play a sound
    public static void playSound(Sound sound) {
        try {
            // Get sound file from resources
            URL soundFile = PlaySound.class.getResource("/sounds/" + sound.getFileName());
            if (soundFile == null) {
                System.err.println("Could not find sound file: " + sound.getFileName());
                return;
            }

            // Get and open the audio input stream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);

            // Get a clip resource
            Clip clip = AudioSystem.getClip();

            // Open and start the audio clip
            clip.open(audioInputStream);
            clip.start();

            // Add listener to release resources when done
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                    try {
                        audioInputStream.close();
                    } catch (IOException e) {
                        System.err.println("Error closing audio stream: " + e.getMessage());
                    }
                }
            });

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Audio file format not supported: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error reading sound file: " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("Audio line unavailable: " + e.getMessage());
        }
    }

    // Convenience methods for specific sounds
    public static void playClickSound() {
        playSound(Sound.CLICK);
    }

    public static void playConfirmationSound() {
        playSound(Sound.CONFIRMATION);
    }

    public static void playPurchaseSound() {
        playSound(Sound.PURCHASE);
    }

    public static void playErrorSound(){
        playSound(Sound.ERROR);
    }
}