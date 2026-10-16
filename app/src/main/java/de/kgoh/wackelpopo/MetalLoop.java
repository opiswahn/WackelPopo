package de.kgoh.wackelpopo;

import java.util.Random;

/**
 * Renders the background tracks: 16 bars of 80s-style metal in E minor, made from a galloping
 * distorted guitar, bass, drums and a square-wave lead with echo. Pure Java, no files.
 * GAME is the main song, FEVER a faster one with double bass and lead all the way through,
 * MENU a calm clean-guitar version of the same chords.
 */
public final class MetalLoop {
    public static final int RATE = 22050;
    public static final int GAME = 0, FEVER = 1, MENU = 2;
    private static final double[] BPM = {152, 176, 104};
    private static final double E2 = 82.41;
    /** Power-chord root of each bar, in semitones above low E: E E C D | E E G A, twice. */
    private static final int[] ROOTS = {0, 0, 8, 10, 0, 0, 3, 5, 0, 0, 8, 10, 0, 0, 3, 5};
    /** Lead arpeggio per bar: the bar's triad (semitones above E4) plus its octave. */
    private static final int[][] TRIADS = {{0, 3, 7}, {0, 3, 7}, {-4, 0, 3}, {-2, 2, 5}, {0, 3, 7}, {0, 3, 7},
            {3, 7, 10}, {5, 8, 12}};
    private static final int[] LEAD_PATTERN = {0, 1, 2, 1, 0, 1, 2, 3, 2, 1, 0, 1, 2, 3, 2, 1};

    private MetalLoop() {
    }

    public static short[] render(int song) {
        boolean fever = song == FEVER, menu = song == MENU;
        double step = 60.0 / BPM[song] / 4;
        int bars = ROOTS.length;
        int len = (int) Math.round(bars * 16 * step * RATE);
        float[] mix = new float[len];
        float[] lead = new float[len];
        Random noise = new Random(42);

        for (int bar = 0; bar < bars; bar++) {
            boolean partB = bar >= 8 || fever;
            // The fever song sits a whole tone higher, which makes it sound more urgent.
            double root = E2 * Math.pow(2, (ROOTS[bar] + (fever ? 2 : 0)) / 12.0);
            for (int s = 0; s < 16; s++) {
                int at = (int) Math.round((bar * 16 + s) * step * RATE);
                if (menu) {
                    // Clean, gentle arpeggio over the same chords, a soft hat and a kick on one.
                    int[] triad = TRIADS[bar % 8];
                    int semi = LEAD_PATTERN[s] == 3 ? triad[0] + 12 : triad[LEAD_PATTERN[s]];
                    pluck(mix, at, 164.81 * Math.pow(2, semi / 12.0), step * 3);
                    if (s % 4 == 0) bass(mix, at, root / 2, step * 3.5);
                    if (s == 0) kick(mix, at);
                    if (s % 4 == 2) hat(mix, at, noise, 0.05);
                    continue;
                }
                int k = s % 4;
                // Gallop: an eighth and two sixteenths; the first hit of each half bar rings a bit longer.
                if (k != 1) {
                    boolean ring = s == 0 || s == 8;
                    guitar(mix, at, root, (ring ? 2 : 1) * step * 0.92, ring);
                }
                if (s % 2 == 0) bass(mix, at, root / 2, step * 1.8);
                boolean kick = fever || (partB ? s % 2 == 0 : (s == 0 || s == 3 || s == 8 || s == 11));
                if (kick) kick(mix, at);
                if (s == 4 || s == 12) snare(mix, at, noise);
                if (s % 2 == 0) hat(mix, at, noise, s % 4 == 0 ? 0.12 : 0.08);
                if (partB) {
                    int[] triad = TRIADS[bar % 8];
                    int idx = LEAD_PATTERN[s];
                    int semi = idx == 3 ? triad[0] + 12 : triad[idx];
                    square(lead, at, 329.63 * Math.pow(2, (semi + (fever ? 2 : 0)) / 12.0), step * 0.95);
                }
            }
            if (!menu && bar % (fever ? 4 : 8) == 0) crash(mix, (int) Math.round(bar * 16 * step * RATE), noise);
        }

        // Dotted-eighth echo on the lead, the classic 80s trick.
        int delay = (int) Math.round(3 * step * RATE);
        for (int i = 0; i < len; i++) mix[i] += lead[i] + 0.35f * lead[(i - delay + len) % len];

        float peak = 0;
        for (float v : mix) peak = Math.max(peak, Math.abs(v));
        float scale = peak > 0 ? 0.9f * 32767 / peak : 0;
        short[] pcm = new short[len];
        for (int i = 0; i < len; i++) pcm[i] = (short) (mix[i] * scale);
        return pcm;
    }

