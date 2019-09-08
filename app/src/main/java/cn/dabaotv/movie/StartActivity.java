package cn.dabaotv.movie;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.utils.BaseActivity;
import cn.dabaotv.movie.utils.CountdownView;
import cn.dabaotv.movie.utils.UpVersion;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.gson.Gson;
import cn.dabaotv.video.R;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.Date;

import cn.m.cn.文件;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static cn.m.cn.系统.hideNavigationBar;

public class StartActivity extends BaseActivity implements View.OnClickListener {


    public static final String KEY_SHARE = "KEY_SHARE";
    public static final String SHARE_OK = "SHARE_OK";
    public static final String SHARE_LOSE = "SHARE_LOSE";
    public static final String KEY_FREE = "KEY_FREE";
    private CountdownView countdownView;
    private static final String path = "/sdcard/key.txt";
    private UpVersion upVersion = null;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (upVersion == null) return;
                AlertDialog dialog = new AlertDialog.Builder(StartActivity.this)
                        .setTitle("版本升级")
                        //.setMessage(upVersion.getUp_info())//设置对话框的内容
                        //设置对话框的按钮
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                handler.sendEmptyMessage(2);
                            }
                        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                            }
                        }).create();
                dialog.show();
            }else {
                downLoad(upVersion);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ScreenUtils.isPortrait()) {
            ScreenUtils.adaptScreen4HorizontalSlide(StartActivity.this, 1080);
        }
        setContentView(R.layout.activity_start);
        hideNavigationBar(getWindow());
        requestPermission();
//以下为修复更新把注释解开
       File file;
        if (FileUtils.isFileExists(path)) {
            file = FileUtils.getFileByPath(path);
            isOK(file);
        } else {
            if (FileUtils.createOrExistsFile(path)) {
                file = FileUtils.getFileByPath(path);
                FileIOUtils.writeFileFromString(file, TimeUtils.getNowString());
                isOK(file);
            }
        }
    }
//结束
    private void init() {
        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        countdownView = findViewById(R.id.countdownView);
        countdownView.setOnClickListener(this);
        countdownView.setTime(3);
        countdownView.setTransaction(new CountdownView.Transaction() {
            @Override
            public void finished() {
                if (!StartActivity.this.isFinishing()) {
                    startActivity(intent);
                    ScreenUtils.cancelAdaptScreen();
                    finish();
                }
            }

            @Override
            public void stop() {
                if (!StartActivity.this.isFinishing()) {
                    startActivity(intent);
                    ScreenUtils.cancelAdaptScreen();
                    finish();
                }
            }
        });


        final String url = QConfig.UP;
        io.reactivex.Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                final String code = Ghttp.getHttp(url);
                emitter.onNext(code);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String code) throws Exception {
                        if (code != null && code.length() > 5) {
                            LogUtils.d(code);
                            try {
                                upVersion = new Gson().fromJson(code, UpVersion.class);
                            }catch (Exception e){
                                upVersion = null;
                            }
                            if (upVersion != null && upVersion.getUp_version() > AppUtils.getAppVersionCode()) {
                                handler.sendEmptyMessage(1);
                            } else {
                                countdownView.start();
                            }
                        } else {
                            LogUtils.d("错误");
                            countdownView.start();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.d("错误", throwable);
                        countdownView.start();
                    }
                });


        MyApplication.refreshUserData();
    }

    private void requestPermission() {
        new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)//这里填写所需要的权限
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            文件.创建目录(StartActivity.this, QConfig.SavaPath);
                            init();
                        } else if (permission.shouldShowRequestPermissionRationale) {
                            AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this)
                                    .setTitle("提示")
                                    .setMessage("软件需要写读文件权限，否则无法正常操作")
                                    .setCancelable(false)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermission();
                                        }
                                    }).create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();
                        } else {
                            ToastUtils.showShort("软件没有读写权限无法操作，请在权限界面打开读写权限后再试！");
                            init();
                        }
                    }

                });
    }

    private void isOK(File file) {
        String s = FileIOUtils.readFile2String(file);
        Date date1 = TimeUtils.string2Date(s);
        Date date2 = TimeUtils.getNowDate();
        long l = TimeUtils.getTimeSpan(date2, date1, TimeConstants.DAY);
        if (l < 1) {
            MyApplication.user_state = Qe.会员_状态_正常;
        } else {
            LogUtils.d("会员_状态不正常");
            //MyApplication.user_state = Qe.会员_状态_已过期;
        }
    }


    private void downLoad(final UpVersion upVersion) {
       final ProgressDialog progressDialog = new ProgressDialog(StartActivity.this);
        progressDialog.setTitle("下载安装中");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        new Thread() {
            @Override
            public void run() {
                super.run();
                int downloadId = PRDownloader.download(upVersion.getUp_url(), "/sdcard/", "new_dabaotv.apk")
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {
                                progressDialog.show();
                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {

                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(Progress progress) {
                                progressDialog.setProgress((int) (((float)progress.currentBytes / progress.totalBytes) * 100));
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                AppUtils.installApp(FileUtils.getFileByPath("/sdcard/new_dabaotv.apk"));
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onError(Error error) {
                                LogUtils.d(error);
                            }
                        });
            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    protected boolean setFitsSystemWindows() {
        return true;
    }

    @Override
    public void onClick(View v) {
        countdownView.stop();
    }

    @Override
    public void onBackPressed() {
        //
        this.recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}
