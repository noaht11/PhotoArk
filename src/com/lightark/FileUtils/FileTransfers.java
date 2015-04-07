package com.lightark.FileUtils;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.lightark.photoark.PhotoArk;

interface FileTransferMonitor
{
	public void transferStarted();
	public void fileTransfered(File source, File target);
	public void transferCancelled();
	public void transferComplete();
}

interface FileCopyMonitor extends FileTransferMonitor
{
	public void fileExists(File source, File target);
}

public class FileTransfers
{
	public static class Copy
	{
		private ArrayList<FileCopyMonitor> monitors = new ArrayList<FileCopyMonitor>();
		
		private SwingWorker<Integer, Integer> copier;
		private CopyDialog copyDialog;
		
		private boolean showDialog;

		private long bytesToCopy = 0;
		
		private long totalBytesCopied = 0;
		private int nearestLargeUnit = 1;
		private long byteMultiplier = (long) Math.pow(1024, 2);
		
		private boolean immediateCancel = false;
		
		public Copy(final File[] filesToCopy, final File targetFolder, Component dialogContainer, boolean _showDialog)
		{
			this.showDialog = _showDialog;
			
			this.bytesToCopy = FileSize.getCombinedFileSize(filesToCopy);
			
			FileSize fileSize = FileSize.getFileSizeInBestUnits(bytesToCopy);
			copyDialog = new CopyDialog(dialogContainer, PhotoArk.appIcons, new File(""), targetFolder, filesToCopy.length, fileSize);
			copyDialog.setProgressBarMax((int)(Math.ceil(FileSize.getFileSizeAs(bytesToCopy, FileSize.UNIT_MB))));
			copier = new SwingWorker<Integer, Integer>()
			{
				@Override
				public Integer doInBackground()
				{								
					int existAction = CopyDialog.ASK;
					boolean resetToAsk = true;
					for(File source : filesToCopy)
					{
						if(isCancelled())
						{
							for(FileCopyMonitor fcm : monitors)
							{
								fcm.transferCancelled();
							}
							return -1;
						}
						File target = new File(targetFolder.getAbsolutePath() + "\\" + source.getName());
						copyDialog.updateCurrentFile(source.getName());
						if(!target.exists())
						{
							copyFile(source, target);
						}
						else
						{
							if(showDialog)
							{
								String fullRename = FileNames.generateRename(targetFolder, target);
								
								if(existAction == CopyDialog.ASK)
								{
									int[] results = copyDialog.showFileExistsDialog(target.getName(), targetFolder.getName(), fullRename);
									if(results[1] == 1)
									{
										resetToAsk = false;
									}
									existAction = results[0];
								}
								
								if(existAction == CopyDialog.COPY_AND_REPLACE)
								{
									target.delete();
									copyFile(source, target);
								}
								else if(existAction == CopyDialog.COPY_AND_RENAME)
								{
									target = new File(targetFolder.getAbsolutePath() + "\\" + fullRename);
									copyFile(source, target);
								}
								else if(existAction == CopyDialog.DONT_COPY)
								{
									
								}
								
								if(resetToAsk)
								{
									existAction = CopyDialog.ASK;
								}
							}
							else
							{
								for(FileCopyMonitor fcm : monitors)
								{
									fcm.fileExists(source, target);
								}
							}
						}
					}
					return 0;
				}
				
				@Override
				public void done()
				{
					if(showDialog)
					{
						copyDialog.dispose();
					}
					for(FileCopyMonitor fcm : monitors)
					{
						fcm.transferComplete();
					}
					JOptionPane.showMessageDialog(copyDialog.getComponentBelow(), "The files have been copied to the selected folder.", "Copy Complete", JOptionPane.INFORMATION_MESSAGE);
				}
			};
			copyDialog.addCancelListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent evt)
				{
					copier.cancel(true);
				}
			});
			copyDialog.addImmCancelListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent evt)
				{
					immediateCancel = true;
					copier.cancel(true);
				}
			});
			
		}

		private void copyFile(File source, File target)
		{
			/*try
			{
				Files.copy(source.toPath(), target.toPath());
				for(FileCopyMonitor fcm : monitors)
				{
					fcm.fileTransfered(source, target);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}*/
			if(target.exists())
			{
				return;
			}
			
			BufferedInputStream inStream = null;
			BufferedOutputStream outStream = null;
			try
			{
				try
				{
					target.createNewFile();
					
					inStream = new BufferedInputStream(new FileInputStream(source));
					outStream = new BufferedOutputStream(new FileOutputStream(target));

					byte[] buffer = new byte[1024 * 1024];
					int bytesRead = 0;
					while(bytesRead != -1)
					{
						if(immediateCancel)
						{
							if(inStream != null)
							{
								inStream.close();
							}
							if(outStream != null)
							{
								outStream.close();
							}
							return;
						}
						bytesRead = inStream.read(buffer);
						totalBytesCopied += bytesRead;
						if(bytesRead > 0)
						{
							outStream.write(buffer, 0, bytesRead);
						}
						
						if(totalBytesCopied >= (nearestLargeUnit * byteMultiplier))
						{
							copyDialog.updateProgressBar(1);
							nearestLargeUnit++;
						}
					}

					for(FileCopyMonitor fcm : monitors)
					{
						fcm.fileTransfered(source, target);
					}
					copyDialog.updateFrom(source.getParentFile());
					copyDialog.updateNumberRemaining(-1, FileSize.getFileSizeInBestUnits((bytesToCopy - totalBytesCopied)));
				}
				finally
				{
					if(inStream != null)
					{
						inStream.close();
					}
					if(outStream != null)
					{
						outStream.close();
					}
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}

		public void addFileCopyMonitor(FileCopyMonitor fcm)
		{
			monitors.add(fcm);
		}
		
		public void removeFileCopyMonitor(FileCopyMonitor fcm)
		{
			monitors.remove(fcm);
		}
		
		public void startCopy()
		{
			copier.execute();
			if(showDialog)
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						copyDialog.setVisible(true);
					}
				});
			}
			for(FileCopyMonitor fcm : monitors)
			{
				fcm.transferStarted();
			}
		}
	}
}