    private static double saw(double f, double t) {
        double p = f * t;
        return 2 * (p - Math.floor(p)) - 1;
    }

    /** Distorted power chord (root, fifth, octave); palm-muted hits are darker and shorter. */
    private static void guitar(float[] out, int at, double f, double dur, boolean ring) {
        int n = (int) (dur * RATE);
        double lp = 0, cut = ring ? 0.6 : 0.38;
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            double raw = saw(f * 1.003, t) + saw(f * 1.5, t) + 0.6 * saw(f * 2 * 0.997, t);
            double y = Math.tanh(2.5 * raw);
            lp += cut * (y - lp);
            double env = Math.min(1, t / 0.003) * Math.exp(-t * (ring ? 2.5 : 9)) * Math.min(1, (n - i) / (RATE * 0.004));
            add(out, at + i, 0.38 * lp * env);
        }
    }

    private static void bass(float[] out, int at, double f, double dur) {
        int n = (int) (dur * RATE);
        double lp = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            lp += 0.12 * (saw(f, t) - lp);
            double env = Math.min(1, t / 0.004) * Math.exp(-t * 4) * Math.min(1, (n - i) / (RATE * 0.005));
            add(out, at + i, 0.28 * lp * env);
        }
    }

    private static void kick(float[] out, int at) {
        int n = (int) (0.18 * RATE);
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            phase += (45 + 75 * Math.exp(-t * 30)) / RATE;
            add(out, at + i, 0.45 * Math.sin(2 * Math.PI * phase) * Math.exp(-t * 22));
        }
    }

    private static void snare(float[] out, int at, Random noise) {
        int n = (int) (0.2 * RATE);
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            double v = 0.32 * (noise.nextDouble() * 2 - 1) * Math.exp(-t * 16)
                    + 0.25 * Math.sin(2 * Math.PI * 190 * t) * Math.exp(-t * 30);
            add(out, at + i, v);
        }
    }

    private static void hat(float[] out, int at, Random noise, double amp) {
        int n = (int) (0.05 * RATE);
        double prev = 0;
        for (int i = 0; i < n; i++) {
            double x = noise.nextDouble() * 2 - 1;
            add(out, at + i, amp * (x - prev) * Math.exp(-i / (RATE * 0.012)));
            prev = x;
        }
    }

    private static void crash(float[] out, int at, Random noise) {
        int n = (int) (1.4 * RATE);
        double prev = 0;
        for (int i = 0; i < n; i++) {
            double x = noise.nextDouble() * 2 - 1;
            add(out, at + i, 0.13 * (x - prev) * Math.exp(-i / (RATE * 0.45)));
            prev = x;
        }
    }

    /** Clean plucked string (Karplus-Strong), for the calm menu song. */
    private static void pluck(float[] out, int at, double f, double dur) {
        int n = (int) (dur * RATE);
        int period = Math.max(2, (int) Math.round(RATE / f));
        double[] ring = new double[period];
        Random r = new Random(at);
        for (int i = 0; i < period; i++) ring[i] = r.nextDouble() * 2 - 1;
        double lp = 0;
        for (int i = 0; i < n; i++) {
            int j = i % period;
            double v = ring[j];
            ring[j] = 0.5 * (v + ring[(j + 1) % period]) * 0.996;
            lp += 0.5 * (v - lp);
            add(out, at + i, 0.22 * lp * Math.min(1, (n - i) / (RATE * 0.01)));
        }
    }

    /** Square-wave lead with a little vibrato. */
    private static void square(float[] out, int at, double f, double dur) {
        int n = (int) (dur * RATE);
        double phase = 0, lp = 0;
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            phase += f * (1 + 0.006 * Math.sin(2 * Math.PI * 5.5 * t)) / RATE;
            double sq = phase - Math.floor(phase) < 0.5 ? 1 : -1;
            lp += 0.3 * (sq - lp);
            double env = Math.min(1, t / 0.003) * (0.7 + 0.3 * Math.exp(-t * 8)) * Math.min(1, (n - i) / (RATE * 0.004));
            add(out, at + i, 0.14 * lp * env);
        }
    }

    /** Adds a sample, wrapping tails around so the loop joins seamlessly. */
    private static void add(float[] out, int i, double v) {
        out[i % out.length] += (float) v;
    }
}
