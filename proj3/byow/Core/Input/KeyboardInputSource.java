package byow.Core.Input;

import edu.princeton.cs.introcs.StdDraw;

public class KeyboardInputSource implements InputSource {

    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                return Character.toUpperCase(StdDraw.nextKeyTyped());
            }
        }
    }

    public boolean possibleNextInput() {
        return StdDraw.hasNextKeyTyped();
    }
}
