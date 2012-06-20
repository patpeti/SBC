package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.xvsm.AbstractFactory;

public class Motor implements Serializable, ICarPart {

	//Static fields
	private static final long serialVersionUID = 1L;
	private static final CarPartType type = CarPartType.MOTOR;
	
	//Fields
	private long id;
	private long pid; //ID of the producer
	private MotorType power; //the power of the motor
	private boolean isDefect;
	
	public Motor(long pid) {
		this.id = AbstractFactory.carPartId;
		AbstractFactory.carPartId++;
		this.pid = pid;
	}

	public long getId() {
		return id;
	};
	
	public long getPid() {
		return pid;
	}
	
	public Object[] getObjectData() {
		String note = isDefect ? "Defect, " : "";
		note += power.toString();
		return new Object[] {id, this.getType(), pid, note};
	}
	
	public CarPartType getType() {
		return Motor.type;
}

	public MotorType getPower() {
		return power;
	}

	public void setPower(MotorType power) {
		this.power = power;
	}

	@Override
	public boolean isDefect() {
		return isDefect;
	}

	public void setDefect(boolean isDefect) {
		this.isDefect = isDefect;
	}
	
	@Override
	public String toString() {
		return this.id + "";
	}
}
