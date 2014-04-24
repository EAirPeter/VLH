package violet.livehelper.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

@SideOnly(Side.CLIENT)
public class VLHGuiPasswordField extends GuiTextField {

	public VLHGuiPasswordField(FontRenderer p1, int p2, int p3, int p4, int p5) {
		super(p1, p2, p3, p4, p5);
	}
	
	@Override
	public String getSelectedText () {
		return "";
	}
	
	@Override
	public void drawTextBox () {
		String tmptext = getText();
		String newtext = new String();
		for (int i = 0; i < tmptext.length(); ++i)
			newtext += '*';
		setText(newtext);
		super.drawTextBox();
		setText(tmptext);
	}
}
