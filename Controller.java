package com.internshala.connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
	private static final int COLUMNS = 7;
	private static final int ROWS = 6;
	private static final int CircleDiameter = 80;

	private static final String discColor1 = "#24303E";
	private static final String discColor2 = "#4CAA88";

	private static final String Player_One = "Player One";
	private static final String Player_Two = "Player Two";

	private boolean isPlayerOneTurn = true;
	private Disc[][] insertedDiscArray = new Disc[ROWS][COLUMNS];


	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscPane;
	@FXML
	public Label playerOneLabel;
	@FXML
	public TextField playerOneTextField,playerTwoTextFields;
	@FXML
	public Button setNamesButton;

	private boolean isAllowedToInsert  = true;
	public void createPlayGround() {

		Shape rectangleWithHoles = createGameStructureGrid();
		rootGridPane.add(rectangleWithHoles, 0, 1);
		List<Rectangle> rectangleList = createClickableColumn();

		for (Rectangle rectangle : rectangleList) {


			rootGridPane.add(rectangle, 0, 1);
		}
		setNamesButton.setOnAction(event -> {
			playerOneTextField.setDisable(true);
			playerTwoTextFields.setDisable(true);
		});
	}

	private Shape createGameStructureGrid() {
		Shape rectangleWithHoles = new Rectangle((COLUMNS + 1) * CircleDiameter, (ROWS + 1) * CircleDiameter);
		for (int row = 0; row < ROWS; row++) {

			for (int col = 0; col < COLUMNS; col++) {

				Circle circle = new Circle();
				circle.setRadius(CircleDiameter / 2.0);
				circle.setCenterX(CircleDiameter / 2.0);
				circle.setCenterY(CircleDiameter / 2.0);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CircleDiameter + 5) + (CircleDiameter / 4.0));
				circle.setTranslateY(row * (CircleDiameter + 5) + CircleDiameter / 4.0);

				rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
			}
		}

		rectangleWithHoles.setFill(Color.WHITE);
		return rectangleWithHoles;
	}

	private List<Rectangle> createClickableColumn() {
		List<Rectangle> rectangleList = new ArrayList<>();

		for (int col = 0; col < COLUMNS; col++) {


			Rectangle rectangle = new Rectangle(CircleDiameter, (ROWS + 1) * CircleDiameter);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CircleDiameter + 5) + CircleDiameter / 4.0);
			rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));


			final int column = col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInsert){
					isAllowedToInsert = false;
				insertDisc(new Disc(isPlayerOneTurn), column);

				}
			});


			rectangleList.add(rectangle);
		}

		return rectangleList;
	}

	private void insertDisc(Disc disc, int column) {

		int row = ROWS - 1;

		while (row >= 0) {
			if (insertedDiscArray[row][column] == null)
				break;
			row--;

		}
		if (row < 0)
			return;

		insertedDiscArray[row][column] = disc;
		insertedDiscPane.getChildren().add(disc);

		int currentRow = row;
		disc.setTranslateX(column * (CircleDiameter + 5) + (CircleDiameter / 4.0));
		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), disc);
		translateTransition.setToY(row * (CircleDiameter + 5) + CircleDiameter / 1.5);
		translateTransition.setOnFinished(event -> {

            isAllowedToInsert = true;
			if (gameEnded(currentRow, column)) {
				gameOver();
				
				return;
			}

			String input1 = playerOneTextField.getText();
			String input2 = playerTwoTextFields.getText();
			isPlayerOneTurn = !isPlayerOneTurn;
			playerOneLabel.setText((isPlayerOneTurn ? input1 : input2));
		});

		translateTransition.play();
	}

	private void gameOver() {
		String winner = isPlayerOneTurn? Player_One:Player_Two;
		System.out.println("Winner is :" +winner );

		Alert alert = new Alert( Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner Is: "+ winner);
		alert.setContentText("Want to Play again ?");
		ButtonType  yesBut = new ButtonType("Yes");
		ButtonType noBut = new ButtonType("No,Exit");
		alert.getButtonTypes().setAll(yesBut,noBut);

		Platform.runLater(() ->{
			Optional <ButtonType> btnclicked = alert.showAndWait();
			if(btnclicked.isPresent() && btnclicked.get() ==yesBut){
				resetGame();
			}else {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {
		insertedDiscPane.getChildren().clear();
		for (int row = 0; row< insertedDiscArray.length; row++){
			for( int col =0 ;col<insertedDiscArray[row].length; col++){
				insertedDiscArray[row][col] = null;
			}
		}

		isPlayerOneTurn=true;
		playerOneLabel.setText(Player_One);

		createPlayGround();
	}

	private boolean gameEnded(int row, int column) {

		List<Point2D> verticalPoint = IntStream.rangeClosed(row - 3, row + 3)
				.mapToObj(r -> new Point2D(r, column))
				.collect(Collectors.toList());


		List<Point2D> horizontalPoint = IntStream.rangeClosed(column - 3, column + 3)
				.mapToObj(col -> new Point2D(row, col))
				.collect(Collectors.toList());
		Point2D startpoint1 = new Point2D(row-3,column+3);
		List <Point2D> diagonalPoints = IntStream.rangeClosed(0,6)
				.mapToObj(i-> startpoint1.add(i,-i))
				.collect(Collectors.toList());


		Point2D startpoint2 = new Point2D(row-3,column-3);
		List <Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i-> startpoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoint)|| checkCombinations(horizontalPoint)
				|| checkCombinations(diagonalPoints)|| checkCombinations(diagonal2Points);

		return isEnded;

	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain =0;

		for (Point2D point : points) {

			int rowIndexForArray = (int) point.getX();
			int columnIndexForArray = (int) point.getY();
			Disc disc = getDiscIfPresent(rowIndexForArray,columnIndexForArray);

			if(disc!=null && disc.isPlayerOneMove== isPlayerOneTurn){
				chain++;
				if(chain == 4){
					return true;
				}
			} else {
				chain=0;
			}
		}
		return false;
	}

private Disc getDiscIfPresent(int row, int column){
		if(row>= ROWS|| row<0 ||column>=COLUMNS || column<0) {
			return null;
		}
			return insertedDiscArray[row][column];
	}
	private static class Disc extends Circle{

	private final boolean isPlayerOneMove;

	public Disc (boolean isPlayerOneMove){
		this.isPlayerOneMove  = isPlayerOneMove;
		setRadius(CircleDiameter/2.0);
		setFill(isPlayerOneMove? Color.valueOf(discColor1):Color.valueOf(discColor2));
		setCenterX(CircleDiameter/2.0);
		setCenterY(CircleDiameter/2.0);
	}
}
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}