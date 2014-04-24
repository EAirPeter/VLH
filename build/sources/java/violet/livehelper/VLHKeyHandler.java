package violet.livehelper;

import java.awt.image.BufferedImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import violet.livehelper.gui.VLHGuiOptions;
import violet.livehelper.gui.VLHGuiPostTieba;
import violet.livehelper.net.VLHTiebaPoster;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

@SideOnly(Side.CLIENT)
public class VLHKeyHandler {

	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final Logger logger = LogManager.getLogger();
	private static final KeyBinding keyOptions = new KeyBinding("vlh.key.options", Keyboard.KEY_V, "vlh.modname");
	private static final KeyBinding keyScreenPost = new KeyBinding("vlh.key.postscreen", Keyboard.KEY_F12, "vlh.modname");
	
	public VLHKeyHandler () {
		ClientRegistry.registerKeyBinding(keyOptions);
		ClientRegistry.registerKeyBinding(keyScreenPost);
	}
	
	@SubscribeEvent
	public void onKeyInput (KeyInputEvent event) {
		if (mc.inGameHasFocus) {
			if (keyScreenPost.getIsKeyPressed()) {
				try {
					BufferedImage screenshot = VioletLiveHelper.saveScreenShot();
					if (VioletLiveHelper.poster.isCanPost()) {
						if (VioletLiveHelper.poster instanceof VLHTiebaPoster)
							mc.displayGuiScreen(new VLHGuiPostTieba((VLHTiebaPoster) VioletLiveHelper.poster, screenshot));
					}
					else {
						VioletLiveHelper.printMessage("vlh.message.postthread.notconfig");
						VLHImageHelper.setClipboardImage(screenshot);
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
}
