package cn.dabaotv.movie;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import cn.dabaotv.video.R;
import cn.m.cn.styles.StyleStatusBar;
import cn.m.cn.信息框;
import cn.m.cn.本地;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;


public class SetActivity extends SwipeBackActivity implements View.OnClickListener {
    private boolean player_auto_next;
    private ImageView redio_player_auto_next;
    private TextView player_parse_waiting_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        StyleStatusBar.setWhiteBar(this);
        loadview();
        loadSetup();
    }

    public void loadSetup(){
        player_auto_next = 本地.get设置(this,"player_next_auto",true);
        refreshView();
    }

    public void loadview() {
        TextView tt_name = (TextView)findViewById(R.id.HeadTitle);
        tt_name.setText("设置");
        findViewById(R.id.set_player_load_time).setOnClickListener(this);
        findViewById(R.id.set_player_autonext).setOnClickListener(this);

        redio_player_auto_next = (ImageView) findViewById(R.id.set_player_autonext_redio);
        redio_player_auto_next.setOnClickListener(this);
        player_parse_waiting_time = (TextView)findViewById(R.id.set_player_parse_waiting_time);

    }



    public void onClick(View v){
        switch (v.getId()){
            case R.id.set_player_load_time:
                showLoadTimeEditDia();
                break;
            case R.id.set_player_autonext:
                player_auto_next = player_auto_next == true ? false : true;
                redio_player_auto_next.setSelected(player_auto_next);
                本地.save设置(this,"player_auto_next",player_auto_next);
                refreshView();

                break;
        }
    }

    public void showLoadTimeEditDia(){
        // 创建对话框构建器
        信息框.输入框3(this, "输入等待时间", player_parse_waiting_time.getText().toString(), new 信息框.diaListener() {
            @Override
            public void text(String t) {
                if (t == null || t.isEmpty()) t = "1";
                int i = 0;
                i = Integer.parseInt(t);
                if (i < 5){
                    Toast.makeText(SetActivity.this, "等待时间不得低于5秒", Toast.LENGTH_SHORT).show();
                    return;
                }
                本地.save设置(SetActivity.this,"player_parse_waiting_time",i);
                player_parse_waiting_time.setText(t + "秒");
            }
        });
    }

    public void refreshView(){
        if (player_auto_next == true) {
            redio_player_auto_next.setImageResource(R.drawable.ic_checked);
        } else {
            redio_player_auto_next.setImageResource(R.drawable.ic_uncheck);
        }
        player_parse_waiting_time.setText(本地.get设置(this,"player_parse_waiting_time",8) + "秒");

    }


}
