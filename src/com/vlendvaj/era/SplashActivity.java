package com.vlendvaj.era;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SplashActivity extends Activity implements OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		
		findViewById(R.id.btnProceed).setOnClickListener(this);
		
		SpannableStringBuilder builder = new SpannableStringBuilder();
		
		String[] orig = getString(R.string.splashTitle).split("\n");
		SpannableString str1 = new SpannableString(orig[0]);
		SpannableString str2 = new SpannableString(orig[1]);
		SpannableString str3 = new SpannableString(orig[2]);
		
		str1.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, 0);
		str1.setSpan(new StyleSpan(Typeface.ITALIC), 1, str1.length(), 0);
		
		str2.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, 0);
		str2.setSpan(new StyleSpan(Typeface.ITALIC), 1, str2.length(), 0);
		
		str3.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, 0);
		str3.setSpan(new StyleSpan(Typeface.ITALIC), 1, str3.length(), 0);
		
		builder.append(str1).append('\n').append(str2).append('\n').append(str3);
		
		((TextView) findViewById(R.id.splashTitle)).setText(builder);
	}

	@Override
	public void onClick(View v) {
		startActivity(new Intent(this, MainActivity.class).putExtra(MainActivity.EXTRA_SPLASH_TAG, true));
		finish();
	}
}
