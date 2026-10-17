package de.kgoh.wackelpopo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Hosts the game and a small pill near the bottom of the start screen that offers the newest version from GitHub. */
public class MainActivity extends Activity implements Updater.Listener {
    private WackelView game;
    private TextView pill;
    private Updater updater;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String pillText;
    private int pillAction;
    private long lastCheck;

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
        lp.bottomMargin = (int) (getResources().getDisplayMetrics().heightPixels * 0.13f);
        root.addView(pill, lp);
        setContentView(root);
        updater = new Updater(this, this);
        handleInstallResult(getIntent());
        handler.post(new Runnable() {
            @Override
            public void run() {
                // The pill only shows on the start screen, so it never covers the game or the game-over card.
                pill.setVisibility(pillText != null && game.isOnStartScreen() ? View.VISIBLE : View.GONE);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleInstallResult(intent);
    }

    private void handleInstallResult(Intent intent) {
        if (intent == null || !Updater.ACTION_INSTALLED.equals(intent.getAction())) return;
        Bundle extras = intent.getExtras();
        if (extras == null) return;
        int status = extras.getInt(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = (Intent) extras.get(Intent.EXTRA_INTENT);
            if (confirm != null) startActivity(confirm);
        } else if (status == PackageInstaller.STATUS_SUCCESS) {
            show("Update installiert!", NONE);
        } else {
            String msg = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
            show("Update abgebrochen" + (msg != null ? " (" + msg + ")" : "") + ". Nochmal versuchen?", INSTALL);
        }
    }

    // ------------------------------------------------------------ Updater.Listener

    @Override
    public void onUpdateAvailable(String versionName) {
        show("⬇ Neue Version " + versionName + ": tippen zum Installieren", START_INSTALL);
    }

    @Override
    public void onUpToDate() {
        show(null, NONE);
    }

    @Override
    public void onNeedsToken() {
        show("🔑 Update braucht deinen GitHub-Zugang: tippen", ASK_TOKEN);
    }

    @Override
    public void onProgress(int percent) {
        show("Lade Update … " + (percent >= 0 ? percent + " %" : ""), NONE);
    }

    @Override
    public void onFailed(String reason) {
        show("Update fehlgeschlagen: " + reason + ". Tippen für neuen Versuch", CHECK);
    }

    private void startInstall() {
        if (Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            show("Erlaube „Unbekannte Apps installieren“ und tippe dann nochmal", START_INSTALL);
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));
            return;
        }
        updater.install();
    }

    private void askToken() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("github_pat_…");
        input.setText(updater.token());
        new AlertDialog.Builder(this)
                .setTitle("GitHub-Zugang")
                .setMessage("Das Repo ist privat. Leg auf GitHub einen Token mit Lesezugriff auf WackelPopo an "
                        + "(Settings → Developer settings → Fine-grained tokens, Contents: Read) und füg ihn hier ein.")
                .setView(input)
                .setPositiveButton("Speichern", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface d, int which) {
                        updater.setToken(input.getText().toString().trim());
                        updater.check();
                    }
                })
                .setNegativeButton("Abbrechen", null)
                .show();
    }

    private static final int NONE = 0, INSTALL = 1, CHECK = 2, START_INSTALL = 3, ASK_TOKEN = 4;

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
            case ASK_TOKEN:
                askToken();
                break;
            default:
                break;
        }
    }

    private void show(String text, int action) {
        pillText = text;
        pillAction = action;
        pill.setText(text);
        pill.setVisibility(text != null && game.isOnStartScreen() ? View.VISIBLE : View.GONE);
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    static SharedPreferences prefs(Activity a) {
        return a.getSharedPreferences("update", MODE_PRIVATE);
    }
}
