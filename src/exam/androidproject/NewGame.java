package exam.androidproject;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import exam.game.AppManager;

public class NewGame extends BaseActivity
{
    AnimationDrawable mAni;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        AppManager.printSimpleLogInfo();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newgame);

        ImageView img = (ImageView) findViewById(R.id.loading);
        mAni = (AnimationDrawable) img.getBackground();

        img.post(new Runnable()
        {
            @Override
            public void run()
            {
                mAni.setOneShot(true);
                mAni.start();
            }
        });
    }
}
