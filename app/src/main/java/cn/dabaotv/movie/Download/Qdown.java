package cn.dabaotv.movie.Download;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import cn.dabaotv.movie.DB.DBDown;
import cn.dabaotv.movie.Function.Jiexi;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.m.bdplayer.VideoDownload;
import cn.m.cn.Function;

/**
 * Created by H19 on 2018/9/5 0005.
 */

public class Qdown implements QConfig {

    private String dirPath = SavaPath;
    private Context ctx;
    private DownData mData = new DownData();
    private int downId = -1;
    private DBDown cutDownItem;

    public Qdown inin(Context ctx){
        this.ctx = ctx;
        return this;
    }
    private List<DBDown> mDownList = new ArrayList<>();
    public void refreshDown(){
        mDownList = DataSupport.findAll(DBDown.class);
        if (cutDownItem == null){
            for (DBDown dbDown : mDownList) {
                if (dbDown.getState() == Qe.DownState_Ing){
                    start(dbDown);
                    return;
                }else if (dbDown.getState() == Qe.DOWNSTATE_N && cutDownItem == null){
                    cutDownItem = dbDown; //
                }
            }
            if (cutDownItem != null){
                start(cutDownItem);
            }
        }else {
            Q.log(cutDownItem.getName());
        }
    }


    public void start(DBDown dbDown){
        cutDownItem = dbDown;
        Q.log("start:",cutDownItem.getName() + cutDownItem.getIndex());

        // 判断是否有视频 没有接解析
        if (dbDown.getDownUrl() == null || dbDown.getDownUrl().isEmpty()){
            // 解析获取视频再来
            mData.state = DownState.Parse;
            mData.msg = "读取视频地址";
            //paresPager(cutDownItem.getCodeUrl());
            startDown1(Function.getMD5(cutDownItem.getUrl() + "_" + cutDownItem.getName()));
        }else {
            String name = cutDownItem.getDownName();
            if (name == null || name.isEmpty()){
                name = Function.getMD5(cutDownItem.getUrl() + "_" + cutDownItem.getName());
                cutDownItem.setDownName(name);
                cutDownItem.save();
            }
            if (cutDownItem.getDownType() == 2){
                startDown1(name);
            }else {
                Q.log("mp4");
                startDown1(name);
                //startDown2(cutDownItem.getDownUrl(),name);
            }
            //startDown(Function.getMD5(cutDownItem.getUrl() + "_" + cutDownItem.getName()));

        }
    }
    private void paresPager(String parseUrl){
        Jiexi mJiexi = new Jiexi().inin((Activity) ctx, new Jiexi.OnListener() {
            @Override
            public void ent(Jiexi t, int errId, final String msg,String type) {
                if (errId == 0){
                    cutDownItem.setDownUrl(msg);
                    if (type.equals("m3u8")){
                        cutDownItem.setDownType(2);
                    }else {
                        cutDownItem.setDownType(1);

                    }
                    cutDownItem.save();
                    start(cutDownItem);
                }else {
                    mData.state = DownState.Error2;
                    out();
                }
            }
            public void ent(String url){

            }
        });
        mJiexi.start(parseUrl);
    }
    private void startDown(String fileName){
        String downUrl = cutDownItem.getDownUrl();
        int downType = cutDownItem.getDownType();
        MDown mDown = new MDown().down((Activity) ctx,downUrl,downType);
        mDown.setDir(dirPath,fileName);
        mDown.setListener(new MDown.DownListener() {
            @Override
            public void 进度改变(int progress) {
                mData.progress = progress;
                out();
            }

            @Override
            public void 下载成功() {
                mData.state = DownState.Complete;
                out();
            }

            @Override
            public void 下载失败(String t) {
                Q.log("erre",t);
                mData.state = DownState.Error;
                out();
            }

            @Override
            public void 下载暂停() {
                mData.state = DownState.Pause;
                out();
            }

            @Override
            public void 下载取消() {
                mData.state = DownState.Cancel;
                out();
            }
        });
        mData.state = DownState.Ing;

        Q.log("start-down" + downType,downUrl);
        mDown.start();
    }
    private void startDown1(String fileName){
        // 先创建目录
        String downurl = "http://us1.wl-cdn.com/hls/20190827/17a5f7fc934240d9e1c39a5c9cbe8cc4/1566846970/index.m3u8";
        Q.log("downdd",downurl);
        //cutDownItem.getDownUrl()
        QM3u8 dTs = new QM3u8().inin((Activity) ctx, downurl, new QM3u8.OnListener() {
            @Override
            public void 回调(int id, int state,String msg) {
                mData.state = state;
                out();
            }

            @Override
            public void 进度改变(int id, long size) {
                mData.progress = size;
                out();
            }
        });
        dTs.setFileInfo(dirPath,fileName);
        dTs.start();

    }
    private int startDown2(String downUrl,String fileName){
        downId = PRDownloader.download(downUrl,dirPath,fileName)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        mData.state = DownState.Ing;
                        out();
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        mData.state = DownState.Pause;
                        out();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        mData.state = DownState.Cancel;
                        out();
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        mData.progress = progress.currentBytes;
                        out();
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        mData.state = DownState.Complete;
                        out();
                    }

                    @Override
                    public void onError(Error error) {
                        Q.log("error1",error.isConnectionError());
                        Q.log("error1",error.isServerError());
                        mData.state = DownState.Error;
                        out();
                    }
                });

        return downId;
    }
    private void startDown3(String fileName){
        VideoDownload videodown = new VideoDownload().inin(ctx, new VideoDownload.DownListener() {
            @Override
            public void 进度改变(int state, int progress, String path) {
                Q.log("state-dd",state);
                Q.log("path-dd",path);
                mData.state = progress;
                //out();
            }
        });
        Q.log("cutdownite",cutDownItem.getDownUrl());
        videodown.start(cutDownItem.getDownUrl(),fileName);


    }
    private void out(){
        Q.log("stateCahgnge");
        if (cutDownItem != null && mData.state!=cutDownItem.getState()){
            cutDownItem.setState(mData.state);
            cutDownItem.save();
        }
        if (mData.state == 5){
            cutDownItem.setDownUrl("");
            cutDownItem.save();
        }


        if (mData.state > 1){
            cutDownItem = null;
            new android.os.Handler().postAtTime(new Runnable() {
                @Override
                public void run() {
                    refreshDown();
                }
            },1000);
        }


        if (mListener != null){
            mListener.change(mData);
        }

        发送状态广播();
    }
    private void 发送状态广播(){
        Intent intent = new Intent("qiju.down");
        intent.putExtra("downId",toT(cutDownItem.getId()));
        intent.putExtra("downState",toT(mData.state));
        intent.putExtra("downProgress",Long.toString(mData.progress));
        ctx.sendBroadcast(intent);
    }

    private Onlistener mListener;
    public void setListener(Onlistener listener){
        mListener = listener;
    }
    public interface Onlistener{
        void change(DownData downData);
    }
    private class DownData{
        long progress;
        int state;
        String msg;
    }
    public interface DownState{
        int Ing = 1; // 进行中
        int Cancel = 2; // 取消
        int Complete = 3; // 完成
        int Pause = 4; // 暂停
        int Error = 5; // 错误
        int Parse = 6; // 解析 读取真实地址
        int Error2 = 7; // 解析失败
    }
    private String toT(int i){
        return Integer.toString(i);
    }


}
