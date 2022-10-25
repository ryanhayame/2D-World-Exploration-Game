package byow.Core;

import java.io.Serializable;

public class GameState implements Serializable {

    // used to figure out if the string input contains a :Q
    // if it doesnt, then gameState = false and restOfInput becomes an empty string ""
    // if it does, then gameState = true
    // if there are more characters after the :Q, it becomes restOfInput
    // if there are no more characters after the :Q, restOfInput becomes an empty string ""
    public boolean gameState;
    public String restOfInput;

    public GameState(boolean gameState, String restOfInput) {
        this.gameState = gameState;
        this.restOfInput = restOfInput;
    }

    public boolean getGameState() {
        return gameState;
    }

    public String getRestOfInput() {
        return restOfInput;
    }
}
