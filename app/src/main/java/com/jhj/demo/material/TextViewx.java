package com.jhj.demo.material;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.widget.TextView;

/**
 * <p>Author: Huajian Jiang
 * <br>Date: 2017/6/25
 * <br>Email: developer.huajianjiang@gmail.com
 */
public class TextViewx extends AppCompatTextView {
    private static final String TAG = TextViewx.class.getSimpleName();
    private OnDrawableClickListener mDrawableClickListener;

    private final Rect mCompoundRect = new Rect();
    private Drawable[] mDrs;

    public TextViewx(Context context) {
        this(context, null);
    }

    public TextViewx(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public TextViewx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDrs = getCompoundDrawablesRelative();
        } else {
            mDrs = getCompoundDrawables();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                return checkCompound(event);
            default:
                return super.onTouchEvent(event);
        }
    }

    private boolean checkCompound(MotionEvent event) {
        if (mDrs != null) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final int left = getLeft();
            final int top = getTop();
            final int right = getRight();
            final int bottom = getBottom();
            final int compoundPaddingLeft = getCompoundPaddingLeft();
            final int compoundPaddingTop = getCompoundPaddingTop();
            final int compoundPaddingRight = getCompoundPaddingRight();
            final int compoundPaddingBottom = getCompoundPaddingBottom();

            int index = 0;
            for (Drawable dr : mDrs) {
                if (dr != null) {
                    dr.copyBounds(mCompoundRect);

                    Log.e(TAG, "mCompoundRect==>" + mCompoundRect.flattenToString());

                    int compoundX;
                    int compoundY;

                    if (index == DrawableType.LEFT.ordinal() ||
                        index == DrawableType.RIGHT.ordinal())
                    {
                        final int vspace =
                                bottom - top - compoundPaddingTop - compoundPaddingBottom;
                        compoundY = (vspace - mCompoundRect.height()) / 2 + compoundPaddingTop;

                        Log.e(TAG, "vspace=" + vspace + ",CompoundH=" + mCompoundRect.height());

                        if (index == DrawableType.LEFT.ordinal()) {
                            compoundX = getPaddingLeft();
                        } else {
                            compoundX = right - left - getPaddingRight() - mCompoundRect.width();
                        }

                    } else {
                        final int hspace =
                                right - left - compoundPaddingRight - compoundPaddingLeft;
                        compoundX = (hspace - mCompoundRect.width()) / 2 + compoundPaddingLeft;

                        if (index == DrawableType.TOP.ordinal()) {
                            compoundY = getPaddingTop();
                        } else {
                            compoundY = bottom - top - getPaddingBottom() - mCompoundRect.height();
                        }
                    }

                    final int checkX = x - compoundX;
                    final int checkY = y - compoundY;

                    if (mCompoundRect.contains(checkX, checkY)) {
                        Log.e(TAG, "hit drawable==>" + index);
                        playSoundEffect(SoundEffectConstants.CLICK);
                        if (mDrawableClickListener != null) {
                            mDrawableClickListener
                                    .onClick(this, DrawableType.getTypeOrdinal(index), dr);
                        }
                        return true;
                    }
                }
                index++;
            }
        }
        return false;
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top,
                                               @Nullable Drawable right, @Nullable Drawable bottom)
    {
        super.setCompoundDrawables(left, top, right, bottom);
        mDrs = getCompoundDrawables();
    }

    @Override
    public void setCompoundDrawablesRelative(@Nullable Drawable start, @Nullable Drawable top,
                                             @Nullable Drawable end, @Nullable Drawable bottom)
    {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mDrs = getCompoundDrawablesRelative();
        }
    }

    public enum DrawableType {
        LEFT, TOP, RIGHT, BOTTOM;

        static DrawableType getTypeOrdinal(int ordinal) {
            switch (ordinal) {
                case 0:
                    return LEFT;
                case 1:
                    return TOP;
                case 2:
                    return RIGHT;
                case 3:
                    return BOTTOM;
                default:
                    throw new RuntimeException("can not find DrawableType for ordinal: " + ordinal);
            }
        }
    }

    public void setOnDrawableClickListener(OnDrawableClickListener listener)
    {
        this.mDrawableClickListener = listener;
    }

    public interface OnDrawableClickListener {
        void onClick(TextView container, DrawableType type, Drawable drawable);
    }

}
