package exam.game;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/*
 * 게임 오브젝트의 기본이 되는 추상 클래스(가 될 예정)
 */
public class Unit extends Bounds
{
    /* 처리를 위한 변수 */
    public String  mCaption;
    private Paint  mPaintText;
    private Paint  mPaintRect;

    /* 그림으로 그려질 비트맵 */
    private Bitmap face;

    /* 게임 속성 */

    public Unit(int x, int y)
    {
        super(x, y, 192, 216);

        mCaption = "(" + x + ", " + y + ")";

        mPaintText = new Paint();
        mPaintText.setColor(Color.DKGRAY);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextSize(25);

        mPaintRect = new Paint();
        mPaintRect.setColor(Color.DKGRAY);
        mPaintRect.setStyle(Style.STROKE);
        mPaintRect.setAntiAlias(true);

        // 텍스트 가로 사이즈 구하기
        // mWidth = (int) Math.ceil(mPaint.measureText(mCaption));

        /* 뻘짓 */
        face = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(face);
        canvas.drawRect(0, 0, width, height, mPaintRect);
        canvas.drawText(mCaption, 0, 25, mPaintText);
        face = Bitmap.createBitmap(face); // 개뻘짓 (immutable과 mutable 비트맵에 성능 차이가 있나싶어서 해봄)
    }

    public Bitmap getFace()
    {
        return face;
    }

    public void action()
    {
    }
}
