package violet.livehelper.gui;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

@SideOnly(Side.CLIENT)
public class VLHGuiScreen extends GuiScreen {
	
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
