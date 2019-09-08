package cn.dabaotv.movie.Q;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import cn.dabaotv.movie.Function.Bion;
import cn.dabaotv.movie.SetActivity;
import cn.dabaotv.movie.record.RecordActivity;
import cn.dabaotv.movie.search.SearchActivity;
import cn.dabaotv.movie.view.list.ItemList;
import cn.dabaotv.movie.Conl.PlayActivity;
import cn.dabaotv.video.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 幻陌 on 2018/9/3 0003.
 */

public class Q {

    public static void goIntent(Context ctx, int item){
        Intent intent = null;
        switch (item){
            case Qe.INTENT_收藏:
                intent = new Intent(ctx, RecordActivity.class);
                intent.putExtra("name","收藏");
                intent.putExtra("type",Qe.RECORDTYPE_收藏);
                break;
            case Qe.INTENT_下载:
                intent = new Intent(ctx, RecordActivity.class);
                intent.putExtra("name","缓存");
                intent.putExtra("type",Qe.RECORDTYPE_缓存);
                break;
            case Qe.INTENT_历史:
                intent = new Intent(ctx, RecordActivity.class);
                intent.putExtra("name","历史");
                intent.putExtra("type",Qe.RECORDTYPE_历史);
                break;
            case Qe.INTENT_设置:

                intent = new Intent(ctx, SetActivity.class);
                break;
        }
        if (intent != null) ctx.startActivity(intent);
    }
    public static void goIntent(Context ctx, int item,String type){
        Intent intent;
        switch (item){
            case Qe.INTENT_搜索:
                intent = new Intent(ctx, SearchActivity.class);
                intent.putExtra("type",type);
                ctx.startActivity(intent);
                break;
        }
    }
    public static void goPlayer(Context ctx, String id){
        Intent intent = new Intent(ctx,PlayActivity.class);
        intent.putExtra("id",id);
        ctx.startActivity(intent);
    }

    public static void goPlayer(Context ctx, String data,int type){
        Intent intent = new Intent(ctx,PlayActivity.class);
        intent.putExtra("data",data);
        intent.putExtra("id","0");
        ctx.startActivity(intent);
    }

    // 只有直链接链接 直接全屏播放
    public static void goPlayer(Context ctx, String url,String type){
        Intent intent = new Intent(ctx,PlayActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("type",type);
        ctx.startActivity(intent);
    }

    public static void log(Object obj2){
        Log.i("qlog",(String) obj2 + "   ");
    }
    public static void log(Object obj1,Object obj2){
        Log.i("qlog " + (String) obj1,obj2 + "  ");
    }
    public static void echo(Context ctx,String t){
        Toast.makeText(ctx, t, Toast.LENGTH_SHORT).show();
    }

    public static Bion getBion(Intent win){
        Bion bion = new Bion();
        bion.name = win.getStringExtra("name");
        bion.url = win.getStringExtra("url");
        bion.msg = win.getStringExtra("msg");
        bion.img = win.getStringExtra("img");
        bion.text = win.getStringExtra("text");
        bion.type = win.getStringExtra("type");
        bion.id = win.getStringExtra("id");

        return bion;
    }

    // 获取播放源对应的图标等
    public static List<ItemList> getCode(){
        List<ItemList> code = new ArrayList<>();
        code.add(new ItemList("优酷","youku", R.mipmap.ic_logo_youku));
        code.add(new ItemList("电影网","1905",R.mipmap.ic_logo_1905));
        code.add(new ItemList("腾讯视频","qq",R.mipmap.ic_logo_qq));
        code.add(new ItemList("芒果TV","mgtv",R.mipmap.ic_logo_mgtv));
        code.add(new ItemList("土豆视频","tudou",R.mipmap.ic_logo_tudou));
        code.add(new ItemList("爱奇艺","iqiyi",R.mipmap.ic_logo_iqiyi));
        code.add(new ItemList("PPTV","pptv",R.mipmap.ic_logo_pp));
        code.add(new ItemList("搜狐视频","sohu",R.mipmap.ic_logo_sohu));
        code.add(new ItemList("乐视视频","letv",R.mipmap.ic_logo_letv));
        code.add(new ItemList("放放（不可播放）","ffhd",R.mipmap.ic_logo_video));
        code.add(new ItemList("吉吉（不可播放）","jjvod",R.mipmap.ic_logo_video));
        code.add(new ItemList("西瓜（不可播放）","xigua",R.mipmap.ic_logo_video));
        return code;
    }


}
