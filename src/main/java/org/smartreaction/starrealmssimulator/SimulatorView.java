package org.smartreaction.starrealmssimulator;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.chart.*;
import starrealmssimulator.model.*;
import starrealmssimulator.service.GameService;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ManagedBean
@ViewScoped
public class SimulatorView implements Serializable {

    private GameState gameState = new GameState();

    private int timesToSimulate = 2000;

    private SimulationResults results;

    private boolean showResults;

    private boolean loadingResults;

    private boolean showErrors;

    private List<String> errorMessages;

    private GameService gameService = new GameService();

    private LineChartModel authorityChart;

    private boolean showWinGameLog;

    private boolean showLossGameLog;

    private boolean showPlayerWinDifferentialByCardsAtEndOfGame;
    private boolean showOpponentWinDifferentialByCardsAtEndOfGame;

    private boolean showPlayerWinPercentageByFirstDeckCard;
    private boolean showOpponentWinPercentageByFirstDeckCard;

    private boolean showPlayerWinPercentageBySecondDeckCard;
    private boolean showOpponentWinPercentageBySecondDeckCard;

    private int timesToSimulateBuys = 500;

    private int timesToSimulateBots = 500;

    private boolean simulatingBuys;

    private boolean simulatingBots;

    Map<Card, CardToBuySimulationResults> buyCardResults;

    Map<String, Float> botResults;

    @PostConstruct
    public void setup() {
        gameState.setBot("VelocityBot");
        gameState.setOpponentBot("VelocityBot");
        gameState.setCurrentPlayer("R");
    }

    public void startSimulation() {
        simulatingBuys = false;
        simulatingBots = false;
        startSimulationSetup();
    }

    public void startBuySimulation() {
        simulatingBuys = true;
        simulatingBots = false;
        startSimulationSetup();
    }

    public void startBotSimulation() {
        simulatingBots = true;
        simulatingBuys = false;
        startSimulationSetup();
    }

    public void startSimulationSetup() {
        showResults = true;
        loadingResults = true;
        showWinGameLog = false;
        showLossGameLog = false;
        showPlayerWinDifferentialByCardsAtEndOfGame = false;
        showOpponentWinDifferentialByCardsAtEndOfGame = false;
        showPlayerWinPercentageByFirstDeckCard = false;
        showOpponentWinPercentageByFirstDeckCard = false;
        showPlayerWinPercentageBySecondDeckCard = false;
        showOpponentWinPercentageBySecondDeckCard = false;
    }

    public void runBuySimulation() {
        addGameStateErrors();

        showErrors = !errorMessages.isEmpty();

        if (!showErrors) {
            buyCardResults = gameService.simulateBestCardToBuy(gameState, timesToSimulateBuys);
        }

        loadingResults = false;
    }

    public void runBotSimulation() {
        addGameStateErrors();

        showErrors = !errorMessages.isEmpty();

        if (!showErrors) {
            botResults = gameService.simulateBestBot(gameState, timesToSimulateBots);
        }

        loadingResults = false;
    }

    public void runSimulation() {
        addGameStateErrors();

        showErrors = !errorMessages.isEmpty();

        if (!showErrors) {
            results = gameService.simulateGameToEnd(new GameStateGame(gameState, gameService), timesToSimulate);
            authorityChart = new LineChartModel();
            authorityChart.setTitle("Average Authority By # Hands Played");
            authorityChart.setLegendPosition("e");

            Axis yAxis = authorityChart.getAxis(AxisType.Y);
            yAxis.setLabel("Authority");
            yAxis.setMin(0);

            Axis xAxis = authorityChart.getAxis(AxisType.X);
            xAxis.setLabel("Hands");
            xAxis.setMin(0);

            ChartSeries playerAuthority = new ChartSeries();
            playerAuthority.setLabel("Player");

            int numZerosFound = 0;
            Map<Integer, Integer> playerAverageAuthorityByTurn = results.getPlayerAverageAuthorityByTurn();
            for (Integer turn : playerAverageAuthorityByTurn.keySet()) {
                Integer authority = playerAverageAuthorityByTurn.get(turn);
                playerAuthority.set(turn, authority);
                if (authority <= 0) {
                    numZerosFound++;
                }
                if (numZerosFound >= 2) {
                    break;
                }
            }

            ChartSeries opponentAuthority = new ChartSeries();
            opponentAuthority.setLabel("Opponent");

            numZerosFound = 0;
            Map<Integer, Integer> opponentAverageAuthorityByTurn = results.getOpponentAverageAuthorityByTurn();
            for (Integer turn : opponentAverageAuthorityByTurn.keySet()) {
                Integer authority = opponentAverageAuthorityByTurn.get(turn);
                opponentAuthority.set(turn, authority);
                if (authority <= 0) {
                    numZerosFound++;
                }
                if (numZerosFound >= 2) {
                    break;
                }
            }

            authorityChart.addSeries(playerAuthority);
            authorityChart.addSeries(opponentAuthority);
        }

        loadingResults = false;
    }

