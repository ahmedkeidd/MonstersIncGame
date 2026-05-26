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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameGUIAdvanced extends Application {

    private static final String FONT = "Verdana";
    private static final int CELL_SIZE = 62;
    private static final String BG_DARK = "#09111f";
    private static final String PANEL_DARK = "#111827";
    private static final String PANEL_LIGHT = "#f8fafc";
    private static final String LINE_SOFT = "#263244";
    private static final String SCARER_RED = "#ef4444";
    private static final String LAUGHER_BLUE = "#38bdf8";
    private static final String ENERGY_GREEN = "#22c55e";

    private Game game;

    private BorderPane root;
    private StackPane sceneStack;
    private VBox notificationLayer;
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
    private String playerName = "Player";
    
    private VBox deckPanel;
    private Label deckTitleLabel;
    
    private Monster lastMovedMonster;
    private int lastOldPosition = -1;
    private int lastNewPosition = -1;
    private Card lastNotifiedCard;
    
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
        StackPane screen = new StackPane();
        screen.setStyle("-fx-background-color: #020617;");

        ImageView splash = cinematicImage("intro_ai_monsters.png", 1200, 820);
        splash.setOpacity(0.42);

        Rectangle shade = new Rectangle(1400, 900);
        shade.setFill(Color.rgb(2, 6, 23, 0.48));

        VBox hero = new VBox(18);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(30));
        hero.setMaxWidth(760);
        hero.setStyle(
                "-fx-background-color: rgba(15,23,42,0.74);" +
                "-fx-border-color: rgba(148,163,184,0.38);" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;"
        );

        Label title = new Label("DooR DasH");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 66));
        title.setEffect(new DropShadow(24, Color.rgb(0, 0, 0, 0.55)));

        Label subtitle = new Label("Welcome to the scream floor.");
        subtitle.setTextFill(Color.web("#dbeafe"));
        subtitle.setFont(Font.font(FONT, FontWeight.BOLD, 19));

        HBox previewImages = new HBox(25);
        previewImages.setAlignment(Pos.CENTER);
        previewImages.getChildren().addAll(
                imageOrText("sully.png", "Sully", 88, 88),
                imageOrText("mike.png", "Mike", 88, 88),
                imageOrText("randall.png", "Randall", 88, 88),
                imageOrText("roz.png", "Roz", 88, 88)
        );

        Label info = new Label("Enter your name before the doors start moving.");
        info.setTextFill(Color.web("#f8fafc"));
        info.setWrapText(true);
        info.setMaxWidth(700);
        info.setAlignment(Pos.CENTER);
        info.setFont(Font.font(FONT, FontWeight.NORMAL, 15));

        TextField nameField = new TextField();
        nameField.setPromptText("Your monster name");
        nameField.setMaxWidth(360);
        nameField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.94);" +
                "-fx-text-fill: #0f172a;" +
                "-fx-prompt-text-fill: #64748b;" +
                "-fx-font-family: '" + FONT + "';" +
                "-fx-font-size: 15;" +
                "-fx-font-weight: bold;" +
                "-fx-padding: 13 16 13 16;" +
                "-fx-background-radius: 12;"
        );

        Button enterButton = new Button("Enter Door");
        enterButton.setPrefSize(220, 48);
        enterButton.setStyle(mainButtonStyle("#f59e0b"));
        enterButton.setOnAction(e -> {
            String typedName = nameField.getText() == null ? "" : nameField.getText().trim();
            playerName = typedName.equals("") ? "Player" : typedName;
            showRoleScreen(stage);
        });
        nameField.setOnAction(e -> enterButton.fire());

        Button howToPlay = new Button("How to Play");
        howToPlay.setStyle(secondaryButtonStyle());
        howToPlay.setOnAction(e -> showInstructionsPopup());

        hero.getChildren().addAll(title, subtitle, previewImages, info, nameField, enterButton, howToPlay);
        screen.getChildren().addAll(splash, shade, hero);

        Scene scene = new Scene(screen, 1150, 780);
        stage.setScene(scene);
        stage.show();
    }

    private void showRoleScreen(Stage stage) {
        BorderPane screen = new BorderPane();
        screen.setPadding(new Insets(28));
        screen.setStyle("-fx-background-color: linear-gradient(to bottom right, #020617, #10213d 45%, #111827);");

        Label title = new Label("Choose your door, " + playerName);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 34));
        title.setEffect(new DropShadow(16, Color.rgb(0, 0, 0, 0.45)));
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setMargin(title, new Insets(0, 0, 24, 0));
        screen.setTop(title);

        HBox choices = new HBox(28);
        choices.setAlignment(Pos.CENTER);
        choices.getChildren().addAll(
                roleChoiceCard(stage, Role.SCARER, "role_scarer_ai.png", "SCARER", "Power, pressure, and big-door energy.", SCARER_RED),
                roleChoiceCard(stage, Role.LAUGHER, "role_laugher_ai.png", "LAUGHER", "Speed, chaos, and clean comeback plays.", LAUGHER_BLUE)
        );
        screen.setCenter(choices);

        Button backButton = new Button("Back");
        backButton.setStyle(secondaryButtonStyle());
        backButton.setOnAction(e -> showStartScreen(stage));
        BorderPane.setAlignment(backButton, Pos.CENTER);
        BorderPane.setMargin(backButton, new Insets(22, 0, 0, 0));
        screen.setBottom(backButton);

        Scene scene = new Scene(screen, 1220, 760);
        stage.setScene(scene);
        stage.show();
    }

    private VBox roleChoiceCard(Stage stage, Role role, String imageName, String title, String subtitle, String accent) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.setPrefSize(510, 560);
        card.setStyle(
                "-fx-background-color: rgba(15,23,42,0.92);" +
                "-fx-border-color: " + accent + ";" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;"
        );

        StackPane artFrame = new StackPane();
        artFrame.setPrefSize(460, 360);
        artFrame.setMaxSize(460, 360);
        artFrame.setStyle(
                "-fx-background-color: #020617;" +
                "-fx-border-color: rgba(255,255,255,0.14);" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;"
        );
        ImageView art = cinematicImage(imageName, 460, 360);
        artFrame.getChildren().add(art);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 30));

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setTextFill(Color.web("#cbd5e1"));
        subtitleLabel.setFont(Font.font(FONT, FontWeight.BOLD, 13));
        subtitleLabel.setWrapText(true);
        subtitleLabel.setAlignment(Pos.CENTER);

        Button chooseButton = new Button("Choose " + title);
        chooseButton.setPrefSize(230, 46);
        chooseButton.setStyle(mainButtonStyle(accent));
        chooseButton.setOnAction(e -> startGame(stage, role));

        card.getChildren().addAll(artFrame, titleLabel, subtitleLabel, chooseButton);
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.018);
            card.setScaleY(1.018);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        return card;
    }

    private void startGame(Stage stage, Role role) {
        try {
            game = new Game(role);
            buildGameScreen(stage);
            refreshAll(playerName + " entered the factory as " + role + ".");
            showNotification("Welcome, " + playerName, "You are playing as " + role + ".", role == Role.SCARER ? SCARER_RED : LAUGHER_BLUE);
        } catch (Exception e) {
            showError("Game could not start", e.getMessage());
        }
    }

    private void buildGameScreen(Stage stage) {
        sceneStack = new StackPane();
        root = new BorderPane();
        root.setPadding(new Insets(14));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #07111f, #0f172a 55%, #1f2937);");

        notificationLayer = new VBox(10);
        notificationLayer.setAlignment(Pos.TOP_CENTER);
        notificationLayer.setMouseTransparent(true);
        notificationLayer.setPadding(new Insets(92, 0, 0, 0));

        currentTurnLabel = new Label();
        currentTurnLabel.setFont(Font.font(FONT, FontWeight.BOLD, 15));
        currentTurnLabel.setTextFill(Color.web("#f8fafc"));

        diceLabel = new Label("Dice: -");
        diceLabel.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 18));
        diceLabel.setStyle(
                "-fx-background-color: #fbbf24;" +
                "-fx-text-fill: #111827;" +
                "-fx-padding: 9 16 9 16;" +
                "-fx-background-radius: 999;"
        );

        usePowerupBox = new CheckBox("Use powerup before rolling");
        usePowerupBox.setFont(Font.font(FONT, FontWeight.BOLD, 13));
        usePowerupBox.setTextFill(Color.web("#dbeafe"));

        rollButton = new Button("Roll Dice / Play Turn");
        rollButton.setStyle(mainButtonStyle(ENERGY_GREEN));
        rollButton.setOnAction(e -> playGuiTurn());

        instructionsButton = new Button("Instructions");
        instructionsButton.setStyle(secondaryButtonStyle());
        instructionsButton.setOnAction(e -> showInstructionsPopup());

        restartButton = new Button("Restart");
        restartButton.setStyle(secondaryButtonStyle());
        restartButton.setOnAction(e -> showStartScreen(stage));

        HBox topBar = new HBox(15, currentTurnLabel, diceLabel, usePowerupBox, rollButton, instructionsButton, restartButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 14, 12, 14));
        topBar.setStyle(
                "-fx-background-color: rgba(15,23,42,0.94);" +
                "-fx-border-color: #334155;" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 14;" +
                "-fx-border-radius: 14;"
        );

        root.setTop(topBar);

        boardGrid = new GridPane();
        boardGrid.setHgap(4);
        boardGrid.setVgap(4);
        boardGrid.setPadding(new Insets(12));
        boardGrid.setAlignment(Pos.CENTER);

        StackPane boardFrame = new StackPane(boardGrid);
        boardFrame.setPadding(new Insets(16));
        boardFrame.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #111827, #020617);" +
                "-fx-border-color: #334155;" +
                "-fx-border-width: 1;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;"
        );
        BorderPane.setMargin(boardFrame, new Insets(14, 14, 0, 0));
        root.setCenter(boardFrame);

        playerPanel = new VBox(8);
        opponentPanel = new VBox(8);
        stationedPanel = new VBox(6);

        winnerLabel = new Label("");
        winnerLabel.setFont(Font.font(FONT, FontWeight.BOLD, 20));
        winnerLabel.setTextFill(Color.web(ENERGY_GREEN));
        winnerLabel.setWrapText(true);

        actionLabel = new Label("Action log");
        actionLabel.setWrapText(true);
        actionLabel.setStyle(infoBoxStyle("#ecfeff", LAUGHER_BLUE));

        cardLabel = new Label("Card: handled internally by engine");
        cardLabel.setWrapText(true);
        cardLabel.setStyle(infoBoxStyle("#fff7ed", "#fdba74"));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(190);
        logArea.setStyle("-fx-control-inner-background: #0f172a; -fx-text-fill: #e2e8f0; -fx-font-family: 'Consolas';");
        
        deckPanel = new VBox(3);
        deckTitleLabel = sectionTitle("Card Deck");

        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(14));
        rightPanel.setPrefWidth(410);
        rightPanel.setStyle(
                "-fx-background-color: " + PANEL_LIGHT + ";" +
                "-fx-border-color: #dbe3ef;" +
                "-fx-border-width: 0 0 0 1;"
        );

       

        rightPanel.getChildren().addAll(
                sectionTitle("Winner"), winnerLabel,
                sectionTitle("Current Action"), actionLabel,
                sectionTitle("Card Info"), cardLabel,
                sectionTitle("Player"), playerPanel,
                sectionTitle("Opponent"), opponentPanel,
                sectionTitle("Stationed Monsters"), stationedPanel,
                sectionTitle("Game Log"), logArea,
                deckTitleLabel, deckPanel
        );
        ScrollPane scrollPane = new ScrollPane(rightPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setRight(scrollPane);

        sceneStack.getChildren().addAll(root, notificationLayer);

        Scene scene = new Scene(sceneStack, 1220, 760);
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
                            "GAME OVER\n\n" +
                            "Winner: " + cheatWinner.getName() + "\n" +
                            "Winner Role: " + cheatWinner.getRole() + "\n\n" +
                            "Player: " + game.getPlayer().getName() + "\n" +
                            "Player Role: " + game.getPlayer().getRole() + "\n" +
                            "Player Final Energy: " + game.getPlayer().getEnergy() + "\n\n" +
                            "Opponent: " + game.getOpponent().getName() + "\n" +
                            "Opponent Role: " + game.getOpponent().getRole() + "\n" +
                            "Opponent Final Energy: " + game.getOpponent().getEnergy();
                        winnerLabel.setText(winText);
                        actionLabel.setText(cheatWinner.getName() + " wins!");
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
        deckTitleLabel.setText("Card Deck (" + deck.size() + " remaining)");
        deckPanel.getChildren().clear();
        for (int i = 0; i < deck.size(); i++) {
            game.engine.cards.Card c = deck.get(i);
            Label cardRow = new Label((i == 0 ? "> [NEXT] " : "  " + (i + 1) + ". ") + c.getName());
            cardRow.setWrapText(true);
            if (i == 0) {
                cardRow.setStyle(
                        "-fx-background-color: #fef3c7;" +
                        "-fx-border-color: #f59e0b;" +
                        "-fx-padding: 7 10 7 10;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-font-weight: bold;"
                );
            } else {
                cardRow.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-padding: 5 9 5 9;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;"
                );
            }
            deckPanel.getChildren().add(cardRow);
        }
    }
    
    private void reportEnergyChanges() {
        int playerNow = game.getPlayer().getEnergy();
        int opponentNow = game.getOpponent().getEnergy();

        if (playerNow != previousPlayerEnergy) {
            appendLog(game.getPlayer().getName() + " energy changed: " 
                    + previousPlayerEnergy + " -> " + playerNow);
        }

        if (opponentNow != previousOpponentEnergy) {
            appendLog(game.getOpponent().getName() + " energy changed: " 
                    + previousOpponentEnergy + " -> " + opponentNow);
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
            animateDiceTo(game.getLastDiceRoll());

            lastNewPosition = before.getPosition();

            refreshAll(before.getName() + " moved from cell " + lastOldPosition + " to cell " + lastNewPosition + ".");

            animateBoard();

            Monster winner = game.getWinner();

            if (winner != null) {
                rollButton.setDisable(true);
                usePowerupBox.setDisable(true);

                String winText =
                        "GAME OVER\n\n" +
                        "Winner: " + winner.getName() + "\n" +
                        "Winner Role: " + winner.getRole() + "\n\n" +
                        "Player: " + game.getPlayer().getName() + "\n" +
                        "Player Role: " + game.getPlayer().getRole() + "\n" +
                        "Player Final Energy: " + game.getPlayer().getEnergy() + "\n\n" +
                        "Opponent: " + game.getOpponent().getName() + "\n" +
                        "Opponent Role: " + game.getOpponent().getRole() + "\n" +
                        "Opponent Final Energy: " + game.getOpponent().getEnergy();

                winnerLabel.setText(winText);
                actionLabel.setText(winner.getName() + " wins!");

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
        alert.setHeaderText("Game Won");

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
                if (card != lastNotifiedCard) {
                    lastNotifiedCard = card;
                    showNotification("Card pulled: " + card.getName(), cardEffectText(card), "#f59e0b");
                }
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
                return imageView("scarer_door.png", CELL_SIZE, CELL_SIZE);
            } else {
                return imageView("laugher_door.png", CELL_SIZE, CELL_SIZE);
            }
        }

        if (cell instanceof MonsterCell) {
            MonsterCell monsterCell = (MonsterCell) cell;
            return monsterImage(monsterCell.getCellMonster(), CELL_SIZE, CELL_SIZE);
        }

        if (cell instanceof CardCell) {
            return imageView("card.png", CELL_SIZE, CELL_SIZE);
        }

        if (cell instanceof ContaminationSock) {
            return imageView("sock.png", CELL_SIZE, CELL_SIZE);
        }

        if (cell instanceof ConveyorBelt) {
            return imageView("conveyor.png", CELL_SIZE, CELL_SIZE);
        }

        return imageView("yellow.png", CELL_SIZE, CELL_SIZE);
    }
    
    private void drawBoard() {

        boardGrid.getChildren().clear();

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {

            Cell cell = getCellAt(index);

            StackPane stack = new StackPane();
            stack.setMinSize(CELL_SIZE, CELL_SIZE);
            stack.setPrefSize(CELL_SIZE, CELL_SIZE);
            stack.setMaxSize(CELL_SIZE, CELL_SIZE);

            ImageView bg = getFullCellImage(cell);
            bg.setFitWidth(CELL_SIZE);
            bg.setFitHeight(CELL_SIZE);
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
                    "-fx-background-radius: 999;"
            );
            
            Label occupants = new Label(getOccupantsAt(index));
            occupants.setMinHeight(15);
            occupants.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.92);" +
                    "-fx-text-fill: #0f172a;" +
                    "-fx-font-size: 9;" +
                    "-fx-font-weight: 900;" +
                    "-fx-padding: 1 4 1 4;" +
                    "-fx-background-radius: 999;"
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
                        "-fx-background-radius: 999;"
                );
            }
            occupants.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.85);" +
                    "-fx-text-fill: #0f172a;" +
                    "-fx-font-size: 9;" +
                    "-fx-font-weight: 900;" +
                    "-fx-padding: 1 4 1 4;" +
                    "-fx-background-radius: 999;"
            );

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            overlay.getChildren().addAll(number, doorInfo, spacer, occupants);

            Pane effectOverlay = cellEffectOverlay(cell);
            if (effectOverlay == null) {
                stack.getChildren().addAll(bg, overlay);
            } else {
                stack.getChildren().addAll(bg, effectOverlay, overlay);
            }
            stack.setStyle(
                    "-fx-border-color: " + LINE_SOFT + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;"
            );

            if (game.getPlayer().getPosition() == index) {
                stack.setEffect(new DropShadow(18, Color.web(LAUGHER_BLUE)));
                stack.setStyle("-fx-border-color: " + LAUGHER_BLUE + "; -fx-border-width: 4; -fx-background-radius: 8; -fx-border-radius: 8;");
            }

            if (game.getOpponent().getPosition() == index) {
                stack.setEffect(new DropShadow(18, Color.web(SCARER_RED)));
                stack.setStyle("-fx-border-color: " + SCARER_RED + "; -fx-border-width: 4; -fx-background-radius: 8; -fx-border-radius: 8;");
            }

            if (game.getPlayer().getPosition() == index && game.getOpponent().getPosition() == index) {
                stack.setEffect(new DropShadow(20, Color.web("#a855f7")));
                stack.setStyle("-fx-border-color: #a855f7; -fx-border-width: 5; -fx-background-radius: 8; -fx-border-radius: 8;");
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
            text += "* ";
        }

        if (game.getCurrent().getPosition() == index) {
            text += "NOW";
        }

        return text;
    }

    private Pane cellEffectOverlay(Cell cell) {
        if (!(cell instanceof ConveyorBelt)) {
            return null;
        }

        Pane belt = new Pane();
        belt.setMouseTransparent(true);
        belt.setPrefSize(CELL_SIZE, CELL_SIZE);
        belt.setMaxSize(CELL_SIZE, CELL_SIZE);
        belt.setClip(new Rectangle(CELL_SIZE, CELL_SIZE));

        for (int i = -1; i < 4; i++) {
            Rectangle stripe = new Rectangle(13, CELL_SIZE + 22);
            stripe.setFill(Color.rgb(56, 189, 248, 0.56));
            stripe.setRotate(38);
            stripe.setTranslateX(i * 22);
            stripe.setTranslateY(-8);
            belt.getChildren().add(stripe);

            TranslateTransition glide = new TranslateTransition(Duration.millis(980), stripe);
            glide.setFromX(i * 22 - 12);
            glide.setToX(i * 22 + 18);
            glide.setCycleCount(Animation.INDEFINITE);
            glide.setAutoReverse(false);
            glide.play();
        }

        Label arrow = new Label(">>");
        arrow.setTextFill(Color.WHITE);
        arrow.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 16));
        arrow.setStyle("-fx-background-color: rgba(15,23,42,0.62); -fx-padding: 1 6 1 6; -fx-background-radius: 999;");
        arrow.setLayoutX(16);
        arrow.setLayoutY(22);
        belt.getChildren().add(arrow);

        return belt;
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
            row.setPadding(new Insets(8));
            row.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #dbe3ef;" +
                    "-fx-background-radius: 8;" +
                    "-fx-border-radius: 8;"
            );

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
            label.setFont(Font.font(FONT, FontWeight.NORMAL, 12));

            row.getChildren().addAll(img, label);
            stationedPanel.getChildren().add(row);
        }
    }

    private VBox monsterCard(Monster monster, boolean isPlayer) {
        VBox box = new VBox(9);
        box.setPadding(new Insets(12));

        if (game.getCurrent() == monster) {
            box.setStyle(
                    "-fx-background-color: #ecfdf5;" +
                    "-fx-border-color: " + ENERGY_GREEN + ";" +
                    "-fx-border-width: 3;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-radius: 12;"
            );
        } else {
            box.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-border-color: #dbe3ef;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-radius: 12;"
            );
        }

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        ImageView image = monsterImage(monster, 72, 72);

        VBox titleBlock = new VBox(4);
        Label name = new Label((isPlayer ? "PLAYER" : "OPPONENT") + " / " + monster.getName());
        name.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 14));
        name.setWrapText(true);

        Label rolePill = new Label(monster.getRole().toString());
        rolePill.setStyle(
                "-fx-background-color: " + (monster.getRole() == Role.SCARER ? "#fee2e2" : "#dbeafe") + ";" +
                "-fx-text-fill: " + (monster.getRole() == Role.SCARER ? "#991b1b" : "#1e40af") + ";" +
                "-fx-font-size: 11;" +
                "-fx-font-weight: 900;" +
                "-fx-padding: 3 8 3 8;" +
                "-fx-background-radius: 999;"
        );
        titleBlock.getChildren().addAll(name, rolePill);

        top.getChildren().addAll(image, titleBlock);

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
        details.setFont(Font.font(FONT, FontWeight.NORMAL, 12));
        details.setStyle("-fx-text-fill: #334155;");

        Label energyLabel = new Label("Energy / " + monster.getEnergy() + " of 1000");
        energyLabel.setFont(Font.font(FONT, FontWeight.BOLD, 12));
        energyLabel.setStyle("-fx-text-fill: #0f172a;");

        HBox energyBar = canisterEnergyBar(monster.getEnergy(), monster.getRole());

        box.getChildren().addAll(top, details, energyLabel, energyBar);

        return box;
    }

    private HBox canisterEnergyBar(int energy, Role role) {
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMinHeight(28);

        int filled = Math.max(0, Math.min(10, (int) Math.ceil(energy / 100.0)));
        String fillColor = role == Role.SCARER ? SCARER_RED : ENERGY_GREEN;

        for (int i = 0; i < 10; i++) {
            StackPane canister = new StackPane();
            canister.setPrefSize(27, 28);
            canister.setMaxSize(27, 28);

            Rectangle body = new Rectangle(22, 24);
            body.setArcWidth(8);
            body.setArcHeight(8);
            body.setFill(i < filled ? Color.web(fillColor) : Color.web("#e2e8f0"));
            body.setStroke(Color.web("#64748b"));
            body.setStrokeWidth(1);

            Rectangle cap = new Rectangle(12, 4);
            cap.setArcWidth(3);
            cap.setArcHeight(3);
            cap.setFill(Color.web("#94a3b8"));
            StackPane.setAlignment(cap, Pos.TOP_CENTER);

            Circle shine = new Circle(3, Color.rgb(255, 255, 255, i < filled ? 0.58 : 0.20));
            shine.setTranslateX(-5);
            shine.setTranslateY(-4);

            canister.getChildren().addAll(body, cap, shine);
            row.getChildren().add(canister);
        }

        return row;
    }

    private ImageView monsterImage(Monster monster, double width, double height) {
        String name = monster.getName().toLowerCase();
        
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

        if (image.isError()) {
            return yellowImage(width, height);
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        return imageView;
    }

    private ImageView cinematicImage(String fileName, double width, double height) {
        Image image = new Image("file:images/" + fileName);
        if (image.isError()) {
            image = new Image("file:images/scarer_door.png");
        }

        ImageView view = new ImageView(image);
        view.setFitWidth(width);
        view.setFitHeight(height);
        view.setPreserveRatio(false);
        view.setSmooth(true);
        return view;
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

    private void animateDiceTo(int finalValue) {
        Timeline timeline = new Timeline();

        for (int i = 0; i < 10; i++) {
            final int value = 1 + (int) (Math.random() * 6);

            KeyFrame frame = new KeyFrame(
                    Duration.millis(i * 60),
                    e -> diceLabel.setText("Dice: " + value)
            );

            timeline.getKeyFrames().add(frame);
        }

        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(680),
                e -> {
                    diceLabel.setText("Dice: " + finalValue);
                    ScaleTransition pop = new ScaleTransition(Duration.millis(160), diceLabel);
                    pop.setFromX(1.0);
                    pop.setFromY(1.0);
                    pop.setToX(1.12);
                    pop.setToY(1.12);
                    pop.setCycleCount(2);
                    pop.setAutoReverse(true);
                    pop.play();
                }
        ));

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
        label.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 14));
        label.setStyle("-fx-text-fill: #0f172a; -fx-padding: 8 0 0 0;");
        return label;
    }

    private String infoBoxStyle(String background, String border) {
        return "-fx-background-color: " + background + ";" +
                "-fx-border-color: " + border + ";" +
                "-fx-border-width: 1;" +
                "-fx-padding: 11;" +
                "-fx-background-radius: 10;" +
                "-fx-border-radius: 10;" +
                "-fx-font-family: '" + FONT + "';";
    }

    private String mainButtonStyle(String color) {
        return "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-family: '" + FONT + "';" +
                "-fx-font-weight: 900;" +
                "-fx-padding: 10 16 10 16;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 4);" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #1f2937;" +
                "-fx-text-fill: white;" +
                "-fx-font-family: '" + FONT + "';" +
                "-fx-font-weight: 800;" +
                "-fx-padding: 9 14 9 14;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: #475569;" +
                "-fx-border-radius: 10;" +
                "-fx-cursor: hand;";
    }

    private void appendLog(String text) {
        if (logArea != null && text != null && !text.equals("")) {
            logArea.appendText(text + "\n");
        }
    }

    private void showNotification(String title, String message, String accent) {
        if (notificationLayer == null) {
            return;
        }

        VBox toast = new VBox(4);
        toast.setMaxWidth(520);
        toast.setPadding(new Insets(13, 16, 13, 16));
        toast.setStyle(
                "-fx-background-color: rgba(15,23,42,0.96);" +
                "-fx-border-color: " + accent + ";" +
                "-fx-border-width: 0 0 0 5;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 22, 0, 0, 8);"
        );

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font(FONT, FontWeight.EXTRA_BOLD, 15));

        Label messageLabel = new Label(message == null ? "" : message);
        messageLabel.setTextFill(Color.web("#cbd5e1"));
        messageLabel.setFont(Font.font(FONT, FontWeight.BOLD, 12));
        messageLabel.setWrapText(true);

        toast.getChildren().addAll(titleLabel, messageLabel);
        notificationLayer.getChildren().add(0, toast);

        toast.setTranslateY(-24);
        toast.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), toast);
        slide.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), toast);
        fadeIn.setToValue(1);

        PauseTransition hold = new PauseTransition(Duration.seconds(2.5));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(360), toast);
        fadeOut.setToValue(0);

        TranslateTransition lift = new TranslateTransition(Duration.millis(360), toast);
        lift.setToY(-18);

        ParallelTransition in = new ParallelTransition(slide, fadeIn);
        ParallelTransition out = new ParallelTransition(fadeOut, lift);
        SequentialTransition sequence = new SequentialTransition(in, hold, out);
        sequence.setOnFinished(e -> notificationLayer.getChildren().remove(toast));
        sequence.play();
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
                "- * = last moved monster\n" +
                "- NOW = current turn monster\n\n" +
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
