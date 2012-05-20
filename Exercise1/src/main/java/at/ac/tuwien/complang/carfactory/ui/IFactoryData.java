package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

/**
 * Implemented by the GUI and used to notify the GUI of
 * new car parts which should be shown.
 *  
 * @author Sebastian Geiger
 */
public interface IFactoryData {
	//Car Methods
	void addCar(Car car);
	void removeCar(Car car);
	void updateCar(Car car);
	void addOrUpdateCar(Car car);
	//Part Methods
	void addPart(ICarPart carPart);
	void removePart(ICarPart carPart);
	void updatePart(ICarPart carPart);
}
