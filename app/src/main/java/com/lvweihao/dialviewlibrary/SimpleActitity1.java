package com.lvweihao.dialviewlibrary;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import com.lvweihao.dialview.DialView;
import com.lvweihao.dialview.OnDialViewTouch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by lvweihao on 2018/7/8.
 */

public class SimpleActitity1 extends AppCompatActivity {
    @BindView(R.id.dialView)
    DialView dialView;
    @BindView(R.id.seekBar)
    SeekBar seekBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple1);
        ButterKnife.bind(this);

        //初始化（0-100）
        seekBar.setProgress(0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dialView.setAngle(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //事件回调
        dialView.setOnDialViewTouchListener(new OnDialViewTouch() {
            @Override
            public void onTouched(int value) {
                seekBar.setProgress(value);
                //TODO 业务逻辑
            }
        });
    }

    @OnClick(R.id.btn_open)
    public void onViewClicked() {
    }
}
