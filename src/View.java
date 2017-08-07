

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class View {
	
	private int numOfCars;
	private Model model;
	private BorderPane border_pane;
	private GridPane details_grid, cars_grid;
	private CarPane carsPane[];
//	private Label lbl[];
	private Slider slRadius;
	private TextField spd_txt[];
	private ComboBox<String> colorComBox, carIdComBox;
	private ObservableList<String> items_color, items_car;

//	private Button btn;

	public View(int numOfCars) {
		
		this.numOfCars = numOfCars;
		carsPane = new CarPane[numOfCars];
//		lbl = new Label[numOfCars];
		spd_txt = new TextField[numOfCars];
	
		border_pane = new BorderPane();
		createDetailsGrid();
		border_pane.setTop(details_grid);
		createCarsGrid();
		border_pane.setCenter(cars_grid);
	}

	public void setModel(Model myModel) {
		model = myModel;
		if (model != null) {
			for (int i=0; i<numOfCars; i++){
			carsPane[i].setCarModel(model.getCarById(i));
			}	
		}
	}
	public Model getModel(Model myModel) {
		return model;
	}

//	public void createColorsComBox() {
//		String colorNames[] = { "RED", "AQUA", "BLUE", "GREEN", "YELLOW", "ORANGE", "PINK", "VIOLET", "WHITE",
//				"TRANSPARENT" };
//		items_color = FXCollections.observableArrayList(colorNames);
//		colorComBox = new ComboBox<>();
//		colorComBox.getItems().addAll(items_color);
//		colorComBox.setMinWidth(200);
//		colorComBox.setValue("RED");
//	}
//
//	public void createCarIdComBox() {
//		items_car = FXCollections.observableArrayList();
//		for (int i = 0; i < numOfCars; i++)
//			items_car.add("car #" + (i+1));
//		carIdComBox = new ComboBox<>();
//		carIdComBox.getItems().addAll(items_car);
//		carIdComBox.setMinWidth(120);
//		carIdComBox.setValue("car #1");
//	}
//
//	public void createSlider() {
//		slRadius = new Slider(5, 20, 0);
//		slRadius.setShowTickLabels(true);
//		slRadius.setShowTickMarks(true);
//		slRadius.setMajorTickUnit(5);
//		slRadius.setBlockIncrement(1);
//	}

	public void createCarsGrid() {
		cars_grid = new GridPane();

		for (int i=0; i<numOfCars; i++){
		carsPane[i] = new CarPane();
		cars_grid.add(carsPane[i], 0, i);
		}

		cars_grid.setStyle("-fx-background-color: beige");
		cars_grid.setGridLinesVisible(true);
		ColumnConstraints column = new ColumnConstraints();
		column.setPercentWidth(100);
		cars_grid.getColumnConstraints().add(column);
		RowConstraints row = new RowConstraints();
		row.setPercentHeight(33);
		
		for (int i=0; i<numOfCars; i++)
			cars_grid.getRowConstraints().add(row);
	

	}

	public void createAllTimelines(int time) {
	
		for (int i=0; i<numOfCars; i++){
			carsPane[i].createTimeline(time);
			
		}
	}
	
	public void stopAllTimelines(){
		
		for (int i=0; i<numOfCars; i++){
			carsPane[i].getTimeline().stop();
		}
	}

	public void createDetailsGrid() {
		
//		Pane pane = new Pane();
//		details_grid = new GridPane();
//		btn = new Button("Generate Cars");
//		btn.setMinWidth(200);
//		
//		for (int i=0; i<numOfCars; i++){
//			lbl[i] = new Label("car #"+(i+1)+": ");
//			spd_txt[i] = new TextField();
//		}
//		createColorsComBox();
//		createCarIdComBox();
//		createSlider();
//		pane.add(colorComBox, 0, 0);
//		pane.add(carIdComBox, 1, 0);
//		pane.add(btn);
//		
//		for (int i=0; i<numOfCars; i++){
//			details_grid.add(lbl[i], i, 0);
//			details_grid.add(spd_txt[i], i, 1);
//		}
//	
//		details_grid.add(pane, numOfCars, 0);
//		details_grid.add(slRadius, numOfCars, 1);
//	
//		pane.getChildren().add(btn);
//		details_grid.add(pane, 0, 0);
		
	}

	public BorderPane getBorderPane() {
		return border_pane;
	}

	public GridPane getDetailsGrid() {
		return details_grid;
	}

	public GridPane getCarsGrid() {
		return cars_grid;
	}

	public void setCarPanesMaxWidth(double newWidth) {
	
		for (int i=0; i<numOfCars; i++)
			carsPane[i].setMaxWidth(newWidth);
	
	}

	public CarPane getCarPaneById(int id) {
		return carsPane[id];
	}

	public TextField getSpeedTxtById(int id) {
		return spd_txt[id];
	}

	public ObservableList<String> getItemsCar() {
		return items_car;
	}

	public ObservableList<String> getItemsColor() {
		return items_color;
	}

	public ComboBox<String> getColorComBox() {
		return colorComBox;
	}

	public ComboBox<String> getCarIdComBox() {
		return carIdComBox;
	}

//	public Button getColorButton() {
//		return btn;
//	}

	public Slider getRadSlider() {
		return slRadius;
	}
}
