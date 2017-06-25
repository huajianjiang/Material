package com.jhj.demo.material;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author HuaJian Jiang.
 *         Date 2016/12/22.
 */
public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        final TextViewx tvx = (TextViewx) findViewById(R.id.tvx);
        tvx.setOnDrawableClickListener(new TextViewx.OnDrawableClickListener() {
            @Override
            public void onClick(TextView container, TextViewx.DrawableType type, Drawable drawable)
            {
                Toast.makeText(SettingsActivity.this, "onDrawableClick=>" + type.name(),
                               Toast.LENGTH_SHORT).show();
            }
        });

        ImageView iv = (ImageView) findViewById(R.id.iv);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(SettingsActivity.this, "ImageViewClick", Toast.LENGTH_SHORT).show();
            }
        });

        final TextView tv = (TextView) findViewById(R.id.tv);
        TextViewDrawableHelper.with(tv).listenDrawableClick(new TextViewDrawableHelper.OnDrawableClickListener() {
            @Override public void onClick(TextView container, TextViewDrawableHelper.DrawableType type,
                                          Drawable drawable)
            {
                Toast.makeText(SettingsActivity.this, "onDrawableClick=>" + type.name(),
                               Toast.LENGTH_SHORT).show();
            }
        });

//        final EditText et = (EditText) findViewById(R.id.searchView);
//        TextViewDrawableHelper.with(et).listenDrawableClick(new TextViewDrawableHelper.OnDrawableClickListener() {
//            @Override public void onClick(TextView container, TextViewDrawableHelper.DrawableType type,
//                                          Drawable drawable)
//            {
//                Toast.makeText(SettingsActivity.this, "onEditTextDrawableClick=>" + type.name(),
//                               Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}
