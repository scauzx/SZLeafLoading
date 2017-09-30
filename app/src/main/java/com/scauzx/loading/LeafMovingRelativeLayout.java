package com.scauzx.loading;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by scauzx on 2017/9/28.
 */

public class LeafMovingRelativeLayout extends RelativeLayout implements LeafView.UpdateLinstener {

    private ImageView mFan;
    private LeafView mleaf;
    private RotateAnimation mRotateAnimation;

    public LeafMovingRelativeLayout(Context context) {
        super(context);
        init(context);
    }

    public LeafMovingRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LeafMovingRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context,R.layout.activity_leaf,this);
        mFan = (ImageView) findViewById(R.id.fan);
        mleaf = (LeafView) findViewById(R.id.leaf);
        mleaf.setUpdateLinstener(this);
    }


    public void startAnimation(){
        mRotateAnimation = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatMode(Animation.RESTART);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setFillAfter(true);
        mFan.startAnimation(mRotateAnimation);
    }

    public void update(int amplitude,int speed,int totalRotateTime,double progress){
        mleaf.addLeaf(amplitude,speed,totalRotateTime,progress);

    }


    public void stopFanRotating(){
        mRotateAnimation.cancel();
        mFan.clearAnimation();
    }



    public void destory() {
        mleaf.destory();
    }

    @Override
    public void update(float progress) {

    }

    @Override
    public void complete() {
        stopFanRotating();
    }
}
