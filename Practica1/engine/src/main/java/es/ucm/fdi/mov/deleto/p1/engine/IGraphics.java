package es.ucm.fdi.mov.deleto.p1.engine;

public interface IGraphics {

    public int getWidth();

    public int getHeight();

    public void setResolution(int x, int y);

    public void setRefResolution(int x, int y);

    public void translate(int x, int y);

    public void scale(int x, int y);

    public void drawImage(IImage image, int posX, int posY, int scaleX, int scaleY);

    public void clear(int color);

    public void setColor(int color);

    public void setFont(IFont font);

    public void fillCircle(int x, int y, int r);

    public void drawText(String text, int x, int y);

    public IImage newImage(String name);

    public IFont newFont(String fileName, int size, boolean isBold);
}
