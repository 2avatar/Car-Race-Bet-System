

import java.io.Serializable;

/** 
 * 
 * A Helpful class for passing objects between client and server by protocols with enums
 * @author Eviatar Admon
 */


public class ServerProtocol implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum serverProtocol {

		BOSS_CLIENT {
			public String toString() {
				return "Boss client";
			}
		},
		NEW_GAMBLER_CLIENT {
			public String toString() {
				return "Gambler client";
			}
		},
		EXISITING_GAMBLER_CLIENT {
			public String toString() {
				return "Gambler client";
			}
		},
		RACE_CLIENT {
			public String toString() {
				return "Race client";
			}
		},
		GENERATE_CAR {
			public String toString() {
				return "Generating car..";
			}
		},
		GENERATE_CAR_SPEED {
			public String toString() {
				return "Generating car speed..";
			}
		},
		EXECUTE_QUERY {
			public String toString() {
				return "Executing query: ";
			}
		},
		LOG_EVENT {
			public String toString() {
				return "Log event: ";
			}
		},
		START_RACE {
			public String toString() {
				return "Race begins";
			}
		},
		RACE_OVER {
			public String toString() {
				return "Race over";
			}
		},
		GAMBLER_BET {
			public String toString() {
				return "Gambler bet";
			}
		},
		GAMBLER_PRIZE {
			public String toString() {
				return "Gambler prize:";
			}
		}, 
		CLOSE_CLIENT{
			public String toString(){
				return "Closing client:";
			}
		},
		GAMBLER_UPDATE_RACE_LIST, CLOSE_SERVER,PERMISSION, UPDATE_VIEW_RACE, UPDATE_HISTORY, GET_CAR_RACE_DETAILS
	}

	private serverProtocol protocol;
	private int raceNum;
	private int carId;
	private int radius;
	private int colorIndex;
	private double speed;
	private int timeInterval;
	private int gamblerId;
	private String str;
	private String userName;
	private String password;
	private double virtualMoney;
	private GamblerBet gamblerBet;
	private String[] raceList;
	private int threeDCarModel;
	private GeneratedCar[] car;
	private boolean permissionApproved;

	public ServerProtocol(serverProtocol protocol) {
		this.protocol = protocol;
	}

	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public serverProtocol getProtocol() {
		return protocol;
	}

	public int getCarId() {
		return carId;
	}

	public void setCarId(int carId) {
		this.carId = carId;
	}

	public int getColorIndex() {
		return colorIndex;
	}

	public void setColorIndex(int colorIndex) {
		this.colorIndex = colorIndex;
	}

	public int getRaceNum() {
		return raceNum;
	}

	public void setRaceNum(int raceNum) {
		this.raceNum = raceNum;
	}

	public double getVirtualMoney() {
		return virtualMoney;
	}

	public void setVirtualMoney(double virtualMoney) {
		this.virtualMoney = virtualMoney;
	}

	public int getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(int timeInterval) {
		this.timeInterval = timeInterval;
	}

	public GamblerBet getGamblerBet() {
		return gamblerBet;
	}

	public void setGamblerBet(GamblerBet gamblerBet) {
		this.gamblerBet = gamblerBet;
	}

	public String[] getRaceList() {
		return raceList;
	}

	public void setRaceList(String[] raceList) {
		this.raceList = raceList;
	}

	public int getThreeDCarModel() {
		return threeDCarModel;
	}

	public void setThreeDCarModel(int threeDCarModel) {
		this.threeDCarModel = threeDCarModel;
	}

	public GeneratedCar[] getCar() {
		return car;
	}

	public void setCar(GeneratedCar[] car) {
		this.car = car;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getGamblerId() {
		return gamblerId;
	}

	public void setGamblerId(int gamblerId) {
		this.gamblerId = gamblerId;
	}

	public boolean isPermissionApproved() {
		return permissionApproved;
	}

	public void setPermissionApproved(boolean permissionApproved) {
		this.permissionApproved = permissionApproved;
	}

}
