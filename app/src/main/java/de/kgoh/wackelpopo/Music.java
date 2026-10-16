package de.kgoh.wackelpopo;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * Loops the MetalLoop songs in the background. Songs are rendered once, then streamed over and over;
 * switching songs fades the old one out and the new one in.
 */
public final class Music {
    private static final short[][] songs = new short[3][];
    private static AudioTrack track;
    private static volatile boolean running;
    /** Bumped on every start, so a thread left over from an earlier start quits. */
    private static volatile int generation;
    /** Volume the music glides towards; the game sets it to match what is happening. */
    private static volatile float target = 0.4f;
    private static volatile int wanted = MetalLoop.MENU;

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
                    int min = AudioTrack.getMinBufferSize(MetalLoop.RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    t = new AudioTrack(AudioManager.STREAM_MUSIC, MetalLoop.RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 8192) * 2, AudioTrack.MODE_STREAM);
                    float volume = 0;
                    t.setVolume(volume);
                    synchronized (Music.class) {
                        if (!running || generation != mine) return;
                        track = t;
                    }
                    int current = wanted;
                    short[] loop = song(current);
                    t.play();
                    int pos = 0, chunk = 2048;
                    while (running && generation == mine) {
                        // Glide to the target volume; when the song should change, fade out first.
                        float goal = wanted != current ? 0 : target;
                        volume += Math.max(-0.006f, Math.min(0.006f, goal - volume));
                        t.setVolume(volume);
                        if (wanted != current && volume <= 0.01f) {
                            current = wanted;
                            loop = song(current);
                            pos = 0;
                        }
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

    private static short[] song(int which) {
        synchronized (songs) {
            if (songs[which] == null) songs[which] = MetalLoop.render(which);
            return songs[which];
        }
    }

    /** Which song should play (MetalLoop.GAME, FEVER or MENU) and how loud: 0 = silent, 1 = full blast. */
    public static void set(int song, float level) {
        wanted = song;
        target = level;
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
