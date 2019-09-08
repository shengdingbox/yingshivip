package cn.dabaotv.movie.view.list;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;
import cn.dabaotv.movie.utils.StringUtils;

import java.util.List;


/**
 * Created by H19 on 2018/5/18 0018.
 */

public class ItemAdapter extends BaseItemDraggableAdapter<ItemList,BaseViewHolder> {
    final private int imageWidth = 600;
    public int style;
    public int isMultiSelection; // 多选模式  默认0，非，1是但无进入多选模式， 2 是并接入编辑模式
    public ItemAdapter(List<ItemList> data){
        super(data);
    }
    public ItemAdapter(int layouResId, List<ItemList> data){
        super(layouResId,data);
    }

    protected void convert(BaseViewHolder helper, final ItemList item){
        if (item == null){
            return;
        }
        if (helper.getView(R.id.name) != null){
            if (item.name != null && !item.name.isEmpty()){
                helper.setText(R.id.name, StringUtils.replaceBlank(item.name));
                helper.getView(R.id.name).setVisibility(View.VISIBLE);
            }else {
                helper.getView(R.id.name).setVisibility(View.GONE);
            }
        }

        if (helper.getView(R.id.msg)!=null){
            if (item.msg != null && !item.msg.isEmpty() ){
                helper.setText(R.id.msg,item.msg);
                helper.getView(R.id.msg).setVisibility(View.VISIBLE);
            }else{
                helper.getView(R.id.msg).setVisibility(View.GONE);
            }
        }



        final ImageView img = helper.getView(R.id.img);
        if (img != null ){
            if (item.img != null && !item.img.isEmpty()) {
                Glide.with(mContext).load(item.img).into(img);
                img.setVisibility(View.VISIBLE);
            }else if (item.imgId != 0) {
                try {
                    img.setImageResource(item.imgId);
                    img.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }else {
                img.setImageBitmap(null);
                img.setVisibility(View.GONE);
            }
        }

        ininStyles(helper,item);


        // 标题是否那种可选中的样式
        if (isMultiSelection == 2) {
            if(item.url == null){
                Log.d("11212","eeeeeeeeeeeeee");
                helper.getView(R.id.select).setVisibility(View.VISIBLE);
                return;
            }
            if (item.url.equals("00000")){ // 这个项目不处理
                helper.getView(R.id.select).setVisibility(View.GONE);
            }else {
                helper.getView(R.id.select).setVisibility(View.VISIBLE);
            }

            if (item.select) {
                helper.setImageResource(R.id.select, R.drawable.ic_checked);
            } else {
                helper.setImageResource(R.id.select, R.drawable.ic_uncheck);
            }
        }else if (isMultiSelection == 1){
            helper.getView(R.id.select).setVisibility(View.GONE);
        }
    }

    private void ininStyles(BaseViewHolder helper,ItemList item){
        TextView t1 = (TextView)helper.getView(R.id.name);
        switch (listStyle){
            case Qe.LISTTYPE_播放列表_全屏:
                if (item.select){
                    //t1.setBackgroundResource(R.drawable.back_playlist_item_full_select);
                    t1.setBackgroundResource(R.color.viewfinder_mask);
                    t1.setTextColor(mContext.getResources().getColor(R.color.playlist_select));
                }else {
                    t1.setTextColor(mContext.getResources().getColor(R.color.white));
                   // t1.setBackgroundResource(R.drawable.back_playlist_item_full);
                    t1.setBackgroundResource(R.color.viewfinder_mask);
                }
                break;
            case Qe.LISTTYPE_播放列表_竖屏:
                if (item.select){
                    t1.setBackgroundResource(R.drawable.back_playlist_item_select);
                    t1.setTextColor(mContext.getResources().getColor(R.color.playlist_select));
                }else {
                    t1.setTextColor(mContext.getResources().getColor(R.color.h3));
                    t1.setBackgroundResource(R.drawable.back_playlist_item);
                }
                break;
            case Qe.LISTTYPE_缓存列表:
                if (item.select){
                    helper.getView(R.id.select).setVisibility(View.VISIBLE);
                }else {
                    helper.getView(R.id.select).setVisibility(View.GONE);
                }
                break;
            case Qe.LISTTYPE_下载进行中:
                ((ProgressBar)helper.getView(R.id.progress)).setProgress(item.z);
                break;


        }
    }

    private int listStyle;
    public void setListStyle(int listStyle) {
        this.listStyle = listStyle;
    }
}
