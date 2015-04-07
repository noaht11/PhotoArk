package com.lightark.photoark.imageviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.lightark.MathUtils.Fraction;
import com.lightark.photoark.ImageData;

public class ImagePanel extends JPanel implements ImageObserver
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public boolean invalidFile = false;
	
	private BufferedImage image;
	
	private File imageFile;
	
	private double imageWidth = 0;
	private double imageHeight = 0;
	
	private double orient = 0;
	
	private double scale;
	private double zoomInc;
	
	private JScrollPane rootComponent;
	
	private JPanel errPanel;
	
	private JPanel parent;
	
	private boolean paintString = false;
	private Color stringColor = Color.black;
	
	private ArrayList<String> stringsToPaint = new ArrayList<String>();
	
	public ImagePanel(BufferedImage img, String filePath, JPanel _parent, boolean showError)
	{
		if(showError || img == null || filePath == null)
		{
			invalidFile = true;
			errPanel = new JPanel();
			errPanel.setLayout(new BorderLayout());
			errPanel.setBackground(Color.white);
			JLabel label = new JLabel("<html><i>Image cannot be displayed</i></html>");
			label.setHorizontalAlignment(JLabel.CENTER);
			errPanel.add(label, BorderLayout.CENTER);
			return;
		}
		
		this.imageFile = new File(filePath);
		this.parent = _parent;

		image = img;
        zoomInc = 0.05;
        scale = 1.0;
        
        imageWidth = img.getWidth();
        imageHeight = img.getHeight();
        
		ImageData data = new ImageData(filePath);
		if(!data.invalidFile)
		{
			ExifIFD0Directory exif = data.getMetadata().getFirstDirectoryOfType(ExifIFD0Directory.class);
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
							imageWidth = image.getHeight();
							imageHeight = image.getWidth();
						}
						else if(orientTag == 8)
						{
							orient = 270;
							imageWidth = image.getHeight();
							imageHeight = image.getWidth();
						}
					}
					catch (MetadataException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

        if(image.getWidth() > parent.getWidth() || image.getHeight() > parent.getHeight())
        {
        	calculateScale(parent.getWidth(), parent.getHeight());
        }
        
        rootComponent = new JScrollPane(this);
        rootComponent.setBackground(Color.white);
        rootComponent.setBorder(null);
        rootComponent.setWheelScrollingEnabled(false);
        rootComponent.getHorizontalScrollBar().setUnitIncrement(10);
        rootComponent.getVerticalScrollBar().setUnitIncrement(10);
        
        this.setBackground(Color.white);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        
        this.addMouseWheelListener(new MouseAdapter()
        {
        	@Override
        	public void mouseWheelMoved(MouseWheelEvent evt)
        	{
        		if(evt.getWheelRotation() < 0)
        		{
        			zoomIn();
        		}
        		else if(evt.getWheelRotation() > 0)
        		{
            		zoomOut();
        		}
        	}
        });
        
        this.addMouseListener(new MouseAdapter()
        {
        	@Override
        	public void mouseClicked(MouseEvent evt)
        	{
        		ImagePanel.this.requestFocusInWindow();
        	}
        });
	}
	
	private void calculateScale(int availableWidth, int availableHeight)
	{
		availableWidth = availableWidth - 20;
		availableHeight = availableHeight - 20;
        
        double adjust = 1.0;
        
        if(availableHeight >= availableWidth)
        {
        	double s1 = (double)availableHeight / (double)imageHeight;
        	double s2 = 1.0;
        	if((s1 * (double)imageWidth) > availableWidth)
        	{
        		s2 = (double)availableWidth / (double)(s1 * (double)imageWidth);
        	}
        	scale = (s1 * s2) * adjust;
        }
        else
        {
        	double s1 = (double)availableWidth / (double)imageWidth;
        	double s2 = 1.0;
        	if((s1 * (double)imageHeight) > availableHeight)
        	{
        		s2 = (double)availableHeight / (double)(s1 * (double)imageHeight);
        	}
        	scale = (s1 * s2) * adjust;
        }
	}
	
	public File getImageFile()
	{
		return imageFile;
	}
	
	public JComponent getRootComponent()
	{
		if(!invalidFile)
		{
			return rootComponent;
		}
		else
		{
			return errPanel;
		}
	}

	public void zoomIn()
	{
		setScale(1);
	}
	
	public void zoomOut()
	{
		setScale(-1);
	}
	
	public void zoomToFit()
	{
		calculateScale(parent.getWidth(), parent.getHeight());
		revalidate();
		repaint();
	}
	
	public void zoomTo1()
	{
		scale = 1;
		repaint();
		revalidate();
		scrollToMiddle();
	}
	
	public void scrollToMiddle()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				Rectangle rect = new Rectangle((getWidth() / 2) - (rootComponent.getWidth() / 2), (getHeight() / 2) - (rootComponent.getHeight() / 2), rootComponent.getWidth(), rootComponent.getHeight());
				ImagePanel.this.scrollRectToVisible(rect);
			}
		});
	}
	
    public void setScale(int direction)  
    {
    	double scaleAdd = (double)direction * zoomInc;
    	if((scale + scaleAdd) >= zoomInc)
    	{
    		scale += direction * zoomInc;
    	}
        revalidate();  
        repaint();
    }
    
    public String getHTMLDataString(boolean includeFileName)
    {
    	generateDataString();
    	String result = "<html>";
    	int indexStart = 0;
    	if(!includeFileName)
    	{
    		indexStart = 1;
    	}
    	for(int i = indexStart;i < stringsToPaint.size();i++)
    	{
    		if(i < (stringsToPaint.size() - 1))
    		{
    			result += stringsToPaint.get(i) + "<br>";
    		}
    	}
    	result += "</html>";
    	return result;
    }
    
    public List<String> getDataList()
    {
    	generateDataString();
    	return stringsToPaint;
    }
    
    public String getDataString(String sep)
    {
    	generateDataString();
    	String result = "";
    	for(int i = 0;i < stringsToPaint.size();i++)
    	{
    		if(i < (stringsToPaint.size() - 1))
    		{
    			result += stringsToPaint.get(i) + sep;
    		}
    	}
    	return result;
    }
    
    private void generateDataString()
    {
    	stringsToPaint.clear();
    	stringsToPaint.add(imageFile.getName());
    	ImageData data = new ImageData(imageFile);
    	if(!data.invalidFile)
    	{
    		ExifSubIFDDirectory exif = data.getMetadata().getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    		if(exif != null)
    		{
    			try
    			{
        			if(exif.containsTag(ExifSubIFDDirectory.TAG_SHUTTER_SPEED))
        			{
        				double apex = exif.getDouble(ExifSubIFDDirectory.TAG_SHUTTER_SPEED);
        				double shutterSpeed = Math.pow(2, apex);
        				int roundedShutterSpeed = (int) Math.round(shutterSpeed);
        				Fraction foundFrac = null;
        				try
        				{
        					foundFrac = new Fraction(1, roundedShutterSpeed);
        					stringsToPaint.add(foundFrac.toString() + " sec");
        				}
        				catch(IllegalArgumentException e)
        				{
        					
        				}
        			}
        			
        			if(exif.containsTag(ExifSubIFDDirectory.TAG_APERTURE))
        			{
        				double apex = exif.getDouble(ExifSubIFDDirectory.TAG_APERTURE);
        				double foundFNum = Math.pow(Math.sqrt(2), apex);
        				foundFNum = Math.round(foundFNum * 100.0) / 100.0;
        				stringsToPaint.add(("F" + foundFNum));
        			}
        			
        			if(exif.containsTag(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT))
        			{
        				int foundISO = exif.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
        				stringsToPaint.add("ISO " + foundISO);
        			}
        			
        			if(exif.containsTag(ExifSubIFDDirectory.TAG_FOCAL_LENGTH))
        			{
        				double foundFL = exif.getDouble(ExifSubIFDDirectory.TAG_FOCAL_LENGTH);
        				stringsToPaint.add(foundFL + "mm");
        			}
    			}
    			catch (MetadataException e)
    			{
    				
    			}
    		}
    	}
    }
    
    public void setStringPaintingEnabled(boolean enabled, Color fontColor)
    {
    	paintString = enabled;
    	stringColor = fontColor;
    	generateDataString();
    	repaint();
    }
   
    @Override
    public Dimension getPreferredSize()  
    {
    	int w = (int)(scale * imageWidth);
    	int h = (int)(scale * imageHeight);
        return new Dimension(w, h);  
    }
    
    @Override
    public Point getToolTipLocation(MouseEvent evt)
    {
    	return new Point(evt.getX() + 10, evt.getY() + 10);
    }
    
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);  
        
		Graphics2D g2 = (Graphics2D)g;  
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                
        AffineTransform at = new AffineTransform();
        
        at.translate(getWidth() / 2, getHeight() / 2);
        at.rotate(Math.toRadians(orient));
        at.scale(scale, scale);
        at.translate(-image.getWidth()/2, -image.getHeight()/2);
        
        g2.drawRenderedImage(image, at);
        
        if(paintString)
        {
        	if(stringColor != null)
        	{
        		g2.setColor(stringColor);
        	}
        	else
        	{
        		g2.setColor(Color.black);
        	}
        	Font textFont = new Font("Tahoma", Font.PLAIN, 15);
        	g2.setFont(textFont);
        	int fontHeight = g2.getFontMetrics(textFont).getHeight();
        	int top = fontHeight + 5;
        	for(String s : stringsToPaint)
        	{
        		g2.drawString(s, 5, top);
        		top += (fontHeight + 5);
        	}
        }        
        g2.dispose();
	}
	
	@Override
	public boolean imageUpdate(Image image, int infoFlags, int x, int y, int width, int height)
	{
		repaint();
		return true;
	}
	
}