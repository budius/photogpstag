package com.budius.photogpstag;

import java.io.File;
import java.text.DecimalFormat;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DialogSettings extends DialogFragment implements
		OnSeekBarChangeListener, OnClickListener {

	public static final String TAG = "DialogSettings.TAG";

	private static final String PREF_NAME = "DialogSettings.preferences";
	private static final String KEY_VALUE = "DialogSettings.key.value";
	private static final int DEFAULT_VALUE = 5 * 60; // 5 min
	private static final int MAX_VALUE = 15 * 60; // 15 min

	private DecimalFormat df = new DecimalFormat("#.0");

	private TextView txt;
	private SeekBar seekbar;
	private Button btnCancel, btnOk;
	private int value;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getDialog().setTitle("Update Interval");

		View v = inflater.inflate(R.layout.settings, null);
		txt = (TextView) v.findViewById(R.id.txt);
		seekbar = (SeekBar) v.findViewById(R.id.seekbar);
		btnCancel = (Button) v.findViewById(R.id.btnCancel);
		btnOk = (Button) v.findViewById(R.id.btnOk);

		value = getInterval(getActivity());
		updateText();
		seekbar.setMax(MAX_VALUE);
		seekbar.setProgress(value);

		seekbar.setOnSeekBarChangeListener(this);
		btnCancel.setOnClickListener(this);
		btnOk.setOnClickListener(this);

		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOk:
			SharedPreferences pref = getActivity().getSharedPreferences(
					PREF_NAME, 0);
			pref.edit().putInt(KEY_VALUE, value).commit();
		case R.id.btnCancel:
			dismiss();
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		value = progress;
		if (value < 1)
			value = 1;
		updateText();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	private void updateText() {
		if (value > 60) {
			txt.setText(df.format(((double) value) / 60.0) + " minutes");
		} else {
			txt.setText(Integer.toString(value) + " seconds");
		}
	}

	/**
	 * Get the current saved interval in seconds.
	 * 
	 * @param context
	 * @return
	 */
	public static int getInterval(Context context) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, 0);
		return pref.getInt(KEY_VALUE, DEFAULT_VALUE);

	}

	public static File getLogFile(Context context) {
		return new File(context.getFilesDir(), "PhotoGPS.log");
	}

}