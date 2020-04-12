package com.github.zinoviy23;

import com.github.zinoviy23.metricGraphs.io.MetricGraphWriter;
import com.github.zinoviy23.metricGraphs.io.graphml.MetricGraphYedWriter;
import com.github.zinoviy23.tapaal.TapaalHeadlessService;
import com.github.zinoviy23.tapaal.extended.ExtendedTapaal;
import com.github.zinoviy23.tapnToMg.converters.ConversionException;
import com.github.zinoviy23.tapnToMg.converters.ConvertersFactory;
import com.github.zinoviy23.tapnToMg.converters.mgToTapn.MetricGraphToTimedArcPetriNetConverter;
import com.github.zinoviy23.tapnToMg.converters.tapnToMg.TimedArcPetriNetToMetricGraphConverter;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import picocli.CommandLine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
    name = "tapn-to-mg",
    description = "Converts Timed Arc Petri Net to Metric Graph and vice versa.",
    version = "tapn-to-mg 0.0"
)
public class TapnToMgApp implements Callable<Integer> {
  @SuppressWarnings("unused")
  @CommandLine.Option(names = {"-f", "--from"}, description = "Source file")
  private File sourceFile;

  @SuppressWarnings("unused")
  @CommandLine.Option(names = "--to-mg", description = "Target metric graph file")
  private File mgFile;

  @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
  @CommandLine.Option(names = "--format", description = "JSON, yEd")
  private String mgFileFormat = "json";

  @SuppressWarnings("unused")
  @CommandLine.Option(names = "--to-tapn", description = "Target Timed Arc Petri Net")
  private File tapnFile;

  @SuppressWarnings("unused")
  @CommandLine.Option(names = {"-g", "--gui"})
  private boolean useGui;

  private static Runnable resultRunnable;

  public static void main(String[] args) {
    var exitCode = new CommandLine(new TapnToMgApp()).execute(args);
    if (exitCode == 0 && resultRunnable != null) {
      resultRunnable.run();
    } else {
      System.exit(exitCode);
    }
  }

  @Override
  public Integer call() throws Exception {
    if (useGui && TapaalHeadlessService.isHeadless()) {
      System.err.println("Cannot use GUI in headless mode");
      return -1;
    } else if (useGui) {
      String[] args;
      if (sourceFile != null) {
        args = new String[] { sourceFile.getAbsolutePath() };
      } else {
        args = new String[0];
      }
      resultRunnable = () -> ExtendedTapaal.main(args);
      return 0;
    }

    if (mgFile != null && tapnFile != null) {
      System.err.println("Provided both mg and tapn destinations");
      return -1;
    }

    if (sourceFile == null || !sourceFile.exists()) {
      System.err.println("Please, provide existing source file");
      return -1;
    }

    if (mgFile != null) {
      if (!"json".equals(mgFileFormat.toLowerCase()) && !"yed".equals(mgFileFormat.toLowerCase())) {
        System.err.println("Unknown format " + mgFileFormat);
        return -1;
      }

      var converter = ConvertersFactory.createFileToTimedArcPetriNetConverter()
          .andThen(new TimedArcPetriNetToMetricGraphConverter());

      try {
        var graph = converter.apply(sourceFile);
        try (var fileWriter = new FileWriter(mgFile)) {
          if ("yed".equals(mgFileFormat.toLowerCase())) {
            try (var graphWriter = new MetricGraphYedWriter(fileWriter, true)) {
              graphWriter.write(graph);
            }
          } else {
            try (var graphWriter = new MetricGraphWriter(fileWriter, true)) {
              graphWriter.write(graph);
            }
          }
          return 0;
        } catch (IOException e) {
          if (e.getCause() != null) {
            System.err.println(e.getCause().getMessage());
          } else {
            System.err.println(e.getMessage());
          }
          return -1;
        }
      } catch (ConversionException e) {
        System.err.println(e.getCause().getMessage());
        return -1;
      }
    }

    if (tapnFile != null) {
      var converter = ConvertersFactory.createFileToMetricGraphConverter()
          .andThen(new MetricGraphToTimedArcPetriNetConverter());

      try {
        var network = converter.apply(sourceFile);
        TimedArcPetriNetNetworkWriter writer = new TimedArcPetriNetNetworkWriter(
            network.getNetwork(),
            List.of(network.getTemplate()),
            Collections.emptyList(),
            network.getNetwork().constants()
        );
        writer.savePNML(tapnFile);
        return 0;
      } catch (ConversionException e) {
        System.err.println(e.getCause().getMessage());
        return -1;
      } catch (IOException e) {
        System.err.println(e.getMessage());
        return -1;
      }
    }

    return 0;
  }
}
