package edu.cmu.cs.eyetrack.helper;

import java.io.File;

public class Util {
	
	
	public static enum PanelID { START_MENU, GAME, BUFFER1, DISTRACTOR_LINEUP, BUFFER2 };

	public static String getTrackItVersion() {
		return "2.0.5";
	}
	
	// Debug settings, debug utilities
	public static boolean DEBUG = true;
	public static void dPrint(Object o) { dPrint(o.toString()); }
	public static void dPrint(String s) { if(DEBUG) System.out.print(s); }
	public static void dPrintln(Object o) { dPrintln(o.toString()); }
	public static void dPrintln(String s) { if(DEBUG) System.out.println(s); }
	
	// CMU-only experiments (temporary activation, not ready for production
	public static boolean CMU_ONLY = true;
	
	// Adapted from Sun's JFileChooser demo
	public static String getExtension(File f) {
		String s = f.getName(), ext = null;
		
		int idx = s.lastIndexOf('.');
		if(idx > 0 && idx < s.length() - 1) {
			ext = s.substring(idx+1).toLowerCase();
		}
		return ext;
	}
}
