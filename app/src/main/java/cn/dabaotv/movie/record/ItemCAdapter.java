package cn.dabaotv.movie.record;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import cn.dabaotv.video.R;

import java.util.List;

/**
 * Created by H19 on 2018/4/18 0018.
 */
public class ItemCAdapter extends BaseItemDraggableAdapter<ItemC,BaseViewHolder> {
    private Context mContext;
    public int itemstyle;

    private boolean isCanSelect;
    public boolean isEditState;
    public boolean displayPlayButton; // 搜索栏才有！ 搜索栏是15

    public ItemCAdapter(List<ItemC> data){
        super(data);
    }
    public ItemCAdapter(Context context, int layouResId, List<ItemC> data, int style){
        super(layouResId,data);
        this.mContext = context;
        this.itemstyle = style;
    }
    public void setItemstyle(int style){
        // 1 name | 2 name+img | 3 name + msg ｜４ name+msg+img
        itemstyle = style;
    }

    public void setEditState(boolean b){
        isEditState = b;
        notifyDataSetChanged();
    }
    public void setCanSelect(boolean b){
        isCanSelect = b;
    }
    public boolean getEditMode(){
        return isEditState;
    }

    protected void convert(BaseViewHolder helper, final ItemC item){
        // 标题 全兼容
        if (isCanSelect == true){
            if (isEditState == true){
                helper.getView(R.id.select).setVisibility(View.VISIBLE);
                if (item.select == true) {
                    helper.setImageResource(R.id.select,R.drawable.ic_checked);
                } else {
                    helper.setImageResource(R.id.select,R.drawable.ic_uncheck);
                }
            }else {
                helper.getView(R.id.select).setVisibility(View.GONE);
            }
            if (item.url == "00000") helper.getView(R.id.select).setVisibility(View.GONE);
        }

        if (this.itemstyle < 6){
            helper.setText(R.id.name,item.name);
        }
        // 图标 5 an 2
        if (this.itemstyle == 2 || this.itemstyle == 4){
            if (item.imgId == 0 && item.img != null){
                Glide.with(mContext).load(item.img).into((ImageView) helper.getView(R.id.img));
            }else if(item.imgId !=0 ){
                helper.setImageResource(R.id.img,item.imgId);
            }
        }
        // msg 3　an 4
        if (this.itemstyle == 3 || this.itemstyle == 4){
            helper.setText(R.id.msg,item.msg);
        }

        if (this.itemstyle == 15){
            if (displayPlayButton == true){
                helper.getView(R.id.search_player_button).setVisibility(View.VISIBLE);
            }else {
                helper.getView(R.id.search_player_button).setVisibility(View.GONE);
            }
            helper.setText(R.id.name,item.name);
            Glide.with(mContext).load(item.img).into((ImageView) helper.getView(R.id.img));
            helper.setText(R.id.msg,item.msg);
        }

    }


}