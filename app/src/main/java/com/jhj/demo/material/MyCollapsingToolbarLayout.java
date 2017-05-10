package com.jhj.demo.material;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.github.huajianjiang.expandablerecyclerview.util.Logger;

/**
 * @author HuaJian Jiang.
 *         Date 2017/1/5.
 */
public class MyCollapsingToolbarLayout extends CollapsingToolbarLayout {
    private static final String TAG = MyCollapsingToolbarLayout.class.getSimpleName();
    private AppBarLayout.OnOffsetChangedListener mOffsetChangedListener;
    private Rect mCollapseRect = new Rect();
    private Rect mTempRect = new Rect();
    private boolean mHasSetExpandedTitle = false;
    private int mToEndOfId;
    private View mToEndOf;

    public MyCollapsingToolbarLayout(Context context) {
        this(context, null);
    }

    public MyCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCollapsingToolbarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyCollapsingToolbarLayout);
//        mToEndOfId = a.getResourceId(R.styleable.MyCollapsingToolbarLayout_toEndOf, -1);
//        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent p = getParent();
        if (p instanceof AppBarLayout) {
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) p));
            if (mOffsetChangedListener == null) {
                mOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) p).addOnOffsetChangedListener(mOffsetChangedListener);
            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        ViewParent p = getParent();
        if (p instanceof AppBarLayout && mOffsetChangedListener != null) {
            ((AppBarLayout) p).removeOnOffsetChangedListener(mOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mToEndOf == null) mToEndOf = mToEndOfId != -1 ? findViewById(mToEndOfId) : null;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Logger.e(TAG, "left=" + left + ",top=" + top + ",right=" + right + ",bottom=" + bottom);
        ViewParent p = getParent();
        if (!(p instanceof AppBarLayout)) return;
        AppBarLayout appBarLayout = (AppBarLayout) p;
        final int collapseRange = appBarLayout.getTotalScrollRange();
        mCollapseRect.set(left, getHeight() - collapseRange, right, bottom);
        Logger.e(TAG, "CollapseRect=" + mCollapseRect.toShortString());
        Logger.e(TAG, "childCount=" + getChildCount());
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            Logger.e(TAG, "child=" + child.toString());
            if (child instanceof Toolbar) continue;
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int contentGravity = lp.expandedContentGravity;
            final int absContentGravity = GravityCompat
                    .getAbsoluteGravity(contentGravity, ViewCompat.getLayoutDirection(this));
            final int hGrav = absContentGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
            final int vGrav = absContentGravity & Gravity.VERTICAL_GRAVITY_MASK;

            int contentLeft;
            int contentTop;

            switch (hGrav) {
                default:
                case Gravity.LEFT:
                    contentLeft = mCollapseRect.left + lp.leftMargin;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    contentLeft =
                            mCollapseRect.left + (mCollapseRect.width() - child.getWidth()) / 2;
                    break;
                case Gravity.RIGHT:
                    contentLeft = mCollapseRect.right - lp.rightMargin;
                    break;
            }

            switch (vGrav) {
                default:
                case Gravity.TOP:
                    contentTop=mCollapseRect.top+lp.topMargin;
                    break;
                case Gravity.CENTER_VERTICAL:
                    contentTop =
                            mCollapseRect.top + (mCollapseRect.height() - child.getHeight()) / 2;
                    break;
                case Gravity.BOTTOM:
                    contentTop = mCollapseRect.bottom-lp.bottomMargin;
                    break;
            }

            mTempRect.set(contentLeft, contentTop, contentLeft + child.getWidth(),
                    contentTop + child.getHeight());
            child.layout(mTempRect.left, mTempRect.top, mTempRect.right, mTempRect.bottom);
        }

        setupExpandedTitle();

    }

    private void setupExpandedTitle() {
        if (mHasSetExpandedTitle) return;
        if (mToEndOf != null) {
            setExpandedTitleMargin(
                    mTempRect.left + mToEndOf.getWidth() + getExpandedTitleMarginStart(),
                    ViewCompat.getMinimumHeight(this) + getExpandedTitleMarginTop(),
                    getExpandedTitleMarginEnd(), getExpandedTitleMarginBottom());
            Logger.e(TAG, "--------》" + getExpandedTitleMarginTop());
            mHasSetExpandedTitle = true;
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MyCollapsingToolbarLayout.LayoutParams;
    }

    @Override
    protected CollapsingToolbarLayout.LayoutParams generateDefaultLayoutParams() {
        return new MyCollapsingToolbarLayout.LayoutParams(MyCollapsingToolbarLayout.LayoutParams.MATCH_PARENT, MyCollapsingToolbarLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MyCollapsingToolbarLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MyCollapsingToolbarLayout.LayoutParams(p);
    }

    public static class LayoutParams extends CollapsingToolbarLayout.LayoutParams {

        int expandedContentGravity;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
//            TypedArray a = c
//                    .obtainStyledAttributes(attrs, R.styleable.MyCollapsingToolbarLayout_Layout);
////            expandedContentGravity = a
////                    .getInt(R.styleable.MyCollapsingToolbarLayout_Layout_layout_expandedContentGravity,
////                            GravityCompat.START | Gravity.TOP);
//            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(19)
        @TargetApi(Build.VERSION_CODES.KITKAT)
        public LayoutParams(FrameLayout.LayoutParams source) {
            super(source);
        }
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            Logger.e(TAG, "TotalScrollRange=" + appBarLayout.getTotalScrollRange());
        }
    }

}
