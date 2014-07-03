package violet.livehelper.gui;

import java.awt.image.BufferedImage;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import violet.livehelper.VLHImageUtil;
import violet.livehelper.net.VLHTiebaPoster;
import violet.livehelper.net.VLHTiebaTask;

@SideOnly(Side.CLIENT)
public class VLHGuiPostCaptcha0 extends VLHGuiScreen {
	
	private VLHTiebaPoster poster;
	private int texId;
	private String reason;
	private GuiTextField tbVCode;
	private String vcode;
	
	public VLHGuiPostCaptcha0 (VLHTiebaPoster pPoster, BufferedImage image) {
		poster = pPoster;
		reason = "(" + poster.getCaptchaReason() + ")";
		texId = VLHImageUtil.addImageTex(image);
	}
	
	@Override
	public void updateScreen () {
		tbVCode.updateCursorCounter();
		vcode = tbVCode.getText();
	}
	
	@Override
	public void drawScreen (int px, int py, float pf) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("vlh.postcaptcha.title", new Object[0]) + reason, width / 2, 15, 0xffffff);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		drawTexturedRect(width / 2 - 30, 40, 60, 20, 1 / 3D, 0, 2 / 3D, 1);
		tbVCode.drawTextBox();
		super.drawScreen(px, py, pf);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui () {
		Keyboard.enableRepeatEvents(true);
		tbVCode = new GuiTextField(fontRendererObj, width / 2 - 30, 72, 60, 20);
		tbVCode.setFocused(true);
		buttonList.add(new GuiButton(0, width / 2 - 30, 106, 60, 20, I18n.format("vlh.postthread.post", new Object[0])));
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	protected void keyTyped(char kch, int kvl) {
		tbVCode.textboxKeyTyped(kch, kvl);
		switch (kvl) {
		case Keyboard.KEY_ESCAPE:
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			break;
		case Keyboard.KEY_RETURN:
		case Keyboard.KEY_NUMPADENTER:
			actionPerformed((GuiButton) buttonList.get(0));
			break;
		}
	}
	
	@Override
	protected void mouseClicked(int x, int y, int clicked) {
		super.mouseClicked(x, y, clicked);
		tbVCode.mouseClicked(x, y, clicked);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
			case 0:
				new Thread(new VLHTiebaTask(VLHTiebaTask.TT_POSTCAPTCHA, poster, vcode)).start();
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
				break;
			}
		}
	}
}
