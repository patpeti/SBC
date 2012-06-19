package at.ac.tuwien.complang.carfactory.ui.tableModels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class FinishedGoodsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 2551634081569657375L;
	private static final String[] SPACE_CONTENT_COLUMNS = {
		"CarID",
		"AssemblerID",
		"Completeness T",
		"Defect T",
		"SupervisorID",
		"BodyID","BodyPID", //Body ID + PID
		"BodyColor", "PainterID", //Color + Painter
		"MotorID", "MotorPID", //Motor ID + PID
		"WHEEL PID", "WHEEL1ID", "WHEEL2ID", "WHEEL3ID", "WHEEL4ID" // Wheel PID + ID 1-4
	};
	private List<Object[]> data = new ArrayList<Object[]>();

	public FinishedGoodsTableModel() { }
	
	public FinishedGoodsTableModel(Object[][] data) {
		if(data == null) return;
		for (Object[] dates : data) {
			this.data.add(dates);
		}
	}

	public int getColumnCount() {
		return SPACE_CONTENT_COLUMNS.length;
	}

	public int getRowCount() {
		if (data != null)
			return data.size();
		else
			return 0;
	}

	public String getColumnName(int col) {
		return SPACE_CONTENT_COLUMNS[col];
	}

	public Object getValueAt(int row, int column) {
		try {
			return data.get(row)[column];
		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	public Class<? extends Object> getColumnClass(int c) {
		Object object = getValueAt(0, c);
		if(object == null) {
			return Object.class;
		} else {
			return getValueAt(0, c).getClass();
		}
	}

	public synchronized boolean addRow(Object[] dates) {
		data.add(dates);
		int row = data.indexOf(dates);
		if(row != -1) {
			fireTableRowsInserted(row, row);
			return true;
		} else {
			return false;
		}
	}

	public synchronized boolean removeRow(Object[] objectData) {
		for(Object[] object : data) {
			if(object[0].equals(objectData[0])) {
				int index = data.indexOf(object);
				data.remove(object);
				fireTableRowsDeleted(index, index);
				return true;
			}
		}
		System.out.println("Could not remove object from finished products");
		return false;
	}
	
	/**
	 * Updates the row which stores the respective data
	 * @param objectData The Data show store in the TableDataModel.
	 * @return Returns true if the row was successfully updated, false otherwise.
	 */
	public synchronized boolean updateRow(Object[] objectData) {
		int index = -1;
		for(Object[] object : data) {
			if(object[0].equals(objectData[0])) {
				index = data.indexOf(object);
				break;
			}
		}
		if(index != -1) {
			data.set(index, objectData);
			fireTableRowsUpdated(index, index);
			return true;
		} else {
			return false;
		}
	}
}