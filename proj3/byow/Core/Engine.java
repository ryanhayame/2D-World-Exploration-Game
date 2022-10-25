package byow.Core;

import byow.Core.Input.KeyboardInputSource;
import byow.Core.Input.StringInputDevice;
import byow.Core.World.World;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;

import static java.lang.Character.isDigit;
import static java.lang.Character.toUpperCase;

public class Engine implements Serializable {
    TERenderer ter = new TERenderer();

    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 50;

    private boolean readyToQuit;

    private boolean atMainMenu;

    private String restOfInput;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        openMainMenu();
        atMainMenu = true;
        while (atMainMenu) {
            KeyboardInputSource keyboard = new KeyboardInputSource();
            if (keyboard.possibleNextInput()) {
                char c = Character.toUpperCase(keyboard.getNextKey());
                if (c == 'N') {
                    openSeedMenu();
                } else if (c == 'L') {
                    // get here by loading from main menu
                    loadGame("");
                    System.exit(1);
                } else if (c == 'Q') {
                    System.exit(0);
                }
            }
        }
    }

    public void openMainMenu() {
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        Font font = new Font("Arial", Font.BOLD, 60);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.8, "RYAN HAYAME'S");
        StdDraw.text(WIDTH / 2, (HEIGHT * 0.8) - 5, "BUILD YOUR OWN WORLD GAME");
        Font font2 = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(font2);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.5, "New Game (N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.3, "Quit (Q)");
        StdDraw.show();
    }

    public void openSeedMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WIDTH / 2, HEIGHT * 0.8, "Seed");
        StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "Hit 'S' to confirm");
        StdDraw.show();
        String input = getSeedInput(20);
        interactWithInputString("N" + input);
    }

    // gets seed from keyboard input; n = how many digits seed can be
    // this method is from lab 13
    public String getSeedInput(int n) {
        StringBuilder str = new StringBuilder();
        while (str.toString().length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (isDigit(c)) {
                    str.append(c);
                    StdDraw.clear(Color.BLACK);
                    StdDraw.text(WIDTH / 2, HEIGHT * 0.8, "Seed");
                    StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "Hit 'S' to confirm");
                    StdDraw.text(WIDTH / 2, HEIGHT * 0.5, str.toString());
                    StdDraw.show();
                } else if (c == 'S') {
                    if (str.toString().length() == 0) {
                        System.exit(0);
                    }
                    str.append(c);
                    StdDraw.clear(Color.BLACK);
                    StdDraw.text(WIDTH / 2, HEIGHT * 0.8, "Seed");
                    StdDraw.text(WIDTH / 2, HEIGHT * 0.4, "Hit 'S' to confirm");
                    StdDraw.text(WIDTH / 2, HEIGHT * 0.5, str.toString());
                    StdDraw.show();
                    return str.toString();
                }
            }
        }
        return str.toString();
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */

    public TETile[][] interactWithInputString(String input) {
        StringInputDevice inputDevice = new StringInputDevice(input);
        int index = 0;
        long currentSeed;
        while (inputDevice.possibleNextInput()) {
            char c = toUpperCase(inputDevice.getNextKey());
            if (c == 'N') {
                currentSeed = getSeedFromInput(input, index);
                createNewWorld(currentSeed);
            } else if (c == 'L') {
                // get here by loading from string input
                // need to load game then process inputs after the 'L'
                StringBuilder stringAfterL = new StringBuilder();
                while (inputDevice.possibleNextInput()) {
                    char afterL = toUpperCase(inputDevice.getNextKey());
                    stringAfterL.append(afterL);
                }
                loadGame(stringAfterL.toString());
                System.exit(1);
            }
            index++;
        }
        System.exit(0);
        return null;
    }

    // takes an input of characters and gets first numbers up to S
    // ex. from original "N1234S" input --> input into here is "123S" --> returns 123
    public long getSeedFromInput(String input, int index) {
        StringBuilder str = new StringBuilder();
        for (int i = index + 1; i < input.length(); i++) {
            Boolean isDigit = isDigit(input.charAt(i));
            if (isDigit) {
                str.append(input.charAt(i));
            } else if (Character.toUpperCase(input.charAt(i)) == 'S'){
                // keeps track of rest of input after S
                StringBuilder remainingInput = new StringBuilder();
                int j = i + 1;
                while (j < input.length()) {
                    char c = input.charAt(j);
                    remainingInput.append(c);
                    j++;
                }
                restOfInput = remainingInput.toString();
                // parselong errors if seed > 9,223,372,036,854,775,807
                return Long.parseLong(str.toString());
            } else {
                // if there is a different letter after the seed
                // ex. N12345Z
                System.out.println("Error: Seed must end with 'S");
                System.exit(0);
            }
        }
        System.out.println("Error: Seed must be less than 20 digits");
        System.exit(0);
        return 0;
    }

    public TETile[][] createNewWorld(long seed) {
        ter.initialize(WIDTH, HEIGHT);
        World map = new World(HEIGHT, WIDTH);
        TETile[][] world = map.getWorld();
        map.createSpawn(seed);
        readyToQuit = false;
        // spawn in enemy on random floor tile
        map.spawnEnemy(seed);
        // moves avatar with input string
        moveAvatarWithStringInputs(map);
        ter.renderFrame(map.getWorld());
        // while game is active
        while (!readyToQuit) {
            // hides visibility of old enemy pathfinding; hides old flower path
            map.updatePathing();
            // A* pathfinding from enemy to avatar; results in new flower path to avatar
            AStarSearch enemyPathing = new AStarSearch(map);
            enemyPathing.createMap(map.getAvatarPosition(), map.getEnemyPosition(), map);
            enemyPathing.search();
            // uses keyboard inputs to control avatar
            readyToQuit = map.controlAvatar();
            // toggles visibility of enemy pathfinding
            if (!map.togglePathing) {
                map.updatePathing();
            }
            // tracks cursor movement to display tile info on HUD
            map.trackCursorLocation();
            ter.renderFrame(world);
        }
        saveGame(map);
        return world;
    }

    public void moveAvatarWithStringInputs (World map) {
        // moves avatar with input string
        GameState gameState = map.moveAvatar(restOfInput);
        // moves enemy for each movement of the player
        for (int i = 0; i < restOfInput.length(); i++) {
            char c = toUpperCase(restOfInput.charAt(i));
            if (c == 'W' || c == 'A' || c == 'S' || c == 'D') {
                map.moveEnemy();
            }
        }
        // is false if string input does not contain :Q
        boolean containsQ = gameState.getGameState();
        restOfInput = gameState.getRestOfInput();
        // if NEW rest of input (after first :Q) contains more characters
        if (containsQ && restOfInput.length() > 0) {
            saveGame(map);
            interactWithInputString(restOfInput);
        }
        // if there is no more string inputs to process
        if (containsQ && restOfInput.isEmpty()) {
            readyToQuit = true;
        }
    }


    public void loadGame(String stringAfterL) {
        readyToQuit = false;
        ter.initialize(WIDTH, HEIGHT);
        File file = new File("Save.txt");
        if (file.exists()) {
            try {
                FileInputStream f = new FileInputStream(file);
                ObjectInputStream o = new ObjectInputStream(f);
                World map = null;
                try {
                    map = (World) o.readObject();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                // process stringAfterL (if string input is used)
                restOfInput = stringAfterL;
                moveAvatarWithStringInputs(map);
                TETile[][] world = map.getWorld();
                ter.renderFrame(world);
                // keeps track of player and mouse movement while game is active
                while (!readyToQuit) {
                    // hides visibility of old enemy pathfinding; hides old flower path
                    map.updatePathing();
                    // A* pathfinding from enemy to avatar; results in new flower path to avatar
                    AStarSearch enemyPathing = new AStarSearch(map);
                    enemyPathing.createMap(map.getAvatarPosition(), map.getEnemyPosition(), map);
                    enemyPathing.search();
                    // uses keyboard inputs to control avatar
                    readyToQuit = map.controlAvatar();
                    // toggles visibility of enemy pathfinding
                    if (!map.togglePathing) {
                        map.updatePathing();
                    }
                    // tracks cursor movement to display tile info on HUD
                    map.trackCursorLocation();
                    ter.renderFrame(world);
                }
                saveGame(map);
                return;
            } catch (FileNotFoundException e) {
                System.out.println("No save file found2");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveGame(World map) {
        File file = new File("Save.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream f = new FileOutputStream(file);
            ObjectOutputStream o = new ObjectOutputStream(f);
            o.writeObject(map);
        } catch (FileNotFoundException e) {
            System.out.println("No save file found");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
