package exam.game;

import java.util.ArrayList;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 * 현재는 테스트용으로 아주 대충 만듦
 * 여기에 게임 오브젝트들을 보관한다.
 * 스테이지 정보와는 다르다.
 * 예를 들어, 이 클래스에서는 몹이 죽으면 그 개체는 제거된다.
 */
public class GameState
{
    ArrayList<Unit> arTestUnits = new ArrayList<Unit>();

    public Bitmap   mImgElement;
    public int      x           = 0;
    public int      y           = 200;
    private int     dx          = 10;
    private int     dy          = 10;

    private int     logicFps    = 0;

    public GameState(Resources res)
    {
        EDElement mElement = new EDElement();

        // 파일 이름으로 리소스 ID 구해서 비트맵 파일을 만든다
        int drawableId = res.getIdentifier(mElement.imgName, "drawable", "exam.androidproject");
        mImgElement = BitmapFactory.decodeResource(res, drawableId);

        // 테스트용으로 사이즈 다른 이미지 대충 쓰니까 크기 조절해야함.
        if ((mImgElement.getWidth() != 64) || (mImgElement.getHeight() != 64))
        {
            mImgElement = Bitmap.createScaledBitmap(mImgElement, 64, 64, true);
        }

        /* 테스트용 유닛 생성 */
        for (int xx = 0; xx <= GameCamera.mLogicalWidth; xx += 192)
        {
            for (int yy = 0; yy <= GameCamera.mLogicalHeight; yy += 216)
            {
                arTestUnits.add(new Unit(xx, yy));
            }
        }

        // arTestUnits.add(new Unit(0, 0));
    }

    public ArrayList<Unit> getUnits()
    {
        return arTestUnits;
    }

    public void move()
    {
        x += dx;
        y += dy;
        if ((x < 0) || (x > GameCamera.mLogicalWidth))
        {
            dx = -dx;
        }
        if ((y < 0) || (y > GameCamera.mLogicalHeight))
        {
            dy = -dy;
        }
    }

    // 대충했음
    synchronized public int getLogicFps()
    {
        return logicFps;
    }

    synchronized public void setLogicFps(int logicFps)
    {
        this.logicFps = logicFps;
    }
}
