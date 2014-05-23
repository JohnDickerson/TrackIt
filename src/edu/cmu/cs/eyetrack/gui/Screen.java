package edu.cmu.cs.eyetrack.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;

import edu.cmu.cs.eyetrack.helper.Util.PanelID;

@SuppressWarnings("serial")
public abstract class Screen extends JPanel {

	protected final EyeTrack owner;
	
	protected PanelID nextScreen;
	
	public Screen(EyeTrack eyeTrack, PanelID nextScreen) {
		this.owner = eyeTrack;
		this.nextScreen = nextScreen;
		
		//
		// If the user ever presses Escape or Q, exit the entire program
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent key) {
				// Fire closing event on attempted exit
				if( key.getKeyCode() == KeyEvent.VK_ESCAPE ) {
					owner.killAndQuit();
				}
			}

			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyReleased(KeyEvent e) {}

			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyTyped(KeyEvent e) {}
		});

	}

	public PanelID getNextScreen() {
		return nextScreen;
	}

	public void setNextScreen(PanelID nextScreen) {
		this.nextScreen = nextScreen;
	}

	protected abstract void initialize();
	protected abstract void tearDown();
}
