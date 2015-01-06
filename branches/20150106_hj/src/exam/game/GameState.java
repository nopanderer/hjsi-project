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

    public Bitmap  mImgElement;

    public GameState(Resources res)
    {
        EDElement mElement = new EDElement();

        int drawableId = res.getIdentifier(mElement.imgName, "drawable", "exam.androidproject");
        mImgElement = BitmapFactory.decodeResource(res, drawableId);
        // AppManager.getInstance().addBitmap(mElement.imgName, mImgElement);

        if ((mImgElement.getWidth() != 64) || (mImgElement.getHeight() != 64))
        {
            mImgElement = Bitmap.createScaledBitmap(mImgElement, 64, 64, true);
        }

        arTestUnits.add(new Mob(15, 15, 64, 64));

    }

    public ArrayList<Mob> getMobs()
    {
        return arTestUnits;
    }
}
