package de.kgoh.wackelpopo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Random;

/** Tiny sound generator, so the app needs no audio files. */
public final class Synth {
    private static final int RATE = 22050;

    private Synth() {
    }

    /** Plays notes (Hz, 0 = rest) one after another, each lasting noteSeconds. */
    public static void melody(final float[] notes, final float noteSeconds) {
        int per = (int) (RATE * noteSeconds);
        short[] pcm = new short[per * notes.length];
        for (int n = 0; n < notes.length; n++) {
            float f = notes[n];
            if (f <= 0) continue;
            for (int i = 0; i < per; i++) {
                double t = (double) i / RATE;
                double env = Math.min(1.0, i / (RATE * 0.01)) * Math.exp(-3.0 * i / per);
                double v = 0.6 * Math.sin(2 * Math.PI * f * t)
                        + 0.25 * Math.sin(4 * Math.PI * f * t)
                        + 0.15 * Math.signum(Math.sin(2 * Math.PI * f * t));
                pcm[n * per + i] = (short) (v * env * 9000);
            }
        }
        play(pcm);
    }

    /** A cartoon "pfffrrt": a wobbly falling buzz with a bit of noise. */
    public static void pups() {
        int len = (int) (RATE * 0.9);
        short[] pcm = new short[len];
        Random rnd = new Random();
        double phase = 0;
        for (int i = 0; i < len; i++) {
            double p = (double) i / len;
            double f = 150 - 85 * p + 12 * Math.sin(i * 2 * Math.PI * 23 / RATE);
            phase += f / RATE;
            double saw = 2 * (phase - Math.floor(phase)) - 1;
            double env = Math.min(1.0, i / (RATE * 0.03)) * (1 - p) * (0.75 + 0.25 * Math.sin(i * 2 * Math.PI * 31 / RATE));
            double v = 0.8 * saw + 0.35 * (rnd.nextDouble() * 2 - 1);
            pcm[i] = (short) (v * env * 10000);
        }
        play(pcm);
    }

    @SuppressWarnings("deprecation")
    private static void play(final short[] pcm) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioTrack track = null;
                try {
                    track = new AudioTrack(AudioManager.STREAM_MUSIC, RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, pcm.length * 2, AudioTrack.MODE_STATIC);
                    track.write(pcm, 0, pcm.length);
                    track.play();
                    Thread.sleep(pcm.length * 1000L / RATE + 150);
                } catch (Exception ignored) {
                    // No sound is fine, the show goes on.
                } finally {
                    if (track != null) track.release();
                }
            }
        }).start();
    }
}
