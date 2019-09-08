package cn.m.bdplayer;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import cn.dabaotv.video.R;

/**
 * Created by H19 on 2018/5/11 0011.
 */

public class player_foot extends RelativeLayout implements View.OnClickListener {
    private View mView;
    private Activity activity;
    public TextView tt_time_start;
    public TextView tt_time_end;
    public ImageView bt_full;
    public TextView tt_drame;
    public TextView tt_line;
    private SeekBar seekBar;
    public ImageView ivPlay;
    public boolean isDisplayNext;
    public boolean isDisplayDrame;
    public boolean isDisplayLine;
    public player_in in;

    public player_foot(Context context) {
        super(context, null);
        loadview();
    }

    public player_foot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        loadview();
    }

    public player_foot(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadview();
    }

    public void setIn(player_in in) {
        this.in = in;
    }

    public void loadview() {
        mView = View.inflate(getContext(), R.layout.m_player_foot, this);
        tt_time_start = mView.findViewById(R.id.tt_time_start);
        tt_time_end = mView.findViewById(R.id.tt_time_end);
        bt_full = mView.findViewById(R.id.bt_full);
        bt_full.setOnClickListener(this);
        ivPlay = mView.findViewById(R.id.foot_bt_play);
        ivPlay.setOnClickListener(this);
        tt_drame = mView.findViewById(R.id.tt_drame);
        tt_drame.setOnClickListener(this);
        seekBar = mView.findViewById(R.id.seekBar);
        tt_line = mView.findViewById(R.id.tt_line);
        tt_line.setOnClickListener(this);
        findViewById(R.id.bt_next).setOnClickListener(this);

    }

    public int display_mode = Locode.DISPLAYMODE_单集;

    public void 设置显示模式(int mode) {
        display_mode = mode;
    }

    public void refresh() {
        if (in.hide全屏按钮() || in.isFull()) {
            bt_full.setVisibility(GONE);
            tt_time_start.setVisibility(VISIBLE);
            tt_time_end.setVisibility(VISIBLE);
        } else {
            bt_full.setVisibility(VISIBLE);
            tt_time_start.setVisibility(GONE);
            tt_time_end.setVisibility(GONE);
        }

        if (isDisplayDrame) {
            tt_drame.setVisibility(VISIBLE);
        } else {
            tt_drame.setVisibility(GONE);
        }
    }


    public void setFullButtonVisibility(int visibility) {
        bt_full.setVisibility(visibility);
    }


    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    public void setMaxProgress(int progress) {
        seekBar.setMax(progress);
    }

    public void setCurProgress(int progress){
        seekBar.setProgress(progress);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == bt_full.getId()) {
            in.回调(player_in.PLAYER_BT_FULLSCREEN);
        } else if (v.getId() == tt_drame.getId()) {
            in.回调(player_in.PLAYER_BT_DRAME);
        } else if (v.getId() == tt_line.getId()) {
            in.回调(player_in.PLAYER_BT_LINE);
        } else if (v.getId() == R.id.bt_next) {
            in.回调(player_in.PLAYER_BT_NEXT);
        } else if (v.getId() == R.id.foot_bt_play) {
            in.回调(player_in.PLAYER_BT_PLAY);
        }

    }
}
