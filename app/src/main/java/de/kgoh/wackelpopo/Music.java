package de.kgoh.wackelpopo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/** Loops the MetalLoop soundtrack in the background; rendered once, then streamed over and over. */
public final class Music {
    private static final float VOLUME = 0.45f;

    private static short[] loop;
    private static AudioTrack track;
    private static volatile boolean running;
    /** Bumped on every start, so a thread left over from an earlier start quits. */
    private static volatile int generation;

    private Music() {
    }

    public static synchronized void start() {
        if (running) return;
        running = true;
        final int mine = ++generation;
        new Thread(new Runnable() {
            @Override
            @SuppressWarnings("deprecation")
            public void run() {
                AudioTrack t = null;
                try {
                    if (loop == null) loop = MetalLoop.render();
                    int min = AudioTrack.getMinBufferSize(MetalLoop.RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    t = new AudioTrack(AudioManager.STREAM_MUSIC, MetalLoop.RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 8192) * 2, AudioTrack.MODE_STREAM);
                    t.setVolume(VOLUME);
                    synchronized (Music.class) {
                        if (!running || generation != mine) return;
                        track = t;
                    }
                    t.play();
                    int pos = 0, chunk = 2048;
                    while (running && generation == mine) {
                        int n = Math.min(chunk, loop.length - pos);
                        int written = t.write(loop, pos, n, AudioTrack.WRITE_NON_BLOCKING);
                        if (written < 0) break;
                        if (written == 0) Thread.sleep(15);
                        pos = (pos + written) % loop.length;
                    }
                } catch (Exception ignored) {
                    // No music is fine, the popo wiggles on.
                } finally {
                    synchronized (Music.class) {
                        if (track == t) track = null;
                    }
                    if (t != null) t.release();
                }
            }
        }).start();
    }

    /** Stops the music; the writer thread notices within a few milliseconds and releases the track. */
    public static synchronized void stop() {
        running = false;
        if (track != null) {
            try {
                track.pause();
                track.flush();
            } catch (Exception ignored) {
                // Already gone.
            }
        }
    }
}
