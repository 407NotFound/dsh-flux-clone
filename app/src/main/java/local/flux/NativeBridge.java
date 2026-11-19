package local.flux;

import android.content.res.AssetManager;
import android.view.Surface;

/** 与原生 ImGui 层对接 */
public final class NativeBridge {
    static {
        System.loadLibrary("fluxui");
    }

    public static native void nativeStart(Surface surface, AssetManager am);

    public static native void nativeResize(int w, int h);

    public static native void nativeStep();

    public static native void nativeTouch(int action, float x, float y);

    public static native void nativeSetPing(int ms);

    public static native void nativeStop();
}
