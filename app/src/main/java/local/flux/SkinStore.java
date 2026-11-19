package local.flux;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 皮肤清单 + 开关状态持久化 */
public class SkinStore {
    private static final String PREF = "flux_store";
    private static final String KEY = "skins";

    public static List<Skin> defaults() {
        List<Skin> list = new ArrayList<>();
        list.add(new Skin("neon", "Neon Core", "发光轮廓 · 1.1", true));
        list.add(new Skin("ice", "Ice Veil", "冰霜材质 · 1.1", false));
        list.add(new Skin("void", "Void Glass", "半透明黑玻璃 · 1.1", false));
        list.add(new Skin("sunset", "Sunset Bloom", "渐变暖色 · 1.2 预留", false));
        return list;
    }

    public static List<Skin> load(Context ctx) {
        List<Skin> base = defaults();
        String raw = ctx.getSharedPreferences(PREF, 0).getString(KEY, null);
        if (raw == null) return base;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length() && i < base.size(); i++) {
                base.get(i).enabled = arr.getJSONObject(i).optBoolean("on", base.get(i).enabled);
            }
        } catch (Exception ignored) {
        }
        return base;
    }

    public static void save(Context ctx, List<Skin> list) {
        try {
            JSONArray arr = new JSONArray();
            for (Skin s : list) {
                JSONObject o = new JSONObject();
                o.put("id", s.id);
                o.put("on", s.enabled);
                arr.put(o);
            }
            SharedPreferences.Editor e = ctx.getSharedPreferences(PREF, 0).edit();
            e.putString(KEY, arr.toString());
            e.apply();
        } catch (Exception ignored) {
        }
    }
}
