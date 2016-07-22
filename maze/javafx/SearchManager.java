package maze.javafx;

import maze.searcher.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class SearchManager {
    private final List<String> searchNames = new ArrayList<>();
    private final List<Function<Random, IMoveAlgorithm>> searches = new ArrayList<>();

    public SearchManager() {
        register("ランダムマウス", (r) -> new RandomMouseAlgorithm(r));
        register("山登り法", (r) -> new HillClimbingAlgorithm(r));
        register("ウォールフォロワ（深さ優先）", (r) -> new WallFollowerAlgorithm());
        register("幅優先", (r) -> new BreadthFirstAlgorithm());
        register("足立法", (r) -> new AdachiAlgorithm(r));
    }

    private void register(String key, Function<Random, IMoveAlgorithm> search) {
        searchNames.add(key);
        searches.add(search);
    }

    public String[] getSearchNames() {
        return searchNames.toArray(new String[searchNames.size()]);
    }

    public Function<Random, IMoveAlgorithm> getSearch(int index) {
        return searches.get(index);
    }
}
