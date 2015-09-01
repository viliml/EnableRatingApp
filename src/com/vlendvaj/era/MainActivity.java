package com.vlendvaj.era;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.google.appinventor.components.runtime.Button;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TextBox;
import com.google.appinventor.components.runtime.TinyDB;
import com.google.appinventor.components.runtime.Web;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.YailList;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.View;

public class MainActivity extends AbstractDatabaseForm {

	// TODO REMOVE!!
	private static boolean DEBUG = false;

	public static final String EXTRA_USERNAME_TAG = MainActivity.class.getName() + ".username";

	public static String userName;

	private Button btnAdd;
	private Button btnApps;
	private TextBox txbName;
	private Button btnSubmit;
	// private Button btnDeleteDB;

	private Web web;

	private static enum State {
		NORMAL, SHOW_APPS
	}

	private State state = State.NORMAL;

	@Override
	protected void $define() {

		tinyDB = new TinyDB(this);

		if (getIntent().hasExtra(EXTRA_USERNAME_TAG)) {
			userName = getIntent().getStringExtra(EXTRA_USERNAME_TAG);
			tinyDB.StoreValue("userName", userName);
		} else {
			userName = (String) tinyDB.GetValue("userName", "");
		}

		if (userName.isEmpty()) {
			startActivity(new Intent(this, FirstLaunchActivity.class));
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

		Scrollable(true);
		Title(getString(R.string.app_name));

		btnAdd = new Button(this);
		btnAdd.Text(getString(R.string.btnAdd));

		btnApps = new Button(this);
		btnApps.Text(getString(R.string.btnApps));

		txbName = new TextBox(this);
		txbName.Visible(false);
		txbName.Hint(getString(R.string.txbName));

		btnSubmit = new Button(this);
		btnSubmit.Visible(false);
		btnSubmit.Text(getString(R.string.btnSubmit));

		// btnDeleteDB = new Button(this);
		// btnDeleteDB.Text("!!ADMIN!!\nDELETE INTERNAL DATABASE");

		web = new Web(this);

		notifier = new Notifier(this);

		EventDispatcher.registerEventForDelegation(this, "MainClick", "Click");
		EventDispatcher.registerEventForDelegation(this, "MainGotText", "GotText");

		Drawable drawable = getBackgroundDrawable();
		Field backgroundImagePath, backgroundDrawable, frameLayout;
		Method setBackground;

		try {
			backgroundImagePath = Form.class.getDeclaredField("backgroundImagePath");
			backgroundDrawable = Form.class.getDeclaredField("backgroundDrawable");
			frameLayout = Form.class.getDeclaredField("frameLayout");
			setBackground = Form.class.getDeclaredMethod("setBackground", View.class);

			backgroundDrawable.setAccessible(true);
			backgroundImagePath.setAccessible(true);
			frameLayout.setAccessible(true);
			setBackground.setAccessible(true);

			backgroundImagePath.set(this, "Test123");
			backgroundDrawable.set(this, drawable);
			setBackground.invoke(this, frameLayout.get(this));
		} catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			Log.wtf("test", e);
		}
	}

	@SuppressWarnings("deprecation")
	private Drawable getBackgroundDrawable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return getBackgroundDrawableLollipop();
		return getResources().getDrawable(R.drawable.background);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private Drawable getBackgroundDrawableLollipop() {
		return getDrawable(R.drawable.background);
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
		Log.wtf(component.toString() + componentName, eventName);
		if (component == btnAdd) {
			switch (eventName) {
			case "Click":
				btnAddClick();
				return true;
			}
			// } else if (component == btnDeleteDB) {
			// switch (eventName) {
			// case "Click":
			// tinyDB.ClearAll();
			// return true;
			// }
		} else if (component == btnApps) {
			switch (eventName) {
			case "Click":
				btnAppsClick();
				return true;
			}
		} else if (component == btnSubmit) {
			switch (eventName) {
			case "Click":
				btnSubmitClick();
				return true;
			}
		} else if (component == web) {
			switch (eventName) {
			case "GotText":
				webGotText((String) args[0], (int) args[1], (String) args[2], (String) args[3]);
				return true;
			}
		}

		return false;
	}

	private void btnAddClick() {
		txbName.Visible(true);
		btnSubmit.Visible(true);

		txbName.Text("");
	}

	private void refresh() {
		Constants.runQuery(web, "SELECT _id, name, rating, count FROM " + Constants.TABLERATINGS
				+ " WHERE visible = 1 ORDER BY rating DESC");

		txbName.Visible(false);
		btnSubmit.Visible(false);
	}

	private void btnAppsClick() {
		state = State.SHOW_APPS;
		refresh();
	}

	private void btnSubmitClick() {
		Constants.runQuery(web, "INSERT INTO " + Constants.TABLERATINGS + "(name, visible)" + " VALUES('"
				+ txbName.Text() + "', " + DEBUG + ")");
	}

	private void webGotText(String url, int responseCode, String responseType, String responseContent) {
		try {
			switch (responseCode) {
			case 200:
				switch (state) {
				case SHOW_APPS:
					updateList(CsvUtil.fromCsvTable(responseContent));
					break;
				default:
					break;
				}
				break;
			case 201:
				Log.wtf("kzugcn", responseContent);
				refresh();
				break;
			default:
				notifier.ShowMessageDialog(replace(responseContent),
						getString(R.string.error) + " " + responseCode, "OK");
			}
		} catch (Exception e) {
			Log.wtf("CSV", "Thrown Exception in CSV parsing", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateList(YailList list) {

		ArrayList<Integer> ids = Lists.newArrayList();
		ArrayList<String> names = Lists.newArrayList();
		ArrayList<Double> ratings = Lists.newArrayList();
		ArrayList<Integer> counts = Lists.newArrayList();

		Log.wtf("list", list.toJSONString());

		boolean first = true;

		for (YailList sublist : (Iterable<YailList>) list.getCdr()) {
			if (first) {
				first = false;
				continue;
			}

			Log.wtf("sublist", sublist.toJSONString());

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
}
