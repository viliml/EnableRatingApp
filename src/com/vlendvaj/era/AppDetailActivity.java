package com.vlendvaj.era;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TinyDB;

import com.vlendvaj.era.fragment.AppDetailFragment;

public class AppDetailActivity extends AbstractDatabaseForm {

	public static final String TAG = AppDetailActivity.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getFragmentManager().beginTransaction()
					.remove(getFragmentManager().findFragmentById(android.R.id.content)).commit();
			finish();
			return;
		}

		if (savedInstanceState == null) {
			AppDetailFragment fragment = new AppDetailFragment();
			fragment.setArguments(getIntent().getExtras());

			getFragmentManager().beginTransaction().add(android.R.id.content, fragment, "details").commit();
		}
	}

	@Override
	protected void $define() {
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getFragmentManager().beginTransaction()
					.remove(getFragmentManager().findFragmentById(android.R.id.content)).commit();
			finish();
			return;
		}

		tinyDB = new TinyDB(this);
		notifier = new Notifier(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void _dispatchErrorOccurredEvent(Component component, String functionName, int errorNumber,
			String messageArgs) {
		Log.wtf("Detail Error", component.toString());
	}
}
