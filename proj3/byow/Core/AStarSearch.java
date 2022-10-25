package byow.Core;

import byow.Core.World.Position;
import byow.Core.World.World;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.ArrayList;

public class AStarSearch {

    public AStarSearch(World map) {
        this.mainMap = map;
    }

    World mainMap;

    int maxCol = 80;
    int maxRow = 50;

    Node[][] node = new Node[maxCol][maxRow];
    Node startNode;
    Node goalNode;
    Node currentNode;
    ArrayList<Node> openList = new ArrayList<>();

    boolean goalReached = false;

    public class Node {
        Node parent;
        int col;
        int row;
        int gCost;
        int hCost;
        int fCost;
        boolean start;
        boolean goal;
        boolean wall;
        boolean open;
        boolean checked;

        public Node (int col, int row) {
            this.col = col;
            this.row = row;
        }

        public void setAsStart() {
            start = true;
        }

        public void setAsGoal() {
            goal = true;
        }

        public void setAsOpen() {
            open = true;
        }

        public void setAsChecked() {
            checked = true;
        }

        public void setAsPath(World map) {
            TETile[][] world = map.getWorld();
            world[col][row] = Tileset.FLOWER;
        }

    }

    // maps out the world into a grid that can be searched using A*
    public void createMap(Position avatarPosition, Position enemyPosition, World map) {
        mainMap = map;
        int col = 0;
        int row = 0;

        int avatarX = avatarPosition.getX();
        int avatarY = avatarPosition.getY();

        int enemyX = enemyPosition.getX();
        int enemyY = enemyPosition.getY();

        TETile[][] world = map.getWorld();

        while (col < maxCol && row < maxRow) {
            node[col][row] = new Node(col,row);
            if (world[col][row].description().equals("wall")) {
                node[col][row].wall = true;
            }
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }

        setStartNode(avatarX, avatarY);
        setGoalNode(enemyX, enemyY);

        setCostOnNodes();
    }

    public void setStartNode(int col, int row) {
        node[col][row].setAsStart();
        startNode = node[col][row];
        currentNode = startNode;
    }

    public void setGoalNode(int col, int row) {
        node[col][row].setAsGoal();
        goalNode = node[col][row];
    }

    // uses Manhattan distance heuristic
    // note: is not 100% accurate for giving the shortest path
    public void getCost(Node node) {
        // gets G cost (distance from start node)
        int xDistance = Math.abs(node.col - startNode.col);
        int yDistance = Math.abs(node.row - startNode.row);
        node.gCost = xDistance + yDistance;

        // gets H cost (distance from goal node)
        xDistance = Math.abs(node.col - startNode.col);
        yDistance = Math.abs(node.row - startNode.row);
        node.hCost = xDistance + yDistance;

        // gets F cost (total cost)
        node.fCost = node.gCost + node.hCost;
    }

    private void setCostOnNodes() {
        int col = 0;
        int row = 0;

        while (col < maxCol && row < maxRow) {
            getCost(node[col][row]);
            col++;
            if (col == maxCol) {
                col = 0;
                row++;
            }
        }
    }

    public void search() {
        while (goalReached == false) {
            int col = currentNode.col;
            int row = currentNode.row;
            currentNode.setAsChecked();
            // list of all nodes available to be evaluated
            openList.remove(currentNode);

            // open up the nodes in each direction
            if (row - 1 >= 0) {
                openNode(node[col][row - 1]);
            }
            if (col - 1 >= 0) {
                openNode(node[col - 1][row]);
            }
            if (row + 1 < maxRow) {
                openNode(node[col][row + 1]);
            }
            if (col + 1 < maxCol) {
                openNode(node[col + 1][row]);
            }

            // find the best node to travel to based on F cost, then H cost
            int bestNodeIndex = 0;
            int bestNodeFCost = 9999;

            for (int i = 0; i < openList.size(); i++) {
                // check if this node's F cost is better
                if (openList.get(i).fCost < bestNodeFCost) {
                    bestNodeFCost = openList.get(i).fCost;
                    bestNodeIndex = i;
                }
                // if F cost is equal, check G costs
                else if (openList.get(i).fCost == bestNodeFCost) {
                    if (openList.get(i).gCost < openList.get(bestNodeIndex).gCost) {
                        bestNodeIndex = i;
                    }
                }
            }
            // after loop, get the best node which is our next step
            // error means that the enemy reached the player
            try {
                currentNode = openList.get(bestNodeIndex);
            } catch (IndexOutOfBoundsException a) {
                // creates game over menu
                StdDraw.clear(Color.BLACK);
                StdDraw.setPenColor(Color.WHITE);
                Font font = new Font("Arial", Font.BOLD, 50);
                StdDraw.setFont(font);
                StdDraw.text(maxCol / 2, maxRow / 2, "GAME OVER!");
                StdDraw.show();
                StdDraw.pause(3000);
                System.exit(100);
            }
            // creates path from start to goal if a possible path is found
            if(currentNode == goalNode) {
                goalReached = true;
                getPath();
            }
        }
    }

    public void openNode(Node node) {
        if(node.open == false && node.checked == false && node.wall == false) {
            // if the node is not opened yet, add it to the open list
            node.setAsOpen();
            node.parent = currentNode;
            openList.add(node);
        }
    }

    // backtracks and finds the best path
    public void getPath() {
        Node current = goalNode;
        while(current != startNode) {
            current = current.parent;
            if (current != startNode) {
                current.setAsPath(mainMap);
            }
        }
    }

    // backtracks one node and returns position
    // used to move the enemy 1 tile towards the player
    public Position getParentPosition() {
        Node parent = goalNode.parent;
        if (parent != null) {
            Position p = new Position(parent.col, parent.row);
            return p;
        }
        return null;
    }

}
