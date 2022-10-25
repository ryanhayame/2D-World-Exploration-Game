package byow.Core.World;

import byow.Core.AStarSearch;
import byow.Core.GameState;
import byow.Core.Input.KeyboardInputSource;
import byow.Core.Input.StringInputDevice;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.time.Year;
import java.util.Random;

import static byow.Core.RandomUtils.uniform;

public class World implements Serializable {
    private final int height;
    private final int width;
    public TETile[][] world;

    private Position avatarPosition;

    private Position enemyPosition;

    public boolean togglePathing = false;

    public boolean readyToQuit = false;

    public World(int height, int width) {
        // creates a new world with given height and width
        this.height = height;
        this.width = width;
        this.world = new TETile[width][height];
        // sets all tiles in the world to NOTHING tiles
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public TETile[][] getWorld() {
        return world;
    }

    public void createSpawn(long seed) {
        Random random = new Random(seed);
        int xPosition = uniform(random, 0, width - 8);
        int yPosition = uniform(random, 0, height - 8);
        Position position = new Position(xPosition, yPosition);
        createSpawnRoom(random, position);
    }

    // creates spawn room
    public void createSpawnRoom(Random random, Position position) {
        // min wall size = 4, max wall size = 10
        int xPosition = position.getX();
        int yPosition = position.getY();
        int randomWidth = uniform(random, 4, 11);
        int randomLength = uniform(random, 4, 11);
        // creates walls and floor of spawn room
        for (int x = xPosition; x < randomWidth + xPosition; x++) {
            for (int y = yPosition; y < randomLength + yPosition; y++) {
                if (x == xPosition || x == randomWidth + xPosition - 1) {
                    // left and right walls
                    world[x][y] = Tileset.WALL;
                } else if (y == yPosition || y == randomLength + yPosition - 1) {
                    // bottom and top walls
                    world[x][y] = Tileset.WALL;
                } else {
                    world[x][y] = Tileset.FLOOR;
                }
            }
        }
        setSpawnPoint(random, position, randomWidth, randomLength);
        // for each side of the room
        for (int i = 1; i <= 4; i++) {
            // 90% chance for each wall to generate a hallway
            int hallSpawnRate = uniform(random, 0, 10);
            if (hallSpawnRate != 0) {
                Position testPositioning = new Position(xPosition + 1, yPosition + 1);
                createHallway(random, testPositioning, randomWidth, randomLength, i);
            }
        }
    }

    public void setSpawnPoint(Random random, Position position, int w, int l) {
        // creates an avatar tile somewhere within the spawn room
        int xPosition = position.getX();
        int yPosition = position.getY();
        int XShift = uniform(random, 1, w - 1);
        int YShift = uniform(random, 1, l - 1);
        world[xPosition + XShift][yPosition + YShift] = Tileset.AVATAR;
        avatarPosition = new Position(xPosition + XShift, yPosition + YShift);
    }

    public Position getAvatarPosition() {
        return this.avatarPosition;
    }

    // position = position of bottom left corner of room (floor, not wall)
    public void createHallway(Random random, Position position, int w, int l, int side) {
        int xPosition = position.getX();
        int yPosition = position.getY();
        // get random hallway length
        int hallLength = uniform(random, 2, 11);
        // side 1 = down, side 2 = left, side 3 = up, side 4 = right
        if (side == 1 || side == 3) {
            // get random x position along wall to make hallway
            int x = uniform(random, 0, w - 2);
            // create hallway depending on side
            if (side == 1) {
                // scout out area under potential hallway
                Position hallwayEntrance = new Position(xPosition + x, yPosition - 1);
                if (!validPotentialTiles(hallwayEntrance, hallLength, side, 1, 1, 1, 1)) {
                    return;
                }
                // create hallway
                for (int y = yPosition - 1; y > yPosition - 1 - hallLength; y--) {
                    world[xPosition + x - 1][y] = Tileset.WALL;
                    world[xPosition + x][y] = Tileset.FLOOR;
                    world[xPosition + x + 1][y] = Tileset.WALL;
                    // save end of hallway position
                    if (y == yPosition - hallLength) {
                        // possibly extend hallway
                        Position endPosition = new Position(xPosition + x, y);
                        extendHallway(random, endPosition, 1);
                    }
                }
            } else if (side == 3) {
                Position hallwayEntrance = new Position(xPosition + x, yPosition + l - 2);
                if (!validPotentialTiles(hallwayEntrance, hallLength, side, 1, 1, 1, 1)) {
                    return;
                }
                for (int y = yPosition + l - 2; y < yPosition + l - 2 + hallLength; y++) {
                    world[xPosition + x - 1][y] = Tileset.WALL;
                    world[xPosition + x][y] = Tileset.FLOOR;
                    world[xPosition + x + 1][y] = Tileset.WALL;
                    // save end of hallway position
                    if (y == yPosition + l + hallLength - 3) {
                        // possibly extend hallway
                        Position endPosition = new Position(xPosition + x, y);
                        extendHallway(random, endPosition, 3);
                    }
                }
            } else {
                return;
            }
        } else if (side == 2 || side == 4) {
            // get random y position along wall to make hallway
            int y = uniform(random, 0, l - 2);
            // create hallway depending on side
            if (side == 2) {
                Position hallwayEntrance = new Position(xPosition - 1, yPosition + y);
                if (!validPotentialTiles(hallwayEntrance, hallLength, side, 1, 1, 1, 1)) {
                    return;
                }
                for (int x = xPosition - 1; x > xPosition - 1 - hallLength; x--) {
                    world[x][yPosition + y - 1] = Tileset.WALL;
                    world[x][yPosition + y] = Tileset.FLOOR;
                    world[x][yPosition + y + 1] = Tileset.WALL;
                    // save end of hallway position
                    if (x == xPosition - hallLength) {
                        // possibly extend hallway
                        Position endPosition = new Position(x, yPosition + y);
                        extendHallway(random, endPosition, 2);
                    }
                }
            } else if (side == 4) {
                Position hallwayEntrance = new Position(xPosition + w - 2, yPosition + y);
                if (!validPotentialTiles(hallwayEntrance, hallLength, side, 1, 1, 1, 1)) {
                    return;
                }
                for (int x = xPosition + w - 2; x < xPosition + w - 2 + hallLength; x++) {
                    world[x][yPosition + y - 1] = Tileset.WALL;
                    world[x][yPosition + y] = Tileset.FLOOR;
                    world[x][yPosition + y + 1] = Tileset.WALL;
                    // save end of hallway position
                    if (x == xPosition + w + hallLength - 3) {
                        // possibly extend hallway
                        Position endPosition = new Position(x, yPosition + y);
                        extendHallway(random, endPosition, 4);
                    }
                }
            } else {
                return;
            }
        }
    }

    // use to avoid array index out of bounds exception
    // returns false if out of bounds
    public boolean checkBounds(int x, int y) {
        if (x < 0 || x > width - 1) {
            return false;
        }
        if (y < 0 || y > height - 1) {
            return false;
        }
        return true;
    }

    // smallX = size of the left wall for hallways/rooms (when side == 1 or 3)
    // bigX = size of the right wall for hallways/rooms (when side == 1 or 3)
    // smallY = size of the more negativeY wall ... (when side == 2 or 4)
    // bigY = size of the more positiveY wall ... (when side == 2 or 4)
    // smallX, bigX, smallY, and bigY are all 1 for hallways
    public boolean validPotentialTiles(Position hallwayEntrance, int hallLength, int side, int smallX, int bigX, int smallY, int bigY) {
        int xPosition = hallwayEntrance.getX();
        int yPosition = hallwayEntrance.getY();
        if (side == 1) {
            // checks all tiles hallway will be built over
            for (int x = xPosition - smallX; x <= xPosition + bigX; x++) {
                for (int y = yPosition - 1; y > yPosition - hallLength; y--) {
                    // stops creating of hallway if there is something already there
                    if (!checkBounds(x, y)) {
                        return false;
                    }
                    if (world[x][y] != Tileset.NOTHING) {
                        return false;
                    }
                }
            }
            return true;
        } else if (side == 2) {
            // checks all tiles hallway will be built over
            for (int x = xPosition - 1; x > xPosition - hallLength; x--) {
                for (int y = yPosition - smallY; y <= yPosition + bigY; y++) {
                    // stops creating of hallway if there is something already there
                    if (!checkBounds(x, y)) {
                        return false;
                    }
                    if (world[x][y] != Tileset.NOTHING) {
                        return false;
                    }
                }
            }
            return true;
        } else if (side == 3) {
            // checks all tiles hallway will be built over
            for (int x = xPosition - smallX; x <= xPosition + bigX; x++) {
                for (int y = yPosition + 1; y < yPosition + hallLength; y++) {
                    // stops creating of hallway if there is something already there
                    if (!checkBounds(x, y)) {
                        return false;
                    }
                    if (world[x][y] != Tileset.NOTHING) {
                        return false;
                    }
                }
            }
            return true;
        } else if (side == 4) {
            // checks all tiles hallway will be built over
            for (int x = xPosition + 1; x < xPosition + hallLength; x++) {
                for (int y = yPosition - smallY; y <= yPosition + bigY; y++) {
                    // stops creating of hallway if there is something already there
                    if (!checkBounds(x, y)) {
                        return false;
                    }
                    if (world[x][y] != Tileset.NOTHING) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void extendHallway(Random random, Position hallEndPosition, int enterDirection) {
        int xPosition = hallEndPosition.getX();
        int yPosition = hallEndPosition.getY();
        // 11% chance to end hall, ~44% change to bend hall, 44% chance to create room
        int hallExtendOption = uniform(random, 0, 9);
        // end hallway if 0
        if (hallExtendOption == 0) {
            world[xPosition][yPosition] = Tileset.WALL;
        }
        // bend hallway if even number
        else if (hallExtendOption % 2 == 0) {
            int hallBendDirection = uniform(random, 1, 5);
            createHallBend(random, hallEndPosition, hallBendDirection, enterDirection);
        }
        // create room if odd number
        else {
            createRoom(random, hallEndPosition, enterDirection);
        }
    }

    public void createHallBend(Random random, Position hallEndPosition, int hallBendDirection, int enterDirection) {
        int xPosition = hallEndPosition.getX();
        int yPosition = hallEndPosition.getY();
        // checks if hall is trying to bend in same direction that it came from
        if (hallBendDirection - enterDirection == 2 || hallBendDirection - enterDirection == -2) {
            world[xPosition][yPosition] = Tileset.WALL;
            return;
        }
        // check to make sure a valid wall can be built at the bend
        for (int i = -1; i <= 1; i++) {
            if (enterDirection == 1) {
                if (!checkBounds(xPosition + i, yPosition - 1)) {
                    return;
                }
                if (world[xPosition + i][yPosition - 1] != Tileset.NOTHING) {
                    world[xPosition][yPosition] = Tileset.WALL;
                    return;
                } else {
                    world[xPosition + i][yPosition - 1] = Tileset.WALL;
                }
            } else if (enterDirection == 2) {
                if (!checkBounds(xPosition - 1, yPosition + i)) {
                    return;
                }
                if (world[xPosition - 1][yPosition + i] != Tileset.NOTHING) {
                    world[xPosition][yPosition] = Tileset.WALL;
                    return;
                } else {
                    world[xPosition - 1][yPosition + i] = Tileset.WALL;
                }
            } else if (enterDirection == 3) {
                if (!checkBounds(xPosition + i, yPosition + 1)) {
                    return;
                }
                if (world[xPosition + i][yPosition + 1] != Tileset.NOTHING) {
                    world[xPosition][yPosition] = Tileset.WALL;
                    return;
                } else {
                    world[xPosition + i][yPosition + 1] = Tileset.WALL;
                }
            } else if (enterDirection == 4) {
                if (!checkBounds(xPosition + 1, yPosition + i)) {
                    return;
                }
                if (world[xPosition + 1][yPosition + i] != Tileset.NOTHING) {
                    world[xPosition][yPosition] = Tileset.WALL;
                    return;
                } else {
                    world[xPosition + 1][yPosition + i] = Tileset.WALL;
                }
            }
        }
        // build hallway out of bend
        createHallway(random, hallEndPosition, 3, 3, hallBendDirection);
    }

    // creates rooms
    public void createRoom(Random random, Position hallEndPosition, int enterDirection) {
        int xPosition = hallEndPosition.getX();
        int yPosition = hallEndPosition.getY();
        int hallLength = uniform(random, 4, 11);
        int randomSmallX = uniform(random, 2, 6);
        int randomBigX = uniform(random, 2, 6);
        int randomSmallY = uniform(random, 2, 6);
        int randomBigY = uniform(random, 2, 6);
        // 999 = temporary values
        // new position of bottom left corner of room (floor, not wall)
        int newXPosition = 999;
        int newYPosition = 999;
        // width and length of new room (needed to create more hallways)
        int w = 999;
        int l = 999;
        // check if a valid room can be built there, if not, close the hallway
        if (!validPotentialTiles(hallEndPosition, hallLength, enterDirection, randomSmallX, randomBigX, randomSmallY, randomBigY)) {
            world[xPosition][yPosition] = Tileset.WALL;
            return;
        }
        // create room
        if (enterDirection == 1) {
            // checks all tiles hallway will be built over
            for (int x = xPosition - randomSmallX; x <= xPosition + randomBigX; x++) {
                for (int y = yPosition; y > yPosition - hallLength; y--) {
                    if (x == xPosition - randomSmallX || x == xPosition + randomBigX) {
                        // left and right walls
                        world[x][y] = Tileset.WALL;
                    } else if (y == yPosition || y == yPosition - hallLength + 1) {
                        // bottom and top walls
                        world[x][y] = Tileset.WALL;
                    } else {
                        world[x][y] = Tileset.FLOOR;
                    }
                }
            }
            newXPosition = xPosition - randomSmallX;
            newYPosition = yPosition - hallLength + 1;
        } else if (enterDirection == 2) {
            // checks all tiles hallway will be built over
            for (int x = xPosition; x > xPosition - hallLength; x--) {
                for (int y = yPosition - randomSmallY; y <= yPosition + randomBigY; y++) {
                    if (x == xPosition || x == xPosition - hallLength + 1) {
                        // left and right walls
                        world[x][y] = Tileset.WALL;
                    } else if (y == yPosition - randomSmallY || y == yPosition + randomBigY) {
                        // bottom and top walls
                        world[x][y] = Tileset.WALL;
                    } else {
                        world[x][y] = Tileset.FLOOR;
                    }
                }
            }
            newXPosition = xPosition - hallLength + 1;
            newYPosition = yPosition - randomSmallY;
        } else if (enterDirection == 3) {
            // checks all tiles hallway will be built over
            for (int x = xPosition - randomSmallX; x <= xPosition + randomBigX; x++) {
                for (int y = yPosition; y < yPosition + hallLength; y++) {
                    if (x == xPosition - randomSmallX || x == xPosition + randomBigX) {
                        // left and right walls
                        world[x][y] = Tileset.WALL;
                    } else if (y == yPosition || y == yPosition + hallLength - 1) {
                        // bottom and top walls
                        world[x][y] = Tileset.WALL;
                    } else {
                        world[x][y] = Tileset.FLOOR;
                    }
                }
            }
            newXPosition = xPosition - randomSmallX;
            newYPosition = yPosition;
        } else if (enterDirection == 4) {
            // checks all tiles hallway will be built over
            for (int x = xPosition; x < xPosition + hallLength; x++) {
                for (int y = yPosition - randomSmallY; y <= yPosition + randomBigY; y++) {
                    if (x == xPosition || x == xPosition + hallLength - 1) {
                        // left and right walls
                        world[x][y] = Tileset.WALL;
                    } else if (y == yPosition - randomSmallY || y == yPosition + randomBigY) {
                        // bottom and top walls
                        world[x][y] = Tileset.WALL;
                    } else {
                        world[x][y] = Tileset.FLOOR;
                    }
                }
            }
            newXPosition = xPosition;
            newYPosition = yPosition - randomSmallY;
        }
        // make doorway again (accidentally turned into wall above)
        world[xPosition][yPosition] = Tileset.FLOOR;
        // get w and l of newly made room
        if (enterDirection == 1 || enterDirection == 3) {
            w = randomSmallX + randomBigX + 1;
            l = hallLength;
        } else {
            w = hallLength;
            l = randomSmallY + randomBigY + 1;
        }
        // for each side of the room
        for (int i = 1; i <= 4; i++) {
            // 90% chance for each wall to generate a hallway
            int hallSpawnRate = uniform(random, 0, 10);
            if (hallSpawnRate != 0) {
                Position newPositioning = new Position(newXPosition + 1, newYPosition + 1);
                createHallway(random, newPositioning, w, l, i);
            }
        }
    }

    // moves character from string input
    // ex. "N25SDD" string input makes world and moves character right twice
    // returns gameState object
    public GameState moveAvatar(String stringInput) {
        StringInputDevice iterator = new StringInputDevice(stringInput);
        while (iterator.possibleNextInput()) {
            int avatarXPosition = avatarPosition.getX();
            int avatarYPosition = avatarPosition.getY();
            char c = Character.toUpperCase(iterator.getNextKey());
            if (c == ':') {
                readyToQuit = true;
            } else if (c == 'Q' && readyToQuit == true) {
                // need to get rest of input string after finding :Q
                StringBuilder str = new StringBuilder();
                while (iterator.possibleNextInput()) {
                    char letter = Character.toUpperCase(iterator.getNextKey());
                    str.append(letter);
                }
                // if there are letters after the ":Q" input
                if (str.toString().length() > 0) {
                    return new GameState(true, str.toString());
                }
                // if the string input ends with ":Q"
                return new GameState(true, "");
            } else {
                readyToQuit = false;
            }
            if (c == 'W' && (world[avatarXPosition][avatarYPosition + 1].description().equals("floor") ||
                    world[avatarXPosition][avatarYPosition + 1].description().equals("flower"))) {
                replaceOldPosition();
                world[avatarXPosition][avatarYPosition + 1] = Tileset.AVATAR;
                avatarPosition = new Position(avatarXPosition, avatarYPosition + 1);
            } else if (c == 'A' && (world[avatarXPosition - 1][avatarYPosition].description().equals("floor") ||
                    world[avatarXPosition - 1][avatarYPosition].description().equals("flower"))) {
                replaceOldPosition();
                world[avatarXPosition - 1][avatarYPosition] = Tileset.AVATAR;
                avatarPosition = new Position(avatarXPosition - 1, avatarYPosition);
            } else if (c == 'S' && (world[avatarXPosition][avatarYPosition - 1].description().equals("floor") ||
                    world[avatarXPosition][avatarYPosition - 1].description().equals("flower"))) {
                replaceOldPosition();
                world[avatarXPosition][avatarYPosition - 1] = Tileset.AVATAR;
                avatarPosition = new Position(avatarXPosition, avatarYPosition - 1);
            } else if (c == 'D' && (world[avatarXPosition + 1][avatarYPosition].description().equals("floor") ||
                    world[avatarXPosition + 1][avatarYPosition].description().equals("flower"))) {
                replaceOldPosition();
                world[avatarXPosition + 1][avatarYPosition] = Tileset.AVATAR;
                avatarPosition = new Position(avatarXPosition + 1, avatarYPosition);
            }
            // toggle pathing
            if (c == 'P' && togglePathing == false) {
                togglePathing = true;
            } else if (c == 'P' && togglePathing == true) {
                togglePathing = false;
            }
        }
        return new GameState(false, "");
    }

    // replaces the avatar tile with a floor tile (used when avatar is moving)
    public void replaceOldPosition() {
        int avatarXPosition = avatarPosition.getX();
        int avatarYPosition = avatarPosition.getY();
        world[avatarXPosition][avatarYPosition] = Tileset.FLOOR;
    }

    // controls avatar using keyboard presses while game is running
    public boolean controlAvatar() {
        KeyboardInputSource keyboard = new KeyboardInputSource();
        if (keyboard.possibleNextInput()) {
            char c = Character.toUpperCase(keyboard.getNextKey());
            // first two if's are for quitting game with ":Q" or ":q"
            if (c == ':') {
                readyToQuit = true;
            } else if (c == 'Q' && readyToQuit == true) {
                return true;
            } else {
                moveAvatar(String.valueOf(c));
                moveEnemy();
                readyToQuit = false;
            }
        }
        return false;
    }


    // tracks location of cursor/mouse for UI
    public void trackCursorLocation() {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        // gets description of the tile when mouse is hovering
        String description = "";
        if (mouseX >= 0 && mouseX < width && mouseY >= 0 && mouseY < height) {
            description = world[mouseX][mouseY].description();
            if (description.equals("tree")) {
                description = "enemy";
            }
        }
        // Displays in game HUD
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(5, height - 1, description);
        StdDraw.text(width - 10, height - 1, "Press 'P' to see enemy pathing");
        StdDraw.show();
    }

    // spawns enemy on a random floor time
    public void spawnEnemy(long seed) {
        boolean spawned = false;
        Random random = new Random(seed);
        while (spawned == false) {
            int x = uniform(random, 0, width);
            int y = uniform(random, 0, height);
            if (world[x][y].description().equals("floor")) {
                world[x][y] = Tileset.TREE;
                enemyPosition = new Position(x, y);
                spawned = true;
            }
        }
    }

    public Position getEnemyPosition() {
        return this.enemyPosition;
    }

    // used to toggle enemy pathing with 'P'
    public void updatePathing() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (world[x][y].description().equals("flower")) {
                    world[x][y] = Tileset.FLOOR;
                }
            }
        }
    }

    // moves enemy 1 tile towards the player for every keyboard input
    public boolean moveEnemy() {
        AStarSearch test = new AStarSearch(this);
        test.createMap(this.getAvatarPosition(), this.getEnemyPosition(), this);
        test.search();
        Position nextEnemyPosition = test.getParentPosition();
        if (nextEnemyPosition != null) {
            int newXPosition = nextEnemyPosition.getX();
            int newYPosition = nextEnemyPosition.getY();
            int oldXPosition = enemyPosition.getX();
            int oldYPosition = enemyPosition.getY();
            world[newXPosition][newYPosition] = Tileset.TREE;
            world[oldXPosition][oldYPosition] = Tileset.FLOOR;
            enemyPosition = new Position(newXPosition, newYPosition);
            return false;
        }
        return true;
    }
}
