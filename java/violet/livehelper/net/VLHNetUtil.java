package violet.livehelper.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VLHNetUtil {
	
	public static final String strHtmlEOL = "<br/>";
	
	private static List<String> cookies;
	
	public static void resetCookies () {
		CookieManager manager = new CookieManager();
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(manager);
		cookies = new ArrayList<String> ();
	}
	
	public static void addCookies (HttpURLConnection conn) {
		for (String s : conn.getHeaderFields().get("Set-Cookie")) {
			if (!s.isEmpty())
				cookies.add(s.split(";", 1)[0]);
		}
	}
	
	public static HttpURLConnection newConnection (String sUrl, String sMethod, boolean bDoInput, boolean bDoOutput) throws Throwable {
		HttpURLConnection conn = (HttpURLConnection) new URL(sUrl).openConnection();
		conn.setRequestMethod(sMethod);
		conn.setDoInput(bDoInput);
		conn.setDoOutput(bDoOutput);
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);
		for (String s : cookies)
			conn.addRequestProperty("Cookie", s);
		return conn;
	}
	
	public static HttpURLConnection newConnectionGet (String sUrl) throws Throwable {
		return newConnection(sUrl, "GET", true, false);
	}
	
	public static HttpURLConnection newConnectionPost (String sUrl) throws Throwable {
		return newConnection(sUrl, "POST", true, true);
	}
	
	public static String parseLineEnd (String sText) {
		StringBuilder dest = new StringBuilder();
		for (int i = 0; i < sText.length(); ++i) {
			char ch = sText.charAt(i);
			if (ch == '\r' || ch == '\n')
				dest.append(strHtmlEOL);
			else
				dest.append(ch);
		}
		return dest.toString();
	}
	
	public static String getFromString (String sText, String sPattern, int nOffset1, int nOffset2) {
		Matcher matcher = Pattern.compile(sPattern).matcher(sText);
		if (matcher.find())
			return sText.substring(matcher.start() + nOffset1, matcher.end() - nOffset2);
		else
			return null;
	}
	
	public static String readFromConnection (HttpURLConnection conn) throws Throwable {
		BufferedReader iConn = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder buff = new StringBuilder();
		char[] bufc = new char[1024];
		while (true) {
			int n = iConn.read(bufc);
			if (n < 0)
				break;
			buff.append(bufc);
		}
		iConn.close();
		return buff.toString();
	}
}
