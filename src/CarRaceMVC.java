

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 
 * Car race gambling system with a client server on javafx using 3D Cars, Events, Threads, Pattern Designs, MVC models, SQL, Read & Write from a File and more.
 * 
 * @author Eviatar Admon
 * 
 */



public class CarRaceMVC extends Application {

	//private final static int INITIATE_NEW_RACE_START = 3;
	protected final static int NUM_OF_CARS = 5;
	private Button btnNewWindow = new Button("New Race");
	private Button btnNewGambler = new Button("New Gambler");
	private ArrayList<Controller> controllerList;
	private ArrayList<View> viewList;
	private ArrayList<Model> modelList;
	// raceid
	private Map<Integer, ObjectOutputStream> raceOutputStreams = new HashMap<>();
	// raceid
	private Map<Integer, Stage> raceStages = new HashMap<>();
	private Integer raceId = 0;
//	private Integer gamblerId = 0;
	private Socket socket;
	private ObjectOutputStream bossToServer;
	private ObjectInputStream bossToClient;
	private ViewRaceUpdate raceUpdate;

	@Override
	public void start(Stage primaryStage) {

		raceUpdate = new ViewRaceUpdate();

		HBox pane = new HBox(10);
		pane.getChildren().addAll(btnNewWindow, btnNewGambler);
		pane.setAlignment(Pos.CENTER);
		pane.setStyle("-fx-background-color: red");
		Scene scene = new Scene(pane, 400, 100);
		primaryStage.setScene(scene); // Place the scene in the stage
		primaryStage.setTitle("CarRaceMVC"); // Set the stage title
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {

				try {

					bossToServer.writeObject(ServerProtocol.serverProtocol.CLOSE_CLIENT);

				} catch (IOException e) {

					e.printStackTrace();
				}

				Platform.exit();
				System.exit(0);

			}
		});

		controllerList = new ArrayList<Controller>();
		viewList = new ArrayList<View>();
		modelList = new ArrayList<Model>();
		btnNewGambler.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {

				createGamblerWindow();
			}

		});

		btnNewWindow.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				createNewRaceWindow();
			}
		});
		primaryStage.show(); // Display the stage
		primaryStage.setAlwaysOnTop(true);

		createServerWindow();

		try {

			socket = new Socket("localhost", 8000);
			bossToServer = new ObjectOutputStream(socket.getOutputStream());
			bossToClient = new ObjectInputStream(socket.getInputStream());

			ServerProtocol sp;
			sp = new ServerProtocol(ServerProtocol.serverProtocol.BOSS_CLIENT);
			sp.setRaceNum(raceId);
			bossToServer.writeObject(sp);

			raceId = (Integer) bossToClient.readObject();
		//	gamblerId = (Integer) bossToClient.readObject();

		} catch (IOException e1) {

			e1.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

		new Thread(() -> {
			try {
				ServerProtocol sp;

				do {

					sp = (ServerProtocol) bossToClient.readObject();

					if (sp.getProtocol() == ServerProtocol.serverProtocol.START_RACE) {
						final Integer raceID = sp.getRaceNum();

						Platform.runLater(() -> {
							createNewRaceWindow();
						});

						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {

							e.printStackTrace();
						}

						Platform.runLater(() -> {

							raceStages.get(raceID).close();
							raceStages.remove(raceID);

						});
					}
					
					else if (sp.getProtocol() == ServerProtocol.serverProtocol.UPDATE_VIEW_RACE){
						raceUpdate.raceUpdate(sp.getStr());
					}
					
					else if (sp.getProtocol() == ServerProtocol.serverProtocol.UPDATE_HISTORY){
						
					}

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.CLOSE_CLIENT) {

						bossToClient.close();
						bossToServer.close();
						socket.close();

					}

				} while (sp.getProtocol() != ServerProtocol.serverProtocol.CLOSE_CLIENT);

			} catch (IOException e) {

				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			}
		}).start();

		// for (int i=0; i<INITIATE_NEW_RACE_START; i++)
		createNewRaceWindow();

	}

	public static void main(String[] args) {
		launch(args);
	}

	public void createGamblerWindow() {

		Socket socket;
		try {

		//	gamblerId++;
			socket = new Socket("localhost", 8000);
			ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream toClient = new ObjectInputStream(socket.getInputStream());
	//		toServer.writeObject(new ServerProtocol(ServerProtocol.serverProtocol.GAMBLER_CLIENT));
	//		toServer.writeObject(gamblerId);

			Gambler gmblr = new Gambler(toServer, toClient);

			Stage GamblerStage = gmblr.getStage();
			// Set the stage
			// title
			// GamblerStage.setScene(gmblr.getScene()); // Place the scene in
			// the
			// stage
			GamblerStage.show(); // Display the stage
			// GamblerStage.setAlwaysOnTop(true);
			GamblerStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent event) {

					try {
						toServer.writeObject(new ServerProtocol(ServerProtocol.serverProtocol.CLOSE_CLIENT));
						// socket.close();
					} catch (IOException e) {

						e.printStackTrace();
					}

				}
			});

		} catch (SocketException e) {

		} catch (IOException e) {

			e.printStackTrace();
		} 

	}

	public void createServerWindow() {

		Server server = new Server();
		Stage ServerStage = new Stage();

		ServerStage.setTitle("Car Server"); // Set the stage title
		ServerStage.setScene(server.getScene()); // Place the scene in the stage
		ServerStage.show(); // Display the stage
		// ServerStage.setAlwaysOnTop(true);
		ServerStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent event) {

				try {

					bossToServer.writeObject(ServerProtocol.serverProtocol.CLOSE_CLIENT);

				} catch (IOException e) {

					e.printStackTrace();
				}

				Platform.exit();
				System.exit(0);

			}
		});
	}

	public void createNewRaceWindow() {

		Socket socket;
		try {

			raceId++;
			socket = new Socket("localhost", 8000);
			ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream toClient = new ObjectInputStream(socket.getInputStream());
			toServer.writeObject(new ServerProtocol(ServerProtocol.serverProtocol.RACE_CLIENT));
			toServer.writeObject(raceId);

			Model model = new Model(raceId, NUM_OF_CARS, toServer);

			View view = new View(NUM_OF_CARS);
			view.setModel(model);

			Controller controller = new Controller(model, view, raceUpdate, NUM_OF_CARS, toClient, toServer);

			modelList.add(model);
			viewList.add(view);
			controllerList.add(controller);
			raceOutputStreams.put(raceId, toServer);

			Stage raceStage = new Stage();
			Scene scene = new Scene(view.getBorderPane(), 1000, 800);

			Camera camera = new PerspectiveCamera(false);

			scene.setCamera(camera);
			
			//camera.setRotate(45);

			controller.setOwnerStage(raceStage);
			// view.createAllTimelines();
			raceStage.setScene(scene);

			raceStage.setTitle("CarRace ID: " + raceId);
			// raceStage.setAlwaysOnTop(true);
			raceStage.show();
			raceStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent event) {

					try {

						String[] rId = raceStage.getTitle().split(" ");
						Integer raceID = Integer.parseInt(rId[2]);
						ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.CLOSE_CLIENT);
						raceOutputStreams.get(raceID).writeObject(sp);
						raceOutputStreams.remove(raceID);
						// socket.close();
					} catch (IOException e) {

						e.printStackTrace();
					}

				}
			});

			raceStages.put(raceId, raceStage);

			scene.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

					view.setCarPanesMaxWidth(newValue.doubleValue());
				}
			});
			
			

		} catch (SocketException e) {

		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}