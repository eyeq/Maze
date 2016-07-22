package maze.javafx;

import maze.generator.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

public class GenerationManager {
    private final List<String> generationNames = new ArrayList<>();
    private final List<BiFunction<int[], Random, IMazeBuilder>> generations = new ArrayList<>();

    public GenerationManager() {
        register("棒倒し法", (wh, r) -> new DefeatStickMazeBuilder(wh[0], wh[1], r));
        register("穴掘り法（バックトラック法）", (wh, r) -> new DigHoleMazeBuilder(wh[0], wh[1], r));
        register("壁伸ばし法", (wh, r) -> new ExtendWallMazeBuilder(wh[0], wh[1], r));
        register("バイナリツリー法", (wh, r) -> new BinaryTreeMazeBuilder(wh[0], wh[1], r));
        register("クラスカル法", (wh, r) -> new KruskalMazeBuilder(wh[0], wh[1], r));
        register("プリム法", (wh, r) -> new PrimMazeBuilder(wh[0], wh[1], r));
        register("再帰的分割法", (wh, r) -> new RecursiveDivisionMazeBuilder(wh[0], wh[1], r));
    }

    private void register(String key, BiFunction<int[], Random, IMazeBuilder> generation) {
        generationNames.add(key);
        generations.add(generation);
    }

    public String[] getGenerationNames() {
        return generationNames.toArray(new String[generationNames.size()]);
    }

    public BiFunction<int[], Random, IMazeBuilder> getGeneration(int index) {
        return generations.get(index);
    }
}
