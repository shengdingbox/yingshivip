package cn.dabaotv.movie.Download;

import android.app.Activity;

import cn.dabaotv.movie.Q.Qe;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.m.cn.文件;
import cn.m.cn.文本;

/**
 * Created by H19 on 2018/6/3 0003.
 */

public class QM3u8 {
    private Activity aty;
    private String fileDir;
    private String fileName;

    private int cutTsIndex; // 当前执行下载的 ts
    private int tsTotal; //  ts seze
    private List<m3u8t> tsList = new ArrayList<>();
    private class m3u8t{
        String url;
        int size;
        float time;
        int state;
        public m3u8t(){}
        public m3u8t(String url,float time){
            this.url = url;
            this.time = time;
        }
    }

    private String fsUrl;
    private String fsPath;// 本地绝对路径
    private int fsDownId; // 下载 m3u8文件的 ID

    public QM3u8 inin(Activity activity,String url,OnListener listener){
        this.aty = activity;
        this.fsUrl = url;
        this.mListener = listener;
        return this;
    }
    public void setFileInfo(String dir,String name){
        this.fileDir = dir;
        this.fileName = name;
        文件.创建目录(aty,fileDir+fileName);
    }
    public void start(){
        if (fsPath == null){
            fsPath = fileDir + fileName + "/fileu3u8.txt";
        }
        if (文件.是否存在(fsPath)){
            ininTsList();
            if (tsList.size() == 0){
                ininTsList2();
            }

            if (tsList.size() == 1 && getSuffix(tsList.get(0).url).equals("m3u8")){
                fsUrl = getUrl(tsList.get(0).url,fsUrl);
                fsDownId = startDown(fsUrl,fileDir + fileName, "/fileu3u8.txt");
                return;
            }

            if (tsList.size() >= 1){
                mListener.回调(0, Qe.DownState_Ing,"缓存中");
                cutTsIndex = 0;
                fsDownId = -1; // 避免冲突
                start2();
            }else {
                mListener.回调(0,Qe.DownState_Error,"获取视频失败");
            }
        }else {
            mListener.回调(0,Qe.DownState_Ing,"获取视频");
            fsDownId = startDown(fsUrl,fileDir + fileName, "/fileu3u8.txt");
        }
    }
    public void start2(){
        String dir = fileDir + fileName;
        String name = "/"+cutTsIndex +".temp";
        String url = getUrl(tsList.get(cutTsIndex).url,fsUrl);
        startDown(url,dir,name);
    }

    private void 回调(int id,int state){
        if (id == fsDownId){
            if (state == Qe.DownState_Complete){
                start();
            }else if (state == Qe.DownState_Error || state == Qe.DownState_Error2){
            }
            return;
        }else {
            switch (state){
                case Qe.DownState_Complete:
                    if (cutTsIndex >= tsTotal){
                        合成();
                    }else {
                        float cc = (float)cutTsIndex / (float)tsTotal;
                        mListener.进度改变(0,(int)(1000 * cc));
                        cutTsIndex++;
                        start2();
                    }

                    break;

            }

        }
    }
    private void 合成(){
        mListener.回调(0,Qe.DownState_Complete,"");
    }
    private void 进度改变(int id,long size){

    }

    private int startDown(String downUrl,String dir,String name){
        DownloadRequest request = PRDownloader.download(downUrl,dir,name).build();
        final int downId = request.getDownloadId();
        request.setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                        回调(downId,Qe.DownState_Pause);
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        回调(downId,Qe.DownState_Cancel);
                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        进度改变(downId,progress.currentBytes);
                    }
                })
                .start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                回调(downId,Qe.DownState_Complete);
            }

            @Override
            public void onError(Error error) {
                回调(downId,Qe.DownState_Error);
            }
        });
        return downId;
    }
    private  void ininTsList(){
        tsList.clear();
        String code = 文件.读取文本内容(aty,fsPath);
        int i1;
        float time = 0;
        String tt = "";
        assert code != null;
        String t1 = code.replaceAll(",\n",",");
        String[] lines = t1.split("\n");

        if (code.contains("##FSURL:###")){
            fsUrl = 文本.get中间文本(code,"##FSURL:###","#####");
        }else {
            String urlxx = "##FSURL:###" + fsUrl + "######";
            code = code + "\n\n" + urlxx;
            文件.写出文本文件(aty,fileDir+fileName,"/fileu3u8.txt",code);
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.startsWith("#")) {
                if (line.startsWith("#EXTINF:")) {
                    line = line.substring(8);
                    if ((i1 = line.indexOf(",")) != -1) {
                        try {
                            tt = line.substring(0, i1);
                            time = Float.parseFloat(tt);
                        } catch (Exception e) {
                            time = 0;
                        }
                        line = line.substring(i1 + 1,line.length());
                        if (line.length() < 10 && i < lines.length){
                            line = lines[i+1];
                        }

                        if (line.length() > 1){
                            tsList.add(new m3u8t(line,time));
                        }
                    }
                }
                continue;
            }
        }


        tsTotal = tsList.size();
    }
    private void ininTsList2(){
        tsList.clear();
        String t1 = 文件.读取文本内容(aty,fsPath);
        int i1;
        float time = 0;
        String tt = "";
        t1.replaceAll(",\n",",");
        String[] lines = t1.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String t2 = line;
            if (line.startsWith("#")) {
                if (line.startsWith("#EXT-X-STREAM-INF:")) {
                    line = lines[i+1];
                    if (line.length() > 1){
                        tsList.add(new m3u8t(line,time));
                    }
                }
                continue;
            }
        }
        tsTotal = tsList.size();
    }

    // 获取绝对链接
    private String getUrl(String conl,String urls){
        if (conl.length() > 4 && conl.substring(0,4).equals("http")){
            return conl;
        }
        String t1 = urls;
        // 先删除 url ？ 后面的数据
        int pisition = t1.indexOf("?");
        if (pisition!=-1){
            t1 = 文本.get文本左边(t1,pisition);
        }

        t1 = 文本.get文本左边(t1,t1.lastIndexOf("/"));

        if (!文本.get文本右边(t1,1).equals("/") && !文本.get文本左边(conl,1).equals("/")){
            t1 = t1 + "/" + conl;
        }else {
            t1 = t1 + conl;
        }
        return t1;
    }
    private String getSuffix(String urls){
        String t1 = urls;
        // 先删除 url ？ 后面的数据
        int pisition = t1.indexOf("?");
        if (pisition!=-1){
            t1 = 文本.get文本左边(t1,pisition);
        }
        t1 = 文本.get文本右边(t1,".");
        return t1;
    }

    public void MergeVideos(String source, String savaFile, int num) throws IOException {
        FileOutputStream out = new FileOutputStream(savaFile);
        FileInputStream in = null;
        for(int i = 0 ; i < num; i++){
            String videoPath = source + "test-" + i + ".tmp";
            File file = new File(videoPath);
            in = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            while((len = in.read(bytes)) > 0){
                out.write(bytes,0,len);
            }
        }
        in.close();
        out.close();
    }
    public static InputStream getStringStream(String sInputString){
        if (sInputString != null && !sInputString.trim().equals("")){
            try{
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            }catch (Exception ex){ex.printStackTrace();}}return null;
    }


    private OnListener mListener;
    public interface OnListener{
        void 回调(int id,int state,String msg);
        void 进度改变(int id,long size);
    }


}
