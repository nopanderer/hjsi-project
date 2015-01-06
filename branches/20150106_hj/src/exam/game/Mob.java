package exam.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/*
 * Mob 클래스
 * Tower와 Mob 클래스는 먼저 만들고
 * 두 클래스의 공통점들이 명확히 보일 때
 * 추상화 시키는게 나을 것 같아서
 * Unit 클래스를 만들지 않음
 */
public class Mob extends Bounds
{
    /* 처리를 위한 변수 */
    public String mCaption;

    /* 게임 속성 */
    private int   hp;      // 몹 체력(지금 필요없음)
    private int   x, y;    // 몹 위치
    private int   step = 5;
    private int   width, height;       // 가로 세로 크기
    private int   range;        // 타격 범위
    private int   oldX, oldY;
    Bitmap        mImgElement;

    public Mob(int x, int y, int width, int height)
    {
        super(x, y, width, height);
        this.mImgElement = mImgElement;

        // x = getX();
        // y = getY();
    }

    // private static final int UP = 0;
    // private static final int DOWN = 1;
    // private static final int LEFT = 2;
    // private static final int RIGHT = 3;

    public String getBitmapKey()
    {
        return mCaption;
    }

    public void draw(Canvas canvas, Bitmap face)
    {
        canvas.drawBitmap(face, x - GameCamera.getInstance().x(), y - GameCamera.getInstance().y(), null);
    }

    public void move()
    {

        System.out.println("x: " + x + " oldX: " + oldX);
        System.out.println("y: " + y + " oldY: " + oldY);

        if (x == oldX && y + step <= GameCamera.getInstance().worldHeight() - 900)
        {
            y += step;
        }

        else if (x + step <= GameCamera.getInstance().worldWidth() - 1500 && y == GameCamera.getInstance().worldHeight() - 900)
        {
            x += step;
        }

        else if (x == GameCamera.getInstance().worldWidth() - 1500 && y - step >= oldY)
        {
            y -= step;
        }

        else if (x - step >= oldX && y == oldY)
        {
            x -= step;
        }
    }

}
