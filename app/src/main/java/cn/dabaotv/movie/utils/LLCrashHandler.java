package cn.dabaotv.movie.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;


public class LLCrashHandler implements UncaughtExceptionHandler {
          
    public static final String TAG = "CrashHandler";
          
    private UncaughtExceptionHandler mDefaultHandler;
    private static LLCrashHandler instance;  
    private Context mContext;
    private Map<String, String> infos = new HashMap<String, String>();
      

    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
      
  
    private LLCrashHandler() {}      
      
     public static LLCrashHandler getInstance() {      
        if(instance == null)  
            instance = new LLCrashHandler();     
        return instance;      
    }      
      
    /**   
     * 初始化   
     */      
    public void init(Context context) {
        mContext = context;      
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }      
      
  
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {       
            mDefaultHandler.uncaughtException(thread, ex);      
        } else {      
            try {      
                sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }      
            //退出程序
  /*          Timer t = new Timer();
            TimerTask closeTask = new TimerTask() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            };
            t.schedule(closeTask, 9000);*/
//            Intent intent = new Intent(mContext, AppStartActivity.class);
//            //PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
//            PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//            AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);


            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }      
    }      
      

    private boolean handleException(final Throwable ex) {
        if (ex == null) {      
            return false;      
        }      

        collectDeviceInfo(mContext);      
          
        new Thread() {
            @Override
            public void run() {      
                Looper.prepare();
                Toast.makeText(mContext, "程序异常", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }      
        }.start();
        saveCatchInfo2File(ex);

        return true;      
    }      
          

    public void collectDeviceInfo(Context ctx) {
        try {      
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {      
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);      
                infos.put("versionCode", versionCode);      
            }      
        } catch (NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }      
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {      
                field.setAccessible(true);      
                infos.put(field.getName(), field.get(null).toString());      
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }      
        }      
    }      
      

    private String saveCatchInfo2File(Throwable ex) {
    	
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");      
        }      
              
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);      
        Throwable cause = ex.getCause();
        while (cause != null) {      
            cause.printStackTrace(printWriter);      
            cause = cause.getCause();      
        }      
        printWriter.close();      
        String result = writer.toString();
        sb.append(result);

        //saveError(sb.toString());

     //   saveException(ex.getMessage(),writer.toString());
        try {      
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory()
    					.getAbsolutePath() +"/qijuvideo_Crash/";
                File dir = new File(path);
                if (!dir.exists()) {      
                    dir.mkdirs();      
                }      
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(sb.toString().getBytes());    
                sendCrashLog2PM(path+fileName);  
                fos.close();      
            }      
            return fileName;      
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }      
        return null;      
    }      

//    private void saveException(String message, String cause) {
//		if(AppContext.personDB != null){
//			AppContext.personDB.save(new TC_BASE_EXCEPT(message, cause));
//		}
//	}

   //  public static void saveError(String logStr){
   //     LonlifeApplication.saveError(logStr);
  //  }
 /*   public static void reportError(String logStr){
        String ip = CommonUtils.getLocalInetAddress().toString();
        if(ip!=null){
            ip = ip.substring(1);
        }

        MyError myerror = new MyError(Integer.parseInt(LonlifeApplication.app_uid ), LonlifeApplication.app_setted_game_id,
                CommonUtils.getMac(), CommonUtils.getVersionName(LonlifeApplication.app_ctx),
                1, logStr, ip);
        String text = JSON.toJSONString(myerror);

        //String str = "{\"uid\":" + LonlifeApplication.app_uid  + "," + "\"gid\":" + LonlifeApplication.app_setted_game_id + "," +   "\"mac\":\"" +  CommonUtils.getMac() + "\"," + "\"version\":\"" + CommonUtils.getVersionName(LonlifeApplication.app_ctx) + "\"," + "\"errorCode\":" + 1 + ","
        //        +   "\"errorInfo\":\"" +  logStr + "\"," +    "\"clientIp\":\"" +  ip + "\""  + "}";
        LonlifeWebAPI.reportError(text,  new LonlifeWebAPI.LonlifeCallback() {
            @Override
            public void onSuccess(String result) {
                String s = EnDecryptUtil.RndDecrypt(result);
                JSONObject jsonObject = JSONObject.parseObject(s);
                int code = jsonObject.getInteger("code");
                if(code==0){
                }else{
                }
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //Toast.makeText(x.app(), "网络故障，请检测您的网络", Toast.LENGTH_LONG).show();
            }
        });
    }*/

	private void sendCrashLog2PM(String fileName){
        if(!new File(fileName).exists()){
            Toast.makeText(mContext,"log文件不存在", Toast.LENGTH_SHORT).show();
            return;  
        }  
        FileInputStream fis = null;
        BufferedReader reader = null;
        String s = null;
        try {  
            fis = new FileInputStream(fileName);
            reader = new BufferedReader(new InputStreamReader(fis, "GBK"));
            while(true){  
                s = reader.readLine();  
                if(s == null) break;    
                Log.i("info", s.toString());
            }  
        } catch (FileNotFoundException e) {
            e.printStackTrace();  
        } catch (IOException e) {
            e.printStackTrace();  
        }finally{   // 关闭流  
            try {  
                reader.close();  
                fis.close();  
            } catch (IOException e) {
                e.printStackTrace();  
            }  
        }  
    }  
}    