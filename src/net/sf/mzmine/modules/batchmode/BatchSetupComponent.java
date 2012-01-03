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

package net.sf.mzmine.modules.batchmode;

import de.schlichtherle.io.FileOutputStream;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineModule;
import net.sf.mzmine.modules.MZmineModuleCategory;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.util.GUIUtils;
import net.sf.mzmine.util.components.DragOrderedJList;
import net.sf.mzmine.util.dialogs.ExitCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

public class BatchSetupComponent extends JPanel implements ActionListener {

    // Logger.
    private static final Logger LOG = Logger.getLogger(BatchSetupComponent.class.getName());

    // Queue operations.
    private enum QueueOperations {
        Replace, Prepend, Insert, Append
    }

    // The batch queue.
    private BatchQueue batchQueue;

    // Widgets.
    private final JComboBox methodsCombo;
    private final JList currentStepsList;
    private final JButton btnAdd;
    private final JButton btnConfig;
    private final JButton btnRemove;
    private final JButton btnClear;
    private final JButton btnLoad;
    private final JButton btnSave;

    /**
     * Create the component.
     */
    public BatchSetupComponent() {

        super(new BorderLayout());

        batchQueue = new BatchQueue();

        // The steps list.
        currentStepsList = new DragOrderedJList();
        currentStepsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Methods combo box.
        methodsCombo = new JComboBox();

        // Add processing modules to combo box by category.
        final MZmineModule[] allModules = MZmineCore.getAllModules();
        for (final MZmineModuleCategory category : MZmineModuleCategory.values()) {

            boolean categoryItemAdded = false;
            for (final MZmineModule module : allModules) {

                // Processing module? Exclude the batch mode module.
                if (!module.getClass().equals(BatchModeModule.class) &&
                    module instanceof MZmineProcessingModule) {

                    final MZmineProcessingModule step = (MZmineProcessingModule) module;

                    // Correct category?
                    if (step.getModuleCategory() == category) {

                        // Add category item?
                        if (!categoryItemAdded) {
                            methodsCombo.addItem("--- " + category + " ---");
                            categoryItemAdded = true;
                        }

                        // Add method item.
                        methodsCombo.addItem(step);
                    }
                }
            }
        }

        // Create Load/Save buttons.
        final JPanel panelTop = new JPanel();
        panelTop.setLayout(new BoxLayout(panelTop, BoxLayout.X_AXIS));
        btnLoad = GUIUtils.addButton(panelTop, "Load...", null, this, "LOAD", "Loads a batch queue from a file");
        btnSave = GUIUtils.addButton(panelTop, "Save...", null, this, "SAVE", "Saves a batch queue to a file");

        final JPanel pnlRight = new JPanel();
        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));
        btnConfig = GUIUtils.addButton(
                pnlRight, "Configure", null, this, "CONFIG", "Configure the selected batch step");
        btnRemove = GUIUtils.addButton(
                pnlRight, "Remove", null, this, "REMOVE", "Remove the selected batch step");
        btnClear = GUIUtils.addButton(
                pnlRight, "Clear", null, this, "CLEAR", "Removes all batch steps");

        final JPanel pnlBottom = new JPanel(new BorderLayout());
        btnAdd = GUIUtils.addButton(pnlBottom, "Add", null, this, "ADD", "Adds the selected method to the batch queue");
        pnlBottom.add(btnAdd, BorderLayout.EAST);
        pnlBottom.add(methodsCombo, BorderLayout.CENTER);

        // Layout sub-panels.
        add(panelTop, BorderLayout.NORTH);
        add(new JScrollPane(currentStepsList), BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);
        add(pnlRight, BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {

        final Object src = e.getSource();

        if (btnAdd.equals(src)) {

            // Processing module selected?
            final Object selectedItem = methodsCombo.getSelectedItem();
            if (selectedItem instanceof MZmineProcessingModule) {

                // Show method's set-up dialog.
                final MZmineProcessingModule selectedMethod = (MZmineProcessingModule) selectedItem;
                final ParameterSet methodParams = selectedMethod.getParameterSet();
                if (methodParams == null || methodParams.showSetupDialog() == ExitCode.OK) {

                    // Add step to queue.
                    batchQueue.add(new BatchStepWrapper(
                            selectedMethod, methodParams == null ? null : methodParams.clone()));
                    currentStepsList.setListData(batchQueue);
                    currentStepsList.setSelectedIndex(currentStepsList.getModel().getSize() - 1);
                }
            }
        }

        if (btnRemove.equals(src)) {

            // Remove selected step.
            final BatchStepWrapper selected = (BatchStepWrapper) currentStepsList.getSelectedValue();
            if (selected != null) {
                final int index = currentStepsList.getSelectedIndex();
                batchQueue.remove(selected);
                currentStepsList.setListData(batchQueue);
                selectStep(index);
            }
        }

        if (btnClear.equals(src)) {

            // Clear the queue.
            batchQueue.clear();
            currentStepsList.setListData(batchQueue);
        }

        if (btnConfig.equals(src)) {

            // Configure the selected item.
            final BatchStepWrapper selected = (BatchStepWrapper) currentStepsList.getSelectedValue();
            final ParameterSet parameters = selected == null ? null : selected.getParameters();
            if (parameters != null) {
                parameters.showSetupDialog();
            }
        }

        if (btnSave.equals(src)) {

            try {
                final File file = ChooserHelper.getSaveFile(this);
                if (file != null) {
                    saveBatchSteps(file);
                }
            }
            catch (Exception ex) {

                JOptionPane.showMessageDialog(this,
                                              "A problem occurred saving the file.\n" + ex.getMessage(),
                                              "Saving Failed",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }

        if (btnLoad.equals(src)) {
            try {
                // Load the steps.
                final File file = ChooserHelper.getLoadFile(this);
                if (file != null) {

                    // Load the batch steps.
                    loadBatchSteps(file);
                }
            }
            catch (Exception ex) {

                JOptionPane.showMessageDialog(this,
                                              "A problem occurred loading the file.\n" + ex.getMessage(),
                                              "Loading Failed",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Get the queue.
     *
     * @return the queue.
     */
    public BatchQueue getValue() {
        return batchQueue;
    }

    /**
     * Sets the queue.
     *
     * @param newValue the new queue.
     */
    public void setValue(final BatchQueue newValue) {

        batchQueue = newValue;
        currentStepsList.setListData(batchQueue);
        selectStep(0);
    }

    /**
     * Select a step of the batch queue.
     *
     * @param step the step's index in the queue.
     */
    private void selectStep(final int step) {
        final int size = currentStepsList.getModel().getSize();
        if (size > 0 && step >= 0) {
            final int index = Math.min(step, size - 1);
            currentStepsList.setSelectedIndex(index);
            currentStepsList.ensureIndexIsVisible(index);
        }
    }

    /**
     * Save the batch queue to a file.
     *
     * @param file the file to save in.
     * @throws ParserConfigurationException if there is a parser problem.
     * @throws TransformerException         if there is a transformation problem.
     * @throws FileNotFoundException        if the file can't be found.
     */
    private void saveBatchSteps(final File file)
            throws ParserConfigurationException, TransformerException, FileNotFoundException {

        // Create the document.
        final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Element element = document.createElement("batch");
        document.appendChild(element);

        // Serialize batch queue.
        batchQueue.saveToXml(element);

        // Create transformer.
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // Write to file and transform.
        transformer.transform(new DOMSource(document), new StreamResult(new FileOutputStream(file)));

        LOG.info("Saved " + batchQueue.size() + " batch step(s) to " + file.getName());
    }

    /**
     * Load a batch queue from a file.
     *
     * @param file the file to read.
     * @throws ParserConfigurationException if there is a parser problem.
     * @throws SAXException                 if there is a SAX problem.
     * @throws IOException                  if there is an i/o problem.
     */
    public void loadBatchSteps(final File file) throws ParserConfigurationException, IOException, SAXException {

        final BatchQueue queue = BatchQueue.loadFromXml(
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getDocumentElement());

        LOG.info("Loaded " + queue.size() + " batch step(s) from " + file.getName());

        // Append, prepend, insert or replace.
        final int option = JOptionPane.showOptionDialog(
                this,
                "How should the loaded batch steps be added to the queue?",
                "Add Batch Steps",
                JOptionPane.NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                QueueOperations.values(),
                QueueOperations.Replace);

        int index = currentStepsList.getSelectedIndex();
        if (option >= 0) {
            switch (QueueOperations.values()[option]) {
                case Replace:
                    index = 0;
                    batchQueue = queue;
                    break;
                case Prepend:
                    index = 0;
                    batchQueue.addAll(0, queue);
                    break;
                case Insert:
                    index = index < 0 ? 0 : index;
                    batchQueue.addAll(index, queue);
                    break;
                case Append:
                    index = batchQueue.size();
                    batchQueue.addAll(queue);
                    break;
            }
        }
        currentStepsList.setListData(batchQueue);
        selectStep(index);
    }
}