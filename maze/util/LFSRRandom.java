package maze.util;

import java.util.Random;

public class LFSRRandom extends Random {
    private int oldSeed = super.next(32);

    public LFSRRandom() {
        super();
    }

    public LFSRRandom(long seed) {
        super(seed);
    }

    @Override
    protected int next(int bits) {
        int nextSeed = (oldSeed << 1);
        // nextSeed += super.next(1);
        nextSeed += ((oldSeed >> 4) & 1) ^ ((oldSeed >> 7) & 1);
        oldSeed = Math.abs(nextSeed);
        return oldSeed >>> (31 - bits);
    }
}
