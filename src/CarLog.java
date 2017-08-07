

import java.io.IOException;
import java.io.ObjectOutputStream;

public class CarLog  {

	private ObjectOutputStream toServer;
	private int raceNum;

	public CarLog(int raceNum, ObjectOutputStream toServer) {

		this.toServer = toServer;
		this.raceNum = raceNum;
		
	}

	public void printMsg(String str) {
		
		ServerProtocol sp = new ServerProtocol(ServerProtocol.serverProtocol.LOG_EVENT);
		sp.setRaceNum(raceNum);
		sp.setStr(str);
		
		try {
			toServer.writeObject(sp);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
	}
}