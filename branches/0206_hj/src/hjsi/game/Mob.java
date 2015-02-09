package hjsi.game;

import android.graphics.Bitmap;

/**
 * Mob 클래스
 * 
 */
public class Mob extends Unit implements Movable, Attackable
{

    /**
     * 최대 체력
     */
    private int    hpMax;
    /**
     * 현재 체력
     */
    private int    hp;
    /**
     * 이동할 거리
     */
    private int    speed;
    /**
     * 공격력
     */
    private int    damage;
    /**
     * 공격속도
     */
    private float  attackSpeed;
    /**
     * 방어력
     */
    private int    armor;
    /**
     * 사정거리
     */
    private int    range;
    /**
     * 웨이브
     */
    private int    wave;
    /**
     * 몹이 생성 되었는가
     */
    public boolean created;
    /**
     * 몹이 죽었는가
     */
    public boolean dead;
    /**
     * 몇 바퀴 돌았나
     */
    public int     lap;

    /* 리젠 */
    private long   beforeTime;
    private int    sleep = 10;
    /**
     * 초기 생성 위치
     */
    private int    oldX, oldY;

    public Mob(int x, int y, Bitmap face, int wave)
    {
        super(x, y, face);

        created = false;
        dead = false;
        lap = 0;
        this.wave = wave;
        beforeTime = System.currentTimeMillis();

        speed = 1;
        range = 400;

        oldX = x;
        oldY = y;

    }

    /*
     * (non-Javadoc)
     * @see hjsi.game.Unit#touch()
     */
    @Override
    public void touch()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void attack()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void move()
    {
        // TODO Auto-generated method stub
        // 10밀리세컨드 마다 5 픽셀씩 이동

        if (System.currentTimeMillis() - beforeTime > sleep)
            beforeTime = System.currentTimeMillis();
        else
            return;

        if (x == oldX && y == oldY)
            lap++;

        // 아래로
        if (x == oldX && y + speed <= 2160 - 900)
        {
            y += speed;
            cntrY += speed;
        }
        // 오른쪽
        else if (x + speed <= 3840 - 1500 && y == 2160 - 900)
        {
            x += speed;
            cntrX += speed;
        }
        // 위로
        else if (x == 3840 - 1500 && y - speed >= oldY)
        {
            y -= speed;
            cntrY -= speed;
        }
        // 왼쪽
        else if (x - speed >= oldX && y == oldY)
        {
            x -= speed;
            cntrX -= speed;
        }

    }

    @Override
    public void action()
    {
        // TODO Auto-generated method stub

    }
}
