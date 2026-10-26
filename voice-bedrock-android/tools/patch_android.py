"""Roda no GitHub depois de `cap add android`: coloca permissões, serviço, código nativo e ícone."""
import os, re, shutil, glob

APP = "android/app/src/main"
manifest_path = f"{APP}/AndroidManifest.xml"
m = open(manifest_path, encoding="utf-8").read()

perms = [
    "android.permission.RECORD_AUDIO",
    "android.permission.MODIFY_AUDIO_SETTINGS",
    "android.permission.FOREGROUND_SERVICE",
    "android.permission.FOREGROUND_SERVICE_MICROPHONE",
    "android.permission.POST_NOTIFICATIONS",
    "android.permission.WAKE_LOCK",
    "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
]
add = "".join(f'    <uses-permission android:name="{p}" />\n' for p in perms if p not in m)
m = m.replace("</manifest>", add + "</manifest>")

if "VoiceService" not in m:
    m = m.replace("</application>",
        '    <service android:name=".VoiceService" android:exported="false" android:foregroundServiceType="microphone" />\n    </application>')
open(manifest_path, "w", encoding="utf-8").write(m)

# código nativo
pkg_dir = f"{APP}/java/com/elfu/voicebedrock"
os.makedirs(pkg_dir, exist_ok=True)
for f in ("MainActivity.java", "VoiceService.java", "ElfuVoicePlugin.java"):
    shutil.copy(f"native/{f}", f"{pkg_dir}/{f}")

# ícone: remove o ícone adaptativo padrão e usa os PNGs
shutil.rmtree(f"{APP}/res/mipmap-anydpi-v26", ignore_errors=True)
for d in glob.glob("android-res/mipmap-*"):
    dest = f"{APP}/res/{os.path.basename(d)}"
    os.makedirs(dest, exist_ok=True)
    for f in glob.glob(d + "/*.png"):
        shutil.copy(f, dest)
print("Android patch OK")
