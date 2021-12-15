package com.cym.utils;

public class AntiXSS {
	
	
	/**
	 * 滤除content中的危险 HTML 代码, 主要是脚本代码, 滚动字幕代码以及脚本事件处理代码
	 * 
	 * @param content 需要滤除的字符串
	 * @return 过滤的结果
	 */
	public static String replaceHtmlCode(String content) {
		if (null == content) {
			return null;
		}
		if (0 == content.length()) {
			return "";
		}
		// 需要滤除的脚本事件关键字
		String[] eventKeywords = { "onmouseover", "onmouseout", "onmousedown", "onmouseup", "onmousemove", "onclick", "ondblclick", "onkeypress", "onkeydown", "onkeyup", "ondragstart",
				"onerrorupdate", "onhelp", "onreadystatechange", "onrowenter", "onrowexit", "onselectstart", "onload", "onunload", "onbeforeunload", "onblur", "onerror", "onfocus", "onresize",
				"onscroll", "oncontextmenu", "alert" };
		content = replace(content, "<script", "<script", false);
		content = replace(content, "</script", "</script", false);
		content = replace(content, "<marquee", "<marquee", false);
		content = replace(content, "</marquee", "</marquee", false);
		content = replace(content, "'", "_", false);// 将单引号替换成下划线
		content = replace(content, "\"", "_", false);// 将双引号替换成下划线
		// 滤除脚本事件代码
		for (int i = 0; i < eventKeywords.length; i++) {
			content = replace(content, eventKeywords[i], "_" + eventKeywords[i], false); // 添加一个"_", 使事件代码无效
		}
		return content;
	}

	/**
	 * 将字符串 source 中的 oldStr 替换为 newStr, 并以大小写敏感方式进行查找
	 * 
	 * @param source 需要替换的源字符串
	 * @param oldStr 需要被替换的老字符串
	 * @param newStr 替换为的新字符串
	 */
	private static String replace(String source, String oldStr, String newStr) {
		return replace(source, oldStr, newStr, true);
	}

	/**
	 * 将字符串 source 中的 oldStr 替换为 newStr, matchCase 为是否设置大小写敏感查找
	 * 
	 * @param source    需要替换的源字符串
	 * @param oldStr    需要被替换的老字符串
	 * @param newStr    替换为的新字符串
	 * @param matchCase 是否需要按照大小写敏感方式查找
	 */
	private static String replace(String source, String oldStr, String newStr, boolean matchCase) {
		if (source == null) {
			return null;
		}
		// 首先检查旧字符串是否存在, 不存在就不进行替换
		if (source.toLowerCase().indexOf(oldStr.toLowerCase()) == -1) {
			return source;
		}
		int findStartPos = 0;
		int a = 0;
		while (a > -1) {
			int b = 0;
			String str1, str2, str3, str4, strA, strB;
			str1 = source;
			str2 = str1.toLowerCase();
			str3 = oldStr;
			str4 = str3.toLowerCase();
			if (matchCase) {
				strA = str1;
				strB = str3;
			} else {
				strA = str2;
				strB = str4;
			}
			a = strA.indexOf(strB, findStartPos);
			if (a > -1) {
				b = oldStr.length();
				findStartPos = a + b;
				StringBuffer bbuf = new StringBuffer(source);
				source = bbuf.replace(a, a + b, newStr) + "";
				// 新的查找开始点位于替换后的字符串的结尾
				findStartPos = findStartPos + newStr.length() - b;
			}
		}
		return source;
	}

	public static void main(String[] args) {
		// String str =
		// "./fabu-advSousuo.jsp?userName=xxx<script>alert(123);</script>&password=yyy";
		String str = "http://192.168.63.87:7001/xxx/xxxx/fabu-search.jsp?searchText=<script>alert('11');</script>";
		System.out.println(AntiXSS.replaceHtmlCode(str));
	}
}