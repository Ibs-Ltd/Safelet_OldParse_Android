package com.safelet.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.safelet.android.R;
import com.safelet.android.global.Utils;

public final class NotificationPopUpNetworkErrorView extends RelativeLayout {

    private final LayoutInflater mInflater;
    private Animation inAnimation;
    private Animation outAnimation;

    public NotificationPopUpNetworkErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (!isInEditMode()) {
            mInit(context);
        }
    }

    public NotificationPopUpNetworkErrorView(Context context) {
        this(context, null);
    }

    private void mInit(Context ctx) {
        mInflater.inflate(R.layout.view_networkerror, this, true);
        inAnimation = AnimationUtils.loadAnimation(ctx, R.anim.slide_up);
        outAnimation = AnimationUtils.loadAnimation(ctx, R.anim.slide_down);
    }

    @Override
    public final void setVisibility(int visibility) {
        if (getVisibility() != visibility) {
            if (visibility == VISIBLE && inAnimation != null) {
                if (!Utils.isOnline())
                    startAnimation(inAnimation);
//                Log.d("Safelet setVisibility ", "".concat("Visible"));
            } else if (((visibility == INVISIBLE) || (visibility == GONE)) && outAnimation != null) {
                startAnimation(outAnimation);
//                Log.d("Safelet setVisibility ", "".concat("Hide"));
            }
        }
        super.setVisibility(visibility);
    }
}
