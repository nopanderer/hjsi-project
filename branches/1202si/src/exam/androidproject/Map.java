package exam.androidproject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ToggleButton;
import exam.game.GameMaster;
import exam.game.GameState;

public class Map extends BaseActivity implements OnClickListener
{
    Button                    settingBtn, btnBook, btnStore;
    ToggleButton              btnPlay;
    Drawable                  drawableBtnPlay_Pause;
    Drawable                  drawableBtnPlay_Play;
    Setting                   setting;
    public static MediaPlayer music;

    /* 게임 정보를 갖고있는 개체 */
    private GameState         gameState;
    /* 게임을 진행하는 인게임 스레드를 가진 개체 */
    private GameMaster        gameMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        actList.add(this);
        super.onCreate(savedInstanceState);

        // GameState 생성
        gameState = new GameState(getResources());

        // surfaceview 등록
        GameSurface gameView = new GameSurface(this, gameState);
        setContentView(gameView);
        // setContentView(R.layout.activity_map);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.activity_map, null);
        addContentView(layout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        drawableBtnPlay_Pause = getResources().getDrawable(R.drawable.btn_pause);
        drawableBtnPlay_Play = getResources().getDrawable(R.drawable.btn_play);

        settingBtn = (Button) findViewById(R.id.setting_btn);
        btnBook = (Button) findViewById(R.id.btn_book);
        btnPlay = (ToggleButton) findViewById(R.id.btn_play);
        btnStore = (Button) findViewById(R.id.btn_store);

        setting = new Setting(Map.this);
        setting.setCanceledOnTouchOutside(false);
        setting.setOnDismissListener(new OnDismissListener()
        {
            @Override
            public void onDismiss(DialogInterface arg0)
            {
                // TODO Auto-generated method stub
                for (int i = 0; i < actList.size(); i++)
                {
                    actList.get(i).finish();
                }
            }
        });

        settingBtn.setOnClickListener(this);
        btnBook.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnStore.setOnClickListener(this);

        music = MediaPlayer.create(this, R.raw.bgm);
        music.setLooping(true);
        music.start();

        /* GameMaster 생성 */
        gameMaster = new GameMaster(gameView.getHolder(), gameState);
    }

    @Override
    protected void onPause()
    {
        super.onStop();
        if (gameMaster != null)
        {
            gameMaster.pauseGame();
            btnPlay.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_play));
        }

        if (music != null)
        {
            music.pause();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (music != null)
        {
            music.start();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (gameMaster != null)
        {
            gameMaster.quitGame();
        }

        if (music != null)
        {
            music.stop();
            music.release(); // 그냥 해봄ㅋ
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v == settingBtn)
        {
            setting.show();

            if (btnPlay.isChecked())
            {
                btnPlay.setBackgroundDrawable(drawableBtnPlay_Play);
                gameMaster.pauseGame();
            }
        }
        else if (v == btnBook)
        {
            Intent Book = new Intent(Map.this, Picturebook.class);
            startActivity(Book);
        }
        else if (v == btnPlay)
        {
            if (btnPlay.isChecked())
            {
                btnPlay.setBackgroundDrawable(drawableBtnPlay_Pause);
                gameMaster.playGame();
            }
            else
            {
                btnPlay.setBackgroundDrawable(drawableBtnPlay_Play);
                gameMaster.pauseGame();
            }
        }
        else if (v == btnStore)
        {
            Intent Store = new Intent(Map.this, Store.class);
            startActivity(Store);
        }
    }
}
