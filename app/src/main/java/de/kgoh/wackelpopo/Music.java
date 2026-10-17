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
    private static final android.media.AudioTimestamp stamp = new android.media.AudioTimestamp();
    private static volatile boolean running;
    /** Bumped on every start, so a thread left over from an earlier start quits. */
    private static volatile int generation;
    /** Volume the music glides towards; the game sets it to match what is happening. */
    private static volatile float target = 0.4f;
    private static volatile int wanted = MetalLoop.MENU;
    /** Frames handed to the track, and where the current and the previous song started in that count. */
    private static volatile long written, songStart, prevStart;
    private static volatile int playing = -1, prevSong = -1;
    /** Bumped when the stage changes, so the writer fades out and loads the songs rendered for the new stage. */
    private static volatile int content;

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
                    int loaded = content;
                    short[] loop = song(current);
                    written = 0;
                    songStart = prevStart = 0;
                    playing = prevSong = current;
                    t.play();
                    int pos = 0, chunk = 2048;
                    while (running && generation == mine) {
                        // Glide to the target volume; when the song should change, fade out first.
                        boolean change = wanted != current || loaded != content;
                        float goal = change ? 0 : target;
                        volume += Math.max(-0.006f, Math.min(0.006f, goal - volume));
                        t.setVolume(volume);
                        if (change && volume <= 0.01f) {
                            loaded = content;
                            prevSong = current;
                            prevStart = songStart;
                            current = wanted;
                            loop = song(current);
                            pos = 0;
                            songStart = written;
                            playing = current;
                        }
                        int n = Math.min(chunk, loop.length - pos);
                        int wrote = t.write(loop, pos, n, AudioTrack.WRITE_NON_BLOCKING);
                        if (wrote < 0) break;
                        if (wrote == 0) Thread.sleep(15);
                        written += wrote;
                        pos = (pos + wrote) % loop.length;
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

    /**
     * What can be heard right now: {seconds into the song, song}, or null without music. The old song may
     * still be in the buffer for a moment after a switch, so the answer follows the playback head, not the writer.
     */
    public static synchronized double[] clock() {
        if (!running || track == null || playing < 0) return null;
        try {
            // The timestamp says which frame leaves the speaker at which moment; the playback head alone runs
            // ahead of what is heard by the output latency, which made the ring feel early.
            long head;
            if (track.getTimestamp(stamp)) {
                head = stamp.framePosition + (System.nanoTime() - stamp.nanoTime) * MetalLoop.RATE / 1000000000L;
            } else {
                head = (track.getPlaybackHeadPosition() & 0xFFFFFFFFL) - MetalLoop.RATE / 10;
            }
            if (head < 0) return null;
            if (head >= songStart) return new double[]{(head - songStart) / (double) MetalLoop.RATE, playing};
            return new double[]{(head - prevStart) / (double) MetalLoop.RATE, prevSong};
        } catch (Exception e) {
            return null;
        }
    }

    /** Switches the songs to another stage's tempo and key; the change fades in like a song change. */
    public static void setStage(int stage) {
        synchronized (songs) {
            if (MetalLoop.stage == stage) return;
            MetalLoop.stage = stage;
            for (int i = 0; i < songs.length; i++) songs[i] = null;
        }
        content++;
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
