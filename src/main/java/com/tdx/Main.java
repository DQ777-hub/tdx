package com.tdx;

import com.GameSettings.Game;
import com.Towers.BasicCliffTower;
import com.Towers.BasicTower;
import com.Towers.Farm;
import com.Towers.Minigunner;
import com.Towers.Tower;
import com.Towers.Tower.TowerSurface;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.media.AudioClip;
import com.tdx.Bloon.BloonType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Main game controller - handles UI, logic, waves, and tower placement. No cap.
 */
public class Main extends Application {
    private PaneWrapper playfield;
    private Label moneyLabel;
    private Label livesLabel;
    private Label waveLabel;
    private Label farmIncomeLabel;
    private Label statusLabel;
    private Button startWaveButton;
    private Button buildGroundTowerButton;
    private Button buildCliffTowerButton;
    private Button buildMinigunnerButton;
    private Button buildFarmButton;
    private Button speedButton;
    private Button autoSkipButton;
    private VBox upgradePanel;
    private Label upgradeTitleLabel;
    private Label upgradeStatsLabel;
    private Button upgradeButton;

    private enum TowerType {
        DART, CLIFF, MINIGUNNER, FARM
    }

    private enum GameState {
        TITLE, PLAYING, WON
    }

    private GameState gameState = GameState.TITLE;
    private Stage primaryStage;
    private String stylesheet;
    private TowerType selectedTowerType = TowerType.DART;
    private TowerSurface selectedSurface = TowerSurface.GROUND;
    private Tower selectedTower = null;
    private Circle selectedTowerRangeDisplay = new Circle();
    private int money = 250;
    private int lives = 20;
    private int wave = 0;
    private int totalFarmIncome = 0;
    private boolean placingTower = false;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean previousWaveRunning = false;
    private double spawnTimer = 0;
    private double gameSpeed = 1.0; // 1.0 = normal, 2.0 = 2x speed
    private boolean autoSkipEnabled = false;

    // Scaling variables
    private double scaleX = 1.0;
    private double scaleY = 1.0;

    private List<Point2D> scaledPath;

    private final List<Bloon> bloons = new ArrayList<>();
    private final List<com.Towers.Tower> towers = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final Circle towerPreview = new Circle(18, Color.TRANSPARENT);
    private final Circle towerRangePreview = new Circle(120, Color.color(1, 1, 1, 0.12));

    // Sound effects and music
    private AudioClip popSound;
    private AudioClip titleMusic;
    private AudioClip gameplayMusic;
    private int currentGameplayTrack = 0;
    private List<String> gameplayTracks = new ArrayList<>();

    /**
     * Load all sounds - pop, title music, and gameplay tracks. It's giving audio
     * excellence, fr.
     */
    private void loadSounds() {
        try {
            // Load pop sound
            java.net.URL popUrl = getClass().getResource("/Medias/Musics/pop.wav");
            if (popUrl != null) {
                popSound = new AudioClip(popUrl.toExternalForm());
                popSound.setVolume(0.7);
            }

            // Load title music
            java.net.URL titleUrl = getClass().getResource("/Medias/Musics/title.wav");
            if (titleUrl != null) {
                titleMusic = new AudioClip(titleUrl.toExternalForm());
                titleMusic.setVolume(0.5);
            }

            // Load gameplay tracks - add them as they exist (up to 3 tracks)
            for (int i = 1; i <= 4; i++) {
                try {
                    java.net.URL gameplayUrl = getClass().getResource("/Medias/Musics/gameplay/track" + i + ".wav");
                    if (gameplayUrl != null) {
                        gameplayTracks.add(gameplayUrl.toExternalForm());
                    }
                } catch (Exception e) {
                    break; // No more tracks
                }
            }

            // Fallback: single gameplay.wav
            if (gameplayTracks.isEmpty()) {
                java.net.URL gameplayUrl = getClass().getResource("/Medias/Musics/gameplay.wav");
                if (gameplayUrl != null) {
                    gameplayTracks.add(gameplayUrl.toExternalForm());
                }
            }
        } catch (Exception e) {
            // Music loading failed silently
        }
    }

    /** Play title music on loop - vibe check passed, no cap. */
    private void playTitleMusic() {
        if (titleMusic != null) {
            titleMusic.setCycleCount(AudioClip.INDEFINITE);
            titleMusic.play();
        }
    }

    /** Stop title music - time to get into the action, slay. */
    private void stopTitleMusic() {
        if (titleMusic != null) {
            titleMusic.stop();
        }
    }

    /** Play random gameplay music track, fr fr. */
    private void playGameplayMusic() {
        if (!gameplayTracks.isEmpty()) {
            currentGameplayTrack = new Random().nextInt(gameplayTracks.size());
            gameplayMusic = new AudioClip(gameplayTracks.get(currentGameplayTrack));
            gameplayMusic.setVolume(0.4);
            gameplayMusic.setCycleCount(AudioClip.INDEFINITE);
            gameplayMusic.play();
        }
    }

