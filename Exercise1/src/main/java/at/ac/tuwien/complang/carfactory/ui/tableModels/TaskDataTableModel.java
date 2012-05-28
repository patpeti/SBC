package at.ac.tuwien.complang.carfactory.ui.tableModels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class TaskDataTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -119642846884075704L;
	private static final String[] TASK_CONTENT_COLUMNS = { "TaskID", "MotorType",
			"Color", "Amount", "AmountCompleted" };
	private List<Object[]> data = new ArrayList<Object[]>();

	public TaskDataTableModel() { }
	
	public TaskDataTableModel(Object[][] data) {
		if(data == null) return;
		for (Object[] dates : data) {
			this.data.add(dates);
		}
	}

	public int getColumnCount() {
		return TASK_CONTENT_COLUMNS.length;
	}

	public int getRowCount() {
		if (data != null)
			return data.size();
		else
			return 0;
	}

	public String getColumnName(int col) {
		return TASK_CONTENT_COLUMNS[col];
	}

	public synchronized Object getValueAt(int row, int column) {
		if(data.size() > row && data.get(row).length > column) {
			return data.get(row)[column];
		} else {
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

	public synchronized boolean deleteRow(Object[] objectData) {
		for(Object[] object : data) {
			if(object[0].equals(objectData[0])) { //compare the unique global space id at position 0 of the data array
				int index = data.indexOf(object);
				data.remove(object);
				fireTableRowsDeleted(index, index);
				return true;
			}
		}
		System.out.println("Could not remove part from table model. ID: " + objectData[0]);
		return false;
	}
	
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
