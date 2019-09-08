package cn.dabaotv.movie.record;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.LinearLayout;

import cn.dabaotv.movie.MyApplication;
import cn.dabaotv.movie.view.list.IListView;
import cn.dabaotv.movie.view.list.ItemAdapter;
import cn.dabaotv.movie.DB.DBDown;
import cn.dabaotv.movie.DB.DBRecord;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.view.list.ItemList;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by 幻陌 on 2018/9/5
 */

public class RecordView extends LinearLayout  {

    private IListView mlistView;
    private boolean isEditMode;

    private ItemClickListener mItemClickListener;
    private int selectnumber; // 记录选中个数
    private Intent windowPlay;
    private Intent windowDown;

    private String mType;
    private String mTid;

    public RecordView(Context context,String type){
        super(context);
        mType = type;
        ininView();
    }
    public RecordView(Context context,String type,String tid){
        super(context);
        mType = type;
        if (mType.equals(Qe.RECORDTYPE_正在缓存) && !isOpenReceiver){
            监听下载广播();
        }
        mTid = tid;
        ininView();
    }

    public void RefreshData(){
        mlistView.clear();
        String type = Qe.RECORDTYPE_历史;
        switch (mType.toString()){
            case Qe.RECORDTYPE_历史:
                type = Qe.RECORDTYPE_历史;
                break;
            case Qe.RECORDTYPE_收藏:
                type = Qe.RECORDTYPE_收藏;
                break;
            case Qe.RECORDTYPE_缓存:
                type = Qe.RECORDTYPE_缓存;
                break;
            case Qe.RECORDTYPE_正在缓存:
                loadDownList();
                return;
        }
        loadRecord(type);
    }


    private void loadRecord(String type){
        mlistView.clear();
        Q.log("type",type);
        if (type.equals(Qe.RECORDTYPE_缓存)){
            List<DBDown> downrecord = DataSupport.select("*")
                    .find(DBDown.class);
            ItemList itemC = new ItemList();
            itemC.name = "正在缓存";
            itemC.msg = "共 "+ downrecord.size() +" 个视频缓存中";
            itemC.imgId = R.drawable.ic_down;
            itemC.url = "00000";
            itemC.Id = -1;
            mlistView.addItem(itemC);
        }
        List<DBRecord> record = DataSupport.select("*")
                .where("type=?", type)
                .order("changetime desc")
                .find(DBRecord.class);
        for (DBRecord e : record){
            ItemList ic = new ItemList();
            ic.name = e.getName();
            ic.msg = "上次播放至 " + (e.getDrama() + 1) + "集";
            ic.img = e.getImg();
            ic.url = e.getUrl();
            ic.Id = e.getId();
            mlistView.addItem(ic);
        }

    }
    private void loadDownList(){
        mlistView.clear();
        mlistView.setStyle(Qe.LISTTYPE_下载进行中);
        List<DBDown> dws = DataSupport.where("state!=?",Integer.toString(Qe.DownState_Complete)).find(DBDown.class);
        for (DBDown e : dws){
            ItemList item = new ItemList();
            item.Id = e.getId();
            item.name = e.getName() + e.getIndex();
            item.img = e.getImg();
            item.msg = getDownStateMsg(e.getState());
            item.z = e.getDownProgress();
            mlistView.addItem(item);
        }
    }
    private String getDownStateMsg(int state){
        String msg = "";
        switch (state){
            case Qe.DOWNSTATE_N:msg = "等待缓存";break;
            case Qe.DownState_Ing:msg = "缓存中";break;
            case Qe.DownState_Pause:msg = "已暂停，点击恢复";break;
            case Qe.DownState_Error:case Qe.DownState_Error2:msg = "缓存失败，点击重试";break;
            case Qe.DownState_Complete:msg = "缓存完毕";break;
        }
        return msg;
    }


    public void ininView(){
        mlistView = new IListView(getContext());
        mlistView.setIsLoadMore(false);
        mlistView.setIsSwipeRefresh(false);
        if (mType.equals(Qe.RECORDTYPE_正在缓存)){
            mlistView.setLayout(R.layout.conl_list_downitem);
        }else {
            mlistView.setLayout(R.layout.itemlayout_history);
        }

        Q.log("dxxtype",mType);
        mlistView.setItemListener(new IListView.itemOnClickListener() {
            @Override
            public void onClick(View v, int position, ItemList item) {
                if (isEditMode){
                    selectItem(position);
                }else {
                    if (windowDown == null) windowDown = new Intent(getContext(), RecordActivity.class);
                    if (mType.equals(Qe.RECORDTYPE_缓存)){
                        if (position == 0){
                            windowDown.putExtra("type",Qe.RECORDTYPE_正在缓存);
                            getContext().startActivity(windowDown);
                        }else {
                            windowDown.putExtra("type",item.name);
                            windowDown.putExtra("tid",item.url);
                            windowDown.putExtra("TableId",item.Id);
                            getContext().startActivity(windowDown);
                        }

                    }else if (mType.equals(Qe.RECORDTYPE_历史) || mType.equals(Qe.RECORDTYPE_收藏)) {
                        Q.goPlayer(getContext(), item.url);
                    }else if (mType.equals(Qe.RECORDTYPE_正在缓存)){
                        setDownState(item,position);
                    }else {
                        Q.goPlayer(getContext(),item.url,Integer.toString(Qe.PLAYTYPE_直播));
                    }
                }
            }
            @Override
            public void startLoadMore(ItemAdapter adapter) {

            }
        });

        this.addView(mlistView);
        RefreshData();
    }

