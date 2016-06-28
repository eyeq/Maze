package maze.generator;

import maze.MazeData;

public interface IMazeBuilder {
    void buildFill();
    void buildFrame();
    void buildMaze();
    MazeData getMazeData();
}
