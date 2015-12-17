package org.smartreaction.starrealmssimulator;

import org.apache.commons.lang3.StringUtils;
import starrealmssimulator.model.Card;
import starrealmssimulator.model.Gambit;
import starrealmssimulator.model.GameState;
import starrealmssimulator.model.SimulationResults;
import starrealmssimulator.service.GameService;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ViewScoped
public class SimulatorView implements Serializable {

    private GameState gameState = new GameState();

    private int timesToSimulate = 1000;

    private SimulationResults results;

    private boolean showResults;

    private boolean loadingResults;

    private boolean showErrors;

    private List<String> errorMessages;

    private GameService gameService = new GameService();

    public void startSimulation() {
        showResults = true;
        loadingResults = true;
    }

    public void runSimulation() {
        addGameStateErrors();

        showErrors = !errorMessages.isEmpty();

        if (!showErrors) {
            results = gameService.simulateGameToEnd(gameState, timesToSimulate);
        }

        loadingResults = false;
    }

    private void addGameStateErrors() {
        errorMessages = new ArrayList<>();

        addErrorMessagesForCardNames(gameState.tradeRow, "Trade Row");

        addErrorMessagesForCardNames(gameState.hand, "Hand");
        addErrorMessagesForCardNames(gameState.deck, "Deck");
        addErrorMessagesForCardNames(gameState.discard, "Discard");
        addErrorMessagesForCardNames(gameState.basesInPlay, "Bases in Play");
        addErrorMessagesForGambitNames(gameState.gambits, "Gambits");

        addErrorMessagesForCardNames(gameState.opponentHandAndDeck, "Opponent Hand and Deck");
        addErrorMessagesForCardNames(gameState.opponentDiscard, "Opponent Discard");
        addErrorMessagesForCardNames(gameState.opponentBasesInPlay, "Opponent Bases in Play");
        addErrorMessagesForCardNames(gameState.opponentGambits, "Opponent Gambits");
    }

    private void addErrorMessagesForCardNames(String cardNames, String cardNamesDescription) {
        if (StringUtils.isNotEmpty(cardNames)) {
            String[] cardNamesArray = cardNames.split(",");
            for (String cardName : cardNamesArray) {
                Card card = gameService.getCardFromName(cardName);
                if (card == null) {
                    errorMessages.add(cardNamesDescription + " has invalid card name: " + cardName);
                }
            }
        }
    }

    private void addErrorMessagesForGambitNames(String gambitNames, String gambitNamesDescription) {
        if (StringUtils.isNotEmpty(gambitNames)) {
            String[] gambitNamesArray = gambitNames.split(",");
            for (String gambitName : gambitNamesArray) {
                Gambit gambit = gameService.getGambitFromName(gambitName);
                if (gambit == null) {
                    errorMessages.add(gambitNamesDescription + " has invalid gambit name: " + gambitName);
                }
            }
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public int getTimesToSimulate() {
        return timesToSimulate;
    }

    public void setTimesToSimulate(int timesToSimulate) {
        this.timesToSimulate = timesToSimulate;
    }

    public SimulationResults getResults() {
        return results;
    }

    public void setResults(SimulationResults results) {
        this.results = results;
    }

    public boolean isShowResults() {
        return showResults;
    }

    public void setShowResults(boolean showResults) {
        this.showResults = showResults;
    }

    public boolean isLoadingResults() {
        return loadingResults;
    }

    public void setLoadingResults(boolean loadingResults) {
        this.loadingResults = loadingResults;
    }

    public boolean isShowErrors() {
        return showErrors;
    }

    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }
}
