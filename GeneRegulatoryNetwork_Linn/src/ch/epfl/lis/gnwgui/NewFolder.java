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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ch.epfl.lis.gnwgui.Folder;
import ch.epfl.lis.gnwgui.windows.NewFolderWindow;

/**
 * This dialog is used to create a new folder.
 * 
 * @author Gilles Roulet (firstname.name@gmail.com)
 * @author Thomas Schaffter (firstname.name@gmail.com)
 */
public class NewFolder extends NewFolderWindow
{
	/** Serialization */
	private static final long serialVersionUID = 1L;

	// ----------------------------------------------------------------------------
	// PUBLIC METHODS

	public NewFolder(Frame aFrame)
	{
		super(aFrame);

		applyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				enterAction();
			}
		});

		cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				escapeAction();
			}
		});
	}

	// ----------------------------------------------------------------------------

	public void enterAction()
	{
		GnwGuiSettings global = GnwGuiSettings.getInstance();
		
		if ( !newName_.getText().isEmpty())
		{
			Folder folder = new Folder(newName_.getText(), GnwGuiSettings.getInstance().getNetworkDesktop());
			GnwGuiSettings.getInstance().getNetworkDesktop().addItemOnDesktop(folder);
			
			if (global.getNetworkDesktop().getDisplayChildrenOf() != null )
			{
				if (global.getNetworkDesktop().getDisplayChildrenOf().getClass() == Folder.class)
					((NetworkDesktop)global.getNetworkDesktop()).moveItemToFolder(folder, (Folder)global.getNetworkDesktop().getDisplayChildrenOf());
				
				((NetworkDesktop)global.getNetworkDesktop()).displayChildrenOf(global.getNetworkDesktop().getDisplayChildrenOf());
			}
		}
		
		global.getNetworkDesktop().refreshDesktop();
		dispose();
	}
}
