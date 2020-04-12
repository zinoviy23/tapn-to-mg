package com.github.zinoviy23.tapaal.extended.extensions;

import com.github.zinoviy23.tapnToMg.converters.ConversionException;
import com.github.zinoviy23.tapnToMg.converters.ConvertersFactory;
import pipe.gui.CreateGui;
import pipe.gui.action.GuiAction;
import pipe.gui.widgets.filebrowser.FileBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ConvertMgToTapnAction extends GuiAction {
  public ConvertMgToTapnAction() {
    super("Convert from Metric Graph", "Open Metric Graph file as Timed Arc Petri Net");
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    var mgFile = FileBrowser.constructor("Metric Graph", "json", FileBrowser.userPath).openFile();
    var converter = ConvertersFactory.createMetricGraphToTimedArcPetriNetConverter();

    CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    SwingWorker<Void, Void> worker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() throws Exception {
        try {
          var resultFile = converter.apply(mgFile);
          FileBrowser.userPath = mgFile.getParent();
          CreateGui.getAppGui().createNewTabFromFile(resultFile);
        } catch (ConversionException e) {
          JOptionPane.showMessageDialog(CreateGui.getAppGui(), e.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
      }

      @Override
      protected void done() {
        try {
          CreateGui.getAppGui().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          get();
          JOptionPane.showMessageDialog(CreateGui.getAppGui(), "For save TAPN use Save as action");
        } catch (Exception e) {
          JOptionPane.showMessageDialog(CreateGui.getAppGui(),
              e.getMessage(),
              "Error loading file",
              JOptionPane.ERROR_MESSAGE
          );
        }
      }
    };

    worker.execute();
  }
}
