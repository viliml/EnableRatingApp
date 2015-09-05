package com.vlendvaj.era;

import java.util.ArrayList;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TinyDB;
import com.google.appinventor.components.runtime.Web;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.YailList;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AbstractDatabaseForm implements OnClickListener {

	// TODO REMOVE!!
	private static boolean DEBUG = false;

	public static final String EXTRA_USERNAME_TAG = MainActivity.class.getName() + ".username";
	public static final String EXTRA_SPLASH_TAG = MainActivity.class.getName() + ".splash";

	public static String userName;

	private Button btnAdd;
	private Button btnApps;
	private EditText txbName;
	private Button btnSubmit;
	// private Button btnDeleteDB;

	private Web web;

	private static enum State {
		NORMAL, SHOW_APPS
	}

	private State state = State.NORMAL;

	@Override
	protected void $define() {

		if (!getIntent().getBooleanExtra(EXTRA_SPLASH_TAG, false)) {
			startActivity(new Intent(this, SplashActivity.class));
			finish();
			return;
		}

		tinyDB = new TinyDB(this);

		if (getIntent().hasExtra(EXTRA_USERNAME_TAG)) {
			userName = getIntent().getStringExtra(EXTRA_USERNAME_TAG);
			tinyDB.StoreValue("userName", userName);
		} else {
			userName = (String) tinyDB.GetValue("userName", "");
		}

		if (userName == null || userName.isEmpty()) {
			startActivity(new Intent(this, FirstLaunchActivity.class).putExtras(getIntent()));
			finish();
			return;
		}

		// TypedArray arr = getThemeArgs();
		// final int back = 0xFF000000 | arr.getColor(0, 0xFF0000);
		// final int text = 0xFF000000 | arr.getColor(1, 0x00FF00);
		// int select = 0xFF0000FF;
		//
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		// {
		// select = 0xFF000000 | arr.getColor(2, select);
		// }
		// arr.recycle();

		setContentView(R.layout.main_activity);

		Title(getString(R.string.app_name));

		btnAdd = (Button) findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(this);

		btnApps = (Button) findViewById(R.id.btnApps);
		btnApps.setOnClickListener(this);

		txbName = (EditText) findViewById(R.id.txbName);

		btnSubmit = (Button) findViewById(R.id.btnSubmit);
		btnSubmit.setOnClickListener(this);

		findViewById(R.id.btnLogOut).setOnClickListener(this);

		// btnDeleteDB = new Button(this);
		// btnDeleteDB.Text("!!ADMIN!!\nDELETE INTERNAL DATABASE");

		web = new Web(this);

		notifier = new Notifier(this);

		EventDispatcher.registerEventForDelegation(this, "MainGotText", "GotText");
	}

	// private TypedArray getThemeArgs() {
	// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	// return getThemeArgs14();
	// }
	//
	// return getTheme().obtainStyledAttributes(new int[] {
	// android.R.attr.background });
	// }
	//
	// @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	// private TypedArray getThemeArgs14() {
	// return getTheme().obtainStyledAttributes(new int[] {
	// android.R.attr.colorBackground,
	// android.R.attr.textColorPrimary, android.R.attr.colorPressedHighlight });
	// }

	@Override
	public void _dispatchErrorOccurredEvent(Component component, String functionName, int errorNumber,
			String messageArgs) {

		if (component == web) {
			showMessageDialog("There is a problem with your Internet connection.",
					getString(R.string.error) + " " + errorNumber + " at " + functionName, "OK");
		}
	}

	@Override
	public boolean dispatchEvent(Component component, String componentName, String eventName,
			Object[] args) {
		if (component == web) {
			switch (eventName) {
			case "GotText":
				webGotText((String) args[0], (int) args[1], (String) args[2], (String) args[3]);
				return true;
			}
		}

		return false;
	}

	private void btnAddClick() {
		txbName.setVisibility(View.VISIBLE);
		btnSubmit.setVisibility(View.VISIBLE);

		txbName.setText("");
	}

	private void refresh() {
		Constants.runQuery(web, "SELECT _id, name, rating, count FROM " + Constants.TABLERATINGS
				+ " WHERE visible = 1 ORDER BY rating DESC");

		txbName.setVisibility(View.GONE);
		btnSubmit.setVisibility(View.GONE);
	}

	private void btnAppsClick() {
		state = State.SHOW_APPS;
		refresh();
	}

	private void btnSubmitClick() {
		Constants.runQuery(web, "INSERT INTO " + Constants.TABLERATINGS + "(name, visible)" + " VALUES('"
				+ txbName.getText() + "', " + DEBUG + ")");
	}

	private void webGotText(String url, int responseCode, String responseType, String responseContent) {
		try {
			switch (responseCode) {
			case 200:
				switch (state) {
				case SHOW_APPS:
					updateList(CsvUtil.fromCsvTable(responseContent));
					state = State.NORMAL;
					break;
				default:
					break;
				}
				break;
			case 201:
				refresh();
				break;
			default:
				notifier.ShowMessageDialog(replace(responseContent),
						getString(R.string.error) + " " + responseCode, "OK");
			}
		} catch (Exception e) {
			Log.e("CSV", "Thrown Exception in CSV parsing", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateList(YailList list) {

		ArrayList<Integer> ids = Lists.newArrayList();
		ArrayList<String> names = Lists.newArrayList();
		ArrayList<Double> ratings = Lists.newArrayList();
		ArrayList<Integer> counts = Lists.newArrayList();

		boolean first = true;

		for (YailList sublist : (Iterable<YailList>) list.getCdr()) {
			if (first) {
				first = false;
				continue;
			}

			ids.add(Integer.valueOf(sublist.getString(0)));
			names.add(sublist.getString(1));
			ratings.add(Double.valueOf(sublist.getString(2)));
			counts.add(Integer.valueOf(sublist.getString(3)));
		}

		setIds(ids);
		setNames(names);
		setRatings(ratings);
		setCounts(counts);

		startActivity(new Intent(this, AppViewActivity.class));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnAdd:
			btnAddClick();
			break;
		// } else if (component == btnDeleteDB) {
		// switch (eventName) {
		// case "Click":
		// tinyDB.ClearAll();
		// return true;
		// }
		case R.id.btnApps:
			btnAppsClick();
			break;
		case R.id.btnSubmit:
			btnSubmitClick();
			break;
		case R.id.btnLogOut:
			tinyDB.ClearTag("userName");
			startActivity(new Intent(this, FirstLaunchActivity.class));
			break;
		}
	}
}
