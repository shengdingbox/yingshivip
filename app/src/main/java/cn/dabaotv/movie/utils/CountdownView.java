package cn.dabaotv.movie.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;

import cn.dabaotv.video.R;


/**
 * Created by 奈蜇 on 2018-07-20.
 * 倒计时View
 */
public class CountdownView extends AppCompatTextView {
    private static final String TAG = "CountdownView";
    private MyAsyncTask myAsyncTask;
    private int time = 3;//默认计数
    private String defaultStr = "跳过";//默认显示的字符串
    private Transaction transaction;//额外的事务
    private boolean IsStop = false;

    public CountdownView(Context context) {
        super(context);
        init(context, null);
    }

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (attrs != null) {
            TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.CountdownView);// 获取配置属性
            time = tArray.getInt(R.styleable.CountdownView_time, 3);
            if(tArray.getString(R.styleable.CountdownView_defaultStr) != null) defaultStr = tArray.getString(R.styleable.CountdownView_defaultStr);
            tArray.recycle();
        }
        setVisibility(GONE);
        setTextColor(Color.WHITE);
        setBackgroundResource(R.drawable.countdownview_bg);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        stop();
        return super.onSaveInstanceState();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    public boolean start() {
        if(!IsStop){
            if (myAsyncTask == null || myAsyncTask.getStatus() == AsyncTask.Status.FINISHED) {
                myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute(time);
                return true;
            }
        }else {
            Log.d(TAG,"线程停止中，请稍后启动！");
        }
        return false;
    }
    @SuppressWarnings("all")
    public boolean stop() {
        if (myAsyncTask != null && myAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            setVisibility(GONE);
            IsStop = true;
            return true;
        }
        return false;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getDefaultStr() {
        return defaultStr;
    }

    public void setDefaultStr(String defaultStr) {
        this.defaultStr = defaultStr;
    }

    @SuppressLint("StaticFieldLeak")
    class MyAsyncTask extends AsyncTask<Integer, Integer, Void> {
        //更新方法体（在UI线程中）
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            setVisibility(VISIBLE);
            setText(defaultStr.concat(String.valueOf(values[0])));
        }
        //执行方法体
        @Override
        protected Void doInBackground(Integer... integers) {
            if (!myAsyncTask.isCancelled()) {
                //使用for循环来模拟进度条的进度.
                for (int i = integers[0]; i >= 0 && !myAsyncTask.isCancelled(); i--) {
                    //调用publishProgress方法将自动触发onProgressUpdate方法来进行进度条的更新.
                    publishProgress(i);
                    Log.d(TAG,"publishProgress:"+i);
                    try {
                        //通过线程休眠模拟耗时操作
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    whetherStop();
                }
            }
            return null;
        }
        //完成回调
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (transaction != null) {
                transaction.finished();
                setVisibility(GONE);
            }
        }
        //cancel 回调
        @Override
        protected void onCancelled() {
            super.onCancelled();
            transaction.stop();
        }
    }
    //是否停止计数（不是暂停）,这个是为了解决
    // System.err: java.lang.InterruptedException
    // at java.lang.Thread.sleep(Native Method)
    //子线程在sleep期间主线程被终止了.
    //强行在sleep完毕后终止主线程！
    private void whetherStop() {
        if (IsStop && myAsyncTask != null && myAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            if (myAsyncTask.cancel(true)) {
                Log.d(TAG, "停止成功!");
            } else {
                Log.d(TAG, "停止失败!");
            }
        }
        IsStop = false;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    //最终方法交给使用者定义
    public interface Transaction {
        //计数完成回调
        void finished();
        //计数停止回调
        void stop();
    }
}
