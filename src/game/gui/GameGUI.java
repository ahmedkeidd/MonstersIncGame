package game.gui;

import game.engine.*;
import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.cells.Cell;
import game.engine.monsters.Monster;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameGUI extends Application {

    private Game game;

    private BorderPane root;
    private GridPane boardGrid;

    private VBox playerPanel;
    private VBox opponentPanel;
    private VBox stationedPanel;

    private Label currentTurnLabel;
    private Label diceLabel;
    private Label cardLabel;
    private Label actionLabel;
    private Label winnerLabel;

    private TextArea logArea;

    private CheckBox usePowerupBox;
    private Button rollButton;
    private Button instructionsButton;
    private Button restartButton;
    
    private VBox deckPanel;
    private Label deckTitleLabel;
    
    private Monster lastMovedMonster;
    private int lastOldPosition = -1;
    private int lastNewPosition = -1;
    
    private int previousPlayerEnergy;
    private int previousOpponentEnergy;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("DooR DasH");
        showStartScreen(stage);
    }

    private void showStartScreen(Stage stage) {
        VBox screen = new VBox(22);
        screen.setAlignment(Pos.CENTER);
        screen.setPadding(new Insets(35));
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e3a8a, #312e81);");

        Label title = new Label("DooR DasH");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setEffect(new DropShadow(20, Color.BLACK));

        Label subtitle = new Label("Scare vs Laugh Touchdown");
        subtitle.setTextFill(Color.LIGHTGRAY);
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox previewImages = new HBox(25);
        previewImages.setAlignment(Pos.CENTER);
        previewImages.getChildren().addAll(
                imageOrText("sully.png", "Sully", 90, 90),
                imageOrText("mike.png", "Mike", 90, 90),
                imageOrText("randall.png", "Randall", 90, 90),
                imageOrText("roz.png", "Roz", 90, 90)
        );

        Label info = new Label("Choose your role, roll the dice, activate powerups, survive cells, and reach the final door.");
        info.setTextFill(Color.WHITE);
        info.setWrapText(true);
        info.setMaxWidth(700);
        info.setAlignment(Pos.CENTER);
        info.setFont(Font.font("Arial", 15));

        Button scarerButton = new Button("😈 Play as SCARER");
        Button laugherButton = new Button("😂 Play as LAUGHER");

        scarerButton.setPrefWidth(220);
        laugherButton.setPrefWidth(220);

        scarerButton.setStyle(mainButtonStyle("#dc2626"));
        laugherButton.setStyle(mainButtonStyle("#2563eb"));

        scarerButton.setOnAction(e -> startGame(stage, Role.SCARER));
        laugherButton.setOnAction(e -> startGame(stage, Role.LAUGHER));

        HBox buttons = new HBox(25, scarerButton, laugherButton);
        buttons.setAlignment(Pos.CENTER);

        Button howToPlay = new Button("📘 How to Play");
        howToPlay.setStyle(secondaryButtonStyle());
        howToPlay.setOnAction(e -> showInstructionsPopup());

        screen.getChildren().addAll(title, subtitle, previewImages, info, buttons, howToPlay);

        Scene scene = new Scene(screen, 1150, 780);
        stage.setScene(scene);
        stage.show();
    }

    private void startGame(Stage stage, Role role) {
        try {
            game = new Game(role);
            buildGameScreen(stage);
            refreshAll("Game started. You are " + role + ".");
            showInfo("Game Started", "You are playing as " + role + ".");
        } catch (Exception e) {
            showError("Game could not start", e.getMessage());
        }
    }

    private void buildGameScreen(Stage stage) {
        root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: #e5e7eb;");

        currentTurnLabel = new Label();
        currentTurnLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        diceLabel = new Label("🎲 Dice: -");
        diceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        diceLabel.setStyle("-fx-background-color: #111827; -fx-text-fill: white; -fx-padding: 9 14 9 14; -fx-background-radius: 10;");

        usePowerupBox = new CheckBox("Use powerup before rolling");
        usePowerupBox.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        rollButton = new Button("🎲 Roll Dice / Play Turn");
        rollButton.setStyle(mainButtonStyle("#16a34a"));
        rollButton.setOnAction(e -> playGuiTurn());

        instructionsButton = new Button("📘 Instructions");
        instructionsButton.setStyle(secondaryButtonStyle());
        instructionsButton.setOnAction(e -> showInstructionsPopup());

        restartButton = new Button("🔄 Restart");
        restartButton.setStyle(secondaryButtonStyle());
        restartButton.setOnAction(e -> showStartScreen(stage));

        HBox topBar = new HBox(15, currentTurnLabel, diceLabel, usePowerupBox, rollButton, instructionsButton, restartButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-background-radius: 12; -fx-border-radius: 12;");

        root.setTop(topBar);

        boardGrid = new GridPane();
        boardGrid.setHgap(1);
        boardGrid.setVgap(1);
        boardGrid.setPadding(new Insets(10));
        boardGrid.setAlignment(Pos.CENTER);
        root.setCenter(boardGrid);

        playerPanel = new VBox(8);
        opponentPanel = new VBox(8);
        stationedPanel = new VBox(6);

        winnerLabel = new Label("");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        winnerLabel.setTextFill(Color.DARKGREEN);
        winnerLabel.setWrapText(true);

        actionLabel = new Label("Action log");
        actionLabel.setWrapText(true);
        actionLabel.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-padding: 10; -fx-background-radius: 8; -fx-border-radius: 8;");

        cardLabel = new Label("Card: handled internally by engine");
        cardLabel.setWrapText(true);
        cardLabel.setStyle("-fx-background-color: #fff7ed; -fx-border-color: #fdba74; -fx-padding: 8; -fx-background-radius: 8; -fx-border-radius: 8;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(190);
        
        deckPanel = new VBox(3);
        deckTitleLabel = sectionTitle("🃏 Card Deck");

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(390);
        rightPanel.setStyle("-fx-background-color: #f8fafc;");

       

        rightPanel.getChildren().addAll(
                sectionTitle("🏆 Winner"), winnerLabel,
                sectionTitle("⚡ Current Action"), actionLabel,
                sectionTitle("🃏 Card Info"), cardLabel,
                sectionTitle("👤 Player"), playerPanel,
                sectionTitle("👾 Opponent"), opponentPanel,
                sectionTitle("📍 Stationed Monsters"), stationedPanel,
                sectionTitle("📜 Game Log"), logArea,
                deckTitleLabel, deckPanel
        );
        ScrollPane scrollPane = new ScrollPane(rightPanel);
        scrollPane.setFitToWidth(true);
        root.setRight(scrollPane);

        Scene scene = new Scene(root, 1220, 760);
        stage.setMaximized(true);

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE:
                    playGuiTurn();
                    break;
                case P:
                    usePowerupBox.setSelected(!usePowerupBox.isSelected());
                    break;
                case I:
                    showInstructionsPopup();
                    break;
                case W:
                    game.getCurrent().setPosition(99);
                    refreshAll("CHEAT: Moved " + game.getCurrent().getName() + " to cell 99.");
                    Monster cheatWinner = game.getWinner();
                    if (cheatWinner != null) {
                        rollButton.setDisable(true);
                        usePowerupBox.setDisable(true);
                        String winText =
                            "🏆 GAME OVER\n\n" +
                            "Winner: " + cheatWinner.getName() + "\n" +
                            "Winner Role: " + cheatWinner.getRole() + "\n\n" +
                            "Player: " + game.getPlayer().getName() + "\n" +
                            "Player Role: " + game.getPlayer().getRole() + "\n" +
                            "Player Final Energy: " + game.getPlayer().getEnergy() + "\n\n" +
                            "Opponent: " + game.getOpponent().getName() + "\n" +
                            "Opponent Role: " + game.getOpponent().getRole() + "\n" +
                            "Opponent Final Energy: " + game.getOpponent().getEnergy();
                        winnerLabel.setText(winText);
                        actionLabel.setText("🏆 " + cheatWinner.getName() + " wins!");
                        appendLog(winText);
                        showGameOverWindow(winText);
                    }
                    break;
                case E:
                    game.getCurrent().setEnergy(game.getCurrent().getEnergy() + 500);
                    refreshAll("CHEAT: Increased " + game.getCurrent().getName() + "'s energy.");
                    break;
                default:
                    break;
            }
        });

        stage.setScene(scene);
        stage.show();
    }
    
    private void refreshDeck() {
        java.util.ArrayList<game.engine.cards.Card> deck = Board.getCards();
        deckTitleLabel.setText("🃏 Card Deck (" + deck.size() + " remaining)");
        deckPanel.getChildren().clear();
        for (int i = 0; i < deck.size(); i++) {
            game.engine.cards.Card c = deck.get(i);
            Label cardRow = new Label((i == 0 ? "▶ [NEXT] " : "  " + (i + 1) + ". ") + c.getName());
            cardRow.setWrapText(true);
            if (i == 0) {
                cardRow.setStyle("-fx-background-color: #fef9c3; -fx-border-color: #facc15; -fx-padding: 4 8 4 8; -fx-background-radius: 6; -fx-border-radius: 6; -fx-font-weight: bold;");
            } else {
                cardRow.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-padding: 3 8 3 8; -fx-background-radius: 6; -fx-border-radius: 6;");
            }
            deckPanel.getChildren().add(cardRow);
        }
    }
    
    private void reportEnergyChanges() {
        int playerNow = game.getPlayer().getEnergy();
        int opponentNow = game.getOpponent().getEnergy();

        if (playerNow != previousPlayerEnergy) {
            appendLog(game.getPlayer().getName() + " energy changed: " 
                    + previousPlayerEnergy + " → " + playerNow);
        }

        if (opponentNow != previousOpponentEnergy) {
            appendLog(game.getOpponent().getName() + " energy changed: " 
                    + previousOpponentEnergy + " → " + opponentNow);
        }
    }
    
    private void playGuiTurn() {
        Monster before = game.getCurrent();

        lastMovedMonster = before;
        lastOldPosition = before.getPosition();

        try {
            if (usePowerupBox.isSelected()) {
                game.usePowerup();
                appendLog("Powerup activated by " + before.getName() + ".");
            }

            

            previousPlayerEnergy = game.getPlayer().getEnergy();
            previousOpponentEnergy = game.getOpponent().getEnergy();

            boolean wasFrozen = before.isFrozen();
            
            reportEnergyChanges();

            if (wasFrozen) {
                appendLog(before.getName() + " was frozen and skipped the turn.");
                actionLabel.setText(before.getName() + " skipped turn due to Freeze.");
            }
            
            game.playTurn();
            diceLabel.setText("Dice: " + game.getLastDiceRoll());

            lastNewPosition = before.getPosition();

            refreshAll(before.getName() + " moved from cell " + lastOldPosition + " to cell " + lastNewPosition + ".");

            animateBoard();

            Monster winner = game.getWinner();

            if (winner != null) {
                rollButton.setDisable(true);
                usePowerupBox.setDisable(true);

                String winText =
                        "🏆 GAME OVER\n\n" +
                        "Winner: " + winner.getName() + "\n" +
                        "Winner Role: " + winner.getRole() + "\n\n" +
                        "Player: " + game.getPlayer().getName() + "\n" +
                        "Player Role: " + game.getPlayer().getRole() + "\n" +
                        "Player Final Energy: " + game.getPlayer().getEnergy() + "\n\n" +
                        "Opponent: " + game.getOpponent().getName() + "\n" +
                        "Opponent Role: " + game.getOpponent().getRole() + "\n" +
                        "Opponent Final Energy: " + game.getOpponent().getEnergy();

                winnerLabel.setText(winText);
                actionLabel.setText("🏆 " + winner.getName() + " wins!");

                appendLog(winText);

                showGameOverWindow(winText);
            }

        } catch (Exception e) {
            String text = "Invalid action: " + e.getMessage();
            actionLabel.setText(text);
            appendLog(text);
            showError("Action blocked", text);
        }

        usePowerupBox.setSelected(false);
    }

    
    private void showGameOverWindow(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("🏆 Game Won");

        ButtonType startButton = new ButtonType("Return to Start Window");
        ButtonType closeButton = new ButtonType("Close");

        alert.getButtonTypes().setAll(startButton, closeButton);
        alert.setContentText(message);

        alert.showAndWait().ifPresent(response -> {
            if (response == startButton) {
                Stage stage = (Stage) root.getScene().getWindow();
                showStartScreen(stage);
            }
        });
    }
    
    
    private String cardEffectText(game.engine.cards.Card card) {
        String type = card.getClass().getSimpleName();

        if (type.contains("Confusion")) {
            return "Swaps / confuses the monster role temporarily.";
        }

        if (type.contains("EnergySteal")) {
            return "Steals energy from the opponent.";
        }

        if (type.contains("Shield")) {
            return "Gives a shield that blocks one energy loss.";
        }

        if (type.contains("StartOver")) {
            return "Sends monster back to the start.";
        }

        if (type.contains("Swapper")) {
            return "Swaps monster positions.";
        }

        return "Card effect applied by engine.";
    }
    
    private void refreshAll(String action) {
        drawBoard();
        updatePanels();
        refreshDeck();

        currentTurnLabel.setText("Current Turn: " + monsterLabel(game.getCurrent()));
        try {
            game.engine.cards.Card card = Board.getLastDrawnCard();

            if (card != null) {
                cardLabel.setText(
                        "Card Drawn: " + card.getName()
                                + "\nType: " + card.getClass().getSimpleName()
                                + "\nEffect: " + cardEffectText(card)
                );
            } else {
                cardLabel.setText("Card: none drawn yet");
            }

        } catch (Exception e) {
            cardLabel.setText("Card: no card drawn yet");
        }
        actionLabel.setText(action);

        appendLog(action);
    }

    private ImageView getFullCellImage(Cell cell) {

        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;

            if (door.getRole() == Role.SCARER) {
                return imageView("scarer_door.png", 58, 58);
            } else {
                return imageView("laugher_door.png", 58, 58);
            }
        }

        if (cell instanceof MonsterCell) {
            MonsterCell monsterCell = (MonsterCell) cell;
            return monsterImage(monsterCell.getCellMonster(), 58, 58);
        }

        if (cell instanceof CardCell) {
            return imageView("card.png", 58, 58);
        }

        if (cell instanceof ContaminationSock) {
            return imageView("sock.png", 58, 58);
        }

        if (cell instanceof ConveyorBelt) {
            return imageView("conveyor.png", 58, 58);
        }

        return imageView("yellow.png", 58, 58);
    }
    
    private void drawBoard() {

        boardGrid.getChildren().clear();

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {

            Cell cell = getCellAt(index);

            StackPane stack = new StackPane();
            stack.setMinSize(58, 58);
            stack.setPrefSize(58, 58);
            stack.setMaxSize(58, 58);

            ImageView bg = getFullCellImage(cell);
            bg.setFitWidth(58);
            bg.setFitHeight(58);
            bg.setPreserveRatio(false);

            VBox overlay = new VBox(2);
            overlay.setAlignment(Pos.TOP_CENTER);
            overlay.setPadding(new Insets(2));

            Label number = new Label("#" + index);
            number.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.7);" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 9;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 1 4 1 4;" +
                    "-fx-background-radius: 4;"
            );
            
            Label occupants = new Label(getOccupantsAt(index));
            occupants.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.85);" +
                    "-fx-font-size: 9;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 1 4 1 4;" +
                    "-fx-background-radius: 4;"
            );

            Label doorInfo = new Label("");

            if (cell instanceof DoorCell) {
                DoorCell door = (DoorCell) cell;
                doorInfo.setText("E:" + door.getEnergy());

                if (door.isActivated()) {
                    doorInfo.setText("USED E:" + door.getEnergy());
                }

                doorInfo.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.9);" +
                        "-fx-font-size: 8;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 1 3 1 3;" +
                        "-fx-background-radius: 4;"
                );
            }
            occupants.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.85);" +
                    "-fx-font-size: 9;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 1 4 1 4;" +
                    "-fx-background-radius: 4;"
            );

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            overlay.getChildren().addAll(number, doorInfo, spacer, occupants);

            stack.getChildren().addAll(bg, overlay);
            stack.setStyle("-fx-border-color: black; -fx-border-width: 1;");

            if (game.getPlayer().getPosition() == index) {
                stack.setEffect(new DropShadow(15, Color.BLUE));
                stack.setStyle("-fx-border-color: blue; -fx-border-width: 4;");
            }

            if (game.getOpponent().getPosition() == index) {
                stack.setEffect(new DropShadow(15, Color.RED));
                stack.setStyle("-fx-border-color: red; -fx-border-width: 4;");
            }

            if (game.getPlayer().getPosition() == index && game.getOpponent().getPosition() == index) {
                stack.setEffect(new DropShadow(15, Color.PURPLE));
                stack.setStyle("-fx-border-color: purple; -fx-border-width: 5;");
            }

            int[] position = indexToDisplayRowCol(index);
            boardGrid.add(stack, position[1], position[0]);
        }
    }
    
    private static Card lastDrawnCard;

    public static Card getLastDrawnCard() {
        return lastDrawnCard;
    }

    private Cell getCellAt(int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;

        if (row % 2 == 1) {
            col = Constants.BOARD_COLS - 1 - col;
        }

        return game.getBoard().getBoardCells()[row][col];
    }

    private int[] indexToDisplayRowCol(int index) {
        int row = 9 - (index / Constants.BOARD_COLS);
        int col = index % Constants.BOARD_COLS;

        if ((index / Constants.BOARD_COLS) % 2 == 1) {
            col = 9 - col;
        }

        return new int[] { row, col };
    }

    @SuppressWarnings("unused")
	private String getCellText(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;
            return door.getRole() + " DOOR";
        }

        if (cell instanceof MonsterCell) {
            MonsterCell monsterCell = (MonsterCell) cell;
            return monsterCell.getCellMonster().getName();
        }

        if (cell instanceof CardCell) {
            return "CARD";
        }

        if (cell instanceof ContaminationSock) {
            return "SOCK";
        }

        if (cell instanceof ConveyorBelt) {
            return "BELT";
        }

        return "REST";
    }

    @SuppressWarnings("unused")
	private ImageView getCellImage(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;

            if (door.getRole() == Role.SCARER) {
                return imageView("scarer_door.png", 28, 28);
            }

            return imageView("laugher_door.png", 28, 28);
        }

        if (cell instanceof MonsterCell) {
            MonsterCell monsterCell = (MonsterCell) cell;
            return monsterImage(monsterCell.getCellMonster(), 28, 28);
        }

        if (cell instanceof CardCell) {
            return imageView("card.png", 28, 28);
        }

        if (cell instanceof ContaminationSock) {
            return imageView("sock.png", 28, 28);
        }

        if (cell instanceof ConveyorBelt) {
            return imageView("conveyor.png", 28, 28);
        }

        return imageView("rest.png", 28, 28);
    }

    @SuppressWarnings("unused")
	private String getCellStyle(Cell cell, int index) {
        String base =
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;" +
                "-fx-border-width: 1.5;";

        if (index == lastNewPosition) {
            base += "-fx-border-color: #f59e0b; -fx-border-width: 4;";
        } else if (index == game.getCurrent().getPosition()) {
            base += "-fx-border-color: #22c55e; -fx-border-width: 3;";
        } else {
            base += "-fx-border-color: #475569;";
        }

        if (cell instanceof DoorCell) {
            DoorCell door = (DoorCell) cell;

            if (door.getRole() == Role.SCARER) {
                return base + "-fx-background-color: linear-gradient(to bottom right, #fecaca, #f87171);";
            }

            return base + "-fx-background-color: linear-gradient(to bottom right, #bae6fd, #38bdf8);";
        }

        if (cell instanceof MonsterCell) {
            return base + "-fx-background-color: linear-gradient(to bottom right, #e9d5ff, #c084fc);";
        }

        if (cell instanceof CardCell) {
            return base + "-fx-background-color: linear-gradient(to bottom right, #fef3c7, #facc15);";
        }

        if (cell instanceof ContaminationSock) {
            return base + "-fx-background-color: linear-gradient(to bottom right, #bbf7d0, #22c55e);";
        }

        if (cell instanceof ConveyorBelt) {
            return base + "-fx-background-color: linear-gradient(to bottom right, #e5e7eb, #94a3b8);";
        }

        return base + "-fx-background-color: white;";
    }

    private String getOccupantsAt(int index) {
        String text = "";

        if (game.getPlayer().getPosition() == index) {
            text += "P ";
        }

        if (game.getOpponent().getPosition() == index) {
            text += "O ";
        }

        if (lastMovedMonster != null && lastMovedMonster.getPosition() == index) {
            text += "⭐ ";
        }

        if (game.getCurrent().getPosition() == index) {
            text += "✨";
        }

        return text;
    }

    private void updatePanels() {
        playerPanel.getChildren().clear();
        opponentPanel.getChildren().clear();
        stationedPanel.getChildren().clear();

        playerPanel.getChildren().add(monsterCard(game.getPlayer(), true));
        opponentPanel.getChildren().add(monsterCard(game.getOpponent(), false));

        for (Monster monster : Board.getStationedMonsters()) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6));
            row.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-background-radius: 6; -fx-border-radius: 6;");

            ImageView img = monsterImage(monster, 34, 34);

            Label label = new Label(
                    monster.getName() +
                    " | " +
                    monster.getRole() +
                    " | E: " +
                    monster.getEnergy() +
                    " | Pos: " +
                    monster.getPosition()
            );

            label.setWrapText(true);

            row.getChildren().addAll(img, label);
            stationedPanel.getChildren().add(row);
        }
    }

    private VBox monsterCard(Monster monster, boolean isPlayer) {
        VBox box = new VBox(7);
        box.setPadding(new Insets(9));

        if (game.getCurrent() == monster) {
            box.setStyle("-fx-background-color: #dcfce7; -fx-border-color: #22c55e; -fx-border-width: 3; -fx-background-radius: 10; -fx-border-radius: 10;");
        } else {
            box.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-background-radius: 10; -fx-border-radius: 10;");
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        ImageView image = monsterImage(monster, 65, 65);

        Label name = new Label((isPlayer ? "PLAYER: " : "OPPONENT: ") + monster.getName());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        name.setWrapText(true);

        top.getChildren().addAll(image, name);

        Label details = new Label(
                "Class: " + monster.getClass().getSimpleName() +
                "\nRole: " + monster.getRole() +
                "\nEnergy: " + monster.getEnergy() +
                "\nPosition: " + monster.getPosition() +
                "\nCurrent Role: " + monster.getRole() +
                "\nOriginal Role: " + monster.getOriginalRole() +
                "\nStatus: " + statusText(monster)
        );

        details.setWrapText(true);

        ProgressBar energyBar = new ProgressBar();
        energyBar.setPrefWidth(310);

        double progress = monster.getEnergy() / 1000.0;

        if (progress < 0) {
            progress = 0;
        }

        if (progress > 1) {
            progress = 1;
        }

        energyBar.setProgress(progress);

        box.getChildren().addAll(top, details, energyBar);

        return box;
    }

    private ImageView monsterImage(Monster monster, double width, double height) {
        String name = monster.getName().toLowerCase();
        System.out.println(name);
        
        if (name.contains("sully") || name.contains("sulley") || name.contains("James P. Sullivan") || name.contains("James") || name.contains("james"))  {
            return imageView("sully.png", width, height);
        }

        if (name.contains("mike")) {
            return imageView("mike.png", width, height);
        }

        if (name.contains("randall")) {
            return imageView("randall.png", width, height);
        }

        if (name.contains("celia")) {
            return imageView("celia.png", width, height);
        }

        if (name.contains("roz")) {
            return imageView("roz.png", width, height);
        }

        if (name.contains("fungus")) {
            return imageView("fungus.png", width, height);
        }

        if (name.contains("needleman")) {
            return imageView("needleman.png", width, height);
        }

        if (name.contains("yeti")) {
            return imageView("yeti.png", width, height);
        }

        if (name.contains("waternoose")) {
            return imageView("waternoose.png", width, height);
        }

        return imageView("yellow.png", width, height);
       
    	}
    

    private ImageView imageView(String fileName, double width, double height) {
        Image image = new Image("file:images/" + fileName);
        System.out.println("Loading: file:images/" + fileName + " error=" + image.isError());

        if (image.isError()) {
            return yellowImage(width, height);
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        return imageView;
    }

    @SuppressWarnings("unused")
	private ImageView emptyImage(double width, double height) {
        return yellowImage(width, height);
    }

    private ImageView yellowImage(double width, double height) {
        Image image = new Image("file:images/yellow.png");

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);

        return imageView;
    }

    private VBox imageOrText(String fileName, String text, double width, double height) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        ImageView img = imageView(fileName, width, height);

        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        box.getChildren().addAll(img, label);
        return box;
    }


    private String statusText(Monster monster) {
        String status = "";

        if (monster.isShielded()) {
            status += "Shielded: blocks next energy loss\n";
        }

        if (monster.isFrozen()) {
            status += "Frozen: skips next turn\n";
        }

        if (monster.isConfused()) {
            status += "Confused: role temporarily swapped";

            try {
                status += " (" + monster.getConfusionTurns() + " turns left)";
            } catch (Exception e) {
                status += " (active)";
            }

            status += "\n";
        }

        if (monster.getRole() != monster.getOriginalRole()) {
            status += "Role Swap: Original = " + monster.getOriginalRole()
                    + ", Current = " + monster.getRole() + "\n";
        }

        String className = monster.getClass().getSimpleName();

        if (className.contains("Dasher")) {
            status += "Momentum Rush: shown if active in engine\n";
        }

        if (className.contains("MultiTasker")) {
            status += "Focus Mode: shown if active in engine\n";
        }

        if (status.equals("")) {
            return "Normal";
        }

        return status;
    }

    private String monsterLabel(Monster monster) {
        return monster.getName() + " [" + monster.getRole() + "]";
    }

    @SuppressWarnings("unused")
	private void animateDice() {
        Timeline timeline = new Timeline();

        for (int i = 0; i < 10; i++) {
            final int value = 1 + (int) (Math.random() * 6);

            KeyFrame frame = new KeyFrame(
                    Duration.millis(i * 60),
                    e -> diceLabel.setText("🎲 Dice: " + value)
            );

            timeline.getKeyFrames().add(frame);
        }

        timeline.play();
    }

    private void animateBoard() {
        FadeTransition fade = new FadeTransition(Duration.millis(280), boardGrid);
        fade.setFromValue(0.45);
        fade.setToValue(1.0);
        fade.play();

        ScaleTransition scale = new ScaleTransition(Duration.millis(220), boardGrid);
        scale.setFromX(0.985);
        scale.setFromY(0.985);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        label.setStyle("-fx-text-fill: #1e293b;");
        return label;
    }

    private String mainButtonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 9 15 9 15;" +
                "-fx-background-radius: 9;" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #334155;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 8 14 8 14;" +
                "-fx-background-radius: 9;" +
                "-fx-cursor: hand;";
    }

    private void appendLog(String text) {
        if (logArea != null && text != null && !text.equals("")) {
            logArea.appendText(text + "\n");
        }
    }

    private void showInstructionsPopup() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Play");
        alert.setHeaderText("DooR DasH Instructions");
        alert.setContentText(
                "Controls:\n" +
                "- SPACE: play turn\n" +
                "- P: toggle powerup\n" +
                "- I: show instructions\n\n" +
                "Legend:\n" +
                "- P = Player\n" +
                "- O = Opponent\n" +
                "- ⭐ = last moved monster\n" +
                "- ✨ = current turn monster\n\n" +
                "Goal:\n" +
                "Reach the final door with enough energy to win."
        );
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "Unknown error" : message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message == null ? "" : message);
        alert.showAndWait();
    }
    
}