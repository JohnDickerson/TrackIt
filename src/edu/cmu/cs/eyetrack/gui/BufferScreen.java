package edu.cmu.cs.eyetrack.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import edu.cmu.cs.eyetrack.helper.Util.PanelID;

@SuppressWarnings("serial")
public class BufferScreen extends Screen {

	private ImageIcon decoration;

	public BufferScreen(EyeTrack owner, PanelID nextScreen, ImageIcon decoration) {
		super(owner, nextScreen);
		this.decoration = decoration;

		// Decorate the frame sparsely, wait for any user input
		setLayout(new BorderLayout());
		add(new JLabel(this.decoration));

		// User trivially clicks through to the next screen
		addMouseListener(new ClickThroughMouseListener());
		addKeyListener(new KeyListener() {

			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_SPACE) {
					processAnyEvent();
				}
			}
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyReleased(KeyEvent e) {}
			//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
			public void keyTyped(KeyEvent e) {}
		});
	}

	protected void processAnyEvent() {
		// If we've hit the limit on the number of trials to run, exit/don't respond
		if(owner.getGameState().getCurrentTrialCount() >= owner.getGameState().getSettings().getExperiment().getTrialCount()) {
			owner.killAndQuit();
			return;
		} 

		// Just move to the next part of the test
		owner.switchContext(nextScreen);
	}

	protected class ClickThroughMouseListener implements MouseListener {
		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseClicked(MouseEvent arg0) {
			processAnyEvent();
		}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseEntered(MouseEvent arg0) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseExited(MouseEvent arg0) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mousePressed(MouseEvent arg0) {}

		//@Override  //TODO Java 1.5 screams about this; remove when not caring about Java 1.5
		public void mouseReleased(MouseEvent arg0) {}
	}

	@Override
	protected void initialize() {

		// If we're about to start a new trial, record the old and finished one
		if( nextScreen == PanelID.GAME ) {
			owner.getGameState().endActiveTrial();
			owner.getGameState().incCurrentTrialCount();
		}
	}

	@Override
	protected void tearDown() {

	}

}
