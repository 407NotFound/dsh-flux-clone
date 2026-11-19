// FluxClone native UI: Dear ImGui 1.92.4 + OpenGL ES 3 on ANativeWindow
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <EGL/egl.h>
#include <GLES3/gl3.h>
#include <jni.h>
#include <string>
#include <cstdio>
#include <ctime>

#include "imgui.h"
#include "imgui_impl_opengl3.h"

namespace {

ANativeWindow *g_window = nullptr;
EGLDisplay g_dpy = EGL_NO_DISPLAY;
EGLSurface g_surf = EGL_NO_SURFACE;
EGLContext g_ctx = EGL_NO_CONTEXT;
int g_w = 0, g_h = 0;
bool g_ready = false;
ImGuiContext *g_imgui = nullptr;
AAssetManager *g_assets = nullptr;
bool g_fontLoaded = false;
int g_tab = 0;
char g_key[40] = "";
const char *g_status = "Без ключа";
bool g_menu_open = false;
int g_ping = -1;          // ms, -1 = 未测
double g_hud_x = -1, g_hud_y = -1;
bool g_dragging = false;
float g_drag_dx = 0, g_drag_dy = 0;

bool init_egl() {
    if (!g_window) return false;
    g_dpy = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (g_dpy == EGL_NO_DISPLAY) return false;
    EGLint major, minor;
    if (!eglInitialize(g_dpy, &major, &minor)) return false;

    const EGLint cfgAttr[] = {
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
        EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8, EGL_DEPTH_SIZE, 0, EGL_STENCIL_SIZE, 8,
        EGL_NONE
    };
    EGLConfig cfg;
    EGLint n = 0;
    if (!eglChooseConfig(g_dpy, cfgAttr, &cfg, 1, &n) || n < 1) return false;

    const EGLint ctxAttr[] = {EGL_CONTEXT_CLIENT_VERSION, 3, EGL_NONE};
    g_ctx = eglCreateContext(g_dpy, cfg, EGL_NO_CONTEXT, ctxAttr);
    if (g_ctx == EGL_NO_CONTEXT) return false;
    g_surf = eglCreateWindowSurface(g_dpy, cfg, g_window, nullptr);
    if (g_surf == EGL_NO_SURFACE) return false;
    if (!eglMakeCurrent(g_dpy, g_surf, g_surf, g_ctx)) return false;
    return true;
}

void load_font() {
    if (g_fontLoaded || !g_assets) return;
    AAsset *a = AAssetManager_open(g_assets, "DejaVuSans.ttf", AASSET_MODE_BUFFER);
    if (!a) return;
    off_t len = AAsset_getLength(a);
    void *buf = malloc((size_t) len);
    AAsset_read(a, buf, (size_t) len);
    AAsset_close(a);
    ImFontConfig fc;
    fc.FontDataOwnedByAtlas = true;
    ImGui::GetIO().Fonts->AddFontFromMemoryTTF(buf, (int) len, 26.0f, &fc,
                                               ImGui::GetIO().Fonts->GetGlyphRangesCyrillic());
    g_fontLoaded = true;
}

void setup_style() {
    ImGuiStyle &s = ImGui::GetStyle();
    s.WindowRounding = 16.0f;
    s.WindowPadding = ImVec2(22, 20);
    s.FrameRounding = 8.0f;
    s.ItemSpacing = ImVec2(8, 10);
    s.Colors[ImGuiCol_WindowBg] = ImVec4(0.055f, 0.075f, 0.102f, 0.94f);
    s.Colors[ImGuiCol_Border] = ImVec4(0.16f, 0.49f, 0.98f, 0.35f);
    s.Colors[ImGuiCol_Text] = ImVec4(0.90f, 0.93f, 0.96f, 1.0f);
    s.Colors[ImGuiCol_FrameBg] = ImVec4(0.129f, 0.157f, 0.20f, 1.0f);
    s.Colors[ImGuiCol_Button] = ImVec4(0.55f, 0.09f, 0.13f, 1.0f);
    s.Colors[ImGuiCol_ButtonHovered] = ImVec4(0.68f, 0.12f, 0.16f, 1.0f);
    s.Colors[ImGuiCol_ButtonActive] = ImVec4(0.45f, 0.07f, 0.10f, 1.0f);
}

void draw_hud() {
    ImGuiIO &io = ImGui::GetIO();
    static ImVec2 pos(0, 0);
    if (g_hud_x < 0) {
        g_hud_x = io.DisplaySize.x * 0.5 - 150;
        g_hud_y = 86;
    }

    ImGui::SetNextWindowPos(ImVec2((float) g_hud_x, (float) g_hud_y), ImGuiCond_Always);
    ImGui::PushStyleVar(ImGuiStyleVar_WindowRounding, 18.0f);
    ImGui::PushStyleVar(ImGuiStyleVar_WindowPadding, ImVec2(14, 9));
    ImGui::PushStyleColor(ImGuiCol_WindowBg, ImVec4(0.09f, 0.10f, 0.12f, 0.96f));
    ImGui::PushStyleColor(ImGuiCol_Border, ImVec4(0.35f, 0.37f, 0.42f, 0.55f));
    ImGui::Begin("##hud", nullptr,
                 ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoResize |
                 ImGuiWindowFlags_NoScrollbar | ImGuiWindowFlags_AlwaysAutoResize |
                 ImGuiWindowFlags_NoSavedSettings | ImGuiWindowFlags_NoMove |
                 ImGuiWindowFlags_NoFocusOnAppearing);

    ImGui::TextColored(ImVec4(0.95f, 0.95f, 0.97f, 1.0f), "flux");
    ImGui::SameLine();
    ImGui::TextColored(ImVec4(0.32f, 0.34f, 0.38f, 1.0f), "|");
    ImGui::SameLine();
    ImGui::TextColored(ImVec4(0.90f, 0.28f, 0.32f, 1.0f), "o");
    ImGui::SameLine();
    ImGui::TextColored(ImVec4(0.85f, 0.87f, 0.90f, 1.0f), "Guest");
    ImGui::SameLine();
    ImGui::TextColored(ImVec4(0.32f, 0.34f, 0.38f, 1.0f), "|");
    ImGui::SameLine();
    if (g_ping < 0) {
        ImGui::TextColored(ImVec4(0.55f, 0.58f, 0.64f, 1.0f), "-- ms");
    } else {
        ImVec4 c = g_ping < 80 ? ImVec4(0.30f, 0.85f, 0.45f, 1.0f)
                 : g_ping < 160 ? ImVec4(0.95f, 0.80f, 0.25f, 1.0f)
                                : ImVec4(0.92f, 0.32f, 0.32f, 1.0f);
        ImGui::TextColored(c, "%d ms", g_ping);
    }
    ImGui::SameLine();
    ImGui::TextColored(ImVec4(0.32f, 0.34f, 0.38f, 1.0f), "|");
    ImGui::SameLine();
    time_t now = time(nullptr);
    struct tm tm_now;
    localtime_r(&now, &tm_now);
    ImGui::TextColored(ImVec4(0.85f, 0.87f, 0.90f, 1.0f), "%02d:%02d:%02d",
                       tm_now.tm_hour, tm_now.tm_min, tm_now.tm_sec);

    // 拖动 + 点击
    ImGui::SetCursorPos(ImVec2(0, 0));
    ImGui::InvisibleButton("##drag", ImGui::GetWindowSize());
    if (ImGui::IsItemActive() && ImGui::IsMouseDragging(0)) {
        ImVec2 d = ImGui::GetIO().MouseDelta;
        g_hud_x += d.x;
        g_hud_y += d.y;
    }
    if (ImGui::IsItemDeactivated() && ImGui::IsMouseReleased(0)) {
        g_menu_open = !g_menu_open;
    }

    ImGui::End();
    ImGui::PopStyleColor(2);
    ImGui::PopStyleVar(2);
}

void draw_ui() {
    draw_hud();
    if (!g_menu_open) return;
    ImGuiIO &io = ImGui::GetIO();
    ImGui::SetNextWindowPos(ImVec2(io.DisplaySize.x * 0.5f, io.DisplaySize.y * 0.5f),
                            ImGuiCond_Always, ImVec2(0.5f, 0.5f));
    ImGui::SetNextWindowSize(ImVec2(330, 0), ImGuiCond_Always);

    ImGui::Begin("flux", nullptr,
                 ImGuiWindowFlags_NoTitleBar | ImGuiWindowFlags_NoResize |
                 ImGuiWindowFlags_NoMove | ImGuiWindowFlags_NoScrollbar |
                 ImGuiWindowFlags_AlwaysAutoResize);

    ImGui::PushFont(io.Fonts->Fonts[0]);

    float w = ImGui::GetContentRegionAvail().x;
    auto center = [&](const char *t) {
        float tw = ImGui::CalcTextSize(t).x;
        ImGui::SetCursorPosX(ImGui::GetCursorPosX() + (w - tw) * 0.5f);
        ImGui::TextUnformatted(t);
    };

    center("flux");
    ImGui::Dummy(ImVec2(0, 6));
    center("Flux Changer");
    ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.55f, 0.58f, 0.65f, 1.0f));
    center("Ваши скины. Без root.");
    ImGui::PopStyleColor();
    ImGui::Dummy(ImVec2(0, 14));

    ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.60f, 0.64f, 0.71f, 1.0f));
    ImGui::TextUnformatted("Ключ доступа");
    ImGui::PopStyleColor();
    ImGui::SetNextItemWidth(-1);
    ImGui::InputTextWithHint("##key", "FLX-NR-XXXXXX-XXXXXX-XXXXXX", g_key, sizeof(g_key));

    ImGui::Dummy(ImVec2(0, 4));
    if (ImGui::Button("Войти", ImVec2(-1, 40))) {
        g_status = g_key[0] ? "Проверка..." : "Введите ключ";
        g_tab = 1;
    }

    ImGui::PushStyleColor(ImGuiCol_Text, ImVec4(0.45f, 0.48f, 0.55f, 1.0f));
    center(g_status);
    ImGui::PopStyleColor();
    ImGui::Dummy(ImVec2(0, 6));

    ImGui::PushStyleColor(ImGuiCol_Button, ImVec4(0.16f, 0.18f, 0.22f, 1.0f));
    if (ImGui::Button("Получить ключ · 12 часов", ImVec2(-1, 36))) {
        g_status = "Telegram: @flux";
    }
    ImGui::Dummy(ImVec2(0, 6));
    if (ImGui::Button("Выход", ImVec2(-1, 36))) {
        g_status = "Выход";
    }
    ImGui::PopStyleColor();

    ImGui::PopFont();
    ImGui::End();
}

}  // namespace

