package exam.game;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/*
 * Mob 클래스
 * Tower와 Mob 클래스는 먼저 만들고
 * 두 클래스의 공통점들이 명확히 보일 때
 * 추상화 시키는게 나을 것 같아서
 * Unit 클래스를 만들지 않음
 * (Bounds 상속 받는거 없앰)
 */
public class Mob
{
    /* 게임 속성 */
    private int    hp;         // 몹 체력
    private int    x, y;       // 위치
    private int    width, height; // 크기
    private int    step  = 5;    // 몹 이동 속도(타이머 만들어지면 초당 속도로 바꿔야함)
    private int    range = 400;  // 타격 범위
    private int    oldX, oldY;   // 초기 생성 위치
    private Bitmap face;         //

    public Mob(int x, int y, int width, int height, Bitmap face)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.face = face;
        // 몹이 초기에 생성되는 위치
        oldX = x;
        oldY = y;

    }

    public void draw(Canvas canvas)
    {
        Paint showRange = new Paint();
        showRange.setAntiAlias(true);
        showRange.setStyle(Paint.Style.STROKE);    // 원의 윤곽선만 그림
        showRange.setStrokeWidth(3);   // 윤곽선 두께
        showRange.setAlpha(0x00);  // 원 안을 투명하게
        showRange.setColor(Color.GREEN);   // 윤곽선은 초록색
        canvas.drawCircle(x - GameCamera.getInstance().x() + 32, y - GameCamera.getInstance().y() + 32, range, showRange);
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
