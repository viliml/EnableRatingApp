package com.vlendvaj.era.admin.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.HandlesEventDispatching;
import com.google.appinventor.components.runtime.TinyDB;
import com.google.appinventor.components.runtime.Web;
import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;
import com.google.appinventor.components.runtime.util.CsvUtil;
import com.google.appinventor.components.runtime.util.YailList;

import com.vlendvaj.era.admin.AbstractDatabaseForm;
import com.vlendvaj.era.admin.AppDetailActivity;
import com.vlendvaj.era.admin.AppViewActivity;
import com.vlendvaj.era.admin.Constants;
import com.vlendvaj.era.admin.MainActivity;
import com.vlendvaj.era.admin.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class AppDetailFragment extends Fragment
		implements ComponentContainer, HandlesEventDispatching, OnClickListener {

	private RatingBar ratingBar;
	private TextView ratingText;
	private TextView textView1;
	private RatingBar ratingBarUser;
	private EditText comment;
	private Button btnSubmit;

	private Web web;
	private TinyDB tinyDB;

	private List<Map<String, String>> data = Lists.newArrayList();

	/**
	 * Create a new instance of DetailsFragment, initialized to show the text at
	 * 'index'.
	 */
	public static AppDetailFragment newInstance(int index) {
		AppDetailFragment f = new AppDetailFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("index", index);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.detail_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
				&& getDatabase() instanceof AppDetailActivity) {
			getDatabase().finish();
			return;
		}
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
				&& getDatabase() instanceof AppViewActivity) {
			return;
		}

		tinyDB = new TinyDB(this);
		web = new Web(this) {
			@Override
			public HandlesEventDispatching getDispatchDelegate() {
				return AppDetailFragment.this;
			}
		};

		EventDispatcher.registerEventForDelegation(this, "DetailGotText", "GotText");

		Constants.runQuery(web, "SELECT user, comment, rating FROM " + Constants.TABLECOMMENTS
				+ " WHERE _gameid = " + getDatabase().getIds().get(getShownIndex()));

		((TextView) getActivity().findViewById(R.id.appName)).setText(getShownName());
		ratingBar = (RatingBar) getActivity().findViewById(R.id.ratingBar);
		ratingText = (TextView) getActivity().findViewById(R.id.ratingText);
		textView1 = (TextView) getActivity().findViewById(R.id.textView1);
		ratingBarUser = (RatingBar) getActivity().findViewById(R.id.ratingBarUser);
		btnSubmit = (Button) getActivity().findViewById(R.id.btnSubmit);
		comment = (EditText) getActivity().findViewById(R.id.comment);

		ratingText.setText(getShownRating().toString().subSequence(0,
				Math.min(4, getShownRating().toString().length())) + " / 5");

		ratingBar.setEnabled(false);
		ratingBar.setStepSize(0.01f);
		ratingBar.setRating(getShownRating().floatValue());

		if ((boolean) tinyDB.GetValue(MainActivity.userName + getShownId().toString(), false)) {
			textView1.setVisibility(View.GONE);
			ratingBarUser.setVisibility(View.GONE);
			btnSubmit.setVisibility(View.GONE);
			comment.setVisibility(View.GONE);
		} else {
			btnSubmit.setOnClickListener(this);
		}
	}

	public int getShownIndex() {
		return getArguments().getInt("index", 0);
	}

	@Override
	public void onClick(View v) {
		if (v == btnSubmit) {
			if (comment.getText().length() == 0) {
				getDatabase().showMessageDialog("Plase submit a comment along with your rating.",
						"No comment", "Back");
				return;
			}

			textView1.setVisibility(View.GONE);
			ratingBarUser.setVisibility(View.GONE);
			btnSubmit.setVisibility(View.GONE);
			comment.setVisibility(View.GONE);

			Integer count = getShownCount();
			Double rating = (getShownRating() * count + ratingBarUser.getRating()) / (count + 1);
			++count;

			Constants.runQuery(web, "UPDATE " + Constants.TABLERATINGS + " SET rating = " + rating
					+ ", count = " + count + " WHERE _id = " + getShownId());
			Constants.runQuery(web,
					"INSERT INTO " + Constants.TABLECOMMENTS + "(_gameid, user, comment, rating) VALUES("
							+ getShownId() + ", '" + getDatabase().getUserName() + "', '"
							+ emplace(comment.getText()) + "', " + ratingBar.getRating() + ")");

			ratingText.setText(rating.toString().subSequence(0, Math.min(4, rating.toString().length()))
					+ " / 5\nRated " + ratingBarUser.getRating() + " / 5");

			ratingBar.setRating(rating.floatValue());

			tinyDB.StoreValue(MainActivity.userName + getShownId().toString(), true);

			updateDatabase(count, rating, comment.getText().toString());
		}
	}

	private void updateDatabase(Integer count, Double rating, String comment) {
		Map<String, String> row = Maps.newHashMap();

		row.put("user", MainActivity.userName);
		row.put("comment", comment);
		row.put("rating", rating.toString());
		data.add(row);

		_updateComments();

		ArrayList<Integer> ids = getDatabase().getIds();
		ArrayList<String> names = getDatabase().getNames();
		ArrayList<Double> ratings = getDatabase().getRatings();
		ArrayList<Integer> counts = getDatabase().getCounts();

		ratings.set(getShownIndex(), rating);
		counts.set(getShownIndex(), count);

		ArrayList<Pair<Pair<Integer, String>, Pair<Double, Integer>>> all = Lists.newArrayList();

		for (int i = 0; i < ids.size(); ++i) {
			all.add(Pair.create(Pair.create(ids.get(i), names.get(i)),
					Pair.create(ratings.get(i), counts.get(i))));
		}

		Collections.sort(all, new Comparator<Pair<Pair<Integer, String>, Pair<Double, Integer>>>() {
			@Override
			public int compare(Pair<Pair<Integer, String>, Pair<Double, Integer>> lhs,
					Pair<Pair<Integer, String>, Pair<Double, Integer>> rhs) {
				return -lhs.second.first.compareTo(rhs.second.first);
			}
		});

		for (int i = 0; i < ids.size(); ++i) {
			ids.set(i, all.get(i).first.first);
			names.set(i, all.get(i).first.second);
			ratings.set(i, all.get(i).second.first);
			counts.set(i, all.get(i).second.second);
		}

		getDatabase().setIds(ids);
		getDatabase().setNames(names);
		getDatabase().setRatings(ratings);
		getDatabase().setCounts(counts);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getActivity().getFragmentManager().findFragmentById(R.id.fragmentList).onResume();
		}
	}

	@Override
	public boolean dispatchEvent(Component component, String componentName, String eventName,
			Object[] args) {
		Log.wtf(component.toString() + componentName, eventName);
		if (component == web && eventName == "GotText") {
			webGotText((String) args[0], (int) args[1], (String) args[2], (String) args[3]);
			return true;
		}
		return false;
	}

	private void webGotText(String url, int responseCode, String responseType, String responseContent) {
		Log.wtf(responseType + responseCode, responseContent);
		try {
			switch (responseCode) {
			case 200:
				updateComments(CsvUtil.fromCsvTable(responseContent));
				break;
			case 201:
				break;
			default:
			}
		} catch (Exception e) {
			Log.e("CSV", "Thrown Exception in CSV parsing", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateComments(YailList list) {
		data = Lists.newArrayList();

		boolean first = true;

		for (YailList sublist : (Iterable<YailList>) list.getCdr()) {
			if (first) {
				first = false;
				continue;
			}

			Map<String, String> row = Maps.newHashMap();
			row.put("user", sublist.getString(0));
			row.put("comment", unplace(sublist.getString(1)));
			row.put("rating", sublist.getString(2));
			data.add(row);

			Log.wtf("row", row.toString());
		}

		Log.wtf("data", data.toString());
		_updateComments();
	}

	private void _updateComments() {
		ListView lv = (ListView) getActivity().findViewById(R.id.commentList);
		SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.list_item_comment,
				new String[] { "user", "comment", "rating" },
				new int[] { android.R.id.text1, android.R.id.text2, R.id.ratingBar });
		adapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				Log.wtf("test " + view.getId(), textRepresentation);
				switch (view.getId()) {
				case android.R.id.text1:
				case android.R.id.text2:
					((TextView) view).setText(textRepresentation);
					return true;
				case R.id.ratingBar:
					((RatingBar) view).setRating(Float.parseFloat(textRepresentation));
					view.setEnabled(false);
					return true;
				}
				return false;
			}
		});
		lv.setAdapter(adapter);
	}

	public void dispatchErrorOccurredEvent(Component component, String functionName, int errorNumber,
			String messageArgs) {

		if (component == web) {
			getDatabase().showMessageDialog("There is a problem with your Internet connection.",
					getString(R.string.error) + " " + errorNumber + " at " + functionName, "OK");
		}
	}

	private static String emplace(CharSequence str) {
		return str.toString().replace("\n", Constants.NEWLINE_REPLACEMENT).replace(",",
				Constants.COMMA_REPLACEMENT);
	}

	private static String unplace(String str) {
		return str.replace(Constants.NEWLINE_REPLACEMENT, "\n").replace(Constants.COMMA_REPLACEMENT,
				",");
	}

	public final AbstractDatabaseForm getDatabase() {
		return (AbstractDatabaseForm) getActivity();
	}

	public Integer getShownId() {
		return getDatabase().getIds().get(getShownIndex());
	}

	public String getShownName() {
		return getDatabase().getNames().get(getShownIndex());
	}

	public Double getShownRating() {
		return getDatabase().getRatings().get(getShownIndex());
	}

	public Integer getShownCount() {
		return getDatabase().getCounts().get(getShownIndex());
	}

	@Override
	public Activity $context() {
		return getActivity();
	}

	@Override
	public Form $form() {
		return getDatabase();
	}

	@Override
	public void $add(AndroidViewComponent component) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setChildWidth(AndroidViewComponent component, int width) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setChildHeight(AndroidViewComponent component, int height) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int Width() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int Height() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canDispatchEvent(Component component, String eventName) {
		return true;
	}
}
