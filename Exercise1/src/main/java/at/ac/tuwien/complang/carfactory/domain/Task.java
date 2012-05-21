package at.ac.tuwien.complang.carfactory.domain;

import java.awt.Color;

public class Task {

	//Fields
	private MotorType motortype;
	private int amount;
	private Color color;
	
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
}
