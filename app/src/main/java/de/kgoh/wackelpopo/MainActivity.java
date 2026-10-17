package de.kgoh.wackelpopo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Hosts the game and a small pill near the bottom of the start screen that offers the newest version from GitHub. */
public class MainActivity extends Activity implements Updater.Listener {
    private WackelView game;
    private TextView pill;
    private Updater updater;
    private final Handler handler = new Handler(Looper.getMainLooper());
    /** The pill's text in both languages, so a language switch shows up on the next refresh. */
    private String pillText, pillTextEn;
    private int pillAction;
    private long lastCheck;
    private long committedAt;
    private final BroadcastReceiver installReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleInstallResult(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        game = new WackelView(this);
        pill = new TextView(this);
        pill.setTextColor(0xFFFFFFFF);
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        int pad = dp(8);
        pill.setPadding(dp(14), pad, dp(14), pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xEE2B1B17);
        bg.setCornerRadius(dp(20));
        pill.setBackground(bg);
        pill.setVisibility(View.GONE);
        pill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                perform(pillAction);
            }
        });
        FrameLayout root = new FrameLayout(this);
        root.addView(game);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        // Between "Tippen zum Starten!" and the daily challenge, where the thumb reaches and no notch is.
        lp.bottomMargin = (int) (getResources().getDisplayMetrics().heightPixels * 0.1f);
        root.addView(pill, lp);
        setContentView(root);
        updater = new Updater(this, this);
        handleInstallResult(getIntent());
        IntentFilter filter = new IntentFilter(Updater.ACTION_INSTALLED);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(installReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(installReceiver, filter);
        handler.post(new Runnable() {
            @Override
            public void run() {
                // The pill only shows on the start screen, so it never covers the game or the game-over card.
                pill.setVisibility(pillText != null && game.isOnStartScreen() ? View.VISIBLE : View.GONE);
                if (pillText != null) {
                    String text = Lang.t(pillText, pillTextEn);
                    if (!text.contentEquals(pill.getText())) pill.setText(text);
                }
                handler.postDelayed(this, 400);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        game.setMusicActive(true);
        if (System.currentTimeMillis() - lastCheck > 10 * 60 * 1000) {
            lastCheck = System.currentTimeMillis();
            updater.check();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        game.setMusicActive(false);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(installReceiver);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleInstallResult(intent);
    }

    private void handleInstallResult(Intent intent) {
        if (intent == null || !Updater.ACTION_INSTALLED.equals(intent.getAction())) return;
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        int status = extras.getInt(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        committedAt = 0;
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = (Intent) extras.get(Intent.EXTRA_INTENT);
            if (confirm != null) startActivity(confirm);
        } else if (status == PackageInstaller.STATUS_SUCCESS) {
            show("Update installiert!", "Update installed!", NONE);
        } else {
            String msg = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (status == PackageInstaller.STATUS_FAILURE_CONFLICT || status == PackageInstaller.STATUS_FAILURE_INCOMPATIBLE) {
                show("Passt nicht zur installierten App: alte App einmal deinstallieren. Tippen für Download",
                        "Doesn't fit the installed app: uninstall the old one once. Tap to download", BROWSER);
                return;
            }
            String detail = msg != null ? " (" + msg + ")" : "";
            show("Update abgebrochen" + detail + ". Nochmal versuchen?",
                    "Update cancelled" + detail + ". Try again?", INSTALL);
        }
    }

    // ------------------------------------------------------------ Updater.Listener

    @Override
    public void onUpdateAvailable(String versionName) {
        show("⬇ Neue Version " + versionName + ": tippen zum Installieren",
                "⬇ New version " + versionName + ": tap to install", START_INSTALL);
    }

    @Override
    public void onUpToDate() {
        show(null, null, NONE);
    }

    @Override
    public void onProgress(int percent) {
        show("Lade Update … " + (percent >= 0 ? percent + " %" : ""),
                "Downloading update … " + (percent >= 0 ? percent + "%" : ""), NONE);
    }

    @Override
    public void onCommitted() {
        show("Android fragt gleich nach der Installation …",
                "Android will ask about installing in a moment …", NONE);
        final long at = committedAt = System.currentTimeMillis();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // No answer from Android at all: offer the download in the browser instead.
                if (committedAt == at) {
                    show("Keine Rückmeldung von Android. Tippen für Download im Browser",
                            "No answer from Android. Tap to download in the browser", BROWSER);
                }
            }
        }, 15000);
    }

    @Override
    public void onFailed(String reason) {
        show("Update fehlgeschlagen: " + reason + ". Tippen für neuen Versuch",
                "Update failed: " + reason + ". Tap to try again", CHECK);
    }

    private void startInstall() {
        if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            show("Erlaube „Unbekannte Apps installieren“ und tippe dann nochmal",
                    "Allow \u201cInstall unknown apps\u201d, then tap again", START_INSTALL);
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));
            return;
        }
        updater.install();
    }

    private static final int NONE = 0, INSTALL = 1, CHECK = 2, START_INSTALL = 3, BROWSER = 4;

    private void perform(int action) {
        switch (action) {
            case INSTALL:
                updater.install();
                break;
            case CHECK:
                updater.check();
                break;
            case START_INSTALL:
                startInstall();
                break;
            case BROWSER:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Updater.RELEASES_PAGE)));
                break;
            default:
                break;
        }
    }

    /** Shows the pill with a German and an English text (both null hides it). */
    private void show(String text, String textEn, int action) {
        pillText = text;
        pillTextEn = textEn;
        pillAction = action;
        pill.setText(text == null ? null : Lang.t(text, textEn));
        pill.setVisibility(text != null && game.isOnStartScreen() ? View.VISIBLE : View.GONE);
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }
}
