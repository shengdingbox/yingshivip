package cn.dabaotv.movie.record;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.video.R;

import cn.m.cn.styles.StyleStatusBar;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class RecordActivity extends SwipeBackActivity implements View.OnClickListener {
    private TextView HeadTitle;
    private TextView HeadTtEdit;

    private Intent intent;
    private String type; // 历史/收藏/下载
    private FrameLayout mFrame;
    private RecordView mRecordView;

    private RelativeLayout SelectDeleteView;

    private int listSize;
    private boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        StyleStatusBar.setWhiteBar(this);
        HeadTitle = (TextView)findViewById(R.id.HeadTitle);
        intent = getIntent();
        loadview();
        type = intent.getStringExtra("type");
        Q.log("TYPE",type);
        if (type == null) return;
        switch (type.toString()){
            case Qe.RECORDTYPE_历史:
                loadRecordView();
                break;
            case Qe.RECORDTYPE_收藏:
                loadRecordView();
                break;
            case Qe.RECORDTYPE_缓存:
                loadRecordView();
                break;
            default:
                loadDownloadSoreData();
        }
        HeadTitle.setText(getIntent().getStringExtra("name"));
    }
    public void loadview(){
        HeadTtEdit = (TextView) findViewById(R.id.HeadTtEdit);
        SelectDeleteView = (RelativeLayout)findViewById(R.id.SelectDeleteView);
        HeadTtEdit.setOnClickListener(this);

        mFrame = (FrameLayout) findViewById(R.id.frame);
    }
    public void loadRecordView(){
        if(type.equals(Qe.RECORDTYPE_缓存)){
            HeadTtEdit.setVisibility(View.GONE);
        }
        mRecordView = new RecordView(this,type);
        mFrame.removeAllViews();
        mFrame.addView(mRecordView);
    }
    public void loadDownloadSoreData(){
        String tid = intent.getStringExtra("tid");
        int TableId = 0;
        mRecordView = new RecordView(this,type,tid);
        mFrame.removeAllViews();
        mFrame.addView(mRecordView);
    }
    public void onEditMode(){
        int state = 0; // 0 非编辑状态、1编辑中
        state = mRecordView.setEditMode(2);
        if (state == 0){
            isEditMode = false;
            HeadTtEdit.setText("编辑");
            SelectDeleteView.setVisibility(View.GONE);
        }else {
            isEditMode = true;
            HeadTtEdit.setText("完成");
            SelectDeleteView.setVisibility(View.VISIBLE);
        }
    }
    public void onDelete(){
        listSize = mRecordView.deleteSelect();

        if (listSize < 1){
            HeadTtEdit.setText("编辑");
            SelectDeleteView.setVisibility(View.GONE);
        }
    }
    public void onSelectAll(){
        boolean b1 = false;
        b1 = mRecordView.setSelectAll();
    }


    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.HeadTtEdit:
                onEditMode();
                break;
            case R.id.SelectDelete:
                // 删除
                onDelete();
                break;
            case R.id.SelectAll:
                // 全选
                onSelectAll();
                break;
            case R.id.HeadReturn:
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (isEditMode == true){
                onEditMode();
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (mRecordView != null){
            mRecordView.pause();
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        if (mRecordView != null){
            mRecordView.resume();
        }
    }



}
