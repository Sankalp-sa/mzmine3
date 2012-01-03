/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peaklistmethods.identification.dbsearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.IsotopePattern;
import net.sf.mzmine.data.PeakIdentity;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.Desktop;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.visualization.molstructure.MolStructureViewer;
import net.sf.mzmine.modules.visualization.spectra.SpectraVisualizerModule;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskStatus;

public class ResultWindow extends JInternalFrame implements ActionListener {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private ResultTableModel listElementModel;
	private JButton btnAdd, btnViewer, btnIsotopeViewer, btnBrowser;

	private PeakListRow peakListRow;
	private JTable IDList;
	private Task searchTask;

	public ResultWindow(PeakListRow peakListRow, double searchedMass,
			Task searchTask) {

		super(null, true, true, true, true);

		this.peakListRow = peakListRow;
		this.searchTask = searchTask;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBackground(Color.white);

		JPanel pnlLabelsAndList = new JPanel(new BorderLayout());
		pnlLabelsAndList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		pnlLabelsAndList.add(new JLabel("List of possible identities"),
				BorderLayout.NORTH);

		listElementModel = new ResultTableModel(searchedMass);
		IDList = new JTable();
		IDList.setModel(listElementModel);
		IDList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		IDList.getTableHeader().setReorderingAllowed(false);

		TableRowSorter<ResultTableModel> sorter = new TableRowSorter<ResultTableModel>(
				listElementModel);
		IDList.setRowSorter(sorter);

		JScrollPane listScroller = new JScrollPane(IDList);
		listScroller.setPreferredSize(new Dimension(350, 100));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.PAGE_AXIS));
		listPanel.add(listScroller);
		listPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		pnlLabelsAndList.add(listPanel, BorderLayout.CENTER);

		JPanel pnlButtons = new JPanel();
		btnAdd = new JButton("Add identity");
		btnAdd.addActionListener(this);
		btnAdd.setActionCommand("ADD");
		btnViewer = new JButton("View structure");
		btnViewer.addActionListener(this);
		btnViewer.setActionCommand("VIEWER");
		btnIsotopeViewer = new JButton("View isotope pattern");
		btnIsotopeViewer.addActionListener(this);
		btnIsotopeViewer.setActionCommand("ISOTOPE_VIEWER");
		btnBrowser = new JButton("Open browser");
		btnBrowser.addActionListener(this);
		btnBrowser.setActionCommand("BROWSER");

		pnlButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		pnlButtons.add(btnAdd);
		pnlButtons.add(btnViewer);
		pnlButtons.add(btnIsotopeViewer);
		pnlButtons.add(btnBrowser);

		setLayout(new BorderLayout());
		setSize(500, 200);
		add(pnlLabelsAndList, BorderLayout.CENTER);
		add(pnlButtons, BorderLayout.SOUTH);
		pack();

	}

	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if (command.equals("ADD")) {
			
			int index = IDList.getSelectedRow();

			if (index < 0) {
				MZmineCore.getDesktop().displayMessage(
						"Select one result to add as compound identity");
				return;

			}
			index = IDList.convertRowIndexToModel(index);

			peakListRow.addPeakIdentity(listElementModel.getCompoundAt(index),
					false);

			// Notify the GUI about the change in the project
			MZmineCore.getCurrentProject().notifyObjectChanged(peakListRow,
					false);
			
			// Repaint the window to reflect the change in the peak list
			MZmineCore.getDesktop().getMainFrame().repaint();

			dispose();
		}

		if (command.equals("VIEWER")) {

			int index = IDList.getSelectedRow();

			if (index < 0) {
				MZmineCore.getDesktop().displayMessage(
						"Select one result to display molecule structure");
				return;
			}
			index = IDList.convertRowIndexToModel(index);

			DBCompound compound = listElementModel.getCompoundAt(index);
			URL url2D = compound.get2DStructureURL();
			URL url3D = compound.get3DStructureURL();
			String name = compound.getName() + " ("
					+ compound.getPropertyValue(PeakIdentity.PROPERTY_ID) + ")";
			MolStructureViewer viewer = new MolStructureViewer(name, url2D,
					url3D);
			Desktop desktop = MZmineCore.getDesktop();
			desktop.addInternalFrame(viewer);

		}

		if (command.equals("ISOTOPE_VIEWER")) {

			int index = IDList.getSelectedRow();

			if (index < 0) {
				MZmineCore.getDesktop().displayMessage(
						"Select one result to display the isotope pattern");
				return;
			}

			index = IDList.convertRowIndexToModel(index);

			final IsotopePattern predictedPattern = listElementModel
					.getCompoundAt(index).getIsotopePattern();

			if (predictedPattern == null)
				return;
			
			ChromatographicPeak peak = peakListRow.getBestPeak();

			RawDataFile dataFile = peak.getDataFile();
			int scanNumber = peak.getRepresentativeScanNumber();
			SpectraVisualizerModule.showNewSpectrumWindow(dataFile, scanNumber, null,
					peak.getIsotopePattern(), predictedPattern);

		}

		if (command.equals("BROWSER")) {
			int index = IDList.getSelectedRow();

			if (index < 0) {
				MZmineCore.getDesktop().displayMessage(
						"Select one compound to display in a web browser");
				return;

			}
			index = IDList.convertRowIndexToModel(index);

			logger.finest("Launching default browser to display compound details");

			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

			DBCompound compound = listElementModel.getCompoundAt(index);
			String urlString = compound
					.getPropertyValue(PeakIdentity.PROPERTY_URL);

			if ((urlString == null) || (urlString.length() == 0))
				return;

			try {
				URL compoundURL = new URL(urlString);
				desktop.browse(compoundURL.toURI());
			} catch (Exception ex) {
				logger.severe("Error trying to launch default browser: "
						+ ex.getMessage());
			}

		}

	}

	public void addNewListItem(final DBCompound compound) {

		// Update the model in swing thread to avoid exceptions
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listElementModel.addElement(compound);
			}
		});

	}

	public void dispose() {

		// Cancel the search task if it is still running
		TaskStatus searchStatus = searchTask.getStatus();
		if ((searchStatus == TaskStatus.WAITING)
				|| (searchStatus == TaskStatus.PROCESSING))
			searchTask.cancel();

		super.dispose();

	}

}
