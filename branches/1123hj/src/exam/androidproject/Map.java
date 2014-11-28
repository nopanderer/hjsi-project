package exam.androidproject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * MapActivity
 * 기능
 * - UI
 * - 터치 이벤트 관리(드래그, 핀치줌)
 * - 맵 그리기
 * 
 * @author HJ
 *
 */
public class Map extends BaseActivity implements OnClickListener
{
    Button                    btnSetting, btnBook, btnStore;
    ToggleButton              btnPlay;
    Setting                   setting;
    public static MediaPlayer music;

    protected void onCreate(Bundle savedInstanceState)
    {
        // 액티비티 스택에 추가
        actList.add(this);

        super.onCreate(savedInstanceState);
        DrawMap dm = new DrawMap(this);

        LayoutInflater inflater = getLayoutInflater();

        dm.setImage("/res/drawable-hdpi/img_map.png");
        setContentView(dm);

        View layout = (View) inflater.inflate(R.layout.activity_map, null);
        addContentView(layout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        // 배경음악
        music = MediaPlayer.create(this, R.raw.bgm);
        music.setLooping(true);
        music.start();

        btnSetting = (Button) findViewById(R.id.setting_btn);
        btnBook = (Button) findViewById(R.id.btn_book);
        btnPlay = (ToggleButton) findViewById(R.id.btn_play);
        btnStore = (Button) findViewById(R.id.btn_store);

        setting = new Setting(Map.this);

        setting.setCanceledOnTouchOutside(false);

        // 설정에서 Quit버튼 눌렀을 때
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

        btnSetting.setOnClickListener(this);
        btnBook.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnStore.setOnClickListener(this);

    }

    public void onClick(View v)
    {
        if (v == btnSetting)
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
        else if (v == btnStore)
        {
            Intent Store = new Intent(Map.this, Store.class);
            startActivity(Store);
        }
    }

}
