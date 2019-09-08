package cn.m.mslide;

/**
 * Created by H19 on 2018/4/6 0006.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * author:Created by ZhangPengFei on 2017/12/2.
 */

public class SlideAdapter extends PagerAdapter {

    private List<SlideItem> list;
    private Context context;


    public SlideAdapter(List<SlideItem> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void Refresh(List<SlideItem> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override // 获取总数
    public int getCount() {
        return Integer.MAX_VALUE;
    }


    @Override // 烧毁项目
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view==object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View inflate = View.inflate(context, R.layout.slideview, null);
        ImageView img = (ImageView) inflate.findViewById(R.id.img);

        if (list.size() < 1) return inflate;
        int index = position % list.size();
        if (index >= list.size()) return inflate;

        final SlideItem item = list.get(index);
        if (item.imgId != 0){
            img.setImageResource(item.imgId);
        }else if (item.imgUrl != null){
            Glide.with(context).load(item.imgUrl).into(img);
        }

        if (list.get(index).name !=null && !list.get(index).name.isEmpty()){
            TextView t1 = (TextView)inflate.findViewById(R.id.name);
            t1.setText(list.get(index).name);
        }


        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) mListener.onClick(view,item);
            }
        });
        container.addView(inflate);
        return inflate;
    }


    // 点击监听
    private onListener mListener;
    public void setOnItemClickListener(onListener listener){
        mListener = listener;
    }
    public interface onListener{
        void onClick(View v,SlideItem t);
    }
}