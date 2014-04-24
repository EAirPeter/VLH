package violet.livehelper.net;

import java.awt.image.BufferedImage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface VLHIPoster {
	
	public boolean login();
	public boolean checkThread();
	public boolean postThread ();
	public boolean isCanPost ();
	public boolean isLoggedIn ();
	public BufferedImage getImage ();
	public String getUsername ();
	public String getPassword ();
	public String getThreadId ();
	public String getText ();
	public void setImage (BufferedImage pImage);
	public void setUsername (String pUsername);
	public void setPassword (String pPassword);
	public void setThreadid (String pThreadId);
	public void setText (String pText);
}
