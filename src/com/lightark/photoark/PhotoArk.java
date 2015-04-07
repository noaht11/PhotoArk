package com.lightark.photoark;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.lightark.FileUtils.FileNames;
import com.lightark.Thread.ThreadManager;
import com.lightark.criteria.ApertureSC;
import com.lightark.criteria.CustomSC;
import com.lightark.criteria.DateTakenSC;
import com.lightark.criteria.FileSC;
import com.lightark.criteria.FlashSC;
import com.lightark.criteria.FocalLengthSC;
import com.lightark.criteria.IptcSC;
import com.lightark.criteria.IsoSC;
import com.lightark.criteria.ShutterSpeedSC;
import com.lightark.license.DefaultLicenseDialog;
import com.lightark.license.DefaultLicenseVerifier;
import com.lightark.license.License;
import com.lightark.license.LicenseAcceptanceListener;
import com.lightark.license.LicenseManager;
import com.lightark.license.LicenseVerifier;
import com.lightark.photoark.imagesearch.SearchCriterion;
import com.lightark.photoark.imageviewer.ImageViewer;

public class PhotoArk
{
	public final static String appName = "PhotoArk";
	public final static String version = "V3.1";
	public final static String updateFlagLocation = "http://light-ark.appspot.com/res/PhotoArk/updateFlag.txt";
	public final static String downloadPageLocation = "http://light-ark.appspot.com/photoark.php";
	public final static String homePageLocation = "http://light-ark.appspot.com/photoark.php";
	public final static String tutorialPageLocation = "http://light-ark.appspot.com/photoark.php";
	public static boolean updateAvailable = false;
	public static String updateVersion = "";
	public static boolean updateCheckFailed = false;

	public static final String[] imageExtensions = new String[]{".jpg",".jpeg",".png",".gif",".bmp"};
	
	private static UpdateChecker uc = null;
	
	public static ArrayList<JFrame> openFrames = new ArrayList<JFrame>();
	
	public static ThreadManager threadManager = new ThreadManager();
	
	public static ArrayList<SearchCriterion> criteriaExtensions = new ArrayList<SearchCriterion>();
	
	public static List<Image> appIcons = new ArrayList<Image>();
	
	public static LicenseManager licenseManager;
	public static DefaultLicenseDialog licenseDialog;
	public static LicenseAcceptanceListener licenseListener;
	
	private static String[] launchArgs;
	
	public static void exitApp()
	{
		for(JFrame frame : openFrames)
		{
			frame.dispose();
		}
		for(Window w : Window.getWindows())
		{
			w.dispose();
		}
		threadManager.terminateAllThreads();
		//System.exit(0);
	}
	
	public static void main(String args[]) throws BackingStoreException
	{
		launchArgs = args;
		
		LicenseVerifier lv = new DefaultLicenseVerifier();
		try
		{
			licenseManager = new LicenseManager(LicenseManager.DEFAULT_LICENSE_FILE, ResourceLoader.loadResource("License/decrypt.key"), lv);
			licenseManager.initializePrefs(PhotoArk.class);
			licenseManager.loadLicenseFromPref();
		}
		catch (ClassNotFoundException | IOException e)
		{
			exitApp();
		}
		
		initGUI();
		
		//initApplication(); //Perform License Check
		launchApplication(); //Don't Perform License Check
	}
	
