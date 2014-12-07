package exam.game;

public class Bounds
{
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public Bounds(int x, int y, int width, int height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int left()
    {
        return x;
    }

    public int left(float scale)
    {
        return (int) (x / scale);
    }

    public int right()
    {
        return left() + width;
    }

    public int right(float scale)
    {
        return (int) (left(scale) + (width * scale));
    }

    public int top()
    {
        return y;
    }

    public int top(float scale)
    {
        return (int) (y / scale);
    }

    public int bottom()
    {
        return top() + height;
    }

    public int bottom(float scale)
    {
        return (int) (top(scale) + (height * scale));
    }
}
