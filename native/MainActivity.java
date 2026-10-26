package com.elfu.voicebedrock;

import android.os.Bundle;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ElfuVoicePlugin.class);
        super.onCreate(savedInstanceState);
    }

    // Mantém o JavaScript (áudio + conexão) rodando quando o app vai para segundo plano.
    @Override
    public void onPause() {
        super.onPause();
        keepWebViewAlive();
    }

    @Override
    public void onStop() {
        super.onStop();
        keepWebViewAlive();
    }

    private void keepWebViewAlive() {
        try {
            WebView w = getBridge().getWebView();
            if (w != null) {
                w.onResume();
                w.resumeTimers();
            }
        } catch (Exception ignored) { }
    }
}
