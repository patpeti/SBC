package at.ac.tuwien.complang.carfactory.application.jms;

import java.awt.Color;

import at.ac.tuwien.complang.carfactory.application.ITaskController;
import at.ac.tuwien.complang.carfactory.domain.MotorType;
import at.ac.tuwien.complang.carfactory.domain.Task;

public class JmsTaskController implements ITaskController {
	
	//Fields
	private static long next_id;
	
	public JmsTaskController() {

	}
	
	@Override
	public void createTask(MotorType type, Color color, int amount) {
		Task task = new Task(next_id);
		next_id++;
		task.setMotortype(type);
		task.setColor(color);
		task.setAmount(amount);
		System.out.println("Not implemented");
	}
}
