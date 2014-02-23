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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import ch.epfl.lis.utilities.filefilters.FilenameUtilities;
import ch.epfl.lis.utilities.filefilters.FilterGnwDesktopZIP;
import ch.epfl.lis.utilities.filefilters.FilterNetworkAll;
import ch.epfl.lis.utilities.filefilters.FilterNetworkDOT;
import ch.epfl.lis.utilities.filefilters.FilterNetworkGML;
import ch.epfl.lis.utilities.filefilters.FilterNetworkSBML;
import ch.epfl.lis.utilities.filefilters.FilterNetworkTSV;
import ch.epfl.lis.utilities.filefilters.FilterNetworkTSVDREAM;
import ch.epfl.lis.utilities.filefilters.FilterNetworkTSVDREAMTSV;
import ch.epfl.lis.gnwgui.idesktop.IElement;
import ch.epfl.lis.gnwgui.idesktop.IFolder;
import ch.epfl.lis.gnwgui.windows.Wait;
import ch.epfl.lis.gnw.GeneNetwork;
import ch.epfl.lis.gnw.GnwSettings;
import ch.epfl.lis.gnw.Parser;
import ch.epfl.lis.imod.ImodNetwork;
import ch.epfl.lis.networks.Structure;
import ch.epfl.lis.networks.ios.ParseException;

import javax.swing.filechooser.FileFilter;

