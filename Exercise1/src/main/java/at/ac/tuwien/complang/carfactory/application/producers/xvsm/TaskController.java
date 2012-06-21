package at.ac.tuwien.complang.carfactory.application.producers.xvsm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.application.producers.ITaskController;
import at.ac.tuwien.complang.carfactory.domain.MotorType;
import at.ac.tuwien.complang.carfactory.domain.Task;

public class TaskController implements ITaskController {
	
	//Fields
	private Capi capi;
	private ContainerReference cref;
	private static long next_id = 1;
	
	public TaskController(Capi capi, ContainerReference cref) {
		this.capi = capi;
		this.cref = cref;
	}
	
	@Override
	public void createTask(MotorType type, Color color, int amount) {
		Task task = new Task(next_id);
		next_id++;
		task.setMotortype(type);
		task.setColor(color);
		task.setAmount(amount);
		List<CoordinationData> coordinators = new ArrayList<CoordinationData>();
		coordinators.add(KeyCoordinator.newCoordinationData(""+task.getId()));
		coordinators.add(LabelCoordinator.newCoordinationData(color.toString()));
		coordinators.add(FifoCoordinator.newCoordinationData());
		try {
			capi.write(cref, new Entry(task, coordinators));
			System.out.println("Task " + task.getId() + " written in space sucessfully");
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void disconnect() {
		//nothing todo
	}
}
