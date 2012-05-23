package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.xvsm.AbstractFactory;

public class Motor implements Serializable, ICarPart {
	private static final long serialVersionUID = 1L;
	private static final CarPartType type = CarPartType.MOTOR;

	//Static fields
//	private static long next_id = 0;
	
	//Fields
	private long id;
	private long pid; //ID of the producer
	private MotorType power; //the power of the motor 
	
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
		return new Object[] {id, this.getType(), pid, ""};
	}
	
	public CarPartType getType() {
		return this.type;
}

	public MotorType getPower() {
		return power;
	}

	public void setPower(MotorType power) {
		this.power = power;
	}
}
