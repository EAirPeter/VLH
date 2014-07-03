package violet.livehelper.gui;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import violet.livehelper.VLHImageUtil;
import violet.livehelper.net.VLHTiebaPoster;
import violet.livehelper.net.VLHTiebaTask;

@SideOnly(Side.CLIENT)
public class VLHGuiPostCaptcha4 extends VLHGuiScreen {
	
	private VLHTiebaPoster poster;
	private int texId;
	private String reason;
	private List<Integer> caps = new ArrayList<Integer>();
	
	public VLHGuiPostCaptcha4 (VLHTiebaPoster pPoster, BufferedImage image) {
		poster = pPoster;
		reason = "(" + poster.getCaptchaReason() + ")";
		texId = VLHImageUtil.addImageTex(image);
	}
	
	@Override
	public void drawScreen (int px, int py, float pf) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("vlh.postcaptcha.title", new Object[0]) + reason, width / 2, 15, 0xffffff);
		int ux = width / 2 - 40;
		drawRect(ux, 58, ux + 80, 78, 0xFFFFFFFF);
		drawRect(ux, 28, ux + 80, 48, 0xFFFFFFFF);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		drawTexturedRect(ux, 28, 80, 20, 0D, 0D, 1D, 1 / 4D);
		for (int i = 0; i < caps.size(); ++i) {
			int x = caps.get(i) / 3;
			int y = caps.get(i) % 3;
			drawTexturedRect(ux + i * 20, 58, 20, 20, x / 3D, (y + 1) / 4D, 1 / 3D, 1 / 4D);
		}
		super.drawScreen(px, py, pf);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui () {
		int ux = width / 2 - 54;
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				buttonList.add(new VLHGuiImageButton(i * 3 + j, texId, ux + i * 36, 92 + j * 36, 36, 36, i / 3D, (j + 1) / 4D, 1 / 3D, 1 / 4D));
		buttonList.add(new GuiButton(9, width / 2 + 42, 58, 20, 20, "<-"));
	}
	
	@Override
	public void actionPerformed (GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
			case 9:
				delCaptcha();
				break;
			default:
				if (button instanceof VLHGuiImageButton) {
					if (addCaptcha ((VLHGuiImageButton) button)) {
						StringBuilder svc = new StringBuilder();
						for (int i : caps)
							svc.append("000").append(i / 3).append("000").append(i % 3);
						new Thread(new VLHTiebaTask(VLHTiebaTask.TT_POSTCAPTCHA, poster, svc.toString())).start();
						mc.displayGuiScreen(null);
						mc.setIngameFocus();
					}
				}
				break;
			}
		}
	}
	
	public boolean addCaptcha (VLHGuiImageButton btn) {
		caps.add(btn.id);
		if (caps.size() < 4)
			return false;
		else
			return true;
	}
	
	public boolean delCaptcha () {
		if (caps.isEmpty())
			return false;
		else {
			caps.remove(caps.size() - 1);
			return true;
		}
	}
}
