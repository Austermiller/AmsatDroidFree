/**
    predict4java: An SDP4 / SGP4 library for satellite orbit predictions

    Copyright (C)  2004-2010  David A. B. Johnson, G4DPZ.

    This class is a Java port of one of the core elements of
    the Predict program, Copyright John A. Magliacane,
    KD2BD 1991-2003: http://www.qsl.net/kd2bd/predict.html

    Dr. T.S. Kelso is the author of the SGP4/SDP4 orbital models,
    originally written in Fortran and Pascal, and released into the
    public domain through his website (http://www.celestrak.com/). 
    Neoklis Kyriazis, 5B4AZ, later re-wrote Dr. Kelso's code in C,
    and released it under the GNU GPL in 2002.
    PREDICT's core is based on 5B4AZ's code translation efforts.

    Author: David A. B. Johnson, G4DPZ <dave@g4dpz.me.uk>

    Comments, questions and bugreports should be submitted via
    http://sourceforge.net/projects/websat/
    More details can be found at the project home page:

    http://websat.sourceforge.net

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, visit http://www.fsf.org/
 */
package uk.me.g4dpz.satellite;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import uk.me.g4dpz.HamSatDroid.AppConfig;

public class SatPassTime implements Serializable {

	private static final long serialVersionUID = -6408342316986801301L;

	private boolean isSelected;
	private String details = "";

	public String getDetails() {
		return details;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	private Date startTime;
	private Date endTime;
	private Date tca;
	private String polePassed;
	private int aos;
	private int los;
	private double maxEl;
	private String TZone = " loc";
	private static final String NEW_LINE = "\n";
	private static final String DEG_NL = "\u00B0\n";
	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
	private static TimeZone timeZoneUTC = TimeZone.getTimeZone("UTC");
	private static TimeZone timeZoneDefault = TimeZone.getDefault();
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM dd, yyyy", Locale.ENGLISH);

	public SatPassTime(final Date startTime, final Date endTime, final Date tca, final String polePassed, final int aosAzimuth, final int losAzimuth, final double maxEl) {
		// constructor stub
		this.startTime = new Date(startTime.getTime());
		this.endTime = new Date(endTime.getTime());
		this.polePassed = polePassed;
		this.aos = aosAzimuth;
		this.los = losAzimuth;
		this.maxEl = maxEl;
		this.tca = new Date(tca.getTime());
		this.details = this.toString();
	}

	public final Date getStartTime() {
		return new Date(startTime.getTime());
	}

	public final Date getEndTime() {
		return new Date(endTime.getTime());
	}

	public final Date getTCA() {
		return new Date(tca.getTime());
	}

	public final void setTCA(final Date theTCA) {
		this.tca = theTCA;
	}

	public final String getPolePassed() {
		return polePassed;
	}

	/**
	 * @return the aos azimuth
	 */
	public final int getAosAzimuth() {
		return aos;
	}

	/**
	 * @return the los azimuth
	 */
	public final int getLosAzimuth() {
		return los;
	}

	/**
	 * @return the maxEl
	 */
	public final double getMaxEl() {
		return maxEl;
	}

	/**
	 * Returns a string representing the contents of the object.
	 */
	@Override
	public String toString() {

		return passDateTimeString() + NEW_LINE + passInfoString();
	}

	public String passDateTimeString() {

		final double duration = (endTime.getTime() - startTime.getTime()) / 60000.0;

		// display the local or UTC date/time depending on the user config setting.
		if (!AppConfig.isUtcDateTime()){
			TIME_FORMAT.setTimeZone(timeZoneDefault);
			DATE_FORMAT.setTimeZone(timeZoneDefault);
			TZone = " loc";
		} else {
			TIME_FORMAT.setTimeZone(timeZoneUTC);
			DATE_FORMAT.setTimeZone(timeZoneUTC);
			TZone = " UTC";
		}

		return "Date: " + DATE_FORMAT.format(startTime) + NEW_LINE + "Start Time: " + TIME_FORMAT.format(startTime) + TZone
				+ NEW_LINE +
				// "End Time: " + mTimeFormatter.format(endDate_time) + "\n" +
				String.format("Duration: %4.1f min.", duration);
	}

	public String passInfoString() {
		return "AOS Azimuth: " + aos + DEG_NL + String.format("Max Elevation: %4.1f\u00B0\n", maxEl) + "LOS Azimuth: " + los
				+ "\u00B0";
	}
}
