package com.movtalent.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.CenterListPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.media.playerlib.widget.GlobalDATA;
import com.movtalent.app.R;
import com.movtalent.app.adapter.event.OnSeriClickListener;
import com.movtalent.app.model.VideoVo;
import com.movtalent.app.model.vo.CommonVideoVo;

import java.util.ArrayList;

import me.drakeet.multitype.ItemViewBinder;

/**
 * @author huangyong
 * createTime 2019-09-15
 */
public class DetailPlaySectionViewBinder extends ItemViewBinder<DetailPlaySection, DetailPlaySectionViewBinder.ViewHolder> {

    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        Log.d("mytest","onCreateViewHolder");
        View root = inflater.inflate(R.layout.item_detail_play_section, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull DetailPlaySection detailPlaySection) {
        Log.d("mytest","调用onBindViewHolder"+detailPlaySection.getCommonVideoVo().getVodPlayFrom());
        CommonVideoVo commonVideoVo = detailPlaySection.getCommonVideoVo();
        holder.setData(holder.itemView.getContext(),
                commonVideoVo.getMovPlayUrlList().get(detailPlaySection.getGroupPlay()),//当前分组下的视频列表集合
                detailPlaySection.getClickListener(),
                detailPlaySection.getGroupPlay());//视频播放器ID
        holder.seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailPlaySection.getClickListener() != null) {
                    detailPlaySection.getClickListener().showAllSeri(detailPlaySection.getCommonVideoVo());
                }
            }
        });

        SparseArray<ArrayList<VideoVo>> movPlayUrlList = commonVideoVo.getMovPlayUrlList();
        String vodPlayFrom = commonVideoVo.getVodPlayFrom();
        String[] from = vodPlayFrom.split("[$][$][$]");
        holder.playRes.setText("切换线路："+from[detailPlaySection.getGroupPlay()]);
        holder.playRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CenterListPopupView popupView = new XPopup.Builder(holder.itemView.getContext()).asCenterList("选择播放线路", from, null, 0,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                Log.d("mytest","切换数据源" +position +"   "+detailPlaySection.getGroupPlay());
                                holder.setData(holder.itemView.getContext(),
                                        movPlayUrlList.get(position), detailPlaySection.getClickListener(), position);
                                detailPlaySection.getClickListener().switchPlay(movPlayUrlList.get(position).get(GlobalDATA.PLAY_INDEX).getPlayUrl(),
                                        GlobalDATA.PLAY_INDEX, position);
//                                getAdapter().notifyDataSetChanged();
                                detailPlaySection.setGroupPlay(position);
                                holder.playRes.setText("切换线路："+from[position]);

                            }
                        });
                popupView.setCheckedPosition(detailPlaySection.getGroupPlay());
                popupView.show();

            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RecyclerView playList;
        TextView seeMore;
        TextView playRes;

        ViewHolder(View itemView) {
            super(itemView);
            playList = itemView.findViewById(R.id.play_list);
            seeMore = itemView.findViewById(R.id.see_all);
            playRes = itemView.findViewById(R.id.play_res);
        }

        public void setData(Context context, ArrayList<VideoVo> videoVos, OnSeriClickListener clickListener, int groupPlay) {
            PlayListAdapter playListAdapter = new PlayListAdapter(videoVos, clickListener,groupPlay);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            playList.setLayoutManager(linearLayoutManager);
            playList.setAdapter(playListAdapter);
        }
    }

}
