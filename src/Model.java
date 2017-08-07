

import java.io.ObjectOutputStream;

import javafx.scene.paint.Color;

public class Model {
	
	private int raceCounter;
	private Car[] cars;
	private CarLog log;
	private Color colors[] = { Color.RED, Color.AQUA, Color.BLUE, Color.GREEN, Color.YELLOW, Color.ORANGE, Color.PINK,
			Color.VIOLET, Color.WHITE, Color.DARKCYAN };

	
	public Model(int raceCounter, int numOfCars, ObjectOutputStream toServer) {
		
		this.raceCounter = raceCounter;
		cars = new Car[numOfCars];
		log = new CarLog(raceCounter, toServer);
		for (int i=0; i<numOfCars; i++)
			cars[i] = new Car(i, raceCounter, log);
		
	}

	public void changeColor(int id, int colorIndex) {
		getCarById(id).setColor(colors[colorIndex]);
	}

	public void changeRadius(int id, int radius) {
		getCarById(id).setRadius(radius);
	}

	public void changeSpeed(int id, double speed) {
		getCarById(id).setSpeed(speed);
	}
	public void changeThreeDCarModel(int id, int threeDCarModel){
		getCarById(id).setThreeDCarModel(threeDCarModel);
	}

	public Car getCarById(int id) {
		return cars[id];
	}

	public int getRaceCounter() {
		return raceCounter;
	}

}
