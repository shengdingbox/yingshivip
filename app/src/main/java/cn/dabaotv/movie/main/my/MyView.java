package cn.dabaotv.movie.main.my;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import cn.dabaotv.movie.MyApplication;
import cn.dabaotv.movie.MzsmActivity;
import com.google.gson.Gson;
import cn.dabaotv.movie.Function.Ghttp;
import cn.dabaotv.movie.Q.Q;
import cn.dabaotv.movie.Q.QConfig;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.record.RecordActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cn.m.cn.数据缓存;

public class MyView extends LinearLayout implements View.OnClickListener, QConfig {

    public MyView(Context context) {
        super(context);
        loadview();
        refreshUserData();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int erron = msg.what;
            switch (erron) {
                case 1:
                    setUserData();
                    break;
                case 2:
                    Toast.makeText(getContext(), "链接服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    userdata it = (userdata) msg.obj;
                    if (it == null) {
                        ToastUtils.showShort("激活失败，请重试");
                        return;
                    }
                    if (it.code == 0) {
                        mUserData = it;
                        setUserData();
                        Toast.makeText(getContext(), "激活码使用成功", Toast.LENGTH_SHORT).show();
                        if (RecDia.isShowing()) RecDia.dismiss();
                    } else {
                        Toast.makeText(getContext(), it.msg + " ", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        public void alll() {
        }

        ;
    };

    public userdata mUserData;

    public class userdata {

        public int code; // 错误码
        public String msg; // 错误提示
        public String name;
        public String score; // 积分
        public String status; // 状态
        public String group; // 用户组ID
        public String face; // user_face
        public String deadtime; // VIP过期时间

        public void userdata() {
        }
    }

    private void setUserData() {
        if (mUserData != null && mUserNmae != null && mUserData.deadtime != null) {
            int time = (int) (System.currentTimeMillis() / 1000);
            String id = mUserData.name;
            String name = "";
            if (time < Integer.parseInt(mUserData.deadtime)) {
                MyApplication.user_state = Qe.会员_状态_正常;
                name = "到期：" + getDateToString(Integer.parseInt(mUserData.deadtime));
            } else {
                MyApplication.user_state = Qe.会员_状态_已过期;
                name = "已到期，点击激活";
            }
            mUserNmae.setText(name);
            mUserId.setText(id);
        }
    }

    public boolean isVip() {
        if (mUserData != null && mUserData.deadtime != null) {
            int time = (int) (System.currentTimeMillis() / 1000);
            Log.e("MyView", "currentTimeMillis:" + time + " " + mUserData.deadtime);
            if (time < Integer.parseInt(mUserData.deadtime)) {
                MyApplication.user_state = Qe.会员_状态_正常;
                return true;
            } else {
                MyApplication.user_state = Qe.会员_状态_已过期;
                return false;
            }
        } else {
            return false;
        }
    }


    public static String getDateToString(int time) {
        Date d = new Date((long) time * 1000);
        return new SimpleDateFormat("yyyy-MM-dd").format(d);
    }


    private TextView mUserNmae;
    private TextView mUserId;

    public void loadview() {
        View v = View.inflate(getContext(), R.layout.main_my, this);
        v.findViewById(R.id.my_history).setOnClickListener(this);
        v.findViewById(R.id.my_download).setOnClickListener(this);
        v.findViewById(R.id.my_collect).setOnClickListener(this);
        v.findViewById(R.id.my_info).setOnClickListener(this);
        v.findViewById(R.id.my_mzsm).setOnClickListener(this);
        v.findViewById(R.id.my_share).setOnClickListener(this);
        v.findViewById(R.id.my_clear).setOnClickListener(this);
        v.findViewById(R.id.my_setup).setOnClickListener(this);
        v.findViewById(R.id.my_buy).setOnClickListener(this);
        v.findViewById(R.id.my_info).setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(mUserId.getText());
                ToastUtils.showShort("复制用户名到剪切板！");
                return true;
            }
        });
        mUserNmae = (TextView) v.findViewById(R.id.user_name);
        mUserId = (TextView) v.findViewById(R.id.userId);

        加载激活窗口();
    }

    public void refreshUserData() {
        String IMEI = getIMEI(getContext());
        MyApplication.IMEI = IMEI;
        final String url = API + "?api=user_data&name=" + IMEI; //注册，没有就注册
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                Message msg = new Message();
                if (code.length() > 5) {
                    msg.what = 1;
                    try {
                        mUserData = new Gson().fromJson(code, userdata.class);
                    } catch (Exception e) {
                        mUserData = null;
                    }

                } else {
                    msg.what = 2;
                }
                handler.sendMessage(msg);
            }
        }).start();
    }

    private View DiaRecView;
    private Dialog RecDia;
    private EditText RecEtKey;

    public void 加载激活窗口() {
        DiaRecView = View.inflate(getContext(), R.layout.main_my_rec, null);
        DiaRecView.findViewById(R.id.user_bt_vip).setOnClickListener(this);
        RecEtKey = (EditText) DiaRecView.findViewById(R.id.user_vip_key);
    }

    public void 弹出激活窗口() {
        if (RecDia == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            RecDia = builder.create();
        }
        RecDia.show();
        RecDia.getWindow().setContentView(DiaRecView);
        RecDia.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    public void 立即激活() {
        String key = RecEtKey.getText().toString();
        if (key.length() < 5) {
            Toast.makeText(getContext(), "请输入有效的激活码", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            key = URLEncoder.encode(key, "utf-8");
        } catch (Exception e) {
        }
        final String url = API + "?api=rec&" + "name=" + mUserData.name + "&key=" + key;//激活
        new Thread(new Runnable() {
            @Override
            public void run() {
                String code = Ghttp.getHttp(url);
                Message msg = new Message();
                if (code.length() > 5) {
                    msg.what = 3;
                    userdata userdata = null;
                    try {
                        userdata = new Gson().fromJson(code, userdata.class);
                    } catch (Exception e) {
                        System.out.print(e);
                    }
                    msg.obj = userdata;
                } else {
                    msg.what = 3;
                }
                handler.sendMessage(msg);
            }
        }).start();
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

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_history:
                Q.goIntent(getContext(), Qe.INTENT_历史);
                break;
            case R.id.my_download:

                Q.goIntent(getContext(),Qe.INTENT_下载);
                break;
            case R.id.my_collect:
                Q.goIntent(getContext(), Qe.INTENT_收藏);
                break;
            case R.id.my_info:
                弹出激活窗口();
                break;
            case R.id.user_bt_vip:
                立即激活();
                break;
            case R.id.my_mzsm:
                Intent window_mz = new Intent(getContext(), MzsmActivity.class);
                getContext().startActivity(window_mz);
                break;
            case R.id.my_share:
                Bitmap image = getImageFromAssetsFile("share.jpg");
                File file = saveBitmap(image, "share");
                ArrayList files = new ArrayList<File>();
                files.add(file);
                originalShareImage(getContext(),files);
                break;
                case R.id.my_clear:
                try {
                    String size = 数据缓存.getTotalCacheSize(getContext());
                    数据缓存.clearAllCache(getContext());
                    Toast.makeText(getContext(), "清理缓存完毕，共 " + size, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.my_setup:
                Q.goIntent(getContext(), Qe.INTENT_设置);
                break;
            case R.id.my_buy:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://faka.dabaotv.cn"));
                ///Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.baidu.com"));
                getContext().startActivity(intent);
                break;
        }
    }

    private Intent WindowRecord;

    public void JumpRecord(String type) {
        if (WindowRecord == null) WindowRecord = new Intent(getContext(), RecordActivity.class);
        WindowRecord.putExtra("type", type);
        getContext().startActivity(WindowRecord);
    }

    public static void originalShareImage(Context context, ArrayList<File> files) {
        Intent share_intent = new Intent();
        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            for (File f : files) {
                Uri imageContentUri = getImageContentUri(context, f);
                imageUris.add(imageContentUri);
            }
        } else {
            for (File f : files) {
                imageUris.add(Uri.fromFile(f));
            }

        }
        share_intent.setAction(Intent.ACTION_SEND_MULTIPLE);//设置分享行为
        share_intent.setType("image/jpg");//设置分享内容的类型
        share_intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        share_intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        context.startActivity(Intent.createChooser(share_intent, "Share"));

    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private static File saveBitmap(Bitmap bm, String picName) {
        try {
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/dabaotv/" + picName + ".jpg";
            File f = new File(dir);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                FileOutputStream out = new FileOutputStream(f);
                bm.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
            }
            return f;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