    private void addGameStateErrors() {
        errorMessages = new ArrayList<>();

        if (!gameState.includeBaseSet.equals("Y") && !gameState.includeColonyWars.equals("Y")) {
            errorMessages.add("You must include either Base Set or Colony Wars");
        }

        if (gameState.turn < 1 || gameState.turn > 100) {
            errorMessages.add("Invalid turn: " + gameState.turn);
        }

        if (gameState.authority <= 0 || gameState.authority > 500) {
            errorMessages.add("Invalid authority: " + gameState.authority);
        }

        if (gameState.opponentAuthority <= 0 || gameState.opponentAuthority > 500) {
            errorMessages.add("Invalid Opponent authority: " + gameState.opponentAuthority);
        }

        if (gameState.shuffles < 0 || gameState.shuffles > 50) {
            errorMessages.add("Invalid shuffles: " + gameState.shuffles);
        }

        if (gameState.opponentShuffles < 0 || gameState.opponentShuffles > 50) {
            errorMessages.add("Invalid Opponent shuffles: " + gameState.opponentShuffles);
        }

        if (simulatingBuys) {
            if (gameState.currentPlayer.equals("R")) {
                errorMessages.add("Is it your turn can't be random when simulating buys");
            }
            if (timesToSimulateBuys < 10 || timesToSimulateBuys > 2000) {
                errorMessages.add("Invalid number of times to simulate buys: " + timesToSimulate);
            }
            if (gameState.bot.equalsIgnoreCase("simulatorbot")) {
                errorMessages.add("You cannot use Simulator Bot when simulating buys");
            }
        } else if (simulatingBots) {
            if (timesToSimulateBots < 10 || timesToSimulateBots > 20000) {
                errorMessages.add("Invalid number of times to simulate bots: " + timesToSimulateBots);
            }
            if (gameState.bot.equalsIgnoreCase("simulatorbot")) {
                errorMessages.add("You cannot use Simulator Bot when simulating bots");
            }
        } else {
            if (timesToSimulate < 10 || timesToSimulate > 20000) {
                errorMessages.add("Invalid number of times to simulate: " + timesToSimulate);
            }
            if (gameState.bot.equalsIgnoreCase("simulatorbot") && (timesToSimulate > 100)) {
                errorMessages.add("Times to simulate can't be greater than 100 when using Simulator Bot");
            }
        }

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
                String cardNameWithoutMultiplier = cardName;
                if (cardName.contains("*")) {
                    cardNameWithoutMultiplier = cardName.substring(0, cardName.indexOf("*"));
                    try {
                        int multiplier = Integer.parseInt(cardName.substring(cardName.indexOf("*") + 1).trim());
                        if (multiplier < 0 || multiplier > 100) {
                            errorMessages.add(cardNamesDescription + " has invalid card multiplier: " + cardName);
                        }
                    } catch (Exception e) {
                        errorMessages.add(cardNamesDescription + " has invalid card multiplier: " + cardName);
                    }
                }
                Card card = gameService.getCardFromName(cardNameWithoutMultiplier);
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

    public LineChartModel getAuthorityChart() {
        return authorityChart;
    }

    public boolean isShowWinGameLog() {
        return showWinGameLog;
    }

    public void setShowWinGameLog(boolean showWinGameLog) {
        this.showWinGameLog = showWinGameLog;
    }

    public boolean isShowLossGameLog() {
        return showLossGameLog;
    }

    public void setShowLossGameLog(boolean showLossGameLog) {
        this.showLossGameLog = showLossGameLog;
    }

    public boolean isShowPlayerWinDifferentialByCardsAtEndOfGame() {
        return showPlayerWinDifferentialByCardsAtEndOfGame;
    }

    public void setShowPlayerWinDifferentialByCardsAtEndOfGame(boolean showPlayerWinDifferentialByCardsAtEndOfGame) {
        this.showPlayerWinDifferentialByCardsAtEndOfGame = showPlayerWinDifferentialByCardsAtEndOfGame;
    }

    public boolean isShowOpponentWinDifferentialByCardsAtEndOfGame() {
        return showOpponentWinDifferentialByCardsAtEndOfGame;
    }

    public void setShowOpponentWinDifferentialByCardsAtEndOfGame(boolean showOpponentWinDifferentialByCardsAtEndOfGame) {
        this.showOpponentWinDifferentialByCardsAtEndOfGame = showOpponentWinDifferentialByCardsAtEndOfGame;
    }

    public boolean isShowPlayerWinPercentageByFirstDeckCard() {
        return showPlayerWinPercentageByFirstDeckCard;
    }

    public void setShowPlayerWinPercentageByFirstDeckCard(boolean showPlayerWinPercentageByFirstDeckCard) {
        this.showPlayerWinPercentageByFirstDeckCard = showPlayerWinPercentageByFirstDeckCard;
    }

    public boolean isShowOpponentWinPercentageByFirstDeckCard() {
        return showOpponentWinPercentageByFirstDeckCard;
    }

    public void setShowOpponentWinPercentageByFirstDeckCard(boolean showOpponentWinPercentageByFirstDeckCard) {
        this.showOpponentWinPercentageByFirstDeckCard = showOpponentWinPercentageByFirstDeckCard;
    }

    public boolean isShowPlayerWinPercentageBySecondDeckCard() {
        return showPlayerWinPercentageBySecondDeckCard;
    }

    public void setShowPlayerWinPercentageBySecondDeckCard(boolean showPlayerWinPercentageBySecondDeckCard) {
        this.showPlayerWinPercentageBySecondDeckCard = showPlayerWinPercentageBySecondDeckCard;
    }

    public boolean isShowOpponentWinPercentageBySecondDeckCard() {
        return showOpponentWinPercentageBySecondDeckCard;
    }

    public void setShowOpponentWinPercentageBySecondDeckCard(boolean showOpponentWinPercentageBySecondDeckCard) {
        this.showOpponentWinPercentageBySecondDeckCard = showOpponentWinPercentageBySecondDeckCard;
    }

    public int getTimesToSimulateBuys() {
        return timesToSimulateBuys;
    }

    public void setTimesToSimulateBuys(int timesToSimulateBuys) {
        this.timesToSimulateBuys = timesToSimulateBuys;
    }

    public Map<Card, CardToBuySimulationResults> getBuyCardResults() {
        return buyCardResults;
    }

    public void setBuyCardResults(Map<Card, CardToBuySimulationResults> buyCardResults) {
        this.buyCardResults = buyCardResults;
    }

    public boolean isSimulatingBuys() {
        return simulatingBuys;
    }

    public void setSimulatingBuys(boolean simulatingBuys) {
        this.simulatingBuys = simulatingBuys;
    }

    public boolean isSimulatingBots() {
        return simulatingBots;
    }

    public void setSimulatingBots(boolean simulatingBots) {
        this.simulatingBots = simulatingBots;
    }

    public int getTimesToSimulateBots() {
        return timesToSimulateBots;
    }

    public void setTimesToSimulateBots(int timesToSimulateBots) {
        this.timesToSimulateBots = timesToSimulateBots;
    }

    public Map<String, Float> getBotResults() {
        return botResults;
    }

    public void setBotResults(Map<String, Float> botResults) {
        this.botResults = botResults;
    }
}
