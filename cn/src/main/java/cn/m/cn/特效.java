package cn.m.cn;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

/**
 * Created by H19 on 2018/8/14 0014.
 */

public class 特效 {
    public static void 移动特效(View v, int startX, int entX, int startY, int entY, int time, boolean keep) {
        AnimationSet translate_animation_set = new AnimationSet(true);
        TranslateAnimation translate_animation = new TranslateAnimation((float)startX, (float)entX, (float)startY, (float)entY);
        translate_animation_set.addAnimation(translate_animation);
        translate_animation.setDuration((long)time);
        translate_animation_set.setFillAfter(keep);

        translate_animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {

            }
        });
        v.startAnimation(translate_animation_set);
    }
    public static void 缩小特效(View v,int time, boolean keep){

    }
}
