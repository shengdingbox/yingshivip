package cn.dabaotv.movie;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.telephony.TelephonyManager;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import cn.dabaotv.movie.Download.Qdown;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.gson.Gson;
import cn.dabaotv.movie.DB.DBDown;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.movie.main.my.MyView;
import cn.dabaotv.movie.view.list.ItemList;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.smtt.sdk.QbSdk;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by H19 on 2018/5/6 0006.
 */

public class MyApplication extends Application implements QConfig {
    public static int user_state;
    public static String user_user;
    public static String IMEI;

    private static final String path = "/sdcard/key.txt";

    private static Context ctx;
    public static Activity aty;


    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(this);
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            public Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                          String errorMessage, String errorStack) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("Key", "Value");
                return map;
            }

            @Override
            public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                           String errorMessage, String errorStack) {
                try {
                    return "Extra data.".getBytes("UTF-8");
                } catch (Exception e) {
                    return null;
                }
            }

        });
        CrashReport.initCrashReport(getApplicationContext(), "c787cdf84b", true,strategy);
//        LLCrashHandler.getInstance().init(this);
        ctx = getApplicationContext();
        LitePal.initialize(ctx);
        ininDownloader(); // 加载下载器
        initX5WebView();
         //addVipTime(ctx);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void showShare(final Context ctx, String text) {
//        MobSDK.init(ctx);
//        OnekeyShare oks = new OnekeyShare();
//        //关闭sso授权
//        oks.disableSSOWhenAuthorize();
//        // title标题，微信、QQ和QQ空间等平台使用
//        //oks.setTitle(ctx.getString(R.string.share));
//        //oks.setTitleUrl("http://thyrsi.com/t6/374/1537583606x-1566680214.jpg"); // titleUrl QQ和QQ空间跳转链接
//        // text是分享文本，所有平台都需要这个字段
//        //oks.setText(ctx.getString(R.string.share));
//        oks.setImageUrl("https://files.catbox.moe/ib3x5e.jpg");//确保SDcard下面存在此张图片
//        //oks.setUrl("http://thyrsi.com/t6/374/1537583606x-1566680214.jpg");
//
//        oks.setCallback(new PlatformActionListener() {
//            @Override
//            public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
//                if(platform.getName().equals("QQ") || platform.getName().equals("Wechat")){
//                    Q.echo(ctx,"请分享到朋友圈或QQ空间！");
//                }else {
//                    Q.echo(ctx,"分享成功");
//                    ACache.get(ctx).put(KEY_SHARE,SHARE_OK,5 * ACache.TIME_DAY);
//                }
//            }
//
//            @Override
//            public void onError(Platform platform, int i, Throwable throwable) {
//                Q.echo(ctx,"分享失败");
//            }
//
//            @Override
//            public void onCancel(Platform platform, int i) {
//                platform.getName();
//                Q.echo(ctx,platform.getName()+"分享取消");
//            }
//        });
//        // 启动分享GUI
//        oks.show(ctx);
    }


    public static void addVipTime(final Context ctx){
        final String url = API + "?api=user_share_addtime&name=" + IMEI;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                if (code.equals("1")){
                    Q.echo(ctx,"获得体验时间成功");
                    MyApplication.user_state = Qe.会员_状态_正常;
                }else {

                }

            }
        }).start();
    }

    public void onDestroy(){

    }

    private void initX5WebView() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Q.log("eerres", arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(this, cb);
    }


    private void ininDownloader(){
        PRDownloader.initialize(getApplicationContext()); // 下载器

        // 即使在应用程序被杀死后启用数据库支持恢复
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .setReadTimeout(30000) // 读取超时 max 30秒
                .setConnectTimeout(30000) // 链接超时 max 30秒
                .build();
        PRDownloader.initialize(getApplicationContext(), config);
    }
    public static void setAty(Activity a){
        aty = a;
    }


    private static Qdown mQDown;
    public static void AddDown(ItemList videoInfo,String index,ItemList item){
        // 判断是否已经有下载记录 如果有则删除
        if (videoInfo == null){
            Q.log("eeenull","空的");
        }
        List<DBDown> dblist = DataSupport.where("url=? and index=?",videoInfo.id,index).find(DBDown.class);
        DBDown mDblist = null;
        if (dblist.size() > 0){
            mDblist = dblist.get(0);
        }else {
            mDblist = new DBDown();
            mDblist.setUrl(videoInfo.id);
            mDblist.setState(Qe.DOWNSTATE_N);
            mDblist.setName(videoInfo.name);
            mDblist.setCodeUrl(item.url);
            mDblist.setCodeNama(item.name);
            mDblist.setImg(videoInfo.img);
            mDblist.setIndex(Integer.parseInt(index));
            mDblist.save();
        }

        if (mQDown == null) mQDown = new Qdown().inin(aty);
        mQDown.refreshDown();
        //mDblist.setState(Qe.RECORDTYPE_缓存);
    }
    public static void RefreshDown(){
        if (mQDown == null) mQDown = new Qdown().inin(aty);
        mQDown.refreshDown();
    }

    public static final String getIMEI(Context context) {
        try {
            //实例化TelephonyManager对象
            TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //获取IMEI号
            String imei = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            /*mTelephony.getDeviceId();
            if (imei == null)
                imei*/

            //在次做个验证，也不是什么时候都能获取到的啊
            if (imei == null)
                imei = "678910";


            return imei;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void refreshUserData() {
        String IMEI = getIMEI(ctx);
        MyApplication.IMEI = IMEI;
        final String url = API + "?api=user_data&name=" + IMEI;
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                if (code.length() > 5) {
                    //已经是VIP
                    MyView.userdata s;
                    try {
                        s = new Gson().fromJson(code, MyView.userdata.class);
                    }catch (Exception e){
                        s = null;
                    }
                    setUserData(s);
                } else {
                    //不是VIP
                    user_state = Qe.会员_状态_未登录;
                }
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
        }).start();
    }


    private static void isOK(File file) {
        String s = FileIOUtils.readFile2String(file);
        if (s != null && s.length() > 0) {
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

    }

    private static void setUserData(MyView.userdata mUserData) {
        if (mUserData != null && mUserData.deadtime != null) {
            int time = (int) (System.currentTimeMillis() / 1000);
            if (time < Integer.parseInt(mUserData.deadtime)) {
                MyApplication.user_state = Qe.会员_状态_正常;
            } else {
                MyApplication.user_state = Qe.会员_状态_已过期;
            }
        }
    }






}
