package local.flux;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** 自绘胶囊开关:开=青蓝渐变,关=灰;圆点带位移动画 */
public class FluxToggle extends View {

    public interface OnChange {
        void onChanged(boolean on);
    }

    private boolean on;
    private float pos;           // 0..1 圆点位置
    private OnChange listener;
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knob = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public FluxToggle(Context c, boolean initial) {
        super(c);
        this.on = initial;
        this.pos = initial ? 1f : 0f;
        knob.setColor(Color.WHITE);
        knob.setShadowLayer(6f, 0f, 2f, 0x66000000);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClickable(true);
        setOnClickListener(v -> setChecked(!on, true));
    }

    public void setOnChange(OnChange l) {
        this.listener = l;
    }

    public boolean isChecked() {
        return on;
    }

    public void setChecked(boolean value, boolean animate) {
        if (on == value) return;
        on = value;
        if (animate) {
            ValueAnimator a = ValueAnimator.ofFloat(pos, on ? 1f : 0f);
            a.setDuration(180);
            a.setInterpolator(new DecelerateInterpolator());
            a.addUpdateListener(an -> {
                pos = (float) an.getAnimatedValue();
                invalidate();
            });
            a.start();
        } else {
            pos = on ? 1f : 0f;
            invalidate();
        }
        if (listener != null) listener.onChanged(on);
    }

    @Override
    protected void onMeasure(int w, int h) {
        int width = dp(42);
        int height = dp(24);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        float r = h / 2f;
        rect.set(0, 0, w, h);

        // 轨道:开→青蓝渐变,关→深灰
        if (on) {
            track.setShader(new android.graphics.LinearGradient(
                    0, 0, w, h,
                    Color.parseColor("#7DF9FF"), Color.parseColor("#4D8BFF"),
                    android.graphics.Shader.TileMode.CLAMP));
        } else {
            track.setShader(null);
            track.setColor(Color.parseColor("#313A48"));
        }
        c.drawRoundRect(rect, r, r, track);

        // 圆点
        float kr = r - dp(3);
        float cx = dp(3) + kr + (w - dp(6) - kr * 2) * pos;
        c.drawCircle(cx, h / 2f, kr, knob);
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }
}
