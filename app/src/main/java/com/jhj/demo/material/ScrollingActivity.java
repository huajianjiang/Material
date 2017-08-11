package com.jhj.demo.material;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jhj.demo.material.test.MyListAdapter;
import com.jhj.demo.material.util.Logger;
import com.jhj.demo.material.util.Utils;

import java.util.List;

public class ScrollingActivity extends AppCompatActivity {
    private static final String TAG = ScrollingActivity.class.getSimpleName();

    private ListView mListView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        final CoordinatorLayout coordinatorLayout =
                (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isAnim = false;
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @android.support.annotation.RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            }
        });


        final int statusBarHeight = Utils.getStatusBarHeight(this);
        final int actionBarHeight = Utils.getActionBarHeight(this);

        Logger.e(TAG, "statusBarHeight=" + statusBarHeight + ",actionBarHeight=" + actionBarHeight);

        Logger.e(TAG, "height of appbar=" +
                      getResources().getDimensionPixelSize(R.dimen.app_bar_height));


        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                Logger.e(TAG, "TotalScrollRange=" + appBarLayout.getTotalScrollRange());
            }
        });


        //        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) image
//                .getLayoutParams();
//        params.setMargins(Utils.dp2px(this, 32),
//                          statusBarHeight + actionBarHeight + Utils.dp2px(this, 16), 0, 0);

        final View behaviorView = findViewById(R.id.common_layout);

        final CollapsingToolbarLayout toolbarLayout =
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);

        //        toolbarLayout.setExpandedTitleMargin(
//                Utils.dp2px(ScrollingActivity.this, 64),
//                actionBarHeight + Utils.dp2px(ScrollingActivity.this, 16), 0, 0);
        toolbarLayout.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                Logger.e(TAG, "toolbarLayout_Height=" + toolbarLayout.getHeight() + ",minHeight=" +
                              toolbarLayout.getMinimumHeight());
                ViewGroup.LayoutParams lp = toolbarLayout.getLayoutParams();
                ViewGroup.MarginLayoutParams blp =
                        (ViewGroup.MarginLayoutParams) behaviorView.getLayoutParams();
                lp.height = behaviorView.getHeight() + actionBarHeight + statusBarHeight +
                            blp.topMargin + blp.bottomMargin;
                toolbarLayout.setLayoutParams(lp);
            }
        });

        NestedScrollView nestedView = (NestedScrollView) findViewById(R.id.nestedScrollView);
        nestedView.setNestedScrollingEnabled(true);

        mListView = (ListView) findViewById(R.id.listView);
        MyListAdapter adapter = new MyListAdapter(getApplicationContext());
        mListView.setAdapter(adapter);

    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
