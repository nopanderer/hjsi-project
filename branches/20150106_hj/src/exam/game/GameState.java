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
    ArrayList<Mob> arTestUnits = new ArrayList<Mob>();

    public Bitmap  mImgMob;                                 // 몹 비트맵
    public long    beforeRegen = System.currentTimeMillis();
    public long    regen       = 1000;                      // create mob per 1 sec
    public int     usedMob     = 0;                         // 몹이 실제로 생성된 수

    public GameState(Resources res)
    {
        makeFace(res);
        createMobs();
    }

    public void makeFace(Resources res)
    {
        int drawableId = res.getIdentifier("mob1", "drawable", "exam.androidproject");
        mImgMob = BitmapFactory.decodeResource(res, drawableId);

        if ((mImgMob.getWidth() != 64) || (mImgMob.getHeight() != 64))
        {
            mImgMob = Bitmap.createScaledBitmap(mImgMob, 64, 64, true);
        }
    }

    public void createMobs()
    {
        for (int i = 0; i < 10; i++)
            // 여기서는 20마리까지지만 실제로는 파일입력을 통해서
            arTestUnits.add(new Mob(90, 90, 64, 64, mImgMob));
    }

    public void addMob()
    {
        if (System.currentTimeMillis() - beforeRegen > regen)
            beforeRegen = System.currentTimeMillis();
        else
            return;

        arTestUnits.get(usedMob).created = true;
        usedMob++;
    }

    public ArrayList<Mob> getMobs()
    {
        return arTestUnits;
    }

}
