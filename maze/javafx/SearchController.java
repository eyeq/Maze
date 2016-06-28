package maze.javafx;

import maze.searcher.IMoveMethod;
import maze.searcher.RandomMouse;
import maze.searcher.WallFollower;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class SearchController {
    private final List<String> searchNames;
    private final List<Function<Random, IMoveMethod>> searches;

    public SearchController() {
        searchNames = Arrays.asList(
                "ランダムマウス",
                "ウォールフォロワ（深さ優先）"
        );
        searches = Arrays.asList(
                (r) -> (new RandomMouse(r)),
                (r) -> (new WallFollower())
        );
    }

    public String[] getSearchNames() {
        return searchNames.toArray(new String[searchNames.size()]);
    }

    public Function<Random, IMoveMethod> getSearch(int index) {
        return searches.get(index);
    }
}
