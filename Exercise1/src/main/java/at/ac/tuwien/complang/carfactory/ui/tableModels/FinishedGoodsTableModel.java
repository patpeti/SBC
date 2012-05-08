package at.ac.tuwien.complang.carfactory.ui.tableModels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class FinishedGoodsTableModel extends AbstractTableModel {
	private static final String[] SPACE_CONTENT_COLUMNS = { "CarID", "PID",
			"BodyID", "BodyPID", "BodyColor", "PainterPID", "MotorID", "MotorPID", "WHEEL1ID", "WHEEL1PID", "WHEEL2ID", "WHEEL2PID", "WHEEL3ID", "WHEEL3PID", "WHEEL4ID", "WHEEL4PID" };
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
		return data.get(row)[column];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public void addRow(Object[] dates) {
		data.add(dates);
		int row = data.indexOf(dates);
		for(int column = 0; column < dates.length; column++) {
			fireTableCellUpdated(row, column);
		}
	}
}