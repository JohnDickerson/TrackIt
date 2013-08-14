package edu.cmu.cs.eyetrack.gui.trial;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;

import edu.cmu.cs.eyetrack.gui.EyeTrack;
import edu.cmu.cs.eyetrack.io.ResourceLoader;
import edu.cmu.cs.eyetrack.state.Settings.Experiment;

public class GameScreenBG {

	public static BufferedImage drawBG(EyeTrack owner) {

		Experiment exp = owner.getGameState().getSettings().getExperiment();
		
		double fullWidth = owner.getSize().getWidth();
		double fullHeight = owner.getSize().getHeight();
		double w = exp.getPixelWidth();
		double h = exp.getPixelHeight();
		double insetX = 0.5*( fullWidth - w );
		double insetY = 0.5*( fullHeight - h );
		double gW = w / exp.getGridXSize();
		double gH = h / exp.getGridYSize();

		// Background image is owner's width x height in total dimension
		BufferedImage bgImage = (BufferedImage) owner.createImage((int) fullWidth, (int) fullHeight);

		Graphics2D g = bgImage.createGraphics();

		// Potentially draw the background images, if they exist
		if(exp.getUsesBGImages()) {
			
			int bgIdx=0;
			List<ImageIcon> bgImages = owner.getGameState().getBackgroundImages();
			for( int xIdx=0; xIdx<exp.getGridXSize(); xIdx++) {
				for( int yIdx=0; yIdx<exp.getGridYSize(); yIdx++) {
					
					// Draw the scaled image at top left point = <xPixel, yPixel>
					int xPixel = Math.round((float) (insetX + xIdx * gW));
					int yPixel = Math.round((float) (insetY + yIdx * gH));
		
					// Grab the next image in our list of loaded images
					Image img = bgImages.get(bgIdx).getImage();
					Image scaledImg = ResourceLoader.getInstance().createScaledImage(img, (int) gW, (int) gH);
					bgIdx = (bgIdx + 1) % bgImages.size();
					
					System.out.println("Drawing image at " + xPixel + ", " + yPixel);
					g.drawImage(scaledImg, xPixel, yPixel, null);
					
					// Draw the image scaled to fit the whole grid block
					//g.drawImage(img,
					//		xPixel, yPixel, (int) (xPixel+gW), (int) (yPixel+gH),
					//		0, 0, img.getScaledInstance(width, height, hints))
				}
			}
		}
		
		
		// Draw the grid itself
		for( int xIdx=0; xIdx<exp.getGridXSize(); xIdx++) {
			double xInterp = xIdx * gW;
			int xPixel = Math.round((float) (insetX + xInterp));
			g.drawLine(xPixel, (int) insetY, xPixel, (int) (fullHeight-insetY));
		}
		// Very right-most line
		g.drawLine((int) (insetX + w - 1), (int) insetY, (int) (insetX + w - 1), (int) (fullHeight-insetY));
		
		for( int yIdx=0; yIdx<exp.getGridYSize(); yIdx++) {
			double yInterp = yIdx * gH;
			int yPixel = Math.round((float) (insetY + yInterp));
			g.drawLine((int) insetX, yPixel, (int) (fullWidth-insetX), yPixel);
		}
		// Very bottom line
		g.drawLine((int) insetX, (int) (insetY + h - 1), (int) (fullWidth-insetX), (int) (insetY + h - 1));
		
		
		// Output the graphics object to an actual image and return
		g.drawImage(bgImage, null, 0, 0);	
		return bgImage;
	}
}
