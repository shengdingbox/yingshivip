package cn.dabaotv.movie.record.download;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import cn.dabaotv.movie.DB.DBDown;
import cn.dabaotv.movie.Q.Qe;
import cn.dabaotv.video.R;

import java.util.List;


public class DownloadListAdapter extends BaseQuickAdapter<DBDown,BaseViewHolder> {
    private boolean isEditMode;
    public DownloadListAdapter(List<DBDown> data){
        super(data);
    }
    public DownloadListAdapter(int layouResId, List<DBDown> data){
        super(layouResId,data);
    }
    protected void convert(BaseViewHolder helper, final DBDown item){
        if (isEditMode == true){
            helper.getView(R.id.select).setVisibility(View.VISIBLE);
            if (item.select == true) {
                helper.setImageResource(R.id.select,R.drawable.ic_checked);
            } else {
                helper.setImageResource(R.id.select,R.drawable.ic_uncheck);
            }
        }else {
            helper.getView(R.id.select).setVisibility(View.GONE);
        }

        helper.setText(R.id.name,item.getName());
        String msg = "";
        switch (item.getState()){
            case Qe.DOWNSTATE_N:msg = "等待缓存";break;
            case Qe.DownState_Ing:msg = "缓存中";break;
            case Qe.DownState_Pause:msg = "已暂停，点击恢复";break;
            case Qe.DownState_Error:case Qe.DownState_Error2:msg = "缓存失败，点击重试";break;
            case Qe.DownState_Complete:msg = "缓存完毕";break;
        }
        helper.setText(R.id.msg,msg);
        helper.setProgress(R.id.progress,item.getDownProgress());
        ImageView img = helper.getView(R.id.img);
        if (img.getDrawable() == null) Glide.with(mContext).load(item.getImg()).into(img);

    }
    public void setEditMode(boolean b){
        isEditMode = b;
        notifyDataSetChanged();
    }


}
