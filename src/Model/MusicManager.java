package Model;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

public class MusicManager {

    private static Clip clip;
    public static void play(String resourcePath) {
        stop();

        try {
            InputStream is = MusicManager.class
                    .getClassLoader()
                    .getResourceAsStream(resourcePath);

            if (is == null) {
                System.err.println("Music resource not found: " + resourcePath);
                return;
            }

            AudioInputStream ais =
                    AudioSystem.getAudioInputStream(new BufferedInputStream(is));

            clip = AudioSystem.getClip();
            clip.open(ais);
            clip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    public static void applyVolume() {
        if (clip == null) return;

        try {
            FloatControl gain =
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            float volume = SysData.getMusicVolume(); // 0.0 – 1.0
            float min = gain.getMinimum();
            float max = gain.getMaximum();

            float dB = min + (max - min) * volume;
            gain.setValue(dB);

        } catch (Exception e) {
            System.err.println("Volume control not supported.");
        }
    }
}
