package at.ac.tuwien.complang.carfactory.ui.panels;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import javax.swing.JPanel;

public class StatusLight extends JPanel {

	private static final long serialVersionUID = 1024130962195046874L;
	//Fields
	private Color color = new Color(200,200,200);
	private final Color borderColor = new Color(200, 200, 200);

	public StatusLight() {
		this.setAlignmentX(RIGHT_ALIGNMENT);
	}
	
	@Override
	public void paint(Graphics g) {
		Shape circle = new Ellipse2D.Float(0, 0, 24.0f, 24.0f);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(this.color);
		g2d.fill(circle);
		g2d.setColor(this.borderColor);
		g2d.draw(circle);
	}
	
	public void setActive() {
		this.color = new Color(128, 255, 128); //lightgreen
		this.repaint();
	}
	
	public void setInactive() {
		this.color = new Color(200,200,200);
		this.repaint();
	}
	
	public void setError() {
		this.color = new Color(255, 128, 128);
		this.repaint();
	}
}
