package cn.m.bdplayer;


import android.content.Context;
import android.util.Log;

import com.baidu.cloud.media.download.DownloadObserver;
import com.baidu.cloud.media.download.DownloadableVideoItem;
import com.baidu.cloud.media.download.VideoDownloadManager;

/**
 * 百度播放器下载
 */

public class VideoDownload{

    private Context ctx;
    public VideoDownload inin(Context ctx, DownListener listener){
        this.ctx = ctx;
        this.mDownListener = listener;
        return this;
    }
    public void start(String downUrl,String userName){

        VideoDownloadManager globalVideoDownloadManager = VideoDownloadManager.getInstance(ctx, userName); // 获得下载管理单例。
        globalVideoDownloadManager.startOrResumeDownloader(downUrl, new DownloadObserver() {
            @Override
            public void update(DownloadableVideoItem downloadableVideoItem) {

                int state = downloadableVideoItem.getStatus().getCode();
                int progress = (int)downloadableVideoItem.getProgress();
                String path = downloadableVideoItem.getLocalAbsolutePath();
                path = downloadableVideoItem.getStatus().getMessage();
                if (state == 4){
                    Log.i("qlog-err",downloadableVideoItem.getErrorCode() + "  ");
                }
                mDownListener.进度改变(state,progress,path);
            }
        }); // 启动下载任务。
    }


    private DownListener mDownListener;
    public interface DownListener {
        void 进度改变(int state, int progress, String path);//`通知当前进度
    }


}
