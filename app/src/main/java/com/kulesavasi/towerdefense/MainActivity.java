package com.kulesavasi.towerdefense;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Çentik (notch) desteği
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        applyGameFix();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyGameFix();
        }
    }

    private void applyGameFix() {
        // Tam ekran (Immersive) modu
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        final WebView webView = bridge.getWebView();
        if (webView != null) {
            WebSettings settings = webView.getSettings();
            settings.setUseWideViewPort(true);
            settings.setLoadWithOverviewMode(true);
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);

            webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    injectWorkingFix(webView);
                }
            }, 800);
        }
    }

    private void injectWorkingFix(WebView webView) {
        // Phaser FIT modu + 0.85 zoom (önceki çalışan ayar)
        // + Sadece UI panellerini ekran sınırları içinde tut
        String js = "(function() {" +
                "  var winW = window.innerWidth;" +
                "  var winH = window.innerHeight;" +
                "  " +
                "  /* === Phaser FIT + 0.85 zoom (ÖNCEKİ ÇALIŞAN AYAR) === */" +
                "  var applyPhaserFix = function() {" +
                "    var game = null;" +
                "    if (window.Phaser && window.Phaser.GAMES && window.Phaser.GAMES.length > 0) {" +
                "      game = window.Phaser.GAMES[0];" +
                "    } else if (window.game && window.game.scale) {" +
                "      game = window.game;" +
                "    } else if (window.phaserGame && window.phaserGame.scale) {" +
                "      game = window.phaserGame;" +
                "    }" +
                "    if (game && game.scale) {" +
                "      if (game.scale.setMode) game.scale.setMode(1);" + // FIT
                "      if (game.config && game.config.scale) game.config.scale.mode = 1;" +
                "      game.scale.setMaxZoom(2);" +
                "      game.scale.setMinZoom(0.3);" +
                "      game.scale.resize(winW, winH);" +
                "      game.scale.refresh();" +
                "      if (game.scale.setZoom) game.scale.setZoom(0.85);" + // Önceki çalışan değer
                "      return true;" +
                "    }" +
                "    return false;" +
                "  };" +
                "  " +
                "  /* === SADECE UI PANELLERİNİ EKRANA SIĞDIR === */" +
                "  var fixUIPanels = function() {" +
                "    /* Tüm olası bilgilendirme kutusu/popup/modal elementlerini bul */" +
                "    var selectors = ['.panel', '.popup', '.modal', '.info-panel', '.tower-info', " +
                "                    '.menu', '.dialog', '.tooltip', '.message', '.notification', " +
                "                    '[class*=\"panel\"]', '[class*=\"popup\"]', '[class*=\"modal\"]', " +
                "                    '[class*=\"info\"]', '[class*=\"menu\"]', '[class*=\"dialog\"]'];" +
                "    " +
                "    var allEls = document.querySelectorAll(selectors.join(','));" +
                "    for (var i = 0; i < allEls.length; i++) {" +
                "      var el = allEls[i];" +
                "      var rect = el.getBoundingClientRect();" +
                "      " +
                "      /* Sadece görünür ve ekran dışına taşan panelleri düzelt */" +
                "      if (rect.width === 0 || rect.height === 0) continue;" +
                "      if (computedStyle(el).display === 'none' || computedStyle(el).visibility === 'hidden') continue;" +
                "      " +
                "      var overflowsRight = rect.right > winW;" +
                "      var overflowsBottom = rect.bottom > winH;" +
                "      var overflowsLeft = rect.left < 0;" +
                "      var overflowsTop = rect.top < 0;" +
                "      " +
                "      if (overflowsRight || overflowsBottom || overflowsLeft || overflowsTop) {" +
                "        var style = el.style;" +
                "        style.maxWidth = Math.min(rect.width, winW * 0.9) + 'px';" +
                "        style.maxHeight = Math.min(rect.height, winH * 0.9) + 'px';" +
                "        style.boxSizing = 'border-box';" +
                "        style.overflow = 'hidden auto';" +
                "        " +
                "        /* Taşma yönüne göre konum düzelt */" +
                "        if (overflowsRight) {" +
                "          style.right = '10px';" +
                "          style.left = 'auto';" +
                "        }" +
                "        if (overflowsBottom) {" +
                "          style.bottom = '10px';" +
                "          style.top = 'auto';" +
                "        }" +
                "        if (overflowsLeft) style.left = '10px';" +
                "        if (overflowsTop) style.top = '10px';" +
                "      }" +
                "    }" +
                "  };" +
                "  " +
                "  /* Yardımcı: computed style al */" +
                "  function computedStyle(el) {" +
                "    return window.getComputedStyle(el);" +
                "  }" +
                "  " +
                "  var runFix = function() {" +
                "    applyPhaserFix();" +
                "    fixUIPanels();" +
                "  };" +
                "  " +
                "  runFix();" +
                "  setInterval(runFix, 500);" +
                "  window.addEventListener('resize', runFix);" +
                "  setTimeout(runFix, 2000);" +
                "  setTimeout(runFix, 5000);" +
                "})();";

        webView.evaluateJavascript(js, null);
    }
}