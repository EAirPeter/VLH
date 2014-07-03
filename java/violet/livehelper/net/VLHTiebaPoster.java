package violet.livehelper.net;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import violet.livehelper.VLHImageUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VLHTiebaPoster {

	private static final String strUrlPassport = "https://passport.baidu.com";
	private static final String strUrlTieba = "http://tieba.baidu.com";
	
	private static final String strUrlPassportApi = strUrlPassport + "/v2/api";
	private static final String strUrlGetapi = strUrlPassportApi + "/?getapi&apiver=v3&class=login&logintype=dialogLogin&tpl=tb";
	private static final String strUrlLogin = strUrlPassportApi + "/?login";

	private static final String strUrlCommit = strUrlTieba + "/f/commit";
	private static final String strUrlAdd = strUrlCommit + "/post/add";
	private static final String strUrlCheckVCode = strUrlCommit + "/commonapi/checkVcode";
	private static final String strUrlGetVCode = strUrlCommit + "/commonapi/getVcode";
	private static final String strUrlTbs = strUrlTieba + "/dc/common/tbs";
	
	private static final String strDatFmtAdd = "ie=utf-8&rich_text=1&__type__=reply&tbs=%s&fid=%s&kw=%s&tid=%s&content=%s&vcode_md5=%s&vcode=%s";
	private static final String strDatFmtCheckVCode = "captcha_vcode_str=%s&captcha_code_type=%s&captcha_input_str=%s&fid=%s";
	private static final String strDatFmtGetVCode = "content=%s&tid=%s&lm=%s&word=%s";
	private static final String strDatFmtImage = "<img width=\"%d\" height=\"%d\" class=\"BDE_Image\" src=\"http://imgsrc.baidu.com/forum/pic/item/%s.jpg\" unselectable=\"on\" pic_type=\"0\"/>";
	private static final String strDatFmtLogin = "apiver=v3&charset=GBK&class=login&logintype=dialogLogin&tpl=tb&username=%s&password=%s&token=%s&codestring=%s&verifycode=%s";
	
	private static final String strUrlFmtPassportCaptcha = strUrlPassport + "/cgi-bin/genimage?%s";
	private static final String strUrlFmtThread = strUrlTieba + "/p/%s";
	private static final String strUrlFmtTiebaCaptcha = strUrlTieba + "/cgi-bin/genimg?%s";
	private static final String strUrlFmtUpload = "http://upload.tieba.baidu.com/upload/pic?tbs=%s&fid=%s";
	
	private static final String strBoundary = "****2147483647+1=-2147483648---BYVIO";
	private static final String strHtmlEOL = "<br/>";
	private static final String strHttpEOL = "\r\n";
	
	private Logger logger = LogManager.getLogger();
	
	private int vldLogin = -1;
	private int vldThread = -1;
	private String username;
	private String password;
	private String threadid;
	private String forumid;
	private String forumkw;
	private BufferedImage image;
	private String text;

	private String strContent;
	private int errno;
	private String capreason;
	private String captcha;
	private String captype;
	private String vcode;
	
	public static final int ERR_UNKNOWN = -128;
	
	private String getToken () throws Throwable {
		HttpURLConnection cTieba = VLHNetUtil.newConnectionGet(strUrlTieba);
		cTieba.connect();
		VLHNetUtil.addCookies(cTieba);
		HttpURLConnection cGetapi = VLHNetUtil.newConnectionGet(strUrlGetapi);
		cGetapi.connect();
		VLHNetUtil.addCookies(cGetapi);
		return VLHNetUtil.getFromString(VLHNetUtil.readFromConnection(cGetapi), "\"token\" : \"\\w+\"", 11, 1);
	}
	
	private String getTbs () throws Throwable {
		HttpURLConnection cTbs = VLHNetUtil.newConnectionGet(strUrlTbs);
		cTbs.connect();
		String buff = VLHNetUtil.readFromConnection(cTbs);
		if (buff.indexOf("\"is_login\":1") == -1)
			return null;
		return VLHNetUtil.getFromString(buff, "\"tbs\":\"\\w+\"", 7, 1);
	}
	
	private String uploadImage (BufferedImage image) throws Throwable {
		ByteArrayOutputStream oByte = new ByteArrayOutputStream();
		ImageIO.write(image, "png", oByte);
		HttpURLConnection cUpload = VLHNetUtil.newConnectionPost(String.format(strUrlFmtUpload, getTbs(), forumid));
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
		return VLHNetUtil.getFromString(VLHNetUtil.readFromConnection(cUpload), "/\\w+\\.jpg", 1, 4);
	}
	
	public static final int LI_INVALIDINPUT = -1;
	
	public int login () {
		VLHNetUtil.resetCookies();
		if (username == null || password == null)
			return vldLogin = LI_INVALIDINPUT;
		if (username.isEmpty() || password.isEmpty())
			return vldLogin = LI_INVALIDINPUT;
		try {
			String token = getToken();
			HttpURLConnection cLogin = VLHNetUtil.newConnectionPost(strUrlLogin);
			DataOutputStream oLogin = new DataOutputStream(cLogin.getOutputStream());
			oLogin.writeBytes(String.format(strDatFmtLogin, URLEncoder.encode(username, "GBK"), URLEncoder.encode(password, "GBK"), token, captcha, vcode));
			oLogin.flush();
			oLogin.close();
			cLogin.connect();
			String buff = VLHNetUtil.readFromConnection(cLogin);
			vldLogin = Integer.valueOf(VLHNetUtil.getFromString(buff, "err_no=\\d+", 7, 0));
			captcha = VLHNetUtil.getFromString(buff, "codeString=\\w+", 11, 0);
			VLHNetUtil.addCookies(cLogin);
			return vldLogin;
		}
		catch (Throwable e) {
			logger.warn("Failed to login", e);
			return vldLogin = -1;
		}
	}
	
	public BufferedImage getTiebaCaptchaImage() {
		try {
			HttpURLConnection cCaptcha = VLHNetUtil.newConnectionGet(String.format(strUrlFmtTiebaCaptcha, captcha));
			cCaptcha.connect();
			return ImageIO.read(cCaptcha.getInputStream());
		} catch (Throwable e) {
			logger.warn("Failed to fetch tieba captcha", e);
			return null;
		}
	}
	
	public BufferedImage getPassportCaptchaImage() {
		try {
			HttpURLConnection cCaptcha = VLHNetUtil.newConnectionGet(String.format(strUrlFmtPassportCaptcha, captcha));
			cCaptcha.connect();
			return ImageIO.read(cCaptcha.getInputStream());
		} catch (Throwable e) {
			logger.warn("Failed to fetch passport captcha", e);
			return null;
		}
	}
	
	public static final int CT_INVALID = -1;
	public static final int CT_ANONYMOUS = -2;
	public static final int CT_UNLIKED = -3;
	public static final int CT_BLOCKED = -4;
	public static final int CT_404 = -5;
	
	public int checkThread() {
		if (threadid == null)
			return vldThread = CT_INVALID;
		if (!threadid.matches("\\d+"))
			return vldThread = CT_INVALID;
		if (!isLoggedIn())
			return vldThread = CT_ANONYMOUS;
		try {
			HttpURLConnection cThread = VLHNetUtil.newConnectionGet(String.format(strUrlFmtThread, threadid));
			cThread.connect();
			String buff = VLHNetUtil.readFromConnection(cThread);
			String isblocked = "PageData.user.is_block = ";
			if (buff.charAt(buff.lastIndexOf(isblocked) + isblocked.length()) != '0')
				return vldThread = CT_BLOCKED;
			if (buff.indexOf("<script src=\"http://tb1.bdstatic.com/??/tb/_/pb_404_rcm_6162aa2.js\"></script>") != -1)
				return vldThread = CT_404;
			forumid = VLHNetUtil.getFromString(buff, "fid:\'\\d+?\'", 5, 1);
			forumkw = VLHNetUtil.getFromString(buff, "kw:\'.+?\'", 4, 1);
			username = StringEscapeUtils.unescapeJava(VLHNetUtil.getFromString(buff, "\"user_name\":\".+?\"", 13, 1));
			if (forumid == null || forumkw == null)
				return vldThread = ERR_UNKNOWN;
			return vldThread = 0;
		}
		catch (Throwable e) {
			logger.warn("Failed to check thread", e);
			return vldThread = ERR_UNKNOWN;
		}
	}
	
	public int checkVCode () {
		try {
			HttpURLConnection cCheck = VLHNetUtil.newConnectionPost(strUrlCheckVCode);
			DataOutputStream oCheck = new DataOutputStream(cCheck.getOutputStream());
			oCheck.writeBytes(String.format(strDatFmtCheckVCode, captcha, captype, vcode, forumid));
			oCheck.flush();
			oCheck.close();
			cCheck.connect();
			String buff = VLHNetUtil.readFromConnection(cCheck);
			return errno = Integer.valueOf(VLHNetUtil.getFromString(buff, "\"anti_valve_err_no\":\\d+", 20, 0));
		}
		catch (Throwable e) {
			logger.warn("Failed to check VCODE", e);
			return ERR_UNKNOWN;
		}
	}
	
	public void getVCode () {
		try {
			HttpURLConnection cGet = VLHNetUtil.newConnectionPost(strUrlGetVCode);
			DataOutputStream oGet = new DataOutputStream(cGet.getOutputStream());
			oGet.writeBytes(String.format(strDatFmtGetVCode, strContent, threadid, forumid, forumkw));
			oGet.flush();
			oGet.close();
			cGet.connect();
			String buff = VLHNetUtil.readFromConnection(cGet);
			capreason = StringEscapeUtils.unescapeJava(VLHNetUtil.getFromString(buff, "\"str_reason\":\".+?\"", 14, 1));
			captcha = VLHNetUtil.getFromString(buff, "\"captcha_vcode_str\":\"\\w+\"", 21, 1);
			captype = VLHNetUtil.getFromString(buff, "\"captcha_code_type\":\\d+", 20, 0);
		}
		catch (Throwable e) {
			logger.warn("Failed to get VCODE", e);
		}
	}
	
	public static final int PT_EMPTYCONTENT = -1;
	public static final int PT_UPLOADFAILED = -2;
	
	public boolean parseContent () {
		if (image == null && text == null)
			return false;
		StringBuilder content = new StringBuilder();
		if (text != null)
			content.append(VLHNetUtil.parseLineEnd(text));
		if (image != null) {
			String img;
			try {
				img = uploadImage(image);
			}
			catch (Throwable e) {
				logger.warn("Failed to upload image", e);
				return false;
			}
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
		try {
			strContent = URLEncoder.encode(content.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.warn("Failed to parse content", e);
			return false;
		}
		return true;
	}
	
	public int postThread () {
		try {
			HttpURLConnection cAdd = VLHNetUtil.newConnectionPost(strUrlAdd);
			DataOutputStream oAdd = new DataOutputStream(cAdd.getOutputStream());
			oAdd.writeBytes(String.format(strDatFmtAdd, getTbs(), forumid, URLEncoder.encode(forumkw, "UTF-8"), threadid, strContent, captcha, vcode));
			oAdd.flush();
			oAdd.close();
			cAdd.connect();
			String buff = VLHNetUtil.readFromConnection(cAdd);
			errno = Integer.valueOf(VLHNetUtil.getFromString(buff, "\"err_code\":\\d+", 11, 0));
			capreason = StringEscapeUtils.unescapeJava(VLHNetUtil.getFromString(buff, "\"str_reason\":\".+?\"", 14, 1));
			captcha = VLHNetUtil.getFromString(buff, "\"captcha_vcode_str\":\"\\w+\"", 21, 1);
			captype = VLHNetUtil.getFromString(buff, "\"captcha_code_type\":\\d+", 20, 0);

			if (errno == 0) {
				image = null;
				text = null;
			}
			return errno;
		}
		catch (Throwable e) {
			logger.warn("Failed to post thread", e);
			return ERR_UNKNOWN;
		}
	}

	public boolean isCanPost () {
		return vldLogin == 0 && vldThread == 0;
	}

	public boolean isLoggedIn () {
		return vldLogin == 0;
	}
	
	public boolean isNeedCaptcha () {
		return captcha != null && !captcha.isEmpty();
	}
	
	public BufferedImage getImage () {
		return image;
	}
	
	public String getUsername () {
		return username;
	}

	public String getPassword () {
		return password;
	}

	public String getCaptchaReason () {
		return capreason;
	}
	
	public int getCaptchaType () {
		return Integer.valueOf(captype);
	}
	
	public String getThreadId () {
		return threadid;
	}
	
	public String getText () {
		return text;
	}
	
	public void setImage (BufferedImage pImage, boolean pWatermarked) {
		image = pImage;
		if (pWatermarked)
			VLHImageUtil.addWaterMark(image, "By @" + username);
	}
	
	public void setUsername (String pUsername) {
		username = pUsername;
	}

	public void setPassword (String pPassword) {
		password = pPassword;
	}

	public void setThreadid (String pThreadId) {
		threadid = pThreadId;
	}
	
	public void setText (String pText) {
		text = pText;
	}
	
	public void setVCode (String pVCode) {
		vcode = pVCode;
	}
}
