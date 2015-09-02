package com.vlendvaj.era;

import com.google.appinventor.components.runtime.Form;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class FirstLaunchActivity extends Form implements OnClickListener {

	private EditText txbUserName;
	private Button btnProceed;

	@Override
	protected void $define() {
		Scrollable(false);

		Title(getString(R.string.firstLaunch));

		setContentView(R.layout.first_launch);

		txbUserName = (EditText) findViewById(R.id.txbUserName);

		btnProceed = (Button) findViewById(R.id.btnProceed);

		btnProceed.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		startActivity(new Intent(this, MainActivity.class).putExtra(MainActivity.EXTRA_USERNAME_TAG,
				txbUserName.getText().toString()));
		finish();
	}
}
