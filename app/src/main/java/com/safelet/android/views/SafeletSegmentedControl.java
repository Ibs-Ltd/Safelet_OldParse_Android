package com.safelet.android.views;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.safelet.android.R;

public class SafeletSegmentedControl extends TabLayout {

    public SafeletSegmentedControl(Context context) {
        super(context);
    }

    public SafeletSegmentedControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafeletSegmentedControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        super.setupWithViewPager(viewPager);
        ViewGroup tabStrip = (ViewGroup) getChildAt(0);
        tabStrip.getChildAt(0).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_left));
        tabStrip.getChildAt(getTabCount() - 1).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.rounded_corner_right));
    }
}
