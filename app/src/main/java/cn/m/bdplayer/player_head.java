package cn.m.bdplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.dabaotv.video.R;

import java.text.SimpleDateFormat;

/**
 * Created by H19 on 2018/5/11 0011.
 */

public class player_head extends LinearLayout implements View.OnClickListener{

    private View m_view;
    public ImageView m_bt_return; // 播放
    public TextView m_tt_name;
    public TextView m_tt_time;
    private ImageView img_cell;
    public ImageView m_bt_push;
    public ImageView bt_fj; // 飞机？？？？
    public ImageView bt_sc;

    public player_in in;
    public player_head(Context context) {
        super(context,null);
        loadview();
    }
    public player_head(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,0);
        loadview();
    }
    public player_head(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadview();
    }
    public void setIn(player_in in){
        this.in = in;
    }
    public void loadview(){
        m_view = View.inflate(getContext(), R.layout.m_player_head,this);
        m_bt_return = (ImageView) m_view.findViewById(R.id.bt_return);
        m_tt_name = (TextView)m_view.findViewById(R.id.tt_name);
        img_cell = (ImageView)findViewById(R.id.img_cell);
        m_tt_time = (TextView)findViewById(R.id.tt_time);
        m_bt_push = (ImageView)m_view.findViewById(R.id.bt_push) ;

        m_bt_return.setOnClickListener(this);  // 下菜单
        m_bt_push.setOnClickListener(this);
        bt_fj = (ImageView)m_view.findViewById(R.id.bt_jump);
        bt_sc = (ImageView)m_view.findViewById(R.id.bt_sc);
        bt_fj.setOnClickListener(this);
        bt_sc.setOnClickListener(this);

    }

    // 刷新界面显示
    public void refresh(){
        if (in.isFull()){
            m_view.findViewById(R.id.buttonbox).setVisibility(VISIBLE);
        }else {
            m_view.findViewById(R.id.buttonbox).setVisibility(GONE);
        }
    }
    public void setName(String name,String msg){
        m_tt_name.setText(name);
    }

    public void onClick(View v){
        if (v.getId() == m_bt_return.getId()){
            in.回调(player_in.PLAYER_BT_RETURN);
        }else if (v.getId() == m_bt_push.getId()){
            in.回调(player_in.PLAYER_BT_PUSH);
            //setPush();
        }else if (v.getId() == bt_fj.getId()){
            in.回调(player_in.PLAYER_BT_JUMP);
        }else if (v.getId() == bt_sc.getId()){
            in.回调(player_in.PLAYER_BT_SC);
        }
    }

    public void setPushButtonVisibility(int visibility){
        m_bt_push.setVisibility(visibility);
    }

    //监听电量变化
    public class BatteryBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context/* 触发广播的Activity */, Intent intent/* 触发广播的意图 */) {
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm");
            String date = sdf.format(new java.util.Date());
            m_tt_time.setText(date);


            Bundle extras = intent.getExtras();//获取意图中所有的附加信息
            //获取当前电量，总电量
            int level = extras.getInt(BatteryManager.EXTRA_LEVEL/*当前电量*/, 0);
            int total = extras.getInt(BatteryManager.EXTRA_SCALE/*总电量*/, 100);

            //电池温度温度
            int temperature = extras.getInt(BatteryManager.EXTRA_TEMPERATURE/*电池温度*/);

            if (level > 95){
                img_cell.setImageResource(R.drawable.ic_player_cell1);
            }else if (level > 70){
                img_cell.setImageResource(R.drawable.ic_player_cell2);
            }else if (level > 45){
                img_cell.setImageResource(R.drawable.ic_player_cell3);
            }else if (level > 20){
                img_cell.setImageResource(R.drawable.ic_player_cell4);
            }else if (level > 1){
                img_cell.setImageResource(R.drawable.ic_player_cell5);
            }

            //电池状态
            int status = extras.getInt(BatteryManager.EXTRA_STATUS/*电池状态*/);
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING://充电
                    if (level > 98){
                        img_cell.setImageResource(R.drawable.ic_player_cell1_c);
                    }else if (level > 70){
                        img_cell.setImageResource(R.drawable.ic_player_cell2_c);
                    }else if (level > 45){
                        img_cell.setImageResource(R.drawable.ic_player_cell3_c);
                    }else if (level > 20){
                        img_cell.setImageResource(R.drawable.ic_player_cell4_c);
                    }else{
                        img_cell.setImageResource(R.drawable.ic_player_cell5_c);
                    }

                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING://放电
                    if (level > 95){
                        img_cell.setImageResource(R.drawable.ic_player_cell1);
                    }else if (level > 70){
                        img_cell.setImageResource(R.drawable.ic_player_cell2);
                    }else if (level > 45){
                        img_cell.setImageResource(R.drawable.ic_player_cell3);
                    }else if (level > 20){
                        img_cell.setImageResource(R.drawable.ic_player_cell4);
                    }else if (level > 1){
                        img_cell.setImageResource(R.drawable.ic_player_cell5);
                    }
                    break;

                case BatteryManager.BATTERY_STATUS_FULL://充满
                    img_cell.setImageResource(R.drawable.ic_player_cell1_c);
                    break;
                //BatteryManager.BATTERY_STATUS_NOT_CHARGING，未充电，包括放电和充满
                //BATTERY_STATUS_UNKNOWN：未知状态
                default:
                    break;
            }
        }
    }
    private BatteryBroadcast bb;
    private IntentFilter iFilter;
    public void 监听电量(){
        bb = new BatteryBroadcast();
        iFilter = new IntentFilter();
        iFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        iFilter.addAction(Intent.ACTION_TIME_TICK); //每分钟变化的action
        getContext().registerReceiver(bb, iFilter);
    }
    private IntentFilter mFilter;
    public void 结束(){
        if (bb != null) getContext().unregisterReceiver(bb);
    }


}
