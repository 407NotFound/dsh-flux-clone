package local.flux;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/** 悬浮球 + 展开面板:v1.2(动画/记忆位置/自绘开关/状态条) */
public class OverlayService extends Service {

    private static final String PREF = "flux_pos";

    private WindowManager wm;
    private WindowManager.LayoutParams ballParams;
    private WindowManager.LayoutParams panelParams;
    private View ball;
    private View panel;
    private List<Skin> skins;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        skins = SkinStore.load(this);
        startForeground(1, notification());
        buildBall();
        buildPanel();
    }

    private Notification notification() {
        String ch = "flux";
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel(ch, "Flux", NotificationManager.IMPORTANCE_LOW));
        }
        PendingIntent pi = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this, ch)
                .setContentTitle("FluxClone 运行中")
                .setContentText("悬浮菜单已开启")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pi)
                .build();
    }

    private WindowManager.LayoutParams params(int w, int h) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                w, h,
                Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        return p;
    }

    private void buildBall() {
        TextView v = new TextView(this);
        v.setText("F");
        v.setTextColor(Color.parseColor("#0B0F14"));
        v.setTextSize(18);
        v.setGravity(Gravity.CENTER);

        GradientDrawable g = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#7DF9FF"), Color.parseColor("#4D8BFF")});
        g.setShape(GradientDrawable.OVAL);
        v.setBackground(g);
        v.setElevation(12f);
        v.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        SharedPreferences sp = getSharedPreferences(PREF, 0);
        ballParams = params(dp(52), dp(52));
        ballParams.x = sp.getInt("x", dp(20));
        ballParams.y = sp.getInt("y", dp(220));
        ball = v;
        wm.addView(ball, ballParams);

        v.setOnTouchListener(new DragTouch());
        v.setOnClickListener(x -> togglePanel());
    }

    private void buildPanel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(16), dp(18), dp(16));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E60E131A"));
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), Color.parseColor("#2A7DF9FF"));
        box.setBackground(bg);
        box.setElevation(16f);

        TextView head = new TextView(this);
        head.setText("FLUX  ·  v1.2");
        head.setTextColor(Color.parseColor("#7DF9FF"));
        head.setTextSize(15);
        box.addView(head);

        TextView state = new TextView(this);
        state.setText(statusLine());
        state.setTextColor(Color.parseColor("#5F6B7D"));
        state.setTextSize(10);
        state.setPadding(0, dp(6), 0, dp(10));
        box.addView(state);

        ScrollView sc = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        for (Skin s : skins) list.addView(row(s, state));
        sc.addView(list);
        sc.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220)));
        box.addView(sc);

        panelParams = params(dp(250), WindowManager.LayoutParams.WRAP_CONTENT);
        panel = box;
    }

    private String statusLine() {
        int on = 0;
        for (Skin s : skins) if (s.enabled) on++;
        return "tunnel off · skins " + on + "/" + skins.size() + " active";
    }

    private View row(Skin s, TextView state) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(9), 0, dp(9));

        View thumb = new View(this);
        GradientDrawable td = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, colorsFor(s.id));
        td.setCornerRadius(dp(10));
        thumb.setBackground(td);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(dp(38), dp(38));
        tp.rightMargin = dp(12);
        thumb.setLayoutParams(tp);
        r.addView(thumb);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        TextView n = new TextView(this);
        n.setText(s.name);
        n.setTextColor(Color.parseColor("#E6ECF5"));
        n.setTextSize(13);
        TextView sub = new TextView(this);
        sub.setText(s.subtitle);
        sub.setTextColor(Color.parseColor("#6B7688"));
        sub.setTextSize(10);
        texts.addView(n);
        texts.addView(sub);
        texts.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        r.addView(texts);

        FluxToggle t = new FluxToggle(this, s.enabled);
        t.setOnChange(v -> {
            s.enabled = v;
            SkinStore.save(this, skins);
            state.setText(statusLine());
        });
        r.addView(t);
        return r;
    }

    private int[] colorsFor(String id) {
        switch (id) {
            case "ice": return new int[]{Color.parseColor("#BFE9FF"), Color.parseColor("#3A7BD5")};
            case "void": return new int[]{Color.parseColor("#2A2E3A"), Color.parseColor("#0B0F14")};
            case "sunset": return new int[]{Color.parseColor("#FF9A6C"), Color.parseColor("#B33B8E")};
            default: return new int[]{Color.parseColor("#7DF9FF"), Color.parseColor("#4D8BFF")};
        }
    }

    private void togglePanel() {
        if (panel.getParent() != null) {
            panel.animate().alpha(0f).scaleX(0.92f).scaleY(0.92f).setDuration(120)
                    .withEndAction(() -> {
                        if (panel.getParent() != null) wm.removeView(panel);
                    }).start();
            return;
        }

        int screenW = getResources().getDisplayMetrics().widthPixels;
        int panelW = dp(250);
        int x = ballParams.x + dp(60);
        if (x + panelW > screenW - dp(8)) x = Math.max(dp(8), screenW - panelW - dp(8));
        panelParams.x = x;
        panelParams.y = ballParams.y;

        panel.setAlpha(0f);
        panel.setScaleX(0.92f);
        panel.setScaleY(0.92f);
        wm.addView(panel, panelParams);
        panel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ball != null && ball.getParent() != null) wm.removeView(ball);
        if (panel != null && panel.getParent() != null) wm.removeView(panel);
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }

    private class DragTouch implements View.OnTouchListener {
        private int startX, startY;
        private float downX, downY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = ballParams.x;
                    startY = ballParams.y;
                    downX = e.getRawX();
                    downY = e.getRawY();
                    return false;
                case MotionEvent.ACTION_MOVE:
                    ballParams.x = startX + (int) (e.getRawX() - downX);
                    ballParams.y = startY + (int) (e.getRawY() - downY);
                    wm.updateViewLayout(ball, ballParams);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    int w = getResources().getDisplayMetrics().widthPixels;
                    int h = getResources().getDisplayMetrics().heightPixels;
                    ballParams.x = Math.max(0, Math.min(ballParams.x, w - dp(52)));
                    ballParams.y = Math.max(0, Math.min(ballParams.y, h - dp(52)));
                    wm.updateViewLayout(ball, ballParams);
                    getSharedPreferences(PREF, 0).edit()
                            .putInt("x", ballParams.x).putInt("y", ballParams.y).apply();
                    return true;
                default:
                    return false;
            }
        }
    }
}
