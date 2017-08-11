package com.jhj.demo.material.behavoir;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.jhj.demo.material.R;
import com.jhj.demo.material.util.Logger;

import java.util.List;

/**
 * @author HuaJian Jiang.
 *         Date 2016/12/24.
 */
@SuppressWarnings("unused")
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CommonBehavior extends CoordinatorLayout.Behavior<View> {
    private static final String TAG = CommonBehavior.class.getSimpleName();

    private OnOffsetUpdateListener mOffsetUpdateListener;
    private int mVerticalOffset;

    private int mSysWindowInsertTop = -1;
    private int mChildVisibleHeightTrigger = -1;

    private Rect mExpandedCollapseBounds = new Rect();
    private Rect mCurrentCollapseBounds = new Rect();
    private Rect mExpandedChildBounds = new Rect();
    private Rect mCurrentChildBounds = new Rect();

    private int mCollapseRangeGravity;
    private int mCollapsingToolbarLayoutId;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private int mToEndOfViewId;
    private View mToEndOfView;
    private Animator mShowAnimator;
    private Animator mHideAnimator;
    private boolean mHasLayout = false;
    private boolean mHasSetExpandedTitleMargin = false;
    private boolean mIsAnimating = false;

    public CommonBehavior() {
        Logger.e(TAG, "***********CommonBehavior************");
    }

    public CommonBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        Logger.e(TAG, "*********** Init CommonBehavior from layout ************");
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CommonBehaviorLayout);
        mCollapseRangeGravity = a
                .getInt(R.styleable.CommonBehaviorLayout_layout_collapseRangeGravity,
                        GravityCompat.START | Gravity.TOP);
        mCollapsingToolbarLayoutId = a
                .getResourceId(R.styleable.CommonBehaviorLayout_collapsingToolbarLayoutId, -1);

        mToEndOfViewId = a.getResourceId(R.styleable.CommonBehaviorLayout_layout_toEndOf, -1);
        final int showAnimatorResId = a.getResourceId(R.styleable.CommonBehaviorLayout_showAnimation, -1);
        final int hideAnimatorResId = a.getResourceId(R.styleable.CommonBehaviorLayout_hideAnimation, -1);

        if (showAnimatorResId != -1)
            mShowAnimator = AnimatorInflater.loadAnimator(context, showAnimatorResId);
        if (hideAnimatorResId != -1)
            mHideAnimator = AnimatorInflater.loadAnimator(context, hideAnimatorResId);

        Logger.e(TAG, "CollapsingToolbarLayoutId=" + mCollapsingToolbarLayoutId);
        a.recycle();
    }

    @Override
    public void onAttachedToLayoutParams(@NonNull CoordinatorLayout.LayoutParams params) {
        super.onAttachedToLayoutParams(params);
    }

    @Override
    public void onDetachedFromLayoutParams() {
        super.onDetachedFromLayoutParams();
    }


    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        findCollapsingToolbarLayout(parent);
        if (mShowAnimator != null) mShowAnimator.setTarget(child);
        if (mHideAnimator != null) mHideAnimator.setTarget(child);
        if (dependency instanceof AppBarLayout && mOffsetUpdateListener == null) {
            mOffsetUpdateListener = new OnOffsetUpdateListener(child);
            ((AppBarLayout) dependency).addOnOffsetChangedListener(mOffsetUpdateListener);
        }
        return dependency instanceof AppBarLayout;
    }

    private void findCollapsingToolbarLayout(CoordinatorLayout parent) {
        if (mCollapsingToolbarLayout != null) return;
        if (mCollapsingToolbarLayoutId != -1) {
            View child = parent.findViewById(mCollapsingToolbarLayoutId);
            if (child instanceof CollapsingToolbarLayout) {
                Logger.e(TAG,"yes ,find !!!!!!!!!!!!!!!!!!!!!!!!!");
                mCollapsingToolbarLayout = (CollapsingToolbarLayout) child;
            }
        }
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        boolean handled = false;

        AppBarLayout appBar = (AppBarLayout) dependency;
        Logger.e(TAG,
                 "onDependentViewChanged===>" + appBar.getLeft() + "," + appBar.getTop() + "," +
                 appBar.getRight() + "," + appBar.getBottom());

        checkAnim(appBar, child);

        if (canUpdateChildView(appBar, child)) {

            int left = mExpandedChildBounds.left;
            final int top = mExpandedChildBounds.top + appBar.getTop();

            CoordinatorLayout.LayoutParams childLp = (CoordinatorLayout.LayoutParams) child
                    .getLayoutParams();
            final int childVisibleHeightTrigger = getChildVisibleHeightTrigger(appBar);

            final float fraction = Math.abs(appBar.getTop()) /
                                   (float) (appBar.getHeight() - childVisibleHeightTrigger);

            float alpha = 1 - (fraction < 0 ? 0 : fraction > 1 ? 1 : fraction);

            Logger.e(TAG, "alphaFraction=" + fraction + ",Height-TriggerAnimHeight=" +
                          (appBar.getHeight() - childVisibleHeightTrigger) + ",top=" +
                          appBar.getTop());

            //水平方向平移过渡
            left -= childLp.leftMargin * (fraction < 0 ? 0 : fraction > 1 ? 1 : fraction);

            mCurrentChildBounds.set(left, top, left + child.getWidth(), top + child.getHeight());
            mCurrentCollapseBounds.set(appBar.getLeft(),
                    appBar.getBottom() + appBar.getTop() - appBar.getTotalScrollRange(),
                    appBar.getRight(), appBar.getBottom());

            //透明度过渡
            child.setAlpha(alpha);

            Logger.e(TAG, "onDependentViewChanged>>>>>>>" + child.getTag() + "\n" +
                          mCurrentChildBounds.toShortString());

            child.layout(mCurrentChildBounds.left, mCurrentChildBounds.top,
                         mCurrentChildBounds.right, mCurrentChildBounds.bottom);

            handled = true;
        }

        return handled;
    }

    private boolean canTransition(AppBarLayout appbar) {
//        Logger.e(TAG, Math.abs(mVerticalOffset) + "-----" +
//                      (appbar.getHeight() - getChildVisibleHeightTrigger(appbar)));
        return Math.abs(mVerticalOffset) <=
               appbar.getHeight() - getChildVisibleHeightTrigger(appbar) && mVerticalOffset < 0;
    }

    private boolean hasExpanded(AppBarLayout appbar) {
        return mVerticalOffset >= 0 || appbar.getBottom() == mExpandedCollapseBounds.bottom;
    }

    private boolean hasCollapsed(AppBarLayout appbar) {
        return Math.abs(mVerticalOffset) >= mExpandedCollapseBounds.height() ||
               appbar.getBottom() == mExpandedCollapseBounds.top;
    }

    private void checkAnim(AppBarLayout appBar,View child) {
        if (mHideAnimator != null || mShowAnimator != null) {
            final int triggerAnimHeight = getChildVisibleHeightTrigger(appBar);
//            Logger.e(TAG, "triggerAnimHeight=" + triggerAnimHeight);

            if (appBar.getBottom() <= triggerAnimHeight) {
                if (mShowAnimator != null && mShowAnimator.isRunning()) mShowAnimator.cancel();
                if (mHideAnimator != null && !mHideAnimator.isRunning() &&
                    child.getVisibility() == View.VISIBLE)
                {
                    Logger.i(TAG, "start alpha_hide animator !!!!!!!!!!!!!!");
                    mHideAnimator.addListener(getHideAnimator(child));
                    mHideAnimator.start();
                }
            } else {
                if (mHideAnimator != null && mHideAnimator.isRunning()) mHideAnimator.cancel();
                if (mShowAnimator != null && !mShowAnimator.isRunning() &&
                    child.getVisibility() == View.GONE)
                {
                    Logger.i(TAG, "start alpha_show animator !!!!!!!!!!!!!!");
                    mShowAnimator.addListener(getShowAnimator(child));
                    mShowAnimator.start();
                }
            }
        }
    }

    private Animator.AnimatorListener getHideAnimator(View child) {
        return mHideAnimListener != null ? mHideAnimListener : new HideAnimatorListener(child);
    }

    private Animator.AnimatorListener getShowAnimator(View child) {
        return mShowAnimListener != null ? mShowAnimListener : new ShowAnimatorListener(child);
    }

    private boolean canUpdateChildView(AppBarLayout appbar, View child) {
        return child.getVisibility() == View.VISIBLE && !hasExpanded(appbar) &&
               !hasCollapsed(appbar);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        Logger.i(TAG, "*********onLayoutChild*********>>>>>>>>>>>" + child.getTag());

        if (child.getVisibility() == View.VISIBLE && !mIsAnimating && !mHasLayout) {

            //找到 AppbarLayout 依赖
            final List<View> dependencies = parent.getDependencies(child);
            AppBarLayout appBar = null;
            for (int i = 0; i < dependencies.size(); i++) {
                final View dependency = dependencies.get(i);
                if (dependency instanceof AppBarLayout) {
                    appBar = (AppBarLayout) dependency;
                    break;
                }
            }

            if (appBar != null) {

                Logger.i(TAG, "++++++++reCalculate  child bounds++++++++++");
                final int collapseRange = appBar.getTotalScrollRange();
                Logger.i(TAG, "collapseRange=" + collapseRange);

                mExpandedCollapseBounds.set(appBar.getLeft(),
                                            appBar.getBottom() - appBar.getTop() - collapseRange,
                                            appBar.getRight(), appBar.getBottom());

                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child
                        .getLayoutParams();
                //约束后的可布置 child 的相对区域
                Rect constrainedRect = new Rect(mExpandedCollapseBounds);
                final boolean changed = constrainedRect.intersect(
                        mExpandedCollapseBounds.left + appBar.getPaddingLeft() + lp.leftMargin,
                        mExpandedCollapseBounds.top + lp.topMargin,
                        mExpandedCollapseBounds.right - appBar.getPaddingRight() - lp.rightMargin,
                        mExpandedCollapseBounds.bottom - appBar.getPaddingBottom() -
                        lp.bottomMargin);

                //计算 child 布局边界
                GravityCompat.apply(mCollapseRangeGravity, child.getMeasuredWidth(),
                        child.getMeasuredHeight(), constrainedRect, mExpandedChildBounds,
                        ViewCompat.getLayoutDirection(parent));

                child.layout(mExpandedChildBounds.left, mExpandedChildBounds.top,
                             mExpandedChildBounds.right, mExpandedChildBounds.bottom);

                mHasLayout = true;

                Logger.i(TAG,
                         "onLayoutChild>>>>>>>>>>>>>>" + mExpandedCollapseBounds.toShortString() +
                         "," + mExpandedChildBounds.toShortString());
            }

            setupExpandedTitleMargin(parent, child);
        }

        return true;
    }


    private void setupExpandedTitleMargin(CoordinatorLayout parent, View child) {
        if (mHasSetExpandedTitleMargin) return;
        findCollapsingToolbarLayout(parent);
        if (mCollapsingToolbarLayout != null) {
            findToEndOfView(child);
            final int expandedTitleMarginLeft;
            if (mToEndOfView != null) {
                Logger.e(TAG, "ToEndOfView.getLeft()=" + mToEndOfView.getLeft());
                expandedTitleMarginLeft = mExpandedChildBounds.left + mToEndOfView.getLeft() +
                                          mToEndOfView.getMeasuredWidth() +
                                          mCollapsingToolbarLayout.getExpandedTitleMarginStart();
            } else {
                expandedTitleMarginLeft = mExpandedChildBounds.left + mExpandedChildBounds.width() +
                                          mCollapsingToolbarLayout.getExpandedTitleMarginStart();
            }

            final int expandedTitleMarginTop =
                    ViewCompat.getMinimumHeight(mCollapsingToolbarLayout) +
                    mCollapsingToolbarLayout.getExpandedTitleMarginTop();

            Logger.e(TAG, "expandedTitleMarginTop=====>" + expandedTitleMarginTop);

            mCollapsingToolbarLayout.setExpandedTitleMargin(expandedTitleMarginLeft,
                                                            expandedTitleMarginTop,
                                                            mCollapsingToolbarLayout.getExpandedTitleMarginEnd(),
                                                            mCollapsingToolbarLayout.getExpandedTitleMarginBottom());
            mHasSetExpandedTitleMargin = true;
        }
    }

    private void findToEndOfView(View child) {
        if (mToEndOfView != null) return;
        if (mToEndOfViewId != -1) {
            mToEndOfView = child.findViewById(mToEndOfViewId);
        }
    }

    @NonNull
    @Override
    public WindowInsetsCompat onApplyWindowInsets(CoordinatorLayout coordinatorLayout, View child,
                                                  WindowInsetsCompat insets)
    {
        Logger.e(TAG, "onApplyWindowInsets");
        mSysWindowInsertTop = insets.getSystemWindowInsetTop();
        return super.onApplyWindowInsets(coordinatorLayout, child, insets);
    }

    private int getChildVisibleHeightTrigger(AppBarLayout appBar) {
        if (mChildVisibleHeightTrigger >= 0) return mChildVisibleHeightTrigger;
        // If we reach here then we don't have a min height explicitly set. Instead we'll take a
        // guess at 1/3 of our height being visible
        int childVisibleHeightTrigger = appBar.getHeight() / 3;

        final int minHeight = ViewCompat.getMinimumHeight(appBar);
        if (minHeight != 0) {
            childVisibleHeightTrigger = minHeight * 2 + mSysWindowInsertTop;
        } else {
            // Otherwise, we'll use twice the min height of our last child
            final int childCount = appBar.getChildCount();
            final int lastChildMinHeight =
                    childCount >= 1 ? ViewCompat.getMinimumHeight(appBar.getChildAt(childCount - 1))
                                    : 0;
            if (lastChildMinHeight != 0) {
                childVisibleHeightTrigger = (lastChildMinHeight * 2) + mSysWindowInsertTop;
            }
        }

        return mChildVisibleHeightTrigger = childVisibleHeightTrigger;
    }



    private Animator.AnimatorListener mHideAnimListener;
    private class HideAnimatorListener extends AnimatorListenerAdapter {
        View child;

         HideAnimatorListener(View child) {
            this.child = child;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            Logger.e(TAG, "HideAnimator_onAnimationStart");
            mIsAnimating = true;
            child.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            Logger.e(TAG, "HideAnimator_onAnimationCancel");
            mIsAnimating = false;
            child.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Logger.e(TAG, "HideAnimator_onAnimationEnd");
            mIsAnimating = false;
            child.setVisibility(View.GONE);
        }
    }


    private Animator.AnimatorListener mShowAnimListener;
    private class ShowAnimatorListener extends AnimatorListenerAdapter {
        View child;

        ShowAnimatorListener(View child) {
            this.child = child;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            Logger.e(TAG, "ShowAnimator_onAnimationStart");
            mIsAnimating = true;
            child.setVisibility(View.VISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            Logger.e(TAG, "ShowAnimator_onAnimationCancel");
            mIsAnimating = false;
            child.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            Logger.e(TAG, "ShowAnimator_onAnimationEnd");
            mIsAnimating = false;
            child.setVisibility(View.VISIBLE);
        }
    }

    private class OnOffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        View child;

        OnOffsetUpdateListener(View child) {
            this.child = child;
        }

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            Logger.i(TAG, "onOffsetChanged=" + verticalOffset);
            mVerticalOffset = verticalOffset;

            int left = mExpandedChildBounds.left;
            final int top = mExpandedChildBounds.top + mVerticalOffset;
            int alpha = 1;

            if (hasExpanded(appBarLayout) || hasCollapsed(appBarLayout)) {
                Logger.i(TAG, "onOffsetChanged_Need force layout child ！！！！");
                if (hasCollapsed(appBarLayout)) {
                    alpha = 0;
                    left = 0;
                }
                child.setAlpha(alpha);
                child.layout(left, top, left + child.getWidth(), top + child.getHeight());
            }
        }
    }

                                                                                                                       }
