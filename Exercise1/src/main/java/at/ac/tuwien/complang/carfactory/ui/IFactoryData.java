package at.ac.tuwien.complang.carfactory.ui;

import at.ac.tuwien.complang.carfactory.domain.Car;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Task;

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
	//DefectCars
	boolean addDefectCar(Car car);
	boolean removeDefectCar(Car car);
	boolean updateDefectCar(Car car);
	//Part Methods
	boolean addPart(ICarPart carPart);
	boolean removePart(ICarPart carPart);
	/**
	 * Updates a part object in the data model.
	 * @param carPart The part which should be updated.
	 * @return Should return true if the part could be
	 * updated and false if the part was not found
	 * in the data model. 
	 */
	boolean updatePart(ICarPart carPart);
	//Task Methods
	boolean addTask(Task task);
	boolean removeTask(Task task);
	boolean updateTask(Task task);
}
