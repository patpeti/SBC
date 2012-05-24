package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.application.xvsm.AbstractFactory;

public class Wheel implements Serializable, ICarPart {
	
	//Static fields
	private static final long serialVersionUID = 1L;
	private static final CarPartType type = CarPartType.WHEEL;
	
	//Fields
	private long id;
	private long pid;
	private boolean isDefect;
	
	public Wheel(long pid) {
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
		return new Object[] {id, this.getType(), pid, isDefect ? "Defect" : ""};
	}
	
	public CarPartType getType() {
		return Wheel.type;
	}
}
