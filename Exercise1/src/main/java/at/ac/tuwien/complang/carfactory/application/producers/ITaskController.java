package at.ac.tuwien.complang.carfactory.application.producers;

import java.awt.Color;

import at.ac.tuwien.complang.carfactory.domain.MotorType;

public interface ITaskController {
	void createTask(MotorType type, Color color, int amount);
	void disconnect();
}
