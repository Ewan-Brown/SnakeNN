package com.nanovash.snakenn.view;

import com.nanovash.snakenn.game.Direction;
import com.nanovash.snakenn.game.GameModel;
import com.nanovash.snakenn.game.Location;
import com.nanovash.snakenn.game.util.LossException;
import com.nanovash.snakenn.game.util.State;
import com.nanovash.snakenn.neuralnetwork.NNGenetics;
import com.nanovash.snakenn.neuralnetwork.NeuralNetwork;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML GridPane board;
    @FXML Button button;
    @FXML Label scoreLabel;
    @FXML ChoiceBox players;
    @FXML ChoiceBox speeds;
    @FXML ChoiceBox walls;
    @FXML TextField input;
    @FXML Button loadInput;
    @FXML Label generation;
    @FXML Button openDir;

    GameModel model = new GameModel();
    int fps = 18;
    Timeline timeline;
    Timeline monitor = new Timeline();
    int lastScore = 0;
    int score = 0;
    boolean pause = false;
    String currentPlayer = "Human";
    NNGenetics trainer = new NNGenetics();
    NeuralNetwork loaded = new NeuralNetwork();

    /**
     * Loads the choice boxes, the board and adds a generation update listener
     */
    public void initialize(URL location, ResourceBundle resources) {
        initChoiceBoxes();
        for (int i = 0; i < GameModel.SIDE; i++) {
            for (int j = 0; j < GameModel.SIDE; j++) {
                Rectangle rectangle = new Rectangle(30, 30);
                rectangle.setFill(Color.WHITE);
                rectangle.setStroke(Color.LIGHTGREY);
                board.add(rectangle, i, j);
            }
        }
        loadStart();
        trainer.getListeners().add(population -> {
            generation.setText("Generation: " + population);
        });
    }

    /**
     * Calls GameModel.start() and draws the returned HashMap respectively
     */
    public void loadStart() {
        score = 0;
        clear();
        HashMap<Location, State> start = model.start();
        for (Location add : start.keySet())
            addToBoard(add, start.get(add));
    }

    /**
     * Calls GameModel.update() and draws the returned HashMap respectively, and in case the player
     * is a neural network, it checks its evaluation for each move and uses the move that got the best evaluation
     */
    public void loadUpdate() {
        HashMap<Location, State> updated = model.update();
        if(updated == null) {
            lastScore = 0;
            button.setText("New Game");
            button.setPrefWidth(100);
            timeline.stop();
            trainer.updateFitnessOfCurrent(score);
            if(currentPlayer.equals("NN Training")) {
                monitor.stop();
                button.fire();
            }
            return;
        }
        clear();
        score = 0;
        for (Location l : updated.keySet())
            addToBoard(l, updated.get(l));

        if(currentPlayer.equals("Human"))
            return;
        //Evaluate each possible move and pick the best according to the neural network
        Direction[] directions = new Direction[] {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        Direction bestDirection = Direction.UP;
        NeuralNetwork decider = currentPlayer.equals("NN Training") ? trainer.getCurrent() : loaded;
        double bestEval = -10;
        for (Direction direction : directions) {
            if(direction.isOpposite(model.getDirection()))
                continue;
            Location present = model.getSnake().getLocation();
            double front = -2;
            Location inFront = null;
            try {
                inFront = new Location(present.getX() + direction.getX(), present.getY() + direction.getY(), true);
            } catch (LossException e) {
                front = -1;
            }
            if(front == -2)
                front = stateToDouble(updated.get(inFront));
            Direction relativeLeft = Direction.getRelativeLeft(direction);
            double left = -2;
            Location toLeft = null;
            try {
                toLeft = new Location(present.getX() + relativeLeft.getX(), present.getY() + relativeLeft.getY(), true);
            } catch (LossException e) {
                left = -1;
            }
            if(left == -2)
                left = stateToDouble(updated.get(toLeft));
            if(direction.equals(Direction.getRelativeLeft(model.getDirection())) && !model.getSnake().getTail().isEmpty())
                left = 0;
            Direction relativeRight = Direction.getRelativeRight(direction);
            double right = -2;
            Location toRight = null;
            try {
                toRight = new Location(present.getX() + relativeRight.getX(), present.getY() + relativeRight.getY(), true);
            } catch (LossException e) {
                right = -1;
            }
            if(right == -2)
                right = stateToDouble(updated.get(toRight));
            if(direction.equals(Direction.getRelativeRight(model.getDirection())) && !model.getSnake().getTail().isEmpty())
                right = 0;
            double delta = Math.sqrt(Math.pow(present.getX() - model.getFood().getX(), 2) + Math.pow(present.getY() - model.getFood().getY(), 2)) - Math.sqrt(Math.pow(present.getX() + direction.getX() - model.getFood().getX(), 2) + Math.pow(present.getY() + direction.getY() - model.getFood().getY(), 2));
            double eval = decider.calcOutput(new double[] {delta, left, front, right});
            if(eval > bestEval) {
                bestEval = eval;
                bestDirection = direction;
            }
            decider.reset();
        }
        model.setPendingDirection(bestDirection);
    }

    /**
     * Converts a State to a double
     * @param state The state to convert
     * @return 1, 0, or -1 respectively
     */
    private double stateToDouble(State state) {
        if(state == null)
            return 0;
        switch (state) {
            case FOOD:
                return 1;
            case TAIL:
                return -1;
        }
        return 0;
    }

    /**
     * Finds the rectangle in the correct location, and then draws in its position according to the State
     * @param location The Rectangle's location
     * @param state The State to draw according to
     */
    public void addToBoard(Location location, State state) {
        for (Node node : board.getChildren())
            if (GridPane.getColumnIndex(node) == location.getX() && GridPane.getRowIndex(node) == location.getY()) {
                Rectangle rectangle = ((Rectangle) node);
                switch(state) {
                    case HEAD:
                        rectangle.setFill(Color.DARKGREEN);
                        break;
                    case TAIL:
                        rectangle.setFill(Color.GREEN);
                        score++;
                        break;
                    case FOOD:
                        rectangle.setFill(Color.BLACK);
                        break;
                }
                scoreLabel.setText("Score: " + score);
            }
    }

    /**
     * Clears the board
     */
    public void clear() {
        for (Node node : board.getChildren())
            ((Rectangle) node).setFill(Color.WHITE);
    }

    /**
     * Handler for the Start button click
     */
    @FXML
    public void buttonPressed() {
        if(button.getText().equals("Start")) {
            button.getScene().setOnKeyPressed(event1 -> {
                if(!currentPlayer.equals("Human"))
                    return;
                KeyCode code = event1.getCode();
                if ((code.equals(KeyCode.UP) || code.equals(KeyCode.W)) && !pause)
                    model.setPendingDirection(Direction.UP);
                else if ((code.equals(KeyCode.DOWN) || code.equals(KeyCode.S)) && !pause)
                    model.setPendingDirection(Direction.DOWN);
                else if ((code.equals(KeyCode.LEFT) || code.equals(KeyCode.A)) && !pause)
                    model.setPendingDirection(Direction.LEFT);
                else if ((code.equals(KeyCode.RIGHT) || code.equals(KeyCode.D)) && !pause)
                    model.setPendingDirection(Direction.RIGHT);
            });
            button.getScene().setOnMouseClicked(event -> {
                board.requestFocus();
            });
            timeline.play();
            if(currentPlayer.equals("NN Training"))
                monitor.play();
            button.setText("Pause");
            button.setPrefWidth(70);
        }
        else if(button.getText().equals("Pause")) {
            timeline.pause();
            if(currentPlayer.equals("NN Training"))
                monitor.pause();
            pause = true;
            button.setText("Resume");
            button.setPrefWidth(80);
        }
        else if(button.getText().equals("Resume")) {
            timeline.play();
            if(currentPlayer.equals("NN Training"))
                monitor.play();
            pause = false;
            button.setText("Pause");
            button.setPrefWidth(70);
        }
        else if(button.getText().equals("New Game")) {
            loadStart();
            timeline.play();
            if(currentPlayer.equals("NN Training"))
                monitor.play();
            button.setText("Pause");
            button.setPrefWidth(70);
        }
    }

    /**
     * Loads a neural network for the "Loaded Network" player type
     */
    @FXML
    public void loadNetwork() {
        NeuralNetwork network;
        try {
            network = new NeuralNetwork(trainer.stringToList(input.getText()));
        } catch(IndexOutOfBoundsException | NumberFormatException e1) {
            return;
        }
        loaded = network;
        players.getSelectionModel().select(2);
    }

    /**
     * Open the program's Appdata directory
     */
    @FXML
    public void openDir() {
        try {
            Desktop.getDesktop().open(trainer.getStorePopulation().getParentFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the choice boxes because I couldn't figure out how to do it through Scene Builder lol
     */
    public void initChoiceBoxes() {
        speeds.getItems().addAll("Normal", "Fast", "NN Training");
        speeds.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.toString()) {
                case "Normal":
                    fps = 15;
                    break;
                case "Fast":
                    fps = 30;
                    break;
                case "NN Training":
                    fps = 60;
            }
            if(timeline != null)
                timeline.stop();
            timeline = new Timeline(new KeyFrame(Duration.millis(1000 / fps), ae -> loadUpdate()));
            timeline.setCycleCount(Animation.INDEFINITE);
            if(button.getText().equals("Pause"))
                timeline.play();
            initMonitor();
        });
        speeds.getSelectionModel().select(0);
        walls.setValue("Disabled");
        walls.getItems().addAll("Disabled", "Enabled");
        walls.valueProperty().addListener((observable, oldValue, newValue) -> {model.setWalls(newValue.toString().equals("Enabled"));});
        players.getItems().addAll("Human", "NN Training", "Loaded NN");
        players.valueProperty().addListener((observable, oldValue, newValue) -> {
            currentPlayer = newValue.toString();
            initMonitor();
        });
        players.getSelectionModel().select(0);
    }

    /**
     * Initializes the monitor to stop a network in case it gets stuck in an infinite loop
     */
    public void initMonitor() {
        monitor.stop();
        monitor = new Timeline(new KeyFrame(Duration.millis(100000 / fps), ae -> {
            if(lastScore >= score) {
                trainer.updateFitnessOfCurrent(score);
                loadStart();
            }
            lastScore = score;
        }));
        monitor.setCycleCount(Animation.INDEFINITE);
        if(button.getText().equals("Pause") && currentPlayer.equals("NN Training"))
            monitor.play();
    }
}