package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.application.enums.SpaceChangeType;
import at.ac.tuwien.complang.carfactory.application.jms.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

/**
 * Implemented by the GUI and used to notify the GUI of
 * new car parts which should be shown.
 *  
 * @author Sebastian Geiger
 */
public interface IQueueObserver {
	void onQueueChange(ICarPart carpart, QueueChangeType changeType);
	//Car Methods
	void addCar(Car car);
	void removeCar(Car car);
	void updateCar(Car car);
	//Part Methods
	void addPart(ICarPart carPart, SpaceChangeType type);
	void removePart(ICarPart carPart);
	void updatePart(ICarPart carPart);
}
