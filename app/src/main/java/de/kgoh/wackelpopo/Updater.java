package de.kgoh.wackelpopo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Installs the newest Wackel-Popo straight from GitHub: version.txt in the repo holds the version
 * code and name of wackel-popo-debug.apk next to it. A token is only needed while the repo is private.
 */
final class Updater {
    static final String ACTION_INSTALLED = "de.kgoh.wackelpopo.INSTALLED";
    private static final String API = "https://api.github.com/repos/opiswahn/WackelPopo/contents/";
    private static final String APK = "wackel-popo-debug.apk";

    interface Listener {
        void onUpdateAvailable(String versionName);

        void onUpToDate();

        void onNeedsToken();

        void onProgress(int percent);

        void onFailed(String reason);
    }

    private final Activity activity;
    private final Listener listener;
    private final SharedPreferences prefs;
    private volatile boolean busy;

    Updater(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
        prefs = MainActivity.prefs(activity);
    }

    String token() {
        return prefs.getString("token", "");
    }

    void setToken(String token) {
        prefs.edit().putString("token", token).apply();
    }

    /** Looks for a newer version in the background and tells the listener what it found. */
    void check() {
        if (busy) return;
        busy = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] remote = readVersion();
                    int code = Integer.parseInt(remote[0]);
                    if (code > installedVersion()) tell(AVAILABLE, remote.length > 1 ? remote[1] : remote[0], 0);
                    else tell(UP_TO_DATE, null, 0);
                } catch (NeedsToken e) {
                    tell(NEEDS_TOKEN, null, 0);
                } catch (Exception e) {
                    // No network or GitHub down: stay quiet, the next start tries again.
                    tell(UP_TO_DATE, null, 0);
                } finally {
                    busy = false;
                }
            }
        }).start();
    }

    /** Downloads the APK into a PackageInstaller session; Android then asks the player to confirm. */
    void install() {
        if (busy) return;
        busy = true;
        tell(PROGRESS, null, 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                PackageInstaller.Session session = null;
                try {
                    HttpURLConnection c = open(APK);
                    long length = c.getContentLengthLong();
                    PackageInstaller installer = activity.getPackageManager().getPackageInstaller();
                    PackageInstaller.SessionParams params =
                            new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    params.setAppPackageName(activity.getPackageName());
                    session = installer.openSession(installer.createSession(params));
                    try (InputStream in = c.getInputStream(); OutputStream out = session.openWrite(APK, 0, length)) {
                        byte[] buf = new byte[64 * 1024];
                        long done = 0;
                        int lastPercent = -2;
                        for (int n; (n = in.read(buf)) > 0; ) {
                            out.write(buf, 0, n);
                            done += n;
                            int percent = length > 0 ? (int) (done * 100 / length) : -1;
                            if (percent != lastPercent) tell(PROGRESS, null, lastPercent = percent);
                        }
                        session.fsync(out);
                    }
                    Intent result = new Intent(activity, MainActivity.class).setAction(ACTION_INSTALLED);
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT
                            | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
                    session.commit(PendingIntent.getActivity(activity, 0, result, flags).getIntentSender());
                    session.close();
                } catch (NeedsToken e) {
                    if (session != null) session.abandon();
                    tell(NEEDS_TOKEN, null, 0);
                } catch (Exception e) {
                    if (session != null) session.abandon();
                    tell(FAILED, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), 0);
                } finally {
                    busy = false;
                }
            }
        }).start();
    }

    private static final int AVAILABLE = 0, UP_TO_DATE = 1, NEEDS_TOKEN = 2, PROGRESS = 3, FAILED = 4;

    /** Hands a result to the listener on the UI thread. */
    private void tell(final int what, final String text, final int percent) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (what) {
                    case AVAILABLE:
                        listener.onUpdateAvailable(text);
                        break;
                    case UP_TO_DATE:
                        listener.onUpToDate();
                        break;
                    case NEEDS_TOKEN:
                        listener.onNeedsToken();
                        break;
                    case PROGRESS:
                        listener.onProgress(percent);
                        break;
                    default:
                        listener.onFailed(text);
                        break;
                }
            }
        });
    }

    private String[] readVersion() throws IOException {
        HttpURLConnection c = open("version.txt");
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            for (int n; (n = in.read(buf)) > 0; ) bytes.write(buf, 0, n);
            return bytes.toString("UTF-8").trim().split("\\s+");
        }
    }

    private HttpURLConnection open(String path) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(API + path + "?ref=main").openConnection();
        c.setRequestProperty("Accept", "application/vnd.github.raw");
        c.setRequestProperty("User-Agent", "Wackel-Popo");
        String token = token();
        if (!token.isEmpty()) c.setRequestProperty("Authorization", "Bearer " + token);
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        int code = c.getResponseCode();
        // GitHub answers 404 for a private repo without a valid token.
        if (code == 401 || code == 403 || code == 404) throw new NeedsToken();
        if (code != 200) throw new IOException("GitHub antwortet " + code);
        return c;
    }

    private int installedVersion() {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    private static final class NeedsToken extends IOException {
    }
}
