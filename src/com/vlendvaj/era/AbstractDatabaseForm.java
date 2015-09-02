package com.vlendvaj.era;

import java.util.ArrayList;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.TinyDB;
import com.google.appinventor.components.runtime.collect.Lists;

import com.vlendvaj.era.fragment.AppDetailFragment;

import android.util.Log;

@SuppressWarnings("unchecked")
public abstract class AbstractDatabaseForm extends Form {

	protected TinyDB tinyDB;
	protected Notifier notifier;

	public String getUserName() {
		return (String) tinyDB.GetValue("userName", null);
	}

	public ArrayList<Integer> getIds() {
		return (ArrayList<Integer>) tinyDB.GetValue("ids", Lists.<Integer> newArrayList());
	}

	public ArrayList<String> getNames() {
		return (ArrayList<String>) tinyDB.GetValue("names", Lists.<String> newArrayList());
	}

	public ArrayList<Double> getRatings() {
		ArrayList<Number> bitList = (ArrayList<Number>) tinyDB.GetValue("ratings",
				Lists.<Number> newArrayList());
		ArrayList<Double> list = Lists.<Double> newArrayList();
		list.ensureCapacity(bitList.size());

		for (Number bits : bitList)
			list.add(Double.longBitsToDouble(bits.longValue()));

		return list;
	}

	public ArrayList<Integer> getCounts() {
		return (ArrayList<Integer>) tinyDB.GetValue("counts", Lists.<Integer> newArrayList());
	}

	public void setIds(ArrayList<Integer> list) {
		tinyDB.StoreValue("ids", list.toArray());
	}

	public void setNames(ArrayList<String> list) {
		tinyDB.StoreValue("names", list.toArray());
	}

	public void setRatings(ArrayList<Double> list) {
		Long[] bitArr = new Long[list.size()];

		for (int i = 0; i < list.size(); ++i)
			bitArr[i] = Double.doubleToLongBits(list.get(i));

		tinyDB.StoreValue("ratings", bitArr);
	}

	public void setCounts(ArrayList<Integer> list) {
		tinyDB.StoreValue("counts", list.toArray());
	}

	@Override
	public void dispatchErrorOccurredEvent(Component component, String functionName, int errorNumber,
			Object... messageArgs) {
		StringBuilder str = new StringBuilder();

		for (Object obj : messageArgs) {
			if (obj instanceof String)
				str.append(replace((String) obj));
			else
				str.append(obj);
		}

		Log.wtf(getString(R.string.error) + " " + errorNumber, str.toString());

		if (component.getDispatchDelegate() instanceof AppDetailFragment) {
			((AppDetailFragment) getFragmentManager().findFragmentByTag("details"))
					.dispatchErrorOccurredEvent(component, functionName, errorNumber, str.toString());
			return;
		}

		_dispatchErrorOccurredEvent(component, functionName, errorNumber, str.toString());
	}

	protected abstract void _dispatchErrorOccurredEvent(Component component, String functionName,
			int errorNumber, String messageArgs);

	protected String replace(String text) {
		return text.replaceAll(Constants.SQLKEY, "***");
	}

	public void showMessageDialog(final String message, final String title, final String buttonText) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				notifier.ShowMessageDialog(message, title, buttonText);
			}
		});
	}

	@Override
	public boolean canDispatchEvent(Component component, String eventName) {
		activeForm = this;
		return true;
	}
}
