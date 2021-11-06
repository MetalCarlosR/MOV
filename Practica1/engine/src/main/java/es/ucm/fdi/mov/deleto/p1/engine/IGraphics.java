package es.ucm.fdi.mov.deleto.p1.engine;


import es.ucm.fdi.mov.deleto.p1.engine.Vec2;

public interface IGraphics {

    public int getLogicWidth();

    public int getLogicHeight();

    public void setResolution(int x, int y);

    public void setOpacity(float opacity);

    public void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY);

    public void clear(int color);

    public void setColor(int color);

    public void setFont(IFont font);

    public void fillCircle(int x, int y, double r);

    public void fillRect(int x, int y, int w, int h);

    public Vec2<Integer> drawText(String text, int x, int y);

    // Pair<Integer, Integer>
    public Vec2<Integer> drawText(String text, int x, int y, double scale);

    public IImage newImage(String name);

    public IFont newFont(String fileName, int size, boolean isBold);
}
