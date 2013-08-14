package edu.cmu.cs.eyetrack.gui.trial;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import edu.cmu.cs.eyetrack.io.ResourceLoader;

@SuppressWarnings("serial")
public class Block extends JLabel {

	private ImageIcon backgroundImage = null;
	private int expectedWidth;
	private int expectedHeight;
	
	public Block(int expectedWidth, int expectedHeight) {
		this(expectedWidth, expectedHeight, null);
	}
	
	public Block(int expected_width, int expected_height, ImageIcon backgroundImage) {
		
		this.expectedWidth = expected_width;
		this.expectedHeight = expected_height;
		this.backgroundImage = backgroundImage;
		initStyle();
	}
	
	private void initStyle() {
		
		// Set a background image, if the user specified it
		if(backgroundImage != null) {
			
			// First, resize to fit in grid box
			ImageIcon scaledIcon = new ImageIcon(ResourceLoader.getInstance().createScaledImage(backgroundImage.getImage(), expectedWidth, expectedHeight));
			
			// Then, place the image / vector art in the box
			//add(new JLabel(scaledIcon));
			setIcon(scaledIcon);
		}
		
		// Grid lines
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		// Only make visible interesting parts of the block (image, border, etc)
		this.setOpaque(false);
	}
}
