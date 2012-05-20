package at.ac.tuwien.complang.carfactory.ui.xvsm;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.IFactoryData;

public class SpaceListener implements NotificationListener {
	IFactoryData data;

	public SpaceListener(IFactoryData data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void entryOperationFinished(
		Notification source,
		Operation operation,
		List<? extends Serializable> entries)
	{
		System.out.println("[XVSM Notification: " + operation.name() + "]");
		if(operation.name().equals("WRITE")) {
			for(Entry entry : (List<Entry>) entries) {
				if (entry.getValue() instanceof Car) {
					System.out.println("[GUI_Notification] New Car written");
					Car car = (Car) entry.getValue();
					data.addOrUpdateCar(car);
				} else if (entry.getValue() instanceof ICarPart) { //its not a car but still a carpart
					ICarPart part = (ICarPart) entry.getValue();
					data.addPart(part);
				}
			}
		} else if(operation.name().equals("TAKE")) {
			for(ICarPart part: (List<ICarPart>) entries) {
				if(!(part instanceof Car)) {
					data.removePart(part);
				}
			}
		}
	}
}
