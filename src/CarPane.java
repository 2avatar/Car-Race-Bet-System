

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.PointLight;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.util.Duration;

public class CarPane extends Pane implements CarEvents {
	class ColorEvent implements EventHandler<Event> {
		@Override
		public void handle(Event event) {
			setColor(car.getColor());
		}
	}

	class RadiusEvent implements EventHandler<Event> {
		@Override
		public void handle(Event event) {
			setRadius(car.getRadius());
		}
	}

	class SpeedEvent implements EventHandler<Event> {
		@Override
		public void handle(Event event) {
			setSpeed(car.getSpeed());
		}
	}

	class ThreeDCarModelEvent implements EventHandler<Event> {
		@Override
		public void handle(Event event) {
			setThreeDCarModel(car.getThreeDCarModel());
		}
	}

	final int MOVE = 1;
	final int STOP = 0;
	private double xCoor;
	private double yCoor;
	private Timeline tl; // speed=setRate()
	private Color color;
	private int r;// radius
	private Car car;
	private int threeDCarModel;

	public CarPane() {
		xCoor = 0;
		r = 5;
	}

	public void setCarModel(Car myCar) {
		car = myCar;
		if (car != null) {
			car.addEventHandler(new ColorEvent(), eventType.COLOR);
			car.addEventHandler(new RadiusEvent(), eventType.RADIUS);
			car.addEventHandler(new SpeedEvent(), eventType.SPEED);
			car.addEventHandler(new ThreeDCarModelEvent(), eventType.THREE_D_CAR_MODEL);
		}
	}

	public Car getCarModel() {
		return car;
	}

	public void moveCar(int n) {
		yCoor = getHeight() - (r * 3);
		setMinSize(10 * r, 6 * r);
		if (xCoor > getWidth()) {
			xCoor = -10 * r;
		} else {
			xCoor += n;
		}
		
		if (threeDCarModel == 0) {
			carModel1();
		}

		if (threeDCarModel == 1) {
			carModel2();
		}
		if (threeDCarModel == 2) {
			carModel3();
		}

		// Draw the car
		// Polygon polygon = new Polygon(xCoor, yCoor - r, xCoor, yCoor - 4 * r,
		// xCoor + 2 * r, yCoor - 4 * r,
		// xCoor + 4 * r, yCoor - 6 * r, xCoor + 6 * r, yCoor - 6 * r, xCoor + 8
		// * r, yCoor - 4 * r,
		// xCoor + 10 * r, yCoor - 4 * r, xCoor + 10 * r, yCoor - r);
		// polygon.setFill(color);
		// polygon.setStroke(Color.BLACK);
		// Draw the wheels
		// Circle wheel1 = new Circle(xCoor + r * 3, yCoor - r, r, Color.BLACK);
		// Circle wheel2 = new Circle(xCoor + r * 7, yCoor - r, r, Color.BLACK);
		// getChildren().clear();
		// getChildren().addAll(polygon, wheel1, wheel2);
	}

