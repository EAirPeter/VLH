package violet.livehelper.net;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VLHTiebaPoster implements VLHIPoster {

	private static final String strUrlPassportApi = "https://passport.baidu.com/v2/api";
	private static final String strUrlTieba = "http://tieba.baidu.com";

	private static final String strUrlGetapi = strUrlPassportApi + "/?getapi&apiver=v3&class=login&logintype=dialogLogin&tpl=tb";
	private static final String strUrlLogin = strUrlPassportApi + "/?login";

	private static final String strUrlAdd = strUrlTieba + "/f/commit/post/add";
	private static final String strUrlTbs = strUrlTieba + "/dc/common/tbs";
	
	private static final String strDatFmtAdd = "ie=utf-8&rich_text=1&__type__=reply&tbs=%s&fid=%s&kw=%s&tid=%s&content=%s";
	private static final String strDatFmtImage = "<img width=\"%d\" height=\"%d\" class=\"BDE_Image\" src=\"http://imgsrc.baidu.com/forum/pic/item/%s.jpg\" unselectable=\"on\" pic_type=\"0\"/>";
	private static final String strDatFmtLogin = "apiver=v3&charset=GBK&class=login&logintype=dialogLogin&tpl=tb&username=%s&password=%s&token=%s";
	
	private static final String strUrlFmtThread = strUrlTieba + "/p/%s";
	private static final String strUrlFmtUpload = "http://upload.tieba.baidu.com/upload/pic?tbs=%s&fid=%s";
	
	private static final String strBoundary = "****2147483647+1=-2147483648---BYVIO";
	private static final String strHtmlEOL = "<br/>";
	private static final String strHttpEOL = "\r\n";
	
	
	private Logger logger = LogManager.getLogger();
	
	private boolean vldLogin;
	private boolean vldThread;
	private String username;
	private String password;
	private String threadid;
	private String forumid;
	private String forumkw;
	private List<String> cookies;
	
	private BufferedImage image;
	private String text;
	
	public VLHTiebaPoster () {
		vldLogin = false;
		vldThread = false;
		username = null;
		password = null;
		threadid = null;
		forumid = null;
		forumkw = null;
		cookies = new ArrayList<String>();
		image = null;
		text = null;
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
	
	private void resetCookies () {
		CookieManager manager = new CookieManager();
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(manager);
		cookies.clear();
	}
	
	private void addCookies (HttpURLConnection conn) {
		for (String s : conn.getHeaderFields().get("Set-Cookie")) {
			if (!s.isEmpty())
				cookies.add(s.split(";", 1)[0]);
		}
	}
	
	private HttpURLConnection newConnection (String sUrl, String sMethod, boolean bDoInput, boolean bDoOutput) throws Throwable {
		HttpURLConnection conn = (HttpURLConnection) new URL(sUrl).openConnection();
		conn.setRequestMethod(sMethod);
		conn.setDoInput(bDoInput);
		conn.setDoOutput(bDoOutput);
		for (String s : cookies)
			conn.addRequestProperty("Cookie", s);
		return conn;
	}
	
	private HttpURLConnection newConnectionGet (String sUrl) throws Throwable {
		return newConnection(sUrl, "GET", true, false);
	}
	
	private HttpURLConnection newConnectionPost (String sUrl) throws Throwable {
		return newConnection(sUrl, "POST", true, true);
	}
	
	private static String readFromConnection (HttpURLConnection conn) throws Throwable {
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
	
	private static String getFromString (String sText, String sPattern, int nOffset1, int nOffset2) {
		Matcher matcher = Pattern.compile(sPattern).matcher(sText);
		if (matcher.find())
			return sText.substring(matcher.start() + nOffset1, matcher.end() - nOffset2);
		return null;
	}
	
	private String getToken () throws Throwable {
		HttpURLConnection cTieba = newConnectionGet(strUrlTieba);
		cTieba.connect();
		addCookies(cTieba);
		HttpURLConnection cGetapi = newConnectionGet(strUrlGetapi);
		cGetapi.connect();
		addCookies(cGetapi);
		return getFromString(readFromConnection(cGetapi), "\"token\" : \"\\w+\"", 11, 1);
	}
	
	private String getTbs () throws Throwable {
		HttpURLConnection cTbs = newConnectionGet(strUrlTbs);
		cTbs.connect();
		String buff = readFromConnection(cTbs);
		if (buff.indexOf("\"is_login\":1") == -1)
			return null;
		return getFromString(buff, "\"tbs\":\"\\w+\"", 7, 1);
	}
	
	private String uploadImage (BufferedImage image) throws Throwable {
		ByteArrayOutputStream oByte = new ByteArrayOutputStream();
		ImageIO.write(image, "png", oByte);
		HttpURLConnection cUpload = newConnectionPost(String.format(strUrlFmtUpload, getTbs(), forumid));
		cUpload.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + strBoundary);
		DataOutputStream oUpload = new DataOutputStream(cUpload.getOutputStream());
		oUpload.writeBytes("--" + strBoundary + strHttpEOL);
		oUpload.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"Minecraft_VLH.png\"" + strHttpEOL);
		oUpload.writeBytes("Content-Type: image/png" + strHttpEOL);
		oUpload.writeBytes(strHttpEOL);
		oUpload.write(oByte.toByteArray(), 0, oByte.toByteArray().length);
		oUpload.writeBytes(strHttpEOL);
		oUpload.writeBytes("--" + strBoundary + "--" + strHttpEOL);
		oUpload.flush();
		oUpload.close();
		cUpload.connect();
		return getFromString(readFromConnection(cUpload), "/\\w+\\.jpg", 1, 4);
	}
	
	@Override
	public boolean login () {
		resetCookies();
		if (username == null || password == null)
			return vldLogin = false;
		if (username.isEmpty() || password.isEmpty())
			return vldLogin = false;
		try {
			String token = getToken();
			HttpURLConnection cLogin = newConnectionPost(strUrlLogin);
			DataOutputStream oLogin = new DataOutputStream(cLogin.getOutputStream());
			oLogin.writeBytes(String.format(strDatFmtLogin, URLEncoder.encode(username, "GBK"), URLEncoder.encode(password, "GBK"), token));
			oLogin.flush();
			oLogin.close();
			cLogin.connect();
			addCookies(cLogin);
			if (getTbs() != null)
				return vldLogin = true;
		}
		catch (Throwable e) {
			logger.warn("Failed to login", e);
			return vldLogin = false;
		}
		return vldLogin = false;
	}

	@Override
	public boolean checkThread() {
		if (threadid == null)
			return vldThread = false;
		if (!threadid.matches("\\d+"))
			return vldThread = false;
		if (!isLoggedIn())
			return vldThread = false;
		try {
			HttpURLConnection cThread = newConnectionGet(String.format(strUrlFmtThread, threadid));
			cThread.connect();
			String buff = readFromConnection(cThread);
			String islike = "PageData.user.is_like = ";
			if (buff.charAt(buff.lastIndexOf(islike) + islike.length()) != '1')
				return vldThread = false;
			String isblocked = "PageData.user.is_block = ";
			if (buff.charAt(buff.lastIndexOf(isblocked) + isblocked.length()) != '0')
				return vldThread = false;
			if (buff.indexOf("<script src=\"http://tb1.bdstatic.com/??/tb/_/pb_404_rcm_6162aa2.js\"></script>") != -1)
				return vldThread = false;
			forumid = getFromString(buff, "fid:\'\\d+?\'", 5, 1);
			forumkw = getFromString(buff, "kw:\'.+?\'", 4, 1);
			username = StringEscapeUtils.unescapeJava(getFromString(buff, "\"user_name\":\".+?\"", 13, 1));
			System.out.println(forumid);
			System.out.println(forumkw);
			System.out.println(username);
			if (forumid != null && forumkw != null)
				return vldThread = true;
		}
		catch (Throwable e) {
			logger.warn("Failed to check thread", e);
			return vldThread = false;
		}
		return vldThread = false;
	}
	
	@Override
	public boolean postThread () {
		if (image == null && text == null)
			return false;
		try {
			StringBuilder content = new StringBuilder();
			if (text != null)
				content.append(parseLineEnd(text));
			if (image != null) {
				String img = uploadImage(image);
				if (img == null)
					return false;
				int picw = image.getWidth();
				int pich = image.getHeight();
				if (picw > 560) {
					pich = pich * 560 / picw;
					picw = 560;
				}
				content.append(strHtmlEOL).append(String.format(strDatFmtImage, picw, pich, img));
			}
			content.append(strHtmlEOL + strHtmlEOL + "\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u3000\u2014\u2014\u2014\u2014\u2014\u2014from Violet's Live Helper");
			HttpURLConnection cAdd = newConnectionPost(strUrlAdd);
			DataOutputStream oAdd = new DataOutputStream(cAdd.getOutputStream());
			oAdd.writeBytes(String.format(strDatFmtAdd, getTbs(), forumid, URLEncoder.encode(forumkw, "UTF-8"), threadid, URLEncoder.encode(content.toString(), "UTF-8")));
			oAdd.flush();
			oAdd.close();
			cAdd.connect();
			if (readFromConnection(cAdd).indexOf("\"no\":0,\"err_code\":0,\"error\":\"\"") != -1) {
				text = "";
				return true;
			}
		}
		catch (Throwable e) {
			logger.warn("Failed to post thread", e);
			return false;
		}
		return false;
	}

	@Override
	public boolean isCanPost () {
		return vldLogin && vldThread;
	}

	@Override
	public boolean isLoggedIn () {
		return vldLogin;
	}

	@Override
	public BufferedImage getImage () {
		return image;
	}
	
	@Override
	public String getUsername () {
		return username;
	}

	@Override
	public String getPassword () {
		return password;
	}

	@Override
	public String getThreadId () {
		return threadid;
	}

	@Override
	public String getText () {
		return text;
	}
	
	@Override
	public void setImage (BufferedImage pImage) {
		image = pImage;
	}
	
	@Override
	public void setUsername (String pUsername) {
		username = pUsername;
	}

	@Override
	public void setPassword (String pPassword) {
		password = pPassword;
	}

	@Override
	public void setThreadid (String pThreadId) {
		threadid = pThreadId;
	}
	
	@Override
	public void setText (String pText) {
		text = pText;
	}
}
