package violet.livehelper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration;
import violet.livehelper.net.VLHTiebaPoster;
import violet.livehelper.proxy.ProxyCommon;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod(modid = VLHConstant.MODID, name = VLHConstant.NAME, version = VLHConstant.VERSION)
public class VioletLiveHelper {

	@Instance(VLHConstant.MODID)
	public static VioletLiveHelper instance;

	@SidedProxy(clientSide = VLHConstant.CLIENTPROXY, serverSide = VLHConstant.SERVERPROXY)
	public static ProxyCommon proxy;

	private static Minecraft mc;
	public static VLHTiebaPoster poster;
	public static String chatHeader;

	@EventHandler
	public void preInit (FMLPreInitializationEvent event) {
		mc = Minecraft.getMinecraft();
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		config.save();
	}

	@EventHandler
	public void load (FMLInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(new VLHEventHandler());
		chatHeader = EnumChatFormatting.DARK_PURPLE.toString() + "[VLiveHelper] " + EnumChatFormatting.RESET.toString();
		poster = new VLHTiebaPoster();
	}
	
	public static BufferedImage saveScreenShot () throws IOException {
		File dir = new File(mc.mcDataDir, "screenshots");
		dir.mkdir();
		return VLHImageUtil.saveScreenShot(dir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
	}
	
	public static void printMessage (String sMsgUnlocalized) {
		mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(chatHeader + I18n.format(sMsgUnlocalized, new Object[0])));
	}
}
