package at.ac.tuwien.complang.carfactory.ui.tableModels;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class SpaceDataTableModel extends AbstractTableModel {
	private static final String[] SPACE_CONTENT_COLUMNS = { "ID", "PartName",
			"PID", "Note" };
	private List<Object[]> data = new ArrayList<Object[]>();

	public SpaceDataTableModel() { }
	
	public SpaceDataTableModel(Object[][] data) {
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

	public synchronized Object getValueAt(int row, int column) {
		return data.get(row)[column];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public synchronized void addRow(Object[] dates) {
		data.add(dates);
		int row = data.indexOf(dates);
		for(int column = 0; column < dates.length; column++) {
			fireTableCellUpdated(row, column);
		}
		fireTableRowsInserted(row, row);
	}

	public synchronized void deleteColumn(Object[] objectData) {
		for(Object[] object : data) {
			if(object[0] == objectData[0]); //compare the unique global space id at position 0 of the data array
			int index = data.indexOf(object);
			data.remove(object);
			fireTableRowsDeleted(index, index);
			break;
		}
	}
}
