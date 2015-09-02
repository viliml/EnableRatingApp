package com.vlendvaj.era.admin.fragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.appinventor.components.runtime.collect.Lists;
import com.google.appinventor.components.runtime.collect.Maps;

import com.vlendvaj.era.admin.AbstractDatabaseForm;
import com.vlendvaj.era.admin.AppDetailActivity;
import com.vlendvaj.era.admin.AppViewActivity;
import com.vlendvaj.era.admin.R;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;

public class AppListFragment extends ListFragment {

	private boolean dualPane;
	private int currentPosition = -1;
	private int currentIndex = -1;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		View detailsFrame = getActivity().findViewById(R.id.frameDetails);
		dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			currentPosition = savedInstanceState.getInt("currentPosition", -1);
			currentIndex = savedInstanceState.getInt("currentIndex", -1);
		}

		if (dualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// Make sure our UI is in the correct state.
			if (currentIndex >= 0)
				showDetails(currentPosition, currentIndex);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.list_fragment, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		setListAdapter(new MyAdapter(getActivity(), android.R.layout.simple_list_item_activated_1,
				((AppViewActivity) getActivity()).getNames()));

		final EditText listFilter = (EditText) getActivity().findViewById(R.id.listFilter);
		listFilter.addTextChangedListener(new TextWatcher() {

			{
				if (listFilter.getText().length() > 0)
					onTextChanged(listFilter.getText(), 0, 0, listFilter.getText().length());
			}

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				// When user changed the Text
				getListView().setItemChecked(currentPosition, false);
				currentPosition = -1;
				((Filterable) getListAdapter()).getFilter().filter(cs);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// no-op. Required method
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// no-op. Required method
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentPosition", currentPosition);
		outState.putInt("currentIndex", currentIndex);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		showDetails(position, (int) id);
	}

	private void showDetails(int position, int index) {

		currentPosition = position;
		currentIndex = index;

		if (dualPane) {
			if (position >= 0)
				getListView().setItemChecked(position, true);

			AppDetailFragment details = (AppDetailFragment) getFragmentManager()
					.findFragmentById(R.id.frameDetails);
			if (details == null || details.getShownIndex() != index) {
				details = AppDetailFragment.newInstance((int) index);

				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frameDetails, details, "details");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		} else {
			Intent intent = new Intent(getActivity(), AppDetailActivity.class);
			intent.putExtra("index", index);
			startActivity(intent);
		}
	}

	public final AbstractDatabaseForm getDatabase() {
		return (AbstractDatabaseForm) getActivity();
	}

	private static class MyAdapter extends ArrayAdapter<String> {

		private Map<Integer, Integer> ids;
		private MyFilter filter;

		public MyAdapter(Context context, int resource, List<String> objects) {
			super(context, resource, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			view.setMinimumWidth(parent.getWidth());
			return view;
		}

		@Override
		public long getItemId(final int position) {
			if (ids != null)
				return ids.get(position);
			return position;
		}

		@Override
		public Filter getFilter() {
			if (filter == null)
				filter = new MyFilter();
			return filter;
		}

		private class MyFilter extends Filter {
			@SuppressWarnings("unchecked")
			@Override
			protected FilterResults performFiltering(CharSequence infix) {
				FilterResults results = new FilterResults();

				try {
					Field mOriginalValues = ArrayAdapter.class.getDeclaredField("mOriginalValues");
					mOriginalValues.setAccessible(true);
					Field mObjects = ArrayAdapter.class.getDeclaredField("mObjects");
					mObjects.setAccessible(true);
					Field mLock = ArrayAdapter.class.getDeclaredField("mLock");
					mLock.setAccessible(true);

					if (mOriginalValues.get(MyAdapter.this) == null) {
						synchronized (mLock) {
							mOriginalValues.set(MyAdapter.this,
									new ArrayList<String>((List<String>) mObjects.get(MyAdapter.this)));
						}
					}

					if (infix == null || infix.length() == 0) {
						ArrayList<String> list;
						synchronized (mLock) {
							list = new ArrayList<String>(
									(ArrayList<String>) mOriginalValues.get(MyAdapter.this));
						}
						results.values = list;
						results.count = list.size();
						ids = null;
					} else {
						String infixString = infix.toString().toLowerCase(Locale.getDefault());

						ArrayList<String> values;
						synchronized (mLock) {
							values = new ArrayList<String>(
									(ArrayList<String>) mOriginalValues.get(MyAdapter.this));
						}

						final int count = values.size();
						final ArrayList<String> newValues = Lists.newArrayList();
						ids = Maps.<Integer, Integer> newHashMap();

						for (int i = 0; i < count; i++) {
							final String value = values.get(i);
							final String valueText = value.toString().toLowerCase(Locale.getDefault())
									.split("\n")[0];

							if (valueText.contains(infixString)) {
								ids.put(newValues.size(), i);
								newValues.add(value);
							}
						}

						results.values = newValues;
						results.count = newValues.size();
					}
				} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
					Log.wtf("bhtgfdgg", e);
				}

				return results;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				// noinspection unchecked
				try {
					Field mObjects = ArrayAdapter.class.getDeclaredField("mObjects");
					mObjects.setAccessible(true);
					mObjects.set(MyAdapter.this, (List<String>) results.values);
					if (results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				} catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
					Log.wtf("hdffb", e);
				}
			}
		}
	}
}
