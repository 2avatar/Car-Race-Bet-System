

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller implements CarEvents {

	private final int NUM_OF_INTERVALS = 3;
	private final int MAXSPEED = 200;
	private int numOfCars;
	private TextField tf[];
	private Stage stg;
	private Model model;
	private View view;
	private ViewRaceUpdate raceUpdate;
	private ObjectInputStream toClient;
	private ObjectOutputStream toServer;
	private String[] mediaNames = {"sexylady.mp3", "dekhomein.mp3", "dekhomein.mp3"};
	private String startRacePath = "startrace.mp3";
	private String endRacePath = "endrace.mp3";
	private GeneratedCar[] cars;
	private boolean flag = true;

	public Controller(Model model, View view, ViewRaceUpdate raceUpdate, int numOfCars, ObjectInputStream toClient,
			ObjectOutputStream toServer) {

		this.numOfCars = numOfCars;
		tf = new TextField[numOfCars];
		this.model = model;
		this.view = view;
		this.toClient = toClient;
		this.toServer = toServer;
		this.raceUpdate = raceUpdate;

		// wait for server to start race
		new Thread(() -> {

			try {

				ServerProtocol sp;

				do {

					sp = (ServerProtocol) toClient.readObject();

					if (sp.getProtocol() == ServerProtocol.serverProtocol.PERMISSION) {
						generateCars();
					}

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.UPDATE_VIEW_RACE){
						
						raceUpdate.raceUpdate(sp.getStr());
						
					}
					
					else if (sp.getProtocol() == ServerProtocol.serverProtocol.START_RACE) {

						Media startRaceMedia = new Media(new File(startRacePath).toURI().toString());
						MediaPlayer startRacePlayer = new MediaPlayer(startRaceMedia);
						
						startRacePlayer.play();
						
						Thread.sleep(4000);

						 String mediaName = mediaNames[((int)Math.random()*1000)%(mediaNames.length)];
						 MediaPlayer media = new MediaPlayer(new Media(new File(mediaName).toURI().toString()));
						 media.seek(new Duration(10000));
						// double mediaTime =
						// media.getTotalDuration().toSeconds();
						// final int timeInterval =
						// ((int)mediaTime)/NUM_OF_INTERVALS;

						int timeInterval = 10;

						Platform.runLater(() -> {
						    media.play();
							view.createAllTimelines(50);
							createCars();
							changeCarsSpeed(timeInterval);
						});

						for (int i = 0; i < NUM_OF_INTERVALS; i++) {
							Thread.sleep(timeInterval * 1000);
							Platform.runLater(() -> {
								changeCarsSpeed(timeInterval);
							});
						}
						Thread.sleep(timeInterval * 1000);
						media.stop();

						Platform.runLater(() -> {
							stopCars();
						});
						
						Media endRaceMedia = new Media(new File(endRacePath).toURI().toString());
						MediaPlayer endRacePlayer = new MediaPlayer(endRaceMedia);
						endRacePlayer.play();
						Thread.sleep(4000);

						sp = new ServerProtocol(ServerProtocol.serverProtocol.RACE_OVER);
						toServer.writeObject(sp);
						toClient.close();
						toServer.close();
						flag = false;
					}

					else if (sp.getProtocol() == ServerProtocol.serverProtocol.CLOSE_CLIENT) {
						toClient.close();
						toServer.close();
						flag = false;
					}

				} while (flag);

			
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

		}).start();
		
		// speedHandlers();
		// handle radius change
		// Slider radSlider = view.getRadSlider();
		// radSlider.valueProperty().addListener(e -> {
		// int car_index =
		// view.getItemsCar().indexOf(view.getCarIdComBox().getValue());
		// int oldRad = model.getCarById(car_index).getRadius();
		// int newRad = (int) radSlider.getValue();
		// if (oldRad != newRad)
		// model.changeRadius(car_index, newRad);
		// });
		// handle color change
		// Button btn = view.getColorButton();
		// btn.setOnAction(new EventHandler<ActionEvent>() {
		// @Override
		// public void handle(ActionEvent event) {
		//
		// view.createAllTimelines(50);
		// generateCars();
		//
		//
		// }
		// });

		// make the slider's value suitable to the selected car in the combo box
		// view.getCarIdComBox().setOnAction(new EventHandler<ActionEvent>() {
		// @Override
		// public void handle(ActionEvent event) {
		// int car_index =
		// view.getItemsCar().indexOf(view.getCarIdComBox().getValue());
		// int r = model.getCarById(car_index).getRadius();
		// view.getRadSlider().setValue(r);
		// }
		// });

	}

	private void createCars() {
		
		for (int i=0; i<numOfCars; i++){
		model.changeColor(i, cars[i].getColorIndex());
		model.changeRadius(i, cars[i].getSizeNumber());
		model.changeThreeDCarModel(i, cars[i].getShapeIndex());
		}
	}

	private void stopCars() {

		for (int i = 0; i < numOfCars; i++)
			view.stopAllTimelines();

	}

	public void changeCarsSpeed(int timeInterval) {

		ServerProtocol carSpeed = new ServerProtocol(ServerProtocol.serverProtocol.GENERATE_CAR_SPEED);
		carSpeed.setTimeInterval(timeInterval);

		for (int i = 0; i < numOfCars; i++) {

			try {
				carSpeed.setCarId(i);
				toServer.writeObject(carSpeed);
				carSpeed = (ServerProtocol) toClient.readObject();
				model.changeSpeed(i, carSpeed.getSpeed());

			} catch (IOException e) {

				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			}
		}

	}

	public void generateCars() {

		ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.GENERATE_CAR);

		try {

			cars = new GeneratedCar[numOfCars];

				toServer.writeObject(sp);
				sp = (ServerProtocol) toClient.readObject();

				GeneratedCar[] fromServerCars = sp.getCar();
				cars = fromServerCars;

		} catch (IOException e1) {

			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {

			e1.printStackTrace();
		}

		raceUpdate.updateCarRaceDetails(cars, sp.getRaceNum());

	}

	public void changeColorView() {

		int car_index = view.getItemsCar().indexOf(view.getCarIdComBox().getValue());
		int color_index = view.getItemsColor().indexOf(view.getColorComBox().getValue());
		model.changeColor(car_index, color_index);

	}

	public void setSpeedModelView(TextField tf, int n) {
		String msg = null;
		try {
			if (!tf.getText().equals("")) {
				Double speed = Double.parseDouble(tf.getText());
				if (0 <= speed && speed <= MAXSPEED) {
					model.changeSpeed(n, speed);
				} else if (speed > MAXSPEED) {
					msg = "You're driving too fast!!! Speed above " + MAXSPEED + "!!!";
				} else {
					msg = "Only Numbers Great or Equals 0 ";
				}
			}
		} catch (Exception e) {
			msg = "Only Numbers Great or Equals 0 ";
		}
		if (msg != null)
			try {
				errorAlert(msg);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
	}

	public void speedHandlers() {

		for (int i = 0; i < numOfCars; i++) {

			tf[i] = view.getSpeedTxtById(i);

			final int id = i;
			tf[i].setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {

					setSpeedModelView(tf[id], id);
				}

			});
		}

	}

	public void setOwnerStage(Stage stg) {
		this.stg = stg;
	}

	public void errorAlert(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.initOwner(stg);
		alert.setTitle("Error");
		alert.setContentText(msg);
		alert.show();
	}
}