/**
 * Implements all the methods to load/save networks from/to files.
 * 
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class IONetwork
{
	/** Logger for this class */
	private static Logger log_ = Logger.getLogger(IONetwork.class.getName());

	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	public IONetwork() {}

	// ----------------------------------------------------------------------------

	public static void open()
	{
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Open Network",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.LOAD);

		dialog.addFilter(new FilterNetworkTSVDREAMTSV());
		//		dialog.addFilter(new FilterNetworkTSVDREAM());
		dialog.addFilter(new FilterNetworkGML());
		dialog.addFilter(new FilterNetworkDOT());
		dialog.addFilter(new FilterNetworkSBML());
		dialog.addFilter(new FilterGnwDesktopZIP());
		dialog.addFilter(new FilterNetworkAll());

		dialog.setAcceptAllFileFilterUsed(false);
		dialog.display();
		
		Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
		wait.setTitle("Open Network");
		NetworkImport ni = new NetworkImport(wait);
		
		ni.fileAbsPath_ = dialog.getSelection();
		ni.f_ = dialog.getSelectedFilter();
		
		ni.execute();
		wait.start();
	}

	// ----------------------------------------------------------------------------

	public static void open(String absPath, FileFilter f)
	{
		try
		{
			if (absPath != null)
			{
				String dir = FilenameUtilities.getDirectory(absPath);
				String filename = FilenameUtilities.getFilenameWithoutPath(absPath);
				URL url = GnwSettings.getInstance().getURL(absPath);

				if (f == null || f instanceof FilterNetworkAll)
					loadItem(filename, url, null); // open according to the extension
				else if (f instanceof FilterNetworkTSVDREAMTSV)
					loadItem(filename, url, ImodNetwork.TSV);
				//				else if (f instanceof FilterNetworkTSVDREAM)
				//					loadItem(filename, url, ImodNetwork.TSV_DREAM);
				else if (f instanceof FilterNetworkGML)
					loadItem(filename, url, ImodNetwork.GML);
				else if (f instanceof FilterNetworkDOT)
					loadItem(filename, url, ImodNetwork.DOT);
				else if (f instanceof FilterNetworkSBML)
					loadItem(filename, url, GeneNetwork.SBML);
				else if (f instanceof FilterGnwDesktopZIP)
					GnwGuiSettings.getInstance().getNetworkDesktop().importDesktopInterface(absPath);
				else
					throw new Exception("Selected format unhandled!");

				// Save the current path as user path
				GnwSettings.getInstance().setOutputDirectory(dir);
			}
		}
		catch (FileNotFoundException e)
		{
			openingFailedDialog("GNW Message", absPath, "File not found!");
			log_.log(Level.WARNING, "IONetwork::open(): " + e.getMessage(), e);
		}
		catch (ParseException e)
		{
			openingFailedDialog("GNW Message", absPath, "Error occurs during parsing.<br>" +
			"See logs for more information.");
			log_.log(Level.WARNING, "IONetwork::open(): " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			openingFailedDialog("GNW Message", absPath, "Unhandled exception!<br>" +
			"See logs for more information.");
			log_.log(Level.WARNING, "IONetwork::open(): " + e.getMessage(), e);
		}
	}

	// ----------------------------------------------------------------------------

	public static void saveAs(NetworkElement item)
	{
		IODialog dialog = new IODialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), "Save network",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

		dialog.addFilter(new FilterNetworkTSV());
		dialog.addFilter(new FilterNetworkTSVDREAM());
		dialog.addFilter(new FilterNetworkGML());
		dialog.addFilter(new FilterNetworkDOT());
		if (item instanceof DynamicalModelElement)
			dialog.addFilter(new FilterNetworkSBML());

		dialog.setAcceptAllFileFilterUsed(false);
		dialog.setSelection(item.getLabel());
		dialog.display();

		if (dialog.getSelection() != null)
		{
			Wait wait = new Wait(GnwGuiSettings.getInstance().getGnwGui().getFrame(), true);
			wait.setTitle("Save network");
			NetworkExport ne = new NetworkExport(wait);
			
			ne.element_ = item;
			ne.fileAbsPath_ = dialog.getSelection();
			ne.f_ = dialog.getSelectedFilter();
			
			ne.execute();
			wait.start();
		}
	}

	// ----------------------------------------------------------------------------

	public static void saveAllSected()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		boolean onlyDynamics = true;
		
		ArrayList<IElement> selected = GnwGuiSettings.getInstance().getNetworkDesktop().getSelectedElements();
		
		//Get all elements to save
		ArrayList<IElement> topElements = new ArrayList<IElement>();
		boolean add = false;
		
		for(IElement itemIE : selected)
		{						
			if( topElements.size() == 0 )
				topElements.add(itemIE);
			else
			{
				for(int i = 0; i < topElements.size(); i++)
				{
					add = false;
					
					if(!global.getNetworkDesktop().getAllChildren(topElements.get(i)).contains(itemIE) && !topElements.contains(itemIE))
						add = true;		
					else
						add = false;	
				}
				
				if(add)
					topElements.add(itemIE);
			}
		}
		
		//Open dialog
		IODialog dialog = new IODialog(global.getGnwGui().getFrame(), "Select Folder",
				GnwSettings.getInstance().getOutputDirectory(), IODialog.SAVE);

		dialog.addFilter(new FilterNetworkTSV());
		dialog.addFilter(new FilterNetworkTSVDREAM());
		dialog.addFilter(new FilterNetworkGML());
		dialog.addFilter(new FilterNetworkDOT());
		
		for ( IElement itemIE : topElements)
		{
			for( IElement child : global.getNetworkDesktop().getAllChildren(itemIE)) {
				if ( child.getClass() != DynamicalModelElement.class && child.getClass() != Folder.class)
					onlyDynamics = false;
			}
			if ( itemIE.getClass() != DynamicalModelElement.class && itemIE.getClass() != Folder.class)
				onlyDynamics = false;
		}
		if(onlyDynamics)
			dialog.addFilter(new FilterNetworkSBML());
		
		dialog.setAcceptAllFileFilterUsed(false);
		dialog.selectOnlyFolder(true);
		
		dialog.display();
		
		try
		{
			if (dialog.getSelection() != null)
			{
				FileFilter selectedFilter = dialog.getSelectedFilter();
				for(IElement itemIE : topElements)
				{
					recursiveSave(itemIE, dialog.getSelection(), selectedFilter);
				}
			}
		} catch (Exception e)
		{
			savingFailedDialog("GNW Message", dialog.getSelection(), "Unhandled exception!<br>" +
				"See logs for more information.");
			log_.log(Level.WARNING, "IONetwork::saveAllSected(): " + e.getMessage(), e);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public static void recursiveSave(IElement item, String path, FileFilter filter)
	{
		
		if (item.getClass() == Folder.class)
		{
			path += "/" +((IFolder)item).getLabel();
			createFolder(path);
		}
		else
		{
			
			saveItem((NetworkElement)item, path, filter);
			path += "/" + item.getLabel();
		}
	
		
		if ( item.hasChildren())
		{
			createFolder(path);
			
			for(IElement e : item.getChildren())
				recursiveSave(e, path, filter);
		}
	}
	
	// ----------------------------------------------------------------------------
	
	public static void createFolder(String path)
	{
		File f = new File(path);
		
		if ( !f.exists() && (new File(path)).mkdirs() )
			log_.log(Level.INFO,path + " created");
		else if ( f.exists())
			;
		else
			log_.log(Level.WARNING,"Could not create folder " + path);
	}
	
	// ----------------------------------------------------------------------------
	
	public static void saveItem(NetworkElement item, String folder, FileFilter filter)
	{
		//Trick for windows paths
		//String path = "file:///" + (new File(folder + "/" + item.getLabel()).getAbsolutePath());
		String path = folder + "/" + item.getLabel();
		
		try
		{
			if (folder != null)
			{
				URL url = GnwSettings.getInstance().getURL(folder);
				FileFilter selectedFilter = filter;
				
				if (selectedFilter instanceof FilterNetworkTSV)
				{	
					String[] extension = {FilterNetworkTSV.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

//					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
//						return;

					exportTSVStructure(item, url);
				}
				else if (selectedFilter instanceof FilterNetworkTSVDREAM)
				{	
					String[] extension = {FilterNetworkTSVDREAM.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

//					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
//						return;

					exportTSVDREAMStructure(item, url);
				}
				else if (selectedFilter instanceof FilterNetworkGML)
				{	
					String[] extension = {FilterNetworkGML.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

//					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
//						return;

					exportGMLStructure(item, url);
				}
				else if (selectedFilter instanceof FilterNetworkDOT)
				{
					String[] extension = {FilterNetworkDOT.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

//					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
//						return;

					exportDOTStructure(item, url);
				}
				else if (selectedFilter instanceof FilterNetworkSBML)
				{	
					String[] extension = {FilterNetworkSBML.ext};
					path = FilenameUtilities.addExtension(path, extension);
					url = new URL(path);

//					if (!FilenameUtilities.writeOrAbort(url.getPath(), GnwGuiSettings.getInstance().getGnwGui().getFrame()))
//						return;

					exportSBMLGeneRegulatoryNetwork(item, url);
				}
				else
					throw new Exception("Error");
			}
		}
		catch (MalformedURLException e)
		{
			savingFailedDialog("GNW Message", path, "Malformed URL!<br>" +
				"See logs for more information.");
			log_.log(Level.WARNING, "IONetwork::saveItem(): " + e.getMessage(), e);
		}
		catch (Exception e)
		{
			savingFailedDialog("GNW Message", path, "Unhandled exception!<br>" +
				"See logs for more information.");
			log_.log(Level.WARNING, "IONetwork::saveItem(): " + e.getMessage(), e);
		}
	}
	
	// ----------------------------------------------------------------------------

	public static void openingFailedDialog(String title, String file, String cause)
	{
		String msg = "Opening '" + file + "' failed:<br><br>" + cause;

		msg = "<html>" + msg + "</html>";
		JOptionPane.showMessageDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), msg, title, JOptionPane.WARNING_MESSAGE);
	}

	// ----------------------------------------------------------------------------

	public static void savingFailedDialog(String title, String file, String cause)
	{
		String msg = "Saving '" + file + "' failed:<br><br>" + cause;

		msg = "<html>" + msg + "</html>";
		JOptionPane.showMessageDialog(GnwGuiSettings.getInstance().getGnwGui().getFrame(), msg, title, JOptionPane.WARNING_MESSAGE);
	}

	// ----------------------------------------------------------------------------

	public static void exportTSVStructure(NetworkElement item, URL absPath) throws MalformedURLException
	{
		try
		{
			if (item instanceof StructureElement)
				((StructureElement)item).getNetwork().saveTSV(absPath); // was saveDREAM
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().saveTSV(absPath); // was saveDREAM
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "IONetwork::exportTSVStructure(): " + e.getMessage(), e);
		}
	}

	// ----------------------------------------------------------------------------

	public static void exportTSVDREAMStructure(NetworkElement item, URL absPath) throws MalformedURLException
	{
		try
		{
			if (item instanceof StructureElement)
			{
				//((StructureElement)item).getNetwork().saveDREAM(absPath); // was saveDREAM
				Parser parser = new Parser(((StructureElement)item).getNetwork(), absPath);
				parser.writeGoldStandard();
			}
			else if (item instanceof DynamicalModelElement)
			{
				//((DynamicalModelElement)item).getGeneNetwork().saveDREAM(absPath); // was saveDREAM
				Parser parser = new Parser(((DynamicalModelElement)item).getGeneNetwork(), absPath);
				parser.writeGoldStandard();
			}
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "IONetwork::exportTSVDREAMStructure(): " + e.getMessage(), e);
		}
	}

	// ----------------------------------------------------------------------------

	public static void exportGMLStructure(NetworkElement item, URL absPath) throws MalformedURLException
	{
		try
		{
			if (item instanceof StructureElement)
			{
				Structure network = ((StructureElement)item).getNetwork();
				NetworkGraph layout = item.getNetworkViewer();
				if (network.saveNodePosition() && layout != null)
					layout.saveStructureLayout();
				network.saveGML(absPath);
			}
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().saveGML(absPath);
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "IONetwork::exportGMLStructure(): " + e.getMessage(), e);
		}
	}

	// ----------------------------------------------------------------------------

	public static void exportDOTStructure(NetworkElement item, URL absPath) throws MalformedURLException, FileNotFoundException
	{
		try
		{
			if (item instanceof StructureElement)
			{
				Structure network = ((StructureElement)item).getNetwork();
				NetworkGraph layout = item.getNetworkViewer();
				if (network.saveNodePosition() && layout != null)
					layout.saveStructureLayout();
				network.saveDOT(absPath);
			}
			else if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().saveDOT(absPath);
		}
		catch (Exception e)
		{
			log_.log(Level.WARNING, "IONetwork::exportDOTStructure(): " + e.getMessage(), e);
		}
	}

	// ----------------------------------------------------------------------------

	public static void exportSBMLGeneRegulatoryNetwork(NetworkElement item, URL absPath) throws MalformedURLException
	{
		try
		{
			if (item instanceof DynamicalModelElement)
				((DynamicalModelElement)item).getGeneNetwork().writeSBML(absPath);
			else
				throw new Exception("Only Gene Regulatory Networks can be exported into SBML format");
			
		}
		catch (Exception e) {
			log_.log(Level.WARNING, "IONetwork::exportSBMLGeneRegulatoryNetwork(): " + e.getMessage());
		}
	}

	// ----------------------------------------------------------------------------
	
	public static NetworkElement loadItem(String name, URL absPath, Integer format) throws 	FileNotFoundException,
	ParseException,
	Exception
	{
		if (name.equals("") || name.charAt(0) == '#')
			name = FilenameUtilities.getFilenameWithoutPath(absPath.getPath());
		name = FilenameUtilities.getFilenameWithoutExtension(name);

		GnwGuiSettings global = GnwGuiSettings.getInstance();

		if ( isStructureFormat(format = isStructureExtension(absPath, format)) )
		{
			StructureElement network = loadStructureItem(name, absPath, format);

			if (global.getNetworkDesktop() != null)
				global.getNetworkDesktop().addItemOnDesktop(network);

			printOpeningInfo(network);

			return network;
		}
		else if ( isDynamicalNetworkFormat(format = isDynamicalNetworkExtension(absPath, format)) )
		{

			DynamicalModelElement grn = loadDynamicNetworkItem(name, absPath, format);

			if (global.getNetworkDesktop() != null)
				global.getNetworkDesktop().addItemOnDesktop(grn);

			printOpeningInfo(grn);

			return grn;
		}
		else
			throw new Exception("Unkown network format!");
	}

	// ----------------------------------------------------------------------------

	public static Integer isStructureExtension(URL absPath, Integer format)
	{
		if (format == null)
		{	
			String extension = FilenameUtilities.getExtension(absPath.getPath());

			if (extension.equals("tsv"))
				format = ImodNetwork.TSV;
			else if (extension.equals("gml"))
				format = ImodNetwork.GML;
			else if (extension.equals("dot"))
				format = ImodNetwork.DOT;
			else
				format = null;
		}

		return format;
	}

	// ----------------------------------------------------------------------------

	public static boolean isStructureFormat(Integer format)
	{	
		if (format == null)
			return false;

		if (format == ImodNetwork.TSV)
			return true;

		if (format == ImodNetwork.GML)
			return true;

		if (format == ImodNetwork.DOT)
			return true;

		return false;
	}

	// ----------------------------------------------------------------------------

	public static boolean isDynamicalNetworkFormat(Integer format)
	{
		if (format == null)
			return false;

		if (format == GeneNetwork.SBML)
			return true;

		return false;
	}

	// ----------------------------------------------------------------------------

	public static Integer isDynamicalNetworkExtension(URL absPath, Integer format)
	{		
		if (format == null)
		{
			String extension = FilenameUtilities.getExtension(absPath.getPath());

			if (extension.equals("xml"))
				format = GeneNetwork.SBML;
			else
				format = null;
		}

		return format;
	}

	// ----------------------------------------------------------------------------

	public static StructureElement loadStructureItem(String name, URL absPath, Integer format) throws 	FileNotFoundException,
	ParseException,
	Exception
	{
		StructureElement network = new StructureElement(name);

		network.load(absPath, format);
		network.getNetwork().setId(name);

		// As DOT format has a place where network Id is defined, we take it
		// as label for the item displayed on the desktop.
		if (format == Structure.DOT)
			network.setText(network.getNetwork().getId());

		return network;
	}

	// ----------------------------------------------------------------------------

	public static DynamicalModelElement loadDynamicNetworkItem(String name, URL absPath, Integer format) throws IOException,
	Exception
	{

		DynamicalModelElement grn = new DynamicalModelElement(name);

		grn.load(absPath, format);
		grn.getGeneNetwork().setId(name);

		return grn;
	}

	// ----------------------------------------------------------------------------

	public static void printOpeningInfo(NetworkElement item)
	{
		String[] msg = openingInfo(item);
		log_.info(msg[0] + "\n" + msg[1] + "\n"); // print to console
	}
	
	// ----------------------------------------------------------------------------

	public static String[] openingInfo(NetworkElement item) {

		String msg[] = {"", ""};

		if (item instanceof StructureElement) {
			Structure n = ((StructureElement)item).getNetwork();
			msg[0] = "Loaded network structure";
			msg[1] = n.getId() + " (" + n.getSize() + " nodes, " + n.getNumEdges() + " edges)";
		} else if (item instanceof DynamicalModelElement) {
			GeneNetwork n = ((DynamicalModelElement)item).getGeneNetwork();
			msg[0] = "Loaded dynamical network";
			msg[1] = n.getId() + " (" + n.getSize() + " genes, " + n.getNumEdges() + " regulations)";
		}
		return msg;
	}
	
	// ----------------------------------------------------------------------------
	// PRIVATE CLASSES
	
	private static class NetworkImport extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		
		/** Absolute path to the network file */
		private String fileAbsPath_;
		
		/** FileFilter selected (information about the type of network to load) */
		private FileFilter f_;
		
		// ----------------------------------------------------------------------------
		// PUBLIC METHODS
	  
		public NetworkImport(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		// ----------------------------------------------------------------------------
		// PROTECTED METHODS
	  
		@Override
		protected Void doInBackground() throws Exception
		{

			open(fileAbsPath_, f_);
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done()
		{
			wDialog_.stop();
		}
	}
	
	// ----------------------------------------------------------------------------
	
	private static class NetworkExport extends SwingWorker<Void, Void>
	{
		/** Dialog displayed during the process */
		private Wait wDialog_;
		
		/** Element to export */
		NetworkElement element_;
		
		/** Absolute path to the network file */
		private String fileAbsPath_;
		
		/** FileFilter selected (information about the type of network to load) */
		private FileFilter f_;
		
		// ----------------------------------------------------------------------------
		// PUBLIC METHODS
	  
		public NetworkExport(Wait gui)
		{
			this.wDialog_ = gui;
		}
		
		// ----------------------------------------------------------------------------
		// PROTECTED METHODS
	  
		@Override
		protected Void doInBackground() throws Exception
		{
			try 
			{
				URL url = GnwSettings.getInstance().getURL(fileAbsPath_);
				FileFilter selectedFilter = f_;
				String output = null;

				if (selectedFilter instanceof FilterNetworkTSV) {
					String[] extension = {FilterNetworkTSV.ext};
					String path = FilenameUtilities.addExtension(fileAbsPath_, extension);
					url = GnwSettings.getInstance().getURL(path);
					exportTSVStructure(element_, url);
					output = "The network " + element_.getLabel() + " was exported in TSV format";
				}
				else if (selectedFilter instanceof FilterNetworkTSVDREAM) {
					String[] extension = {FilterNetworkTSVDREAM.ext};
					String path = FilenameUtilities.addExtension(fileAbsPath_, extension);
					url = GnwSettings.getInstance().getURL(path);
					exportTSVDREAMStructure(element_, url);
					output = "The network " + element_.getLabel() + " was exported in DREAM format";
				}
				else if (selectedFilter instanceof FilterNetworkGML)
				{
					String[] extension = {FilterNetworkGML.ext};
					String path = FilenameUtilities.addExtension(fileAbsPath_, extension);
					url = GnwSettings.getInstance().getURL(path);
					exportGMLStructure(element_, url);
					output = "The network " + element_.getLabel() + " was exported in GML format";
				}
				else if (selectedFilter instanceof FilterNetworkDOT)
				{
					String[] extension = {FilterNetworkDOT.ext};
					String path = FilenameUtilities.addExtension(fileAbsPath_, extension);
					url = GnwSettings.getInstance().getURL(path);
					exportDOTStructure(element_, url);
					output = "The network " + element_.getLabel() + " was exported in DOT format";
				}
				else if (selectedFilter instanceof FilterNetworkSBML) {
					String[] extension = {FilterNetworkSBML.ext};
					String path = FilenameUtilities.addExtension(fileAbsPath_, extension);
					url = GnwSettings.getInstance().getURL(path);
					exportSBMLGeneRegulatoryNetwork(element_, url);
					output = "The network " + element_.getLabel() + " was exported in SBML format";
				}
				else
					throw new Exception("Error");

				// Save the current path as user path
//					GnwSettings.getInstance().setOutputDirectory(dialog.getDirectory());
				log_.info(output);

				
			} catch (MalformedURLException e) {
				savingFailedDialog("GNW Message", fileAbsPath_, "Malformed URL!<br>" +
				"See logs for more information.");
				log_.log(Level.WARNING, "IONetwork::saveAs(): " + e.getMessage(), e);
			} catch (Exception e) {
				savingFailedDialog("GNW Message", fileAbsPath_, "Unhandled exception!<br>" +
				"See logs for more information.");
				log_.log(Level.WARNING, "IONetwork::saveAs(): " + e.getMessage(), e);
			}
			
			return null;
		}
		
		// ----------------------------------------------------------------------------
	  
		@Override
		protected void done()
		{
			wDialog_.stop();
		}
	}
}