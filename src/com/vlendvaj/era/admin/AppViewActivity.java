package com.vlendvaj.era.admin;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TinyDB;

import android.util.Log;

public class AppViewActivity extends AbstractDatabaseForm {

	public static final String TAG = AppViewActivity.class.getName();

	@Override
	protected void $define() {

		setContentView(R.layout.appview_activity);

		Title(getString(R.string.app_name));

		tinyDB = new TinyDB(this);
		notifier = new Notifier(this);
	}

	@Override
	public void _dispatchErrorOccurredEvent(Component component, String functionName, int errorNumber,
			String messageArgs) {
		Log.wtf("Appview Error", component.toString());
	}
}