	private void carModel3() {

		int CAR_SIZE = r * 11;

		int CYLINDER_HEAD_RADIUS = CAR_SIZE / 5;
		int CYLINDER_HEAD_HEIGHT = (CAR_SIZE / 5) * 2;

		int CYLINDER_RADIUS = CAR_SIZE / 5;
		int CYLINDER_HEIGHT = CAR_SIZE;

		int SPHERE_RADIUS = CAR_SIZE / 5;

		int X_CORD = (int) xCoor;
		int Y_CORD = (int) yCoor;
		int Z_CORD = 0;

		PhongMaterial mat = new PhongMaterial(color);

		Cylinder cyl2 = new Cylinder(CYLINDER_HEAD_RADIUS, CYLINDER_HEAD_HEIGHT);

		cyl2.setTranslateX(X_CORD);
		cyl2.setTranslateY((Y_CORD) - (CYLINDER_HEAD_HEIGHT / 2) - (CYLINDER_RADIUS / 2));
		cyl2.setTranslateZ(Z_CORD);

		cyl2.setMaterial(mat);

		Cylinder cyl1 = new Cylinder(CYLINDER_RADIUS, CYLINDER_HEIGHT);

		cyl1.setTranslateX(X_CORD);
		cyl1.setTranslateY(Y_CORD);
		cyl1.setTranslateZ(Z_CORD);

		cyl1.setMaterial(mat);
		cyl1.setRotate(90);

		Sphere sp1 = new Sphere(SPHERE_RADIUS);

		sp1.setTranslateX(X_CORD - (CYLINDER_HEIGHT / 4));
		sp1.setTranslateY(Y_CORD + (CYLINDER_RADIUS / 2));
		sp1.setTranslateZ(Z_CORD);

		sp1.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		Sphere sp2 = new Sphere(SPHERE_RADIUS);

		sp2.setTranslateX(X_CORD + (CYLINDER_HEIGHT / 4));
		sp2.setTranslateY(Y_CORD + (CYLINDER_RADIUS / 2));
		sp2.setTranslateZ(Z_CORD);

		sp2.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		PointLight Light1 = new PointLight();

		Light1.setColor(Color.WHITE);
		Light1.setTranslateX(0);
		Light1.setTranslateY(0);
		Light1.setTranslateZ(-200);

		PointLight Light2 = new PointLight();

		Light2.setColor(Color.WHITE);
		Light2.setTranslateX(1000);
		Light2.setTranslateY(800);
		Light2.setTranslateZ(-200);

		PointLight Light3 = new PointLight();

		Light3.setColor(Color.WHITE);
		Light3.setTranslateX(0);
		Light3.setTranslateY(800);
		Light3.setTranslateZ(-200);
			
		getChildren().clear();
		getChildren().addAll(cyl2, cyl1, sp1, sp2, Light1, Light2, Light3);
		
	}

	private void carModel2() {

		int CAR_SIZE = r * 11;

		int BOX_WIDTH = (CAR_SIZE / 5) * 2;
		int BOX_HEIGHT = (CAR_SIZE / 5) * 2;
		int BOX_DEPTH = (CAR_SIZE / 5) * 3;

		int CYLINDER_RADIUS = CAR_SIZE / 5;
		int CYLINDER_HEIGHT = CAR_SIZE;

		int SPHERE_RADIUS = CAR_SIZE / 5;

		int X_CORD = (int) xCoor;
		int Y_CORD = (int) yCoor;
		int Z_CORD = 0;

		PhongMaterial mat = new PhongMaterial(color);

		Box box1 = new Box(BOX_WIDTH, BOX_HEIGHT, BOX_DEPTH);

		box1.setTranslateX(X_CORD);
		box1.setTranslateY((Y_CORD) - (BOX_HEIGHT / 2) - (CYLINDER_RADIUS / 2));
		box1.setTranslateZ(Z_CORD);

		box1.setMaterial(mat);

		Cylinder cyl1 = new Cylinder(CYLINDER_RADIUS, CYLINDER_HEIGHT);

		cyl1.setTranslateX(X_CORD);
		cyl1.setTranslateY(Y_CORD);
		cyl1.setTranslateZ(Z_CORD);

		cyl1.setMaterial(mat);
		cyl1.setRotate(90);

		Sphere sp1 = new Sphere(SPHERE_RADIUS);

		sp1.setTranslateX(X_CORD - (CYLINDER_HEIGHT / 4));
		sp1.setTranslateY(Y_CORD + (CYLINDER_RADIUS / 2));
		sp1.setTranslateZ(Z_CORD);

		sp1.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		Sphere sp2 = new Sphere(SPHERE_RADIUS);

		sp2.setTranslateX(X_CORD + (CYLINDER_HEIGHT / 4));
		sp2.setTranslateY(Y_CORD + (CYLINDER_RADIUS / 2));
		sp2.setTranslateZ(Z_CORD);

		sp2.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		PointLight Light1 = new PointLight();

		Light1.setColor(Color.WHITE);
		Light1.setTranslateX(0);
		Light1.setTranslateY(0);
		Light1.setTranslateZ(-200);

		PointLight Light2 = new PointLight();

		Light2.setColor(Color.WHITE);
		Light2.setTranslateX(1000);
		Light2.setTranslateY(800);
		Light2.setTranslateZ(-200);

		PointLight Light3 = new PointLight();

		Light3.setColor(Color.WHITE);
		Light3.setTranslateX(0);
		Light3.setTranslateY(800);
		Light3.setTranslateZ(-200);
		
		getChildren().clear();
		getChildren().addAll(box1, cyl1, sp1, sp2, Light1, Light2, Light3);

	}

