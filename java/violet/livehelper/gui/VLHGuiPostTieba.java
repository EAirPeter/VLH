package violet.livehelper.gui;

import java.awt.image.BufferedImage;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import violet.livehelper.VLHImageHelper;
import violet.livehelper.VioletLiveHelper;
import violet.livehelper.net.VLHTiebaPoster;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class VLHGuiPostTieba extends GuiScreen {

	VLHTiebaPoster poster;
	
	private static final int butBack = 0;
	private static final int butWmrk = 1;
	private static final int butPost = 2;
	private GuiTextField textBox;
	BufferedImage image;
	private String text;
	private boolean wmrked;

	public VLHGuiPostTieba (VLHTiebaPoster pPoster, BufferedImage pImage) {
		poster = pPoster;
		image = pImage;
		text = poster.getText();
	}
	
	@Override
	public void updateScreen() {
		textBox.updateCursorCounter();
		text = textBox.getText();
	}
	
	@Override
	public void drawScreen(int x, int y, float f) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("vlh.postthread.title", new Object[0]), width / 2, 15, 0xffffff);
		drawString(fontRendererObj, I18n.format("vlh.postthread.text", new Object[0]), width / 2 - 200, 54, 0xa0a0a0);
		textBox.drawTextBox();
		super.drawScreen(x, y, f);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		textBox = new GuiTextField(fontRendererObj, width / 2 - 200, 66, 400, 20);
		textBox.setMaxStringLength(128);
		textBox.setText(text == null ? "" : text);
		textBox.setFocused(true);
		textBox.setCanLoseFocus(false);
		buttonList.add(new GuiButton(butBack, width / 2 - 100, height * 13 / 36 + 54, 200, 20, I18n.format("menu.returnToGame", new Object[0])));
		buttonList.add(new GuiButton(butWmrk, width / 2 - 154, height * 4 / 18 + 54, 150, 20, I18n.format("vlh.postthread.wmrk", new Object[0])));
		buttonList.add(new GuiButton(butPost, width / 2 + 4, height * 4 / 18 + 54, 150, 20, I18n.format("vlh.postthread.post", new Object[0])));
		if (wmrked)
			((GuiButton) buttonList.get(butWmrk)).displayString = I18n.format("vlh.postthread.wmrked", new Object[0]);
		if (!poster.isCanPost()) {
			((GuiButton) buttonList.get(butPost)).enabled = false;
			((GuiButton) buttonList.get(butPost)).displayString = I18n.format("vlh.postthread.configfirst", new Object[0]);
		}
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	protected void keyTyped(char kch, int kvl) {
		textBox.textboxKeyTyped(kch, kvl);
	}
	
	@Override
	protected void mouseClicked(int x, int y, int clicked) {
		super.mouseClicked(x, y, clicked);
		textBox.mouseClicked(x, y, clicked);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch(button.id) {
			case butBack:
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
				break;
			case butWmrk:
				if (wmrked) {
					wmrked = false;
					((GuiButton) buttonList.get(butWmrk)).displayString = I18n.format("vlh.postthread.wmrk", new Object[0]);
				}
				else {
					wmrked = true;
					((GuiButton) buttonList.get(butWmrk)).displayString = I18n.format("vlh.postthread.wmrked", new Object[0]);
				}
				break;
			case butPost:
				if (wmrked)
					VLHImageHelper.addWaterMark(image, "By @" + poster.getUsername());
				poster.setImage(image);
				poster.setText(text);
				if (poster.postThread()) {
					VioletLiveHelper.printMessage("vlh.message.postthread.success");
					mc.displayGuiScreen(null);
					mc.setIngameFocus();
				}
				else
					((GuiButton) buttonList.get(butPost)).displayString = I18n.format("vlh.message.postthread.failed", new Object[0]);
				break;
			}
		}
	}
	
	@Override
	public void handleKeyboardInput() {
		int vl = Keyboard.getEventKey();
		char ch = Keyboard.getEventCharacter();
		if (Keyboard.getEventKeyState() || (vl == 0 && Character.isDefined(ch))) {
			if (vl == Keyboard.KEY_F11) {
				mc.toggleFullscreen();
				return;
			}
			keyTyped(ch, vl);
		}
	}
}
