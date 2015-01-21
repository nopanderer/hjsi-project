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
    private int    hp;        // 몹 체력
    private int    x, y;      // 위치
    private int    width, height;                          // 크기
    private int    step  = 5;    // 5픽셀씩
    private int    sleep = 10;   // 10milsec
    private int    range = 400;  // 타격 범위
    private int    oldX, oldY;   // 초기 생성 위치

    private long   beforeTime;   // 움직이기 전 시간
    private int    wave;

    public Bitmap  face;

    public boolean created;      // 몹이 생성 되었는가
    public boolean dead;         // 몹이 죽었는가
    public int     lap;          // 몇 바퀴 돌았나(2바퀴 돌면 몹 자체 파괴)

    public Mob(int x, int y, int width, int height, int wave)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        created = false;
        dead = false;
        lap = 0;
        this.wave = wave;

        beforeTime = System.currentTimeMillis();
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
        canvas.drawBitmap(AppManager.getInstance().getBitmap("mob" + wave), x - GameCamera.getInstance().x(), y - GameCamera.getInstance().y(), null);
    }

    public void move()
    {
        // 10밀리세컨드 마다 5 픽셀씩 이동
        if (System.currentTimeMillis() - beforeTime > sleep)
            beforeTime = System.currentTimeMillis();
        else
            return;

        if (x == oldX && y == oldY)
            lap++;

        // 아래로
        if (x == oldX && y + step <= GameCamera.getInstance().worldHeight() - 900)
        {
            y += step;
        }
        // 오른쪽
        else if (x + step <= GameCamera.getInstance().worldWidth() - 1500 && y == GameCamera.getInstance().worldHeight() - 900)
        {
            x += step;
        }
        // 위로
        else if (x == GameCamera.getInstance().worldWidth() - 1500 && y - step >= oldY)
        {
            y -= step;
        }
        // 왼쪽
        else if (x - step >= oldX && y == oldY)
        {
            x -= step;
        }

    }

}
