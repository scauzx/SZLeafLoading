package com.scauzx.loading;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Administrator on 2017/9/26.
 */

public class LeafView extends View {

    private Paint mPaint;
    private int mHeight,mWidth;//整个view的高度和宽度
    private RectF mAcrRect;
    private float mCurrentProgress = 20;
    private ArrayList<Leaf> mLeafs;
    private ValueAnimator mLeafAnimator;
    private Bitmap mBitmap;
    private Bitmap mBackBitmap;
    private Random mRandom;
    private String TAG = LeafView.class.getSimpleName();
    private static final int LEFT_MARGIN = 9;
    private int paddingLeft,paddingRight;
    private double mProgressHeight; //进度条的高度
    private double mProgressWidth; //进度条的宽度
    private UpdateLinstener mUpdateLinstener; //下载完的回调，是用来使风扇在下载完的时候停止转动
    private String COLOR_WHITE = "#ffffff";
    private String COLOR_UPDATE = "#f5a418";
    public LeafView(Context context) {
        super(context);
        init(context);
    }

    public LeafView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LeafView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if(mPaint == null){
            mPaint = new Paint();
        }
        paddingLeft = UiUtils.dipToPx(context,LEFT_MARGIN);
        paddingRight = UiUtils.dipToPx(context,RIGHT_MARGIN);
        mBackBitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.back)).getBitmap();
    }


    /**
     *  获取到控件的高度和宽度
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = h;
        mWidth = w;
        if(mBitmap == null){
            Bitmap b = ((BitmapDrawable)getResources().getDrawable(R.drawable.leaf)).getBitmap();
            mBitmap = Bitmap.createScaledBitmap(b,b.getWidth()/3*2,b.getHeight()/3*2,true);
        }
        mProgressHeight = mHeight - 2*paddingLeft - mBitmap.getWidth();
        mProgressWidth = mWidth - paddingLeft - paddingRight/2;
        mAcrRect = new RectF(paddingLeft,paddingLeft,mHeight-paddingLeft,mHeight-paddingLeft);
        Log.d(TAG,"mHeight is " + mHeight + " paddingLedt is " + paddingLeft);
        if(mLeafs == null){
            mLeafs = new ArrayList<>();
        }
    }


    private static final int RIGHT_MARGIN = 30;
    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(Color.parseColor(COLOR_WHITE));

        canvas.drawArc(mAcrRect,90,180,true,mPaint);

        canvas.drawRect(mHeight/2,paddingLeft,mWidth - paddingLeft - paddingRight/2 ,getHeight()-paddingLeft,mPaint);

        mPaint.setColor(Color.parseColor(COLOR_UPDATE));
        drawLeaf(canvas);
        if(mUpdateLinstener != null){
            mUpdateLinstener.update(mCurrentProgress/(mWidth-paddingLeft-paddingRight/2));
        }
        if(mCurrentProgress <= 0 ){
            return;
        }
        if(mCurrentProgress > 0 && mCurrentProgress < mHeight/2 - paddingLeft){
            //填充圆弧的情况
            double degree = Math.toDegrees(Math.acos((mHeight/2 - mCurrentProgress)/mHeight*2));
            float start =  180 - (float)degree;
            float all = (float) degree * 2;
            canvas.drawArc(mAcrRect,start,all,false,mPaint);
        }else if(mCurrentProgress < mProgressWidth){
            //填充完圆弧，填充矩形
            canvas.drawArc(mAcrRect,90,180,true,mPaint);
            canvas.drawRect(mHeight/2,0,mCurrentProgress,getHeight(),mPaint);
        }else{
            canvas.drawArc(mAcrRect,90,180,true,mPaint);
            canvas.drawRect(mHeight/2,0,mWidth - paddingLeft - paddingRight/2,getHeight(),mPaint);
            if(mUpdateLinstener != null){
                mUpdateLinstener.complete();
            }
            mLeafAnimator.cancel();
        }
        canvas.drawBitmap(mBackBitmap,0,0,mPaint);
    }


    /**
     * 画出叶子
     */
    private void drawLeaf(Canvas canvas){
        if(mCurrentProgress < mWidth && (mLeafs == null || mLeafs.size() == 0)){
            mLeafs.add(getLeaf(0,0,0,0,true));
            addLeafRandom();
        }

        for(int i = 0;i<mLeafs.size();i++){
            Leaf leaf = mLeafs.get(i);
            if(leaf.xLocation > mCurrentProgress){
                Matrix matrix = new Matrix();
                matrix.postTranslate((float) leaf.xLocation,(float)leaf.yLocation); //画出位置
                if(leaf.rotateStartTime == 0){
                    leaf.rotateStartTime = System.currentTimeMillis();
                }
                long a = ((System.currentTimeMillis() - leaf.rotateStartTime)%leaf.totalRotateTime)* 360/leaf.totalRotateTime ;
                matrix.postRotate((int)a,(float) leaf.xLocation + mBitmap.getWidth()/2,(float)leaf.yLocation + mBitmap.getHeight()/2);
                canvas.drawBitmap(mBitmap,matrix,mPaint);
            }else{
                mCurrentProgress += leaf.progress;
            }
        }
        removeInivalLeaf();
    }


    /**
     * 随机增加叶子
     */
    private Thread mRancomAddLeafThread;
    public void addLeafRandom(){
        if(mRandom == null){
            mRandom = new Random();
        }
        if(mRancomAddLeafThread == null){
            mRancomAddLeafThread = new Thread(){
                @Override
                public void run() {
                    while(mCurrentProgress < mWidth - paddingLeft - paddingRight/2){
                        int time = mRandom.nextInt(1000);
                        try {
                            Thread.sleep(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(mLeafs.size() < 4){
                            mLeafs.add(getLeaf(0,0,0,0,true));

                        }
                    }
                }
            };
        }
        if(mRancomAddLeafThread.isAlive()){
            return;
        }
        mRancomAddLeafThread.start();
    }

    private boolean isRemovingLeaf = false; //正在删除无用叶子的时候不能重绘
    /**
     * 叶子到达边界后，删除无用叶子
     */
    public void removeInivalLeaf(){
        if(mLeafs == null || mLeafs.size() == 0){
            return;
        }
        isRemovingLeaf = true;
        Iterator<Leaf> iters = mLeafs.iterator();
        while (iters.hasNext()){
            Leaf leaf = iters.next();
            if(leaf.xLocation <= mCurrentProgress){
                iters.remove();
            }
        }
        isRemovingLeaf = false;
    }


    /**
     * 更新叶子
     */
    private void  updataLeaf(){
        if(mLeafAnimator == null){
            mLeafAnimator = ValueAnimator.ofFloat(0,1.0f);
            mLeafAnimator.setDuration(1000);
            mLeafAnimator.setRepeatMode(ValueAnimator.RESTART);
            mLeafAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mLeafAnimator.setInterpolator(new LinearInterpolator());
            mLeafAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Leaf leaf;
                    for(int i=0;i<mLeafs.size();i++){
                        leaf = mLeafs.get(i);
                        leaf.xLocation -= leaf.speed;
                        leaf.yLocation = (mProgressHeight/leaf.amplitude*0.5* Math.sin(toRadians(leaf.xLocation) +leaf.degree));
                        if(leaf.yLocation > 0){
                            leaf.yLocation = mHeight/2 - leaf.yLocation;
                        }else{
                            leaf.yLocation = mHeight/2 - leaf.yLocation;
                        }

                    }
                    if(!isRemovingLeaf){
                        invalidate();
                    }
                }
            });

        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mLeafAnimator.start();
            }
        });
    }

    /**
     * 将度数转化为弧长
     * @param angel
     * @return
     */
    public float toRadians(double angel)
    {
        return (float)(Math.PI * angel / 180) ;
    }


    /**
     * 这边假设叶子的运行轨迹是正弦函数绘制的线，那它的函数为 y = (mHeight - 2*padding)/(2*amplitude)sin(x+a)，从右边出来时得到一点的位置，根据这个位置得到a的值，然后每次绘制时通过x得到y
     */

    /**
     * 获取叶子
     * @return
     */
    private Leaf getLeaf(int amplitude,int speed,int totalRotateTime,double progress,boolean isRandom){
        if(mRandom == null){
            mRandom = new Random();
        }
        Leaf leaf = new Leaf();
        leaf.xLocation = mWidth - mBitmap.getWidth() - paddingRight;
        if(isRandom){
            leaf.amplitude = getAmplitude();
            leaf.totalRotateTime = getTotalRotateTime();
            leaf.speed = mRandom.nextInt(3) + 1;
            leaf.progress = mRandom.nextInt(60) + 30;
        }else{
            leaf.amplitude = amplitude;
            leaf.speed = speed;
            leaf.totalRotateTime = totalRotateTime;
            leaf.progress = progress * mProgressWidth/100;
        }
        leaf.yLocation = mRandom.nextDouble() * mProgressHeight/2;
        if(leaf.yLocation >= mProgressHeight/(2*leaf.amplitude)){
            leaf.yLocation = mProgressHeight/(2*leaf.amplitude);
        }
        double value  = 2*leaf.yLocation*leaf.amplitude/mProgressHeight;
        double degree = Math.toDegrees(Math.asin(value))-toRadians(leaf.xLocation);
        leaf.degree = degree;

        return leaf;
    }


    public void addLeaf(int amplitude,int speed,int totalRotateTime,double progress){
        mLeafs.add(getLeaf(amplitude,speed,totalRotateTime,progress,false));
        if(mLeafAnimator == null || !mLeafAnimator.isRunning()){
            updataLeaf();
        }
    }

    /**
     * 获取正弦函数的最高高度
     * @return
     */
    private int getAmplitude(){
        if(mRandom == null){
            mRandom = new Random();
        }
        return mRandom.nextInt(4) +1;
    }


    public void setUpdateLinstener(UpdateLinstener updateLinstener)
    {
        this.mUpdateLinstener = updateLinstener;
    }

    /**
     * 获取旋转一圈的时间
     * @return
     */
    private int getTotalRotateTime(){
        if(mRandom == null){
            mRandom = new Random();
        }
        return mRandom.nextInt(2500) + 700;
    }

    public void destory() {
        mLeafs.clear();
        mBitmap.recycle();
        mBackBitmap.recycle();
    }


    class Leaf{
        double xLocation;
        double yLocation;
        int speed;
        double degree;
        int amplitude; //震动高度,为高度的几分之一
        int totalRotateTime;
        long rotateStartTime = 0;
        double progress; // 代表前进的百分比

    }

    interface UpdateLinstener{
        void update(float progress);
        void complete();
    }

}
