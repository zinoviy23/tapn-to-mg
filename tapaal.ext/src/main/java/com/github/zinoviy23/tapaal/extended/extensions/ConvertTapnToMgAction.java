package com.github.zinoviy23.tapaal.extended.extensions;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.util.ContainerUtil;
import com.github.zinoviy23.tapnToMg.converters.tapnToMg.TimedArcPetriNetToMetricGraphConversionException;
import com.github.zinoviy23.tapnToMg.converters.tapnToMg.TimedArcPetriNetToMetricGraphConverter;
import pipe.gui.CreateGui;
import pipe.gui.action.GuiAction;
import pipe.gui.widgets.filebrowser.FileBrowser;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class ConvertTapnToMgAction extends GuiAction {
  protected ConvertTapnToMgAction(String name, String tooltip) {
    super(name, tooltip);
  }

  protected abstract String getFileExt();

  @Override
  public void actionPerformed(ActionEvent e) {
    if (CreateGui.getAppGui().getSelectedTabIndex() < 0) {
      return;
    }
    var currentTab = CreateGui.getCurrentTab();

    var path = FileBrowser.constructor("Metric Graph", getFileExt(), FileBrowser.userPath)
        .saveFile("graph");

    SwingWorker<Void, Void> worker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() {
        var network = currentTab.network();
        if (network.allTemplates().size() > 1) {
          JOptionPane.showMessageDialog(CreateGui.getAppGui(), "There is too may templates. Will use only first", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        var net = ContainerUtil.first(network.allTemplates());
        if (net == null) {
          JOptionPane.showMessageDialog(CreateGui.getAppGui(), "There isn't any template in network!", "Error", JOptionPane.ERROR_MESSAGE);
          return null;
        }

        var converter = new TimedArcPetriNetToMetricGraphConverter();
        try {
          var graph = converter.convert(net);
          if (save(graph, path)) {
            JOptionPane.showMessageDialog(CreateGui.getAppGui(), "Graph saved!");
          }
        } catch (TimedArcPetriNetToMetricGraphConversionException e) {
          JOptionPane.showMessageDialog(CreateGui.getAppGui(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          return null;
        }
        return null;
      }
    };

    worker.execute();
  }

  protected abstract boolean save(MetricGraph graph, String path);
}
