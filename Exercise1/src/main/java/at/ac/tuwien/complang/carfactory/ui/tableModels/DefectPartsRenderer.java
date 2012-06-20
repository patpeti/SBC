package at.ac.tuwien.complang.carfactory.ui.tableModels;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

/**
 * A renderer that renders the tabel cell for an ICarPart either
 * light green or light red, depending on whether it is defect.
 * String alignment is RIGHT.
 *  
 * @author Sebastian Geiger
 */
public class DefectPartsRenderer extends DefaultTableCellRenderer {

	//Static Fields
	private static final long serialVersionUID = -8444451690175495986L;

	private boolean colored;

	public DefectPartsRenderer(boolean colored) {
		this.colored = colored;
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		//component is by default an instance of JLabel
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(value == null || !(value instanceof ICarPart)) return component;
		ICarPart part = (ICarPart) value;
		if(colored) {
			if(part.isDefect()) {
				component.setBackground(new java.awt.Color(255, 160, 160));
			} else {
				component.setBackground(new java.awt.Color(160, 255, 160));
			}
		}
		((JLabel) component).setHorizontalAlignment(SwingConstants.RIGHT);
		return component;
	}
}
