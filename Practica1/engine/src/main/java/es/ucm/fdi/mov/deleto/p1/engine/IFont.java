package es.ucm.fdi.mov.deleto.p1.engine;

public interface IFont {
    /**
     * Might be used to know how much pixels skip to safely offset of canvas borders.
     * For example: Graphics.GetWindowLogicHeight()-(font.getSize()/2);
     *              would give back the 'y' position where the font would be rendered at the bottom of
     *              the screen
     *
     * @return font logic size.
     */
    int getSize();
}
