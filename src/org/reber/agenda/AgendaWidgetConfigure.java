/*
 * Copyright (C) 2011 Brian Reber
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by Brian Reber.
 * THIS SOFTWARE IS PROVIDED 'AS IS' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.reber.agenda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.reber.agenda.util.CalendarUtilities;
import org.reber.agenda.util.Constants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * This is the Activity that shows up when the user first says
 * they want to add our Agenda widget to the homescreen.  It is
 * used to configure the widget to act how they want it to.
 * 
 * @author brianreber
 */
public class AgendaWidgetConfigure extends PreferenceActivity {
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	private CalendarUtilities util;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			finish();
		}

		ListPreference appChoice = new ListPreference(getApplicationContext());
		appChoice.setEntries(getAppList(getApplicationContext()));

	}

	/**
	 * Gets a list of installed apps that could be a calendar app.
	 * 
	 * @return
	 * A list of apps that could be calendar apps
	 */
	private CharSequence[] getAppList(final Context ctx) {
		List<CharSequence> packages = new ArrayList<CharSequence>();
		PackageManager packageManager = ctx.getPackageManager();

		packages.add(ctx.getResources().getText(R.string.none));

		for (PackageInfo rs : packageManager.getInstalledPackages(0)) {
			String packageNameLC = rs.packageName.toLowerCase();
			if ((packageNameLC.contains("cal") || packageNameLC.contains("agenda")) && !packageNameLC.contains("calc")) {
				packages.add(rs.applicationInfo.loadLabel(packageManager));
			}
		}

		Collections.sort(packages, new Comparator<CharSequence>() {
			@Override
			public int compare(CharSequence object1, CharSequence object2) {
				if (object1.equals(ctx.getResources().getText(R.string.none))) {
					return -1;
				} else if (object2.equals(ctx.getResources().getText(R.string.none))) {
					return 1;
				}

				return ((String) object1).compareTo((String) object2);
			}
		});

		CharSequence[] toRet = new CharSequence[packages.size()];

		for (int i = 0; i < packages.size(); i++) {
			toRet[i] = packages.get(i);
		}

		return toRet;
	}


	/**
	 * Sets a recurring alarm for the given appwidget id.
	 * 
	 * @param ctx
	 * @param mAppWidgetId
	 * The id of the app widget for which this alarm is being set
	 */
	public static void setAlarm(Context ctx, int mAppWidgetId) {
		Log.d(Constants.TAG, "Setting alarm - " + mAppWidgetId);
		Intent widgetUpdate = new Intent();
		widgetUpdate.setAction(AgendaWidgetProvider.WIDGET_UPDATE);
		widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, mAppWidgetId, widgetUpdate, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		// Set it to update every 14 mins
		alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 3000, 14 * 60 * 1000, pendingIntent);

		AgendaWidgetProvider.addIdToAlarm(ctx, mAppWidgetId);
	}

	/**
	 * Removes a recurring alarm for the given appwidget id.
	 * 
	 * @param ctx
	 * @param mAppWidgetId
	 * The id of the app widget to remove the alarm for
	 */
	public static void cancelAlarm(Context ctx, int mAppWidgetId) {
		Log.d(Constants.TAG, "Cancelling alarm - " + mAppWidgetId);
		Intent widgetUpdate = new Intent();
		widgetUpdate.setAction(AgendaWidgetProvider.WIDGET_UPDATE);
		widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { mAppWidgetId });
		PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, mAppWidgetId, widgetUpdate, 0);
		AlarmManager alarmManager = (AlarmManager) ctx.getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);

		AgendaWidgetProvider.removeIdFromList(ctx, mAppWidgetId);
	}
}