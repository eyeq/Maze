package maze.javafx;

import maze.generator.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class GenerationController {
    private final List<String> generationNames;
    private final List<BiFunction<int[], Random, IMazeBuilder>> generations;

    public GenerationController() {
        generationNames = Arrays.asList(
                "棒倒し法",
                "穴掘り法（バックトラック法）",
                "壁伸ばし法",
                "バイナリツリー法",
                "クラスカル法",
                "プリム法"
        );
        generations = Arrays.asList(
                (wh, r) -> new DefeatStickMazeBuilder(wh[0], wh[1], r),
                (wh, r) -> new DigHoleMazeBuilder(wh[0], wh[1], r),
                (wh, r) -> new ExtendWallMazeBuilder(wh[0], wh[1], r),
                (wh, r) -> new BinaryTreeMazeBuilder(wh[0], wh[1], r),
                (wh, r) -> new KruskalMazeBuilder(wh[0], wh[1], r),
                (wh, r) -> new PrimMazeBuilder(wh[0], wh[1], r)
        );
    }

    public String[] getGenerationNames() {
        return generationNames.toArray(new String[generationNames.size()]);
    }

    public BiFunction<int[], Random, IMazeBuilder> getGeneration(int index) {
        return generations.get(index);
    }
}
