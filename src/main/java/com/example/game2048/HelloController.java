package com.example.game2048;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Random;

public class HelloController {

    @FXML private GridPane gridPane;
    @FXML private Label scoreLabel;
    @FXML private Button restartButton;

    private int[][] board = new int[4][4];
    private int score = 0;
    private final Random random = new Random();

    private double startX, startY; // Для свайпов

    @FXML
    public void initialize() {
        restartButton.setOnAction(e -> restartGame());
        startGame();
    }

    private void startGame() {
        score = 0;
        board = new int[4][4];
        addRandomTile();
        addRandomTile();
        drawBoard();

        // Устанавливаем слушатели клавиш и мыши
        Scene scene = gridPane.getScene();
        if (scene != null) setupControls(scene);
        else {
            gridPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) setupControls(newScene);
            });
        }
    }

    private void setupControls(Scene scene) {
        // Клавиатура
        scene.setOnKeyPressed(this::handleKey);

        // Свайпы мышью
        scene.setOnMousePressed(this::onMousePressed);
        scene.setOnMouseReleased(this::onMouseReleased);
    }

    private void restartGame() {
        startGame();
    }

    // ===================== Управление =====================

    private void handleKey(KeyEvent event) {
        boolean moved = false;
        switch (event.getCode()) {
            case UP, W -> moved = moveUp();
            case DOWN, S -> moved = moveDown();
            case LEFT, A -> moved = moveLeft();
            case RIGHT, D -> moved = moveRight();
        }

        if (moved) {
            addRandomTile();
            drawBoard();
            checkGameOver();
        }
    }

    private void onMousePressed(MouseEvent e) {
        startX = e.getSceneX();
        startY = e.getSceneY();
    }

    private void onMouseReleased(MouseEvent e) {
        double dx = e.getSceneX() - startX;
        double dy = e.getSceneY() - startY;

        // Минимальный порог для движения
        double threshold = 40;

        boolean moved = false;
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > threshold) moved = moveRight();
            else if (dx < -threshold) moved = moveLeft();
        } else {
            if (dy > threshold) moved = moveDown();
            else if (dy < -threshold) moved = moveUp();
        }

        if (moved) {
            addRandomTile();
            drawBoard();
            checkGameOver();
        }
    }

    // ===================== Отрисовка =====================

    private void drawBoard() {
        gridPane.getChildren().clear();
        scoreLabel.setText(String.valueOf(score));

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int val = board[i][j];
                StackPane cell = new StackPane();
                cell.getStyleClass().add("tile");
                cell.getStyleClass().add("tile-" + val);

                Text text = new Text(val == 0 ? "" : String.valueOf(val));
                text.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
                cell.getChildren().add(text);

                gridPane.add(cell, j, i);
            }
        }
    }

    // ===================== Игровая логика =====================

    private void addRandomTile() {
        int emptyCount = 0;
        for (int[] row : board)
            for (int v : row)
                if (v == 0) emptyCount++;

        if (emptyCount == 0) return;

        int pos = random.nextInt(emptyCount);
        int value = random.nextDouble() < 0.9 ? 2 : 4;
        int count = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) {
                    if (count == pos) {
                        board[i][j] = value;
                        return;
                    }
                    count++;
                }
            }
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < 4; i++) {
            int[] newRow = new int[4];
            int pos = 0;
            boolean merged = false;
            for (int j = 0; j < 4; j++) {
                if (board[i][j] != 0) {
                    if (pos > 0 && newRow[pos - 1] == board[i][j] && !merged) {
                        newRow[pos - 1] *= 2;
                        score += newRow[pos - 1];
                        merged = true;
                        moved = true;
                    } else {
                        newRow[pos++] = board[i][j];
                        merged = false;
                        if (j != pos - 1) moved = true;
                    }
                }
            }
            board[i] = newRow;
        }
        return moved;
    }

    private boolean moveRight() {
        reverseRows();
        boolean moved = moveLeft();
        reverseRows();
        return moved;
    }

    private boolean moveUp() {
        transpose();
        boolean moved = moveLeft();
        transpose();
        return moved;
    }

    private boolean moveDown() {
        transpose();
        boolean moved = moveRight();
        transpose();
        return moved;
    }

    private void reverseRows() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                int tmp = board[i][j];
                board[i][j] = board[i][3 - j];
                board[i][3 - j] = tmp;
            }
        }
    }

    private void transpose() {
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                int tmp = board[i][j];
                board[i][j] = board[j][i];
                board[j][i] = tmp;
            }
        }
    }

    // ===================== Проверка конца игры =====================

    private void checkGameOver() {
        if (has2048()) {
            showAlert("🎉 Победа!", "Вы собрали 2048!");
        } else if (!canMove()) {
            showAlert("💀 Игра окончена!", "Ходов больше нет!");
        }
    }

    private boolean has2048() {
        for (int[] row : board)
            for (int v : row)
                if (v == 2048) return true;
        return false;
    }

    private boolean canMove() {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) return true;
                if (i < 3 && board[i][j] == board[i + 1][j]) return true;
                if (j < 3 && board[i][j] == board[i][j + 1]) return true;
            }
        return false;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
