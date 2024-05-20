package com.safelet.android.interactors.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

public class IconGenerator extends com.google.maps.android.ui.IconGenerator {

    public IconGenerator(Context context) {
        super(context);
    }

    public Bitmap makeIcon(Drawable pin, CharSequence text) {
        Bitmap popup = makeIcon(text);
        int width = Math.max(pin.getIntrinsicHeight(), popup.getWidth());
        Bitmap overlay = Bitmap.createBitmap(width, pin.getIntrinsicHeight() + popup.getHeight(), popup.getConfig());
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(popup, new Matrix(), null);
        canvas.save();
        canvas.translate((width - pin.getIntrinsicWidth()) / 2, popup.getHeight());
        pin.setBounds(0, 0, pin.getIntrinsicWidth(), pin.getIntrinsicHeight());
        pin.draw(canvas);
        canvas.restore();
        return overlay;
    }
}
