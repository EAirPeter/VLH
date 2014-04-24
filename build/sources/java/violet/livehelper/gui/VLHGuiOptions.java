package violet.livehelper.gui;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import violet.livehelper.net.VLHIPoster;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class VLHGuiOptions extends GuiScreen {

	private static final int butBack = 0;
	private static final int butLogin = 1;
	private static final int butCheck = 2;
	private final GuiTextField[] textBoxes = new GuiTextField[3];
	private int curfoc;
	private String username;
	private String password;
	private String threadid;
	private boolean unpwchgd;
	private boolean thidchgd;
	private VLHIPoster poster;
	
	public VLHGuiOptions (VLHIPoster pPoster) {
		curfoc = -1;
		poster = pPoster;
		username = poster.getUsername();
		password = poster.getPassword();
		threadid = poster.getThreadId();
		unpwchgd = false;
		thidchgd = false;
	}
	
	@Override
	public void updateScreen() {
		for (GuiTextField i : textBoxes)
			i.updateCursorCounter();
		username = textBoxes[0].getText();
		password = textBoxes[1].getText();
		threadid = textBoxes[2].getText();
	}
	
	@Override
	public void drawScreen(int x, int y, float f) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("vlh.options.title", new Object[0]), width / 2, 15, 0xffffff);
		drawString(fontRendererObj, I18n.format("vlh.options.username", new Object[0]), width / 2 - 154, 54, 0xa0a0a0);
		drawString(fontRendererObj, I18n.format("vlh.options.password", new Object[0]), width / 2 + 4, 54, 0xa0a0a0);
		drawString(fontRendererObj, I18n.format("vlh.options.threadid", new Object[0]), width / 2 - 154, height * 13 / 36 + 54, 0xa0a0a0);
		for (GuiTextField i : textBoxes)
			i.drawTextBox();
		super.drawScreen(x, y, f);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(butBack, width / 2 - 75, height * 7 / 12 + 54, 150, 20, I18n.format("menu.returnToGame", new Object[0])));
		buttonList.add(new GuiButton(butLogin, width / 2 - 75, height * 2 / 9 + 54, 150, 20, I18n.format("vlh.options.login", new Object[0])));
		buttonList.add(new GuiButton(butCheck, width / 2 + 4, height * 13 / 36 + 66, 150, 20, I18n.format("vlh.options.check", new Object[0])));
		textBoxes[0] = new GuiTextField(fontRendererObj, width / 2 - 154, 66, 150, 20);
		textBoxes[0].setText(username == null ? "" : username);
		textBoxes[1] = new VLHGuiPasswordField(fontRendererObj, width / 2 + 4, 66, 150, 20);
		textBoxes[1].setText(password == null ? "" : password);
		textBoxes[2] = new GuiTextField(fontRendererObj, width / 2 - 154, height * 13 / 36 + 66, 150, 20);
		textBoxes[2].setText(threadid == null ? "" : threadid);
		if (poster.isCanPost()) {
			((GuiButton) buttonList.get(butLogin)).enabled = false;
			((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.loggedin", new Object[0]);
			((GuiButton) buttonList.get(butCheck)).enabled = false;
			((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.valid", new Object[0]);
		}
		else if (poster.isLoggedIn()) {
			((GuiButton) buttonList.get(butLogin)).enabled = false;
			((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.loggedin", new Object[0]);
			((GuiButton) buttonList.get(butCheck)).enabled = true;
			((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.check", new Object[0]);
		}
		else {
			((GuiButton) buttonList.get(butLogin)).enabled = true;
			((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.login", new Object[0]);
			((GuiButton) buttonList.get(butCheck)).enabled = false;
			((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.loginfirst", new Object[0]);
		}
	}
	
	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	protected void keyTyped(char kch, int kvl) {
		unpwchgd |= textBoxes[0].textboxKeyTyped(kch, kvl);
		unpwchgd |= textBoxes[1].textboxKeyTyped(kch, kvl);
		thidchgd |= textBoxes[2].textboxKeyTyped(kch, kvl);
		if (unpwchgd || !poster.isLoggedIn()) {
			((GuiButton) buttonList.get(butLogin)).enabled = true;
			((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.login", new Object[0]);
			((GuiButton) buttonList.get(butCheck)).enabled = false;
			((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.loginfirst", new Object[0]);
		} else if (thidchgd || !poster.isCanPost()) {
			((GuiButton) buttonList.get(butLogin)).enabled = false;
			((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.loggedin", new Object[0]);
			((GuiButton) buttonList.get(butCheck)).enabled = true;
			((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.check", new Object[0]);
		}
		switch (kvl) {
		case Keyboard.KEY_ESCAPE:
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			break;
		case Keyboard.KEY_TAB:
			if (curfoc != -1)
				textBoxes[curfoc].setFocused(false);
			curfoc = (curfoc + 1) % textBoxes.length;
			textBoxes[curfoc].setFocused(true);
			break;
		case Keyboard.KEY_RETURN:
		case Keyboard.KEY_NUMPADENTER:
			if (curfoc == 0 || curfoc == 1)
				actionPerformed((GuiButton) buttonList.get(butLogin));
			else
				actionPerformed((GuiButton) buttonList.get(butCheck));
			break;
		}
	}
	
	@Override
	protected void mouseClicked(int x, int y, int clicked) {
		super.mouseClicked(x, y, clicked);
		curfoc = -1;
		for (GuiTextField i : textBoxes)
			i.mouseClicked(x, y, clicked);
		for (int i = 0; i < textBoxes.length; ++i)
			if (textBoxes[i].isFocused()) {
				curfoc = i;
				break;
			}
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			switch (button.id) {
			case butBack:
				mc.displayGuiScreen(null);
				mc.setIngameFocus();
				break;
			case butLogin:
				poster.setUsername(username);
				poster.setPassword(password);
				unpwchgd = false;
				if (poster.login()) {
					((GuiButton) buttonList.get(butLogin)).enabled = false;
					((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.loggedin", new Object[0]);
					((GuiButton) buttonList.get(butCheck)).enabled = true;
					((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.check", new Object[0]);
				}
				else {
					((GuiButton) buttonList.get(butLogin)).enabled = true;
					((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.failed", new Object[0]);
					((GuiButton) buttonList.get(butCheck)).enabled = false;
					((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.loginfirst", new Object[0]);
				}
				break;
			case butCheck:
				poster.setThreadid(threadid);
				if (poster.checkThread()) {
					username = poster.getUsername();
					textBoxes[0].setText(username);
					((GuiButton) buttonList.get(butLogin)).enabled = false;
					((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.loggedin", new Object[0]);
					((GuiButton) buttonList.get(butCheck)).enabled = false;
					((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.valid", new Object[0]);
				}
				else if (poster.isLoggedIn()) {
					((GuiButton) buttonList.get(butLogin)).enabled = false;
					((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.loggedin", new Object[0]);
					((GuiButton) buttonList.get(butCheck)).enabled = true;
					((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.invalid", new Object[0]);
				}
				else {
					((GuiButton) buttonList.get(butLogin)).enabled = true;
					((GuiButton) buttonList.get(butLogin)).displayString = I18n.format("vlh.options.login", new Object[0]);
					((GuiButton) buttonList.get(butCheck)).enabled = false;
					((GuiButton) buttonList.get(butCheck)).displayString = I18n.format("vlh.options.loginfirst", new Object[0]);
				}
				thidchgd = false;
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
