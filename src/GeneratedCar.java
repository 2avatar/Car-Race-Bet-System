

import java.io.Serializable;

/**
 * A helpful class to pass generated cars data between client and the server
 * @author Eviatar Admon
 */


public class GeneratedCar implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String carShape;
	private int shapeIndex;
	private String carType;
	private String carSize;
	private int sizeNumber;
	private String carColor;
	private int colorIndex;
	
	public GeneratedCar(String shape, int shapeIndex, String type, String size, int sizeNumber, String color, int Colorindex){
		
		this.carShape = shape;
		this.shapeIndex = shapeIndex;
		this.carType = type;
		this.carSize = size;
		this.sizeNumber = sizeNumber;
		this.carColor = color;
		this.colorIndex = Colorindex;
		
	}

	public String getCarShape() {
		return carShape;
	}

	public int getShapeIndex() {
		return shapeIndex;
	}

	public String getCarType() {
		return carType;
	}

	public String getCarSize() {
		return carSize;
	}

	public int getSizeNumber() {
		return sizeNumber;
	}

	public String getCarColor() {
		return carColor;
	}

	public int getColorIndex() {
		return colorIndex;
	}

	@Override
	public String toString() {
		
		return "shape: "+carShape+", type: "+carType+", color: "+carColor+", size: "+carSize;
	}
	
	
	
}
