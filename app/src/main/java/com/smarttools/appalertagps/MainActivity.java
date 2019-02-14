package com.smarttools.appalertagps;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.polyak.iconswitch.IconSwitch;
import com.polyak.iconswitch.IconSwitch.Checked;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int DURATION_COLOR_CHANGE_MS = 400;

    private int[] toolbarColors;
    private int[] statusBarColors;
    private ValueAnimator statusBarAnimator;
    private Interpolator contentInInterpolator;
    private Interpolator contentOutInterpolator;
    private Point revealCenter;

    private Window window;
    private View toolbar;
    private View content;
    private IconSwitch iconSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        window = getWindow();

        initColors();
        initAnimationRelatedFields();

        content = findViewById(R.id.content);
        toolbar = findViewById(R.id.toolbar);
        TextView title = (TextView) findViewById(R.id.toolbar_title);
        title.setText(R.string.app_name);

        iconSwitch = (IconSwitch) findViewById(R.id.icon_switch);
        iconSwitch.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {

            @Override
            public void onCheckChanged(Checked current) {

                updateColors(true);
                changeContentVisibility();
            }
        });

        updateColors(false);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment fragment = (SupportMapFragment) fm.findFragmentById(R.id.map_container);

        if (fragment == null) {

            fragment = new SupportMapFragment();
            fm.beginTransaction().replace(R.id.map_container, fragment).commit();
        }

        fragment.getMapAsync(this);
    }

    private void updateColors(boolean animated) {

        int colorIndex = iconSwitch.getChecked().ordinal();

        toolbar.setBackgroundColor(toolbarColors[colorIndex]);

        if (animated) {

            switch (iconSwitch.getChecked()) {

                case LEFT:
                    statusBarAnimator.reverse();
                    break;
                case RIGHT:
                    statusBarAnimator.start();
                    break;
            }

            revealToolbar();
        }
        else {

            window.setStatusBarColor(statusBarColors[colorIndex]);
        }
    }

    private void revealToolbar() {

        iconSwitch.getThumbCenter(revealCenter);
        moveFromSwitchToToolbarSpace(revealCenter);
        ViewAnimationUtils.createCircularReveal(toolbar, revealCenter.x, revealCenter.y, iconSwitch.getHeight(), toolbar.getWidth()).setDuration(DURATION_COLOR_CHANGE_MS).start();
    }

    private void changeContentVisibility() {

        int targetTranslation = 0;
        Interpolator interpolator = null;

        switch (iconSwitch.getChecked()) {

            case LEFT:
                targetTranslation = 0;
                interpolator = contentInInterpolator;
                break;

            case RIGHT:
                targetTranslation = content.getHeight();
                interpolator = contentOutInterpolator;
                break;
        }

        content.animate().cancel();

        content.animate()
                .translationY(targetTranslation)
                .setInterpolator(interpolator)
                .setDuration(DURATION_COLOR_CHANGE_MS)
                .start();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void initAnimationRelatedFields() {

        revealCenter = new Point();

        statusBarAnimator = createArgbAnimator(statusBarColors[Checked.LEFT.ordinal()], statusBarColors[Checked.RIGHT.ordinal()]);
        contentInInterpolator = new OvershootInterpolator(0.5f);
        contentOutInterpolator = new DecelerateInterpolator();
    }

    private void initColors() {

        toolbarColors = new int[Checked.values().length];
        statusBarColors = new int[toolbarColors.length];

        toolbarColors[Checked.LEFT.ordinal()] = color(R.color.informationPrimary);
        statusBarColors[Checked.LEFT.ordinal()] = color(R.color.informationPrimaryDark);
        toolbarColors[Checked.RIGHT.ordinal()] = color(R.color.mapPrimary);
        statusBarColors[Checked.RIGHT.ordinal()] = color(R.color.mapPrimaryDark);
    }

    private ValueAnimator createArgbAnimator(int leftColor, int rightColor) {

        ValueAnimator animator = ValueAnimator.ofArgb(leftColor, rightColor);

        animator.setDuration(DURATION_COLOR_CHANGE_MS);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                if (animator == statusBarAnimator) {

                    int color = (Integer) animator.getAnimatedValue();

                    window.setStatusBarColor(color);
                }
            }
        });

        return animator;
    }

    private void moveFromSwitchToToolbarSpace(Point point) {

        point.set(point.x + iconSwitch.getLeft(), point.y + iconSwitch.getTop());
    }

    @ColorInt
    private int color(@ColorRes int res) {

        return ContextCompat.getColor(this, res);
    }
}
