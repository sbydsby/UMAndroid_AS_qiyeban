package com.sheca.zhongmei.util;

import java.util.HashMap;
import java.util.Map;

public class WebUtil {
	private static String truncateUrlPage(String strURL) {
		String strAllParam = null;
		String[] arrSplit = null;
		// strURL = strURL.trim().toLowerCase();
		arrSplit = strURL.split("[?]");
		if (strURL.length() > 1) {
			if (arrSplit.length > 1) {
				if (arrSplit[1] != null) {
					strAllParam = arrSplit[1];
				}
			}
		}
		return strAllParam;
	}

	public static String getUrlPath(String strURL) {
		String[] arrSplit = null;
		// strURL = strURL.trim().toLowerCase();
		arrSplit = strURL.split("[?]");
		return arrSplit[0];
	}

	public static Map<String, String> getURLRequest(String URL) {
		Map<String, String> mapRequest = new HashMap<String, String>();
		String[] arrSplit = null;
		String strUrlParam = truncateUrlPage(URL);
		if (strUrlParam == null) {
			return mapRequest;
		}
		// 每个键值为一组
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			// String[] arrSplitEqual = null;
			// arrSplitEqual = strSplit.split("[=]");
			// // 解析出键值
			// if (arrSplitEqual.length > 1) {
			// // 正确解析
			// mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
			// } else {
			// if (arrSplitEqual[0] != "") {
			// // 只有参数没有值，不加入
			// mapRequest.put(arrSplitEqual[0], "");
			// }
			// }

			int index1 = strSplit.indexOf("=");
			mapRequest.put(strSplit.substring(0, index1),
					strSplit.substring(index1 + 1));
		}
		return mapRequest;
	}
}