    public void setDownState(ItemList item,int position){
        int state2 = 0;
        switch (item.z){
            case Qe.DownState_Error: case Qe.DownState_Error2: // 重新下载 接入等待中
                state2 = 0;
                break;
            case Qe.DownState_Pause: // 到等待
                state2 = 0;
                break;
            case Qe.DOWNSTATE_N: case Qe.DownState_Ing: // 暂停
                state2 = Qe.DownState_Pause;
                break;
        }
        item.z = state2;
        item.msg = getDownStateMsg(state2);
        // 保存到数据库中
        DBDown dbDown = DataSupport.find(DBDown.class,item.Id);
        if (dbDown!=null){
            dbDown.setState(state2);
            dbDown.save();
            MyApplication.RefreshDown();
        }

        mlistView.refreshItem(position);
    }
    public boolean setSelectAll(){
        boolean b1;
        if (selectnumber == mlistView.size()){
            b1 = false;
            selectnumber = 0;
        }else{
            b1 = true;
            selectnumber = mlistView.size();
        }
        for (int i1 = 0;i1 < mlistView.size();i1++){
            mlistView.getItem(i1).select = b1;
        }
        mlistView.refresh();
        return b1;
    }
    public void selectItem(int position){
        if (mlistView.getItem(position).select == true){
            mlistView.getItem(position).select = false;
            selectnumber = selectnumber - 1;
        }else {
            mlistView.getItem(position).select = true;
            selectnumber = selectnumber + 1;
        }
    }
    public int setEditMode(int b){
        // 0非编辑、1 编辑中、2自动识别
        if (b == 2){
            if (isEditMode){
                b = 0; // 编辑中则转为不编辑
            }else {
                b = 1; // 未开启编辑则开启编辑模式
            }
        }

        if (b == 1){
            isEditMode = true;
            mlistView.setMultiSelectionMode(2);
        }else{
            isEditMode = false;
            mlistView.setMultiSelectionMode(1);
        }
        return b;
    }
    public boolean getEditMode(){return isEditMode;}
    public int deleteSelect(){
        for (int i1 = 0;i1 < mlistView.size();i1++){
            if (mlistView.getItem(i1).select == true){
                DataSupport.delete(DBRecord.class,mlistView.getItem(i1).Id);
            }
        }
        // 刷新表中数据
        RefreshData();
        if (mlistView.size() < 1) setEditMode(2);
        return mlistView.size();
    }
    public interface ItemClickListener{
        void ItemClick(int position);
    }
    public void setItemOnClickListener(ItemClickListener itemOnClickListener){
        mItemClickListener = itemOnClickListener;
    }


    private int cutDownId = 0;
    private DBDown cutDown;

    private void 设置状态(String id,String state,String progres){
        if (mlistView == null || mlistView.getList() == null) return;
        if (mlistView.getList().size() < cutDownId || mlistView.getItem(cutDownId) == null
                || mlistView.getItem(cutDownId).Id != Integer.parseInt(id)){
            // 重新选择选中项目
            cutDownId = -1;
            for (int i = 0; i < mlistView.getList().size(); i++) {
                if (mlistView.getList().get(i).Id == Integer.parseInt(id)){
                    cutDownId = i;
                    break;
                }
            }
        }

        if (cutDownId != -1){
            mlistView.getList().get(cutDownId).z = Integer.parseInt(progres);
            mlistView.getList().get(cutDownId).msg = getDownStateMsg(Integer.parseInt(state));
            mlistView.refreshItem(cutDownId);
        }


    }

    private boolean isOpenReceiver;
    public void 监听下载广播(){
        isOpenReceiver = true;
        msgReceiver = new MsgReceiver();// 1. 实例化BroadcastReceiver子类 &  IntentFilter
        IntentFilter intentFilter = new IntentFilter();// 2. 设置接收广播的类型
        intentFilter.addAction("qiju.down");
        getContext().registerReceiver(msgReceiver, intentFilter);// 3. 动态注册：调用Context的registerReceiver（）方法
    }
    public class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent){
            if (!mType.equals(Qe.RECORDTYPE_正在缓存))return;
            String downId = intent.getStringExtra("downId");
            String downState = intent.getStringExtra("downState");
            String downProgress = intent.getStringExtra("downProgress");
            if (downId == null || downState == null || downProgress == null){
                return;
            }
            设置状态(downId,downState,downProgress);
        }
    }
    private MsgReceiver msgReceiver;
    public void resume(){
        if (mType.equals(Qe.RECORDTYPE_正在缓存) && !isOpenReceiver){
            监听下载广播();
        }
    }
    public void pause(){
        if (isOpenReceiver){
            getContext().unregisterReceiver(msgReceiver);
        }

    }



}
