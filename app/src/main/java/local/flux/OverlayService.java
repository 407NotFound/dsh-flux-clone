package local.flux;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** 全屏透明 SurfaceView + 原生 ImGui 渲染 */
public class OverlayService extends Service implements SurfaceHolder.Callback {

    private WindowManager wm;
    private SurfaceView sv;
    private volatile boolean running;
    private Thread renderThread;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, notification());
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        showSurface();
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
                .setContentText("ImGui 界面已开启")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pi)
                .build();
    }

    private void showSurface() {
        sv = new SurfaceView(this);
        sv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        sv.getHolder().addCallback(this);

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= 26
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                flags, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;

        sv.setOnTouchListener((v, e) -> {
            NativeBridge.nativeTouch(e.getActionMasked(), e.getX(), e.getY());
            return true;
        });

        wm.addView(sv, p);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        NativeBridge.nativeStart(holder.getSurface(), getAssets());
        startRenderLoop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        NativeBridge.nativeResize(w, h);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRenderLoop();
        NativeBridge.nativeStop();
    }

    private void startRenderLoop() {
        if (running) return;
        running = true;
        renderThread = new Thread(() -> {
            while (running) {
                NativeBridge.nativeStep();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException ignored) {
                }
            }
        }, "flux-render");
        renderThread.start();
    }

    private void stopRenderLoop() {
        running = false;
        if (renderThread != null) {
            try {
                renderThread.join(500);
            } catch (InterruptedException ignored) {
            }
            renderThread = null;
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
        stopRenderLoop();
        if (sv != null && sv.getParent() != null) wm.removeView(sv);
        super.onDestroy();
    }
}
