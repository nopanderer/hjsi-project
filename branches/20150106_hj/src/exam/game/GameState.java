package exam.game;

import java.util.LinkedList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 * 현재는 테스트용으로 아주 대충 만듦 여기에 게임 오브젝트들을 보관한다. 스테이지 정보와는 다르다. 예를 들어, 이 클래스에서는 몹이 죽으면 그 개체는 제거된다.
 */
public class GameState
{
    LinkedList<Mob>  Mobs        = new LinkedList<Mob>();

    public Resources res;
    public Bitmap    mImgMob;                                 // 몹 비트맵
    public long      beforeRegen = System.currentTimeMillis();                  // 리젠하기 전 시간
    public long      regen       = 1000;                      // create mob per 1 sec
    public int       usedMob     = 0;                         // 몹이 실제로 생성된(내부적 카운터 위해)
    public int       deadMob     = 0;                         // 죽은 몹
    public int       curMob      = 0;                         // 현재 몹
    public int       wave        = 1;

    public final int MAX_MOP     = 10;

    public GameState(Resources res)
    {
        this.res = res;
        makeFace();
        createMobs();
    }

    public void makeFace()
    {

        int drawableId = res.getIdentifier("mob" + wave, "drawable", "exam.androidproject");
        BitmapFactory.Options option = new BitmapFactory.Options();

        option.inSampleSize = 4;

        mImgMob = BitmapFactory.decodeResource(res, drawableId, option);

        if ((mImgMob.getWidth() != 64) || (mImgMob.getHeight() != 64))
        {
            mImgMob = Bitmap.createScaledBitmap(mImgMob, 64, 64, true);
        }

        AppManager.getInstance().addBitmap("mob" + wave, mImgMob);

    }

    public void createMobs()
    {
        for (int i = 0; i < MAX_MOP; i++)
            // 여기서는 10마리까지지만 실제로는 파일입력을 통해서
            Mobs.add(new Mob(90, 90, 64, 64, wave));
    }

    public void addMob()
    {
        if (System.currentTimeMillis() - beforeRegen > regen)
            beforeRegen = System.currentTimeMillis();
        else
            return;

        Mobs.get(usedMob).created = true;
        usedMob++;
        curMob++;
    }

    public void destroyMob()
    {
        AppManager.getInstance().recycleBitmap("mob" + wave);
        Mobs.clear();
    }

    public LinkedList<Mob> getMobs()
    {
        return Mobs;
    }

}
