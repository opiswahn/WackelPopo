package de.kgoh.wackelpopo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Draws the cartoon popo and runs the Popo-Klatsch-Counter game.
 *
 * Every tap adds to the counter, but the popo cools down and the counter keeps sinking, faster and
 * faster. Several fingers landing together form a "Griff" with a bonus. Points are spent in the shop
 * on upgrades. At -10 the game is over. The total Klatscher of a game unlock the 100 awards, special
 * feats unlock 30 more, and the total goes into the highscore table.
 */
public class WackelView extends View {
    private static final int INK = 0xFF2B1B17;
    private static final int SKIN = 0xFFFFC1A1;
    private static final int SKIN_COLD = 0xFFB3E5FC;
    private static final int SKIN_HOT = 0xFFFF8A80;
    private static final int SKIN_SHADE = 0x8CF29E80;
    private static final int BLUSH = 0x80FF8A8A;
    private static final int GOLD = 0xFFFFC107;
    private static final int GREEN = 0xFF43A047;
    private static final int RED = 0xFFE53935;
    private static final int PURPLE = 0xFFAB47BC;
    private static final int CARD = 0xFFFFF8E1;
    private static final int[] PARTY = {0xFFE53935, 0xFFFF9800, 0xFFFFEB3B, 0xFF4CAF50, 0xFF2196F3, 0xFF9C27B0, 0xFFFF4081, 0xFF00BCD4};
    private static final int[] RAINBOW = {0xFFE53935, 0xFFFF9800, 0xFFFFEB3B, 0xFF4CAF50, 0xFF2196F3, 0xFF7E57C2};

    private static final float IDLE_ENERGY = 0.25f;
    private static final float PERIOD_S = 0.52f;
    private static final double GAME_OVER_AT = -10;
    private static final double BASE_DRAIN = 0.85;
    /** The cool-down speeds up by this factor every second: doubles roughly every minute. */
    private static final double DRAIN_GROWTH = 1.014;
    private static final double FEVER_BONUS = 1.25;
    private static final double CRIT_FACTOR = 3;
    /** Rhythm: a tap this close to the beat is PERFEKT, a bit further GUT, anything else DANEBEN. */
    private static final float PERFECT_S = 0.09f, GOOD_S = 0.2f;
    /** Points every game starts with, and seconds of warm-up without drain or miss penalties. */
    private static final double START_POINTS = 25;
    private static final float WARMUP_S = 8f;
    private static final int FEVER_COMBO = 16;
    /** Fingers landing within this time of each other belong to the same Griff. */
    private static final float CHORD_GAP_S = 0.09f;
    private static final float CHORD_MAX_S = 0.22f;
    private static final int EFFECT_COUNT = 12;

    private static final int READY = 0, PLAYING = 1, OVER = 2;
    private static final int NO_OVERLAY = 0, SHOP = 1, TROPHY = 2, WARDROBE = 3;

    private static final int SHAPE_RECT = 0, SHAPE_CIRCLE = 1, SHAPE_HEART = 2, SHAPE_STAR = 3, SHAPE_PUFF = 4,
            SHAPE_TEXT = 5, SHAPE_BUBBLE = 6, SHAPE_COIN = 7;

    // Popo moves: short dances layered on top of the constant wobble.
    private static final int MOVE_NONE = 0, MOVE_HOPS = 1, MOVE_SHAKE = 2, MOVE_CIRCLE = 3, MOVE_SPIN = 4,
            MOVE_PENDULUM = 5, MOVE_JELLY = 6, MOVE_FLIP = 7, MOVE_MOONWALK = 8, MOVE_STOMP = 9, MOVE_SQUISH = 10,
            MOVE_PULSE = 11, MOVE_TWIST = 12, MOVE_WAVE = 13, MOVE_SIDEHOP = 14, MOVE_DIZZY = 15, MOVE_SPRING = 16,
            MOVE_SLIDE = 17, MOVE_BOW = 18, MOVE_DOUBLEHOP = 19, MOVE_HULA = 20, MOVE_TAPFOOT = 21, MOVE_KICK = 22;
    private static final int[] SMALL_MOVES = {MOVE_HOPS, MOVE_SHAKE, MOVE_JELLY, MOVE_SQUISH, MOVE_TWIST, MOVE_WAVE,
            MOVE_DOUBLEHOP, MOVE_SPRING, MOVE_KICK};
    private static final int[] MEDIUM_MOVES = {MOVE_CIRCLE, MOVE_PENDULUM, MOVE_MOONWALK, MOVE_STOMP, MOVE_PULSE,
            MOVE_SIDEHOP, MOVE_DIZZY, MOVE_SLIDE, MOVE_HULA, MOVE_BOW};
    private static final int[] BIG_MOVES = {MOVE_SPIN, MOVE_FLIP, MOVE_HULA, MOVE_SLIDE};
    private static final int[] IDLE_MOVES = {MOVE_TAPFOOT, MOVE_PENDULUM, MOVE_WAVE, MOVE_BOW, MOVE_KICK, MOVE_HOPS};

    // Outfits: shorts colour, pattern and sneaker colour change with every award.
    private static final int[] SHORTS_COLORS = {0xFF3F7FE0, 0xFFE53935, 0xFF43A047, 0xFF8E24AA, 0xFFFF9800, 0xFF00ACC1, 0xFFEC407A, 0xFF212121};
    private static final int[] SNEAKER_COLORS = {0xFFE53935, 0xFF1E88E5, 0xFFFFFFFF, 0xFFFDD835, 0xFF43A047, 0xFF212121, 0xFFFF7043};
    private static final int PATTERNS = 8;

