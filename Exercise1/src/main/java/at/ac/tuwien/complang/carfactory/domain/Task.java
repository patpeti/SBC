package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;
import java.io.Serializable;

public class Task implements Serializable {

	//Static Fields
	private static final long serialVersionUID = -818265961255247515L;

	//Fields
	private MotorType motortype;
	private int amount;
	/**
	 * The completed members
	 */
	private int amountCompleted; //written by supervisor
	private int carAmount; //written by assembler, indicates how many cars have been assembled with the required motor type.
	private int paintAmount; //written by painter, indicates how many cars/bodies have been painted.
	private Color color;
	private long id;
	
	public Task(long id) {
		this.id = id;
	}
	
	public MotorType getMotortype() {
		return motortype;
	}
	
	public void setMotortype(MotorType motortype) {
		this.motortype = motortype;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}

	public long getId() {
		return id;
	}
	
	/**
	 * @return Returns true if the amount of painted and produced parts
	 * is equal or greater than the required amount. 
	 */
	public boolean isFinished() {
		if(amount >= paintAmount && amount >= carAmount) {
			return true;
		} else {
			return false;
		}
	}
	
	public Object[] getObjectData() {
		String colorString;
		if(color == null) {
			colorString = "NONE";
		} else if(color.equals(Color.RED)) {
			colorString = "RED";
		} else if(color.equals(Color.BLUE)) {
			colorString = "BLUE";
		} else if(color.equals(Color.GREEN)) {
			colorString = "GREEN";
		} else {
			colorString = String.format("(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
		}
		return new Object[] {
				id,
				motortype.getType(),
				colorString,
				amount,
				amountCompleted
		};
	}

	/**
	 * @return Returns the amount of painted object. If this is
	 * less than the getAmount value, the painter needs to paint
	 * more cars/bodies.
	 */
	public int getCarAmount() {
		return carAmount;
	}

	/**
	 * Increase the amount of assembled cars by amount.
	 * @param amount The amount of cars that have been assembled.
	 */
	public void increaseCarAmount(int amount) {
		this.carAmount += amount;
	}

	/**
	 * @return Returns the amount of painted objects. If this is
	 * less than the getAmount() value, the painter needs to paint
	 * more cars/bodies.
	 */
	public int getPaintAmount() {
		return paintAmount;
	}

	/**
	 * Increase the amount of painted cars by amount.
	 * @param amount The amount of cars that have been painted.
	 */
	public void increasePaintAmount(int amount) {
		this.paintAmount += amount;
	}
}
