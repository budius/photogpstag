package com.budius.photogpstag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.location.Location;
import android.util.Log;

public class FileHandler {

	final private File mFile;
	private JSONArray json;

	public FileHandler(File logFile) {
		mFile = logFile;
		if (mFile.exists()) {
			extractJson();
		} else {
			json = new JSONArray();
		}
	}

	public void addToLog(Location loc) {
		json.put(new JsonLocation(loc));
		dumpJson();
	}

	public Location getFromLog(long time) {

		Location prev = null;
		for (int i = 0; i < json.length(); i++) {
			Location l;
			try {
				l = new JsonLocation(json.getJSONObject(i)).getLocation();
				if (prev != null) {
					if (prev.getTime() < time && l.getTime() > time) {
						return getMiddle(prev, l);
					}
				}
				prev = l;
			} catch (JSONException e) {
				Log.e("Budius", "JSONException. " + e.getMessage());
			}
		}

		return null;
	}

	public ArrayList<Location> getLog() {
		ArrayList<Location> l = new ArrayList<Location>();

		for (int i = 0; i < json.length(); i++) {
			try {
				l.add(new JsonLocation(json.getJSONObject(i)).getLocation());
			} catch (JSONException e) {
				Log.e("Budius", "JSONException. " + e.getMessage());
			}
		}

		return l;
	}

	private Location getMiddle(Location l1, Location l2) {
		Location r = new Location(l2);
		r.setLatitude((l1.getLatitude() + l2.getLatitude()) / 2.0);
		r.setLongitude((l1.getLongitude() + l2.getLongitude()) / 2.0);
		r.setTime((l1.getTime() / l2.getTime()) / 2);
		return r;
	}

	private void extractJson() {
		Reader r;
		try {

			r = new FileReader(mFile);
			BufferedReader br = new BufferedReader(r);
			String buffer;
			StringBuilder sb = new StringBuilder();

			while ((buffer = br.readLine()) != null) {
				sb.append(buffer);
			}

			br.close();
			r.close();

			if (sb.length() > 0) {
				json = new JSONArray(sb.toString());
			} else {
				json = new JSONArray();
			}

		} catch (FileNotFoundException e) {
			Log.e("Budius", "FileNotFoundException. " + e.getMessage());
		} catch (IOException e) {
			Log.e("Budius", "IOException. " + e.getMessage());
		} catch (JSONException e) {
			Log.e("Budius", "JSONException. " + e.getMessage());
		}
	}

	private void dumpJson() {
		mFile.delete();
		Writer w;
		try {
			w = new FileWriter(mFile);
			w.write(json.toString());
			w.flush();
			w.close();
		} catch (IOException e) {
			Log.e("Budius", "IOException. " + e.getMessage());
		}
	}

}
