/*
Copyright (c) 2008-2010 Daniel Marbach & Thomas Schaffter

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper(s) listed
on http://gnw.sourceforge.net.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package ch.epfl.lis.gnwgui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnwgui.idesktop.IBin;
import ch.epfl.lis.gnwgui.idesktop.IDesktop;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.idesktop.IFolder;
import ch.epfl.lis.gnwgui.windows.Wait;
import ch.epfl.lis.utilities.IO;
import ch.epfl.lis.utilities.Unzipper;
import ch.epfl.lis.utilities.Zipper;
import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterGnwDesktopZIP;

/**
 * Extends a iDesktop to display open, bin and network icons.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class NetworkDesktop extends IDesktop
{
	/** Import item */
	private IElement import_ = null;
	/** New Folder item */
	private IElement newFolder_ = null;
	/** Export network desktop content */
	private IElement exportDesktop_ = null;
	/** Instance of the bin which will be placed on the desktop. */
	private IBin bin_ = null;

	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(NetworkDesktop.class.getName());

	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	/**
	 * Constructor
	 * @param name Name of the desktop
	 */
	public NetworkDesktop(String name) {
		super(name);
		init();
	}

	// ----------------------------------------------------------------------------

	@SuppressWarnings("serial")
	public void init() {

		GnwGuiSettings global = GnwGuiSettings.getInstance();
		
		/**
		 * Create and initialize an item which will be used to import the
		 * networks onto the desktop, and finally place it on the desktop.
		 */
		import_ = new IElement("Open", this) {

			@Override
			public void leftMouseButtonInvocationSimple()
			{	
				IONetwork.open();
			}
			public void wheelMouseButtonInvocation()
			{
				IONetwork.open();
			}

			public void rightMouseButtonInvocation() {
				
				IONetwork.open();
			}

			protected void leftMouseButtonInvocationDouble() {
				
				IONetwork.open();
			}
			
			public IElement copyElement() {return null;}
		};
		import_.setItemIcon(new ImageIcon(global.getImportNetworkIcon()).getImage());
		import_.setDestroyable(false);
		import_.setToolTipText("<html>Import a network structure or a kinetic network model</html>");
		addItemOnDesktop(import_);
		
		newFolder_ = new IElement("New Folder",this) {
			protected void wheelMouseButtonInvocation() {}
			protected void rightMouseButtonInvocation() {
				NewFolder nf = new NewFolder(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				nf.setVisible(true);
			}
			
			protected void leftMouseButtonInvocationSimple() {
				NewFolder nf = new NewFolder(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				nf.setVisible(true);
			}
			protected void leftMouseButtonInvocationDouble() {
				NewFolder nf = new NewFolder(GnwGuiSettings.getInstance().getGnwGui().getFrame());
				nf.setVisible(true);
			}
			@Override
			public IElement copyElement() {return null;}
		};
		newFolder_.setItemIcon(new ImageIcon(global.getFolderIcon()).getImage());
		newFolder_.setDestroyable(false);
		newFolder_.setToolTipText("<html>Create a new folder");
		addItemOnDesktop(newFolder_);
		
		exportDesktop_ = new IElement("Export Desktop", this)
		{
			protected void wheelMouseButtonInvocation() {}
			protected void rightMouseButtonInvocation()
			{
				exportDesktopInterface();
			}
			
			protected void leftMouseButtonInvocationSimple()
			{
				exportDesktopInterface();
			}
			protected void leftMouseButtonInvocationDouble()
			{
				exportDesktopInterface();
			}
			@Override
			public IElement copyElement() {return null;}
		};
		exportDesktop_.setItemIcon(new ImageIcon(global.getExportNetworkDesktopImage()).getImage());
		exportDesktop_.setDestroyable(false);
		exportDesktop_.setToolTipText("<html>Export all the networks on the desktop to a ZIP archive</html>");
		addItemOnDesktop(exportDesktop_);
		
		
		/**
		 * Create and initialize a bin and place it on the desktop.
		 */
		bin_ = new IBin(this, "Recycle Bin");
		bin_.setEmptyIcon(new ImageIcon(global.getBinEmptyIcon()).getImage());
		bin_.setFilledIcon(new ImageIcon(global.getBinFullIcon()).getImage());
		bin_.setToolTipText("Drag-and-drop networks to delete (cannot be undone)");
		addItemOnDesktop(bin_);

	

		displayOptionsDialog(desktopPanel_);
		displayRenameDialog(desktopPanel_);
		displayExtractionDialog(desktopPanel_);
		displayVisualizationDialog(desktopPanel_);
		displayOpenDialog(desktopPanel_);
		displaySaveDialog(desktopPanel_);
		displayBenchmarkDialog(desktopPanel_);
		displayKineticModelGenerationDialog(desktopPanel_);
	}
	
	// ----------------------------------------------------------------------------
	
	public void exportDesktopInterface()
	{
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Export Desktop",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

		dialog.addFilter(new FilterGnwDesktopZIP());

		dialog.setAcceptAllFileFilterUsed(false);
		dialog.setSelection("gnw_desktop.zip");
		dialog.display();
		
		if (dialog.getSelection() != null)
		{
			log_.log(Level.INFO, "Export desktop");

			String selection = dialog.getSelection();
			String absPath = FilenameUtilities.getDirectory(selection) + "/";
			String zipFilename = FilenameUtilities.getFilenameWithoutPath(selection);
			zipFilename = Zipper.appendZipExtensionIfRequired(zipFilename);
			
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Export desktop");
			DesktopExport de = new DesktopExport(wait);
			
			de.setAbsPath(absPath);
			de.setZipFilename(zipFilename);
			
			de.execute();
			wait.start();
			
			log_.log(Level.INFO, "All the networks have been saved to the ZIP archive " + absPath + zipFilename);
			log_.log(Level.INFO, "Done");
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void importDesktopInterface(String zipFileAbsPath)
	{
		log_.log(Level.INFO, "Import desktop");
		
		importDesktop(zipFileAbsPath);
		
		log_.log(Level.INFO, "Done");
	}
	
	// ----------------------------------------------------------------------------
	
	public void exportDesktop(String absPath, String zipFilename)
	{
		try
		{			
			String tmp = absPath + "gnw_desktop_tmp";
			File tmpFolder = new File(tmp);
			tmpFolder.mkdir();
			
			tmp = tmp + "/gnw_desktop";
			File desktopFolder = new File(tmp);
			desktopFolder.mkdir();
			
			NetworkDesktopMap map = new NetworkDesktopMap(this);
			map.encode(tmp + "/map.xml");
			
			ArrayList<IElement> list = content_.get(0);

			for (IElement element : list)
				saveDesktopRecursively(element, tmp + "/", map);
			
			String zipFile = absPath + zipFilename;
			Zipper.zipFolder(tmp, zipFile);
			System.gc();
			if(!IO.deleteFolder(tmpFolder))
				throw new Exception("Unable to delete tmp folder " + tmpFolder.getAbsolutePath());
			
		}
		catch(Exception e)
		{
			log_.log(Level.WARNING, "NetworkDesktop::exportDesktop(): " + e.getMessage(), e);
		} 
	}
	
	// ----------------------------------------------------------------------------
	
	public void importDesktop(String zipFileAbsPath)
	{
		try
		{
			// log_.log(Level.INFO, "Empty desktop");
			emptyDesktop();
			
			// log_.log(Level.INFO, "Extract networks from zip archive");
			String destAbsPath = FilenameUtilities.getDirectory(zipFileAbsPath) + "/";
			String tmp = destAbsPath + "gnw_desktop_tmp";
			File tmpFolder = new File(tmp);
			tmpFolder.mkdir();
			
	        Unzipper unzip = new Unzipper();
	        String unZipFile = zipFileAbsPath;  
	        String unZipOutFolder = tmp + "/";
	        unzip.recursiveUnzip(new File(unZipFile), new File(unZipOutFolder));
	       
			// log_.log(Level.INFO, "Load networks");
			File gnwDesktop = new File(tmp + "/gnw_desktop");
			if (gnwDesktop.exists() && gnwDesktop.isDirectory())
			{
				NetworkDesktopMap map = new NetworkDesktopMap(this);
				map.decode(tmp + "/gnw_desktop/map.xml");
				
				refreshDesktop();
			}
			else
				throw new Exception("Unable to access " + gnwDesktop.getAbsolutePath());
			
			System.gc();
			// remove extracted files
			if(!IO.deleteFolder(tmpFolder))
				throw new Exception("Unable to delete tmp folder " + tmpFolder.getAbsolutePath());
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "NetworkDesktop::importDesktop(): " + e.getMessage(), e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public void loadDesktopRecursively(String path, IFolder folder) throws Exception
	{
		File file = new File(path);
		
		if (!file.exists())
			throw new Exception("File/folder " + file.toString() + " doesn't exist");
		
		String name = FilenameUtilities.getFilenameWithoutExtension(path);
		URL url = GnwSettings.getInstance().getURL(path);
		
		if (file.isFile())
		{	
			Integer format = null;
			
			if ( (format = IONetwork.isStructureExtension(url, null)) != null)
			{
				StructureElement element = IONetwork.loadStructureItem(name, url, format); // TODO
				IONetwork.printOpeningInfo(element);
				
				if (folder == null)
					this.addItemOnDesktop(element);
				else
					this.addItemOnDesktop(element, folder);
				
			}
			else if ( (format = IONetwork.isDynamicalNetworkExtension(url, null)) != null)
			{
				DynamicalModelElement element = IONetwork.loadDynamicNetworkItem(name, url, format);
				IONetwork.printOpeningInfo(element);
				
				if (folder == null)
					this.addItemOnDesktop(element);
				else
					this.addItemOnDesktop(element, folder);
			}
		}
		else if (file.isDirectory())
		{
			String[] children = file.list();
			java.util.Arrays.sort(children, String.CASE_INSENSITIVE_ORDER);
			IFolder newFolder = null;
			
			if (name != "gnw_desktop")
				newFolder = new IFolder(name, this);
			
			if (folder == null)
				addItemOnDesktop(newFolder);
			else
				folder.addChild(newFolder);
			
			for (int i = 0; i < children.length; i++)
				loadDesktopRecursively(path + "/" + children[i], newFolder);
		}
	}
	
	// ----------------------------------------------------------------------------

	public void saveDesktopRecursively(IElement element, String root, NetworkDesktopMap map) throws Exception
	{
		if (element instanceof StructureElement)
		{
			URL url = GnwSettings.getInstance().getURL(root + map.getNextFilename());
			IONetwork.exportTSVStructure((StructureElement) element, url);
		}
		else if (element instanceof DynamicalModelElement)
		{
			URL url = GnwSettings.getInstance().getURL(root + map.getNextFilename());
			IONetwork.exportSBMLGeneRegulatoryNetwork((DynamicalModelElement) element, url);
		}
		
		if (element.hasChildren() || element instanceof IFolder)
		{
//			File f = new File(root + element.getLabel());
//			f.mkdir();
			
			for (int i = 0; i < element.getChildren().size(); i++)
				saveDesktopRecursively(element.getChildren().get(i), root, map);
				//saveDesktopRecursively(element.getChildren().get(i), root + element.getLabel() + "/", map);
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * Action to execute when an item is released. For instance, if the item is released on
	 * the bin and is destroyable, the item is remove from the desktop.
	 */
	public void itemReleased(IElement item) {
		Folder folder = null;
		boolean movedToAFolder = false;
		if (isItemOnAnother(item, bin_)) {
			if (!item.equals(bin_) && item.isDestroyable()) {
				bin_.addItemIntoBin(item);
				removeItemFromDesktop(item); // Step2: Remove the item from the desktop
				return;
			}
		}
		
		//Check for folder move
		IElement oldDisplayChildrenOf = getDisplayChildrenOf();
		for(int i=0;i < content_.size();i++) {
			for(int j=0;j< content_.get(i).size();j++) {
				if ( content_.get(i).get(j).getClass() == Folder.class && content_.get(i).get(j) != item) {
					if( !item.getChildren().contains(content_.get(i).get(j)) && item.getFather() != content_.get(i).get(j) && isItemOnAnother(item, content_.get(i).get(j))) {
						folder = (Folder)content_.get(i).get(j);
						
						moveItemToFolder(item, folder);
						movedToAFolder = true;
						displayChildrenOf(folder);
						displayChildrenOf(oldDisplayChildrenOf);
						if ( getDisplayChildrenOf() == item) {
							displayChildrenOf(folder);
						}
						return;
					}
				}
			}
		}

		if ( !isItemOnAnother(item) && !movedToAFolder && isItemOnColumn(item) != getElementPosition(item).x && isItemOnColumn(item) >= 0) {
			int col = isItemOnColumn(item);
			//log_.log(Level.INFO,"Moving " + item.getLabel() + " from column " + getElementPosition(item).x + " to column " + isItemOnColumn(item));

			if (col == 0) {
				content_.get(0).add(item);
				if ( item.getFather() != null)
					item.getFather().getChildren().remove(item);
				
				item.setFather(null);
			}
			else {
				if ( content_.get(col).get(0).getFather() != item) {
					if ( item.getFather() != null)
						item.getFather().getChildren().remove(item);
					IElement father = content_.get(col).get(0).getFather();
					
					//log_.log(Level.INFO,"New father: " + father.getLabel());
					content_.get(getElementPosition(item).x).remove(item);
					item.setFather(father);
					father.addChild(item);
					
					if( father != null)
						displayChildrenOf(father);
				}
			}
		}
		if( getDisplayChildrenOf() != null )			
			displayChildrenOf(getDisplayChildrenOf());
		
		repaintDesktop();
		desktopPanel_.repaint();		
	}

	
	/**
	 * Move to a folder
	 */
	
	public void moveItemToFolder(IElement item, IFolder folder) {
		
		if ( item.getFather()!=null) {
			
			item.getFather().getChildren().remove(item);
		}		
		Point pos = getElementPosition(item);
		
		if (pos == null)
			return;
		
		int c = (int) pos.getX();
		content_.get(c).remove(item);
		
		
		folder.addChild(item);
		
		item.setFather(folder);
		//GnwGuiSettings.getInstance().getNetworkDesktop().displayChildrenOf(folder);
	}

	// ----------------------------------------------------------------------------

	/**
	 * Show the options window when the user presses ENTER on an element from
	 * the desktop.
	 */
	@SuppressWarnings("serial")
	public void displayOptionsDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_OPTIONS_DIALOG");
		jp.getActionMap().put("DISPLAY_OPTIONS_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				if ( getNumberOfSelectedElements() == 1)
					displayOptionsDialog();
				else
					multipleElementsSelectionMenu();
			}
		});
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to do when F2 or R are pressed.
	 */
	@SuppressWarnings("serial")
	public void displayRenameDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		KeyStroke k2 = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_RENAME_DIALOG");
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k2, "DISPLAY_RENAME_DIALOG2");

		jp.getActionMap().put("DISPLAY_RENAME_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayRenameDialog();
			}
		});

		jp.getActionMap().put("DISPLAY_RENAME_DIALOG2", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayRenameDialog();
			}
		});
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to do when E is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayExtractionDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_EXTRACTION_DIALOG");
		jp.getActionMap().put("DISPLAY_EXTRACTION_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayExtractionDialog();
			}
		});
	}
	
	
	// ----------------------------------------------------------------------------

	/**
	 * Action to do when K is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayKineticModelGenerationDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_K, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_KINETIC_MODEL_GENERATION_DIALOG");
		jp.getActionMap().put("DISPLAY_KINETIC_MODEL_GENERATION_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayKineticModelGenerationDialog();
			}
		});
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to do when V is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayVisualizationDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_V, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_VISUALIZATION_DIALOG");
		jp.getActionMap().put("DISPLAY_VISUALIZATION_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayVisualizationDialog();
			}
		});
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to do when O (letter) is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayOpenDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_O, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_OPEN_DIALOG");
		jp.getActionMap().put("DISPLAY_OPEN_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayOpenDialog();
			}
		});
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to do when S is pressed.
	 */
	@SuppressWarnings("serial")
	public void displaySaveDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_SAVE_DIALOG");
		jp.getActionMap().put("DISPLAY_SAVE_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displaySaveDialog();
			}
		});
	}


	// ----------------------------------------------------------------------------

	/**
	 * Action to do when B is pressed.
	 */
	@SuppressWarnings("serial")
	public void displayBenchmarkDialog(JComponent jp) {
		KeyStroke k = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
		jp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(k, "DISPLAY_BENCHMARK_DIALOG");
		jp.getActionMap().put("DISPLAY_BENCHMARK_DIALOG", new AbstractAction() {
			public void actionPerformed(ActionEvent arg0) {
				displayBenchmarkDialog();
			}
		});
	}

	// ----------------------------------------------------------------------------

	/**
	 * Calls the options window of the selected element.
	 */
	public void displayOptionsDialog() {
		IElement element = IElement.curItem;

		if (element == import_) {
			IONetwork.open();
			return;
		}

		if (element != null && element != bin_) {
			Options dialog = null;
			if (element instanceof StructureElement)
				dialog = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				dialog = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (DynamicalModelElement) element);
			else
				dialog = null;

			if (dialog != null) {
				dialog.setVisible(true);
				return;
			}
		}
	}


	// ----------------------------------------------------------------------------

	/**
	 * Open the dialog "Rename" if the selected item is a network.
	 */
	public void displayRenameDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			Rename dialog = null;
			if (element instanceof StructureElement)
				dialog = new Rename(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				dialog = new Rename(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (DynamicalModelElement) element);
			else if (element instanceof IFolder)
				dialog = new Rename(GnwGuiSettings.getInstance().getGnwGui().getFrame(), element);
			else
				dialog = null;

			if (dialog != null) {
				dialog.setVisible(true);
				return;
			}
		}
	}


	// ----------------------------------------------------------------------------

	/**
	 * Open the dialog "Subnet Extractions" if the selected item is a network.
	 */
	public void displayExtractionDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			if (element instanceof StructureElement)
				Options.subnetworkExtraction((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				Options.subnetworkExtraction((DynamicalModelElement) element);
		}
	}
	
	// ----------------------------------------------------------------------------

	
	public void displayKineticModelGenerationDialog() {
		IElement element = IElement.curItem;
		Options options = null;
		
		if (element instanceof StructureElement)
			options = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (StructureElement) element);
		else if (element instanceof DynamicalModelElement)
			options = new Options(GnwGuiSettings.getInstance().getGnwGui().getFrame(), (DynamicalModelElement) element);
		
		if (options != null)
			options.conversion2DynamicalModel();
	}


	// ----------------------------------------------------------------------------

	/**
	 * Open the "Visualization" dialog if the selected item is a network.
	 */
	public void displayVisualizationDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_) {
			if (element instanceof StructureElement)
				Options.viewNetwork((StructureElement) element);
			else if (element instanceof DynamicalModelElement)
				Options.viewNetwork((DynamicalModelElement) element);
		}
	}


	// ----------------------------------------------------------------------------

	/**
	 * Open the "Opening network" dialog.
	 */
	public void displayOpenDialog() {
		IONetwork.open();
	}


	// ----------------------------------------------------------------------------

	/** Open the "Saving network" dialog. */
	public void displaySaveDialog()
	{
		IElement element = IElement.curItem;

		if (getNumberOfSelectedElements() == 1)
		{
			if (element != null && element != import_ && element != bin_ && element.getClass() != Folder.class)
			{
				if (element instanceof StructureElement)
					IONetwork.saveAs((StructureElement) element);
				else if (element instanceof DynamicalModelElement)
					IONetwork.saveAs((DynamicalModelElement) element);
			}
			else if ( element.getClass() == Folder.class)
				IONetwork.saveAllSected();
		}
		else if (getNumberOfSelectedElements() > 1 )
		{
			IONetwork.saveAllSected();
		}
	}

	// ----------------------------------------------------------------------------

	/**
	 * Open the "Benchmark generator" dialog if the selected item is a dynamical model.
	 */
	public void displayBenchmarkDialog() {
		IElement element = IElement.curItem;
		if (element != null && element != import_ && element != bin_ && element instanceof DynamicalModelElement) {
			try {
				Options.generateDREAM3GoldStandard((DynamicalModelElement) element);
			} catch (Exception e) {
				log_.log(Level.WARNING, "NetworkDesktop::displayBenchmarkDialog(): " + e.getMessage(), e);
			}
		}
	}


	public void multipleElementsSelectionMenu()
	{
//		Multi m = new Multi();	
//		m.setVisible(true);	
		
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		Options od = new Options(global.getGnwGui().getFrame(), new Multi());
		od.setMenu(Options.MULTIPLE_SELECTION_MENU);
		od.setVisible(true);
	}
	
	
	// ----------------------------------------------------------------------------
	// PRIVATE CLASSES
	
	private class DesktopExport extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		
		/** Folder where the desktop will be exported */
		private String absPath_;
		
		/** Zip filename */
		private String zipFilename_;
		
		// ----------------------------------------------------------------------------
		// PUBLIC METHODS
	  
		public DesktopExport(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		// ----------------------------------------------------------------------------
		// PROTECTED METHODS
	  
		@Override
		protected Void doInBackground() throws Exception
		{
			exportDesktop(absPath_, zipFilename_);
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done()
		{
			wDialog_.stop();
		}
		
		// ----------------------------------------------------------------------------
		// SETTERS AND GETTERS
		
		public void setAbsPath(String path) { absPath_ = path; }
		public void setZipFilename(String filename) { zipFilename_ = filename; }
	}
}
