package de.kgoh.wackelpopo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Installs the newest Wackel-Popo straight from GitHub. Every push to main builds a release tagged
 * build-<versionCode> with the APK attached; the app compares that versionCode with its own.
 */
final class Updater {
    static final String ACTION_INSTALLED = "de.kgoh.wackelpopo.INSTALLED";
    static final String RELEASES_PAGE = "https://github.com/opiswahn/WackelPopo/releases/latest";
    private static final String REPO = "https://api.github.com/repos/opiswahn/WackelPopo/";
    private static final String APK = "wackel-popo.apk";

    interface Listener {
        void onUpdateAvailable(String versionName);

        void onUpToDate();

        void onProgress(int percent);

        void onFailed(String reason);

        void onCommitted();
    }

    private final Activity activity;
    private final Listener listener;
    private volatile boolean busy;
    /** Where the newest APK lives, found by the last check. */
    private volatile String apkUrl;

    Updater(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    /** Looks for a newer version in the background and tells the listener what it found. */
    void check() {
        if (busy) return;
        busy = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] remote = readLatest();
                    int code = Integer.parseInt(remote[0]);
                    if (code > installedVersion()) tell(AVAILABLE, remote[1], 0);
                    else tell(UP_TO_DATE, null, 0);
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
                    HttpURLConnection c = open(apkUrl, "application/octet-stream");
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
                    // The result comes back as a broadcast: an activity start from the installer is blocked
                    // on newer Android versions, which left the player with a finished download and nothing else.
                    Intent result = new Intent(ACTION_INSTALLED).setPackage(activity.getPackageName());
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT
                            | (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0);
                    session.commit(PendingIntent.getBroadcast(activity, 0, result, flags).getIntentSender());
                    session.close();
                    tell(COMMITTED, null, 0);
                } catch (Exception e) {
                    if (session != null) session.abandon();
                    tell(FAILED, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName(), 0);
                } finally {
                    busy = false;
                }
            }
        }).start();
    }

    private static final int AVAILABLE = 0, UP_TO_DATE = 1, PROGRESS = 2, FAILED = 3, COMMITTED = 4;

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
                    case PROGRESS:
                        listener.onProgress(percent);
                        break;
                    case COMMITTED:
                        listener.onCommitted();
                        break;
                    default:
                        listener.onFailed(text);
                        break;
                }
            }
        });
    }

    /** Returns {versionCode, versionName} of the newest build and remembers where its APK is. */
    private String[] readLatest() throws IOException, JSONException {
        JSONObject release = new JSONObject(read(open(REPO + "releases/latest", "application/vnd.github+json")));
        String tag = release.getString("tag_name");
        JSONArray assets = release.getJSONArray("assets");
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            if (asset.getString("name").equals(APK) && tag.startsWith("build-")) {
                apkUrl = asset.getString("browser_download_url");
                String name = release.optString("name", tag).replace("Wackel-Popo ", "");
                return new String[]{tag.substring("build-".length()), name};
            }
        }
        throw new IOException(Lang.t("Kein APK im neuesten Release", "No APK in the latest release"));
    }

    private static String read(HttpURLConnection c) throws IOException {
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            for (int n; (n = in.read(buf)) > 0; ) bytes.write(buf, 0, n);
            return bytes.toString("UTF-8");
        }
    }

    private HttpURLConnection open(String url, String accept) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestProperty("Accept", accept);
        c.setRequestProperty("User-Agent", "Wackel-Popo");
        c.setConnectTimeout(15000);
        c.setReadTimeout(30000);
        int code = c.getResponseCode();
        if (code != 200) throw new IOException(Lang.t("GitHub antwortet ", "GitHub answers ") + code);
        return c;
    }

    private int installedVersion() {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }
}
