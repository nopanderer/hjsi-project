package exam.game;

/**
 * ElementView 테스트용으로 하나 만듦
 * 
 * @author 이상인
 */
public class EDElement
{
    public String name;
    public int    level;
    public int    type;
    public int    dmg;
    public float  rate;
    public String imgName;

    public EDElement()
    {
        name = "불";
        level = 0;
        type = 0;
        dmg = 5;
        rate = 0.5f;
        imgName = "match";
    }

    @Override
    public String toString()
    {
        String element = "<하급> " + name;

        return element;

    }
}
