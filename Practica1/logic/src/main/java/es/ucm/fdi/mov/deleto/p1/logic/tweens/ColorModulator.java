package es.ucm.fdi.mov.deleto.p1.logic.tweens;

/**
 * TweenTarget that modulates 2 colors so when update gets
 *  on 0 we return initial, 0.5 we return a 50 50 blend of them
 *  and on 1 we return final and everything in between
 */
public class ColorModulator implements ITweenTarget<Integer> {

    private final int _initial;
    private final int _final;
    private int _actual;

    public ColorModulator(int i, int f)
    {
        _initial = i;
        _final = f;
        _actual = _initial;
    }

    @Override
    public void update(double delta) {
        int   r1 = (_initial >> 16) & 0xff;
        int   r2 = (_final >> 16) & 0xff;
        int   g1 = (_initial >> 8) & 0xff;
        int   g2 = (_final >> 8) & 0xff;
        int   b1 = _initial & 0xff;
        int   b2 = _final & 0xff;
        _actual =  0xff << 24 | ((int) (((r2 - r1) * delta) + r1) << 16 | (int) (((g2 - g1) * delta) + g1) << 8 |  (int) (((b2 - b1) * delta) + b1));
    }

    @Override
    public Integer get() {
        return _actual;
    }
}
