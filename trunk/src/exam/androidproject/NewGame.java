package exam.androidproject;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

public class NewGame extends BaseActivity
{
    AnimationDrawable mAni;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newgame);

        ImageView img = (ImageView) findViewById(R.id.loading);
        mAni = (AnimationDrawable) img.getBackground();

        img.post(new Runnable()
        {
            public void run()
            {
                mAni.setOneShot(true);
                mAni.start();
            }
        });
    }

}
