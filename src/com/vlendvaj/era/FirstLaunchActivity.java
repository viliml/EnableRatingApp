package com.vlendvaj.era;

import android.content.Intent;

import com.google.appinventor.components.runtime.Button;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.TextBox;
import com.vlendvaj.era.R;

public class FirstLaunchActivity extends Form {

	private TextBox txbUserName;
	private Button btnProceed;

	@Override
	protected void $define() {
		Scrollable(false);

		Title(getString(R.string.firstLaunch));

		txbUserName = new TextBox(this);
		txbUserName.Hint(getString(R.string.txbUserName));

		btnProceed = new Button(this);
		btnProceed.Text(getString(R.string.btnProceed));

		EventDispatcher.registerEventForDelegation(this, "EventClick", "Click");
	}

	@Override
	public boolean dispatchEvent(Component component, String componentName, String eventName,
			Object[] args) {
		if (component == btnProceed && eventName == "Click") {
			startActivity(new Intent(this, MainActivity.class).putExtra(MainActivity.EXTRA_USERNAME_TAG,
					txbUserName.Text()));
			finish();
			return true;
		}
		return false;
	}
}
