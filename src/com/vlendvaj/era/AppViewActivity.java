package com.vlendvaj.era;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TinyDB;

public class AppViewActivity extends AbstractDatabaseForm {

	public static final String TAG = AppViewActivity.class.getName();

	@Override
	protected void $define() {

		setContentView(R.layout.appview_activity);

		tinyDB = new TinyDB(this);
		notifier = new Notifier(this);
	}

	@Override
	public void _dispatchErrorOccurredEvent(Component component, String functionName, int errorNumber,
			String messageArgs) {
		//No-op
	}
}
