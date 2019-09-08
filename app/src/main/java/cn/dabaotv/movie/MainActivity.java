package cn.dabaotv.movie;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.main.MainView;
import cn.dabaotv.movie.main.StartHelper;
import cn.dabaotv.movie.utils.BaseActivity;

import org.litepal.LitePal;

import cn.m.cn.styles.StyleStatusBar;

public class MainActivity extends BaseActivity implements QConfig {
    public MainView mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainView = findViewById(R.id.main_view);
        StyleStatusBar.setWhiteBar(this);
        LitePal.getDatabase();

        new StartHelper().inin(this);
        MyApplication.setAty(this);
//        MobSDK.init(this,"27d1d8271c3a3","7e18eea30e15d53b2afd58414bdc9347");

    }

    @Override
    protected boolean setFitsSystemWindows() {
        return true;
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("你真的要离开" + getString(R.string.app_name) + "吗？☹")//设置对话框的内容
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
                            finish();
                        }
                    }).create();
            dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mainView != null) {
            mainView.reload();
        }


       /* if(ACache.get(getApplicationContext()).isExist(KEY_SHARE,STRING)){
            String lce = ACache.get(getApplicationContext()).getAsString(KEY_SHARE);
            if (lce != null) {
                if(lce.equals(SHARE_OK)){
                    //Toast.makeText(getApplicationContext(),"已分享软件，请尽情使用！",Toast.LENGTH_SHORT).show();
                    Log.d("TAG","已分享软件，请尽情使用！");
                }else {
                    Toast.makeText(getApplicationContext(),"分享到朋友圈或QQ空间即可观看！",Toast.LENGTH_SHORT).show();
                    String text = 分享.getValue(getApplicationContext(),"share","http://www.baidu.com/");
                    text = getApplicationContext().getString(R.string.share) + "\n" + text;
                    MyApplication.showShare(getApplicationContext(),text);
                }
            }else {
                Toast.makeText(getApplicationContext(),"分享到朋友圈或QQ空间即可观看！",Toast.LENGTH_SHORT).show();
                String text = 分享.getValue(getApplicationContext(),"share","http://www.baidu.com/");
                text = getApplicationContext().getString(R.string.share) + "\n" + text;
                MyApplication.showShare(getApplicationContext(),text);
            }
        }else {
            Toast.makeText(getApplicationContext(),"分享到朋友圈或QQ空间即可观看！",Toast.LENGTH_SHORT).show();
            String text = 分享.getValue(getApplicationContext(),"share","http://www.baidu.com/");
            text = getApplicationContext().getString(R.string.share) + "\n" + text;
            MyApplication.showShare(getApplicationContext(),text);
        }*/
    }
}
