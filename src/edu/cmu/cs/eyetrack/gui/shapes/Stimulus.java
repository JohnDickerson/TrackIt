package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;

import javax.swing.JFrame;

import org.jdesktop.core.animation.timing.Animator;

import edu.cmu.cs.eyetrack.gui.shapes.sabine.part2.*;
import edu.cmu.cs.eyetrack.helper.Coordinate;

public abstract class Stimulus {

	protected Shape shape;
	protected Color color = Color.BLACK;
	protected Color borderColor = Color.BLACK;
	protected String name;
	
	protected int width, height;
	
	protected Animator animator;
	
	public static enum StimulusClass { CMU, UCOLORADO };

	public Stimulus(String name, Color color, int width, int height, Color borderColor) {
		this.name = name;
		this.color = color;
		this.width = width;
		this.height = height;
		this.borderColor = borderColor;
	}

	public Stimulus(String name, Color color, int width, int height) {
		this(name, color, width, height, Color.BLACK);
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setColor(Color color) { 
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public Stimulus factoryClone() {
		return factoryClone(null);
	}
	public abstract Stimulus factoryClone(Color color);
	
	public Coordinate<Double> getCenter() {
		return new Coordinate<Double>( shape.getBounds().getCenterX(), shape.getBounds().getCenterY() );
	}
	
	public abstract void move(int newX, int newY);
	
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fill(shape);
		g.setColor(borderColor);
		g.draw(shape);
	}
	
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}
	
	public boolean isEquivalent(Stimulus s) {
		return getName().equals(s.getName());
	}

	public Animator getAnimator() {
		return animator;
	}

	public void setAnimator(Animator animator) {
		this.animator = animator;
	}

	public static void main(String args[]) {
		//final Stimulus stimulus = new Sabine1Stimulus("Sabine 1", Color.BLACK, 300, 300);
		final Stimulus stimulus = new SabineNew8Stimulus("Sabine Test", Color.BLACK, 300, 300);
		stimulus.move(175, 175);
		JFrame frame = new JFrame() {
			private static final long serialVersionUID = 1L;
			public void paint(Graphics g) {
				stimulus.draw((Graphics2D) g);
			}
		};
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		//stimulus.move(50, 50);
	}
}
