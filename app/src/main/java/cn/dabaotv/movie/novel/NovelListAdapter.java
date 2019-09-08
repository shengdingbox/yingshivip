package cn.dabaotv.movie.novel;

import android.content.Context;

import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import cn.dabaotv.video.R;

import java.util.List;

/**
 * Created by H19 on 2018/5/13 0013.
 */

public class NovelListAdapter extends BaseItemDraggableAdapter<NovelListItem,BaseViewHolder> {


    public NovelListAdapter(List<NovelListItem> data){
        super(data);
    }
    public NovelListAdapter(Context context, int layouResId, List<NovelListItem> data, int style){
        super(layouResId,data);
        this.mContext = context;
    }
    protected void convert(BaseViewHolder helper, final NovelListItem item){
        // 标题 全兼容
        helper.setText(R.id.name,item.name);
        if (item.select == true){
            helper.setTextColor(R.id.name,0xFF1090FF);

        }else {
            helper.setTextColor(R.id.name,0xFF333333);
        }
    }
}
