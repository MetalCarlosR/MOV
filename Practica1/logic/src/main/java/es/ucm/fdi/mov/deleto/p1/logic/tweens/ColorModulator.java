package es.ucm.fdi.mov.deleto.p1.logic.tweens;

public class ColorModulator implements ITweenTarget<Integer> {

    int _initial;
    int _final;
    int _actual;

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