    /** Stop gameplay music - silence before victory, periodt. */
    private void stopGameplayMusic() {
        if (gameplayMusic != null) {
            gameplayMusic.stop();
        }
    }

    /** Stop all music when game ends - moment of silence before reflection, fr. */
    private void stopAllMusic() {
        stopTitleMusic();
        stopGameplayMusic();
    }

    /**
     * Play pop sound - hits different when bloons destroyed, chef's kiss energy.
     */
    private void playPopSound() {
        if (popSound != null) {
            popSound.stop();
            popSound.play();
        }
    }

    private int currentWaveIndex = 0;
    private List<Object[]> currentWaveComposition = new ArrayList<>();
    private int currentCompositionIndex = 0;
    private int spawnedFromComposition = 0;

    /** Entry point - lowkey where the magic starts, fr fr. */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Setup stage and show title screen - lowkey the gateway to the gaming
     * experience.
     */
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle(Game.title);
        primaryStage.setFullScreen(true);
        stylesheet = getClass().getResource("/styles.css").toExternalForm();
        showTitleScreen();
        primaryStage.show();
    }

    /** Show title screen - where players get hyped. The aesthetic? Immaculate. */
    private void showTitleScreen() {
        loadSounds();
        playTitleMusic();

        VBox titleContainer = new VBox();
        titleContainer.getStyleClass().add("title-container");
        titleContainer.setAlignment(Pos.CENTER);
        titleContainer.setSpacing(20);

        Label titleLabel = new Label("TDX TOWER DEFENSE");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Defend the kingdom against waves of bloons!");
        subtitleLabel.getStyleClass().add("subtitle-label");

        Label instructionLabel = new Label("12 Waves • 4 Tower Types • Survive to Win");
        instructionLabel.getStyleClass().add("instruction-label");

        Button playButton = new Button("START GAME");
        playButton.getStyleClass().add("primary-button");
        playButton.setOnAction(event -> {
            stopTitleMusic();
            startGame();
        });

        Button exitButton = new Button("EXIT");
        exitButton.getStyleClass().add("exit-button");
        exitButton.setOnAction(event -> System.exit(0));

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonsBox.getChildren().addAll(playButton, exitButton);

        titleContainer.getChildren().addAll(titleLabel, subtitleLabel, instructionLabel, new Separator(),
                buttonsBox);
        titleContainer.setPadding(new Insets(50));

        Scene titleScene = new Scene(titleContainer);
        titleScene.getStylesheets().add(stylesheet);
        primaryStage.setScene(titleScene);
    }

    /**
     * Setup playfield, buttons, labels, and animation loop - main character energy,
     * slay.
     */
    private void startGame() {
        gameState = GameState.PLAYING;

        // 1920x1080 because I have a 1920x1080 monitor
        playfield = new PaneWrapper(1920, 1080);
        playfield.getStyleClass().add("playfield");
        playfield.setMaxWidth(1920);
        playfield.setMaxHeight(1080);
        playfield.setPrefWidth(1920);
        playfield.setPrefHeight(1080);
        HBox.setHgrow(playfield, Priority.ALWAYS);
        VBox.setVgrow(playfield, Priority.ALWAYS);
        drawPath();
        drawSpawnArea();

        // Start gameplay music
        playGameplayMusic();

        buildGroundTowerButton = new Button("Build Dart Monkey (150)");
        buildGroundTowerButton.setMaxWidth(Double.MAX_VALUE);
        buildGroundTowerButton.setOnAction(event -> startTowerPlacement(TowerType.DART));

        buildCliffTowerButton = new Button("Build Cliff Archer (180)");
        buildCliffTowerButton.setMaxWidth(Double.MAX_VALUE);
        buildCliffTowerButton.setOnAction(event -> startTowerPlacement(TowerType.CLIFF));

        buildMinigunnerButton = new Button("Build Minigunner (2300)");
        buildMinigunnerButton.setMaxWidth(Double.MAX_VALUE);
        buildMinigunnerButton.setOnAction(event -> startTowerPlacement(TowerType.MINIGUNNER));

        buildFarmButton = new Button("Build Farm (1200)");
        buildFarmButton.setMaxWidth(Double.MAX_VALUE);
        buildFarmButton.setOnAction(event -> startTowerPlacement(TowerType.FARM));

        startWaveButton = new Button("Start Wave");
        startWaveButton.setMaxWidth(Double.MAX_VALUE);
        startWaveButton.setOnAction(event -> spawnNextWave());

        speedButton = new Button("2x Speed: OFF");
        speedButton.setMaxWidth(Double.MAX_VALUE);
        speedButton.getStyleClass().add("speed-button");
        speedButton.setOnAction(event -> toggleSpeed());

        autoSkipButton = new Button("Auto Skip: OFF");
        autoSkipButton.setMaxWidth(Double.MAX_VALUE);
        autoSkipButton.getStyleClass().add("autoskip-button");
        autoSkipButton.setOnAction(event -> toggleAutoSkip());

        moneyLabel = createStatusLabel("Money: " + money);
        livesLabel = createStatusLabel("Lives: " + lives);
        waveLabel = createStatusLabel("Wave: " + wave);
        farmIncomeLabel = createStatusLabel("Farm Income: $0");
        statusLabel = createStatusLabel("Click Build, then place a tower.");

        upgradePanel = createUpgradePanel();

        VBox statsBox = new VBox(10, moneyLabel, livesLabel, waveLabel, farmIncomeLabel, buildGroundTowerButton,
                buildCliffTowerButton,
                buildMinigunnerButton, buildFarmButton, startWaveButton, new Separator(), speedButton, autoSkipButton,
                statusLabel, new Separator(), upgradePanel);
        statsBox.setPadding(new Insets(10));
        statsBox.setMinWidth(220);
        statsBox.setMaxWidth(220);
        statsBox.setPrefWidth(220);
        statsBox.getStyleClass().add("stats-box");

        BorderPane root = new BorderPane();
        root.setCenter(playfield);
        root.setRight(statsBox);
        root.setPrefWidth(Double.MAX_VALUE);
        root.setPrefHeight(Double.MAX_VALUE);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(stylesheet);
        playfield.setOnMouseMoved(event -> updatePreview(event));
        playfield.setOnMouseClicked(event -> handleClick(event.getX(), event.getY()));

        // Setup tower preview nodes before listeners are triggered
        towerPreview.setStroke(Color.YELLOW);
        towerPreview.setStrokeWidth(2);
        towerPreview.setFill(Color.color(1, 1, 1, 0.14));
        towerPreview.setVisible(false);
        towerRangePreview.setStroke(Color.TRANSPARENT);
        towerRangePreview.setVisible(false);

        // Listen for playfield size changes to update scaling
        playfield.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateScaling();
            redrawGame();
        });
        playfield.heightProperty().addListener((obs, oldVal, newVal) -> {
            updateScaling();
            redrawGame();
        });

        primaryStage.setScene(scene);

        // Initial scaling update
        updateScaling();

        AnimationTimer loop = new AnimationTimer() {
            private long lastTime = 0;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double elapsedSeconds = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                updateGame(elapsedSeconds);
            }
        };
        loop.start();
    }

    /**
     * Create styled UI label - white text on dark bg? Absolutely immaculate, fr.
     */
    private Label createStatusLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("status-label");
        label.setWrapText(true);
        label.setMaxWidth(200);
        return label;
    }

    /**
     * Create upgrade panel - shows tower stats and upgrade options, hits different,
     * slay.
     */
    private VBox createUpgradePanel() {
        upgradeTitleLabel = new Label("No tower selected");
        upgradeTitleLabel.setTextFill(Color.WHITE);
        upgradeTitleLabel.setFont(Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 14));
        upgradeTitleLabel.setWrapText(true);
        upgradeTitleLabel.setMaxWidth(200);

        upgradeStatsLabel = new Label("Click on a tower to see upgrade options");
        upgradeStatsLabel.setTextFill(Color.LIGHTGRAY);
        upgradeStatsLabel.setFont(Font.font(12));
        upgradeStatsLabel.setWrapText(true);
        upgradeStatsLabel.setMaxWidth(200);

        upgradeButton = new Button("Upgrade");
        upgradeButton.setMaxWidth(Double.MAX_VALUE);
        upgradeButton.getStyleClass().add("upgrade-button");
        upgradeButton.setDisable(true);
        upgradeButton.setOnAction(event -> doUpgrade());

        VBox panel = new VBox(10, upgradeTitleLabel, upgradeStatsLabel, upgradeButton);
        panel.setPadding(new Insets(10));
        panel.getStyleClass().add("upgrade-panel");
        return panel;
    }

    /**
     * Update scaling based on playfield size - responsive design that's bussin', no
     * cap.
     */
    private void updateScaling() {
        scaleX = playfield.getWidth() / GameConfig.BASE_WIDTH;
        scaleY = playfield.getHeight() / GameConfig.BASE_HEIGHT;

        // Recalculate scaled path
        scaledPath = new ArrayList<>();
        for (Point2D p : GameConfig.PATH) {
            scaledPath.add(new Point2D(p.getX() * scaleX, p.getY() * scaleY));
        }
    }

    /**
     * Get scaled path - keeps bloon path perfect at any resolution, lowkey
     * essential.
     */
    private List<Point2D> getScaledPath() {
        if (scaledPath == null) {
            updateScaling();
        }
        return scaledPath;
    }

    /**
     * Redraw entire scene - clearing and refreshing everything is chef's kiss every
     * time.
     */
    private void redrawGame() {
        playfield.getChildren().clear();
        drawPath();
        drawSpawnArea();
        drawCliffArea();
        playfield.getChildren().addAll(towerRangePreview, towerPreview, selectedTowerRangeDisplay);

        // Redraw all towers
        for (Tower tower : towers) {
            playfield.getChildren().add(tower.getView());
        }

        // Redraw all bloons
        for (Bloon bloon : bloons) {
            playfield.getChildren().add(bloon.getView());
        }

        // Redraw all projectiles
        for (Projectile projectile : projectiles) {
            playfield.getChildren().add(projectile.getView());
        }
    }

    /**
     * Draw bloon path - the golden road where they travel. The path is life, fr.
     */
    private void drawPath() {
        List<Point2D> scaledPath = getScaledPath();
        for (int i = 0; i < scaledPath.size() - 1; i++) {
            Point2D start = scaledPath.get(i);
            Point2D end = scaledPath.get(i + 1);
            Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
            line.setStroke(Color.web("#e0d6a0"));
            line.setStrokeWidth(32 * scaleX);
            line.setOpacity(0.45);
            playfield.getChildren().add(line);
        }
    }

    /**
     * Draw spawn area - cool zone where bloons pop out. Styling is immaculate, no
     * cap.
     */
    private void drawSpawnArea() {
        List<Point2D> scaledPath = getScaledPath();
        Point2D start = scaledPath.get(0);
        Rectangle spawn = new Rectangle(start.getX() - 30 * scaleX, start.getY() - 30 * scaleY, 60 * scaleX,
                60 * scaleY);
        spawn.setFill(Color.color(0, 0, 0, 0.15));
        spawn.setStroke(Color.WHITE);
        spawn.setStrokeWidth(1.5);
        playfield.getChildren().add(spawn);
    }

    /**
     * Start tower placement mode - showing preview of where tower goes. Chef's
     * kiss, slay.
     */
    private void startTowerPlacement(TowerType towerType) {
        int cost = 0;
        switch (towerType) {
            case DART:
                cost = 150;
                selectedSurface = TowerSurface.GROUND;
                break;
            case CLIFF:
                cost = 180;
                selectedSurface = TowerSurface.CLIFF;
                break;
            case MINIGUNNER:
                cost = 2300;
                selectedSurface = TowerSurface.GROUND;
                break;
            case FARM:
                cost = 1200;
                selectedSurface = TowerSurface.GROUND;
                break;
        }

        if (money < cost) {
            statusLabel.setText("Not enough money for that tower.");
            return;
        }

        selectedTowerType = towerType;
        placingTower = true;
        towerPreview.setVisible(true);
        towerRangePreview.setVisible(true);

        // Set tower and range preview radii based on tower type
        double scale = Math.max(scaleX, scaleY);
        towerPreview.setRadius(18 * scale);

        switch (towerType) {
            case DART:
                towerRangePreview.setRadius(120 * scale);
                statusLabel.setText("Click on empty ground to place the tower.");
                break;
            case CLIFF:
                towerRangePreview.setRadius(140 * scale);
                statusLabel.setText("Click inside the cliff zone to place the cliff tower.");
                break;
            case MINIGUNNER:
                towerRangePreview.setRadius(100 * scale);
                statusLabel.setText("Click to place the Minigunner.");
                break;
            case FARM:
                towerRangePreview.setRadius(0 * scale);
                towerRangePreview.setVisible(false);
                statusLabel.setText("Click to place the Farm.");
                break;
        }
    }

    /**
     * Update tower preview on mouse move - real-time feedback that's bussin', no
     * lie.
     */
    private void updatePreview(javafx.scene.input.MouseEvent event) {
        if (!placingTower) {
            towerPreview.setVisible(false);
            towerRangePreview.setVisible(false);
            return;
        }

        double x = event.getX();
        double y = event.getY();

        // Check bounds
        if (x < 0 || x > playfield.getWidth() || y < 0 || y > playfield.getHeight()) {
            towerPreview.setVisible(false);
            towerRangePreview.setVisible(false);
            return;
        }

        towerPreview.setVisible(true);
        towerRangePreview.setVisible(true);
        towerPreview.setCenterX(x);
        towerPreview.setCenterY(y);
        towerRangePreview.setCenterX(x);
        towerRangePreview.setCenterY(y);

        if (selectedSurface == TowerSurface.CLIFF) {
            towerPreview.setStroke(Color.CYAN);
            towerRangePreview.setFill(Color.color(0, 1, 1, 0.08));
        } else {
            towerPreview.setStroke(Color.LIME);
            towerRangePreview.setFill(Color.color(0.2, 1, 0.2, 0.08));
        }

        if (canPlaceTower(x, y, selectedSurface)) {
            towerPreview.setFill(Color.color(0.2, 1, 0.2, 0.24));
        } else {
            towerPreview.setFill(Color.color(1, 0.2, 0.2, 0.24));
        }
    }

    /**
     * Handle click - decides if placing or selecting tower. Gatekeeper with
     * precision, fr.
     */
    private void handleClick(double sceneX, double sceneY) {
        if (gameOver) {
            return;
        }

        // Only respond to clicks within the playfield area
        if (sceneX < 0 || sceneX > playfield.getWidth() || sceneY < 0 || sceneY > playfield.getHeight()) {
            return;
        }

        if (!placingTower && trySelectTower(sceneX, sceneY)) {
            return;
        }
        if (placingTower) {
            attemptBuild(sceneX, sceneY);
        }
    }

    /**
     * Try to select tower - essential for upgrades and works flawlessly, lowkey
     * fire.
     */
    private boolean trySelectTower(double sceneX, double sceneY) {
        for (Tower tower : towers) {
            if (tower.contains(sceneX, sceneY)) {
                selectTower(tower);
                return true;
            }
        }
        // Clicked empty space, deselect
        selectTower(null);
        return false;
    }

    /**
     * Select tower and update upgrade panel - shows what tower can become. Info
     * display? Immaculate.
     */
    private void selectTower(Tower tower) {
        selectedTower = tower;
        selectedTowerRangeDisplay.setFill(Color.TRANSPARENT);

        if (tower == null) {
            upgradeTitleLabel.setText("No tower selected");
            upgradeStatsLabel.setText("Click on a tower to see upgrade options");
            upgradeButton.setDisable(true);
            return;
        }

        upgradeTitleLabel.setText(tower.name + " - Level " + tower.level);
        upgradeButton.setDisable(!tower.canUpgrade() || money < tower.getUpgradeCost());

        StringBuilder stats = new StringBuilder();
        if (tower instanceof BasicTower) {
            BasicTower t = (BasicTower) tower;
            stats.append("Damage: ").append(t.getDisplayDamage()).append(" -> ").append(t.getDisplayDamage() + 1)
                    .append("\n");
            stats.append("Fire Rate: ").append(String.format("%.1f", t.getDisplayFireRate())).append(" -> ")
                    .append(String.format("%.1f", t.getDisplayFireRate() + 0.2)).append("\n");
            if (tower.level >= tower.maxLevel) {
                stats.append("Piercing: YES\n");
            }
        } else if (tower instanceof BasicCliffTower) {
            BasicCliffTower t = (BasicCliffTower) tower;
            stats.append("Damage: ").append(t.getDisplayDamage()).append(" -> ").append(t.getDisplayDamage() + 1)
                    .append("\n");
            stats.append("Fire Rate: ").append(String.format("%.1f", t.getDisplayFireRate())).append(" -> ")
                    .append(String.format("%.1f", t.getDisplayFireRate() + 0.2)).append("\n");
        } else if (tower instanceof Minigunner) {
            Minigunner t = (Minigunner) tower;
            stats.append("Damage: ").append(t.getDisplayDamage()).append(" -> ").append(t.getDisplayDamage() + 1)
                    .append("\n");
            stats.append("Fire Rate: ").append(String.format("%.1f", t.getDisplayFireRate())).append(" -> ")
                    .append(String.format("%.1f", t.getDisplayFireRate() + 0.5)).append("\n");
            if (tower.level >= tower.maxLevel) {
                stats.append("Piercing: YES\n");
            }
        } else if (tower instanceof Farm) {
            Farm t = (Farm) tower;
            stats.append("Income: $").append(String.format("%.1f", t.getDisplayIncome())).append("/sec").append("\n");
            stats.append("Next Level: $").append(String.format("%.1f", t.getDisplayIncome() + 0.5)).append("/sec")
                    .append("\n");
        }

        if (!(tower instanceof Farm)) {
            stats.append("Range: ").append((int) tower.range).append(" -> ").append((int) (tower.range + 15))
                    .append("\n");
        }
        if (tower.canUpgrade()) {
            stats.append("Cost: $").append(tower.getUpgradeCost());
        } else {
            stats.append("Max Level Reached!");
        }
        upgradeStatsLabel.setText(stats.toString());

        // Show range circle
        selectedTowerRangeDisplay.setCenterX(tower.x);
        selectedTowerRangeDisplay.setCenterY(tower.y);
        selectedTowerRangeDisplay.setRadius(tower.range * Math.max(scaleX, scaleY));
        selectedTowerRangeDisplay.setFill(Color.color(1, 1, 1, 0.1));
        selectedTowerRangeDisplay.setStroke(Color.YELLOW);
        selectedTowerRangeDisplay.setStrokeWidth(2);
    }

    /**
     * Perform upgrade - check money, apply, and refresh UI. Upgrades hit different,
     * slay.
     */
    private void doUpgrade() {
        if (selectedTower == null || !selectedTower.canUpgrade()) {
            return;
        }
        int cost = selectedTower.getUpgradeCost();
        if (money < cost) {
            statusLabel.setText("Not enough money!");
            return;
        }
        money -= cost;
        selectedTower.upgrade();
        updateLabels();
        selectTower(selectedTower); // Refresh the panel
        statusLabel.setText(selectedTower.name + " upgraded to level " + selectedTower.level + "!");
    }

    /**
     * Attempt to build tower - check valid placement, deduct money, add tower. Main
     * character energy.
     */
    private void attemptBuild(double sceneX, double sceneY) {
        if (!placingTower || gameOver) {
            return;
        }

        if (!canPlaceTower(sceneX, sceneY, selectedSurface)) {
            statusLabel.setText("Unable to place tower here.");
            return;
        }

        Tower tower;
        double scale = Math.max(scaleX, scaleY);

        switch (selectedTowerType) {
            case DART:
                tower = new BasicTower(sceneX, sceneY, scale);
                break;
            case CLIFF:
                tower = new BasicCliffTower(sceneX, sceneY, scale);
                break;
            case MINIGUNNER:
                tower = new Minigunner(sceneX, sceneY, scale);
                break;
            case FARM:
                tower = new Farm(sceneX, sceneY, scale);
                break;
            default:
                return;
        }

        if (money < tower.cost) {
            statusLabel.setText("Not enough money to place this tower.");
            return;
        }

        towers.add(tower);
        playfield.getChildren().add(tower.getView());
        money -= tower.cost;
        updateLabels();
        placingTower = false;
        towerPreview.setVisible(false);
        towerRangePreview.setVisible(false);
        statusLabel.setText("Tower placed. Click a tower to upgrade it.");
    }

    /**
     * Validate tower placement - check bounds, path, terrain, and spacing. Heavy
     * lifting, periodt.
     */
    private boolean canPlaceTower(double x, double y, TowerSurface surface) {
        // Check bounds within playfield with small margin
        double margin = 40;
        if (x < margin || x > playfield.getWidth() - margin || y < margin || y > playfield.getHeight() - margin) {
            return false;
        }

        if (isOnPath(x, y)) {
            return false;
        }

        if (surface == TowerSurface.CLIFF && !isCliffArea(x, y)) {
            return false;
        }
        if (surface == TowerSurface.GROUND && isCliffArea(x, y)) {
            return false;
        }

        double minTowerDistance = 50 * Math.max(scaleX, scaleY);
        for (com.Towers.Tower tower : towers) {
            double dx = tower.x - x;
            double dy = tower.y - y;
            if (Math.hypot(dx, dy) < minTowerDistance) {
                return false;
            }
        }

        return true;
    }

    /** Check if point is on bloon path - sacred zone we gotta respect, fr. */
    private boolean isOnPath(double x, double y) {
        List<Point2D> scaledPath = getScaledPath();
        for (int i = 0; i < scaledPath.size() - 1; i++) {
            Point2D start = scaledPath.get(i);
            Point2D end = scaledPath.get(i + 1);
            double distance = pointToSegmentDistance(x, y, start, end);
            if (distance < 30 * scaleX) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if in cliff area - special zone where cliff towers belong. Mechanics
     * slap, fr.
     */
    private boolean isCliffArea(double x, double y) {
        double minX = 20 * scaleX;
        double maxX = (20 + 240) * scaleX;
        double minY = 20 * scaleY;
        double maxY = (20 + 160) * scaleY;
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * Draws the cliff area on the playfield. It's that beautiful cyan rectangle
     * that shows
     * where cliff towers belong. The aesthetic is immaculate and the visual
     * communication is chef's kiss.
     */
    private void drawCliffArea() {
        Rectangle cliffArea = new Rectangle(20 * scaleX, 20 * scaleY, 240 * scaleX, 160 * scaleY);
        cliffArea.setFill(Color.color(0.3, 0.8, 1, 0.12));
        cliffArea.setStroke(Color.web("#88d7ff"));
        cliffArea.setStrokeWidth(1.5);
        playfield.getChildren().add(cliffArea);
    }

    /**
     * Calculates the shortest distance from a point to a line segment.
     * Used for path collision detection. Lowkey this is some mathematical
     * excellence right here,
     * and it's absolutely essential for the game to work correctly, no cap.
     *
     * @param px    the X coordinate of the point
     * @param py    the Y coordinate of the point
     * @param start the starting point of the segment
     * @param end   the ending point of the segment
     * @return the shortest distance from the point to the segment
     */
    private double pointToSegmentDistance(double px, double py, Point2D start, Point2D end) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        if (dx == 0 && dy == 0) {
            dx = px - start.getX();
            dy = py - start.getY();
            return Math.hypot(dx, dy);
        }
        double t = ((px - start.getX()) * dx + (py - start.getY()) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        double projX = start.getX() + t * dx;
        double projY = start.getY() + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    /**
     * Spawns the next wave of bloons. This method increments the wave counter,
     * loads the wave
     * composition, and starts spawning bloons on a timer. When a new wave starts,
     * the intensity
     * goes up and it's absolutely bussin' - this is where the real challenge
     * begins, fr fr.
     */
    private void spawnNextWave() {
        if (gameOver || gameWon) {
            return;
        }
        if (waveRunning()) {
            statusLabel.setText("Wave already in progress.");
            return;
        }

        wave++;

        // Check if we've exceeded the wave limit
        if (wave > GameConfig.WAVES.size()) {
            winGame();
            return;
        }

        currentWaveIndex = wave - 1;
        currentWaveComposition = new ArrayList<>(GameConfig.WAVES.get(currentWaveIndex));
        currentCompositionIndex = 0;
        spawnedFromComposition = 0;
        spawnTimer = 0;
        updateLabels();
        statusLabel.setText("Wave " + wave + " has begun.");
    }

    /**
     * Checks if a wave is currently in progress. A wave is considered running if
     * there are
     * still bloons to spawn or bloons currently on the field. Simple but essential
     * logic.
     *
     * @return true if a wave is running
     */
    private boolean waveRunning() {
        return !currentWaveComposition.isEmpty() || !bloons.isEmpty();
    }

    /**
     * Updates the game state each frame. This is the heartbeat of the entire game -
     * it handles
     * bloon movement, tower firing, projectile updates, and wave progression. This
     * method is
     * absolutely essential and it's doing all the heavy lifting. Lowkey the unsung
     * hero, fr fr.
     *
     * @param elapsedSeconds the time elapsed since the last frame
     */
    private void updateGame(double elapsedSeconds) {
        if (gameOver || gameWon) {
            return;
        }

        // Apply game speed multiplier
        elapsedSeconds *= gameSpeed;

        // Auto-skip to next wave if enabled
        if (autoSkipEnabled && !waveRunning()) {
            spawnNextWave();
        }

        if (waveRunning()) {
            spawnTimer -= elapsedSeconds;
            if (!currentWaveComposition.isEmpty() && spawnTimer <= 0) {
                spawnBloon();
                spawnTimer = 0.8;
            }
        }

        Iterator<Bloon> bloonIterator = bloons.iterator();
        while (bloonIterator.hasNext()) {
            Bloon bloon = bloonIterator.next();
            if (!bloon.isAlive()) {
                bloonIterator.remove();
                playfield.getChildren().remove(bloon.getView());
                playPopSound();
                money += 20;
                updateLabels();
                continue;
            }
            bloon.update(elapsedSeconds);
            if (bloon.hasReachedEnd()) {
                bloonIterator.remove();
                playfield.getChildren().remove(bloon.getView());
                lives -= 1;
                updateLabels();
                if (lives <= 0) {
                    endGame();
                    return;
                }
            }
        }

        for (com.Towers.Tower tower : towers) {
            int projsBeforeUpdate = projectiles.size();
            tower.update(elapsedSeconds, bloons, projectiles);
            // Add any newly created projectiles to the playfield
            for (int i = projsBeforeUpdate; i < projectiles.size(); i++) {
                playfield.getChildren().add(projectiles.get(i).getView());
            }

            // Collect income from farms
            if (tower instanceof Farm) {
                Farm farm = (Farm) tower;
                int income = farm.collectIncome();
                money += income;
                totalFarmIncome += income;
            }
        }

        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            boolean remove = projectile.update(elapsedSeconds);
            if (remove) {
                projectileIterator.remove();
                playfield.getChildren().remove(projectile.getView());
            }
        }

        boolean running = waveRunning();
        if (!running && previousWaveRunning && !placingTower) {
            statusLabel.setText("Wave complete! Build towers and press Start Wave.");
        }
        previousWaveRunning = running;
    }

    /**
     * Spawns ballon based on the array list of balloons
     */
    private void spawnBloon() {
        if (currentWaveComposition.isEmpty()) {
            return;
        }

        double scale = Math.max(scaleX, scaleY);

        // Get current composition entry
        Object[] current = currentWaveComposition.get(currentCompositionIndex);
        int count = (int) current[0];
        BloonType type = (BloonType) current[1];

        // Spawn the bloon
        Bloon bloon = new Bloon(getScaledPath(), 70 + wave * 3, 1, scale, type);
        bloons.add(bloon);
        playfield.getChildren().add(bloon.getView());

        // Increment spawned count for this composition entry
        spawnedFromComposition++;

        // If we've spawned all bloons of this type, move to next composition entry
        if (spawnedFromComposition >= count) {
            currentCompositionIndex++;
            spawnedFromComposition = 0;

            // If we've finished all composition entries, clear the composition
            if (currentCompositionIndex >= currentWaveComposition.size()) {
                currentWaveComposition.clear();
            }
        }
    }

    /**
     * Updates all the UI labels to reflect the current game state. Money, lives,
     * wave number,
     * and farm income all get refreshed. Lowkey this method is essential for
     * keeping players
     * informed about what's happening - it's giving transparency energy, periodt.
     */
    private void updateLabels() {
        moneyLabel.setText("Money: " + money);
        livesLabel.setText("Lives: " + lives);
        waveLabel.setText("Wave: " + wave);
        farmIncomeLabel.setText("Farm Income: $" + totalFarmIncome);
    }

    /**
     * Toggles the game speed between normal (1x) and double speed (2x).
     * This is for the players who want to speed run the waves or just need a
     * faster-paced experience.
     * When enabled, it absolutely hits different and the tension goes up, fr fr.
     */
    private void toggleSpeed() {
        gameSpeed = (gameSpeed == 1.0) ? 2.0 : 1.0;
        speedButton.setText("2x Speed: " + (gameSpeed == 2.0 ? "ON" : "OFF"));
        if (gameSpeed == 2.0) {
            speedButton.getStyleClass().remove("speed-button");
            speedButton.getStyleClass().add("button-active");
        } else {
            speedButton.getStyleClass().remove("button-active");
            speedButton.getStyleClass().add("speed-button");
        }
    }

    /**
     * Toggles the auto-skip feature which automatically starts the next wave when
     * the current
     * one completes. For players who want a non-stop action experience, this is
     * absolutely bussin'.
     * Lowkey this feature is peak convenience, no cap.
     */
    private void toggleAutoSkip() {
        autoSkipEnabled = !autoSkipEnabled;
        autoSkipButton.setText("Auto Skip: " + (autoSkipEnabled ? "ON" : "OFF"));
        if (autoSkipEnabled) {
            autoSkipButton.getStyleClass().remove("autoskip-button");
            autoSkipButton.getStyleClass().add("button-active");
        } else {
            autoSkipButton.getStyleClass().remove("button-active");
            autoSkipButton.getStyleClass().add("autoskip-button");
        }
    }

    /**
     * End game when lives = 0 - gut punch but comeback is satisfying. That's the
     * vibe.
     */
    private void endGame() {
        gameOver = true;
        statusLabel.setText("Game Over! Refresh to play again.");
        stopGameplayMusic();
        buildGroundTowerButton.setDisable(true);
        buildCliffTowerButton.setDisable(true);
        buildMinigunnerButton.setDisable(true);
        buildFarmButton.setDisable(true);
        startWaveButton.setDisable(true);
        speedButton.setDisable(true);
        autoSkipButton.setDisable(true);
    }

    private void winGame() {
        gameWon = true;
        gameState = GameState.WON;
        stopGameplayMusic();
        showWinningScreen();
    }

    private void showWinningScreen() {
        VBox winContainer = new VBox();
        winContainer.getStyleClass().add("win-container");
        winContainer.setAlignment(Pos.CENTER);
        winContainer.setSpacing(30);

        Label winLabel = new Label("🎉 YOU WIN! 🎉");
        winLabel.getStyleClass().add("win-label");

        Label statsLabel = new Label("Final Stats:\n" +
                "Waves Completed: " + wave + " / 12\n" +
                "Final Money: $" + money + "\n" +
                "Lives Remaining: " + lives);
        statsLabel.getStyleClass().add("win-stats-label");
        statsLabel.setWrapText(true);

        Button playAgainButton = new Button("PLAY AGAIN");
        playAgainButton.getStyleClass().add("success-button");
        playAgainButton.setOnAction(event -> restartGame());

        Button exitButton = new Button("EXIT");
        exitButton.getStyleClass().add("danger-button");
        exitButton.setOnAction(event -> System.exit(0));

        VBox buttonsBox = new VBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.getChildren().addAll(playAgainButton, exitButton);

        winContainer.getChildren().addAll(winLabel, new Separator(), statsLabel, new Separator(), buttonsBox);
        winContainer.setPadding(new Insets(50));

        Scene winScene = new Scene(winContainer);
        winScene.getStylesheets().add(stylesheet);
        primaryStage.setScene(winScene);
    }

    private void restartGame() {
        // Reset all game state
        money = 250;
        lives = 20;
        wave = 0;
        totalFarmIncome = 0;
        gameOver = false;
        gameWon = false;
        placingTower = false;
        gameSpeed = 1.0;
        autoSkipEnabled = false;
        bloons.clear();
        towers.clear();
        projectiles.clear();
        currentWaveComposition.clear();
        currentCompositionIndex = 0;
        spawnedFromComposition = 0;

        // Show title screen to restart
        showTitleScreen();
    }
}
