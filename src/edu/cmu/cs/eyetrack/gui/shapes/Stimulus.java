package edu.cmu.cs.eyetrack.gui.shapes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.jdesktop.core.animation.timing.Animator;

import edu.cmu.cs.eyetrack.gui.shapes.sabine.part1.*;
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

	protected static void takeStimulusScreenshot(final Stimulus stimulus, String basePath) {

		stimulus.move(300, 300);

		JFrame frame = new JFrame() {
			private static final long serialVersionUID = 1L;
			public void paint(Graphics g) {
				stimulus.draw((Graphics2D) g);
			}
		};
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		try {
			//BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			BufferedImage image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB); 
			Graphics2D graphics2D = image.createGraphics(); 
			graphics2D.setBackground(Color.WHITE);
			graphics2D.clearRect(0,0,(int)frame.getWidth(),(int)frame.getHeight());
			frame.paint(graphics2D); 
			ImageIO.write(image, "png", new File(basePath + ".png"));
		} catch(IOException e) {
			e.printStackTrace();
		} catch (HeadlessException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		Color color = Color.GRAY;
		int stimSize = 500;
		List<Stimulus> stimList = new ArrayList<Stimulus>(Arrays.asList(
				new Sabine1Stimulus("old_stimulus_01", color, stimSize, stimSize),
				new Sabine2Stimulus("old_stimulus_02", color, stimSize, stimSize),
				new Sabine3Stimulus("old_stimulus_03", color, stimSize, stimSize),
				new Sabine4Stimulus("old_stimulus_04", color, stimSize, stimSize),
				new Sabine5Stimulus("old_stimulus_05", color, stimSize, stimSize),
				new Sabine6Stimulus("old_stimulus_06", color, stimSize, stimSize),
				new Sabine7Stimulus("old_stimulus_07", color, stimSize),
				new Sabine8Stimulus("old_stimulus_08", color, stimSize, stimSize),
				new Sabine9Stimulus("old_stimulus_09", color, stimSize, stimSize),
				new Sabine10Stimulus("old_stimulus_10", color, stimSize, stimSize),
				new SabineNew1Stimulus("new_stimulus_01", color, stimSize, stimSize),
				new SabineNew2Stimulus("new_stimulus_02", color, stimSize, stimSize),
				new SabineNew3Stimulus("new_stimulus_03", color, stimSize, stimSize),
				new SabineNew4Stimulus("new_stimulus_04", color, stimSize, stimSize),
				new SabineNew5Stimulus("new_stimulus_05", color, stimSize, stimSize),
				new SabineNew6Stimulus("new_stimulus_06", color, stimSize, stimSize),
				new SabineNew7Stimulus("new_stimulus_07", color, stimSize, stimSize),
				new SabineNew8Stimulus("new_stimulus_08", color, stimSize, stimSize),
				new SabineNew9Stimulus("new_stimulus_09", color, stimSize, stimSize),
				new SabineNew10Stimulus("new_stimulus_10", color, stimSize, stimSize)
				));

		for(final Stimulus stimulus : stimList) {
			takeStimulusScreenshot(stimulus, stimulus.getName());
		}
	}
}
