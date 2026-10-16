package de.kgoh.wackelpopo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Keeps the ten best rounds on the device. */
public class HighscoreStore {
    public static final int MAX_ENTRIES = 10;

    public static class Entry {
        public final int score;
        public final long time;

        Entry(int score, long time) {
            this.score = score;
            this.time = time;
        }
    }

    private final SharedPreferences prefs;
    private final List<Entry> entries = new ArrayList<Entry>();

    public HighscoreStore(Context context) {
        prefs = context.getSharedPreferences("highscores", Context.MODE_PRIVATE);
        String raw = prefs.getString("entries", "");
        for (String part : raw.split(";")) {
            String[] f = part.split("\\|");
            if (f.length != 2) continue;
            try {
                entries.add(new Entry(Integer.parseInt(f[0]), Long.parseLong(f[1])));
            } catch (NumberFormatException ignored) {
                // Skip anything we can't read.
            }
        }
        sort();
    }

    public List<Entry> entries() {
        return entries;
    }

    public int best() {
        return entries.isEmpty() ? 0 : entries.get(0).score;
    }

    /** Adds a finished round and returns its place (1-based), or 0 if it didn't make the list. */
    public int add(int score, long time) {
        Entry entry = new Entry(score, time);
        entries.add(entry);
        sort();
        while (entries.size() > MAX_ENTRIES) entries.remove(entries.size() - 1);
        int place = entries.indexOf(entry) + 1;
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            if (sb.length() > 0) sb.append(';');
            sb.append(e.score).append('|').append(e.time);
        }
        prefs.edit().putString("entries", sb.toString()).apply();
        return place;
    }

    private void sort() {
        Collections.sort(entries, new Comparator<Entry>() {
            @Override
            public int compare(Entry a, Entry b) {
                if (a.score != b.score) return b.score - a.score;
                return a.time < b.time ? -1 : (a.time == b.time ? 0 : 1);
            }
        });
    }
}