    private static class Particle {
        float x, y, vx, vy, rot, vr, size, grow, gravity, drag = 1f, age, life, delay;
        int color, shape, style;
        String label;
    }

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bg = new Paint();
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private final Random rnd = new Random();
    private final List<Particle> particles = new ArrayList<Particle>();
    private final List<float[]> pendingBursts = new ArrayList<float[]>();
    private final List<String[]> bannerQueue = new ArrayList<String[]>();
    private final HighscoreStore scores;
    private final Awards awards;
    private final Shop shop = new Shop();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY);
    private final NumberFormat intFormat = NumberFormat.getIntegerInstance(Locale.GERMANY);

    private final long start = System.nanoTime();
    private float now;
    private float energy = IDLE_ENERGY;
    private long lastFrame = start;

    // Game state.
    private int state = READY;
    private int overlay = NO_OVERLAY;
    private long pausedNanos;
    private int trophyTab;
    private int selectedAward = -1;
    private double counter;
    private double total;
    private float playTime;
    private float lastTap = -100f;
    /** Time of the beat the next tap is judged against, and whether it has already been hit. */
    private float nextBeat;
    private boolean nextBeatHit, chordHit;
    private int combo, bestCombo;
    private float lastHitAt = -10f;
    private int lastJudge;
    private final String versionLabel;

    // The Kanadier: a cut-out head (South Park style) that pops up and flaps its top half while talking.
    private android.graphics.Bitmap kanadierTop, kanadierBottom;
    private float kanadierAt = -100f;
    private String kanadierLine = "";
    private final float[] missTimes = new float[4];
    private int missIndex;
    private int nextAward;
    /** Klatscher of all earlier games; awards count these plus this game's total. */
    private double lifetimeBase;
    private int awardsThisGame;
    private float overAt;
    private int lastPlace;
    private boolean newRecord;
    private int taps;
    private int upgradesBought;
    private int critStreak;
    private boolean wasNegative, belowNine;
    private final float[] tapTimes = new float[32];
    private int tapIndex;
    private String bestChord;
    private double bestChordMult;

    // The Griff being collected right now.
    private boolean chordOpen;
    private int chordLeft, chordRight;
    private float chordStart, chordLast;
    private double chordGain;
    private String lastChordKey;
    private float lastChordAt = -100f;
    private int chordRepeat;

    // Fever mode: kicks in after a few seconds of very fast tapping.
    private boolean fever;
    private float feverStart;

    // Bonus star flying across the screen.
    private boolean starActive;
    private float starX, starY, starVX, starBaseY, starBorn, nextStarAt;
    private int starsCaught;

    // Popo move.
    private int move = MOVE_NONE;
    private float moveStart, moveDur = 1f, movePower, moveDir = 1f;
    private int movePrio;
    private float nextIdleAt;
    private final float[] legLift = new float[2];
    private int outfit;

    // Red hand prints that fade on the cheeks.
    private final float[][] prints = new float[8][];
    private int printIndex;

    // Effects (times in seconds; an effect is off once its time is in the past).
    private String bannerTop, bannerBottom;
    private int bannerColor = GOLD;
    private float bannerStart = -100f;
    private float crownUntil = -100f;
    private float discoUntil = -100f;
    private float rocketStart = -100f;
    private float giantStart = -100f;
    private float shakeUntil = -100f;
    private float flashUntil = -100f;
    private float punch;
    private float nextWordAt;

    // Goodies falling from the sky (see Goodies).
    private int goodie = -1;
    private float goodieX, goodieY, goodieBaseX, goodieBorn, nextGoodieAt;
    private int goodiesCaught, stinkDodged;
    /** playTime until which each timed goodie is active. */
    private final float[] boostUntil = new float[Goodies.ICONS.length];
    private boolean musicOn;

    // Boss popos: every couple of minutes the popo turns into a boss that has to be clapped away in time.
    private static final String[] BOSS_NAMES = {"Graf Gesäß", "Backzilla", "König Wabbel", "Der Eiserne Hintern",
            "Popozilla", "Baron von Backe"};
    private static final float BOSS_SECONDS = 20f;
    private boolean bossActive;
    private int bossHp, bossMaxHp, bossesBeaten;
    private float bossEnd, nextBossAt, bossHitAt = -10f;
    private String bossName = "";

    // Popo evolution: at these Klatscher in one game the popo transforms for the rest of the game.
    private static final long[] EVO_AT = {1000, 5000, 15000, 40000, 100000, 250000};
    private static final String[] EVO_NAMES = {"Bronze-Popo", "Silber-Popo", "Gold-Popo", "Regenbogen-Popo",
            "Galaxie-Popo", "Lava-Popo"};
    private static final int[] EVO_COLORS = {0xFFD7A26B, 0xFFCFD8DC, 0xFFFFD54F, 0xFFFF80AB, 0xFF5E35B1, 0xFFFF7043};
    /** Stages that summon a Mega-Boss right after the transformation (Gold and Galaxie). */
    private static final boolean[] EVO_MEGA = {false, false, true, false, true, false};
    private int evo;
    /** 0..1: how red the cheeks glow from lots of klatscher in a short time; cools down by itself. */
    private float heat;
    private float nextSteamWordAt;
    private float evoStart = -10f;
    private boolean megaBoss;

    private final Wardrobe wardrobe;
    private final Daily daily;
    private boolean dailyDoneNow, feverReached;
    private int symmetryCount;
    private long talerThisGame;
    private final RectF shareBtn = new RectF();
    private final RectF[][] wardrobeCells = new RectF[Wardrobe.ITEMS.length][];
    private static final String[] SHARE_LINES = {"Schlag das, wenn du kannst!", "Mein Popo ist schneller als deiner!",
            "Hier wird nicht gekleckert, hier wird geklatscht!", "Wackel-Weltmeister am Werk!",
            "Die Backen glühen noch!", "Wer klatscht mehr?"};
    private final android.content.SharedPreferences settings;

    // Layout.
    private float popoX, popoY, popoR, unit;
    private final RectF trophyBtn = new RectF();
    private final RectF shopBtn = new RectF();
    private final RectF musicBtn = new RectF();
    private final RectF card = new RectF();
    private final RectF closeBtn = new RectF();
    private final RectF[] shopRows = new RectF[Shop.NAMES.length];
    private final RectF[] tabs = {new RectF(), new RectF()};
    private float gridLeft, gridTop, gridCell;

    public WackelView(Context context) {
        super(context);
        scores = new HighscoreStore(context);
        awards = new Awards(context);
        String version;
        try {
            version = "v" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            version = "";
        }
        versionLabel = version;
        try {
            kanadierTop = android.graphics.BitmapFactory.decodeStream(context.getAssets().open("kanadier_oben.png"));
            kanadierBottom = android.graphics.BitmapFactory.decodeStream(context.getAssets().open("kanadier_unten.png"));
        } catch (java.io.IOException e) {
            kanadierTop = kanadierBottom = null;
        }
        settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        wardrobe = new Wardrobe(context);
        daily = new Daily(context);
        for (int cat = 0; cat < wardrobeCells.length; cat++) {
            wardrobeCells[cat] = new RectF[Wardrobe.ITEMS[cat].length];
            for (int i = 0; i < wardrobeCells[cat].length; i++) wardrobeCells[cat][i] = new RectF();
        }
        musicOn = settings.getBoolean("music", true);
        for (int i = 0; i < shopRows.length; i++) shopRows[i] = new RectF();
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        setHapticFeedbackEnabled(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bg.setShader(new RadialGradient(w / 2f, h / 2f, Math.max(w, h) * 0.7f,
                0xFFFFF176, 0xFFFFB300, Shader.TileMode.CLAMP));
        unit = Math.min(w, h * 0.8f);
        popoX = w / 2f;
        popoY = h * 0.43f;
        popoR = unit * 0.19f;
        float s = unit * 0.13f, m = unit * 0.04f, top = h * 0.035f;
        trophyBtn.set(w - s - m, top, w - m, top + s);
        shopBtn.set(m, top, m + s, top + s);
        float ms = s * 0.75f;
        musicBtn.set(w - m - ms, top + s + m * 0.5f, w - m, top + s + m * 0.5f + ms);
        card.set(unit * 0.05f, h * 0.1f, w - unit * 0.05f, h * 0.92f);
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_POINTER_DOWN) return true;
        int idx = event.getActionIndex();
        float x = event.getX(idx), y = event.getY(idx);
        boolean firstFinger = action == MotionEvent.ACTION_DOWN;

        if (overlay == SHOP) {
            for (int i = 0; i < shopRows.length; i++) {
                if (shopRows[i].contains(x, y)) {
                    buy(i);
                    return true;
                }
            }
            if (closeBtn.contains(x, y) || !card.contains(x, y)) overlay = NO_OVERLAY;
            return true;
        }
        if (overlay == WARDROBE) {
            for (int cat = 0; cat < wardrobeCells.length; cat++) {
                for (int i = 0; i < wardrobeCells[cat].length; i++) {
                    if (!wardrobeCells[cat][i].contains(x, y)) continue;
                    if (wardrobe.pick(cat, i)) {
                        Synth.melody(new float[]{659, 988}, 0.07f);
                        startMove(pick(MEDIUM_MOVES), 0.8f, 0.8f, 2);
                    } else {
                        vibrate(new long[]{0, 30, 40, 30});
                    }
                    return true;
                }
            }
            if (closeBtn.contains(x, y) || !card.contains(x, y)) overlay = NO_OVERLAY;
            return true;
        }
        if (overlay == TROPHY) {
            if (tabs[0].contains(x, y)) {
                trophyTab = 0;
            } else if (tabs[1].contains(x, y)) {
                trophyTab = 1;
            } else if (trophyTab == 1 && x >= gridLeft && y >= gridTop && x < gridLeft + gridCell * 10
                    && y < gridTop + gridCell * ((Awards.TOTAL + 9) / 10)) {
                int picked = (int) ((y - gridTop) / gridCell) * 10 + (int) ((x - gridLeft) / gridCell);
                if (picked < Awards.TOTAL) selectedAward = picked;
            } else {
                overlay = NO_OVERLAY;
            }
            return true;
        }
        if (firstFinger && musicBtn.contains(x, y)) {
            musicOn = !musicOn;
            settings.edit().putBoolean("music", musicOn).apply();
            if (musicOn) Music.start();
            else Music.stop();
            return true;
        }
        if (firstFinger && trophyBtn.contains(x, y)) {
            overlay = TROPHY;
            selectedAward = -1;
            return true;
        }
        if (firstFinger && shopBtn.contains(x, y)) {
            // The same corner is the shop while playing and the wardrobe otherwise.
            overlay = state == PLAYING ? SHOP : WARDROBE;
            return true;
        }
        if (state == OVER) {
            if (firstFinger && now - overAt > 1.2f) {
                if (shareBtn.contains(x, y)) shareRecord();
                else newGame();
            }
            return true;
        }
        if (state == READY) {
            // The starting tap only starts; the first beat comes a moment later.
            newGame();
            return true;
        }
        if (goodie >= 0 && Math.hypot(x - goodieX, y - goodieY) < unit * 0.1f) {
            catchGoodie();
            return true;
        }
        if (starActive && Math.hypot(x - starX, y - starY) < unit * 0.11f) {
            catchStar();
            return true;
        }
        klatsch(x, y);
        return true;
    }

    /** Called by the activity when it comes to the front or leaves it. */
    public void setMusicActive(boolean active) {
        if (active && musicOn) Music.start();
        else Music.stop();
    }

    /** True while a round is running; the update pill hides then. */
    public boolean isPlaying() {
        return state == PLAYING;
    }

    /** True on the start screen, the only place the update pill shows. */
    public boolean isOnStartScreen() {
        return state == READY;
    }

    private void newGame() {
        state = PLAYING;
        counter = START_POINTS;
        total = 0;
        playTime = 0;
        lifetimeBase = awards.lifetime();
        nextAward = Awards.firstAbove(lifetimeBase);
        awardsThisGame = 0;
        taps = 0;
        upgradesBought = 0;
        critStreak = 0;
        wasNegative = false;
        belowNine = false;
        bestChord = null;
        bestChordMult = 0;
        chordOpen = false;
        chordRepeat = 0;
        fever = false;
        starActive = false;
        starsCaught = 0;
        goodie = -1;
        goodiesCaught = 0;
        bossActive = false;
        bossesBeaten = 0;
        evo = 0;
        nextBeat = now + 1.5f;
        nextBeatHit = false;
        combo = 0;
        bestCombo = 0;
        heat = 0;
        evoStart = -10f;
        megaBoss = false;
        nextBossAt = 75f + rnd.nextFloat() * 20f;
        dailyDoneNow = false;
        feverReached = false;
        symmetryCount = 0;
        talerThisGame = 0;
        stinkDodged = 0;
        nextGoodieAt = 10f + rnd.nextFloat() * 8f;
        java.util.Arrays.fill(boostUntil, 0f);
        nextStarAt = 20f + rnd.nextFloat() * 15f;
        shop.reset();
        crownUntil = -100f;
        outfit = rnd.nextInt(1000);
        int games = awards.countGame();
        if (games >= 10) special(Awards.REGULAR);
        if (games >= 50) special(Awards.VETERAN);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 4) special(Awards.NIGHT_OWL);
        if (hour >= 5 && hour < 7) special(Awards.EARLY_BIRD);
        queueBanner("Finde den Takt!", "Klatsch, wenn der Ring sich schließt", Color.WHITE);
    }

    private void klatsch(float x, float y) {
        if (chordOpen && now - chordStart > CHORD_MAX_S) resolveChord();
        boolean left = x < popoX;
        prints[printIndex++ % prints.length] = new float[]{left ? -1 : 1, rnd.nextFloat() - 0.5f, rnd.nextFloat() - 0.5f,
                (rnd.nextFloat() - 0.5f) * 50f, now};
        if (chordOpen) {
            if (chordHit) {
                tapBurst(x, y);
                if (bossActive) hitBoss();
            }
            // Another finger of the same grip: it raises the grip's multiplier, no points of its own.
            if (left) chordLeft++;
            else chordRight++;
            chordLast = now;
            return;
        }

        lastTap = now;
        taps++;
        tapTimes[tapIndex++ % tapTimes.length] = now;
        if (tapRate(1f) >= 12) special(Awards.TAP_MACHINE);

        // Judge the timing against the beat ring.
        float off = Math.abs(now - nextBeat);
        lastJudge = nextBeatHit || off > GOOD_S ? 0 : (off <= PERFECT_S ? 2 : 1);
        chordOpen = true;
        chordStart = now;
        chordLast = now;
        chordLeft = left ? 1 : 0;
        chordRight = left ? 0 : 1;
        chordHit = lastJudge > 0;
        if (!chordHit) {
            // Off the beat: costs what a tap would bring and breaks the combo, so hammering does not pay.
            // Half a tap and nothing during warm-up, so a few slips never end a game but hammering still drains.
            double loss = playTime < WARMUP_S ? 0 : shop.tapGain(false) * 0.5;
            counter -= loss;
            chordGain = 0;
            if (loss > 0) floatText("−" + fmt(loss), x, y, 0xFFB0BEC5, 0.8f, 0);
            word("Daneben", popoX, popoY - popoR * 1.35f, 0xFFB0BEC5, 0.8f, 0);
            breakCombo();
            // Four misses within two seconds is hammering: time for the Kanadier.
            missTimes[missIndex++ % missTimes.length] = now;
            boolean hammering = true;
            for (float t : missTimes) hammering &= now - t < 2f;
            if (hammering) kanadier("Einfach mal NEIN sagen!");
            return;
        }
        nextBeatHit = true;
        lastHitAt = now;
        combo++;
        bestCombo = Math.max(bestCombo, combo);
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        tapBurst(x, y);
        if (bossActive) hitBoss();
        if (combo == FEVER_COMBO) startFever();
        double timing = lastJudge == 2 ? 3 : 1.5;
        double comboMult = Math.min(3, 1 + 0.1 * combo);
        double gain = shop.tapGain(combo >= 5) * timing * comboMult * (fever ? FEVER_BONUS : 1) * (boosted(Goodies.CHOCO) ? 2 : 1);
        boolean crit = rnd.nextDouble() < shop.critChance() + (boosted(Goodies.CLOVER) ? 0.25 : 0);
        if (crit) {
            gain *= CRIT_FACTOR;
            critStreak++;
            if (critStreak >= 3) special(Awards.LUCKY);
        } else {
            critStreak = 0;
        }
        counter += gain;
        total += gain;
        energy = Math.min(energy + 0.6f, 1.8f);
        heat = Math.min(1f, heat + 0.07f);
        chordGain = gain;

        if (taps >= 1000) special(Awards.TAPS_1000);
        if (taps >= 5000) special(Awards.TAPS_5000);

        startMove(pick(SMALL_MOVES), 0.45f, 0.6f + rnd.nextFloat() * 0.4f, 0);

        if (evo >= 4) {
            Particle p = spawn(x, y, SHAPE_STAR, evo == 4 ? PARTY[rnd.nextInt(PARTY.length)] : EVO_COLORS[evo - 1], popoR * 0.06f, 0.8f);
            p.vy = -popoR * 1.5f;
            p.vr = 300f;
        }
        floatText((crit ? "KRIT! +" : "+") + fmt(gain), x, y, crit ? GOLD : Color.WHITE, crit ? 1.3f : 0.9f, crit ? 2 : 0);
        word(lastJudge == 2 ? "PERFEKT!" : "Gut", popoX, popoY - popoR * 1.35f, lastJudge == 2 ? GOLD : Color.WHITE,
                lastJudge == 2 ? 1f : 0.8f, 0);
        if (combo % 10 == 0) {
            word("Combo x" + combo + "! " + Words.pick(Words.PRAISE, rnd), popoX, popoY - popoR * 1.75f, GOLD, 1.1f, 5);
            Synth.melody(new float[]{784, 1047}, 0.06f);
        }
    }

    /** A tiny burst at each finger, in a different flavour each time. */
    private void tapBurst(float x, float y) {
        int kind = rnd.nextInt(5);
        int[] shapes = {SHAPE_STAR, SHAPE_HEART, SHAPE_RECT, SHAPE_CIRCLE, SHAPE_PUFF};
        for (int i = 0; i < 3; i++) {
            Particle p = spawn(x, y, shapes[kind], kind == 4 ? Color.WHITE : PARTY[rnd.nextInt(PARTY.length)],
                    popoR * (kind == 4 ? 0.08f : 0.05f + 0.03f * rnd.nextFloat()), 0.6f);
            double a = rnd.nextFloat() * Math.PI * 2;
            float sp = popoR * (1.5f + 2f * rnd.nextFloat());
            p.vx = (float) Math.cos(a) * sp;
            p.vy = (float) Math.sin(a) * sp;
            p.drag = 3f;
            p.vr = (rnd.nextFloat() - 0.5f) * 600f;
            if (kind == 4) p.grow = popoR * 0.2f;
        }
    }

    private int lastPicked = -1;

    /** Picks a move from the set, never the same one twice in a row. */
    private int pick(int[] moves) {
        int m;
        do {
            m = moves[rnd.nextInt(moves.length)];
        } while (m == lastPicked && moves.length > 1);
        lastPicked = m;
        return m;
    }

    /** Taps per second over the last window seconds. */
    private float tapRate(float window) {
        int n = 0;
        for (float t : tapTimes) if (now - t <= window) n++;
        return n / window;
    }

    private void resolveChord() {
        chordOpen = false;
        Chords.Chord chord = Chords.of(chordLeft, chordRight);
        if (chord == null || !chordHit) return;
        chordRepeat = chord.key.equals(lastChordKey) && now - lastChordAt < 2.5f ? chordRepeat + 1 : 1;
        lastChordKey = chord.key;
        lastChordAt = now;
        double repeatBonus = 1 + 0.05 * (Math.min(chordRepeat, 5) - 1);
        double bonus = chordGain * (chord.multiplier * repeatBonus - 1);
        counter += bonus;
        total += bonus;
        if (chord.multiplier > bestChordMult) {
            bestChordMult = chord.multiplier;
            bestChord = chord.name;
        }

        // Grips happen constantly with many fingers, so they get a small show; the name only pops up
        // when the grip changes, and the series label only at 3 and 5 in a row.
        if (chordRepeat == 1) {
            String label = chord.name + "  x" + fmtMult(chord.multiplier);
            word(label, popoX, popoY - popoR * (0.5f + 0.1f * chord.tier), PARTY[rnd.nextInt(PARTY.length)],
                    1f + 0.1f * chord.tier, chord.tier >= 2 ? 1 : 0);
        } else if (chordRepeat == 3 || chordRepeat == 5) {
            word("Serie x" + chordRepeat + "!", popoX, popoY + popoR * 1.9f, GOLD, 1f, 6);
        }
        floatText("+" + fmt(bonus), popoX, popoY + popoR * 0.5f, GREEN, 1f, 0);

        switch (chord.tier) {
            case 0:
                confetti(4 + chordLeft + chordRight);
                break;
            case 1:
                confetti(12);
                startMove(pick(MEDIUM_MOVES), 0.8f, 0.6f, 1);
                break;
            case 2:
                confetti(20);
                startMove(pick(MEDIUM_MOVES), 0.9f, 0.8f, 2);
                if (chordRepeat == 1) Synth.melody(new float[]{523, 784}, 0.06f);
                break;
            default:
                confetti(30);
                punch = 0.04f;
                startMove(pick(MEDIUM_MOVES), 1.0f, 1f, 2);
                if (chordRepeat == 1) Synth.melody(new float[]{523, 659, 784}, 0.06f);
                break;
        }

        special(Awards.TWO_FINGERS);
        int lo = Math.min(chordLeft, chordRight), hi = Math.max(chordLeft, chordRight);
        if (lo == 2 && hi == 2) {
            special(Awards.SYMMETRY);
            symmetryCount++;
        }
        if (lo == 1 && hi == 3) special(Awards.PISA);
        if (lo == 3 && hi == 3) special(Awards.WITCH);
        if (lo == 4 && hi == 4) special(Awards.OCTOPUS);
        if (lo == 5 && hi == 5) special(Awards.TEN_FINGERS);
        if (chordRepeat >= 5) special(Awards.RHYTHM);
    }

    private void catchStar() {
        starActive = false;
        starsCaught++;
        double bonus = Math.max(20, 6 * drainRate() + 8 * shop.tapGain(false));
        counter += bonus;
        total += bonus;
        queueBanner("STERN GEFANGEN!", "+" + fmt(bonus), GOLD);
        sparkles(50);
        for (int i = 0; i < 40; i++) {
            Particle p = spawn(starX, starY, SHAPE_STAR, i % 2 == 0 ? GOLD : Color.WHITE, popoR * 0.1f, 1.2f);
            double a = rnd.nextFloat() * Math.PI * 2;
            float sp = popoR * (2 + 4 * rnd.nextFloat());
            p.vx = (float) Math.cos(a) * sp;
            p.vy = (float) Math.sin(a) * sp;
            p.vr = 300f;
        }
        Synth.melody(new float[]{1047, 1319, 1568, 2093}, 0.06f);
        vibrate(new long[]{0, 50, 30, 50});
        special(Awards.STAR);
        if (starsCaught >= 5) special(Awards.STARS_5);
    }

    private void buy(int i) {
        if (shop.maxed(i)) return;
        long price = shop.price(i);
        if (counter < price) {
            vibrate(new long[]{0, 30, 40, 30});
            return;
        }
        counter -= price;
        shop.buy(i);
        upgradesBought++;
        Synth.melody(new float[]{659, 988}, 0.08f);
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        word(Words.pick(Words.BUY, rnd), getWidth() / 2f, card.top + unit * 0.3f, GREEN, 1f, 0);
        if (upgradesBought >= 25) special(Awards.SHOPPER);
        if (shop.maxed(i)) special(Awards.MAXED);
        if (shop.level(Shop.GOLD) >= 5) special(Awards.GOLDEN);
    }

    private void update(float dt) {
        if (chordOpen && now - chordLast > CHORD_GAP_S) resolveChord();
        if (state != OVER && now - lastTap > 3f && now >= nextIdleAt && now > moveStart + moveDur) {
            startMove(pick(IDLE_MOVES), 1.2f, 0.5f, 0);
            nextIdleAt = now + 2.5f + rnd.nextFloat() * 2f;
        }
        if (state != PLAYING) return;
        playTime += dt;
        double auto = autoRate();
        counter += (auto - (playTime < WARMUP_S ? 0 : drainRate())) * dt;
        total += auto * dt;
        while (nextAward < Awards.COUNT && lifetimeBase + total >= Awards.threshold(nextAward)) award(nextAward++);

        updateBeat();
        updateFever(dt);
        heat = Math.max(0f, heat - dt * 0.12f);
        if (heat > 0.85f) {
            // Glowing hot: little steam clouds rise off the cheeks.
            if (rnd.nextFloat() < dt * 12) {
                Particle p = spawn(popoX + (rnd.nextFloat() - 0.5f) * popoR * 2.2f, popoY - popoR * 0.6f, SHAPE_PUFF,
                        0xCCFFFFFF, popoR * 0.08f, 1f);
                p.vy = -popoR * 1.8f;
                p.grow = popoR * 0.25f;
            }
            if (now >= nextSteamWordAt) {
                nextSteamWordAt = now + 6f;
                String[] hot = {"Aua, heiß!", "Feuerrot!", "Glühpopo!", "Tomaten-Alarm!", "Der brennt!"};
                word(hot[rnd.nextInt(hot.length)], popoX, popoY - popoR * 1.5f, 0xFFFF1744, 1.2f, rnd.nextInt(Words.STYLES));
            }
        }
        updateStar(dt);
        updateGoodie(dt);
        while (evo < EVO_AT.length && total >= EVO_AT[evo]) evolve(evo++);
        updateBoss();
        updateDaily();

        if (counter < 0) {
            wasNegative = true;
            if (counter < -9) belowNine = true;
            if (now >= nextWordAt) {
                nextWordAt = now + 1.3f;
                word(Words.pick(Words.DANGER, rnd), popoX + (rnd.nextFloat() - 0.5f) * popoR * 2, popoY - popoR * 1.3f, RED, 1.1f, 4);
            }
        } else if (belowNine) {
            belowNine = false;
            special(Awards.CLOSE_CALL);
            word(Words.pick(Words.RESCUE, rnd), popoX, popoY - popoR * 1.4f, GREEN, 1.4f, 2);
        }
        if (wasNegative && counter >= 1000) special(Awards.COMEBACK);
        if (upgradesBought == 0 && counter >= 500) special(Awards.MISER);
        if (counter >= 1e6) special(Awards.SCROOGE);
        if (playTime >= 180) special(Awards.SURVIVE_3);
        if (playTime >= 600) special(Awards.SURVIVE_10);
        if (playTime >= 1200) special(Awards.SURVIVE_20);

        if (counter <= GAME_OVER_AT) gameOver();
    }

    /** Fever starts at a combo of 16 and lasts until the combo breaks. */
    private void startFever() {
        fever = true;
        feverStart = now;
        queueBanner("FIEBER-MODUS!", "Combo " + FEVER_COMBO + ": alles x1,25, bleib im Takt!", RED);
        Synth.melody(new float[]{523, 659, 784, 1047}, 0.06f);
        special(Awards.FEVER);
        feverReached = true;
    }

    /** A missed beat or a tap off the beat ends the combo (and with it the fever). */
    private void breakCombo() {
        if (combo >= 10) word("Combo weg!", popoX, popoY - popoR * 1.75f, 0xFFB0BEC5, 1f, 0);
        combo = 0;
        if (fever) {
            fever = false;
            word("Fieber vorbei", popoX, popoY - popoR * 1.55f, Color.WHITE, 0.9f, 0);
        }
    }

    /** Seconds between beats: calm at first, getting faster the longer you last. */
    private float beatInterval() {
        return Math.max(0.45f, 1.0f - playTime * 0.002f);
    }

    /** Lets the Kanadier pop up with a line, at most every 12 seconds. */
    private void kanadier(String line) {
        if (kanadierTop == null || now - kanadierAt < 12f) return;
        kanadierAt = now;
        kanadierLine = line;
        Synth.melody(new float[]{196, 0, 196, 247}, 0.08f);
    }

    private void updateBeat() {
        while (now > nextBeat + GOOD_S) {
            if (!nextBeatHit && combo > 0) breakCombo();
            nextBeat += beatInterval();
            nextBeatHit = false;
        }
    }

    private void updateFever(float dt) {
        if (!fever) return;
        if (now - feverStart >= 30f) special(Awards.LONG_FEVER);
        if (rnd.nextFloat() < dt * 8) {
            Particle p = spawn(popoX + (rnd.nextFloat() - 0.5f) * popoR * 3, popoY + popoR * 0.6f, SHAPE_PUFF,
                    PARTY[rnd.nextInt(3)], popoR * 0.1f, 1f);
            p.vy = -popoR * 2.5f;
            p.grow = popoR * 0.15f;
        }
    }

    private void updateStar(float dt) {
        if (!starActive && playTime >= nextStarAt) {
            starActive = true;
            starBorn = now;
            boolean fromLeft = rnd.nextBoolean();
            starX = fromLeft ? -unit * 0.1f : getWidth() + unit * 0.1f;
            starBaseY = getHeight() * (0.18f + 0.4f * rnd.nextFloat());
            starVX = (getWidth() + unit * 0.2f) / 4.5f * (fromLeft ? 1 : -1);
            nextStarAt = playTime + 60f + rnd.nextFloat() * 30f;
            Synth.melody(new float[]{1568, 2093}, 0.05f);
        }
        if (!starActive) return;
        starX += starVX * dt;
        starY = starBaseY + (float) Math.sin((now - starBorn) * 3) * unit * 0.06f;
        if (rnd.nextFloat() < 0.6f) {
            Particle p = spawn(starX, starY, SHAPE_STAR, rnd.nextBoolean() ? GOLD : Color.WHITE, unit * 0.015f, 0.6f);
            p.vy = unit * 0.05f;
        }
        if (now - starBorn > 5f) starActive = false;
    }

    /** How loud the music should be right now: quiet in menus, louder when things heat up. */
    private int musicSong() {
        if (state != PLAYING) return MetalLoop.MENU;
        return fever || bossActive ? MetalLoop.FEVER : MetalLoop.GAME;
    }

    private float musicLevel() {
        if (overlay != NO_OVERLAY) return 0.15f;
        if (state == PLAYING && bossActive) return 0.85f;
        if (state == READY) return 0.35f;
        if (state == OVER) return 0.25f;
        if (fever || now < discoUntil) return 0.75f;
        if (counter < 0) return 0.6f;
        return 0.3f + 0.25f * Math.min(1f, tapRate(2f) / 6f);
    }

    private void updateBoss() {
        if (!bossActive && goodie < 0 && playTime >= nextBossAt) {
            bossActive = true;
            bossMaxHp = bossHp = (15 + 5 * bossesBeaten) * (megaBoss ? 2 : 1);
            bossEnd = playTime + BOSS_SECONDS;
            bossName = (megaBoss ? "MEGA-" : "") + BOSS_NAMES[rnd.nextInt(BOSS_NAMES.length)];
            queueBanner(megaBoss ? "MEGA-BOSS!" : "BOSS-POPO!", bossName + " · " + bossMaxHp + " Treffer im Takt in 20 s!", PURPLE);
            Synth.melody(new float[]{98, 0, 98, 0, 117, 98}, 0.1f);
            shakeUntil = now + 0.6f;
            vibrate(new long[]{0, 200, 80, 200});
        }
        if (bossActive && playTime >= bossEnd) {
            bossActive = false;
            nextBossAt = playTime + 100f + rnd.nextFloat() * 40f;
            megaBoss = false;
            word("Boss entkommen!", popoX, popoY - popoR * 1.5f, Color.WHITE, 1.2f, 4);
            Synth.melody(new float[]{392, 370, 349, 330}, 0.12f);
        }
    }

    /** The popo transforms into the next stage: new colour, aura and a big show; some stages call a Mega-Boss. */
    private void evolve(int stage) {
        evoStart = now;
        queueBanner("POPO-EVOLUTION!", "✨ " + EVO_NAMES[stage] + " ✨", EVO_COLORS[stage]);
        flashUntil = now + 0.35f;
        punch = 0.1f;
        startMove(MOVE_SPIN, 1.2f, 1f, 4);
        for (int i = 0; i < 70; i++) {
            Particle p = spawn(popoX, popoY, i % 3 == 0 ? SHAPE_STAR : SHAPE_CIRCLE, i % 2 == 0 ? EVO_COLORS[stage] : Color.WHITE,
                    popoR * (0.05f + 0.05f * rnd.nextFloat()), 1.4f);
            double a = rnd.nextFloat() * Math.PI * 2;
            float sp = popoR * (2.5f + 4f * rnd.nextFloat());
            p.vx = (float) Math.cos(a) * sp;
            p.vy = (float) Math.sin(a) * sp;
            p.drag = 1.5f;
            p.vr = 400f;
        }
        Synth.melody(new float[]{262, 330, 392, 523, 659, 784, 1047}, 0.08f);
        vibrate(new long[]{0, 120, 60, 120, 60, 400});
        if (stage == 2) special(Awards.EVO_GOLD);
        if (stage == 5) special(Awards.EVO_LAVA);
        if (EVO_MEGA[stage] && !bossActive) {
            megaBoss = true;
            nextBossAt = playTime + 3f;
        }
    }

    /** Every finger on the popo takes one point off the boss. */
    private void hitBoss() {
        bossHp--;
        bossHitAt = now;
        if (bossHp > 0) return;
        bossActive = false;
        bossesBeaten++;
        nextBossAt = playTime + 100f + rnd.nextFloat() * 40f;
        double bonus = (25 * drainRate() + 20 * shop.tapGain(false)) * (megaBoss ? 2 : 1);
        counter += bonus;
        total += bonus;
        long taler = (10 + 5L * bossesBeaten) * (megaBoss ? 2 : 1);
        if (megaBoss) special(Awards.MEGA_BOSS);
        megaBoss = false;
        wardrobe.addTaler(taler);
        talerThisGame += taler;
        String loot = rnd.nextFloat() < 0.35f ? wardrobe.gift(rnd) : null;
        queueBanner("BOSS BESIEGT!", "+" + fmt(bonus) + " · +" + taler + " Taler" + (loot != null ? " · Beute: " + loot : ""), GOLD);
        confetti(90);
        for (int k = 0; k < 4; k++) {
            pendingBursts.add(new float[]{now + k * 0.25f, getWidth() * (0.15f + 0.7f * rnd.nextFloat()),
                    getHeight() * (0.12f + 0.3f * rnd.nextFloat())});
        }
        crownUntil = now + 15f;
        startMove(MOVE_FLIP, 1.2f, 1f, 4);
        Synth.melody(new float[]{392, 523, 659, 784, 1047, 1319, 1568}, 0.09f);
        vibrate(new long[]{0, 80, 50, 80, 50, 300});
        special(Awards.BOSS_1);
        if (bossesBeaten >= 3) special(Awards.BOSS_3);
    }

    /** How far today's challenge is in this game. */
    private double dailyProgress() {
        switch (daily.kind()) {
            case Daily.GOODIES:
                return goodiesCaught;
            case Daily.SURVIVE:
                return playTime;
            case Daily.KLATSCHER:
                return total;
            case Daily.BOSS:
                return bossesBeaten;
            case Daily.SYMMETRY:
                return symmetryCount;
            case Daily.FEVER:
                return feverReached ? 1 : 0;
            case Daily.UPGRADES:
                return upgradesBought;
            case Daily.NO_SHOP:
                return upgradesBought == 0 ? playTime : 0;
            case Daily.STARS:
                return starsCaught;
            default:
                return stinkDodged;
        }
    }

    private void updateDaily() {
        if (dailyDoneNow || daily.doneToday()) return;
        if (dailyProgress() < Daily.GOALS[daily.kind()]) return;
        dailyDoneNow = true;
        int taler = daily.complete();
        wardrobe.addTaler(taler);
        talerThisGame += taler;
        queueBanner("TAGES-CHALLENGE GESCHAFFT!", "+" + taler + " Taler · 🔥 " + daily.streak() + " Tage in Folge", GREEN);
        confetti(60);
        Synth.melody(new float[]{523, 659, 784, 1047}, 0.09f);
    }

    /** Paints a picture of this game's result and offers it to WhatsApp and co. */
    private void shareRecord() {
        try {
            int bw = 1080, bh = 1350;
            android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(bw, bh, android.graphics.Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            Paint grad = new Paint();
            grad.setShader(new RadialGradient(bw / 2f, bh / 2f, bh * 0.7f, 0xFFFFF176, 0xFFFFB300, Shader.TileMode.CLAMP));
            c.drawRect(0, 0, bw, bh, grad);
            drawOutlined(c, "Wackel-Popo", bw / 2f, 150, 110, INK, 0);
            float savedEnergy = energy;
            energy = 0.8f;
            drawPopo(c, bw / 2f, 560, 210, 1.2f);
            energy = savedEnergy;
            drawOutlined(c, fmt(total) + " Klatscher", bw / 2f, 1010, fit(fmt(total) + " Klatscher", 120, bw * 0.9f), GOLD, 0);
            String line1 = newRecord ? "NEUER REKORD!" : (lastPlace > 0 ? "Platz " + lastPlace + " in meiner Bestenliste" : "Gleich nochmal!");
            drawOutlined(c, line1, bw / 2f, 1120, fit(line1, 64, bw * 0.9f), Color.WHITE, 0);
            String quote = SHARE_LINES[rnd.nextInt(SHARE_LINES.length)];
            drawOutlined(c, quote, bw / 2f, 1240, fit(quote, 56, bw * 0.9f), INK, 0);
            java.io.FileOutputStream out = new java.io.FileOutputStream(ShareProvider.file(getContext()));
            bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmp.recycle();

            android.content.Intent send = new android.content.Intent(android.content.Intent.ACTION_SEND);
            send.setType("image/png");
            send.putExtra(android.content.Intent.EXTRA_STREAM, ShareProvider.URI);
            send.putExtra(android.content.Intent.EXTRA_TEXT, "Ich hab " + fmt(total) + " Klatscher beim Wackel-Popo geschafft! " + quote);
            send.setClipData(android.content.ClipData.newRawUri("", ShareProvider.URI));
            send.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            getContext().startActivity(android.content.Intent.createChooser(send, "Rekord teilen"));
        } catch (Exception e) {
            word("Teilen klappt gerade nicht", popoX, popoY, RED, 1f, 0);
        }
    }

    private boolean boosted(int g) {
        return state == PLAYING && playTime < boostUntil[g];
    }

    private double autoRate() {
        return shop.autoRate() + (boosted(Goodies.ROBOT) ? 3 * shop.goldMultiplier() : 0);
    }

    /** Drops a goodie every 15-30 seconds; it floats down on a parachute for about 5 seconds. */
    private void updateGoodie(float dt) {
        float landY = getHeight() * 0.7f;
        if (goodie < 0 && playTime >= nextGoodieAt) {
            goodie = Goodies.random(rnd);
            goodieBorn = now;
            goodieBaseX = getWidth() * (0.18f + 0.64f * rnd.nextFloat());
            goodieY = -unit * 0.12f;
            nextGoodieAt = playTime + 30f + rnd.nextFloat() * 20f;
            Synth.melody(new float[]{1319, 1047, 1319}, 0.05f);
        }
        if (goodie < 0) return;
        goodieY += (landY + unit * 0.12f) / 5f * dt;
        goodieX = goodieBaseX + (float) Math.sin((now - goodieBorn) * 2.2f) * unit * 0.06f;
        if (goodieY >= landY) {
            // Missed: it lands with a little puff.
            for (int i = 0; i < 8; i++) {
                Particle p = spawn(goodieX, landY, SHAPE_PUFF, Color.WHITE, unit * 0.02f, 0.6f);
                p.vx = (rnd.nextFloat() - 0.5f) * unit * 0.6f;
                p.grow = unit * 0.05f;
            }
            if (goodie == Goodies.STINK) {
                word("Puh, ausgewichen!", goodieX, landY - unit * 0.05f, GREEN, 0.9f, 0);
                if (++stinkDodged >= 3) special(Awards.DODGER);
            }
            goodie = -1;
        }
    }

    private void catchGoodie() {
        int g = goodie;
        goodie = -1;
        String effect = Goodies.EFFECTS[g];
        int color = GOLD;
        switch (g) {
            case Goodies.PEACH: {
                double bonus = Math.max(15, 4 * drainRate() + 6 * shop.tapGain(false));
                counter += bonus;
                total += bonus;
                effect = "+" + fmt(bonus);
                hearts(12);
                break;
            }
            case Goodies.GIFT: {
                int best = -1;
                for (int i = 0; i < Shop.NAMES.length; i++) {
                    if (!shop.maxed(i) && (best < 0 || shop.price(i) < shop.price(best))) best = i;
                }
                if (best >= 0) {
                    shop.buy(best);
                    upgradesBought++;
                    effect = "Gratis: " + Shop.NAMES[best] + "!";
                } else {
                    counter += 100;
                    total += 100;
                    effect = "+100";
                }
                confetti(25);
                special(Awards.PRESENT);
                break;
            }
            case Goodies.STINK:
                counter -= Math.max(0, counter * 0.15);
                color = 0xFF7CB342;
                Synth.pups();
                for (int i = 0; i < 14; i++) {
                    Particle p = spawn(goodieX, goodieY, SHAPE_PUFF, 0xFF9CCC65, unit * 0.03f, 1.2f);
                    p.vx = (rnd.nextFloat() - 0.5f) * unit * 0.5f;
                    p.vy = -unit * 0.1f * rnd.nextFloat();
                    p.grow = unit * 0.08f;
                }
                special(Awards.NOSE_CLIP);
                kanadier("Bei Stinkbomben: Einfach mal NEIN sagen!");
                break;
            case Goodies.MAGNET:
                nextStarAt = playTime;
                sparkles(12);
                break;
            default:
                boostUntil[g] = playTime + Goodies.SECONDS[g];
                sparkles(12);
                break;
        }
        if (g != Goodies.STINK) {
            Synth.melody(new float[]{784, 1047, 1319}, 0.06f);
            goodiesCaught++;
            if (goodiesCaught >= 10) special(Awards.GOODIE_HUNTER);
        }
        word(Goodies.ICONS[g] + " " + Goodies.NAMES[g], goodieX, goodieY - unit * 0.04f, color, 1f, 1);
        floatText(effect, goodieX, goodieY + unit * 0.05f, Color.WHITE, 1f, 0);
    }

    private double drainRate() {
        if (boosted(Goodies.ICE)) return 0;
        return BASE_DRAIN * Math.pow(DRAIN_GROWTH, playTime) * shop.drainFactor();
    }

    private void gameOver() {
        state = OVER;
        overlay = NO_OVERLAY;
        overAt = now;
        fever = false;
        starActive = false;
        goodie = -1;
        bossActive = false;
        talerThisGame += (long) (total / 500);
        wardrobe.addTaler((long) (total / 500));
        awards.setLifetime(lifetimeBase + total);
        int score = (int) Math.min(Integer.MAX_VALUE, total);
        int best = scores.best();
        lastPlace = score > 0 ? scores.add(score, System.currentTimeMillis()) : 0;
        newRecord = lastPlace == 1 && score > best;
        if (newRecord) {
            confetti(160);
            Synth.melody(new float[]{523, 659, 784, 1047, 0, 784, 1047}, 0.13f);
        } else {
            Synth.melody(new float[]{523, 494, 466, 440, 0, 330}, 0.16f);
        }
        vibrate(new long[]{0, 400});
    }

    // ---------------------------------------------------------------- awards

    private void award(int i) {
        awards.setLifetime(lifetimeBase + total);
        if (!awards.unlock(i)) return;
        awardsThisGame++;
        queueBanner("AWARD " + (i + 1) + " · NEU!", Awards.NAMES[i], PARTY[i % PARTY.length]);
        vibrate(new long[]{0, 80, 70, 80, 70, 250});
        if (awards.normalUnlockedCount() >= 50) special(Awards.COLLECTOR);
        int effect = i % EFFECT_COUNT;
        float power = 1f + i / 60f;
        if (effect != 1) {
            Synth.melody(i >= 90 ? new float[]{392, 523, 659, 784, 1047, 1319, 1568}
                    : new float[]{392, 523, 659, 784, 659, 1047}, 0.11f);
        }
        startMove(pick(BIG_MOVES), 1.1f, 1f, 3);
        outfit++;
        switch (effect) {
            case 0:
                confetti((int) (150 * power));
                break;
            case 1:
                Synth.pups();
                rainbowPuffs((int) (40 * power));
                break;
            case 2:
                crownUntil = now + 20f;
                sparkles((int) (40 * power));
                break;
            case 3:
                discoUntil = now + 5f + power;
                break;
            case 4:
                rocketStart = now;
                break;
            case 5:
                for (int k = 0; k < (int) (5 * power); k++) {
                    pendingBursts.add(new float[]{now + k * 0.3f,
                            getWidth() * (0.15f + 0.7f * rnd.nextFloat()), getHeight() * (0.1f + 0.35f * rnd.nextFloat())});
                }
                break;
            case 6:
                hearts((int) (60 * power));
                break;
            case 7:
                giantStart = now;
                sparkles((int) (30 * power));
                break;
            case 8:
                shootingStars((int) (12 * power));
                break;
            case 9:
                bubbles((int) (50 * power));
                break;
            case 10:
                coins((int) (60 * power));
                break;
            default:
                shakeUntil = now + 1.5f;
                Synth.melody(new float[]{98, 0, 87, 0, 98}, 0.12f);
                break;
        }
    }

    /** Unlocks a special award; the big show only happens the first time. */
    private void special(int k) {
        if (!awards.unlock(Awards.COUNT + k)) return;
        queueBanner("★ SPEZIAL-AWARD ★", Awards.SPECIALS[k][0], PURPLE);
        flashUntil = now + 0.3f;
        punch = 0.1f;
        confetti(120);
        sparkles(40);
        for (int i = 0; i < 3; i++) {
            pendingBursts.add(new float[]{now + 0.2f + i * 0.25f, getWidth() * (0.2f + 0.6f * rnd.nextFloat()),
                    getHeight() * (0.15f + 0.25f * rnd.nextFloat())});
        }
        Synth.melody(new float[]{784, 988, 1175, 1568, 0, 1568, 2093}, 0.09f);
        vibrate(new long[]{0, 100, 60, 100, 60, 300});
    }

    private void queueBanner(String top, String bottom, int color) {
        bannerQueue.add(new String[]{top, bottom, String.valueOf(color)});
    }

    private void startMove(int type, float dur, float power, int prio) {
        if (now < moveStart + moveDur && prio < movePrio) return;
        move = type;
        moveDir = rnd.nextBoolean() ? 1f : -1f;
        moveStart = now;
        moveDur = dur;
        movePower = power;
        movePrio = prio;
    }

    @SuppressWarnings("deprecation")
    private void vibrate(long[] pattern) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null || !v.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) {
            v.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            v.vibrate(pattern, -1);
        }
    }

    // ------------------------------------------------------------- particles

    private Particle spawn(float x, float y, int shape, int color, float size, float life) {
        Particle p = new Particle();
        p.x = x;
        p.y = y;
        p.shape = shape;
        p.color = color;
        p.size = size;
        p.life = life;
        if (particles.size() < 1200) particles.add(p);
        return p;
    }

    private void floatText(String label, float x, float y, int color, float scale, int style) {
        Particle p = spawn(x, y - popoR * 0.3f, SHAPE_TEXT, color, unit * 0.055f * scale, 0.9f);
        p.label = label;
        p.style = style;
        p.vy = -popoR * 2.2f;
        p.vx = (rnd.nextFloat() - 0.5f) * popoR;
    }

    /** A comic word that pops, wobbles, zooms, flies, shakes, waves or stretches (see drawWord). */
    private void word(String label, float x, float y, int color, float scale, int style) {
        Particle p = spawn(x, y, SHAPE_TEXT, color, unit * 0.07f * scale, 1.4f);
        p.label = label;
        p.style = style;
        p.rot = (rnd.nextFloat() - 0.5f) * 20f;
        p.drag = 3f;
        if (style == 3) {
            p.vx = (rnd.nextBoolean() ? 1 : -1) * popoR * 3f;
            p.vy = -popoR * 4f;
            p.gravity = popoR * 6f;
            p.drag = 0.3f;
        } else {
            p.vy = -popoR * 0.6f;
        }
    }

    private void confetti(int n) {
        for (int i = 0; i < n; i++) {
            Particle p = spawn(popoX, popoY, SHAPE_RECT, PARTY[rnd.nextInt(PARTY.length)], popoR * (0.08f + 0.06f * rnd.nextFloat()), 3f);
            double a = -Math.PI / 2 + (rnd.nextFloat() - 0.5f) * 2.4f;
            float speed = popoR * (6f + 9f * rnd.nextFloat());
            p.vx = (float) Math.cos(a) * speed;
            p.vy = (float) Math.sin(a) * speed;
            p.gravity = popoR * 9f;
            p.drag = 0.6f;
            p.vr = (rnd.nextFloat() - 0.5f) * 900f;
        }
    }

    private void rainbowPuffs(int n) {
        for (int i = 0; i < n; i++) {
            Particle p = spawn(popoX, popoY + popoR * 0.6f, SHAPE_PUFF, RAINBOW[i % RAINBOW.length], popoR * 0.15f, 2.2f);
            p.delay = i * 0.02f;
            double a = Math.PI / 2 + (rnd.nextFloat() - 0.5f) * 2.2f;
            float speed = popoR * (1.5f + 2.5f * rnd.nextFloat());
            p.vx = (float) Math.cos(a) * speed;
            p.vy = (float) Math.sin(a) * speed;
            p.grow = popoR * 0.35f;
            p.drag = 0.8f;
        }
    }

    private void sparkles(int n) {
        for (int i = 0; i < n; i++) {
            double a = rnd.nextFloat() * Math.PI * 2;
            float d = popoR * (1.2f + 1.3f * rnd.nextFloat());
            Particle p = spawn(popoX + (float) Math.cos(a) * d, popoY + (float) Math.sin(a) * d, SHAPE_STAR,
                    rnd.nextBoolean() ? GOLD : Color.WHITE, popoR * (0.1f + 0.1f * rnd.nextFloat()), 1.6f);
            p.delay = rnd.nextFloat() * 0.8f;
            p.vr = 120f;
        }
    }

    private void hearts(int n) {
        int[] colors = {0xFFFF4081, 0xFFE53935, 0xFFFF80AB, 0xFFD500F9};
        for (int i = 0; i < n; i++) {
            Particle p = spawn(getWidth() * rnd.nextFloat(), -popoR * 0.3f, SHAPE_HEART, colors[rnd.nextInt(colors.length)],
                    popoR * (0.12f + 0.12f * rnd.nextFloat()), 5f);
            p.delay = rnd.nextFloat() * 2.5f;
            p.vy = popoR * (1.5f + 1.5f * rnd.nextFloat());
            p.vx = (rnd.nextFloat() - 0.5f) * popoR;
            p.drag = 0f;
            p.vr = (rnd.nextFloat() - 0.5f) * 120f;
        }
    }

    private void firework(float x, float y) {
        int color = PARTY[rnd.nextInt(PARTY.length)];
        int n = 70;
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            float speed = popoR * (2.5f + 1.5f * rnd.nextFloat());
            Particle p = spawn(x, y, i % 3 == 0 ? SHAPE_STAR : SHAPE_CIRCLE, i % 5 == 0 ? Color.WHITE : color, popoR * 0.06f, 1.8f);
            p.vx = (float) Math.cos(a) * speed;
            p.vy = (float) Math.sin(a) * speed;
            p.gravity = popoR * 1.5f;
            p.drag = 1.2f;
        }
    }

    private void shootingStars(int n) {
        for (int i = 0; i < n; i++) {
            Particle p = spawn(-popoR + rnd.nextFloat() * getWidth() * 0.5f, -popoR, SHAPE_STAR,
                    i % 3 == 0 ? GOLD : Color.WHITE, popoR * (0.15f + 0.1f * rnd.nextFloat()), 2f);
            p.delay = rnd.nextFloat() * 1.5f;
            p.vx = popoR * 6f;
            p.vy = popoR * 5f;
            p.drag = 0f;
            p.vr = 360f;
            for (int k = 1; k <= 4; k++) {
                Particle t = spawn(p.x - k * popoR * 0.25f, p.y - k * popoR * 0.2f, SHAPE_CIRCLE, Color.WHITE, p.size * (0.4f - k * 0.07f), 2f);
                t.delay = p.delay;
                t.vx = p.vx;
                t.vy = p.vy;
                t.drag = 0f;
            }
        }
    }

    private void bubbles(int n) {
        for (int i = 0; i < n; i++) {
            Particle p = spawn(getWidth() * rnd.nextFloat(), getHeight() + popoR * 0.3f, SHAPE_BUBBLE,
                    RAINBOW[rnd.nextInt(RAINBOW.length)], popoR * (0.1f + 0.2f * rnd.nextFloat()), 4f);
            p.delay = rnd.nextFloat() * 2f;
            p.vy = -popoR * (2f + 2f * rnd.nextFloat());
            p.vx = (rnd.nextFloat() - 0.5f) * popoR;
            p.drag = 0f;
        }
    }

    private void coins(int n) {
        for (int i = 0; i < n; i++) {
            Particle p = spawn(getWidth() * rnd.nextFloat(), -popoR * 0.3f, SHAPE_COIN, GOLD, popoR * (0.12f + 0.06f * rnd.nextFloat()), 3.5f);
            p.delay = rnd.nextFloat() * 1.5f;
            p.vy = popoR * 2f;
            p.gravity = popoR * 6f;
            p.drag = 0f;
            p.rot = rnd.nextFloat() * 360f;
        }
    }

    private void updateParticles(float dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            if (p.delay > 0) {
                p.delay -= dt;
                continue;
            }
            p.age += dt;
            if (p.age >= p.life) {
                particles.remove(i);
                continue;
            }
            float damp = (float) Math.exp(-p.drag * dt);
            p.vx *= damp;
            p.vy = p.vy * damp + p.gravity * dt;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.rot += p.vr * dt;
            p.size += p.grow * dt;
            if (p.shape == SHAPE_COIN) p.rot += dt * 400f;
        }
    }

    // ------------------------------------------------------------------ frame

    @Override
    protected void onDraw(Canvas c) {
        long nanos = System.nanoTime();
        float dt = Math.min(0.05f, (nanos - lastFrame) / 1e9f);
        // Shop or trophies open mid-game: the game clock stands still, so beats, bosses and drain wait.
        if (state == PLAYING && overlay != NO_OVERLAY) {
            pausedNanos += nanos - lastFrame;
            dt = 0;
        }
        lastFrame = nanos;
        now = (nanos - start - pausedNanos) / 1e9f;
        // Ease the extra wiggle back down to the idle sway.
        energy = IDLE_ENERGY + (energy - IDLE_ENERGY) * (float) Math.exp(-dt * 1.6f);
        punch *= (float) Math.exp(-dt * 6f);
        float phase = (float) (now / PERIOD_S * 2 * Math.PI);

        update(dt);
        for (int i = pendingBursts.size() - 1; i >= 0; i--) {
            float[] b = pendingBursts.get(i);
            if (now >= b[0]) {
                firework(b[1], b[2]);
                pendingBursts.remove(i);
            }
        }
        if (now - bannerStart > 2.4f && !bannerQueue.isEmpty()) {
            String[] b = bannerQueue.remove(0);
            bannerTop = b[0];
            bannerBottom = b[1];
            bannerColor = Integer.parseInt(b[2]);
            bannerStart = now;
        }
        if (state == OVER && rnd.nextFloat() < dt * 1.2f) {
            Particle z = spawn(popoX + popoR * 1.2f, popoY - popoR, SHAPE_TEXT, Color.WHITE, unit * 0.06f, 2.5f);
            z.label = "Z";
            z.vy = -popoR * 0.8f;
            z.vx = popoR * 0.3f;
            z.drag = 0f;
            z.grow = unit * 0.03f;
        }
        updateParticles(dt);

        float w = getWidth();
        float h = getHeight();

        c.save();
        if (now < shakeUntil) {
            float k = Math.min(1f, (shakeUntil - now) / 1.5f) * popoR * 0.15f;
            c.translate((rnd.nextFloat() - 0.5f) * k, (rnd.nextFloat() - 0.5f) * k);
        }
        c.scale(1 + punch, 1 + punch, w / 2f, popoY);
        drawBackground(c, w, h);
        boolean disco = now < discoUntil || fever;
        drawOutlined(c, "Wackel-Popo", w / 2f, h * 0.1f, unit * 0.085f, disco ? Color.WHITE : INK, 0);

        // Rocket: fly up and out, then drop back in from the top with a bounce.
        float offsetY = 0f;
        boolean flame = false;
        float rt = now - rocketStart;
        if (rt >= 0 && rt < 3f) {
            if (rt < 1.4f) {
                float p = rt / 1.4f;
                offsetY = -p * p * h * 1.2f;
                flame = true;
            } else if (rt < 1.6f) {
                offsetY = -h * 1.2f;
            } else {
                float p = (rt - 1.6f) / 1.4f;
                offsetY = -h * (1f - p) * (1f - p) * (float) Math.abs(Math.cos(p * Math.PI * 1.5f));
            }
        }
        float gt = now - giantStart;
        float giant = 1f;
        if (gt >= 0 && gt < 2.4f) {
            float env = (float) Math.sin(Math.PI * gt / 2.4f);
            giant = 1f + env * (0.85f + 0.05f * (float) Math.sin(gt * 30f));
        }
        c.save();
        c.translate(0, offsetY);
        float big = giant * (bossActive ? 1.2f : 1f);
        c.scale(big, big, popoX, popoY);
        applyMove(c);
        if (flame) drawFlame(c, popoX, popoY + popoR * 2.45f, popoR);
        if (evo > 0) drawAura(c);
        drawPopo(c, popoX, popoY, popoR, phase);
        if (move == MOVE_DIZZY && now < moveStart + moveDur) drawDizzyStars(c);
        c.restore();
        if (flame && rnd.nextFloat() < 0.8f) {
            Particle p = spawn(popoX + (rnd.nextFloat() - 0.5f) * popoR, popoY + offsetY + popoR * 2.9f, SHAPE_PUFF, 0xFFBDBDBD, popoR * 0.15f, 1.2f);
            p.vy = popoR * 2f;
            p.grow = popoR * 0.3f;
        }

        drawDanger(c, w, h);
        drawParticles(c);
        if (starActive) drawBonusStar(c);
        if (state == PLAYING && overlay == NO_OVERLAY) drawBeatRing(c);
        if (goodie >= 0) drawGoodie(c);
        Music.set(musicSong(), musicLevel());
        if (state == PLAYING) drawBoosts(c);
        if (bossActive) drawBossBar(c, w, h);
        drawHud(c, w, h);
        drawButtons(c);
        drawBanner(c, w, h);
        drawKanadier(c, w, h);
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.LEFT);
        text.setTextSize(unit * 0.03f);
        text.setColor(disco ? Color.WHITE : INK);
        text.setAlpha(140);
        c.drawText(versionLabel, unit * 0.03f, h - unit * 0.025f, text);
        text.setAlpha(255);
        text.setTextAlign(Paint.Align.CENTER);
        c.restore();

        if (now < flashUntil) c.drawColor(Color.argb((int) (200 * Math.min(1f, (flashUntil - now) / 0.25f)), 255, 255, 255));
        if (state == OVER && overlay == NO_OVERLAY) drawGameOver(c, w, h);
        if (overlay == SHOP) drawShop(c, w, h);
        if (overlay == TROPHY) drawTrophyScreen(c, w, h);
        if (overlay == WARDROBE) drawWardrobe(c, w, h);

        postInvalidateOnAnimation();
    }

    /** Moves the whole popo according to the current dance move (and shivers or sleeps). */
    private void applyMove(Canvas c) {
        float mx = 0, my = 0, rot = 0, sx = 1, sy = 1;
        float t = (now - moveStart) / moveDur;
        float r = popoR, p = movePower, dir = moveDir;
        legLift[0] = 0;
        legLift[1] = 0;
        if (t >= 0 && t < 1) {
            float s = (float) Math.sin(Math.PI * t);
            float ease = t < 0.5f ? 2 * t * t : 1 - 2 * (1 - t) * (1 - t);
            double tau = 2 * Math.PI;
            switch (move) {
                case MOVE_HOPS:
                    my = -s * r * 0.6f * p;
                    sy = 1 + 0.12f * s * p;
                    sx = 1 / sy;
                    legLift[0] = legLift[1] = s * r * 0.35f * p;
                    break;
                case MOVE_SHAKE:
                    mx = (float) Math.sin(t * 45) * r * 0.18f * (1 - t) * p;
                    break;
                case MOVE_CIRCLE:
                    mx = (float) Math.sin(tau * 2 * t) * r * 0.4f * p;
                    my = -(1 - (float) Math.cos(tau * 2 * t)) * r * 0.15f * p;
                    rot = (float) Math.sin(tau * 2 * t) * 8 * p;
                    break;
                case MOVE_SPIN:
                    rot = 360 * ease;
                    my = -s * r * 0.4f;
                    break;
                case MOVE_PENDULUM:
                    rot = (float) Math.sin(tau * 1.5 * t) * 28 * (1 - t) * p;
                    break;
                case MOVE_JELLY:
                    sx = 1 + 0.25f * (float) Math.sin(tau * 3 * t) * (1 - t) * p;
                    sy = 2 - sx;
                    break;
                case MOVE_FLIP:
                    my = -s * r * 2.2f * p;
                    rot = -360 * ease;
                    break;
                case MOVE_MOONWALK:
                    mx = (float) Math.sin(tau * t) * r * 0.9f * p;
                    rot = -(float) Math.cos(tau * t) * 8 * p;
                    legLift[0] = Math.max(0, (float) Math.sin(tau * 3 * t)) * r * 0.3f;
                    legLift[1] = Math.max(0, -(float) Math.sin(tau * 3 * t)) * r * 0.3f;
                    break;
                case MOVE_STOMP:
                    float st = (float) Math.abs(Math.sin(4 * Math.PI * t));
                    my = st * r * 0.15f * p;
                    sy = 1 - 0.12f * st * p;
                    sx = 1 + 0.08f * st * p;
                    legLift[0] = Math.max(0, (float) Math.sin(4 * Math.PI * t)) * r * 0.45f * p;
                    legLift[1] = Math.max(0, -(float) Math.sin(4 * Math.PI * t)) * r * 0.45f * p;
                    break;
                case MOVE_SQUISH:
                    sy = 1 - 0.3f * p * (1 - t) * (float) Math.cos(5 * Math.PI * t);
                    sx = 2 - sy;
                    break;
                case MOVE_PULSE:
                    sx = sy = 1 + 0.3f * p * s;
                    break;
                case MOVE_TWIST:
                    rot = (float) Math.sin(t * 30) * 12 * (1 - t) * p;
                    break;
                case MOVE_WAVE:
                    rot = (float) Math.sin(tau * 2 * t) * 10 * (1 - t) * p;
                    sx = 1 + 0.1f * (float) Math.sin(tau * 4 * t) * p;
                    sy = 2 - sx;
                    break;
                case MOVE_SIDEHOP:
                    mx = s * r * 0.6f * dir * p;
                    my = -(float) Math.abs(Math.sin(tau * t)) * r * 0.45f * p;
                    rot = s * 10 * dir;
                    legLift[dir > 0 ? 0 : 1] = s * r * 0.4f;
                    break;
                case MOVE_DIZZY:
                    mx = (float) Math.cos(tau * 3 * t) * r * 0.15f * p;
                    my = (float) Math.sin(tau * 3 * t) * r * 0.08f * p;
                    rot = (float) Math.sin(tau * 3 * t) * 7 * p;
                    break;
                case MOVE_SPRING:
                    sy = 1 + 0.45f * s * p * (1 - t * 0.5f);
                    sx = 1 - 0.18f * s * p;
                    my = -s * r * 0.25f * p;
                    break;
                case MOVE_SLIDE:
                    mx = s * r * 1.4f * dir * p;
                    rot = -s * 12 * dir;
                    legLift[dir > 0 ? 1 : 0] = s * r * 0.3f;
                    break;
                case MOVE_BOW:
                    rot = s * 22 * dir * p;
                    sy = 1 - 0.1f * s * p;
                    break;
                case MOVE_DOUBLEHOP:
                    float dh = (float) Math.abs(Math.sin(tau * t));
                    my = -dh * r * 0.45f * p;
                    legLift[0] = legLift[1] = dh * r * 0.25f;
                    break;
                case MOVE_HULA:
                    mx = (float) Math.sin(tau * 3 * t) * r * 0.3f * p;
                    rot = -(float) Math.sin(tau * 3 * t) * 7 * p;
                    sy = 1 + 0.05f * (float) Math.cos(tau * 6 * t);
                    break;
                case MOVE_TAPFOOT:
                    legLift[dir > 0 ? 1 : 0] = Math.max(0, (float) Math.sin(tau * 3 * t)) * r * 0.3f;
                    rot = (float) Math.sin(tau * 1.5 * t) * 3;
                    break;
                case MOVE_KICK:
                    legLift[dir > 0 ? 1 : 0] = s * r * 0.7f * p;
                    rot = s * 8 * dir;
                    break;
                default:
                    break;
            }
        }
        if (state == PLAYING && counter < 0) {
            float k = (float) Math.min(1, -counter / -GAME_OVER_AT);
            mx += (rnd.nextFloat() - 0.5f) * r * 0.08f * k;
        }
        if (state == OVER) {
            sy *= 0.9f;
            rot += 6;
        }
        float px = popoX, py = popoY + r;
        c.translate(mx, my);
        c.rotate(rot, px, py);
        c.scale(sx, sy, px, py);
    }

    private void drawBackground(Canvas c, float w, float h) {
        if (fever && now >= discoUntil) {
            // Sunburst: rotating rays in party colours.
            c.drawColor(0xFFFF7043);
            float cx = w / 2f, cy = popoY, len = h * 1.2f;
            int rays = 16;
            for (int i = 0; i < rays; i++) {
                double a = now * 0.8 + i * 2 * Math.PI / rays;
                fill.setColor(PARTY[i % PARTY.length]);
                fill.setAlpha(150);
                path.reset();
                path.moveTo(cx, cy);
                path.lineTo(cx + (float) Math.cos(a) * len, cy + (float) Math.sin(a) * len);
                path.lineTo(cx + (float) Math.cos(a + Math.PI / rays) * len, cy + (float) Math.sin(a + Math.PI / rays) * len);
                path.close();
                c.drawPath(path, fill);
            }
            fill.setAlpha(255);
            return;
        }
        if (now >= discoUntil) {
            c.drawRect(-w, -h, w * 2, h * 2, bg);
            return;
        }
        float hue = (now * 120f) % 360f;
        c.drawColor(Color.HSVToColor(new float[]{hue, 0.55f, 0.35f}));
        // Spotlights sweeping from the disco ball.
        float bx = w / 2f, by = h * 0.2f;
        for (int i = 0; i < 6; i++) {
            double a = now * 1.3 + i * Math.PI / 3;
            fill.setColor(Color.HSVToColor(90, new float[]{(hue + i * 60) % 360, 0.8f, 1f}));
            path.reset();
            path.moveTo(bx, by);
            path.lineTo(bx + (float) Math.cos(a - 0.12) * h, by + (float) Math.sin(a - 0.12) * h);
            path.lineTo(bx + (float) Math.cos(a + 0.12) * h, by + (float) Math.sin(a + 0.12) * h);
            path.close();
            c.drawPath(path, fill);
        }
        float br = w * 0.07f;
        line.setColor(INK);
        line.setStrokeWidth(br * 0.08f);
        c.drawLine(bx, 0, bx, by - br, line);
        fill.setColor(0xFFCFD8DC);
        c.drawCircle(bx, by, br, fill);
        fill.setColor(Color.WHITE);
        for (int i = 0; i < 8; i++) {
            double a = now * 2 + i * Math.PI / 4;
            float d = br * 0.6f * (float) Math.abs(Math.cos(a));
            fill.setAlpha(120 + (int) (135 * Math.abs(Math.sin(a * 1.7))));
            c.drawCircle(bx + (float) Math.sin(a) * d, by + (float) Math.cos(a * 0.7) * br * 0.5f, br * 0.12f, fill);
        }
        fill.setAlpha(255);
        c.drawCircle(bx, by, br, line);
    }

    /** Red pulsing edge while the counter is below zero. */
    private void drawDanger(Canvas c, float w, float h) {
        if (state != PLAYING || counter >= 0) return;
        float k = (float) Math.min(1, -counter / -GAME_OVER_AT);
        float pulse = 0.6f + 0.4f * (float) Math.sin(now * (8 + 10 * k));
        int alpha = (int) (160 * k * pulse);
        fill.setShader(new RadialGradient(w / 2f, h / 2f, Math.max(w, h) * 0.75f,
                new int[]{0x00E53935, 0x00E53935, Color.argb(alpha, 229, 57, 53)}, new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(-w, -h, w * 2, h * 2, fill);
        fill.setShader(null);
        drawOutlined(c, "SCHNELL KLATSCHEN!", w / 2f, h * 0.2f, unit * 0.075f * (0.95f + 0.1f * pulse), RED, 0);
    }

    private void drawDizzyStars(Canvas c) {
        for (int i = 0; i < 3; i++) {
            double a = now * 6 + i * 2 * Math.PI / 3;
            float x = popoX + (float) Math.cos(a) * popoR * 1.1f;
            float y = popoY - popoR * 1.35f + (float) Math.sin(a) * popoR * 0.25f;
            c.save();
            c.translate(x, y);
            c.rotate(now * 200);
            starPath(popoR * 0.14f);
            fill.setColor(GOLD);
            c.drawPath(path, fill);
            line.setColor(INK);
            line.setStrokeWidth(popoR * 0.025f);
            c.drawPath(path, line);
            c.restore();
        }
    }

    /**
     * The beat ring: a ring shrinks towards the dashed target circle around the popo and touches it exactly on
     * the beat. Tapping right then is PERFEKT.
     */
    private void drawBeatRing(Canvas c) {
        float target = popoR * 2.2f;
        float iv = beatInterval();
        float until = nextBeat - now;
        float k = Math.max(0f, Math.min(1f, until / iv));
        boolean inWindow = Math.abs(until) <= PERFECT_S && !nextBeatHit;
        line.setStyle(Paint.Style.STROKE);
        line.setColor(Color.WHITE);
        line.setAlpha(110);
        line.setStrokeWidth(popoR * 0.035f);
        c.drawCircle(popoX, popoY, target, line);
        if (!nextBeatHit) {
            line.setColor(inWindow ? GOLD : Color.WHITE);
            line.setAlpha((int) (255 * (1f - 0.75f * k)));
            line.setStrokeWidth(popoR * (inWindow ? 0.09f : 0.055f));
            c.drawCircle(popoX, popoY, target + popoR * 1.6f * k, line);
        }
        float since = now - lastHitAt;
        if (since < 0.3f) {
            line.setColor(lastJudge == 2 ? GOLD : Color.WHITE);
            line.setAlpha((int) (255 * (1f - since / 0.3f)));
            line.setStrokeWidth(popoR * 0.08f);
            c.drawCircle(popoX, popoY, target * (1f + since * 0.6f), line);
        }
        line.setAlpha(255);
    }

    /**
     * Slides in from the bottom right, raises a stop hand and talks: the top of the head flaps up
     * like a lid, the way Canadians do in South Park.
     */
    private void drawKanadier(Canvas c, float w, float h) {
        float t = now - kanadierAt;
        if (kanadierTop == null || t < 0 || t > 3.2f) return;
        float slide = t < 0.3f ? 1 - t / 0.3f : (t > 2.9f ? (t - 2.9f) / 0.3f : 0);
        float hw = unit * 0.3f;
        float scale = hw / kanadierTop.getWidth();
        float topH = kanadierTop.getHeight() * scale, botH = kanadierBottom.getHeight() * scale;
        float x = w - hw - unit * 0.04f;
        float mouthY = h * 0.6f + slide * (h * 0.45f);
        // Stop hand next to the head.
        drawStopHand(c, x - unit * 0.06f, mouthY + botH * 0.2f, unit * 0.11f);
        rect.set(x, mouthY, x + hw, mouthY + botH);
        c.drawBitmap(kanadierBottom, null, rect, null);
        float flap = t < 2.2f ? 28f * Math.abs((float) Math.sin(t * 13f)) : 0f;
        c.save();
        c.rotate(flap, x + hw * 0.92f, mouthY);
        rect.set(x, mouthY - topH, x + hw, mouthY);
        c.drawBitmap(kanadierTop, null, rect, null);
        c.restore();
        if (slide == 0) {
            // Speech bubble to the upper left.
            float bw = unit * 0.5f, bh = unit * 0.19f, bx = x - bw - unit * 0.02f, by = mouthY - topH - bh * 0.4f;
            bx = Math.max(unit * 0.03f, bx);
            fill.setColor(Color.WHITE);
            line.setColor(INK);
            line.setStrokeWidth(unit * 0.008f);
            rect.set(bx, by, bx + bw, by + bh);
            c.drawOval(rect, fill);
            c.drawOval(rect, line);
            path.reset();
            path.moveTo(bx + bw * 0.78f, by + bh * 0.85f);
            path.lineTo(x + hw * 0.15f, mouthY - topH * 0.35f);
            path.lineTo(bx + bw * 0.9f, by + bh * 0.7f);
            c.drawPath(path, fill);
            c.drawLine(bx + bw * 0.78f, by + bh * 0.85f, x + hw * 0.15f, mouthY - topH * 0.35f, line);
            c.drawLine(bx + bw * 0.9f, by + bh * 0.7f, x + hw * 0.15f, mouthY - topH * 0.35f, line);
            text.setStyle(Paint.Style.FILL);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(INK);
            String[] words = kanadierLine.split(" ");
            String l1 = "", l2 = "";
            for (String word : words) {
                if (l1.length() < kanadierLine.length() / 2) l1 += (l1.isEmpty() ? "" : " ") + word;
                else l2 += (l2.isEmpty() ? "" : " ") + word;
            }
            text.setTextSize(fit(l1.length() > l2.length() ? l1 : l2, unit * 0.05f, bw * 0.78f));
            c.drawText(l1, bx + bw / 2f, by + bh * 0.45f, text);
            c.drawText(l2, bx + bw / 2f, by + bh * 0.75f, text);
        }
    }

    private void drawStopHand(Canvas c, float cx, float cy, float s) {
        int skin = 0xFFF2B38F;
        fill.setColor(skin);
        line.setColor(INK);
        line.setStrokeWidth(s * 0.06f);
        float wave = (float) Math.sin(now * 10) * 6f;
        c.save();
        c.rotate(wave, cx, cy + s);
        for (int i = 0; i < 4; i++) {
            float fx = cx - s * 0.42f + i * s * 0.28f;
            rect.set(fx - s * 0.12f, cy - s * (0.95f + (i == 1 || i == 2 ? 0.1f : 0f)), fx + s * 0.12f, cy);
            c.drawRoundRect(rect, s * 0.12f, s * 0.12f, fill);
            c.drawRoundRect(rect, s * 0.12f, s * 0.12f, line);
        }
        rect.set(cx + s * 0.35f, cy - s * 0.25f, cx + s * 0.75f, cy + s * 0.05f);
        c.drawRoundRect(rect, s * 0.14f, s * 0.14f, fill);
        c.drawRoundRect(rect, s * 0.14f, s * 0.14f, line);
        rect.set(cx - s * 0.56f, cy - s * 0.3f, cx + s * 0.5f, cy + s * 0.7f);
        c.drawRoundRect(rect, s * 0.25f, s * 0.25f, fill);
        c.drawRoundRect(rect, s * 0.25f, s * 0.25f, line);
        c.restore();
    }

    /** A soft pulsing glow behind an evolved popo, plus a ring that bursts outwards right after it evolves. */
    private void drawAura(Canvas c) {
        int color = evo == 4 ? Color.HSVToColor(new float[]{(now * 90) % 360, 0.6f, 1f}) : EVO_COLORS[evo - 1];
        float pulse = 1f + 0.06f * (float) Math.sin(now * 4);
        for (int i = 3; i >= 1; i--) {
            fill.setColor(color);
            fill.setAlpha(28 + 10 * evo);
            c.drawCircle(popoX, popoY, popoR * (1.6f + 0.35f * i) * pulse, fill);
        }
        fill.setAlpha(255);
        float t = now - evoStart;
        if (t >= 0 && t < 1.2f) {
            line.setColor(color);
            line.setAlpha((int) (255 * (1 - t / 1.2f)));
            line.setStrokeWidth(popoR * 0.12f * (1 - t / 1.2f) + 1);
            c.drawCircle(popoX, popoY, popoR * (1.5f + 4f * t), line);
            line.setAlpha(255);
        }
    }

    /** The falling goodie, hanging from a little parachute. */
    private void drawGoodie(Canvas c) {
        float s = unit * 0.075f;
        float sway = (float) Math.sin((now - goodieBorn) * 2.2f + 1.2f) * 12f;
        c.save();
        c.rotate(sway, goodieX, goodieY);
        float top = goodieY - s * 2.6f;
        line.setColor(INK);
        line.setStrokeWidth(s * 0.05f);
        c.drawLine(goodieX - s * 1.1f, top, goodieX - s * 0.3f, goodieY - s * 0.6f, line);
        c.drawLine(goodieX + s * 1.1f, top, goodieX + s * 0.3f, goodieY - s * 0.6f, line);
        rect.set(goodieX - s * 1.15f, top - s * 0.9f, goodieX + s * 1.15f, top + s * 0.9f);
        fill.setColor(goodie == Goodies.STINK ? 0xFF9CCC65 : PARTY[goodie % PARTY.length]);
        c.drawArc(rect, 180, 180, true, fill);
        line.setStrokeWidth(s * 0.08f);
        c.drawArc(rect, 180, 180, true, line);
        fill.setColor(Color.WHITE);
        fill.setAlpha(170);
        c.drawCircle(goodieX, goodieY, s * 0.8f, fill);
        fill.setAlpha(255);
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(s * 1.1f);
        c.drawText(Goodies.ICONS[goodie], goodieX, goodieY + s * 0.38f, text);
        c.restore();
    }

    /** Active timed goodies as small pills under the shop button, with seconds left. */
    private void drawBoosts(Canvas c) {
        float x = shopBtn.left, y = shopBtn.bottom + unit * 0.03f, h = unit * 0.06f;
        for (int g = 0; g < boostUntil.length; g++) {
            float left = boostUntil[g] - playTime;
            if (left <= 0) continue;
            String label = Goodies.ICONS[g] + " " + (int) Math.ceil(left) + " s";
            text.setStyle(Paint.Style.FILL);
            text.setTextSize(h * 0.6f);
            float tw = text.measureText(label);
            rect.set(x, y, x + tw + h * 0.6f, y + h);
            fill.setColor(0xCC2B1B17);
            c.drawRoundRect(rect, h / 2, h / 2, fill);
            text.setColor(left < 2 && (int) (now * 6) % 2 == 0 ? GOLD : Color.WHITE);
            text.setTextAlign(Paint.Align.LEFT);
            c.drawText(label, x + h * 0.3f, y + h * 0.7f, text);
            y += h * 1.2f;
        }
        text.setTextAlign(Paint.Align.CENTER);
    }

    private void drawBonusStar(Canvas c) {
        float s = unit * 0.07f * (1 + 0.1f * (float) Math.sin(now * 10));
        fill.setColor(GOLD);
        fill.setAlpha(70);
        c.drawCircle(starX, starY, s * 1.8f, fill);
        fill.setAlpha(255);
        c.save();
        c.translate(starX, starY);
        c.rotate(now * 180);
        starPath(s);
        c.drawPath(path, fill);
        line.setColor(INK);
        line.setStrokeWidth(s * 0.12f);
        c.drawPath(path, line);
        c.restore();
        drawOutlined(c, "Fang mich!", starX, starY - s * 1.6f, unit * 0.035f, Color.WHITE, 0);
    }

    private void drawHud(Canvas c, float w, float h) {
        boolean dark = now < discoUntil || fever;
        int ink = dark ? Color.WHITE : INK;
        if (state == READY) {
            drawOutlined(c, "Tippen zum Starten!", w / 2f, h * 0.79f, unit * 0.075f, ink, 0);
            smallText(c, "Klatsch im Takt: wenn der Ring den Kreis trifft!", w / 2f, h * 0.835f, ink);
            smallText(c, "Mehrere Finger gleichzeitig = Griff-Bonus!", w / 2f, h * 0.87f, ink);
            String challenge = daily.doneToday()
                    ? "✔ Tages-Challenge geschafft · 🔥 " + daily.streak() + " Tage in Folge"
                    : "Tages-Challenge: " + Daily.TEXTS[daily.kind()] + " (+" + daily.reward() + " Taler)";
            smallText(c, challenge, w / 2f, h * 0.905f, daily.doneToday() ? GREEN : ink);
            smallText(c, "Awards: " + awards.unlockedCount() + " von " + Awards.TOTAL + "   ·   Rekord: " + fmt(scores.best()), w / 2f, h * 0.945f, ink);
            return;
        }
        // The counter itself.
        int color = counter < 0 ? RED : (counter >= cheapestPrice() ? GREEN : Color.WHITE);
        float bump = 1 + 0.08f * Math.max(0, energy - IDLE_ENERGY);
        drawOutlined(c, fmt(counter), w / 2f, h * 0.775f, unit * 0.12f * bump, color, 0);
        String counterLabel = fever ? "🔥 FIEBER-MODUS: alles x1,25 🔥"
                : (evo > 0 ? "✨ " + EVO_NAMES[evo - 1] + " ✨" + (evo < EVO_AT.length ? "  ·  nächste Stufe bei " + fmt(EVO_AT[evo]) : "")
                : "Popo-Klatsch-Counter  ·  Verwandlung bei " + fmt(EVO_AT[0]));
        smallText(c, counterLabel, w / 2f, h * 0.81f, ink);
        String rates = "Combo x" + combo + "   ·   +" + fmt(shop.tapGain(false)) + " pro Klatsch   ·   −" + fmt1(drainRate()) + "/s";
        if (autoRate() > 0) rates += "   ·   Auto +" + fmt1(autoRate()) + "/s";
        smallText(c, rates, w / 2f, h * 0.843f, ink);

        // Progress to the next award.
        float bw = unit * 0.9f, bh = unit * 0.05f, bx = w / 2f - bw / 2f, by = h * 0.86f;
        float progress = 1f;
        String label = "Alle 100 Punkte-Awards geholt!";
        if (nextAward < Awards.COUNT) {
            long from = nextAward == 0 ? 0 : Awards.threshold(nextAward - 1);
            long to = Awards.threshold(nextAward);
            progress = (float) Math.max(0, Math.min(1, (lifetimeBase + total - from) / (double) (to - from)));
            label = "Award " + (nextAward + 1) + " bei " + fmt(to) + ": " + Awards.NAMES[nextAward];
        }
        fill.setColor(0x33000000);
        rect.set(bx, by, bx + bw, by + bh);
        c.drawRoundRect(rect, bh / 2, bh / 2, fill);
        fill.setColor(GOLD);
        rect.set(bx, by, bx + Math.max(bh, bw * progress), by + bh);
        c.drawRoundRect(rect, bh / 2, bh / 2, fill);
        text.setTextSize(fit(label, bh * 0.62f, bw * 0.94f));
        text.setColor(INK);
        text.setTextAlign(Paint.Align.CENTER);
        c.drawText(label, w / 2f, by + bh * 0.72f, text);

        // Fever charge: fills with the combo, fever starts when it is full.
        if (!fever && combo > 0) {
            float fw = unit * 0.5f, fh = unit * 0.015f, fx = w / 2f - fw / 2f, fy = h * 0.915f;
            fill.setColor(0x33000000);
            rect.set(fx, fy, fx + fw, fy + fh);
            c.drawRoundRect(rect, fh / 2, fh / 2, fill);
            fill.setColor(RED);
            rect.set(fx, fy, fx + fw * Math.min(1f, combo / (float) FEVER_COMBO), fy + fh);
            c.drawRoundRect(rect, fh / 2, fh / 2, fill);
        }

        smallText(c, "Gesamt: " + fmt(total) + "   ·   Rekord: " + fmt(Math.max(scores.best(), total)), w / 2f, h * 0.955f, ink);
    }

    private void smallText(Canvas c, String s, float x, float y, int color) {
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(color);
        text.setAlpha(210);
        text.setTextSize(fit(s, unit * 0.038f, getWidth() * 0.94f));
        c.drawText(s, x, y, text);
        text.setAlpha(255);
    }

    /** Shrinks a font size so the text fits into maxWidth. */
    private float fit(String s, float size, float maxWidth) {
        text.setTextSize(size);
        float width = text.measureText(s);
        return width > maxWidth ? size * maxWidth / width : size;
    }

    private long cheapestPrice() {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < Shop.NAMES.length; i++) if (!shop.maxed(i)) best = Math.min(best, shop.price(i));
        return best;
    }

    private void drawButtons(Canvas c) {
        drawButtonFrame(c, trophyBtn);
        drawTrophyIcon(c, trophyBtn);
        drawButtonFrame(c, musicBtn);
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(musicBtn.width() * 0.55f);
        text.setColor(INK);
        c.drawText(musicOn ? "🎸" : "🔇", musicBtn.centerX(), musicBtn.centerY() + musicBtn.width() * 0.2f, text);
        drawButtonFrame(c, shopBtn);
        if (state != PLAYING) {
            text.setStyle(Paint.Style.FILL);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(shopBtn.width() * 0.55f);
            text.setColor(INK);
            c.drawText("👕", shopBtn.centerX(), shopBtn.centerY() + shopBtn.width() * 0.2f, text);
            return;
        }
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(shopBtn.width() * 0.55f);
        text.setColor(INK);
        c.drawText("🛒", shopBtn.centerX(), shopBtn.centerY() + shopBtn.width() * 0.2f, text);
        if (counter >= cheapestPrice()) {
            // Something is affordable: little pulsing green dot.
            fill.setColor(GREEN);
            c.drawCircle(shopBtn.right - shopBtn.width() * 0.08f, shopBtn.top + shopBtn.width() * 0.08f,
                    shopBtn.width() * (0.14f + 0.03f * (float) Math.sin(now * 8)), fill);
        }
    }

    private void drawButtonFrame(Canvas c, RectF r) {
        float s = r.width();
        fill.setColor(Color.WHITE);
        c.drawRoundRect(r, s * 0.25f, s * 0.25f, fill);
        line.setColor(INK);
        line.setStrokeWidth(s * 0.05f);
        c.drawRoundRect(r, s * 0.25f, s * 0.25f, line);
    }

    private void drawTrophyIcon(Canvas c, RectF r) {
        float s = r.width();
        float cx = r.centerX(), top = r.top + s * 0.2f;
        line.setColor(INK);
        line.setStrokeWidth(s * 0.04f);
        rect.set(cx - s * 0.36f, top + s * 0.04f, cx - s * 0.18f, top + s * 0.22f);
        c.drawArc(rect, 90, 180, false, line);
        rect.set(cx + s * 0.18f, top + s * 0.04f, cx + s * 0.36f, top + s * 0.22f);
        c.drawArc(rect, 270, 180, false, line);
        fill.setColor(GOLD);
        path.reset();
        path.moveTo(cx - s * 0.25f, top);
        path.lineTo(cx + s * 0.25f, top);
        path.quadTo(cx + s * 0.25f, top + s * 0.38f, cx, top + s * 0.4f);
        path.quadTo(cx - s * 0.25f, top + s * 0.38f, cx - s * 0.25f, top);
        path.close();
        c.drawPath(path, fill);
        c.drawPath(path, line);
        c.drawLine(cx, top + s * 0.4f, cx, top + s * 0.52f, line);
        rect.set(cx - s * 0.16f, top + s * 0.52f, cx + s * 0.16f, top + s * 0.6f);
        c.drawRect(rect, fill);
        c.drawRect(rect, line);
    }

    private void drawBanner(Canvas c, float w, float h) {
        float t = now - bannerStart;
        if (bannerTop == null || t < 0 || t > 2.4f) return;
        float pop = t < 0.25f ? 0.3f + 0.9f * (t / 0.25f) : (t < 0.4f ? 1.2f - (t - 0.25f) / 0.15f * 0.2f : 1f);
        int alpha = t > 1.9f ? Math.max(1, (int) (255 * (1f - (t - 1.9f) / 0.5f))) : 255;
        c.save();
        c.translate(w / 2f, h * 0.165f);
        c.scale(pop, pop);
        c.rotate((float) Math.sin(t * 6f) * 3f);
        drawOutlined(c, bannerTop, 0, 0, fit(bannerTop, unit * 0.075f, w * 0.9f), bannerColor, alpha);
        drawOutlined(c, bannerBottom, 0, unit * 0.09f, fit(bannerBottom, unit * 0.075f, w * 0.9f), Color.WHITE, alpha);
        c.restore();
    }

    // -------------------------------------------------------------- overlays

    private void drawCard(Canvas c, RectF r) {
        c.drawColor(0x99000000);
        fill.setColor(CARD);
        c.drawRoundRect(r, unit * 0.05f, unit * 0.05f, fill);
        line.setColor(INK);
        line.setStrokeWidth(unit * 0.012f);
        c.drawRoundRect(r, unit * 0.05f, unit * 0.05f, line);
    }

    private void drawShop(Canvas c, float w, float h) {
        drawCard(c, card);
        float y = card.top + unit * 0.11f;
        drawOutlined(c, "Popo-Shop", w / 2f, y, unit * 0.085f, GOLD, 0);
        y += unit * 0.075f;
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(unit * 0.045f);
        text.setColor(counter < 0 ? RED : INK);
        c.drawText("Counter: " + fmt(counter) + "  (sinkt weiter!)", w / 2f, y, text);

        float rowTop = y + unit * 0.04f;
        float rowH = (card.bottom - unit * 0.2f - rowTop) / Shop.NAMES.length;
        float left = card.left + unit * 0.04f, right = card.right - unit * 0.04f;
        for (int i = 0; i < Shop.NAMES.length; i++) {
            RectF r = shopRows[i];
            r.set(left, rowTop + i * rowH + rowH * 0.06f, right, rowTop + (i + 1) * rowH - rowH * 0.06f);
            boolean maxed = shop.maxed(i);
            long price = shop.price(i);
            boolean affordable = !maxed && counter >= price;
            fill.setColor(affordable ? 0xFFDCEDC8 : 0xFFF5F5F5);
            c.drawRoundRect(r, unit * 0.03f, unit * 0.03f, fill);
            line.setStrokeWidth(unit * 0.006f);
            line.setColor(affordable ? GREEN : 0xFFBDBDBD);
            c.drawRoundRect(r, unit * 0.03f, unit * 0.03f, line);

            float iconSize = Math.min(r.height() * 0.55f, unit * 0.09f);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(iconSize);
            text.setColor(INK);
            c.drawText(Shop.ICONS[i], r.left + unit * 0.07f, r.centerY() + iconSize * 0.35f, text);

            float pw = unit * 0.19f, ph = Math.min(r.height() * 0.5f, unit * 0.08f);
            float textLeft = r.left + unit * 0.14f, textWidth = r.right - pw - unit * 0.05f - textLeft;
            String title = Shop.NAMES[i] + "  Lv " + shop.level(i);
            String desc = shop.describe(i);
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(fit(title, Math.min(r.height() * 0.3f, unit * 0.045f), textWidth));
            c.drawText(title, textLeft, r.centerY() - r.height() * 0.06f, text);
            text.setTextSize(fit(desc, Math.min(r.height() * 0.22f, unit * 0.032f), textWidth));
            text.setAlpha(170);
            c.drawText(desc, textLeft, r.centerY() + r.height() * 0.26f, text);
            text.setAlpha(255);

            rect.set(r.right - pw - unit * 0.02f, r.centerY() - ph / 2, r.right - unit * 0.02f, r.centerY() + ph / 2);
            fill.setColor(maxed ? 0xFF9E9E9E : (affordable ? GREEN : 0xFFBDBDBD));
            c.drawRoundRect(rect, ph / 2, ph / 2, fill);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.WHITE);
            String priceLabel = maxed ? "MAX" : fmt(price);
            text.setTextSize(fit(priceLabel, ph * 0.5f, pw * 0.85f));
            c.drawText(priceLabel, rect.centerX(), rect.centerY() + ph * 0.18f, text);
        }

        float bw = unit * 0.55f, bh = unit * 0.1f;
        closeBtn.set(w / 2f - bw / 2, card.bottom - unit * 0.05f - bh, w / 2f + bw / 2, card.bottom - unit * 0.05f);
        fill.setColor(0xFFFF7043);
        c.drawRoundRect(closeBtn, bh / 2, bh / 2, fill);
        line.setColor(INK);
        line.setStrokeWidth(unit * 0.008f);
        c.drawRoundRect(closeBtn, bh / 2, bh / 2, line);
        text.setColor(Color.WHITE);
        text.setTextSize(bh * 0.42f);
        c.drawText("Weiter klatschen!", closeBtn.centerX(), closeBtn.centerY() + bh * 0.15f, text);
        drawParticles(c, true);
    }

    private void drawTrophyScreen(Canvas c, float w, float h) {
        drawCard(c, card);
        float tabTop = card.top + unit * 0.04f, tabH = unit * 0.1f;
        String[] names = {"Bestenliste", "Awards " + awards.unlockedCount() + "/" + Awards.TOTAL};
        float tw = (card.width() - unit * 0.12f) / 2;
        for (int i = 0; i < 2; i++) {
            RectF t = tabs[i];
            t.set(card.left + unit * 0.04f + i * (tw + unit * 0.04f), tabTop, card.left + unit * 0.04f + i * (tw + unit * 0.04f) + tw, tabTop + tabH);
            fill.setColor(trophyTab == i ? GOLD : 0xFFE0E0E0);
            c.drawRoundRect(t, tabH / 2, tabH / 2, fill);
            line.setColor(INK);
            line.setStrokeWidth(unit * 0.006f);
            c.drawRoundRect(t, tabH / 2, tabH / 2, line);
            text.setStyle(Paint.Style.FILL);
            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(INK);
            text.setTextSize(tabH * 0.4f);
            c.drawText(names[i], t.centerX(), t.centerY() + tabH * 0.14f, text);
        }
        float contentTop = tabTop + tabH + unit * 0.04f;
        if (trophyTab == 0) drawHighscores(c, w, contentTop);
        else drawAwardGrid(c, w, contentTop);

        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(INK);
        text.setAlpha(150);
        text.setTextSize(unit * 0.035f);
        c.drawText(trophyTab == 1 ? "Medaille antippen für Details · sonst schließen" : "Tippen zum Schließen",
                w / 2f, card.bottom - unit * 0.035f, text);
        text.setAlpha(255);
    }

    private void drawHighscores(Canvas c, float w, float top) {
        List<HighscoreStore.Entry> entries = scores.entries();
        float row = (card.bottom - unit * 0.12f - top) / HighscoreStore.MAX_ENTRIES;
        float size = Math.min(row * 0.5f, unit * 0.055f);
        float y = top + row * 0.6f;
        text.setStyle(Paint.Style.FILL);
        text.setColor(INK);
        text.setTextSize(size);
        if (entries.isEmpty()) {
            text.setTextAlign(Paint.Align.CENTER);
            c.drawText("Noch keine Spiele. Los, klatschen!", w / 2f, y + row, text);
        }
        int[] medals = {GOLD, 0xFFB0BEC5, 0xFFD7A26C};
        for (int i = 0; i < entries.size(); i++) {
            HighscoreStore.Entry e = entries.get(i);
            float ry = y + i * row;
            float mx = card.left + unit * 0.09f;
            text.setTextSize(size);
            if (i < medals.length) {
                fill.setColor(medals[i]);
                c.drawCircle(mx, ry - size * 0.35f, size * 0.75f, fill);
            }
            text.setColor(INK);
            text.setTextAlign(Paint.Align.CENTER);
            c.drawText(String.valueOf(i + 1), mx, ry, text);
            text.setTextAlign(Paint.Align.LEFT);
            c.drawText(fmt(e.score) + " Klatscher", card.left + unit * 0.18f, ry, text);
            text.setTextAlign(Paint.Align.RIGHT);
            text.setAlpha(150);
            c.drawText(dateFormat.format(new Date(e.time)), card.right - unit * 0.05f, ry, text);
            text.setAlpha(255);
        }
    }

    private void drawAwardGrid(Canvas c, float w, float top) {
        int cols = 10, rows = (Awards.TOTAL + cols - 1) / cols;
        float availH = card.bottom - unit * 0.27f - top;
        gridCell = Math.min((card.width() - unit * 0.1f) / cols, availH / rows);
        gridLeft = w / 2f - gridCell * cols / 2f;
        gridTop = top;
        for (int i = 0; i < Awards.TOTAL; i++) {
            float cx = gridLeft + (i % cols + 0.5f) * gridCell;
            float cy = gridTop + (i / cols + 0.5f) * gridCell;
            boolean got = awards.isUnlocked(i);
            boolean special = i >= Awards.COUNT;
            int color = special ? PURPLE : (i >= 90 ? 0xFFE040FB : (i >= 50 ? GOLD : 0xFFFFB74D));
            fill.setColor(got ? color : 0xFFE0E0E0);
            c.drawCircle(cx, cy, gridCell * 0.42f, fill);
            line.setColor(i == selectedAward ? RED : (got ? INK : 0xFFBDBDBD));
            line.setStrokeWidth(gridCell * (i == selectedAward ? 0.1f : 0.05f));
            c.drawCircle(cx, cy, gridCell * 0.42f, line);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(gridCell * 0.32f);
            text.setColor(got ? (special ? Color.WHITE : INK) : 0xFF9E9E9E);
            String label = got ? (special ? "★" : String.valueOf(i + 1)) : "?";
            c.drawText(label, cx, cy + gridCell * 0.11f, text);
        }
        float y = gridTop + gridCell * rows + unit * 0.065f;
        String title, detail;
        int show = selectedAward;
        if (show < 0) {
            for (int i = 0; i < Awards.TOTAL; i++) {
                if (!awards.isUnlocked(i)) {
                    show = i;
                    break;
                }
            }
        }
        if (show < 0) {
            title = "Alle " + Awards.TOTAL + " Awards!";
            detail = "Du bist das POPO-UNIVERSUM.";
        } else if (show < Awards.COUNT) {
            title = (awards.isUnlocked(show) ? "✔ " : "") + "Award " + (show + 1) + ": " + Awards.NAMES[show];
            detail = "bei " + fmt(Awards.threshold(show)) + " Klatschern über alle Spiele";
        } else {
            String[] sp = Awards.SPECIALS[show - Awards.COUNT];
            title = (awards.isUnlocked(show) ? "✔ " : "") + "★ " + sp[0];
            detail = sp[1];
        }
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(INK);
        text.setTextSize(fit(title, unit * 0.045f, card.width() * 0.9f));
        c.drawText(title, w / 2f, y, text);
        text.setAlpha(170);
        text.setTextSize(fit(detail, unit * 0.038f, card.width() * 0.9f));
        c.drawText(detail, w / 2f, y + unit * 0.055f, text);
        text.setAlpha(255);
    }

    private void drawGameOver(Canvas c, float w, float h) {
        float t = now - overAt;
        float pop = Math.min(1f, t / 0.3f);
        rect.set(card.left, h * 0.26f, card.right, h * 0.74f);
        RectF box = new RectF(rect);
        c.save();
        c.scale(pop, pop, w / 2f, h / 2f);
        drawCard(c, box);
        float y = box.top + unit * 0.13f;
        drawOutlined(c, "POPO EINGESCHLAFEN!", w / 2f, y, fit("POPO EINGESCHLAFEN!", unit * 0.075f, box.width() * 0.85f), RED, 0);
        y += unit * 0.12f;
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(INK);
        text.setTextSize(unit * 0.06f);
        c.drawText(fmt(total) + " Klatscher", w / 2f, y, text);
        y += unit * 0.075f;
        String info = "Awards: " + awardsThisGame + "   ·   Zeit: " + (int) playTime + " s   ·   Sterne: " + starsCaught;
        text.setTextSize(fit(info, unit * 0.04f, box.width() * 0.9f));
        c.drawText(info, w / 2f, y, text);
        if (bestChord != null) {
            y += unit * 0.06f;
            String chord = "Bester Griff: " + bestChord;
            text.setTextSize(fit(chord, unit * 0.04f, box.width() * 0.9f));
            c.drawText(chord, w / 2f, y, text);
        }
        y += unit * 0.085f;
        if (newRecord) {
            drawOutlined(c, "NEUER REKORD!", w / 2f, y, unit * 0.07f, GOLD, 0);
        } else {
            text.setTextSize(unit * 0.045f);
            text.setColor(INK);
            c.drawText(lastPlace > 0 ? "Platz " + lastPlace + " in der Bestenliste" : "Nicht in den Top " + HighscoreStore.MAX_ENTRIES, w / 2f, y, text);
        }
        y += unit * 0.065f;
        text.setTextSize(unit * 0.04f);
        text.setColor(INK);
        c.drawText("+" + fmt(talerThisGame) + " Popo-Taler für die Garderobe", w / 2f, y, text);
        float bw = unit * 0.42f, bh = unit * 0.085f;
        shareBtn.set(w / 2f - bw / 2f, box.bottom - unit * 0.2f, w / 2f + bw / 2f, box.bottom - unit * 0.2f + bh);
        fill.setColor(GREEN);
        c.drawRoundRect(shareBtn, bh / 2, bh / 2, fill);
        line.setColor(INK);
        line.setStrokeWidth(unit * 0.006f);
        c.drawRoundRect(shareBtn, bh / 2, bh / 2, line);
        text.setColor(Color.WHITE);
        text.setTextSize(unit * 0.045f);
        c.drawText("📤 Rekord teilen", w / 2f, shareBtn.centerY() + unit * 0.016f, text);
        if (t > 1.2f) {
            text.setTextSize(unit * 0.042f);
            text.setColor(INK);
            text.setAlpha(150 + (int) (100 * Math.sin(now * 4)));
            c.drawText("Tippen für ein neues Spiel", w / 2f, box.bottom - unit * 0.05f, text);
            text.setAlpha(255);
        }
        c.restore();
    }

    /** Big comic lettering: coloured text with a dark outline (plain dark text gets no outline). */
    private void drawOutlined(Canvas c, String s, float x, float y, float size, int color, int alpha) {
        int a = alpha == 0 ? 255 : alpha;
        text.setTextSize(size);
        text.setTextAlign(Paint.Align.CENTER);
        if (color != INK) {
            text.setStyle(Paint.Style.STROKE);
            text.setStrokeWidth(size * 0.14f);
            text.setStrokeJoin(Paint.Join.ROUND);
            text.setColor(INK);
            text.setAlpha(a);
            c.drawText(s, x, y, text);
        }
        text.setStyle(Paint.Style.FILL);
        text.setColor(color);
        text.setAlpha(a);
        c.drawText(s, x, y, text);
        text.setAlpha(255);
    }

    private String fmt(double v) {
        if (Math.abs(v) >= 1e9) return String.format(Locale.GERMANY, "%.1f Mrd.", v / 1e9);
        if (Math.abs(v) >= 1e7) return String.format(Locale.GERMANY, "%.1f Mio.", v / 1e6);
        return intFormat.format((long) v);
    }

    private static String fmt1(double v) {
        return String.format(Locale.GERMANY, "%.1f", v);
    }

    private static String fmtMult(double v) {
        String s = String.format(Locale.GERMANY, "%.2f", v);
        return s.replaceAll("0+$", "").replaceAll(",$", "");
    }

    private void drawParticles(Canvas c) {
        drawParticles(c, false);
    }

    /** Draws particles; with wordsOnly just the words (used on top of the shop). */
    private void drawParticles(Canvas c, boolean wordsOnly) {
        for (Particle p : particles) {
            if (p.delay > 0) continue;
            if (wordsOnly && p.shape != SHAPE_TEXT) continue;
            float fade = p.age > p.life * 0.7f ? 1f - (p.age - p.life * 0.7f) / (p.life * 0.3f) : 1f;
            if (p.shape == SHAPE_TEXT) {
                drawWord(c, p, fade);
                continue;
            }
            fill.setColor(p.color);
            fill.setAlpha((int) (255 * fade * (p.shape == SHAPE_PUFF ? 0.75f : 1f)));
            c.save();
            c.translate(p.x, p.y);
            c.rotate(p.rot);
            float s = p.size;
            switch (p.shape) {
                case SHAPE_RECT:
                    c.drawRect(-s, -s * 0.5f, s, s * 0.5f, fill);
                    break;
                case SHAPE_HEART:
                    path.reset();
                    path.moveTo(0, s * 0.9f);
                    path.cubicTo(-s * 1.6f, -s * 0.2f, -s * 0.6f, -s * 1.4f, 0, -s * 0.4f);
                    path.cubicTo(s * 0.6f, -s * 1.4f, s * 1.6f, -s * 0.2f, 0, s * 0.9f);
                    c.drawPath(path, fill);
                    break;
                case SHAPE_STAR:
                    starPath(s);
                    c.drawPath(path, fill);
                    break;
                case SHAPE_PUFF:
                    c.drawCircle(0, 0, s, fill);
                    c.drawCircle(-s * 0.7f, s * 0.2f, s * 0.7f, fill);
                    c.drawCircle(s * 0.7f, s * 0.25f, s * 0.65f, fill);
                    break;
                case SHAPE_BUBBLE:
                    fill.setAlpha((int) (70 * fade));
                    c.drawCircle(0, 0, s, fill);
                    line.setColor(p.color);
                    line.setAlpha((int) (220 * fade));
                    line.setStrokeWidth(s * 0.12f);
                    c.drawCircle(0, 0, s, line);
                    line.setAlpha(255);
                    fill.setColor(Color.WHITE);
                    fill.setAlpha((int) (220 * fade));
                    c.drawCircle(-s * 0.35f, -s * 0.35f, s * 0.2f, fill);
                    break;
                case SHAPE_COIN:
                    // Spinning coin: squash horizontally with the rotation.
                    float squeeze = Math.abs((float) Math.cos(Math.toRadians(p.rot)));
                    c.rotate(-p.rot);
                    rect.set(-s * Math.max(0.15f, squeeze), -s, s * Math.max(0.15f, squeeze), s);
                    c.drawOval(rect, fill);
                    line.setColor(0xFFB8860B);
                    line.setStrokeWidth(s * 0.15f);
                    c.drawOval(rect, line);
                    break;
                default:
                    c.drawCircle(0, 0, s, fill);
                    break;
            }
            c.restore();
        }
        fill.setAlpha(255);
    }

    private void starPath(float s) {
        path.reset();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            float r = i % 2 == 0 ? s : s * 0.42f;
            float x = (float) Math.cos(a) * r, y = (float) Math.sin(a) * r;
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
    }

    /**
     * Word animation styles: 0 pop, 1 wobble, 2 zoom in from huge, 3 fly in an arc, 4 shake,
     * 5 rainbow wave (each letter bobs), 6 stretch.
     */
    private void drawWord(Canvas c, Particle p, float fade) {
        int alpha = Math.max(1, (int) (255 * fade));
        float a = p.age;
        float size = fit(p.label, p.size, getWidth() * 0.92f);
        c.save();
        c.translate(p.x, p.y);
        switch (p.style) {
            case 1: {
                float s = 1 + 0.18f * (float) Math.sin(a * 18);
                c.rotate((float) Math.sin(a * 9) * 10);
                c.scale(s, s);
                break;
            }
            case 2: {
                float s = a < 0.25f ? 2.8f - 1.8f * (a / 0.25f) : 1f;
                c.scale(s, s);
                c.rotate(p.rot * 0.5f);
                break;
            }
            case 3:
                c.rotate(p.rot + a * 120 * Math.signum(p.vx));
                break;
            case 4:
                c.translate((float) Math.sin(a * 70) * size * 0.12f, (float) Math.cos(a * 55) * size * 0.06f);
                break;
            case 5:
                drawWave(c, p.label, size, alpha, a);
                c.restore();
                return;
            case 6: {
                float k = (float) Math.exp(-a * 4) * (float) Math.sin(a * 25);
                c.scale(1 + 0.6f * k, 1 - 0.4f * k);
                break;
            }
            default: {
                float s = a < 0.12f ? 0.3f + 1.1f * (a / 0.12f) : (a < 0.25f ? 1.4f - (a - 0.12f) / 0.13f * 0.4f : 1f);
                c.rotate(p.rot);
                c.scale(s, s);
                break;
            }
        }
        drawOutlined(c, p.label, 0, 0, size, p.color, alpha);
        c.restore();
    }

    /** Letters bob up and down one after another, each in its own rainbow colour. */
    private void drawWave(Canvas c, String s, float size, int alpha, float age) {
        text.setTextSize(size);
        float total = text.measureText(s);
        float x = -total / 2;
        for (int i = 0; i < s.length(); i++) {
            String ch = s.substring(i, i + 1);
            text.setTextSize(size);
            float cw = text.measureText(ch);
            float dy = (float) Math.sin(age * 12 - i * 0.7f) * size * 0.25f;
            drawOutlined(c, ch, x + cw / 2, dy, size, RAINBOW[i % RAINBOW.length], alpha);
            x += cw;
        }
    }

    // ------------------------------------------------------------------ popo

    private void drawFlame(Canvas c, float x, float y, float r) {
        float flicker = 0.8f + 0.4f * rnd.nextFloat();
        int[] colors = {0xFFFF5722, 0xFFFF9800, 0xFFFFEB3B};
        for (int i = 0; i < colors.length; i++) {
            float fw = r * (1.2f - i * 0.35f), fh = r * (1.6f - i * 0.45f) * flicker;
            path.reset();
            path.moveTo(x - fw, y);
            path.quadTo(x - fw * 0.6f, y + fh * 0.6f, x, y + fh);
            path.quadTo(x + fw * 0.6f, y + fh * 0.6f, x + fw, y);
            path.close();
            fill.setColor(colors[i]);
            c.drawPath(path, fill);
        }
    }

    /** Skin colour: icy blue when the counter is in the minus, rosy red in fever mode. */
    private int skinColor() {
        if (state == PLAYING && counter < 0) {
            return blend(SKIN, SKIN_COLD, (float) Math.min(1, -counter / -GAME_OVER_AT) * 0.8f);
        }
        if (bossActive) return blend(0xFFCE93D8, Color.WHITE, now - bossHitAt < 0.08f ? 0.6f : 0f);
        int base = evoSkin();
        if (fever) base = blend(base, SKIN_HOT, 0.35f + 0.15f * (float) Math.sin(now * 8));
        // Lots of klatscher make it glow bright red, starting gently and getting really vivid.
        return blend(base, 0xFFFF1744, heat * heat * 0.9f);
    }

    /** Skin of the current evolution stage, morphing in over the first second after a transformation. */
    private int evoSkin() {
        if (evo == 0) return SKIN;
        int stage = evo - 1;
        int to;
        if (stage == 3) {
            to = Color.HSVToColor(new float[]{(now * 90) % 360, 0.45f, 1f});
        } else if (stage == 5) {
            to = blend(0xFFFF5722, 0xFFFFAB40, 0.5f + 0.5f * (float) Math.sin(now * 5));
        } else {
            to = EVO_COLORS[stage];
        }
        float t = Math.min(1f, (now - evoStart) / 1.0f);
        int from = stage == 0 ? SKIN : EVO_COLORS[stage - 1];
        return blend(from, to, t);
    }

    private static int blend(int a, int b, float t) {
        return Color.rgb((int) (Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                (int) (Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                (int) (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }

    private void drawPopo(Canvas c, float cx, float cy, float r, float phase) {
        float stroke = r * 0.08f;
        line.setStrokeWidth(stroke);
        float s = (float) Math.sin(phase);
        float bounce = r * 0.16f * energy;
        float leftY = cy + s * bounce;
        float rightY = cy - s * bounce;
        float tilt = (float) Math.sin(phase * 0.5f) * 7f * energy;
        float squash = 1f + 0.05f * energy * (float) Math.sin(phase * 2f);
        int skin = skinColor();

        // Motion lines when it's really going.
        if (energy > 0.5f) {
            int alpha = (int) (255 * Math.min(1f, (energy - 0.5f) * 1.5f));
            line.setColor(now < discoUntil || fever ? Color.WHITE : INK);
            line.setAlpha(alpha);
            for (int i = 0; i < 3; i++) {
                float y = cy - r * 0.5f + i * r * 0.5f;
                float len = r * (0.35f + 0.1f * i);
                c.drawLine(cx - r * 2.25f, y, cx - r * 2.25f + len, y, line);
                c.drawLine(cx + r * 2.25f - len, y, cx + r * 2.25f, y, line);
            }
            line.setAlpha(255);
        }

        c.save();
        c.rotate(tilt, cx, cy + r * 1.6f);
        drawLegs(c, cx, cy, r, stroke, skin);

        c.save();
        c.scale(squash, 2f - squash, cx, cy);
        float a = r * 1.02f;
        float b = r * 0.98f;
        float lx = cx - r * 0.86f;
        float rx = cx + r * 0.86f;

        // Cheeks: fills first, then outlines clipped so they meet in the crack.
        drawCheekFill(c, lx, leftY, a, b, skin);
        drawCheekFill(c, rx, rightY, a, b, skin);
        drawPrints(c, lx, leftY, rx, rightY, a, b);
        line.setColor(INK);
        c.save();
        c.clipRect(cx - r * 3f, cy - r * 3f, cx, cy + r * 3f);
        rect.set(lx - a, leftY - b, lx + a, leftY + b);
        c.drawOval(rect, line);
        c.restore();
        c.save();
        c.clipRect(cx, cy - r * 3f, cx + r * 3f, cy + r * 3f);
        rect.set(rx - a, rightY - b, rx + a, rightY + b);
        c.drawOval(rect, line);
        c.restore();

        // Crack.
        float hL = halfHeightAt(cx - lx, a, b);
        float hR = halfHeightAt(rx - cx, a, b);
        float top = Math.min(leftY - hL, rightY - hR);
        float bottom = Math.max(leftY + hL, rightY + hR);
        path.reset();
        path.moveTo(cx, top);
        path.quadTo(cx + s * r * 0.08f, (top + bottom) / 2f, cx, bottom);
        c.drawPath(path, line);

        if (evo >= 5) {
            // Galaxie and Lava: twinkling specks across the cheeks.
            for (int i = 0; i < 14; i++) {
                float px = (i * 0.618f) % 1f, py = (i * 0.382f + 0.13f) % 1f;
                float sx = (i % 2 == 0 ? lx : rx) + (px - 0.5f) * a * 1.3f;
                float sy = (i % 2 == 0 ? leftY : rightY) + (py - 0.5f) * b * 1.3f;
                fill.setColor(evo == 5 ? Color.WHITE : 0xFFFFF59D);
                fill.setAlpha((int) (140 + 115 * Math.sin(now * 3 + i)));
                c.drawCircle(sx, sy, r * (0.025f + 0.02f * (i % 3)), fill);
            }
            fill.setAlpha(255);
        }

        // Shine and blush.
        fill.setColor(0xD9FFFFFF);
        rect.set(lx - a * 0.55f, leftY - b * 0.62f, lx - a * 0.13f, leftY - b * 0.38f);
        c.drawOval(rect, fill);
        rect.set(rx + a * 0.13f, rightY - b * 0.62f, rx + a * 0.55f, rightY - b * 0.38f);
        c.drawOval(rect, fill);
        fill.setColor(BLUSH);
        float blush = r * (0.2f + 0.25f * heat);
        c.drawCircle(lx - a * 0.2f, leftY + b * 0.2f, blush, fill);
        c.drawCircle(rx + a * 0.2f, rightY + b * 0.2f, blush, fill);

        drawGlasses(c, bossActive ? 1 : wardrobe.worn(Wardrobe.GLASSES), lx, rx, leftY, rightY, a, b, stroke);
        float hatBase = Math.min(leftY, rightY) - b * 0.72f;
        if (now < crownUntil) drawCrown(c, cx, hatBase, r, stroke);
        else drawHat(c, bossActive ? 5 : wardrobe.worn(Wardrobe.HATS), cx, hatBase, r, stroke);

        c.restore();
        c.restore();
    }

    /** Red hand prints where you just klatsched; they fade within a second. */
    private void drawPrints(Canvas c, float lx, float ly, float rx, float ry, float a, float b) {
        for (float[] pr : prints) {
            if (pr == null) continue;
            float age = now - pr[4];
            if (age > 1f) continue;
            float x = (pr[0] < 0 ? lx : rx) + pr[1] * a * 0.7f;
            float y = (pr[0] < 0 ? ly : ry) + pr[2] * b * 0.6f;
            float u = a * 0.17f;
            fill.setColor(0xFFFF5252);
            fill.setAlpha((int) (150 * (1 - age)));
            c.save();
            c.translate(x, y);
            c.rotate(pr[3]);
            rect.set(-u, -u * 0.6f, u, u * 1.2f);
            c.drawOval(rect, fill);
            for (int f = 0; f < 4; f++) {
                float fx = -u * 0.75f + f * u * 0.5f;
                rect.set(fx - u * 0.2f, -u * (1.9f - Math.abs(f - 1.5f) * 0.25f), fx + u * 0.2f, -u * 0.3f);
                c.drawRoundRect(rect, u * 0.2f, u * 0.2f, fill);
            }
            c.save();
            c.rotate(-50, -u * 0.9f, u * 0.2f);
            rect.set(-u * 1.1f, -u * 0.5f, -u * 0.7f, u * 0.4f);
            c.drawRoundRect(rect, u * 0.2f, u * 0.2f, fill);
            c.restore();
            c.restore();
        }
        fill.setAlpha(255);
    }

    private void drawHat(Canvas c, int hat, float cx, float base, float r, float stroke) {
        if (hat == 0) return;
        line.setColor(INK);
        line.setStrokeWidth(stroke * 0.8f);
        switch (hat) {
            case 1: // cap
                rect.set(cx - r * 0.5f, base - r * 0.5f, cx + r * 0.5f, base + r * 0.5f);
                fill.setColor(0xFFE53935);
                c.drawArc(rect, 180, 180, true, fill);
                c.drawArc(rect, 180, 180, true, line);
                rect.set(cx + r * 0.2f, base - r * 0.08f, cx + r * 0.95f, base + r * 0.06f);
                c.drawRoundRect(rect, r * 0.07f, r * 0.07f, fill);
                c.drawRoundRect(rect, r * 0.07f, r * 0.07f, line);
                break;
            case 2: // party hat
                path.reset();
                path.moveTo(cx - r * 0.38f, base);
                path.lineTo(cx, base - r * 0.95f);
                path.lineTo(cx + r * 0.38f, base);
                path.close();
                fill.setColor(0xFF42A5F5);
                c.drawPath(path, fill);
                line.setColor(0xFFFFEB3B);
                for (int i = 1; i <= 2; i++) {
                    float y = base - r * 0.3f * i, hw = r * 0.38f * (1 - 0.3f * i / 0.95f);
                    c.drawLine(cx - hw, y, cx + hw, y, line);
                }
                line.setColor(INK);
                c.drawPath(path, line);
                fill.setColor(0xFFFF4081);
                c.drawCircle(cx, base - r * 0.95f, r * 0.1f, fill);
                break;
            case 3: // cowboy hat
                fill.setColor(0xFF8D6E63);
                rect.set(cx - r * 1.0f, base - r * 0.16f, cx + r * 1.0f, base + r * 0.12f);
                c.drawOval(rect, fill);
                c.drawOval(rect, line);
                rect.set(cx - r * 0.42f, base - r * 0.62f, cx + r * 0.42f, base);
                c.drawRoundRect(rect, r * 0.2f, r * 0.2f, fill);
                c.drawRoundRect(rect, r * 0.2f, r * 0.2f, line);
                fill.setColor(0xFF4E342E);
                c.drawRect(cx - r * 0.42f, base - r * 0.16f, cx + r * 0.42f, base - r * 0.04f, fill);
                break;
            case 4: // top hat
                fill.setColor(0xFF212121);
                rect.set(cx - r * 0.7f, base - r * 0.1f, cx + r * 0.7f, base + r * 0.06f);
                c.drawRoundRect(rect, r * 0.05f, r * 0.05f, fill);
                rect.set(cx - r * 0.4f, base - r * 0.9f, cx + r * 0.4f, base - r * 0.05f);
                c.drawRect(rect, fill);
                fill.setColor(0xFFE53935);
                c.drawRect(cx - r * 0.4f, base - r * 0.28f, cx + r * 0.4f, base - r * 0.14f, fill);
                line.setColor(Color.WHITE);
                line.setStrokeWidth(stroke * 0.4f);
                c.drawLine(cx - r * 0.3f, base - r * 0.8f, cx - r * 0.3f, base - r * 0.4f, line);
                line.setColor(INK);
                break;
            default: // viking helmet, also worn by every boss
                fill.setColor(0xFFF5F5F5);
                for (int side = -1; side <= 1; side += 2) {
                    path.reset();
                    path.moveTo(cx + side * r * 0.4f, base - r * 0.2f);
                    path.quadTo(cx + side * r * 0.95f, base - r * 0.35f, cx + side * r * 0.85f, base - r * 0.95f);
                    path.quadTo(cx + side * r * 0.7f, base - r * 0.45f, cx + side * r * 0.35f, base - r * 0.42f);
                    path.close();
                    c.drawPath(path, fill);
                    c.drawPath(path, line);
                }
                rect.set(cx - r * 0.55f, base - r * 0.6f, cx + r * 0.55f, base + r * 0.5f);
                fill.setColor(0xFF90A4AE);
                c.drawArc(rect, 180, 180, true, fill);
                c.drawArc(rect, 180, 180, true, line);
                fill.setColor(0xFFFFC107);
                c.drawRect(cx - r * 0.55f, base - r * 0.1f, cx + r * 0.55f, base + r * 0.02f, fill);
                fill.setColor(0xFF607D8B);
                for (int i = -2; i <= 2; i++) c.drawCircle(cx + i * r * 0.2f, base - r * 0.04f, r * 0.035f, fill);
                break;
        }
        line.setStrokeWidth(stroke);
    }

    /** Glasses sit across the upper cheeks, which makes the popo look like a face. */
    private void drawGlasses(Canvas c, int kind, float lx, float rx, float ly, float ry, float a, float b, float stroke) {
        if (kind == 0) return;
        float gy = Math.min(ly, ry) - b * 0.18f, lr = a * 0.34f;
        float l = lx + a * 0.22f, rr = rx - a * 0.22f;
        line.setColor(INK);
        line.setStrokeWidth(stroke * (kind == 2 ? 1.1f : 0.7f));
        c.drawLine(l + lr, gy, rr - lr, gy, line);
        for (float gx : new float[]{l, rr}) {
            switch (kind) {
                case 1: // sunglasses
                    fill.setColor(0xFF212121);
                    rect.set(gx - lr, gy - lr * 0.7f, gx + lr, gy + lr * 0.7f);
                    c.drawRoundRect(rect, lr * 0.4f, lr * 0.4f, fill);
                    fill.setColor(0x66FFFFFF);
                    c.drawCircle(gx - lr * 0.4f, gy - lr * 0.25f, lr * 0.18f, fill);
                    break;
                case 2: // nerd glasses
                    fill.setColor(0x99E1F5FE);
                    c.drawCircle(gx, gy, lr * 0.85f, fill);
                    c.drawCircle(gx, gy, lr * 0.85f, line);
                    break;
                default: // heart glasses
                    fill.setColor(0xFFFF1744);
                    float hs = lr * 1.1f;
                    path.reset();
                    path.moveTo(gx, gy + hs * 0.7f);
                    path.cubicTo(gx - hs * 1.2f, gy - hs * 0.1f, gx - hs * 0.5f, gy - hs * 0.9f, gx, gy - hs * 0.35f);
                    path.cubicTo(gx + hs * 0.5f, gy - hs * 0.9f, gx + hs * 1.2f, gy - hs * 0.1f, gx, gy + hs * 0.7f);
                    path.close();
                    c.drawPath(path, fill);
                    line.setStrokeWidth(stroke * 0.5f);
                    c.drawPath(path, line);
                    break;
            }
        }
        line.setStrokeWidth(stroke);
    }

    /** Boss health bar and timer under the title. */
    private void drawBossBar(Canvas c, float w, float h) {
        float bw = unit * 0.7f, bh = unit * 0.05f, bx = w / 2f - bw / 2f, by = h * 0.215f;
        String label = "👿 " + bossName + "  ·  " + (int) Math.ceil(Math.max(0, bossEnd - playTime)) + " s";
        drawOutlined(c, label, w / 2f, by - unit * 0.015f, fit(label, unit * 0.045f, w * 0.9f), Color.WHITE, 0);
        fill.setColor(0x66000000);
        rect.set(bx, by, bx + bw, by + bh);
        c.drawRoundRect(rect, bh / 2, bh / 2, fill);
        fill.setColor(PURPLE);
        rect.set(bx, by, bx + Math.max(bh, bw * bossHp / (float) bossMaxHp), by + bh);
        c.drawRoundRect(rect, bh / 2, bh / 2, fill);
        line.setColor(INK);
        line.setStrokeWidth(unit * 0.006f);
        rect.set(bx, by, bx + bw, by + bh);
        c.drawRoundRect(rect, bh / 2, bh / 2, line);
    }

    private void drawWardrobe(Canvas c, float w, float h) {
        drawCard(c, card);
        float y = card.top + unit * 0.09f;
        String title = "👕 Garderobe";
        drawOutlined(c, title, w / 2f, y, unit * 0.065f, GOLD, 0);
        y += unit * 0.06f;
        text.setStyle(Paint.Style.FILL);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(INK);
        text.setTextSize(unit * 0.04f);
        c.drawText("🪙 " + fmt(wardrobe.taler()) + " Popo-Taler", w / 2f, y, text);
        y += unit * 0.03f;
        int cols = 5;
        float cell = card.width() * 0.9f / cols, left = w / 2f - cell * cols / 2f;
        for (int cat = 0; cat < Wardrobe.ITEMS.length; cat++) {
            y += unit * 0.055f;
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(unit * 0.04f);
            text.setColor(INK);
            c.drawText(Wardrobe.CATEGORIES[cat], left, y, text);
            y += unit * 0.015f;
            int worn = wardrobe.worn(cat);
            for (int i = 0; i < Wardrobe.ITEMS[cat].length; i++) {
                float x0 = left + (i % cols) * cell, y0 = y + (i / cols) * cell;
                RectF r = wardrobeCells[cat][i];
                r.set(x0 + cell * 0.05f, y0 + cell * 0.05f, x0 + cell * 0.95f, y0 + cell * 0.95f);
                boolean owned = wardrobe.owned(cat, i);
                fill.setColor(i == worn ? 0xFFC8E6C9 : (owned ? Color.WHITE : 0xFFEEEEEE));
                c.drawRoundRect(r, cell * 0.15f, cell * 0.15f, fill);
                line.setColor(i == worn ? GREEN : INK);
                line.setStrokeWidth(unit * (i == worn ? 0.008f : 0.004f));
                c.drawRoundRect(r, cell * 0.15f, cell * 0.15f, line);
                String[] item = Wardrobe.ITEMS[cat][i];
                text.setTextAlign(Paint.Align.CENTER);
                text.setColor(INK);
                text.setAlpha(owned ? 255 : 140);
                text.setTextSize(cell * 0.38f);
                c.drawText(item[0], r.centerX(), r.top + cell * 0.45f, text);
                text.setAlpha(255);
                text.setTextSize(fit(item[1], cell * 0.15f, r.width() * 0.92f));
                c.drawText(item[1], r.centerX(), r.top + cell * 0.66f, text);
                String status = i == worn ? "an" : (owned ? "" : "🪙 " + item[2]);
                text.setTextSize(cell * 0.14f);
                text.setColor(i == worn ? GREEN : INK);
                c.drawText(status, r.centerX(), r.top + cell * 0.83f, text);
            }
            y += cell * ((Wardrobe.ITEMS[cat].length + cols - 1) / cols);
        }
        text.setColor(INK);
        text.setTextSize(unit * 0.033f);
        text.setAlpha(170);
        c.drawText("Taler gibt's fürs Spielen, für Bosse und Tages-Challenges.", w / 2f, card.bottom - unit * 0.04f, text);
        text.setAlpha(255);
    }

    private void drawCrown(Canvas c, float cx, float base, float r, float stroke) {
        float hw = r * 0.6f, h = r * 0.6f;
        path.reset();
        path.moveTo(cx - hw, base);
        path.lineTo(cx - hw, base - h * 0.75f);
        path.lineTo(cx - hw * 0.5f, base - h * 0.35f);
        path.lineTo(cx, base - h);
        path.lineTo(cx + hw * 0.5f, base - h * 0.35f);
        path.lineTo(cx + hw, base - h * 0.75f);
        path.lineTo(cx + hw, base);
        path.close();
        fill.setColor(GOLD);
        c.drawPath(path, fill);
        line.setColor(INK);
        line.setStrokeWidth(stroke * 0.8f);
        c.drawPath(path, line);
        fill.setColor(0xFFE53935);
        c.drawCircle(cx, base - h * 0.3f, r * 0.08f, fill);
        fill.setColor(0xFF2196F3);
        c.drawCircle(cx - hw * 0.55f, base - h * 0.18f, r * 0.06f, fill);
        c.drawCircle(cx + hw * 0.55f, base - h * 0.18f, r * 0.06f, fill);
        line.setStrokeWidth(stroke);
    }

    private void drawCheekFill(Canvas c, float x, float y, float a, float b, int skin) {
        fill.setColor(skin);
        rect.set(x - a, y - b, x + a, y + b);
        c.drawOval(rect, fill);
        fill.setColor(SKIN_SHADE);
        rect.set(x - a * 0.75f, y + b * 0.2f, x + a * 0.75f, y + b * 0.9f);
        c.drawOval(rect, fill);
    }

    private void drawLegs(Canvas c, float cx, float cy, float r, float stroke, int skin) {
        float legW = r * 0.62f;
        float legTop = cy + r * 0.3f;
        float[] xs = {cx - r * 0.95f, cx + r * 0.95f};
        line.setColor(INK);
        for (int k = 0; k < 2; k++) {
            float x = xs[k];
            float legBottom = cy + r * 2.2f - legLift[k];
            rect.set(x - legW / 2, legTop, x + legW / 2, legBottom);
            fill.setColor(skin);
            c.drawRoundRect(rect, legW / 2, legW / 2, fill);
            c.drawRoundRect(rect, legW / 2, legW / 2, line);
            // Sneaker.
            rect.set(x - legW * 0.75f, legBottom - r * 0.25f, x + legW * 0.75f, legBottom + r * 0.2f);
            int sneaker = SNEAKER_COLORS[outfit % SNEAKER_COLORS.length];
            fill.setColor(sneaker);
            c.drawRoundRect(rect, r * 0.2f, r * 0.2f, fill);
            c.drawRoundRect(rect, r * 0.2f, r * 0.2f, line);
            fill.setColor(sneaker == Color.WHITE ? 0xFFE53935 : Color.WHITE);
            c.drawRect(x - legW * 0.7f, legBottom + r * 0.09f, x + legW * 0.7f, legBottom + r * 0.14f, fill);
        }
        // Shorts pulled down around the knees, in today's outfit.
        float shortsTop = cy + r * 1.25f;
        float shortsH = r * 0.55f;
        float left = cx - r * 1.6f;
        float width = r * 3.2f;
        RectF shorts = new RectF(left, shortsTop, left + width, shortsTop + shortsH);
        int wornShorts = wardrobe.worn(Wardrobe.SHORTS);
        fill.setColor(wornShorts > 0 ? SHORTS_COLORS[wornShorts - 1] : SHORTS_COLORS[outfit % SHORTS_COLORS.length]);
        c.drawRoundRect(shorts, r * 0.25f, r * 0.25f, fill);
        c.save();
        path.reset();
        path.addRoundRect(shorts, r * 0.25f, r * 0.25f, Path.Direction.CW);
        c.clipPath(path);
        drawPattern(c, shorts, r, (outfit / 3) % PATTERNS);
        c.restore();
        c.drawRoundRect(shorts, r * 0.25f, r * 0.25f, line);
    }

    /** Shorts patterns: dots, stripes, hearts, stars, checks, zigzag, flowers, plain with a waistband. */
    private void drawPattern(Canvas c, RectF b, float r, int pattern) {
        fill.setColor(Color.WHITE);
        fill.setAlpha(220);
        float h = b.height();
        int i = 0;
        switch (pattern) {
            case 0:
                for (float x = b.left + r * 0.25f; x < b.right; x += r * 0.32f, i++) {
                    c.drawCircle(x, b.top + (i % 2 == 0 ? h * 0.33f : h * 0.68f), r * 0.07f, fill);
                }
                break;
            case 1:
                for (float x = b.left - h; x < b.right; x += r * 0.35f) {
                    path.reset();
                    path.moveTo(x, b.bottom);
                    path.lineTo(x + r * 0.15f, b.bottom);
                    path.lineTo(x + r * 0.15f + h, b.top);
                    path.lineTo(x + h, b.top);
                    path.close();
                    c.drawPath(path, fill);
                }
                break;
            case 2:
                fill.setColor(0xFFFF80AB);
                for (float x = b.left + r * 0.3f; x < b.right; x += r * 0.45f, i++) {
                    float y = b.top + (i % 2 == 0 ? h * 0.38f : h * 0.62f), u = r * 0.08f;
                    c.drawCircle(x - u * 0.5f, y - u * 0.3f, u * 0.6f, fill);
                    c.drawCircle(x + u * 0.5f, y - u * 0.3f, u * 0.6f, fill);
                    path.reset();
                    path.moveTo(x - u * 1.05f, y - u * 0.1f);
                    path.lineTo(x + u * 1.05f, y - u * 0.1f);
                    path.lineTo(x, y + u);
                    path.close();
                    c.drawPath(path, fill);
                }
                break;
            case 3:
                fill.setColor(0xFFFFEB3B);
                for (float x = b.left + r * 0.3f; x < b.right; x += r * 0.42f, i++) {
                    c.save();
                    c.translate(x, b.top + (i % 2 == 0 ? h * 0.35f : h * 0.65f));
                    starPath(r * 0.11f);
                    c.drawPath(path, fill);
                    c.restore();
                }
                break;
            case 4:
                float q = h / 3;
                for (int row = 0; row < 3; row++) {
                    for (float x = b.left + (row % 2) * q; x < b.right; x += q * 2) {
                        c.drawRect(x, b.top + row * q, x + q, b.top + (row + 1) * q, fill);
                    }
                }
                break;
            case 5:
                line.setColor(Color.WHITE);
                line.setStrokeWidth(r * 0.06f);
                path.reset();
                path.moveTo(b.left, b.top + h * 0.5f);
                for (float x = b.left; x < b.right; x += r * 0.2f, i++) {
                    path.lineTo(x + r * 0.2f, b.top + (i % 2 == 0 ? h * 0.25f : h * 0.75f));
                }
                c.drawPath(path, line);
                line.setColor(INK);
                break;
            case 6:
                for (float x = b.left + r * 0.35f; x < b.right; x += r * 0.55f, i++) {
                    float y = b.top + (i % 2 == 0 ? h * 0.4f : h * 0.6f);
                    fill.setColor(Color.WHITE);
                    for (int k = 0; k < 5; k++) {
                        double a = k * Math.PI * 2 / 5;
                        c.drawCircle(x + (float) Math.cos(a) * r * 0.08f, y + (float) Math.sin(a) * r * 0.08f, r * 0.06f, fill);
                    }
                    fill.setColor(0xFFFFEB3B);
                    c.drawCircle(x, y, r * 0.05f, fill);
                }
                break;
            default:
                c.drawRect(b.left, b.top, b.right, b.top + h * 0.25f, fill);
                break;
        }
        fill.setAlpha(255);
    }

    /** Half the height of an ellipse at horizontal distance dx from its centre. */
    private static float halfHeightAt(float dx, float a, float b) {
        float t = 1f - (dx / a) * (dx / a);
        return t <= 0f ? 0f : b * (float) Math.sqrt(t);
    }
}
