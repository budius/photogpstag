package com.budius.photogpstag;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

public class JsonLocation extends JSONObject {

	private static final String KEY_ALTITUDE = "JsonLocation.key.altitude";
	private static final String KEY_LAT = "JsonLocation.key.latitude";
	private static final String KEY_LNG = "JsonLocation.key.longitude";
	private static final String KEY_TIME = "JsonLocation.key.time";
	private static final String KEY_ACC = "JsonLocation.key.accuracy";
	private static final String KEY_BEARING = "JsonLocation.key.bearing";
	private static final String KEY_PROVIDER = "JsonLocation.key.provider";
	private static final String KEY_SPEED = "JsonLocation.key.speed";

	private static final String[] ALL_KEYS = { KEY_ALTITUDE, KEY_LAT,
			KEY_LNG, KEY_TIME, KEY_ACC, KEY_BEARING, KEY_PROVIDER, KEY_SPEED };

	public JsonLocation(Location loc) {
		try {
			put(KEY_ALTITUDE, loc.getAltitude());
			put(KEY_LAT, loc.getLatitude());
			put(KEY_LNG, loc.getLongitude());
			put(KEY_TIME, loc.getTime());
			put(KEY_ACC, loc.getAccuracy());
			put(KEY_BEARING, loc.getBearing());
			put(KEY_PROVIDER, loc.getProvider());
			put(KEY_SPEED, loc.getSpeed());
		} catch (JSONException e) {
			Log.e("Budius", "JSONException. " + e.getMessage());
		}
	}

	public JsonLocation(JSONObject copyFrom) throws JSONException {
		super(copyFrom, ALL_KEYS);
	}

	public Location getLocation() {
		Location l = new Location(optString(KEY_PROVIDER, ""));
		l.setAltitude(optDouble(KEY_ALTITUDE, 0));
		l.setLatitude(optDouble(KEY_LAT, 0));
		l.setLongitude(optDouble(KEY_LNG, 0));
		l.setTime(optLong(KEY_TIME, 0));
		l.setAccuracy((float) optDouble(KEY_ACC, 0));
		l.setBearing((float) optDouble(KEY_BEARING, 0));
		l.setSpeed((float) optDouble(KEY_SPEED, 0));
		return l;
	}
}
