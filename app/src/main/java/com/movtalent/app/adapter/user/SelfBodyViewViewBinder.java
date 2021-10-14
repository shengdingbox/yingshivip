package com.movtalent.app.adapter.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.azhon.appupdate.manager.DownloadManager;
import com.lib.common.util.DataInter;
import com.lib.common.util.SharePreferencesUtil;
import com.lib.common.util.utils.DataCleanManager;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.CenterListPopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.media.playerlib.PlayApp;
import com.movtalent.app.R;
import com.movtalent.app.model.dto.UpdateDto;
import com.movtalent.app.presenter.UpdatePresenter;
import com.movtalent.app.presenter.iview.IUpdate;
import com.movtalent.app.util.UserUtil;
import com.movtalent.app.view.AllDownLoadActivity;
import com.movtalent.app.view.AllFavorActivity;
import com.movtalent.app.view.AllHistoryActivity;
import com.movtalent.app.view.CastDescriptionnActivity;
import com.movtalent.app.view.LoginActivity;
import com.movtalent.app.view.ReportActivitys;
import com.movtalent.app.view.SettingActivity;
import com.movtalent.app.view.UserProfileActivity;
import com.movtalent.app.view.dialog.BottomShareView;

import butterknife.BindView;
import kale.sharelogin.content.ShareContent;
import kale.sharelogin.content.ShareContentPic;
import me.drakeet.multitype.ItemViewBinder;

/**
 * @author huangyong
 * createTime 2019-09-16
 */
public class SelfBodyViewViewBinder extends ItemViewBinder<SelfBodyView, SelfBodyViewViewBinder.ViewHolder> {


    TextView exit;

    private DownloadManager manager;


    @NonNull
    @Override
    protected ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View root = inflater.inflate(R.layout.item_self_body_view, parent, false);
        return new ViewHolder(root);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, @NonNull SelfBodyView selfBodyView) {

        holder.selfDlan.setOnClickListener(v -> {
            CastDescriptionnActivity.start(holder.itemView.getContext());
        });
        holder.selfFavor.setOnClickListener(v -> {
            if (UserUtil.isLogin()) {
                AllFavorActivity.startTo(v.getContext());
            } else {
                LoginActivity.start(v.getContext());
            }
        });
        holder.selfHistory.setOnClickListener(v -> AllHistoryActivity.startTo(v.getContext()));
        holder.selfDown.setOnClickListener(v -> AllDownLoadActivity.startTo(v.getContext()));
        holder.selfReport.setOnClickListener(v -> ReportActivitys.start(v.getContext()));
        holder.selfCearCache.setOnClickListener(v -> {
          new XPopup.Builder(holder.itemView.getContext()).asConfirm("提示！", "清空缓存后，图片缓存及登录状态将被清除，下载和浏览记录不会被清除。", new OnConfirmListener() {
              @Override
              public void onConfirm() {
                  DataCleanManager.cleanInternalCache(holder.itemView.getContext());
                  UserUtil.exitLogin(holder.itemView.getContext());
                  holder.itemView.getContext().sendBroadcast(new Intent(DataInter.KEY.ACTION_REFRESH_COIN));
              }
          }).show();
        });
        holder.selfQQ.setOnClickListener(v -> {
            int mzsm = R.string.mzsm;
            new XPopup.Builder(v.getContext()).asConfirm("免责声明", String.valueOf(mzsm), () -> {
            }).show();
        });
        holder.selfShare.setOnClickListener(v -> {
            final Bitmap thumbBmp = ((BitmapDrawable) v.getContext().getResources().getDrawable(R.drawable.share)).getBitmap();
            ShareContent mShareContent = new ShareContentPic(thumbBmp);
            new XPopup.Builder(v.getContext()).asCustom(new BottomShareView(v.getContext(), mShareContent)).show();
        });

        //账户设置点击
        holder.selfJump1.setOnClickListener(v -> UserProfileActivity.start(v.getContext()));
        //播放器设置点击
        String[] arr = {"MediaPlayer解码","ExoPlayer解码","IjkPlayer解码"};
        holder.selfJump2.setOnClickListener(v -> {
            CenterListPopupView listPopupView = new XPopup.Builder(v.getContext()).asCenterList("切换解码器", arr, new OnSelectListener() {
                @Override
                public void onSelect(int position, String text) {
                    switch (position) {
                        case 0:
                            PlayApp.swich(PlayApp.PLAN_ID_MEDIA);
                            SharePreferencesUtil.setIntSharePreferences(v.getContext(),DataInter.KEY.PLAY_CODEC,0);
                            break;
                        case 1:
                            PlayApp.swich(PlayApp.PLAN_ID_EXO);
                            SharePreferencesUtil.setIntSharePreferences(v.getContext(),DataInter.KEY.PLAY_CODEC,1);
                            break;
                        case 2:
                            PlayApp.swich(PlayApp.PLAN_ID_IJK);
                            SharePreferencesUtil.setIntSharePreferences(v.getContext(),DataInter.KEY.PLAY_CODEC,2);
                            break;
                    }
                }
            });
            listPopupView.show();
            int preferences = SharePreferencesUtil.getIntSharePreferences(v.getContext(), DataInter.KEY.PLAY_CODEC, 0);
            listPopupView.setCheckedPosition(preferences);
        });
        //升级版本点击
        holder.selfJump3.setOnClickListener(v-> {});
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView selfDlan;
        TextView selfFavor;
        TextView selfHistory;
        TextView selfDown;
        TextView selfReport;
        TextView selfCearCache;
        TextView selfQQ;
        TextView selfShare;
        TextView selfJump1;
        TextView selfJump2;
        TextView selfJump3;

        ViewHolder(View itemView) {
            super(itemView);

            selfDlan = itemView.findViewById(R.id.self_dlan);
            selfFavor = itemView.findViewById(R.id.self_favor);
            selfHistory = itemView.findViewById(R.id.self_his);
            selfDown = itemView.findViewById(R.id.self_down);
            selfReport = itemView.findViewById(R.id.self_report);
            selfCearCache = itemView.findViewById(R.id.self_clear_cache);
            selfQQ = itemView.findViewById(R.id.self_qqgroup);
            selfShare = itemView.findViewById(R.id.self_share);
            selfJump1 = itemView.findViewById(R.id.self_jump1);
            selfJump2 = itemView.findViewById(R.id.self_jump2);
            selfJump3 = itemView.findViewById(R.id.self_jump3);
        }

    }
}
