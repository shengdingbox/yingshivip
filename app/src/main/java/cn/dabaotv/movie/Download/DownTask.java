package cn.dabaotv.movie.Download;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by H19 on 2018/4/24 0024.
 */

public class DownTask extends AsyncTask<String, Integer, Integer> {

    public String 保存目录 = "";

    //定义四个下载状态常量
    public static final int 下载成功 = 0;//下载成功
    public static final int 下载失败 = 1;//下载失败
    public static final int 下载暂停 = 2;//下载暂停
    public static final int 下载取消 = 3;//下载取消

    private int 下载状态;

    //在构造函数中通过这个参数回调
    private MDown.DownListener downloadListener;

    private boolean isCanceled = false;
    private boolean isPaused = false;
    private int lastProgress;
    private String msg = "";

    public DownTask(MDown.DownListener Listener) {
        this.downloadListener = Listener;
    }
    //后台执行具体的下载逻辑
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        long downloadedLength = 0;//记录下载文件的长度
        if (strings.length < 2) return 下载失败;
        String downloadUrl = strings[0];
        String directory = strings[1];
        String fileName = strings[2];
        file = new File(directory + fileName);
        if (file.exists()) downloadedLength = file.length();
        long contentLength = 0;
        try {
            contentLength = getContentLength(downloadUrl);
        }catch (Exception e) {
            e.printStackTrace();
            Log.i("mdown-err e1",e.toString());
        }

        if (contentLength == 0) {
            msg = "链接文件失败";
            return 下载失败;// 链接文件失败
        } else if (contentLength == downloadedLength) {
            return 下载成功;//已下载字节和总文件字节长度相等，则下载成功
        }

        try {
            OkHttpClient client = new OkHttpClient();//断点下载，指定从哪个字节开始上一次的下载
            Request request = null;
            Response response = null;
            request = new Request.Builder().addHeader("RANGE", "bytes = " + downloadedLength + "-").url(downloadUrl).build();
            response = client.newCall(request).execute();
            if (response != null) {
                is = response.body().byteStream();
                try {
                    savedFile = new RandomAccessFile(file, "rw");
                }catch (Exception e){
                    Log.i("downtask-",e.toString());
                    msg = "没有权限";
                    return 下载失败;
                }
                savedFile.seek(downloadedLength);//跳过已下载字节
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1) {
                    if (isCanceled) {
                        return 下载取消;
                    } else if (isPaused) {
                        return 下载暂停;
                    } else {
                        total += len;
                        savedFile.write(b, 0, len);//计算已下载的百分比
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return 下载成功;
            }
        } catch (IOException e) {
            e.printStackTrace();
            msg = "链接文件失败";
            return 下载失败;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("mdown-err e2",e.toString());
            }
        }

        msg = "未知错误";
        return 下载失败;
    }

    //在界面上更新当前的下载进度
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            //回调方法中的onProgress
            downloadListener.进度改变(progress);
            lastProgress = progress;
        }
    }

    //通知最终的下载结果
    //用的listener来回调方法。
    @Override
    protected void onPostExecute(Integer status) {
        下载状态 = status;
        switch (status) {
            case 下载成功:
                downloadListener.下载成功();
                break;
            case 下载失败:
                downloadListener.下载失败(msg);
                break;
            case 下载暂停:
                downloadListener.下载暂停();
                break;
            case 下载取消:
                downloadListener.下载取消();
                break;
            default:
                break;
        }
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }
    public int 取状态(){
        return 下载状态;
    }

    private long getContentLength(String downloadUrl) throws IOException {
        long contentLength = 0;
        OkHttpClient client = new OkHttpClient();
        try {
            Request request = new Request.Builder().url(downloadUrl).build();
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                contentLength = response.body().contentLength();
                response.close();
            }
        }catch (Exception e){
            contentLength = 0;
        }
        return contentLength;
    }
}