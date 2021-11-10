package es.ucm.fdi.mov.deleto.p1.engine;

/**
 * Graphics Engine interface for platform independent simple rendering needs
 */
public interface IGraphics {

    /**
     * Our graphics engine will be given a "logic" canvas size that the application will use
     * independent of real coordinates. We will scale to fit application with margins either up and
     * down or left and right, based on which fits best.
     */

    /**
     * Sets the logic canvas size
     * @param width width of the canvas
     * @param height height of the canvas
     */
    void setResolution(int width, int height);

    /**
     * Getters for width and height
     */
    int getLogicHeight();
    int getLogicWidth();


    /**
     * Sets the opacity for all subsequent rendering requests
     * @param opacity a value between 0 and 1, where 0 means fully transparent and 1 fully opaque
     */
    void setOpacity(float opacity);

    /**
     * Draws an image on the position given with specified scale factors
     * undefined behaviour if scale = 0
     *
     * @param image image to draw
     * @param posX x canvas coordinate
     * @param posY y canvas coordinate
     * @param scaleX horizontal scale factor
     * @param scaleY vertical scale factor
     */
    void drawImage(IImage image, int posX, int posY, float scaleX, float scaleY);

    /**
     * Sets color for subsequent rendering calls
     * @param color
     */
    void setColor(int color);

    /**
     * Sets the font for subsequent draw text calls
     * @param font
     */
    void setFont(IFont font);

    /**
     * Draws a circle filled with the actual color
     * @param x horizontal coordinate of circle's center
     * @param y vertical coordinate of circle's center
     * @param r circle's radius
     */
    void fillCircle(int x, int y, double r);

    /**
     * Draws a rectangle filled with the actual color
     * @param x horizontal coordinate of rectangle's center
     * @param y vertical coordinate of rectangle's center
     * @param w width of whole rectangle
     * @param h height of whole rectangle
     */
    void fillRect(int x, int y, int w, int h);

    /**
     * Draws given string with currently selected font.
     * Will split text on new line characters '\n'
     *
     * @param text string to render
     * @param x horizontal center
     * @param y vertical center
     * @param scale scale to apply to currently selected font
     * @return where a next character would be placed in logical coordinates
     */
    Vec2<Integer> drawText(String text, int x, int y, double scale);

    /**
     * Same as drawText but with 1.0 scale
     */
    Vec2<Integer> drawText(String text, int x, int y);

    /**
     * Factory method to create an Image object
     * @param name path to requested image
     * @return an IImage interface object
     */
    IImage newImage(String name);

    /**
     * Factory method to create a Font object
     * @param fileName path to requested font
     * @param size size in ppt of font to load
     * @param isBold whether the font should be created in bold form or not
     * @return an IImage IFont object
     */
    IFont newFont(String fileName, int size, boolean isBold);
}
