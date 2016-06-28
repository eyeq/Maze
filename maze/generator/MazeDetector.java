package maze.generator;

import maze.MazeData;

public class MazeDetector {
    private IMazeBuilder builder;

    public MazeDetector(IMazeBuilder mazeBuilder) {
        builder = mazeBuilder;
    }

    public MazeData construct() {
        builder.buildFill();
        builder.buildFrame();
        builder.buildMaze();
        return builder.getMazeData();
    }
}
