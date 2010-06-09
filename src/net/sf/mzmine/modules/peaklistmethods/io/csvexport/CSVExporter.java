/*
 * Copyright 2006-2010 The MZmine 2 Development Team
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
package net.sf.mzmine.modules.peaklistmethods.io.csvexport;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import net.sf.mzmine.data.ParameterSet;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.desktop.Desktop;
import net.sf.mzmine.desktop.MZmineMenu;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.main.MZmineModule;
import net.sf.mzmine.modules.batchmode.BatchStep;
import net.sf.mzmine.modules.batchmode.BatchStepCategory;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.util.dialogs.ExitCode;
import net.sf.mzmine.util.dialogs.ParameterSetupDialog;

public class CSVExporter implements MZmineModule, ActionListener,
        BatchStep {

	final static String helpID = "net/sf/mzmine/modules/io/csvexport/help/CSVExport.html";

    private CSVExporterParameters parameters;
    private Desktop desktop;

    /**
     * @see net.sf.mzmine.main.MZmineModule#initModule(net.sf.mzmine.main.MZmineCore)
     */
    public void initModule() {

        this.desktop = MZmineCore.getDesktop();

        parameters = new CSVExporterParameters();

        desktop.addMenuItem(MZmineMenu.PEAKLISTEXPORT, "Export to CSV file",
                "Export peak list data into CSV file", KeyEvent.VK_C, true,
                this, null);

    }

    public ParameterSet getParameterSet() {
        return parameters;
    }

    public void setParameters(ParameterSet parameters) {
        this.parameters = (CSVExporterParameters) parameters;
    }

    public void actionPerformed(ActionEvent event) {

        PeakList[] selectedPeakLists = desktop.getSelectedPeakLists();
        if (selectedPeakLists.length != 1) {
            desktop.displayErrorMessage("Please select a single peak list for export");
            return;
        }

        ExitCode setupExitCode = setupParameters(parameters);

        if (setupExitCode != ExitCode.OK) {
            return;
        }

        runModule(null, selectedPeakLists, parameters);

    }

    /**
     * @see net.sf.mzmine.modules.BatchStep#toString()
     */
    public String toString() {
        return "Peak list exporter";
    }

    public BatchStepCategory getBatchStepCategory() {
        return BatchStepCategory.PROJECT;
    }

    public Task[] runModule(RawDataFile[] dataFiles, PeakList[] peakLists,
            ParameterSet parameters) {

        CSVExportTask task = new CSVExportTask(peakLists[0],
                (CSVExporterParameters) parameters);

        // start this group
		MZmineCore.getTaskController().addTask(task);

        return new Task[] { task };

    }

    public ExitCode setupParameters(ParameterSet parameters) {

        ParameterSetupDialog dialog = new ParameterSetupDialog(
                "Please set parameter values for " + toString(),
                (CSVExporterParameters) parameters, helpID);

        dialog.setVisible(true);

        return dialog.getExitCode();
    }
}