package com.scauzx.loading;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Random;

/**
 * Created by scauzx on 2017/8/31.
 */
public class MainActivity extends AppCompatActivity {
    private LeafMovingRelativeLayout mLeafMovingRelativeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLeafMovingRelativeLayout = new LeafMovingRelativeLayout(this);
        setContentView(mLeafMovingRelativeLayout);
        mLeafMovingRelativeLayout.startAnimation();
        download();
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

    }

    /**
     * 模拟下载更新界面
     */
    public void download(){
        Thread  downLoadThread = new Thread() {
            @Override
            public void run() {
                super.run();
                int  total = 0;
                int progress;
                Random random = new Random();
                while (total < 100) {
                    int time = random.nextInt(1000);
                    try {
                        Thread.sleep(time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    progress = random.nextInt(7) + 1;
                    total = total + progress;
                    mLeafMovingRelativeLayout.update(random.nextInt(4) + 1,random.nextInt(5) + 3,random.nextInt(2000) + 1000,progress);

                }
            }
        };
        downLoadThread.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLeafMovingRelativeLayout.destory();
    }

}
