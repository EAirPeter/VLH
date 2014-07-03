package violet.livehelper;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import violet.livehelper.gui.VLHGuiOptions;
import violet.livehelper.gui.VLHGuiPostCaptcha0;
import violet.livehelper.gui.VLHGuiPostCaptcha4;
import violet.livehelper.gui.VLHGuiPostTieba;
import violet.livehelper.net.VLHTiebaPoster;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

@SideOnly(Side.CLIENT)
public class VLHEventHandler {

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final Logger logger = LogManager.getLogger();
	private static final KeyBinding keyOptions = new KeyBinding("vlh.key.options", Keyboard.KEY_V, "vlh.modname");
	private static final KeyBinding keyScreenPost = new KeyBinding("vlh.key.postscreen", Keyboard.KEY_F12, "vlh.modname");
	
	public static final int KH_POSTCAPTCHA = 10;
	public static final int KH_POSTCAPTCHA0 = 10;
	public static final int KH_POSTCAPTCHA1 = 11;
	public static final int KH_POSTCAPTCHA2 = 12;
	public static final int KH_POSTCAPTCHA3 = 13;
	public static final int KH_POSTCAPTCHA4 = 14;
	public static int type;
	public static BufferedImage captcha;
	
	public VLHEventHandler () {
		ClientRegistry.registerKeyBinding(keyOptions);
		ClientRegistry.registerKeyBinding(keyScreenPost);
	}
	
	@SubscribeEvent
	public void onKeyInput (KeyInputEvent event) throws Throwable {
		if (mc.inGameHasFocus) {
			if (keyScreenPost.getIsKeyPressed()) {
				try {
					BufferedImage screenshot = VioletLiveHelper.saveScreenShot();
					if (VioletLiveHelper.poster.isCanPost()) {
						if (VioletLiveHelper.poster instanceof VLHTiebaPoster)
							mc.displayGuiScreen(new VLHGuiPostTieba(VioletLiveHelper.poster, screenshot));
					}
					else {
						VioletLiveHelper.printMessage("vlh.message.postthread.notconfig");
						VLHImageUtil.setClipboardImage(screenshot);
					}
				}
				catch (Throwable e) {
					logger.warn("Couldn\'t save screenshot", e);
				}
			}
			if (keyOptions.getIsKeyPressed()) {
				mc.displayGuiScreen(new VLHGuiOptions(VioletLiveHelper.poster));
			}
		}
	}
	
	@SubscribeEvent
	public void onClientTick (ClientTickEvent event) {
		if (event.phase != Phase.END)
			return;
		switch (type) {
		case KH_POSTCAPTCHA0:
			type = 0;
			mc.displayGuiScreen(new VLHGuiPostCaptcha0(VioletLiveHelper.poster, captcha));
			break;
		case KH_POSTCAPTCHA4:
			type = 0;
			mc.displayGuiScreen(new VLHGuiPostCaptcha4(VioletLiveHelper.poster, captcha));
			break;
		}
	}
}
