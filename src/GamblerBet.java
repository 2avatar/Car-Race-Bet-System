

import java.io.Serializable;

/**
 * A helpful class to pass gambler bets data between client and the server
 * @author Eviatar Admon
 */

public class GamblerBet implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int raceId;
	private int carId;
	private int betAmount;
	private int gamblerId;

	public GamblerBet (int raceId, int carId, int betAmount, int gamblerId){
		
		this.carId = carId;
		this.raceId = raceId;
		this.betAmount = betAmount;
		this.gamblerId = gamblerId;
		
	}
	
	public int getRaceId() {
		return raceId;
	}

	public int getBetAmount() {
		return betAmount;
	}

	public int getGamblerId() {
		return gamblerId;
	}

	public int getCarId() {
		return carId;
	}
	
	
	
}
