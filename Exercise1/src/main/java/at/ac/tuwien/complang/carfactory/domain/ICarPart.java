package at.ac.tuwien.complang.carfactory.domain;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;

public interface ICarPart {
	long getId();
	long getPid();
	Object[] getObjectData();
	CarPartType getType();
	
}
