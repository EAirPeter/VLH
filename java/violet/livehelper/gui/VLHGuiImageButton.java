package violet.livehelper.gui;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;

@SideOnly(Side.CLIENT)
public class VLHGuiImageButton extends GuiButton {
	
	private int texId;
	private double u, v, w, h;
	
	public VLHGuiImageButton (int pid, int pTexId, int px, int py, int pw, int ph, double pu, double pv, double pw1, double ph1) {
		super(pid, px, py, pw, ph, "");
		texId = pTexId;
		u = pu;
		v = pv;
		w = pw1;
		h = ph1;
	}
	
	public void drawTexturedRect (int x, int y, int w, int h, double u, double v, double w1, double h1) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + h, zLevel, u, v + h1);
		tessellator.addVertexWithUV(x + w, y + h, zLevel, u + w1, v + h1);
		tessellator.addVertexWithUV(x + w, y, zLevel, u + w1, v);
		tessellator.addVertexWithUV(x, y, zLevel, u, v);
		tessellator.draw();
	}
	
	@Override
	public void drawButton (Minecraft mc, int mx, int my) {
		if (visible) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			field_146123_n = mx >= xPosition && mx < xPosition + width && my >= yPosition && my < yPosition + height;
			this.mouseDragged(mc, mx, my);
			drawRect(xPosition + 2, yPosition + 2, xPosition + width - 2, yPosition + height - 2, 0xFFFFFFFF);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
			drawTexturedRect(xPosition + 4, yPosition + 4, width - 8, height - 8, u, v, w, h);
		}
	}
	
	public int getTexId () {
		return texId;
	}
}
