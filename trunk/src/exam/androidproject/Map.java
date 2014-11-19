package exam.androidproject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class Map extends BaseActivity implements OnClickListener
{
    Button                    settingBtn, btnBook;
    ToggleButton              btnPlay;
    Setting                   setting;
    public static MediaPlayer music;

    protected void onCreate(Bundle savedInstanceState)
    {
        actList.add(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        music = MediaPlayer.create(this, R.raw.bgm);
        music.setLooping(true);
        music.start();

        settingBtn = (Button) findViewById(R.id.setting_btn);
        btnBook = (Button) findViewById(R.id.btn_book);
        btnPlay = (ToggleButton) findViewById(R.id.btn_play);

        setting = new Setting(Map.this);

        setting.setCanceledOnTouchOutside(false);

        setting.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface arg0)
            {
                // TODO Auto-generated method stub
                for (int i = 0; i < actList.size(); i++)
                    actList.get(i).finish();
            }
        });

        settingBtn.setOnClickListener(this);
        btnBook.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

    }

    public void onClick(View v)
    {
        if (v == settingBtn)
            setting.show();
        else if (v == btnBook)
        {
            Intent Book = new Intent(Map.this, Picturebook.class);
            startActivity(Book);
        }
        else if (v == btnPlay)
        {
            if (btnPlay.isChecked())
            {
                btnPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_pause));
            }
            else
            {
                btnPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_play));
            }
        }
    }
}
