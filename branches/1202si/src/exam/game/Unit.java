package exam.game;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;

/*
 * 게임 오브젝트의 기본이 되는 추상 클래스(가 될 예정)
 */
public class Unit
{
    /* 처리를 위한 변수 */
    private String mCaption;
    private Paint  mPaint;
    private Paint  mPaint2;
    private Rect   mBound = new Rect();

    /* 게임 속성 */
    private int    posX, posY;
    private int    mWidth, mHeight;

    public Unit(int x, int y)
    {
        posX = x;
        posY = y;
        mCaption = "(" + posX + ", " + posY + ")";
        mWidth = 192;
        mHeight = 216;

        mPaint = new Paint();
        mPaint.setColor(Color.DKGRAY);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(25);

        mPaint2 = new Paint();
        mPaint2.setColor(Color.DKGRAY);
        mPaint2.setStyle(Style.STROKE);
        mPaint2.setAntiAlias(true);

        // 텍스트 가로 사이즈 구하기
        // mWidth = (int) Math.ceil(mPaint.measureText(mCaption));
    }

    public void draw(Canvas canvas, GameCamera camera)
    {
        camera.getPhysicalBound(mBound, posX, posY, mWidth, mHeight);
        if (camera.isInScreen(mBound))
        {
            canvas.drawRect(mBound, mPaint2);
            canvas.drawText(mCaption, mBound.left, mBound.top, mPaint);
        }
    }

    public void action()
    {
    }
}
