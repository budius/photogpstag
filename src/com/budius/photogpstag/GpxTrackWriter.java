package com.budius.photogpstag;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.location.Location;

/**
 * Write track as GPX to a file.
 * 
 * @author Sandor Dornbush
 */
public class GpxTrackWriter {

	private static final NumberFormat ELEVATION_FORMAT = NumberFormat
			.getInstance(Locale.US);
	private static final NumberFormat COORDINATE_FORMAT = NumberFormat
			.getInstance(Locale.US);
	static {
		/*
		 * GPX readers expect to see fractional numbers with US-style
		 * punctuation. That is, they want periods for decimal points, rather
		 * than commas.
		 */
		ELEVATION_FORMAT.setMaximumFractionDigits(1);
		ELEVATION_FORMAT.setGroupingUsed(false);

		COORDINATE_FORMAT.setMaximumFractionDigits(6);
		COORDINATE_FORMAT.setMaximumIntegerDigits(3);
		COORDINATE_FORMAT.setGroupingUsed(false);
	}

	private PrintWriter printWriter;

	public GpxTrackWriter() {
	}

	public void prepare(File file) throws IOException {
		this.printWriter = new PrintWriter(file);
	}

	public void close() {
		if (printWriter != null) {
			printWriter.close();
			printWriter = null;
		}
	}

	public void writeHeader() {
		if (printWriter != null) {
			printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			printWriter.println("<gpx");
			printWriter.println("version=\"1.1\"");
			printWriter.println("creator=\"Budius\"");
			printWriter.println("xmlns=\"http://www.topografix.com/GPX/1/1\"");
			printWriter
					.println("xmlns:topografix=\"http://www.topografix.com/GPX/Private/TopoGrafix/0/1\"");
			printWriter
					.println("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
			printWriter
					.println("xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1"
							+ " http://www.topografix.com/GPX/1/1/gpx.xsd"
							+ " http://www.topografix.com/GPX/Private/TopoGrafix/0/1"
							+ " http://www.topografix.com/GPX/Private/TopoGrafix/0/1/topografix.xsd\">");
			// printWriter.println("<metadata>");
			// printWriter.println("<name>" +
			// StringUtils.formatCData(track.getName()) + "</name>");
			// printWriter.println("<desc>" +
			// StringUtils.formatCData(track.getDescription()) + "</desc>");
			// printWriter.println("</metadata>");
		}
	}

	public void writeFooter() {
		if (printWriter != null) {
			printWriter.println("</gpx>");
		}
	}

	public void writeBeginTrack() {
		if (printWriter != null) {
			printWriter.println("<trk>");
			printWriter.println("<name> track </name>");
			// printWriter.println("<desc>" +
			// StringUtils.formatCData(track.getDescription()) + "</desc>");
			// printWriter.println("<type>" +
			// StringUtils.formatCData(track.getCategory()) + "</type>");
			printWriter
					.println("<extensions><topografix:color>c0c0c0</topografix:color></extensions>");
		}
	}

	public void writeEndTrack() {
		if (printWriter != null) {
			printWriter.println("</trk>");
		}
	}

	public void writeOpenSegment() {
		printWriter.println("<trkseg>");
	}

	public void writeCloseSegment() {
		printWriter.println("</trkseg>");
	}

	public void writeLocation(Location location) {
		if (printWriter != null) {
			printWriter.println("<trkpt " + formatLocation(location) + ">");
			printWriter.println("<ele>"
					+ ELEVATION_FORMAT.format(location.getAltitude())
					+ "</ele>");
			printWriter.println("<time>"
					+ formatDateTimeIso8601(location.getTime()) + "</time>");
			printWriter.println("</trkpt>");
		}
	}

	/**
	 * Formats a location with latitude and longitude coordinates.
	 * 
	 * @param location
	 *            the location
	 */
	private String formatLocation(Location location) {
		return "lat=\"" + COORDINATE_FORMAT.format(location.getLatitude())
				+ "\" lon=\""
				+ COORDINATE_FORMAT.format(location.getLongitude()) + "\"";
	}

	private static final SimpleDateFormat ISO_8601_DATE_TIME_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

	static {
		ISO_8601_DATE_TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Formats the time using the ISO 8601 date time format with fractional
	 * seconds in UTC time zone.
	 * 
	 * @param time
	 *            the time in milliseconds
	 */
	public static String formatDateTimeIso8601(long time) {
		return ISO_8601_DATE_TIME_FORMAT.format(time);
	}

}
