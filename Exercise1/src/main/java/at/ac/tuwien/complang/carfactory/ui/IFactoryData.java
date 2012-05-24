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
	boolean addCar(Car car);
	boolean removeCar(Car car);
	boolean updateCar(Car car);
	//Part Methods
	boolean addPart(ICarPart carPart);
	boolean removePart(ICarPart carPart);
	boolean updatePart(ICarPart carPart);
}
