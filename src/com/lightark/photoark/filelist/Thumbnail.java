package com.lightark.photoark.filelist;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.lightark.photoark.ImageData;

public class Thumbnail extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean invalidFile = false;
	
	private ImageData imageData;
	
	private String filePath;
	
	private BufferedImage originalImage = null;
	private BufferedImage scaledImage;
	
	private int orient = 0;
	
	private boolean calculateDim;
	private int scaledWidth;
	private int scaledHeight;
	private int availableWidth;
	private int availableHeight;
	
	private Container parent = null;
	
	public Thumbnail(URL filePath, boolean calculateDim, int width, int height)
	{
		this.calculateDim = calculateDim;
		this.scaledWidth = width;
		this.scaledHeight = height;
		this.availableWidth = width;
		this.availableHeight = height;

		try
		{
			originalImage = ImageIO.read(filePath);
			init(false);
		}
		catch(IOException e)
		{
			invalidFile = true;
		}
	}
	
	public Thumbnail(String filePath, boolean calculateDim, int width, int height)
	{
		this.calculateDim = calculateDim;
		this.scaledWidth = width;
		this.scaledHeight = height;
		this.availableWidth = width;
		this.availableHeight = height;
		this.filePath = filePath;
		this.imageData = new ImageData(filePath);
		init(true);
	}
	
	private void init(boolean extract)
	{
		this.setOpaque(false);
		if(invalidFile)
		{
			return;
		}

		if(imageData != null && !imageData.invalidFile)
		{
			ExifIFD0Directory exif = imageData.getMetadata().getFirstDirectoryOfType(ExifIFD0Directory.class);
			if(exif != null)
			{
				if(exif.containsTag(ExifIFD0Directory.TAG_ORIENTATION))
				{
					int orientTag;
					try
					{
						orientTag = exif.getInt(ExifIFD0Directory.TAG_ORIENTATION);
						if(orientTag == 3)
						{
							orient = 180;
						}
						else if(orientTag == 6)
						{
							orient = 90;
						}
						else if(orientTag == 8)
						{
							orient = 270;
						}
					}
					catch (MetadataException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		if(extract)
		{
			if(imageData != null && !imageData.invalidFile)
			{
				ExifThumbnailDirectory dir = imageData.getMetadata().getFirstDirectoryOfType(ExifThumbnailDirectory.class);
				if(dir != null && dir.hasThumbnailData())
				{
					byte[] array = dir.getThumbnailData();
					try
					{
						originalImage = ImageIO.read(new ByteArrayInputStream(array));
						init(false);
					}
					catch (IOException e)
					{
						invalidFile = true;
						return;
					}
				}
				else
				{
					try
					{
						originalImage = ImageIO.read(new File(filePath));
						init(false);
					}
					catch(IOException e)
					{
						invalidFile = true;
						return;
					}
				}
			}
			else
			{
				try
				{
					originalImage = ImageIO.read(new File(filePath));
				}
				catch (IOException e)
				{
					invalidFile = true;
					return;
				}
				init(false);
			}
		}
		else
		{
			if(originalImage == null)
			{
				invalidFile = true;
				return;
			}
			
			if(originalImage.getWidth() == availableWidth && originalImage.getHeight() == availableHeight)
			{
				scaledImage = originalImage;
				originalImage.flush();
				originalImage = null;
				return;
			}
			if(calculateDim)
			{
				calculateDimensions(availableWidth, availableHeight);
			}
			boolean higherQuality = false;
			if(originalImage.getWidth() / 2 > scaledWidth || originalImage.getHeight() / 2 > scaledHeight)
			{
				higherQuality = true;
			}
			scaledImage = Thumbnail.getScaledInstance(originalImage, scaledWidth, scaledHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, higherQuality);
			originalImage.flush();
			originalImage = null;
		}
	}

	
	private void calculateDimensions(int availableWidth, int availableHeight)
	{
		if(invalidFile)
		{
			return;
		}
		double scale = 1.0;
		
        int imgHeight = originalImage.getHeight();
        int imgWidth = originalImage.getWidth();

        if(orient == 90 || orient == 270)
        {
        	imgHeight = originalImage.getWidth();
        	imgWidth = originalImage.getHeight();
        }
        
        double adjust = 1.0;
        
        if(availableHeight >= availableWidth)
        {
        	double s1 = (double)availableHeight / (double)imgHeight;
        	double s2 = 1.0;
        	if((s1 * (double)imgWidth) > availableWidth)
        	{
        		s2 = (double)availableWidth / (double)(s1 * (double)imgWidth);
        	}
        	scale = (s1 * s2) * adjust;
        }
        else
        {
        	double s1 = (double)availableWidth / (double)imgWidth;
        	double s2 = 1.0;
        	if((s1 * (double)imgHeight) > availableHeight)
        	{
        		s2 = (double)availableHeight / (double)(s1 * (double)imgHeight);
        	}
        	scale = (s1 * s2) * adjust;
        }
        
        scaledWidth = (int) (scale * imgWidth);
        scaledHeight = (int) (scale * imgHeight);
        if(orient == 90 || orient == 270)
        {
        	scaledWidth = (int) (scale * imgHeight);
        	scaledHeight = (int) (scale * imgWidth);
        }
        
        if(scaledWidth <= 0)
        {
        	scaledWidth = 1;
        }
        if(scaledHeight <= 0)
        {
        	scaledHeight = 1;
        }
	}
	
	public void setParent(Container parent)
	{
		this.parent = parent;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if(invalidFile)
		{
			return;
		}
		Graphics2D g2 = (Graphics2D)g;

		int w = 0;
		int h = 0;
		if(parent == null)
		{
			w = getWidth();  
			h = getHeight();
		}
		else
		{
			w = parent.getWidth();
			h = parent.getHeight();
		}
        /*int imageWidth = scaledWidth;
        int imageHeight = scaledHeight;  
        int x = (w - imageWidth)/2;  
        int y = (h - imageHeight)/2;
		g2.drawImage(scaledImage, x, y, null);*/
		
        AffineTransform at = new AffineTransform();
        
        at.translate(w / 2, h / 2);
        at.rotate(Math.toRadians(orient));
        at.translate(-scaledImage.getWidth()/2, -scaledImage.getHeight()/2);
        
        g2.drawRenderedImage(scaledImage, at);
	}
	
	
	
	public static BufferedImage getScaledInstance(BufferedImage img, int targetWidth, int targetHeight, Object hint, boolean higherQuality)
	{
		/*Image scaled = img.getScaledInstance(targetWidth, targetHeight, BufferedImage.SCALE_FAST);
		BufferedImage buffered = new BufferedImage(targetWidth, targetHeight, img.getType());
		Graphics2D g2 = buffered.createGraphics();
		g2.drawImage(scaled, 0, 0, null);
		g2.dispose();
		return buffered;*/
		int type = (img.getTransparency() == Transparency.OPAQUE) ?
				BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage ret = (BufferedImage)img;
		int w, h;
		if (higherQuality)
		{
			w = img.getWidth();
			h = img.getHeight();
		}
		else
		{
			w = targetWidth;
			h = targetHeight;
		}

		do 
		{
			if (higherQuality && w > targetWidth)
			{
				w /= 2;
				if (w < targetWidth)
				{
					w = targetWidth;
				}
			}

			if (higherQuality && h > targetHeight)
			{
				h /= 2;
				if (h < targetHeight)
				{
					h = targetHeight;
				}
			}

			BufferedImage tmp = new BufferedImage(w, h, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
			g2.drawImage(ret, 0, 0, w, h, null);
			g2.dispose();

			ret = tmp;
		}
		while (w != targetWidth || h != targetHeight);

		return ret;
	}
}