package com.github.zinoviy23.tapaal.extended.extensions;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.io.MetricGraphJsonWriter;
import pipe.gui.CreateGui;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;

public class ConvertTapnToJsonMgAction extends ConvertTapnToMgAction {
  public ConvertTapnToJsonMgAction() {
    super("Convert to Json Metric Graph", "Convert current Timed Arc Petri Net to Metric Graph in JSON format");
  }

  @Override
  protected String getFileExt() {
    return "json";
  }

  @Override
  protected boolean save(MetricGraph graph, String path) {
    try (var writer = new FileWriter(path); MetricGraphJsonWriter gw = new MetricGraphJsonWriter(writer, true)) {
      gw.write(graph);
      return true;
    } catch (IOException e) {
      JOptionPane.showMessageDialog(CreateGui.getAppGui(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    return false;
  }
}
