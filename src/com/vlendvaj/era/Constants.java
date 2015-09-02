package com.vlendvaj.era;

import com.google.appinventor.components.runtime.Web;
import com.google.appinventor.components.runtime.util.YailList;

public class Constants {
	public static final String URL = "http://www.roby.hr/android/spoji.php";
	public static final String SQLKEY = "QUd8Xe1";
	public static final String TABLERATINGS = "gameratings";
	public static final String TABLECOMMENTS = "comments";

	public static final YailList requestHeaders = YailList.makeList(new YailList[] {
			YailList.makeList(new String[] { "Content-Type", "application/x-www-form-urlencoded" }) });

	public static void runQuery(Web web, String query) {
		web.Url(Constants.URL);
		web.RequestHeaders(Constants.requestHeaders);
		web.PostText(
				web.BuildRequestData(
						YailList.makeList(new YailList[] {
								YailList.makeList(
										new String[] { "key", web.UriEncode(Constants.SQLKEY) }),
								YailList.makeList(new String[] { "query", web.UriEncode(query) }) })));
	}

	public static final String NEWLINE_REPLACEMENT = "%&_newline_&%";
	public static final String COMMA_REPLACEMENT = "%&_comma_&%";

}
