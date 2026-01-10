package Model;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

public final class SoundManager {

    public enum Sfx { CLICK, BOOM, REVEAL, FLAG, WIN, LOSE, ERROR }

    private static boolean enabled = true;

    // Cache decoded clips (fast)
    private static final Map<Sfx, Clip> cache = new EnumMap<>(Sfx.class);

    private SoundManager() {}

    public static void setEnabled(boolean on) {
        enabled = on;
        if (!enabled) stopAll();
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void play(Sfx sfx) {
        if (!enabled) return;

        Clip clip = cache.get(sfx);
        if (clip == null) {
            clip = loadClip(pathFor(sfx));
            if (clip == null) return;
            cache.put(sfx, clip);
        }

        
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public static void stopAll() {
        for (Clip c : cache.values()) {
            if (c != null && c.isRunning()) c.stop();
        }
    }

    private static String pathFor(Sfx sfx) {
        return switch (sfx) {
            case CLICK -> "/assets/sfx/click.mp3";
            case BOOM -> "/assets/sfx/boom.mp3"; 
            case REVEAL -> "/assets/sfx/reveal.mp3";
            case FLAG -> "/assets/sfx/flag.mp3";
            case WIN -> "/assets/sfx/win.mp3";
            case LOSE -> "/assets/sfx/lose.mp3";
            case ERROR -> "/assets/sfx/error.mp3";
        };
    }

    private static Clip loadClip(String resourcePath) {
        try {
            InputStream raw = SoundManager.class.getResourceAsStream(resourcePath);
            if (raw == null) {
                System.err.println("Sound not found: " + resourcePath);
                return null;
            }

            // BufferedInputStream is important for SPI decoders
            try (BufferedInputStream in = new BufferedInputStream(raw);
                 AudioInputStream ais = AudioSystem.getAudioInputStream(in)) {

                AudioFormat baseFormat = ais.getFormat();

                // Ensure format is playable by Clip (PCM)
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false
                );

                try (AudioInputStream dais = AudioSystem.getAudioInputStream(decodedFormat, ais)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(dais);
                    return clip;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }
}
