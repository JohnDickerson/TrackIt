package edu.cmu.cs.eyetrack.io;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import edu.cmu.cs.eyetrack.helper.Util;

public class ResourceLoader {
	
	private static ResourceLoader loader = null;
	
	private HashMap<String, ImageIcon> globalImageMap;
	
	protected ResourceLoader() {
		globalImageMap = new HashMap<String, ImageIcon>();
	}
	
	public static ResourceLoader getInstance() {
		if(loader == null) {
			loader = new ResourceLoader();
		}
		return loader;
	}
	
	
	/**
	 * Creates an ImageIcon from the image located at path.
	 * If path is invalid, alerts user and returns null.
	 * Adapted from Sun's IconDemoProject codebase.
	 * @param path Path to image resource
	 * @return ImageIcon if path is valid, null otherwise
	 */
	public ImageIcon getImageIcon(String path) {
		
		if(globalImageMap.get(path) != null) {
			return globalImageMap.get(path);
		}
		
		URL imgURL = getClass().getClassLoader().getResource(path);
		if(imgURL == null) {
			return new ImageIcon(path);
		}
		
		return new ImageIcon(imgURL);
	}
	
	public ImageIcon getImageIcon(File file) {
		//try {
			//return getImageIcon(file.getCanonicalPath());
			return getImageIcon(file.getAbsolutePath());
		//} catch(IOException e) {return null;}
	}
	
	/**
	 * Resizes the image src to a new width by height image.
	 * Adapted from Sun's IconDemoProject codebase.
	 * @param src unscaled source image
	 * @param width width of to-be-scaled image
	 * @param height height of to-be-scaled image
	 * @return new, width x height scaled version of src image
	 */
	public Image createScaledImage(Image src, int width, int height) {
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        
        return img;
	}
	
	
	public ArrayList<ImageIcon> loadImagesFromDirectory(String absolutePath) {
		
		// We check to see if there *are* any images in this directory; if
		// there are, load them
		File imgDirectory = new File(absolutePath);
		File[] files = imgDirectory.listFiles(new FilenameFilter() {

			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public boolean accept(File dir, String name) {
				
				// Filter all non-image files
				if(name.endsWith(".bmp") ||
					name.endsWith(".png") || 
					name.endsWith(".jpg") ||
					name.endsWith(".jpeg") ||
					name.endsWith(".tiff") ||
					name.endsWith(".gif")) {
					return true;
				}
				
				return false;
			}
		});
		
		// If the directory is illegal or we found no images, return failure
		if(files == null || files.length == 0) { return null; }
		
		Util.dPrintln("Usable images found: " + files.length);
		
		ArrayList<ImageIcon> imageList = new ArrayList<ImageIcon>();
		// Load images now, for use later
		for(File imageFile : files) {
			imageList.add(getImageIcon(imageFile));
		}
		
		return imageList;
	}
}
