
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 
 * Server side which manipulates the clients model and uses SQL, Writing & Reading data to file, Threads, HashMaps and more. As well
 * it handles 3 kinds of clients: Races, Gamblers and the Boss client (as if: Administrator).
 * @author Eviatar Admon
 *
 */

public class Server {

	private final static String[] CAR_SHAPE = { "Sport", "Salon", "Cabriolet" };
	private final static String[] CAR_TYPE = { "Mustang", "Porsh", "Yaguar", "Mercedes", "Mazda" };
	private final static String[] CAR_SIZE = { "Large", "Normal", "Mini" };
	private final static int[] CAR_SIZE_NUMBER = { 8, 13, 18 };
	private final static String[] CAR_COLOR = { "RED", "AQUA", "BLUE", "GREEN", "YELLOW", "ORANGE", "PINK", "VIOLET",
			"WHITE", "DARKCYAN" };
	private final static String GAMBLER_ID_FILE_NAME = "gamblerid.dat";
	private final static String RACE_ID_FILE_NAME = "raceid.dat";
	private final static int MIN_BET_CARS = 3;
	private final static double BOSS_PRECENT = 5;
	private final static int NUM_OF_CARS = 5;
	private ServerSocket serverSocket;
	private TextArea ta;
	private Connection connection;
	private Statement statement;
	private ResultSet rst;
	private boolean flag = true;
	private Scene scene;
	private boolean anyRaceIsOnGo = false;
	private ObjectOutputStream bossToClient;
	private ObjectInputStream bossToServer;
	private TableView<?> historyTableView = new TableView<Object>();

	// raceId // car details
	private Map<Integer, GeneratedCar[]> raceCarMap = new HashMap<>();
	// raceId // races to begin
	private List<Integer> raceHandlerList = new ArrayList<>();
	// raceId // car accumulate bet
	private Map<Integer, Integer[]> gamblerBetHanlderMap = new HashMap<>();
	// raceId // all bets for each race
	private Map<Integer, ArrayList<GamblerBet>> gamblerBetMap = new HashMap<>();
	// gmblrId // gamblers map
	private Map<Integer, ObjectOutputStream> gamblerMap = new HashMap<>();
	// raceid // race map
	private Map<Integer, ObjectOutputStream> raceMap = new HashMap<>();

	public Server() {

		initiateDataBase();

		createHistoryWindow();
		// createRetrievalWindow();

		new Thread(() -> {

			createServerSocket();

			while (flag) {

				try {

					Socket socket = serverSocket.accept();

					ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream toServer = new ObjectInputStream(socket.getInputStream());

					ServerProtocol sp = (ServerProtocol) toServer.readObject();
					Platform.runLater(() -> {
						ta.appendText(sp.getProtocol().toString() + " Connected" + '\n');
					});

					if (sp.getProtocol() == ServerProtocol.serverProtocol.RACE_CLIENT)
						new Thread(new HandleARaceClient(toClient, toServer)).start();

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.NEW_GAMBLER_CLIENT
							|| sp.getProtocol() == ServerProtocol.serverProtocol.EXISITING_GAMBLER_CLIENT) {

						new Thread(new HandleAGamblerClient(toClient, toServer, sp)).start();

					} else if (sp.getProtocol() == ServerProtocol.serverProtocol.BOSS_CLIENT) {

						bossToClient = toClient;
						bossToServer = toServer;

						bossToClient.writeObject(readFromFile(RACE_ID_FILE_NAME));
						// bossToClient.writeObject(readFromFile(GAMBLER_ID_FILE_NAME));

					} else if (sp.getProtocol() == ServerProtocol.serverProtocol.CLOSE_SERVER) {

						this.flag = false;
						socket.close();
						closeServerSocket();
					}

				} catch (IOException e) {

				} catch (ClassNotFoundException e) {

					e.printStackTrace();
				}

			}

		}).start();

		ta = new TextArea();
		ta.setEditable(false);
		ta.setWrapText(true);
		ta.setPrefSize(650, 450);

