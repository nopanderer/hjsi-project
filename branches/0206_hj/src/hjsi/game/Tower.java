package hjsi.game;

import android.graphics.Bitmap;

public class Tower extends Unit
{
    public String            name;
    /**
     * 공격력
     */
    public int               damage;
    /**
     * 공격속도
     */
    public float             attackSpeed;
    /**
     * 사정거리
     */
    private int              range;
    /**
     * 등급
     */
    public int               type;
    public String            imgName;

    private static final int PRIMITIVE = 0;
    private static final int BASIC     = 1;
    private static final int SPECIAL   = 2;
    private static final int MIGHTY    = 3;
    private static final int TOP       = 4;
    private static final int LEGEND    = 5;
    private static final int HIDDEN    = 6;

    public Tower()
    {
        super();
        name = "불";
        type = 0;
        damage = 5;
        attackSpeed = 0.5f;
        imgName = "element_match";
    }

    public Tower(int x, int y, Bitmap face)
    {
        super(x, y, face);
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
