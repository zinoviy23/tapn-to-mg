package com.github.zinoviy23.tapaal.extended.extensions;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.io.graphml.MetricGraphYedWriter;
import pipe.gui.CreateGui;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;

public class ConvertTapnToYedMgAction extends ConvertTapnToMgAction {
  public ConvertTapnToYedMgAction() {
    super("Convert to yEd Metric Graph", "Convert current Timed Arc Petri Net to Metric Graph in yEd format");
  }

  @Override
  protected String getFileExt() {
    return "graphml";
  }

  @Override
  protected boolean save(MetricGraph graph, String path) {
    try (var fileWriter = new FileWriter(path); var graphWriter = new MetricGraphYedWriter(fileWriter, true)) {
      graphWriter.write(graph);
      return true;
    } catch (IOException e) {
      JOptionPane.showMessageDialog(CreateGui.getAppGui(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    return false;
  }
}