		scene = new Scene(new ScrollPane(ta), 655, 455);

	}

	// @SuppressWarnings({"unchecked", "rawtypes"})
	// private void createRetrievalWindow() {
	//
	// VBox vb = new VBox();
	// Button find = new Button("find");
	//
	// FlowPane flowPane = new FlowPane(3, 3);
	//
	// TextArea gamblerID = new TextArea();
	// TextArea raceID = new TextArea();
	//
	// gamblerID.setPrefColumnCount(10);
	// gamblerID.setPrefRowCount(1);
	// raceID.setPrefRowCount(1);
	// raceID.setPrefColumnCount(10);
	//
	// flowPane.getChildren().addAll(new Label("Gambler: "), gamblerID, new
	// Label("Race:"), raceID, find);
	// vb.getChildren().addAll(flowPane, retrievalTableView);
	//
	// find.setOnAction(e -> {
	//
	// String query = "";
	// String gamblerId = gamblerID.getText();
	// String raceId = raceID.getText();
	//
	//
	// ObservableList data = FXCollections.observableArrayList();
	// retrievalTableView.setItems(data);
	//
	// gamblerID.clear();
	// raceID.clear();
	//
	// try {
	//
	// if (!gamblerId.isEmpty() && !raceId.isEmpty()) {
	//
	// query = "select Student.firstName,Faculty.firstName,Course.title from
	// Enrollment,TaughtBy,Course,Student,Faculty"
	// + " where Enrollment.ssn = '" + gamblerId + "'" + " and Student.ssn = '"
	// + gamblerId + "'"
	// + " and Enrollment.courseId = TaughtBy.courseId" + " and TaughtBy.ssn =
	// Faculty.ssn"
	// + " and TaughtBy.courseId = Course.courseId";
	//
	// }
	//
	// else if (gamblerId.isEmpty() && !raceId.isEmpty()) {
	//
	// // String cars, result, gamblers;
	//
	// query = "select car.carid, car.type, car.shape, car.color, car.size from
	// car where car.raceId = "+raceId;
	// rst = statement.executeQuery(query);
	// rst.first();
	//
	//
	//
	// }
	//
	// else if (!gamblerId.isEmpty() && raceId.isEmpty()) {
	//
	// query = "select * from gambler where gambler.gamblerId = '"+ gamblerId
	// +"'";
	//
	// }
	//
	// } catch (SQLException e1) {
	//
	// e1.printStackTrace();
	// }
	//
	//
	// if (!query.isEmpty()) {
	//
	// // populateTableView(query, retrievalTableView);
	//
	// }
	// });
	//
	// Scene scene = new Scene(vb, 530, 300);
	// Stage primaryStage = new Stage();
	// primaryStage.setScene(scene);
	// primaryStage.setTitle("Retrieval Window");
	// primaryStage.show();
	// primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	// public void handle(WindowEvent event) {
	//
	// }
	// });
	//
	// }

	
	/**
	 *  This function creates the Database window on the game, it brings us current data and history data from races, gamblers, cars and more.
	 *  all the information we need
	 */
	
	private void createHistoryWindow() {

		Button btShowHistory = new Button("Submit");
		ComboBox<String> cb = new ComboBox<>();

		Button btShowContents = new Button("Show Contents");
		ComboBox<String> cboTableName = new ComboBox<>();
		Label lblStatus = new Label();
		TextField ta = new TextField();

		HBox hb = new HBox(5);
		hb.getChildren().addAll(new Label("Query Name"), cb, btShowHistory);
		hb.setAlignment(Pos.CENTER);

		ta.setPrefHeight(1);
		ta.setPrefColumnCount(5);

		HBox hBox = new HBox(5);
		hBox.getChildren().addAll(new Label("Table Name"), cboTableName, new Label("ID:"), ta, btShowContents);
		hBox.setAlignment(Pos.CENTER);

		VBox vb = new VBox(5);
		vb.getChildren().addAll(hBox, hb);

		BorderPane pane = new BorderPane();
		pane.setCenter(historyTableView);
		pane.setTop(vb);
		pane.setBottom(lblStatus);
		// Create a scene and place it in the stage
		Scene scene = new Scene(pane, 1000, 300);
		Stage primaryStage = new Stage();
		primaryStage.setTitle("History Window"); // Set the stage title
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.show(); // Display the stage

		cb.getItems().addAll("Race Details", "Gambler Details");
		cb.getSelectionModel().selectFirst();

		try {
			DatabaseMetaData dbMetaData = connection.getMetaData();
			ResultSet rsTables = dbMetaData.getTables(null, null, null, new String[] { "TABLE" });
			// System.out.print("User tables: ");
			while (rsTables.next()) {
				if (!rsTables.getString("TABLE_NAME").equals("gamblerregisteration"))
					cboTableName.getItems().add(rsTables.getString("TABLE_NAME"));
			}
			cboTableName.getSelectionModel().selectFirst();

		} catch (SQLException e1) {

			e1.printStackTrace();
		}

		btShowHistory.setOnAction(e -> {

			String tableName = cb.getValue();

			writeTableView(tableName, historyTableView);

		});

		btShowContents.setOnAction(e -> {

			String tableName = cboTableName.getValue();
			String queryString;
			if (ta.getText().isEmpty())
				queryString = "select * from " + tableName;
			else
				queryString = "select * from " + tableName + " where " + tableName + "Id = " + ta.getText();

			ta.clear();
			populateTableView(queryString, historyTableView);

		});

	}

	/**
	 * Inserts information to the table view by the selected table name from the combo box
	 * @param tableName the table name selected from the combo box
	 * @param tableView the table view that we want to insert information
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void writeTableView(String tableName, TableView tableView) {

		tableView.getColumns().clear();
		ObservableList<?> data = FXCollections.observableArrayList();
		tableView.setItems(data);

		if (tableName.equals("Race Details"))
			showRaceDetails(tableView);
		if (tableName.equals("Gambler Details"))
			showGamblerDetails(tableView);

	}
	
	/**
	 * extracts dynamic information by different querys from the sql to get gamblers information on one view 
	 * @param tableView the table view that we want to insert information
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showGamblerDetails(TableView tableView) {

		String columnLabels[] = { "Gambler ID", "Name", "Race Played", "Car Bet", "Total Bet", "Total Prize",
				"Profit" };

		for (int i = 0; i < columnLabels.length; i++) {
			int columNum = i;
			TableColumn<String[], String> column = new TableColumn<>(columnLabels[columNum]);
			column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columNum]));
			tableView.getColumns().add(column);
		}

		try {

			String gamblerDetailQuery = "Select * from Gambler";
			rst = statement.executeQuery(gamblerDetailQuery);

			if (rst.first()) {

				int count = 0;
				do {
					count++;
				} while (rst.next());

				rst.first();

				String gamblerIds[] = new String[count];
				String gamblerNames[] = new String[count];

				gamblerIds[0] = rst.getString(1);
				gamblerNames[0] = rst.getString(2);

				for (int i = 1; rst.next(); i++) {

					gamblerIds[i] = rst.getString(1);
					gamblerNames[i] = rst.getString(2);

				}

				for (int i = 0; i < gamblerIds.length; i++) {

					String races = "";
					String cars = "";
					int totalbet = 0;
					double totalprize = 0;
					double profit = 0;
					Set<String> racesSet = new HashSet<String>();

					rst = statement
							.executeQuery("Select * from GamblerBet where GamblerBet.gamblerBetId = " + gamblerIds[i]);
					if (rst.first()) {
						do {

							cars += rst.getString(2) + ", ";
							totalbet += rst.getInt(4);
							racesSet.add(rst.getString(1));

						} while (rst.next());
					}

					Iterator it = racesSet.iterator();
					while (it.hasNext()) {
						races += it.next() + ", ";
					}

					rst = statement.executeQuery(
							"Select * from GamblerPrize where GamblerPrize.gamblerPrizeId = " + gamblerIds[i]);
					if (rst.first()) {
						do {

							totalprize += rst.getDouble(3);

						} while (rst.next());
					}

					profit = totalprize - totalbet;

					String cells[] = new String[columnLabels.length];
					cells[0] = gamblerIds[i];
					cells[1] = gamblerNames[i];
					cells[2] = races;
					cells[3] = cars;
					cells[4] = totalbet + "";
					cells[5] = totalprize + "";
					cells[6] = profit + "";

					tableView.getItems().add(cells);

				}
			}

		} catch (SQLException e) {

			e.printStackTrace();
		}

	}

	/**
	 * extracts dynamic information by different querys from the sql to get races information on one view 
	 * @param tableView the table view that we want to insert information
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void showRaceDetails(TableView tableView) {

		String columnLabels[] = { "Race ID", "Date Begin", "Date finish", "Cars", "Car Winner", "Distance", "Total Bet", "Profit", "Gamblers Played" };

		for (int i = 0; i < columnLabels.length; i++) {
			int columNum = i;
			TableColumn<String[], String> column = new TableColumn<>(columnLabels[columNum]);
			column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columNum]));
			tableView.getColumns().add(column);
		}

		try {

			rst = statement.executeQuery("Select * from RaceBegin");

			if (rst.first()) {

				int count = 0;
				do {
					count++;
				} while (rst.next());

				String raceIds[] = new String[count];
				String datesBegin[] = new String[count];

				rst.first();

				raceIds[0] = rst.getString(1);
				datesBegin[0] = rst.getString(2);

				for (int i = 1; rst.next(); i++) {

					raceIds[i] = rst.getString(1);
					datesBegin[i] = rst.getString(2);

				}

				for (int i = 0; i < raceIds.length; i++) {

					String cars = "";
					String carWinner = "";
					double totalBets = 0;
					double profit = 0;
					String gamblers = "";
					String dateFinish = "";
					String distance = "";
					Set<String> gamblersSet = new HashSet<String>();

					rst = statement.executeQuery("select car.carid from car where car.raceid = " + raceIds[i]);
					if (rst.first()) {
						do {
							cars += rst.getString(1) + ", ";
						} while (rst.next());
					}

					rst = statement.executeQuery("select * from RaceStatistic where raceStatisticId = " + raceIds[i]);
					if (rst.first()) {

						carWinner = rst.getString(2);
						distance = rst.getString(3);
						totalBets = rst.getDouble(4);
						profit = rst.getDouble(5);
						dateFinish = rst.getString(6);
					

					}

					rst = statement.executeQuery(
							"select GamblerBet.gamblerBetId from GamblerBet where GamblerBet.raceId = " + raceIds[i]);
					if (rst.first()) {
						do {
							gamblersSet.add(rst.getString(1));
						} while (rst.next());

						Iterator it = gamblersSet.iterator();
						while (it.hasNext()) {
							gamblers += it.next() + ", ";
						}

					}

					String cells[] = new String[columnLabels.length];
					cells[0] = raceIds[i];
					cells[1] = datesBegin[i];
					cells[2] = dateFinish;
					cells[3] = cars;
					cells[4] = carWinner;
					cells[5] = distance;
					cells[6] = totalBets + "";
					cells[7] = profit + "";
					cells[8] = gamblers + "";

					tableView.getItems().add(cells);

				}
			}

		} catch (SQLException e) {

			e.printStackTrace();
		}

	}
	
	/**
	 * extracts static information from tables
	 * @param query the table query 
	 * @param tableView the table view that we want to insert information	
	 */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateTableView(String query, TableView tableView) {

		tableView.getColumns().clear();
		ObservableList<?> data = FXCollections.observableArrayList();
		tableView.setItems(data);
		try {

			rst = statement.executeQuery(query);

			int numOfColums = rst.getMetaData().getColumnCount();
			for (int i = 1; i <= numOfColums; i++) {
				int columNum = i - 1;
				TableColumn<String[], String> column = new TableColumn<>(rst.getMetaData().getColumnLabel(i));
				column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()[columNum]));
				tableView.getColumns().add(column);
			}
			while (rst.next()) {
				String[] cells = new String[numOfColums];
				for (int i = 1; i <= numOfColums; i++)
					cells[i - 1] = rst.getString(i);
				tableView.getItems().add(cells);
			}

		} catch (Exception e) {
			System.out.println("Error on Building Data");
		}
	}

	public Scene getScene() {
		return scene;
	}

	private void closeServerSocket() {

		try {

			serverSocket.close();
			bossToClient.close();
			bossToServer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}
	
	/**
	 * reads the last race / gambler id that was on the server to continuously generate ids
	 * @param filename the file name to read the data
	 * @return Integer of the last id that was on
	 */

	private Integer readFromFile(String filename) {

		try {

			RandomAccessFile raf = new RandomAccessFile(filename, "r");

			raf.seek(0);
			Integer id = (Integer) raf.readInt();
			raf.close();

			return id;

		} catch (FileNotFoundException e) {

			writeToFile(0, filename);
			return readFromFile(filename);

		} catch (IOException e) {

			e.printStackTrace();
		}

		return null;
	}

	/**
	 * writes the last race / gambler id that was on the server to continuously generate ids
	 * @param id the last id that was generated
	 * @param filename the file name to write the data
	 */
	
	private void writeToFile(Integer id, String filename) {

		try {

			RandomAccessFile raf = new RandomAccessFile(filename, "rw");

			raf.seek(0);
			raf.writeInt(id);

			raf.close();

		} catch (FileNotFoundException e) {

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 * Initiates the data base when the server is up
	 */
	
	private void initiateDataBase() {

		try {
			// Load the JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			// Class.forName("oracle.jdbc.driver.OracleDriver");
			Platform.runLater(() -> {
				ta.appendText("Driver loaded" + '\n');
			});
			// Establish a connection
			connection = DriverManager.getConnection("jdbc:mysql://localhost/javabook?useSSL=false", "scott", "tiger");
			// jdbc:mysql://localhost:3306/Peoples?autoReconnect=true&useSSL=false
			Platform.runLater(() -> {
				ta.appendText("Database connected" + '\n');
			});
			try {

				statement = connection.createStatement();

				RandomAccessFile raf = new RandomAccessFile("database_created", "r");
				raf.close();

			} catch (FileNotFoundException e) {

				createNewDatabase();

			} catch (SQLException e) {

				e.printStackTrace();
			}

			// Create a statement

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * Creates a new Database on first login.
	 * it manipulates Files to know if its the first time the server is up
	 */

	private void createNewDatabase() {

		try {

			RandomAccessFile raf = new RandomAccessFile("database_created", "rw");
			raf.close();

			// statement = connection.createStatement();

			FileInputStream fstream = new FileInputStream("createsampletables_mysql.txt");
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = "", strLine1 = "";
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				if (strLine != null && !strLine.trim().equals("")) {
					if ((strLine.trim().indexOf("/*") < 0) && (strLine.trim().indexOf("*/") < 0)) {
						if (strLine.indexOf(';') >= 0) {
							strLine1 += strLine;
							System.out.println(strLine1);
							statement.execute(strLine1);
							strLine1 = "";
						} else
							strLine1 += strLine;
					}
				}
			}

			br.close();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void createServerSocket() {

		try {
			serverSocket = new ServerSocket(8000);
			Platform.runLater(() -> {
				ta.appendText("Server started -> " + new Date() + '\n');
			});
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 * update dynamically race lists to gamblers by sending them all races that are on 
	 * and races that were over/canceled
	 */
	
	private void updateRaceListsToAllGamblers() {

		String[] raceList = new String[raceMap.size()];
		Iterator<Integer> raceIter = raceMap.keySet().iterator();
		Iterator<ObjectOutputStream> gamblerIter = gamblerMap.values().iterator();
		ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.GAMBLER_UPDATE_RACE_LIST);

		for (int i = 0; i < raceList.length; i++) {
			raceList[i] = raceIter.next() + "";
		}

		sp.setRaceList(raceList);

		for (int i = 0; i < gamblerMap.size(); i++) {

			try {
				gamblerIter.next().writeObject(sp);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

	}
	
	/**
	 * Notifies the race with the most bets on, to begin. it is synchronized because all handlers can fire this function
	 * and it has to be synchronized to avoid starting a race a few times. 
	 * 
	 */

	private synchronized void notifyRaceToBegin() {

		if (!anyRaceIsOnGo && raceHandlerList.size() > 0) {

			Integer raceId = -1;
			int totalAmountOfBet = 0;

			if (raceHandlerList.size() > 1) {

				Iterator<Integer> raceIdIter = raceHandlerList.iterator();
				Integer[] carBets;

				for (int i = 0; i < raceHandlerList.size(); i++) {

					int tempTotalAmountOfBet = 0;
					Integer tempRaceId = raceIdIter.next();
					carBets = gamblerBetHanlderMap.get(tempRaceId);
					if (carBets != null) {
						for (int j = 0; j < carBets.length; j++) {
							tempTotalAmountOfBet += carBets[j];
						}
					}
					if (tempTotalAmountOfBet > totalAmountOfBet) {
						totalAmountOfBet = tempTotalAmountOfBet;
						raceId = tempRaceId;

					}
				}
			} else if (raceHandlerList.size() == 1) {
				raceId = raceHandlerList.iterator().next();
			}

			if (raceId != -1) {

				ObjectOutputStream raceClient = raceMap.get(raceId);
				anyRaceIsOnGo = true;

				raceHandlerList.remove(raceId);
				raceMap.remove(raceId);

				String raceBegin = "Race: " + raceId + " Has Begun";
				Platform.runLater(() -> {
					ta.appendText(raceBegin + '\n');
				});

				try {

					ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.START_RACE);
					raceClient.writeObject(sp);

					sp = new ServerProtocol(ServerProtocol.serverProtocol.UPDATE_VIEW_RACE);
					sp.setStr(raceBegin);
					bossToClient.writeObject(sp);

					updateRaceListsToAllGamblers();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Every time a gambler bet is commited, this function checks if a race can start.
	 * 
	 */

	private boolean checkIfRaceCanStart(int raceId) {

		int carBetCounter = 0;
		Integer[] carBetArr;

		if (!gamblerBetHanlderMap.containsKey(raceId))
			return false;

		carBetArr = gamblerBetHanlderMap.get(raceId);

		for (int i = 0; i < carBetArr.length; i++) {

			if (carBetArr[i] > 0)
				carBetCounter++;

			if (carBetCounter >= MIN_BET_CARS) {
				raceHandlerList.add(raceId);
				return true;
			}
		}

		String raceWaitingUpdate = "Race: " + raceId + " is waiting for " + (MIN_BET_CARS - carBetCounter)
				+ " more car bets";

		Platform.runLater(() -> {
			ta.appendText(raceWaitingUpdate + '\n');
		});

		ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.UPDATE_VIEW_RACE);
		sp.setStr(raceWaitingUpdate);

		try {
			bossToClient.writeObject(sp);
		} catch (IOException e) {

			e.printStackTrace();
		}

		return false;
	}

	/**
	 * A Gambler Handler class which handles a gambler client
	 * @author Eviatar Admon
	 *
	 */
	
	private class HandleAGamblerClient implements Runnable {

		private ObjectOutputStream toClient;
		private ObjectInputStream toServer;
		private Integer gmblrId;
		private boolean gamblerFlag = true;
		private ServerProtocol user;

		public HandleAGamblerClient(ObjectOutputStream toClient, ObjectInputStream toServer, ServerProtocol sp) {

			this.toClient = toClient;
			this.toServer = toServer;
			this.user = sp;

		}

		@Override
		public void run() {

			try {

				if (user.getProtocol() == ServerProtocol.serverProtocol.EXISITING_GAMBLER_CLIENT) {
					while (!checkUser()) {
						user = (ServerProtocol) toServer.readObject();
					}
				} else
					createNewUser();

				gamblerMap.put(gmblrId, toClient);

				Platform.runLater(() -> {
					ta.appendText("Gambler: " + gmblrId + '\n');
				});

				updateRaceListsToGambler();

				while (gamblerFlag) {

					ServerProtocol sp = (ServerProtocol) toServer.readObject();

					if (sp.getProtocol() == ServerProtocol.serverProtocol.GAMBLER_BET) {

						GamblerBet gb = sp.getGamblerBet();
						addBetToMap(gb);
						if (checkIfRaceCanStart(gb.getRaceId()))
							if (!anyRaceIsOnGo)
								notifyRaceToBegin();
					}else if (sp.getProtocol() == ServerProtocol.serverProtocol.GET_CAR_RACE_DETAILS){
						
						sp.setCar(raceCarMap.get(sp.getRaceNum()));
						toClient.writeObject(sp);
						
						
					}
					else if (sp.getProtocol() == ServerProtocol.serverProtocol.CLOSE_CLIENT)
						closeClient();

				}

			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (SQLException e) {

				e.printStackTrace();
			}
		}
		
		/**
		 * Creates a new Gambler client
		 * @throws SQLException writes gambler to database
		 * @throws IOException 
		 */

		private void createNewUser() throws SQLException, IOException {

			Integer gamblerId = readFromFile(GAMBLER_ID_FILE_NAME);
			gamblerId++;
			toClient.writeObject(gamblerId);

			CallableStatement callableStatement = connection
					.prepareCall("insert into GamblerRegisteration values(?, ?, ?);");
			callableStatement.setString(1, gamblerId + "");
			callableStatement.setString(2, user.getUserName());
			callableStatement.setString(3, user.getPassword());

			callableStatement.execute();

			callableStatement = connection.prepareCall("insert into Gambler values(?, ?);");
			callableStatement.setString(1, gamblerId + "");
			callableStatement.setString(2, user.getUserName());

			callableStatement.execute();
			// System.out.println(gamblerId);
			writeToFile(gamblerId, GAMBLER_ID_FILE_NAME);

			this.gmblrId = gamblerId;

		}
		
		/**
		 * checks if an existing gambler that was trying to connect, exists.
		 * @return boolean if the client exists on data base
		 * @throws SQLException reads client information from databse (if exists)
		 * @throws IOException
		 * @throws ClassNotFoundException gets data from stream
		 */

		private boolean checkUser() throws SQLException, IOException, ClassNotFoundException {

			String query = "select GamblerRegisteration.gamblerId from GamblerRegisteration"
					+ " where GamblerRegisteration.name = '" + user.getUserName()
					+ "' and GamblerRegisteration.password =  '" + user.getPassword() + "'";

			// String query = "select GamblerRegisteration.gamblerId
			// from GamblerRegisteration"
			// + " where GamblerRegisteration.name = 'ev' and
			// GamblerRegisteration.password = '123'";

			rst = statement.executeQuery(query);

			if (rst.first()) {

				Integer gamblerId = Integer.parseInt(rst.getString(1));
				user.setGamblerId(gamblerId);
				user.setPermissionApproved(true);

				toClient.writeObject(user);

				this.gmblrId = gamblerId;

				return true;

			} else {

				user.setPermissionApproved(false);
				toClient.writeObject(user);
				return false;

			}

		}
		
		/**
		 * 
		 * updates race list to a single gambler which was connected.
		 * 
		 */

		private void updateRaceListsToGambler() {

			String[] raceList = new String[raceMap.size()];
			Iterator<Integer> raceIter = raceMap.keySet().iterator();
			ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.GAMBLER_UPDATE_RACE_LIST);

			for (int i = 0; i < raceList.length; i++) {
				raceList[i] = raceIter.next() + "";
			}

			sp.setRaceList(raceList);

			try {
				toClient.writeObject(sp);
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		private void closeClient() {

			Platform.runLater(() -> {
				ta.appendText("Gambler: " + gmblrId + " Has disconnected" + '\n');
			});

			try {
				gamblerFlag = false;
				gamblerMap.get(gmblrId).writeObject(new ServerProtocol(ServerProtocol.serverProtocol.CLOSE_CLIENT));
				gamblerMap.remove(gmblrId);
				toClient.close();
				toServer.close();

			} catch (IOException e) {

				e.printStackTrace();
			}

		}

		/**
		 * add gamblers bet on handler map
		 * @param gb GamblerBet class helper
		 */
		
		private synchronized void addBetToHanlderMap(GamblerBet gb) {

			Integer[] carBetArr;

			if (gamblerBetHanlderMap.containsKey(gb.getRaceId()))
				carBetArr = gamblerBetHanlderMap.get(gb.getRaceId());
			else {
				carBetArr = new Integer[NUM_OF_CARS];
				for (int i = 0; i < NUM_OF_CARS; i++)
					carBetArr[i] = 0;
			}

			carBetArr[gb.getCarId()] += gb.getBetAmount();

			gamblerBetHanlderMap.put(gb.getRaceId(), carBetArr);

		}
		
		/**
		 * add gamblers bet on map and writes it to database.
		 * synchronized to avoid 2 gamblers that can add a bet to the same race on the map
		 * @param gb GamblerBet class helper
		 * @throws SQLException writes gamblers bet to gamblers bet table
		 */

		private synchronized void addBetToMap(GamblerBet gb) throws SQLException {

			ArrayList<GamblerBet> l;

			if (gamblerBetMap.containsKey(gb.getRaceId()))
				l = gamblerBetMap.get(gb.getRaceId());
			else
				l = new ArrayList<>();

			l.add(gb);

			Platform.runLater(() -> {
				ta.appendText("Gambler: " + gb.getGamblerId() + " Race: " + gb.getRaceId() + " Car: "
						+ (gb.getCarId() + 1) + " Amount of Bet: " + gb.getBetAmount() + '\n');
			});

			CallableStatement callableStatement = connection.prepareCall("insert into GamblerBet values(?, ?, ?, ?);");
			callableStatement.setString(1, gb.getRaceId() + "");
			callableStatement.setString(2, (gb.getRaceId() * 10) + (gb.getCarId() + 1) + "");
			callableStatement.setString(3, gb.getGamblerId() + "");
			callableStatement.setInt(4, gb.getBetAmount());

			callableStatement.execute();

			gamblerBetMap.put(gb.getRaceId(), l);

			addBetToHanlderMap(gb);

		}
	}

	/**
	 * A Race Handler class which handles a race client
	 * @author Eviatar Admon
	 *
	 */
	
	private class HandleARaceClient implements Runnable {

		private ObjectOutputStream toClient;
		private ObjectInputStream toServer;
		private Integer raceId;
		private double[] carDistance = new double[NUM_OF_CARS];
		private boolean raceFlag = true;

		public HandleARaceClient(ObjectOutputStream toClient, ObjectInputStream toServer) {

			this.toClient = toClient;
			this.toServer = toServer;

			for (int i = 0; i < NUM_OF_CARS; i++)
				carDistance[i] = 0;

		}

		@Override
		public void run() {

			try {

				raceId = ((Integer) toServer.readObject());

				writeToFile(raceId, RACE_ID_FILE_NAME);

				Platform.runLater(() -> {
					ta.appendText("Race: " + raceId + '\n');
				});

				raceMap.put(raceId, toClient);

				CallableStatement callableStatement = connection.prepareCall("insert into RaceBegin values(?, ?);");
				callableStatement.setString(1, raceId + "");
				callableStatement.setString(2, new Date().toString());

				callableStatement.execute();

				updateRaceListsToAllGamblers();
				sendPermissionForGeneratingCars();

				while (raceFlag) {

					ServerProtocol sp = (ServerProtocol) toServer.readObject();

					if (sp.getProtocol() == ServerProtocol.serverProtocol.GENERATE_CAR)
						generateCar(sp);

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.EXECUTE_QUERY)
						executeQuery(sp);

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.LOG_EVENT)
						addLogEvent(sp);

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.GENERATE_CAR_SPEED)
						generateCarSpeed(sp);

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.RACE_OVER)
						raceOver(sp);
					else if (sp.getProtocol() == ServerProtocol.serverProtocol.CLOSE_CLIENT)
						raceClosed(sp);

				}

			} catch (IOException e) {

				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} catch (SQLException e) {

				e.printStackTrace();
			}

		}
		
/**
 * Sends permission for generating cars to avoid client sending a request before server is on
 * @throws IOException
 */
		private void sendPermissionForGeneratingCars() throws IOException {

			ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.PERMISSION);
			toClient.writeObject(sp);

		}

		/**
		 * When a race is closed before it begun, it is removed from server
		 * @param sp Holds the Server Protocol for closing a race
		 * @throws IOException 
		 */
		
		private void raceClosed(ServerProtocol sp) throws IOException {

			Platform.runLater(() -> {
				ta.appendText("Race: " + raceId + " Has disconnected" + '\n');
			});
			raceFlag = false;
			raceMap.get(raceId).writeObject(sp);
			raceMap.remove(raceId);
			raceCarMap.remove(raceId);
			gamblerBetMap.remove(raceId);
			gamblerBetHanlderMap.remove(raceId);
			toClient.close();
			toServer.close();
			updateRaceListsToAllGamblers();

		}
		

	/**
	 * When a race is over, notifies the next race to begin, 
	 * @param sp Holds the Server Protocol for ending a race
	 * @throws IOException
	 * @throws SQLException
	 */

		private void raceOver(ServerProtocol sp) throws IOException, SQLException {

			gamblerWinner(carWinner());

			String raceOver = "Race: " + raceId + " is over";
			Platform.runLater(() -> {
				ta.appendText(raceOver + '\n');
			});

			ServerProtocol raceIsOver = new ServerProtocol(ServerProtocol.serverProtocol.UPDATE_VIEW_RACE);
			raceIsOver.setStr(raceOver);

			bossToClient.writeObject(raceIsOver);

			anyRaceIsOnGo = false;
			raceFlag = false;

			Iterator<Integer> raceIter = raceMap.keySet().iterator();
			for (int i = 0; i < raceMap.size(); i++) {
				checkIfRaceCanStart(raceIter.next());
			}

			gamblerBetMap.remove(raceId);
			gamblerBetHanlderMap.remove(raceId);
			raceCarMap.remove(raceId);

			sp = new ServerProtocol(ServerProtocol.serverProtocol.START_RACE);
			sp.setRaceNum(raceId);

			bossToClient.writeObject(sp);

			notifyRaceToBegin();

		}

		/**
		 * When a race is over, this function checks which gambler won by the winning car
		 * @param carWinner the number of the car who won
		 * @throws SQLException for adding the information to Race Statistic table
		 */
		
		private void gamblerWinner(int carWinner) throws SQLException {

			double sumBetAmounts = 0;
			Integer[] carBets = gamblerBetHanlderMap.get(raceId);
			double bossFee = ((double) (100 - BOSS_PRECENT) / 100);
			ArrayList<GamblerBet> gamblersBets = gamblerBetMap.get(raceId);

			double winnerGamblersBetAmount = 0;
			ArrayList<GamblerBet> BetWinners = new ArrayList<>();

			// calculate bet
			for (int i = 0; i < NUM_OF_CARS; i++) {
				sumBetAmounts += carBets[i];

			}

			CallableStatement callableStatement = connection
					.prepareCall("insert into RaceStatistic values(?, ?, ?, ?, ?, ?);");
			callableStatement.setString(1, raceId + "");
			callableStatement.setString(2, (raceId * 10) + (carWinner + 1) + "");
			callableStatement.setString(3, ((int)carDistance[carWinner])+"");
			callableStatement.setDouble(4, sumBetAmounts);
			callableStatement.setDouble(5, (sumBetAmounts - (sumBetAmounts * bossFee)));
			callableStatement.setString(6, new Date().toString());
			callableStatement.execute();

			double tempSumBetamounts = sumBetAmounts;
			Platform.runLater(() -> {
				ta.appendText("Race: " + raceId + " Total Bets: " + tempSumBetamounts + '\n');
			});

			// check win bets
			for (int i = 0; i < gamblersBets.size(); i++) {

				GamblerBet gb = gamblersBets.get(i);

				if (gb.getCarId() == carWinner) {
					// System.out.println("Bet Winner
					// gambler:"+gb.getGamblerId());
					BetWinners.add(gb);
				}
			}

			// sum winners bet amount
			for (int i = 0; i < BetWinners.size(); i++) {
				winnerGamblersBetAmount += BetWinners.get(i).getBetAmount();
			}
			// System.out.println("winner gambler bet Amount:
			// "+winnerGamblersBetAmount);

			// take 5% to boss

			// System.out.println("boss fee: "+bossFee);
			sumBetAmounts = sumBetAmounts * bossFee;

			final double tempSumBetamount = sumBetAmounts;
			Platform.runLater(() -> {
				ta.appendText("Race: " + raceId + " Total Bets after fee: " + tempSumBetamount + '\n');
			});

			// send prizes to winners
			for (int i = 0; i < BetWinners.size(); i++) {

				GamblerBet gamblerBet = BetWinners.get(i);
				Integer gamblerId = gamblerBet.getGamblerId();
				double gamblerBetAmount = gamblerBet.getBetAmount();
				double prize = sumBetAmounts * (gamblerBetAmount / winnerGamblersBetAmount);
				// System.out.println("Gambler id: "+gamblerId+" prize:
				// "+prize);
				ObjectOutputStream gamblerStream = gamblerMap.get(gamblerId);
				ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.GAMBLER_PRIZE);
				sp.setVirtualMoney(prize);

				callableStatement = connection.prepareCall("insert into GamblerPrize values(?, ?, ?);");
				callableStatement.setString(1, gamblerId + "");
				callableStatement.setString(2, raceId + "");
				callableStatement.setDouble(3, (int)prize);

				callableStatement.execute();

				try {
					gamblerStream.writeObject(sp);
				} catch (IOException e) {

					e.printStackTrace();
				}
			}

		}
		
		/**
		 * checks which car has won the race
		 * @return int of the car number who won
		 */

		private int carWinner() {

			int carWinnerId = 0;
			double maxCarDistance = 0;

			for (int i = 0; i < carDistance.length; i++) {
				if (carDistance[i] > maxCarDistance) {
					maxCarDistance = carDistance[i];
					carWinnerId = i;
				}
			}

			String carWinner = "Winner Car: " + (carWinnerId + 1);
			Platform.runLater(() -> {
				ta.appendText(carWinner + '\n');
			});

			ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.UPDATE_VIEW_RACE);
			sp.setStr(carWinner);
			try {
				bossToClient.writeObject(sp);
			} catch (IOException e) {

				e.printStackTrace();
			}

			return carWinnerId;

		}
		
		/**
		 * a request from the race client to generate new car speeds
		 * @param sp Holds the Server Protocol for generating new car speed
		 * @throws IOException 
		 */

		private void generateCarSpeed(ServerProtocol sp) throws IOException {

			final int MAX_SPEED = 20, MIN_SPEED = 1;
			double speed;
			if (sp.getCarId() == 0)
				speed = 100;
			else
				speed = (double) (((int) (Math.random() * 1000)) % (MAX_SPEED - MIN_SPEED)) + MIN_SPEED;

			sp.setSpeed(speed);

			carDistance[sp.getCarId()] += speed * sp.getTimeInterval();

			toClient.writeObject(sp);

		}
		
		/**
		 * adds an event to the server Text Area
		 * @param sp Holds the Server Protocol for adding a log event
		 */

		private void addLogEvent(ServerProtocol sp) {

			ta.appendText(sp.getProtocol().toString() + "\n");
			ta.appendText("Race: " + sp.getRaceNum() + " event: " + sp.getStr() + "\n");

		}
		
		/**
		 * Generates cars for the race client model.
		 * @param sp Holds the Server Protocol for generating a car
		 * @throws IOException
		 * @throws SQLException Adds the generated cars to the Car Table
		 */

		private void generateCar(ServerProtocol sp) throws IOException, SQLException {

			ta.appendText("Generating Cars.." + "\n");

			GeneratedCar[] cars = new GeneratedCar[NUM_OF_CARS];

			CallableStatement callableStatement;

			for (int i = 0; i < NUM_OF_CARS; i++) {

				int colorIndex = ((int) (Math.random() * 1000)) % CAR_COLOR.length;
				int sizeIndex = ((int) (Math.random() * 1000)) % CAR_SIZE.length;
				int threeDCarModelIndex = ((int) (Math.random() * 1000)) % CAR_SHAPE.length;
				int carTypeIndex = ((int) (Math.random() * 1000)) % CAR_TYPE.length;

				// System.out.println("color :" +CAR_COLOR[colorIndex]);
				// System.out.println("size :" +CAR_SIZE[sizeIndex]);
				// System.out.println("shape :"
				// +CAR_SHAPE[threeDCarModelIndex]);
				// System.out.println("type :" +CAR_TYPE[carTypeIndex]);

				// statement.execute("insert into Car " + " values(" + i + "," +
				// car.getCarType() + "," +
				// car.getCarShape() + "," +
				// car.getCarColor() + "," +
				// car.getCarSize() + ");");

				GeneratedCar car = new GeneratedCar(CAR_SHAPE[threeDCarModelIndex], threeDCarModelIndex,
						CAR_TYPE[carTypeIndex], CAR_SIZE[sizeIndex], CAR_SIZE_NUMBER[sizeIndex], CAR_COLOR[colorIndex],
						colorIndex);

				// String query = "insert into Car " + " values(" + i + "," +
				// car.getCarType() + "," +
				// car.getCarShape() + "," +
				// car.getCarColor() + "," +
				// car.getCarSize() + ");";

				String carId = raceId * 10 + (i + 1) + "";

				callableStatement = connection.prepareCall("insert into Car values(?, ?, ?, ?, ?, ?);");
				callableStatement.setString(1, carId);
				callableStatement.setString(2, raceId + "");
				callableStatement.setString(3, car.getCarType());
				callableStatement.setString(4, car.getCarShape());
				callableStatement.setString(5, car.getCarColor());
				callableStatement.setString(6, car.getCarSize());

				callableStatement.execute();

				// statement.execute(query);

				cars[i] = car;

			}

			sp.setCar(cars);
			sp.setRaceNum(raceId);

			raceCarMap.put(raceId, cars);

			// sp.setSpeed(1);
			toClient.writeObject(sp);

		}

		private void executeQuery(ServerProtocol sp) {

		}
	}
}