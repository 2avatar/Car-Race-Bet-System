

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * A Helpful class to update clients on the race. if its on, how many car bets left, the cars details and more.
 * @author Eviatar Admon
 *
 */

public class ViewRaceUpdate {

	private TextArea raceDetailsTextField;
	
	public ViewRaceUpdate() {

		Stage raceDetailsStage;
		Scene raceDetailsScene;
		
		raceDetailsTextField = new TextArea();
		raceDetailsStage = new Stage();
		raceDetailsScene = new Scene(raceDetailsTextField, 600, 400);
		
		
		raceDetailsTextField.setEditable(false);
		raceDetailsTextField.setWrapText(true);

		raceDetailsStage.setScene(raceDetailsScene);
		raceDetailsStage.show();
		raceDetailsStage.setTitle("Race update");

	}

	public void updateCarRaceDetails(GeneratedCar[] car, int raceId) {

		raceUpdate("race: "+raceId+ " is on:");

		for (int i = 0; i < car.length; i++)
			raceUpdate("Car number: "+(i+1)+" "+car[i].toString());

	}

	public void raceUpdate(String update) {

		raceDetailsTextField.appendText(update+"\n");

	}

}