	private void carModel1() {

		int CAR_SIZE = r * 11;

		int BOX_WIDTH = CAR_SIZE;
		int BOX_HEIGHT = CAR_SIZE / 3;
		int BOX_DEPTH = CAR_SIZE / 2;

		int CYLINDER_RADIUS = CAR_SIZE / 4;
		int CYLINDER_HEIGHT = CAR_SIZE / 4;

		int SPHERE_RADIUS = CAR_SIZE / 6;

		int X_CORD = (int) xCoor;
		int Y_CORD = (int) yCoor;
		int Z_CORD = 0;

		PhongMaterial mat = new PhongMaterial(color);

		Box box1 = new Box(BOX_WIDTH, BOX_HEIGHT, BOX_DEPTH);

		box1.setTranslateX(X_CORD);
		box1.setTranslateY(Y_CORD);
		box1.setTranslateZ(Z_CORD);

		box1.setMaterial(mat);

		Cylinder cyl1 = new Cylinder(CYLINDER_RADIUS, CYLINDER_HEIGHT);

		cyl1.setTranslateX(X_CORD);
		cyl1.setTranslateY((Y_CORD) - (BOX_HEIGHT / 2) - (CYLINDER_HEIGHT / 2));
		cyl1.setTranslateZ(Z_CORD);

		cyl1.setMaterial(mat);

		Sphere sp1 = new Sphere(SPHERE_RADIUS);

		sp1.setTranslateX(X_CORD - (BOX_WIDTH / 4));
		sp1.setTranslateY(Y_CORD + (BOX_HEIGHT / 2) - (SPHERE_RADIUS / 3));
		sp1.setTranslateZ(Z_CORD - 30);

		sp1.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		Sphere sp2 = new Sphere(SPHERE_RADIUS);

		sp2.setTranslateX(X_CORD + (BOX_WIDTH / 4));
		sp2.setTranslateY(Y_CORD + (BOX_HEIGHT / 2) - (SPHERE_RADIUS / 3));
		sp2.setTranslateZ(Z_CORD - 30);

		sp2.setMaterial(new PhongMaterial(Color.CHOCOLATE));

		PointLight Light1 = new PointLight();

		Light1.setColor(Color.WHITE);
		Light1.setTranslateX(0);
		Light1.setTranslateY(0);
		Light1.setTranslateZ(-200);

		PointLight Light2 = new PointLight();

		Light2.setColor(Color.WHITE);
		Light2.setTranslateX(1000);
		Light2.setTranslateY(800);
		Light2.setTranslateZ(-200);

		PointLight Light3 = new PointLight();

		Light3.setColor(Color.WHITE);
		Light3.setTranslateX(0);
		Light3.setTranslateY(800);
		Light3.setTranslateZ(-200);

		getChildren().clear();
		getChildren().addAll(box1, cyl1, sp1, sp2, Light1, Light2, Light3);

	}

	public void createTimeline(int time) {
		EventHandler<ActionEvent> eventHandler = e -> {
			moveCar(MOVE); // move car pane according to limits
		};
		tl = new Timeline();
		tl.setCycleCount(Timeline.INDEFINITE);
		KeyFrame kf = new KeyFrame(Duration.millis(time), eventHandler);
		tl.getKeyFrames().add(kf);
		tl.play();
	}

	public Timeline getTimeline() {
		return tl;
	}

	public void setColor(Color color) {
		this.color = color;
		if (car.getSpeed() == STOP)
			moveCar(STOP);
	}

	public void setRadius(int r) {
		this.r = r;
		if (car.getSpeed() == STOP)
			moveCar(STOP);
	}

	public void setThreeDCarModel(int threeDCarModel) {
		this.threeDCarModel = threeDCarModel;
		if (car.getSpeed() == STOP)
			moveCar(STOP);
	}

	public void setSpeed(double speed) {
		if (speed == STOP) {
			tl.stop();
		} else {
			tl.setRate(speed);
			tl.play();
		}
	}

	public double getX() {
		return xCoor;
	}

	public double getY() {
		return yCoor;
	}
}
