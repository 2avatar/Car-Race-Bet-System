

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.event.Event;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;



public class Car implements CarEvents {
	private int id;
	private int model_id;
	private CarLog log;
	private double speed;
	private Color color;
	private int wheelRadius;
	private int threeDCarModel;
	private Map<eventType, ArrayList<EventHandler<Event>>> carHashMap;

	public Car(int id, int model_id, CarLog log) {
		
		this.id = id;
		this.model_id = model_id;
		this.log = log;
		this.speed = 0;
		this.color = Color.RED;
		this.wheelRadius = 5;
		carHashMap = new HashMap<eventType, ArrayList<EventHandler<Event>>>();
		for (eventType et : eventType.values())
			carHashMap.put(et, new ArrayList<EventHandler<Event>>());
	}

	public int getId() {
		return id;
	}

	public int getModelId() {
		return model_id;
	}

	public Color getColor() {
		return color;
	}

	public int getRadius() {
		return wheelRadius;
	}

	public double getSpeed() {
		return speed;
	}
	
	public int getThreeDCarModel(){
		return threeDCarModel;
	}
	
	public void setThreeDCarModel(int threeDCarModel){
		this.threeDCarModel = threeDCarModel;
		processEvent(eventType.THREE_D_CAR_MODEL, new ActionEvent());
	}

	public void setColor(Color color) {
		this.color = color;
		processEvent(eventType.COLOR, new ActionEvent());
	}

	public void setSpeed(double speed) {
		this.speed = speed;
		processEvent(eventType.SPEED, new ActionEvent());
	}

	public void setRadius(int wheelRadius) {
		this.wheelRadius = wheelRadius;
		processEvent(eventType.RADIUS, new ActionEvent());
	}

	public synchronized void addEventHandler(EventHandler<Event> l, eventType et) {
		ArrayList<EventHandler<Event>> al;
		al = carHashMap.get(et);
		if (al == null)
			al = new ArrayList<EventHandler<Event>>();
		al.add(l);
		carHashMap.put(et, al);
	}

	public synchronized void removeEventHandler(EventHandler<Event> l, eventType et) {
		ArrayList<EventHandler<Event>> al;
		al = carHashMap.get(et);
		if (al != null && al.contains(l))
			al.remove(l);
		carHashMap.put(et, al);
	}

	private void processEvent(eventType et, Event e) {
		String msg;
		ArrayList<EventHandler<Event>> al;
		synchronized (this) {
			al = carHashMap.get(et);
			if (al == null)
				return;
		}
		msg = "CarRaceView: " + (getModelId() + 1) + " | car number: " + (getId() + 1) + " | actionCommand: "
				+ et.toString();
		
		if(et == eventType.SPEED)
			msg = msg+" : "+getSpeed();
		else if (et == eventType.RADIUS)
			msg = msg+" : "+getRadius();
		else if (et == eventType.COLOR)
			msg = msg+" : "+getColor();
		
		log.printMsg(msg);
		for (int i = 0; i < al.size(); i++) {
			EventHandler<Event> handler = (EventHandler<Event>) al.get(i);
			handler.handle(e);
		}
	}
}