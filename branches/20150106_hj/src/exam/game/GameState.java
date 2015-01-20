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
    public long      beforeRegen = System.currentTimeMillis();
    public long      regen       = 1000;                      // create mob per 1 sec
    public int       usedMob     = 0;                         // 몹이 실제로 생성된 수
    public int       deadMob     = 0;
    public int       curMob      = 0;
    public int       wave        = 1;

    public final int MAX_MOP     = 10;

    public GameState(Resources res)
    {
        this.res = res;
        makeFace(wave);
        createMobs();
    }

    /**
     * 몹 비트맵 저장
     * 
     * @param res
     *            몹 비트맵
     */
    public void makeFace(int wave)
    {
        int drawableId = res.getIdentifier("mob" + wave, "drawable", "exam.androidproject");
        BitmapFactory.Options option = new BitmapFactory.Options();

        option.inSampleSize = 4;

        mImgMob = BitmapFactory.decodeResource(res, drawableId, option);

        if ((mImgMob.getWidth() != 64) || (mImgMob.getHeight() != 64))
        {
            mImgMob = Bitmap.createScaledBitmap(mImgMob, 64, 64, true);
        }
    }

    public void createMobs()
    {
        for (int i = 0; i < MAX_MOP; i++)
            // 여기서는 10마리까지지만 실제로는 파일입력을 통해서
            Mobs.add(new Mob(90, 90, 64, 64, mImgMob));
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
        for (Mob mob : getMobs())
        {
            mob.face.recycle();
            mob.face = null;
        }
        Mobs.clear();
    }

    public LinkedList<Mob> getMobs()
    {
        return Mobs;
    }

}