	public static void initGUI()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e)
		{
			
		}
		catch (InstantiationException e)
		{
			
		}
		catch (IllegalAccessException e)
		{
			
		}
		catch (UnsupportedLookAndFeelException e)
		{
			
		}
		ToolTipManager.sharedInstance().setInitialDelay(100);

		appIcons.add(new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_256.png")).getImage());
		appIcons.add(new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_128.png")).getImage());
		appIcons.add(new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_64.png")).getImage());
		appIcons.add(new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_48.png")).getImage());
		appIcons.add(new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_32.png")).getImage());
		appIcons.add(new ImageIcon(ResourceLoader.loadResource("Resources/Icons/Icon_16.png")).getImage());
	}
	
	public static void initApplication()
	{
		licenseDialog = new DefaultLicenseDialog(licenseManager, appIcons);
		licenseListener = new LicenseAcceptanceListener()
		{
			@Override
			public void licenseAccepted(LicenseManager lm, License l)
			{
				lm.savePrefs();
				launchApplication();
			}
		};
		licenseDialog.addLicenseAcceptanceListener(licenseListener);
		
		licenseCheck();
	}
	
	public static void launchApplication()
	{
		final String filepath;
		if(launchArgs != null && launchArgs.length == 1)
		{
			filepath = launchArgs[0];
		}
		else
		{
			filepath = null;
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				criteriaExtensions.add(new DateTakenSC().newInstance());
				criteriaExtensions.add(new ShutterSpeedSC().newInstance());
				criteriaExtensions.add(new ApertureSC().newInstance());
				criteriaExtensions.add(new FocalLengthSC().newInstance());
				criteriaExtensions.add(new IsoSC().newInstance());
				criteriaExtensions.add(new FlashSC().newInstance());
				criteriaExtensions.add(new IptcSC().newInstance());
				criteriaExtensions.add(new FileSC().newInstance());
				criteriaExtensions.add(new CustomSC().newInstance());
				try
				{
					uc = new UpdateChecker(new URL(PhotoArk.updateFlagLocation));
					PhotoArk.updateAvailable = uc.checkForUpdate();
					if(PhotoArk.updateAvailable)
					{
						PhotoArk.updateVersion = uc.getLatestVersionString();
					}
				}
				catch (MalformedURLException e)
				{
					updateCheckFailed = true;
				}
				catch (IOException e)
				{
					updateCheckFailed = true;
				}
				
				JFrame frame = null;
				if(filepath == null)
				{
					frame = new LaunchFrame();
					frame.setVisible(true);
				}
				else
				{
					File openFile = new File(filepath);
					if(openFile.exists())
					{
						String fileExt = FileNames.getExtension(openFile).toUpperCase();
						boolean validImageFile = false;
						for(String s : imageExtensions)
						{
							if(s.toUpperCase().endsWith(fileExt))
							{
								validImageFile = true;
							}
						}
						
						if(!validImageFile)
						{
							JOptionPane.showMessageDialog(null, appName + " can only open " + (Arrays.toString(imageExtensions).substring(1, Arrays.toString(imageExtensions).length() - 1)) + " files.", "Invalid File", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						File[] filesInFolder = openFile.getParentFile().listFiles();
						String[] filePathsInFolder = new String[filesInFolder.length];
						for(int i = 0;i < filesInFolder.length;i++)
						{
							filePathsInFolder[i] = filesInFolder[i].getAbsolutePath();
						}
						String folder = openFile.getParentFile().getAbsolutePath();
						int index = Arrays.asList(filePathsInFolder).indexOf(openFile.getAbsolutePath());
						
						frame = new ImageViewer(new String[]{}, index, folder, false);
						frame.setVisible(true);
					}
					else
					{
						frame = new LaunchFrame();
						frame.setVisible(true);
					}
					openFrames.add(frame);
				}
				
				if(updateAvailable)
				{
					String message = "An update for " + appName + " is available.\n\n You're current version: " + version + "\nAvailable for download: " + updateVersion + "\n\nTo update immediately click on \"Download Now\".\nOnce your default browser opens to the " + appName + " downloads page, follow the instructions for updating.";

					int result = JOptionPane.showOptionDialog(frame, message, "Update Available", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Download Now", "Remind Me Later"}, 0);
					if(result == 0)
					{
						try
						{
							Desktop.getDesktop().browse(new URI(downloadPageLocation));
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						catch (URISyntaxException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		});
	}
	
	public static void licenseCheck()
	{
		if(licenseManager.verifyLicense())
		{
			licenseManager.savePrefs();
			launchApplication();
		}
		else
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					licenseDialog.setVisible(true);
				}
			});
		}
	}
}