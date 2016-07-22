package maze.searcher;

import maze.MazeData;

import java.awt.*;
import java.util.*;
import java.util.List;

public class BreadthFirstAlgorithm implements IMoveAlgorithm {
    private Direction prevDirection = Direction.NORTH_EAST;
    private Node node = new Node(null, null);

    @Override
    public Direction getMoveDirection(Point current, Point entrance, Point destination, MazeData mazeData, List<Direction> canMoves) {
        List<Direction> canMovesNoDiagonal = new ArrayList<>();
        for(Direction direction : new Direction[]{Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH}) {
            if(canMoves.contains(direction)) {
                canMovesNoDiagonal.add(direction);
            }
        }
        prevDirection = getMoveDirection(current, prevDirection.reverse(), canMovesNoDiagonal, true);
        return prevDirection;
    }

    protected Direction getMoveDirection(Point current, Direction prevDirectionReverse, List<Direction> canMoves, boolean isBack) {
        if(canMoves.size() == 1 && node.parent == null && node.point == null) {
            node.point = new Point(current);
            node = new Node(node, canMoves.get(0));
            node.parent.children.add(node);
            return node.directionFromParent;
        }
        // 一本道
        if(canMoves.size() == 2 && !(node.parent == null && node.point == null) && !current.equals(node.point)) {
            for(Direction direction : canMoves) {
                if(direction != prevDirectionReverse) {
                    return direction;
                }
            }
        }
        // 分岐路
        if(canMoves.size() != 1) {
            // 未到達の分岐路
            if(node.point == null) {
                node.point = new Point(current);
                for(Direction direction : canMoves) {
                    if(direction == prevDirectionReverse) {
                        continue;
                    }
                    node.children.add(new Node(node, direction));
                }
                if(node.parent != null) {
                    boolean isDead = true;
                    Node temp = node.parent;
                    while(temp != null) {
                        if(temp.children.size() > 1) {
                            isDead = false;
                            break;
                        }
                        temp = temp.parent;
                    }
                    if(isDead) {
                        node.parent = null;
                    } else if(isBack){
                        node.directionToParent = prevDirectionReverse;
                        Direction direction = node.directionToParent;
                        node = node.parent;
                        return direction;
                    }
                }
            }
            // 到達済みの分岐路
            if(!node.children.isEmpty()) {
                Direction next = prevDirectionReverse;
                for(int i = 0; i < 4; i++) {
                    int index = canMoves.indexOf(next) + 1;
                    if(index == canMoves.size()) {
                        index = 0;
                    }
                    next = canMoves.get(index);

                    if(node.directionToParent == next) {
                        node = node.parent;
                        break;
                    }
                    for(Node child : node.children) {
                        if(child.directionFromParent == next) {
                            node = child;
                            i = 4;
                            break;
                        }
                    }
                }
                return next;
            }
        }
        // 行き止まり
        if(node.parent == null) {
            return null;
        }
        Direction direction = node.directionToParent == null ? prevDirectionReverse : node.directionToParent;
        node.parent.children.remove(node);
        node = node.parent;
        return direction;
    }

    private class Node {
        private Node parent;
        private List<Node> children;
        private Direction directionFromParent;
        private Direction directionToParent;
        private Point point;

        Node(Node parent, Direction direction) {
            this.parent = parent;
            this.children = new ArrayList<>();
            this.directionFromParent = direction;
        }
    }
}
