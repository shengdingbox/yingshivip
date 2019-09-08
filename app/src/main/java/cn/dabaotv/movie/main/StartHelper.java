package cn.dabaotv.movie.main;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import cn.dabaotv.movie.DB.DBAd;
import cn.dabaotv.movie.DB.DBjk;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by H19 on 2018/9/5 0005.
 */

public class StartHelper implements QConfig {

    private Context ctx;
    public StartHelper inin(Context ctx){
        this.ctx = ctx;
        ininQdata();
        return this;
    }

    public void ininQdata(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = API + "?api=qiju";//获取解析接口和更新
                Message mes = new Message();
                try {
                    String code = Ghttp.getHttp(url);
                    mAppData = new Gson().fromJson(code,QijuData.class);
                    if (mAppData.jiekou == null){
                        mes.what = -1;
                    }else {
                        mes.what = 1;
                    }

                }catch (Exception e){
                    mes.what = -1;
                }
                handler.sendMessage(mes);
            }
            public void ddd(){}
        }).start();



    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    mVersionInfo = mAppData.version;
                    String mVersion = "";
                    try {
                        //mVersion = MStylem.getVersionName(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    /*if (mVersionInfo.up_version.length() > 2){
                        MSystemSettings.putValue(MainActivity.this,"share",mVersionInfo.up_url);//
                        String[] ver1 = mVersion.split("[.]");
                        String[] ver2 = mVersionInfo.up_version.split("[.]");
                        for (int i = 0; i < ver2.length; i++) {
                            if (ver1.length > i && Integer.parseInt(ver2[i]) > Integer.parseInt(ver1[i])){
                                post(new Runnable() {
                                    @Override
                                    public void run() {
                                        发现新版本();
                                    }
                                });
                                break;
                            }
                        }
                    }*/

                    // 广告
                    DataSupport.deleteAll(DBAd.class);
                    for (DBAd sqlAd : mAppData.ad) {
                        sqlAd.save();
                    }

                    // 接口
                    if (mAppData.jiekou.size() > 0){
                        DataSupport.deleteAll(DBjk.class);
                        for (DBjk jiekou : mAppData.jiekou) {
                            jiekou.save();
                        }
                    }
                    break;
                case -1:
                    Q.echo(ctx,"链接服务器失败");
                    break;
            }
        }
        public void llll(){}
    };
    private QijuData mAppData;
    private class QijuData{
        public VersionInfo version;
        public List<DBAd> ad;
        public List<DBjk> jiekou;
    }
    private VersionInfo mVersionInfo;
    private class VersionInfo{
        public String up_version;
        public String up_time;
        public String up_url;
        public String up_info;
        public String mode;
        public VersionInfo(){}
    }

}
