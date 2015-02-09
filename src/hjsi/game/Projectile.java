package hjsi.game;

import android.graphics.Bitmap;

/**
 * 투사체
 * 
 * @author Administrator
 *
 */
public class Projectile extends Unit implements Movable
{

    /**
     * 목표로 한 몹의 인덱스
     */
    private int    target;
    /**
     * 투사체 속도
     */
    private int    speed;
    /**
     * 몹과 충돌했는지
     */
    public boolean isHit;
    /**
     * 임시 타이머 변수
     */
    private long   beforeTime;
    private int    sleep = 15;

    /**
     * 비트맵 크기
     */

    public Projectile(int x, int y, int target, Bitmap face)
    {
        super(x, y, face);
        this.target = target;

        speed = 3;
        isHit = false;

        beforeTime = System.currentTimeMillis();
    }

    @Override
    public void move()
    {
        if (System.currentTimeMillis() - beforeTime > sleep)
            beforeTime = System.currentTimeMillis();
        else
            return;

        /* 충돌검사 */
        if ((x >= targetX() && x <= targetXWidth()) && y >= targetY() && y <= targetYHeight())
            isHit = true;

        /* 유도 알고리즘 */
        if (targetCntrX() < x)
        {
            x -= speed;
            if (targetCntrY() < y)
                y -= speed;
            else if (targetCntrY() > y)
                y += speed;
        }
        if (targetCntrX() > x)
        {
            x += speed;
            if (targetCntrY() < y)
                y -= speed;
            else if (targetCntrY() > y)
                y += speed;
        }
        if (targetCntrX() == x)
        {
            if (targetCntrY() < y)
                y -= speed;
            else if (targetCntrY() > y)
                y += speed;
        }

    }

    private int targetX()
    {
        return GameState.getInstance().Mobs.get(target).x;
    }

    private int targetY()
    {
        return GameState.getInstance().Mobs.get(target).y;
    }

    private int targetCntrX()
    {
        return GameState.getInstance().Mobs.get(target).cntrX;
    }

    private int targetCntrY()
    {
        return GameState.getInstance().Mobs.get(target).cntrY;
    }

    private int targetXWidth()
    {
        return GameState.getInstance().Mobs.get(target).x + GameState.getInstance().Mobs.get(target).width;
    }

    private int targetYHeight()
    {
        return GameState.getInstance().Mobs.get(target).y + GameState.getInstance().Mobs.get(target).height;
    }

    @Override
    public void action()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void touch()
    {
        // TODO Auto-generated method stub
    }
}
