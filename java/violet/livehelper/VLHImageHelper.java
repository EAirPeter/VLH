package violet.livehelper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VLHImageHelper {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	private static IntBuffer pixelBuffer;
	private static int[] pixelValues;

	public static BufferedImage saveScreenShot (File fileDir, int imageWidth, int imageHeight, Framebuffer framebuffer) throws IOException {
		if (OpenGlHelper.isFramebufferEnabled()) {
			imageWidth = framebuffer.framebufferTextureWidth;
			imageHeight = framebuffer.framebufferTextureHeight;
		}
		int tot = imageWidth * imageHeight;
		if (pixelBuffer == null || pixelBuffer.capacity() < tot) {
			pixelBuffer = BufferUtils.createIntBuffer(tot);
			pixelValues = new int[tot];
		}
		GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		pixelBuffer.clear();
		if (OpenGlHelper.isFramebufferEnabled()) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
			GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
		}
		else
			GL11.glReadPixels(0, 0, imageWidth, imageHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
		pixelBuffer.get(pixelValues);
		TextureUtil.func_147953_a(pixelValues, imageWidth, imageHeight);
		BufferedImage image = null;
		if (OpenGlHelper.isFramebufferEnabled()) {
			image = new BufferedImage(framebuffer.framebufferWidth, framebuffer.framebufferHeight, 1);
			int l = framebuffer.framebufferTextureHeight - framebuffer.framebufferHeight;
			for (int i = l; i < framebuffer.framebufferTextureHeight; ++i)
				for (int j = 0; j < framebuffer.framebufferTextureWidth; ++j)
					image.setRGB(j, i - l, pixelValues[i * framebuffer.framebufferTextureWidth + j]);
		}
		else {
			image = new BufferedImage(imageWidth, imageHeight, 1);
			image.setRGB(0, 0, imageWidth, imageHeight, pixelValues, 0, imageWidth);
		}
		String filename = "vlh-" + dateFormat.format(new Date());
		File fileImage = new File(fileDir, filename + ".png");
		for (int i = 2; fileImage.exists(); ++i)
			fileImage = new File(fileDir, filename + "_" + i + ".png");
		ImageIO.write(image, "png", fileImage);
		return image;
	}
	
	public static void setClipboardImage(final Image image) {
		class ImageSelection implements Transferable {
			
			private Image image;
			
			public ImageSelection (Image pImage) {
				image = pImage;
			}
			
			@Override
			public DataFlavor[] getTransferDataFlavors () {
				return new DataFlavor[] {DataFlavor.imageFlavor};
			}

			@Override
			public boolean isDataFlavorSupported (DataFlavor flavor) {
				return DataFlavor.imageFlavor.equals(flavor);
			}

			@Override
			public Object getTransferData (DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				if (!DataFlavor.imageFlavor.equals(flavor))
					throw new UnsupportedFlavorException(flavor);
				return image;
			}
		}
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageSelection(image), null);
	}
	
	public static void addWaterMark (BufferedImage image, String text) {
		int hImage = image.getHeight();
		Graphics2D gImage = image.createGraphics();
		gImage.setFont(new Font("\u9ED1\u4F53", Font.PLAIN, 24));
		gImage.setColor(Color.DARK_GRAY);
		gImage.drawString(text, 3, hImage - 5);
		gImage.drawString(text, 5, hImage - 4);
		gImage.setColor(Color.WHITE);
		gImage.drawString(text, 4, hImage - 4);
		gImage.dispose();
	}
}
