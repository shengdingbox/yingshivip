package cn.dabaotv.movie.Download;

import android.app.Activity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.m.cn.文件;
import cn.m.cn.权限;

/**
 * Created by H19 on 2018/6/3 0003.
 */

public class M3U8Down {
    private DownTask mTask;
    private int Down位置;
    private String Down地址;
    private String sava路径;
    private String savaFilename;
    private int 重试次数 = 0;

    private Activity mActivity;

    private String con;
    private List<m3u8t> list = new ArrayList<>();
    private MDown.DownListener mListener;
    private MDown.DownListener mDownListener = new MDown.DownListener() {
        @Override
        public void 进度改变(int progress) {
        }

        @Override
        public void 下载成功() {
            Down位置 = Down位置 + 1;
            重试次数 = 0;
            tian();
        }

        @Override
        public void 下载失败(String t) {
            if (t.equals("链接文件失败")){
                重试次数 ++ ;
                if (重试次数 > 5){
                    mListener.下载失败("链接超时");
                    return;
                }
            }
            mListener.下载失败(t);
        }

        @Override
        public void 下载暂停() {
            mListener.下载暂停();
        }

        @Override
        public void 下载取消() {
            mListener.下载取消();
        }
    };
    public M3U8Down New(Activity activity, String con){
        this.con = con;
        this.mActivity = activity;
        return this;
    }
    public void setDownUrl(String url){
        this.Down地址 = url;
        Down地址 = url.substring(0,url.lastIndexOf("/") + 1);
    }
    public void setDir(String dir){
        sava路径 = dir;
    }
    public void setFileName(String name){
        savaFilename = name;
    }
    private void 加载列表(){
        String t1 = con;
        int i1;
        float time = 0;
        String tt = "";
        t1.replaceAll(",\n",",");
        String[] lines = t1.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String t2 = line;
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
                            list.add(new m3u8t(line,time));
                        }

                    }
                }
                continue;
            }
        }
    }
    public List<m3u8t> getList(){
        return list;
    }
    public void 开始下载(){
        加载列表();
        Down位置 = 0;
        tian();
    }
    public void tian(){

        if (is暂停 == true){return;}
        if (list.size() == 0){
            mListener.下载失败("列表为空");
            return;
        }

        if (Down位置 >= list.size()){
            // 合成视频
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        MergeVideos(sava路径,savaFilename + ".ts",list.size());
                        mListener.下载成功();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mListener.下载失败("合成视频失败");
                    }
                }
            }).start();
            return;
        }

        mTask = null;
        mTask = new DownTask(mDownListener);
        String url = list.get(Down位置).url;
        if (url.substring(4).equals("http") == false){
            url = Down地址 + url;
        }

        权限.申请文件操作权限(mActivity);
        boolean is = 文件.是否存在(sava路径 + "test-" + Down位置 + ".tmp");
        if (is == true) is = 文件.是否存在(sava路径 + "test-" + (Down位置 + 1) + ".tmp");
        if (is == false){
            mTask.execute(url,sava路径,"test-" + Down位置 + ".tmp");
        }else {
            Down位置 = Down位置 + 1;
            tian();
        }
        double length = (double)Down位置 / (double)list.size() * 100D;
        long ii = (long)length;
        mListener.进度改变((int) ii);

    }

    private boolean is暂停 = false;
    public void 暂停(){
        is暂停 = true;
    }
    public void 继续(){
        is暂停 = false;
        tian();
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
    public class m3u8t{
        String url;
        int size;
        float time;
        int state;
        private m3u8t(){}
        private m3u8t(String url,float time){
            this.url = url;
            this.time = time;
        }
    }
    public static InputStream getStringStream(String sInputString){
        if (sInputString != null && !sInputString.trim().equals("")){
            try{
                ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
                return tInputStringStream;
            }catch (Exception ex){ex.printStackTrace();}}return null;
    }

    public void setListener(MDown.DownListener listener){
        this.mListener = listener;
    }
}
