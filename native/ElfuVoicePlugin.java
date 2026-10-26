package com.elfu.voicebedrock;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.PermissionState;

@CapacitorPlugin(
    name = "ElfuVoice",
    permissions = { @Permission(alias = "notifications", strings = { Manifest.permission.POST_NOTIFICATIONS }) }
)
public class ElfuVoicePlugin extends Plugin {

    @PluginMethod
    public void start(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 33 && getPermissionState("notifications") != PermissionState.GRANTED) {
            requestPermissionForAlias("notifications", call, "afterNotifPerm");
            return;
        }
        doStart(call);
    }

    @PermissionCallback
    private void afterNotifPerm(PluginCall call) {
        // mesmo sem permissão de notificação o serviço funciona; só não aparece o aviso
        doStart(call);
    }

    private void doStart(PluginCall call) {
        try {
            Context ctx = getContext();
            Intent i = new Intent(ctx, VoiceService.class);
            if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i); else ctx.startService(i);
            JSObject r = new JSObject();
            r.put("ok", true);
            r.put("batteryOk", isIgnoringBattery());
            call.resolve(r);
        } catch (Exception e) {
            call.reject("Não foi possível iniciar o serviço: " + e.getMessage());
        }
    }

    @PluginMethod
    public void stop(PluginCall call) {
        try {
            Context ctx = getContext();
            ctx.stopService(new Intent(ctx, VoiceService.class));
        } catch (Exception ignored) { }
        call.resolve();
    }

    private boolean isIgnoringBattery() {
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        return pm == null || Build.VERSION.SDK_INT < 23 || pm.isIgnoringBatteryOptimizations(getContext().getPackageName());
    }

    /** Abre o pedido do Android "permitir que o app rode sem restrição de bateria". */
    @PluginMethod
    public void batteryExemption(PluginCall call) {
        try {
            if (!isIgnoringBattery()) {
                Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + getContext().getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
            }
        } catch (Exception ignored) { }
        call.resolve();
    }
}
