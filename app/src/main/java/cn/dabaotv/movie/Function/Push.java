package cn.dabaotv.movie.Function;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.hpplay.bean.CastDeviceInfo;
import com.hpplay.callback.CastDeviceServiceCallback;
import com.hpplay.callback.ExecuteResultCallBack;
import com.hpplay.callback.TransportCallBack;
import com.hpplay.link.HpplayLinkControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H19 on 2018/5/12 0012.
 */

public class Push implements TransportCallBack,ExecuteResultCallBack {
    private Push mPush;
    private Context mContext;
    private String mv_url;
    private HpplayLinkControl mHpplayLinkControl;

    public Push push(Context context){
        if (mPush == null){
            mPush = new Push();
            mPush.mContext = context;
            mPush.mHpplayLinkControl = HpplayLinkControl.getInstance();
            mPush.mHpplayLinkControl.initHpplayLink(context,"b83132dbf005403898eb054c1ab243fa");//(context, "d6e00454760f767acc8a8c466218fa5f");
            //mPush.mHpplayLinkControl.setDebug(true);
            //mPush.mHpplayLinkControl.setTransportCallBack(this);
        }
        return mPush;
    }

    private long mPrevCastStopTimeStamp = 0;

    private List<CastDeviceInfo> mDeviceList = new ArrayList<>();
    private int mCurConnDeviceIndex;
    public void start(String url){
        mv_url = url;
        if (mDeviceList.size() > 0){
            showList();
            return;
        }else {
            if (loading == false){
                loading = true;
                mdns_search();
            }else {
                mHandler.sendEmptyMessage(-5);
            }
        }
    }
    private boolean loading = false;
    public void mdns_search(){
        if((0 != mPrevCastStopTimeStamp) && (System.currentTimeMillis() - mPrevCastStopTimeStamp) < 3000) {
            Toast.makeText(mContext,"投屏点击太频繁,请稍后重试", Toast.LENGTH_SHORT).show();
            loading = false;
            return;
        }
        showLoading(mContext,"投屏","检测可用投屏设备");
        mDeviceList.clear();
        mPrevCastStopTimeStamp = System.currentTimeMillis();
        mHpplayLinkControl.castServiceDiscovery(mContext, new CastDeviceServiceCallback() {
            @Override
            public void onNoneCastDeviceService() {
                mHpplayLinkControl.castServiceStopDiscovery();
                mHandler.sendEmptyMessage(34);
                if (push_dialog.isShowing()) push_dialog.dismiss();
                loading = false;
            }

            @Override
            public void onCastDeviceServiceAvailable(List<CastDeviceInfo> list) {
                loading = false;
                if(list.size() <= 0) {
                    // 检测列表失败
                    mHandler.sendEmptyMessage(-1);
                    return;
                }
                mDeviceList = list;
                if (push_dialog.isShowing()) push_dialog.dismiss();
                mHandler.sendEmptyMessage(0);
                showList();

            }

        });
    }
    public boolean mdns_connect(int index){
        //这个是需要连接的设备名称;
        if (index < mDeviceList.size()){
            mCurConnDeviceIndex = index;
            mHpplayLinkControl.castConnectDevice(mDeviceList.get(index),null);
            mHpplayLinkControl.castServiceStopDiscovery();
            mHandler.sendEmptyMessage(35);
            return true;
        }else {
            return false;
        }
    }

    public Push disconnect() {
        //退出整个通信并释放
        mHpplayLinkControl.castDisconnectDevice();
        return mPush;
    }
    private ProgressDialog push_dialog;
    private onListener listener;
    public void setListener(onListener listener){
        this.listener = listener;
    }

    @Override
    public void onResultDate(Object o, int i) {
        Toast.makeText(mContext, (String)((boolean)o ? "成功":"失败"), Toast.LENGTH_SHORT).show();
    }

    public interface onListener{
        void onSerechEnd(int state, String msg);
    }
    public void showLoading(Context context, String title, String msg){
        push_dialog = new ProgressDialog(context);
        push_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        push_dialog.setTitle(title);
        push_dialog.setMessage(msg);
        push_dialog.setIndeterminate(false); // 设置ProgressDialog 的进度条是否不明确 false 就是不设置为不明确
        push_dialog.setCancelable(true); // 设置ProgressDialog 是否可以按退回键取消
        push_dialog.setButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }); // 设置ProgressDialog 的一个Button
        push_dialog.show();
    }
    public void showList() {
        final String items[] = new String[mDeviceList.size()];
        for (int i = 0; i < mDeviceList.size(); i++) {
            items[i] = mDeviceList.get(i).getHpplayLinkName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,3);
        builder.setTitle("选择投屏设备");
        // builder.setMessage("是否确认退出?"); //设置内容
        /*builder.setIcon(R.mipmap.ic_launcher);*/
        // 设置列表显示，注意设置了列表显示就不要设置builder.setMessage()了，否则列表不起作用。
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mCurConnDeviceIndex = which;
                mHpplayLinkControl.castConnectDevice(mDeviceList.get(mCurConnDeviceIndex),null);
                mHpplayLinkControl.castServiceStopDiscovery();
                mHandler.sendEmptyMessage(35);

                pushVideo();
            }
        });
        builder.create().show();
    }

    private void pushVideo(){
        mHpplayLinkControl.castStartMediaPlay(this, 10, mv_url,HpplayLinkControl.PUSH_VIDEO,13);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    listener.onSerechEnd(0,"");
                    break;
                case 34 :
                    listener.onSerechEnd(-1,"未发现投屏设备，请开启后重新搜索");
                    break;
                case 35 :
                    if(null != mDeviceList.get(mCurConnDeviceIndex).getHpplayLinkName()) {
                        listener.onSerechEnd(-1,"已投屏到设备" + mDeviceList.get(mCurConnDeviceIndex).getHpplayLinkName());
                    } else {
                        listener.onSerechEnd(-1,"已投屏到设备");
                    }
                    break;
                case -1:
                    listener.onSerechEnd(-1,"未发现投屏设备");
                    break;
                case -5:
                    listener.onSerechEnd(-1,"获取设备中");
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onTransportData(Object o) {
        String s = (String) o;
        Log.e("eeeee","------"+s);
    }
}
