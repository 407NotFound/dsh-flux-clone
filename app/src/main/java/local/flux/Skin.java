package local.flux;

/** 一个皮肤条目:名称 / 资源包名 / 是否启用 */
public class Skin {
    public final String id;
    public final String name;
    public final String subtitle;
    public boolean enabled;

    public Skin(String id, String name, String subtitle, boolean enabled) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.enabled = enabled;
    }
}
