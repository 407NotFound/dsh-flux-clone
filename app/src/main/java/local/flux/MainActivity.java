package local.flux;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(56, 120, 56, 56);
        root.setBackgroundColor(Color.parseColor("#0B0F14"));

        TextView title = new TextView(this);
        title.setText("Flux HUD 1.4");
        title.setTextColor(Color.parseColor("#7DF9FF"));
        title.setTextSize(26);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("自建架构 · 悬浮菜单 · 动画 + 记忆位置");
        sub.setTextColor(Color.parseColor("#8A94A6"));
        sub.setTextSize(13);
        root.addView(sub);

        status = new TextView(this);
        status.setTextColor(Color.parseColor("#C8D2E0"));
        status.setTextSize(12);
        status.setPadding(0, 40, 0, 40);
        root.addView(status);

        root.addView(button("请求悬浮窗权限", v -> askOverlay()));
        root.addView(button("启动悬浮菜单", v -> startOverlay()));
        root.addView(button("停止", v -> stopService(new Intent(this, OverlayService.class))));

        setContentView(root);

        // 有权限就自动起 HUD
        if (Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this)) {
            startForegroundService(new Intent(this, OverlayService.class));
        }
    }

    private Button button(String text, View.OnClickListener l) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setOnClickListener(l);
        return btn;
    }

    private void askOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        } else {
            status.setText("悬浮窗权限已就绪");
        }
    }

    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            status.setText("先给悬浮窗权限");
            askOverlay();
            return;
        }
        startForegroundService(new Intent(this, OverlayService.class));
        status.setText("菜单已启动,回到游戏即可看到悬浮球");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) {
            boolean ok = Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(this);
            status.setText(ok ? "权限:悬浮窗 OK" : "权限:缺悬浮窗");
        }
    }
}
