

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * A gamblers client class
 * @author Eviatar Admon
 *
 */

public class Gambler {

	private final static String[] EXISITING_GAMBLER_LABEL_INPUT_NAMES = { "User Name: ", "Password: " };
	private final static String[] NEW_GAMBLER_LABEL_INPUT_NAMES = { "User Name: ", "Password: ", "Confirm Password: " };
	private final static String[] GAMBLER_LABEL_INPUT_NAMES = { "Race ID: ", "Car Number: ", "Amount of Money: " };
	private final static String[] CARS = { "1", "2", "3", "4", "5" };
	private final static String[] CARS_DETAILS = {"Car ID:", "Car Shape:", "Car Type:", "Car Color:", "Car Size:"};
	private int wonMoney = 0;
	private int gamblerId;
	private ObjectOutputStream toServer;
	private ObjectInputStream toClient;
	private TextField tf;
	private Label wonlbl = new Label(wonMoney + "");
	private Label warninglbl = new Label();
	private Scene scene;
	private ComboBox<String> carCmb;
	private ObservableList<String> carOb;
	private ComboBox<String> raceCmb;
	private ObservableList<String> raceOb;
	private Stage gmblrStage;
	private TableView<String[]> carTableView = new TableView<String[]>();

	/**
	 * Gamblers Constructor
	 * @param toServer The Server Stream
	 * @param toClient The Client Stream
	 */
	
	public Gambler(ObjectOutputStream toServer, ObjectInputStream toClient) {

		// this.gamblerId = gamblerId;
		this.toServer = toServer;
		this.toClient = toClient;

		gmblrStage = new Stage();

		Button createNewUser = new Button("Create New User");
		Button exisitingUser = new Button("Exisiting User");

		HBox hb = new HBox(10);

		hb.getChildren().addAll(createNewUser, exisitingUser);

		createNewUser.setOnAction(e -> {

			createNewGamblerWindow();

		});

		exisitingUser.setOnAction(e -> {

			exisitingGamblerWindow();

		});

		scene = new Scene(hb, 250, 35);
		gmblrStage.setScene(scene);
		gmblrStage.setTitle("Registeration Phase");

	}
	
	/*
	 * Opens an existing gambler user
	 */

	private void exisitingGamblerWindow() {

		TextField userName = new TextField();
		PasswordField password = new PasswordField();
		Button join = new Button("Join");

		Label[] lbl = new Label[EXISITING_GAMBLER_LABEL_INPUT_NAMES.length];

		GridPane gp = new GridPane();

		for (int i = 0; i < EXISITING_GAMBLER_LABEL_INPUT_NAMES.length; i++) {

			lbl[i] = new Label(EXISITING_GAMBLER_LABEL_INPUT_NAMES[i]);
			gp.add(lbl[i], 0, i);

		}

		gp.add(userName, 1, 0);
		gp.add(password, 1, 1);

		gp.add(join, 0, EXISITING_GAMBLER_LABEL_INPUT_NAMES.length);
		gp.add(warninglbl, 1, EXISITING_GAMBLER_LABEL_INPUT_NAMES.length);

		join.setOnAction(e -> {

			if (!(userName.getText().isEmpty() || password.getText().isEmpty())) {

				ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.EXISITING_GAMBLER_CLIENT);
				sp.setUserName(userName.getText());
				sp.setPassword(password.getText());

				try {
					toServer.writeObject(sp);
					sp = (ServerProtocol)toClient.readObject();
					if (sp.isPermissionApproved()) {
						gamblerId = sp.getGamblerId();
						warninglbl.setText("");
						gameGamblerWindow();
					} else
						warninglbl.setText("Wrong information, please try again..");
				} catch (IOException e1) {
				
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
			
					e1.printStackTrace();
				}

			} else
				warninglbl.setText("Please fill information");

		});