extern "C" JNIEXPORT void JNICALL
Java_local_flux_NativeBridge_nativeStart(JNIEnv *env, jclass, jobject surface, jobject am) {
    if (g_window) ANativeWindow_release(g_window);
    g_window = ANativeWindow_fromSurface(env, surface);
    if (am) g_assets = AAssetManager_fromJava(env, am);
    if (!init_egl()) return;

    IMGUI_CHECKVERSION();
    if (!g_imgui) {
        g_imgui = ImGui::CreateContext();
        ImGui::SetCurrentContext(g_imgui);
        ImGuiIO &io = ImGui::GetIO();
        io.IniFilename = nullptr;
        setup_style();
        ImGui_ImplOpenGL3_Init("#version 300 es");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_local_flux_NativeBridge_nativeResize(JNIEnv *, jclass, jint w, jint h) {
    g_w = w;
    g_h = h;
    ImGui::SetCurrentContext(g_imgui);
    ImGui::GetIO().DisplaySize = ImVec2((float) w, (float) h);
    if (g_surf != EGL_NO_SURFACE) eglMakeCurrent(g_dpy, g_surf, g_surf, g_ctx);
    if (g_w > 0 && g_h > 0) glViewport(0, 0, g_w, g_h);
}

extern "C" JNIEXPORT void JNICALL
Java_local_flux_NativeBridge_nativeStep(JNIEnv *, jclass) {
    if (!g_imgui || !g_surf) return;
    ImGui::SetCurrentContext(g_imgui);
    load_font();
    if (g_w <= 0 || g_h <= 0) return;

    ImGui_ImplOpenGL3_NewFrame();
    ImGui::NewFrame();
    draw_ui();
    ImGui::Render();

    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
    eglSwapBuffers(g_dpy, g_surf);
}

extern "C" JNIEXPORT void JNICALL
Java_local_flux_NativeBridge_nativeTouch(JNIEnv *, jclass, jint action, jfloat x, jfloat y) {
    if (!g_imgui) return;
    ImGui::SetCurrentContext(g_imgui);
    ImGuiIO &io = ImGui::GetIO();
    const bool down = (action == 0);
    const bool move = (action == 2);
    if (down || move) io.AddMousePosEvent(x, y);
    io.AddMouseButtonEvent(0, down || move);
    if (action == 1) io.AddMouseButtonEvent(0, false);
}

extern "C" JNIEXPORT void JNICALL
Java_local_flux_NativeBridge_nativeSetPing(JNIEnv *, jclass, jint ms) {
    g_ping = ms;
}

extern "C" JNIEXPORT void JNICALL
Java_local_flux_NativeBridge_nativeStop(JNIEnv *, jclass) {
    if (g_imgui) {
        ImGui_ImplOpenGL3_Shutdown();
        ImGui::DestroyContext(g_imgui);
        g_imgui = nullptr;
    }
    if (g_dpy != EGL_NO_DISPLAY) {
        eglMakeCurrent(g_dpy, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (g_ctx != EGL_NO_CONTEXT) eglDestroyContext(g_dpy, g_ctx);
        if (g_surf != EGL_NO_SURFACE) eglDestroySurface(g_dpy, g_surf);
        eglTerminate(g_dpy);
        g_dpy = EGL_NO_DISPLAY;
        g_surf = EGL_NO_SURFACE;
        g_ctx = EGL_NO_CONTEXT;
    }
    if (g_window) {
        ANativeWindow_release(g_window);
        g_window = nullptr;
    }
}
