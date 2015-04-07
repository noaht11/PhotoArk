package com.lightark.photoark.imageviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.lightark.photoark.ImageData;

public class MetadataPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private File image;
	private ImageData imageData;
	
	private JScrollPane rootComponent;
		
	public MetadataPanel(String imagePath)
	{
		this(new File(imagePath));
	}
	
	public MetadataPanel(File _image)
	{
		this.image = _image;
		this.imageData = new ImageData(image);

		
		if(imageData.invalidFile)
		{
			this.setLayout(new BorderLayout());
			
			JLabel label = new JLabel("<html><i>Image format does not support metadata</i></html>");
			label.setHorizontalAlignment(JLabel.CENTER);
			this.add(label, BorderLayout.CENTER);
		}
		else
		{
			this.setLayout(new BorderLayout());
			
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			
			//this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
						
			Font font = new Font("Tahoma", Font.BOLD + Font.ITALIC, 13);
			int count = 0;
			for(Directory d : imageData.getMetadata().getDirectories())
			{
				if(d.getTagCount() > 0)
				{
					JPanel titleContainer = new JPanel();
					titleContainer.setBackground(Color.white);
					titleContainer.setLayout(new BorderLayout());
					
					JLabel title = new JLabel(d.getName() + "   ");
					title.setFont(font);
					
					int topPadding = 0;
					if(count > 0)
					{
						topPadding = 20;
					}
					title.setBorder(BorderFactory.createEmptyBorder(topPadding, 0, 5, 0));
					
					titleContainer.add(title, BorderLayout.LINE_START);
					
					contentPanel.add(titleContainer);
					
					contentPanel.add(createTable(d));
				}
				count++;
			}
			
			this.add(contentPanel, BorderLayout.PAGE_START);
		}
		
        rootComponent = new JScrollPane(this);
        rootComponent.setBackground(Color.white);
        rootComponent.setBorder(null);
        rootComponent.getHorizontalScrollBar().setUnitIncrement(10);
        rootComponent.getVerticalScrollBar().setUnitIncrement(10);

        this.setBackground(Color.white);
	}
	
	private JTable createTable(Directory d)
	{
		String[] columns = new String[]{"Tag", "Value"};
		
		Object[][] data = new Object[d.getTagCount()][2];
		
		int i = 0;
		for(Tag t : d.getTags())
		{
			data[i][0] = t.getTagName();
			data[i][1] = t.getDescription();
			i++;
		}
		
		TableModel model = new DefaultTableModel(data, columns)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		
		JTable table = new JTable(model);
		table.setFocusable(false);
		table.setRowSelectionAllowed(false);
		return table;
	}

	public File getImageFile()
	{
		return image;
	}
	
	public JScrollPane getRootComponent()
	{
		return rootComponent;
	}
	
}