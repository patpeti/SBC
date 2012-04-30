package at.ac.tuwien.complang.carfactory.domain;

import java.io.Serializable;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;

public interface ICarPart extends Serializable{
	long getId();
	long getPid();
	Object[] getObjectData();
	CarPartType getType();
	
}
