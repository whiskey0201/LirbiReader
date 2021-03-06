package com.foobnix.pdf.info;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;

public class Urls {

    public static void open(Context a, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        a.startActivity(browserIntent);
    }

    public static void openPdfPro(Context a) {
        try {
            open(a, "market://details?id=com.foobnix.pro.pdf.reader");
        } catch (Exception e) {
            // android 1.6
            open(a, "https://play.google.com/store/apps/details?id=com.foobnix.pro.pdf.reader");
        }
    }

    public static void openTTS(Context a) {
        try {
            open(a, "market://details?id=com.google.android.tts");
        } catch (Exception e) {
            // android 1.6
            open(a, "https://play.google.com/store/apps/details?id=com.google.android.tts");
        }
    }

    public static String getLangCode() {
        try {
            return Locale.getDefault().getLanguage();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isRtl() {
        try {
            return TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL;
        } catch (Exception e) {
            return false;
        }
    }

}
