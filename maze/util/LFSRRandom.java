package maze.util;

import java.util.Random;

public class LFSRRandom extends Random {
    private int oldSeed = super.next(32);

    @Override
    protected int next(int bits) {
        int nextSeed = (oldSeed << 1) + super.next(1);
        oldSeed = Math.abs(nextSeed);
        return oldSeed;
    }
}
