package violet.livehelper.net;

import java.awt.image.BufferedImage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import violet.livehelper.VLHEventHandler;
import violet.livehelper.VioletLiveHelper;

@SideOnly(Side.CLIENT)
public class VLHTiebaTask implements Runnable {
	
	public static final int TT_POSTTHREAD = 0;
	public static final int TT_POSTCAPTCHA = 1;
	
	private int type;
	private VLHTiebaPoster poster;
	
	public VLHTiebaTask (int pType, VLHTiebaPoster pPoster) {
		type = pType;
		poster = pPoster;
	}
	
	private String text;
	private BufferedImage image;
	private boolean wmrked;
	
	public VLHTiebaTask (int pType, VLHTiebaPoster pPoster, String pText, BufferedImage pImage, boolean pWmrked) {
		type = pType;
		if (type != TT_POSTTHREAD)
			throw new IllegalArgumentException("pType has to be TT_POSTTHREAD");
		poster = pPoster;
		text = pText;
		image = pImage;
		wmrked = pWmrked;
	}
	
	private String vcode;
	
	public VLHTiebaTask (int pType, VLHTiebaPoster pPoster, String pVCode) {
		type = pType;
		if (type != TT_POSTCAPTCHA)
			throw new IllegalArgumentException("pType has to be TT_POSTCAPTCHA");
		poster = pPoster;
		vcode = pVCode;
	}
	
	private void postThread () {
		int rl = poster.postThread();
		switch (rl) {
		case 0:
			VioletLiveHelper.printMessage("vlh.message.postthread.success");
			break;
		case 40:
			VLHEventHandler.captcha = poster.getTiebaCaptchaImage();
			VioletLiveHelper.printMessage("vlh.message.postthread.vcoderequired");
			VLHEventHandler.type = VLHEventHandler.KH_POSTCAPTCHA + poster.getCaptchaType();
			break;
		default:
			VioletLiveHelper.printMessage("vlh.message.postthread.failed");
			break;
		}
	}
	
	public void doPostThread () {
		poster.setText(text);
		poster.setImage(image, wmrked);
		if (!poster.parseContent()) {
			VioletLiveHelper.printMessage("vlh.message.parsecontent.failed");
			return;
		}
		postThread();
	}
	
	public void doPostCaptcha () {
		poster.setVCode(vcode);
		if (poster.checkVCode() == 0)
			postThread();
		else {
			poster.getVCode();
			VLHEventHandler.captcha = poster.getTiebaCaptchaImage();
			VioletLiveHelper.printMessage("vlh.message.postthread.vcoderequired");
			VLHEventHandler.type = VLHEventHandler.KH_POSTCAPTCHA + poster.getCaptchaType();
		}
	}
	
	@Override
	public void run() {
		switch (type) {
		case TT_POSTTHREAD:
			doPostThread();
			break;
		case TT_POSTCAPTCHA:
			doPostCaptcha();
			break;
		}
	}

}
