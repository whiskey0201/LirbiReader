package com.foobnix.pdf.info.view;

import com.foobnix.android.utils.Dips;
import com.foobnix.android.utils.LOG;
import com.foobnix.pdf.info.R;
import com.foobnix.pdf.info.TintUtil;
import com.foobnix.pdf.info.wrapper.AppState;
import com.foobnix.pdf.info.wrapper.DocumentController;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class BrigtnessDraw extends View {

    public BrigtnessDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        textView = new TextView(context);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textView.setTextSize(16);

        Drawable icon = ContextCompat.getDrawable(context, R.drawable.glyphicons_190_brightness_increase);
        TintUtil.setDrawableTint(icon.getCurrent(), Color.WHITE);

        textView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        textView.setCompoundDrawablePadding(Dips.dpToPx(10));

        textView.setBackgroundResource(android.R.drawable.toast_frame);

        toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(textView);
        toast.setDuration(1000);
    }

    float y, x;
    int distance = Dips.dpToPx(5);

    private Activity activity;
    private TextView textView;
    Toast toast;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (activity == null || !AppState.get().isBrighrnessEnable) {
            return false;
        }

        LOG.d("BrigtnessDraw", event);

        final int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_DOWN) {
            y = event.getY();
            x = event.getX();
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (Math.abs(y - event.getY()) >= distance) {
                LOG.d("BrigtnessDraw", event.getX());
                float dy = (y - event.getY());
                double fast = 0.03;
                double slow = 0.005;
                if (dy > 0) {

                    if (AppState.getInstance().brightness > 0.1) {
                        AppState.getInstance().brightness += fast;
                    } else {
                        AppState.getInstance().brightness += slow;
                    }
                } else {

                    if (AppState.getInstance().brightness < 0.1) {
                        AppState.getInstance().brightness -= slow;
                    } else {
                        AppState.getInstance().brightness -= fast;
                    }
                }

                if (AppState.getInstance().brightness <= -0.005) {
                    AppState.getInstance().brightness = -0.005f;
                }

                if (AppState.getInstance().brightness >= 1) {
                    AppState.getInstance().brightness = 1f;
                }
                DocumentController.applyBrigtness(activity);
                y = event.getY();
                showToast();
            }
        }
        return true;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.onClickListener = l;
    }

    OnClickListener onClickListener;

    public void showToast() {
        if (AppState.getInstance().brightness < 0) {
            textView.setText(R.string.automatic);
        } else {
            textView.setText("" + (int) (AppState.getInstance().brightness * 100));
        }
        toast.show();
    }

    public void showToast(String text) {
        textView.setText(text);
        toast.show();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

}
