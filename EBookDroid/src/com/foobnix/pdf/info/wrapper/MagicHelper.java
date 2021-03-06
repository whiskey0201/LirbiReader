package com.foobnix.pdf.info.wrapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.ebookdroid.LirbiApp;

import com.foobnix.android.utils.LOG;
import com.foobnix.android.utils.TxtUtils;
import com.foobnix.pdf.info.IMG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.ColorUtils;
import android.widget.TextView;

public class MagicHelper {

    public static boolean isNeedMagic = true;

    public static int hash() {
        StringBuilder builder = new StringBuilder();

        builder.append(AppState.get().isUseBGImageDay);
        builder.append(AppState.get().isUseBGImageNight);
        builder.append(AppState.get().bgImageDayTransparency);
        builder.append(AppState.get().bgImageNightTransparency);
        builder.append(AppState.get().bgImageDayPath);
        builder.append(AppState.get().bgImageNightPath);
        return builder.toString().hashCode();

    }

    public static int darkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] -= 0.1f;
        return Color.HSVToColor(hsv);
    }

    // - darker, + ligther
    public static int otherColor(int color, float value) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] -= value;
        return Color.HSVToColor(hsv);
    }


    public static ByteArrayInputStream scaleCenterCrop(byte[] source, int w, int h) {
        Bitmap decodeStream = BitmapFactory.decodeStream(new ByteArrayInputStream(source));

        Bitmap scaleCenterCrop = scaleCenterCrop(decodeStream, h, w, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaleCenterCrop.compress(CompressFormat.PNG, 95, out);

        scaleCenterCrop.recycle();

        return new ByteArrayInputStream(out.toByteArray());

    }

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth, boolean withEffect) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        LOG.d("scaleCenterCrop", sourceWidth, sourceHeight, newHeight, newWidth);

        LOG.d("RATIO", (float) sourceHeight / sourceWidth);

        // Compute the scaling factors to fit the new height and width,
        // respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;

        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source
        // bitmap
        // will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw
        // our
        // new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, Config.ARGB_4444);
        Canvas canvas = new Canvas(dest);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);

        canvas.drawBitmap(source, null, targetRect, paint);

        if (withEffect) {
            applyBookEffect(dest);
        }

        source.recycle();
        source = null;

        return dest;
    }

    public static void applyBookEffect(Bitmap dest) {
        if (AppState.getInstance().isBookCoverEffect) {
            Canvas canvas = new Canvas(dest);
            IMG.bookBGNoMark.setBounds(0, 0, dest.getWidth(), dest.getHeight());
            IMG.bookBGNoMark.draw(canvas);
        }
    }

    public static void applyBookEffectWithLogo(Bitmap dest) {
        Canvas canvas = new Canvas(dest);
        IMG.bookBGWithMark.setBounds(0, 0, dest.getWidth(), dest.getHeight());
        IMG.bookBGWithMark.draw(canvas);
    }

    public static boolean isNeedMagic() {
        if (!isNeedMagic) {
            return false;
        }

        boolean isDay = AppState.get().isInvert && //
                (AppState.get().colorDayBg != AppState.COLOR_WHITE || //
                        AppState.get().colorDayText != AppState.COLOR_BLACK); //

        boolean isNigth = !AppState.get().isInvert && //
                (AppState.get().colorNigthBg != AppState.COLOR_BLACK || //
                        AppState.get().colorNigthText != AppState.COLOR_WHITE); //

        return isDay || isNigth;
    }

    public static boolean isNeedBookBackgroundImage() {
        return (!AppState.get().isInvert && AppState.get().isUseBGImageNight) || (AppState.get().isInvert && AppState.get().isUseBGImageDay);
    }

    public static String getImagePath() {
        return !AppState.get().isInvert ? AppState.get().bgImageNightPath : AppState.get().bgImageDayPath;
    }

    public static String getImagePath(boolean isDay) {
        return !isDay ? AppState.get().bgImageNightPath : AppState.get().bgImageDayPath;
    }

    public static int getTransparencyInt() {
        return AppState.get().isInvert ? AppState.get().bgImageDayTransparency : AppState.get().bgImageNightTransparency;
    }

    public static final String IMAGE_BG_1 = "bg/bg1.jpg";
    public static final String IMAGE_BG_2 = "bg/bg2.jpg";
    public static final String IMAGE_BG_3 = "bg/bg3.jpg";

    public static Bitmap updateTextViewBG(TextView textView, int transparency, String path) {
        textView.setDrawingCacheEnabled(true);
        textView.buildDrawingCache(true);

        Bitmap drawingCache = textView.getDrawingCache();
        if (drawingCache == null) {
            return null;
        }

        Bitmap updates = null;
        try {
            updates = updateWithBackground(drawingCache, transparency, loadBitmap(path));
        } catch (Exception e) {
            LOG.e(e);
        }

        textView.setDrawingCacheEnabled(false);
        return updates;

    }

    public static Drawable getBgImageDrawable(String name) {
        final BitmapDrawable background = new BitmapDrawable(loadBitmap(name));
        background.setAlpha(AppState.get().bgImageDayTransparency);
        return background;
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        try {
            return BitmapFactory.decodeStream(context.getAssets().open(filePath));
        } catch (IOException e) {
            LOG.e(e);
        }
        return null;
    }

    public static Drawable getBgImageDayDrawable(boolean withAlpa) {
        return new BitmapDrawable(updateWithBackground(loadBitmap(AppState.get().bgImageDayPath), withAlpa ? AppState.get().bgImageDayTransparency : AppState.DAY_TRANSPARENCY, Color.WHITE));
    }

    public static Drawable getBgImageNightDrawable(boolean withAlpa) {
        return new BitmapDrawable(updateWithBackground(loadBitmap(AppState.get().bgImageNightPath), withAlpa ? AppState.get().bgImageNightTransparency : AppState.NIGHT_TRANSPARENCY, Color.BLACK));
    }

    static Bitmap bg1;
    static String bgPath = getImagePath();
    static int mainColor = Color.TRANSPARENT;

    public static Bitmap getBackgroundImage() {
        if (bg1 != null && getImagePath().equals(bgPath)) {
            return bg1;
        }
        bgPath = getImagePath();
        bg1 = loadBitmap(getImagePath());
        mainColor = getDominantColor(bg1);
        return bg1;
    }

    public static Bitmap loadBitmap(String name) {
        if (TxtUtils.isEmpty(name)) {
            return null;
        }
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Config.RGB_565;

        if (name.startsWith("/") && !new File(name).exists()) {
            return loadBitmap(MagicHelper.IMAGE_BG_1);
        }

        if (name.startsWith("/")) {
            return BitmapFactory.decodeFile(name, opt);
        }
        try {
            InputStream oldBook = LirbiApp.context.getAssets().open(name);
            Bitmap decodeStream = BitmapFactory.decodeStream(oldBook);
            Bitmap res = decodeStream.copy(Config.RGB_565, false);
            decodeStream.recycle();
            return res;
        } catch (Exception e) {
            LOG.e(e);
            return null;
        }
    }

    public static Bitmap updateWithBackground(Bitmap bitmap) {
        return updateWithBackground(bitmap, getTransparencyInt(), getBackgroundImage());
    }

    public static Bitmap updateWithBackground(Bitmap bitmap, int alpha, Bitmap bgBitmap) {
        Paint p = new Paint();
        p.setAlpha(alpha);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(result);

        // for PDF only
        Matrix m = new Matrix();
        float sx = (float) bitmap.getWidth() / bgBitmap.getWidth();
        float sy = (float) bitmap.getHeight() / bgBitmap.getHeight();
        m.setScale(sx, sy);
        canvas.drawBitmap(bgBitmap, m, new Paint());
        canvas.drawBitmap(bitmap, 0, 0, p);

        bitmap.recycle();
        bitmap = null;
        return result;
    }

    public static Bitmap updateWithBackground(Bitmap bitmap, int alpha, int color) {
        Paint p = new Paint();
        p.setAlpha(alpha);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(result);

        canvas.drawColor(color);
        canvas.drawBitmap(bitmap, 0, 0, p);
        return result;
    }

    public static int getTextColor() {
        return AppState.get().isInvert ? AppState.get().colorDayText : AppState.get().colorNigthText;
    }

    public static int getBgColor() {
        if (AppState.get().isInvert && AppState.get().isUseBGImageDay) {
            // return Color.parseColor("#EFEBDE");
        }
        if (!AppState.get().isInvert && AppState.get().isUseBGImageNight) {
            // return Color.parseColor("#52493A");
        }

        return AppState.get().isInvert ? AppState.get().colorDayBg : AppState.get().colorNigthBg;
    }

    public static float[] getHSV(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv;
    }

    private static float lightness(int color) {
        int R = Color.red(color);
        int G = Color.green(color);
        int B = Color.blue(color);

        return (float) (0.2126 * R + 0.7152 * G + 0.0722 * B);
    }

    static int colorCacheInput = Color.WHITE;
    static int colorCache = Color.WHITE;

    public static int ligtherColor(int color) {
        if (color == colorCacheInput) {
            return colorCache;
        }

        int r1 = Color.red(color);
        int g1 = Color.green(color);
        int b1 = Color.blue(color);
        int k = 10;
        int res = Color.rgb(r1 > 128 ? r1 - k : r1 + k, g1 > 128 ? g1 - k : g1 + k, b1 > 128 ? b1 - k : b1 + k);
        colorCacheInput = color;
        colorCache = res;
        return res;

    }

    public static final int myColorIng = Color.BLUE;

    public static final int addR = Color.red(myColorIng);
    public static final int addG = Color.green(myColorIng);
    public static final int addB = Color.blue(myColorIng);

    public static float[] myColorHSL = new float[3];
    static {
        ColorUtils.colorToHSL(myColorIng, myColorHSL);
    }

    public void udpateColorsMagic(Bitmap bitmap) {
        if (!isNeedMagic()) {
            return;
        }
        int pixels[] = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        MagicHelper.udpateColorsMagic(pixels);
        bitmap.setPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public static int[] updatePixelsFromTo(int from, int to, int[] allpixels) {
        for (int i = 0; i < allpixels.length; i++) {
            if (allpixels[i] == from) {
                allpixels[i] = to;
                continue;
            }
        }
        return allpixels;
    }

    public static boolean isLight(int color) {
        return color >= 200 && color <= 255;
    }

    public static boolean isDark(int color) {
        return color >= 0 && color <= 55;
    }

    @Deprecated
    // experimanttal
    public static void udpateColorsMagic1(int[] allpixels) {
        if (!isNeedMagic()) {
            return;
        }

        int textColor = MagicHelper.getTextColor();
        int bgColor = MagicHelper.getBgColor();

        boolean isNeedFont = AppState.get().isCustomizeBgAndColors;

        for (int i = 0; i < allpixels.length; i++) {

            if (isNeedFont && allpixels[i] == Color.BLACK) {
                allpixels[i] = textColor;
                continue;
            }

            if (allpixels[i] == Color.WHITE) {
                allpixels[i] = bgColor;
                continue;
            }

            int color = allpixels[i];
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            if (isLight(r) && isLight(g) && isLight(b)) {
                allpixels[i] = mixColorsFontColor(color, bgColor);
                continue;
            }
            if (isNeedFont && isDark(r) && isDark(g) && isDark(b)) {
                allpixels[i] = mixColorsFontColor(~color, textColor);
                continue;
            }

        }

    }

    public static void udpateColorsMagic(int[] allpixels) {
        if (!isNeedMagic()) {
            return;
        }
        LOG.d("MAGIC ON");

        int textColor = MagicHelper.getTextColor();
        int bgColor = MagicHelper.getBgColor();

        for (int i = 0; i < allpixels.length; i++) {
            int color = allpixels[i];

            if (color == Color.BLACK) {
                allpixels[i] = textColor;
                continue;
            }
            //
            if (allpixels[i] == Color.WHITE) {
                allpixels[i] = bgColor;
                continue;
            }

            int k = Color.red(color) + Color.green(color) + Color.blue(color);
            if (k > 350) {
                // ligth font color
                allpixels[i] = mixColorsFontColor(color, bgColor);
            } else {
                // dark font color
                allpixels[i] = mixColorsFontColor(~color, textColor);
            }
        }
    }

    public static int mixColorsBg(int col1, int col2) {
        int r1, g1, b1, r2, g2, b2;

        r1 = Color.red(col1);
        g1 = Color.green(col1);
        b1 = Color.blue(col1);

        r2 = Color.red(col2);
        g2 = Color.green(col2);
        b2 = Color.blue(col2);

        int r3 = overlay(r1, r2);
        int g3 = overlay(g1, g2);
        int b3 = overlay(b1, b2);

        return Color.rgb(r3, g3, b3);
    }

    public static int mixColorsFontColor(int col1, int col2) {
        int r1, g1, b1, r2, g2, b2;

        r1 = Color.red(col1);
        g1 = Color.green(col1);
        b1 = Color.blue(col1);

        r2 = Color.red(col2);
        g2 = Color.green(col2);
        b2 = Color.blue(col2);

        int r3 = multiply(r1, r2);
        int g3 = multiply(g1, g2);
        int b3 = multiply(b1, b2);

        return Color.rgb(r3, g3, b3);
    }

    public static int multiply(int r1, int r2) {
        return r1 * r2 / 255;
    }

    public static int screen(int c1, int c2) {
        return 255 - (((255 - c1) * (255 - c2)) / 255);
    }

    public static int overlay(int c1, int c2) {
        return (c1 < 128) ? (2 * c2 * c1 / 255) : (255 - 2 * (255 - c2) * (255 - c1) / 255);
    }

    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    public static int mixTwoColors(int color1, int color2, float amount) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL = 16;
        final byte GREEN_CHANNEL = 8;
        final byte BLUE_CHANNEL = 0;

        final float inverseAmount = 1.0f - amount;

        int a = ((int) (((color1 >> ALPHA_CHANNEL & 0xff) * amount) + ((color2 >> ALPHA_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int r = ((int) (((color1 >> RED_CHANNEL & 0xff) * amount) + ((color2 >> RED_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int g = ((int) (((color1 >> GREEN_CHANNEL & 0xff) * amount) + ((color2 >> GREEN_CHANNEL & 0xff) * inverseAmount))) & 0xff;
        int b = ((int) (((color1 & 0xff) * amount) + ((color2 & 0xff) * inverseAmount))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    public static String colorToString(int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    public static int getDominantColor(Bitmap bitmap) {
        if (null == bitmap)
            return Color.TRANSPARENT;

        int redBucket = 0;
        int greenBucket = 0;
        int blueBucket = 0;

        int pixelCount = bitmap.getWidth() * bitmap.getHeight();
        int[] pixels = new int[pixelCount];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int y = 0, h = bitmap.getHeight(); y < h; y++) {
            for (int x = 0, w = bitmap.getWidth(); x < w; x++) {
                int color = pixels[x + y * w]; // x + y * width
                redBucket += (color >> 16) & 0xFF; // Color.red
                greenBucket += (color >> 8) & 0xFF; // Color.greed
                blueBucket += (color & 0xFF); // Color.blue
            }
        }

        return Color.rgb(redBucket / pixelCount, greenBucket / pixelCount, blueBucket / pixelCount);
    }

    public static Bitmap trimBitmap(Bitmap bmp) {
        int bgColor = getBgColor();
        int imgHeight = bmp.getHeight();
        int imgWidth = bmp.getWidth();

        // TRIM WIDTH - LEFT
        int startWidth = 0;
        for (int x = 0; x < imgWidth; x++) {
            if (startWidth == 0) {
                for (int y = 0; y < imgHeight; y++) {
                    if (bmp.getPixel(x, y) != bgColor) {
                        startWidth = x;
                        break;
                    }
                }
            } else
                break;
        }

        // TRIM WIDTH - RIGHT
        int endWidth = 0;
        for (int x = imgWidth - 1; x >= 0; x--) {
            if (endWidth == 0) {
                for (int y = 0; y < imgHeight; y++) {
                    if (bmp.getPixel(x, y) != bgColor) {
                        endWidth = x;
                        break;
                    }
                }
            } else
                break;
        }

        // TRIM HEIGHT - TOP
        int startHeight = 0;
        for (int y = 0; y < imgHeight; y++) {
            if (startHeight == 0) {
                for (int x = 0; x < imgWidth; x++) {
                    if (bmp.getPixel(x, y) != bgColor) {
                        startHeight = y;
                        break;
                    }
                }
            } else
                break;
        }

        // TRIM HEIGHT - BOTTOM
        int endHeight = 0;
        for (int y = imgHeight - 1; y >= 0; y--) {
            if (endHeight == 0) {
                for (int x = 0; x < imgWidth; x++) {
                    if (bmp.getPixel(x, y) != bgColor) {
                        endHeight = y;
                        break;
                    }
                }
            } else
                break;
        }

        return Bitmap.createBitmap(bmp, startWidth, startHeight, endWidth - startWidth, endHeight - startHeight);

    }

    public static boolean isColorDarkSimple(int color) {
        int k = Color.red(color) + Color.green(color) + Color.blue(color);
        if (k > 550) {// 350
            return false;
        } else {
            return true;
        }
    }

    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness < 0.5) {
            return false; // It's a light color
        } else {
            return true; // It's a dark color
        }
    }

}
