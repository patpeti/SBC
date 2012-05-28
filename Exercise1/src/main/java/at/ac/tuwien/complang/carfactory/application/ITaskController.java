package at.ac.tuwien.complang.carfactory.application;

import java.awt.Color;

import at.ac.tuwien.complang.carfactory.domain.MotorType;

public interface ITaskController {
	void createTask(MotorType type, Color color, int amount);
}