		scene = new Scene(gp, 375, 100);
		gmblrStage.setScene(scene);
		gmblrStage.setTitle("Exisitin User");

	}
	
	/**
	 * Creates a new gambler user
	 */

	private void createNewGamblerWindow() {

		TextField userName = new TextField();
		PasswordField password = new PasswordField();
		PasswordField confirmPassword = new PasswordField();
		Button create = new Button("Create");

		Label[] lbl = new Label[NEW_GAMBLER_LABEL_INPUT_NAMES.length];

		GridPane gp = new GridPane();

		for (int i = 0; i < NEW_GAMBLER_LABEL_INPUT_NAMES.length; i++) {

			lbl[i] = new Label(NEW_GAMBLER_LABEL_INPUT_NAMES[i]);
			gp.add(lbl[i], 0, i);

		}

		gp.add(userName, 1, 0);
		gp.add(password, 1, 1);
		gp.add(confirmPassword, 1, 2);

		gp.add(create, 0, NEW_GAMBLER_LABEL_INPUT_NAMES.length);
		gp.add(warninglbl, 1, NEW_GAMBLER_LABEL_INPUT_NAMES.length);

		create.setOnAction(e -> {

			if (!(userName.getText().isEmpty() || password.getText().isEmpty()
					|| confirmPassword.getText().isEmpty())) {

				if (password.getText().equals(confirmPassword.getText())) {

					warninglbl.setText("");
					ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.NEW_GAMBLER_CLIENT);
					sp.setUserName(userName.getText());
					sp.setPassword(password.getText());

					try {
						toServer.writeObject(sp);
						gamblerId = (Integer) toClient.readObject();
						gameGamblerWindow();
					} catch (IOException e1) {
						
						e1.printStackTrace();
					} catch (ClassNotFoundException e1) {
					
						e1.printStackTrace();
					}

				} else
					warninglbl.setText("Passwords does not match");

			} else
				warninglbl.setText("Please fill information");

		});

		scene = new Scene(gp, 375, 130);
		gmblrStage.setScene(scene);
		gmblrStage.setTitle("New User");

	}
	
	/**
	 * Creates a server listener for streaming data from server
	 */
	
	private void createServerListener() {

		new Thread(() -> {

			try {

				boolean flag = true;
				while (flag) {
					ServerProtocol sp = (ServerProtocol) this.toClient.readObject();

					if (sp.getProtocol() == ServerProtocol.serverProtocol.GAMBLER_UPDATE_RACE_LIST) {
						Platform.runLater(() -> {
							raceOb.setAll(sp.getRaceList());
						});
					} else if (sp.getProtocol() == ServerProtocol.serverProtocol.GAMBLER_PRIZE) {
						wonMoney += sp.getVirtualMoney();
						Platform.runLater(() -> {
							wonlbl.setText(wonMoney + "");
						});
						
					} else if (sp.getProtocol() == ServerProtocol.serverProtocol.GET_CAR_RACE_DETAILS){
						updateCarTableView(sp);
					}
					else if (sp.getProtocol() == ServerProtocol.serverProtocol.CLOSE_CLIENT) {
						flag = false;
						toServer.close();
						toClient.close();
					}

				}
			} catch (ClassNotFoundException e1) {

				e1.printStackTrace();
			} catch (IOException e1) {

				e1.printStackTrace();
			}

		}).start();

	}
	
	/**
	 * updates cars table view
	 * @param sp Holds the Server Protocl for updating car table view 
	 */
	
	private void updateCarTableView(ServerProtocol sp) {
		
		GeneratedCar[] cars;
		String[] carIds = new String[CARS.length];
		cars = sp.getCar();
		
		carTableView.getItems().clear();
		//carCmb.getItems().clear();
		
		
		for (int i=0; i<CARS.length; i++){
			
			String[] cells = new String[CARS_DETAILS.length];
			
			carIds[i] = (sp.getRaceNum()*10)+(i+1)+"";
			cells[0] = (sp.getRaceNum()*10)+(i+1)+"";
			cells[1] = cars[i].getCarShape();
			cells[2] = cars[i].getCarType();
			cells[3] = cars[i].getCarColor();
			cells[4] = cars[i].getCarSize();
			
			carTableView.getItems().add(cells);
			
		}
		
		carOb = FXCollections.observableArrayList(carIds);
		Platform.runLater(() ->{
			carCmb.setItems(carOb);
		});
			
	}
	
	/**
	 * Opens the final gamblers window to bet on games(races)
	 */

	private void gameGamblerWindow() {

		Button sendBet = new Button("Submit Bet");
		Label[] lbl = new Label[GAMBLER_LABEL_INPUT_NAMES.length];
		GridPane gp = new GridPane();
		HBox hb = new HBox(10);
		
		for (int i = 0; i < CARS_DETAILS.length; i++) {
			int columNum = i;
			TableColumn<String[], String> column = new TableColumn<>(CARS_DETAILS[columNum]);
			column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columNum]));
			carTableView.getColumns().add(column);
		}

		tf = new TextField();

		carCmb = new ComboBox<>();
		//carOb = FXCollections.observableArrayList(CARS);

		raceCmb = new ComboBox<>();
		raceOb = FXCollections.observableArrayList();
		raceCmb.setItems(raceOb);

		raceCmb.valueProperty().addListener(new ChangeListener<String>() {
	        @Override public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
	        	
	        	try {
	        		
	        		if (t1 != null){
	        		
	        		ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.GET_CAR_RACE_DETAILS);
	        		sp.setRaceNum((Integer)Integer.parseInt(t1));
	        	
					toServer.writeObject(sp);
	        		}				
					
				} catch (IOException e) {
			
					e.printStackTrace();
				} 
	        	
	        	
	        }    
	    });
		sendBet.setOnAction(e -> {

			try {

				String rId = raceCmb.getValue() + "";
				String cId = carCmb.getValue() + "";
				cId = cId.substring(cId.length()-1);
				String bAmount = tf.getText().trim();

				if (!bAmount.equals("") && !rId.equals("null")) {

					warninglbl.setText("");

					int raceId = Integer.parseInt(rId);
					// carId begins from 0
					int carId = Integer.parseInt(cId) - 1;
					int betAmount = Integer.parseInt(bAmount);

					ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.GAMBLER_BET);
					sp.setGamblerBet(new GamblerBet(raceId, carId, betAmount, this.gamblerId));
					this.toServer.writeObject(sp);
				} else if (bAmount.equals(""))
					warninglbl.setText("Please put amount of money");
				else
					warninglbl.setText("Please insert race");

			} catch (NumberFormatException e1) {

				warninglbl.setText("Wrong amount of money");

			} catch (IOException e1) {

			}

		});

		for (int i = 0; i < GAMBLER_LABEL_INPUT_NAMES.length; i++) {

			lbl[i] = new Label(GAMBLER_LABEL_INPUT_NAMES[i]);
			gp.add(lbl[i], 0, i);

		}

		gp.add(raceCmb, 1, 0);
		gp.add(carCmb, 1, 1);
		gp.add(tf, 1, 2);

		gp.add(new Label("Amount Won: "), 0, GAMBLER_LABEL_INPUT_NAMES.length);
		gp.add(wonlbl, 1, GAMBLER_LABEL_INPUT_NAMES.length);

		gp.add(sendBet, 0, GAMBLER_LABEL_INPUT_NAMES.length + 1);
		gp.add(warninglbl, 1, GAMBLER_LABEL_INPUT_NAMES.length + 1);
		
		
		hb.getChildren().addAll(gp, carTableView);

		scene = new Scene(hb, 730, 180);
		gmblrStage.setTitle("Gambler ID: " + gamblerId);
		gmblrStage.setScene(scene);

		createServerListener();

	}

	public Stage getStage() {
		return gmblrStage;
	}

}
