package exam.androidproject;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Continue extends BaseActivity
{
    AnimationDrawable mAni;
    Handler           mHandler = new Handler()
                               {
                                   @Override
                                   public void handleMessage(Message msg)
                                   {
                                       if (msg.what == 0)
                                       {
                                           setResult(RESULT_OK);
                                           finish();
                                       }
                                   }

                               };

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue);

        String[] strHelp = getResources().getStringArray(R.array.help_text);
        TextView helpTextView = (TextView) findViewById(R.id.helpTextView);
        helpTextView.setText(strHelp[0]);

        Thread worker = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Thread.sleep(2000); // 여기서 로딩 작업을 한다고 치고..
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                mHandler.sendEmptyMessage(0);
                mAni.stop();
            }
        });

        LinearLayout loading = (LinearLayout) findViewById(R.id.loading);
        mAni = (AnimationDrawable) loading.getBackground();
        mAni.setOneShot(false);
        mAni.start();
        worker.start();

    }
}
