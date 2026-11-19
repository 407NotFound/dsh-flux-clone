package local.flux;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/** 悬浮球 + 展开面板:1.1 的美化界面都在这 */
public class OverlayService extends Service {

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

    private WindowManager.LayoutParams params(int w, int h, boolean touchable) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                w, h,
                Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = 32;
        p.y = 220;
        if (!touchable) p.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
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

        ballParams = params(dp(52), dp(52), true);
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
        head.setText("FLUX  ·  v1.1");
        head.setTextColor(Color.parseColor("#7DF9FF"));
        head.setTextSize(15);
        box.addView(head);

        TextView hint = new TextView(this);
        hint.setText("皮肤开关");
        hint.setTextColor(Color.parseColor("#8A94A6"));
        hint.setTextSize(11);
        hint.setPadding(0, dp(10), 0, dp(6));
        box.addView(hint);

        ScrollView sc = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        for (Skin s : skins) list.addView(row(s));
        sc.addView(list);
        sc.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(210)));
        box.addView(sc);

        panelParams = params(dp(250), WindowManager.LayoutParams.WRAP_CONTENT, true);
        panel = box;
    }

    private View row(Skin s) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0, dp(8), 0, dp(8));

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

        Switch sw = new Switch(this);
        sw.setChecked(s.enabled);
        sw.setOnCheckedChangeListener((CompoundButton b, boolean checked) -> {
            s.enabled = checked;
            SkinStore.save(this, skins);
        });
        r.addView(sw);
        return r;
    }

    private void togglePanel() {
        if (panel.getParent() != null) {
            wm.removeView(panel);
        } else {
            panelParams.x = ballParams.x + dp(60);
            panelParams.y = ballParams.y;
            wm.addView(panel, panelParams);
        }
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

    /** 拖动悬浮球 */
    private class DragTouch implements View.OnTouchListener {
        private int startX, startY;
        private float downX, downY;

        @Override
        public boolean onTouch(View v, MotionEvent e) {
            switch (e.getAction()) {
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
                default:
                    return false;
            }
        }
    }
}
