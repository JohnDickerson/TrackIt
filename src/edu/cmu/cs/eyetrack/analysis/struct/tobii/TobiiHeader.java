package edu.cmu.cs.eyetrack.analysis.struct.tobii;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Date;

public class TobiiHeader {

	private String sysOS;
	private String sysUser;
	private String sysMachine;

	private String recName;
	private Date recDateTime;
	private Long recDateTimestamp;
	private Dimension recResolution;
	private Rectangle2D.Double screenSizeMM;

	private Date exportDateTime;

	private String participant;	


	public TobiiHeader() {

	}


	public boolean isCompatible(TobiiHeader header) {

		if(null==header) {
			System.err.println("TobiiHeader is null.");
			return false;
		}
		//if(!getParticipant().equals(header.getParticipant())) {
		//	System.err.println("Participants differ: " + getParticipant() + " vs. " + header.getParticipant() + "   , cannot continue.");
		//	return false;
		//}   // apparently don't care about this?  experimenters name things incorrectly
		if(!getRecDateTime().equals(header.getRecDateTime())) {
			System.err.println("Recording times differ:\n" + getRecDateTime() + "\n" + header.getRecDateTime() + "\nCannot continue.");
			return false;
		}
		return true;
	}

	public String getSysOS() {
		return sysOS;
	}


	public void setSysOS(String sysOS) {
		this.sysOS = sysOS;
	}


	public String getSysUser() {
		return sysUser;
	}


	public void setSysUser(String sysUser) {
		this.sysUser = sysUser;
	}


	public String getSysMachine() {
		return sysMachine;
	}


	public void setSysMachine(String sysMachine) {
		this.sysMachine = sysMachine;
	}


	public String getRecName() {
		return recName;
	}


	public void setRecName(String recName) {
		this.recName = recName;
	}


	public Date getRecDateTime() {
		return recDateTime;
	}


	public void setRecDateTime(Date recDateTime) {
		this.recDateTime = recDateTime;
	}


	public Dimension getRecResolution() {
		return recResolution;
	}


	public void setRecResolution(Dimension recResolution) {
		this.recResolution = recResolution;
	}


	public Date getExportDateTime() {
		return exportDateTime;
	}


	public void setExportDateTime(Date exportDateTime) {
		this.exportDateTime = exportDateTime;
	}


	public String getParticipant() {
		return participant;
	}


	public void setParticipant(String participant) {
		this.participant = participant;
	}


	public Rectangle2D.Double getScreenSizeMM() {
		return screenSizeMM;
	}


	public void setScreenSizeMM(Rectangle2D.Double screenSizeMM) {
		this.screenSizeMM = screenSizeMM;
	}


	public Long getRecDateTimestamp() {
		return recDateTimestamp;
	}


	public void setRecDateTimestamp(Long recDateTimestamp) {
		this.recDateTimestamp = recDateTimestamp;
	}


	@Override
	public String toString() {
		return "TobiiHeader [sysOS=" + sysOS + ", sysUser=" + sysUser
				+ ", sysMachine=" + sysMachine + ", recName=" + recName
				+ ", recDateTime=" + recDateTime + ", recDateTimestamp=" + recDateTimestamp + ", recResolution="
				+ recResolution + ", screenSizeMM=" + screenSizeMM + ", exportDateTime=" + exportDateTime
				+ ", participant=" + participant + "]";
	}
}
