package exam.game;

import java.util.ArrayList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 * 현재는 테스트용으로 아주 대충 만듦 여기에 게임 오브젝트들을 보관한다. 스테이지 정보와는 다르다. 예를 들어, 이 클래스에서는 몹이 죽으면 그 개체는 제거된다.
 */
public class GameState
{
    ArrayList<Mob>    arTestUnits = new ArrayList<Mob>();

    public Bitmap     mImgMob;                           // 몹 비트맵
    public int        wave        = 1;                   // 현재 웨이브
    private final int FINAL_WAVE  = 2;                   // 끝판 라운드

    public GameState(Resources res)
    {
        for (; wave <= FINAL_WAVE; wave++)
        {
            int drawableId = res.getIdentifier("mob" + wave, "drawable", "exam.androidproject");
            mImgMob = BitmapFactory.decodeResource(res, drawableId);

            if ((mImgMob.getWidth() != 64) || (mImgMob.getHeight() != 64))
            {
                mImgMob = Bitmap.createScaledBitmap(mImgMob, 64, 64, true);
            }

            arTestUnits.add(new Mob(90, 90, 64, 64, mImgMob));
        }
    }

    public ArrayList<Mob> getMobs()
    {
        return arTestUnits;
    }
}
