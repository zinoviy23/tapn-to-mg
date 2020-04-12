package com.github.zinoviy23.tapnToMg.converters;

import com.github.zinoviy23.metricGraphs.MetricGraph;
import com.github.zinoviy23.metricGraphs.io.ValidatedMetricGraphReader;
import com.github.zinoviy23.metricGraphs.util.ContainerUtil;
import com.github.zinoviy23.tapnToMg.converters.mgToTapn.ConvertedTimedArcPetriNet;
import com.github.zinoviy23.tapnToMg.converters.mgToTapn.MetricGraphToTimedArcPetriNetConverter;
import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.ModelLoader;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import org.jetbrains.annotations.NotNull;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ConvertersFactory {
  public static @NotNull Function<File, File> createMetricGraphToTimedArcPetriNetConverter() {
    return createFileToMetricGraphConverter()
        .andThen(new MetricGraphToTimedArcPetriNetConverter())
        .andThen(createTimedArcPetriNetToFileConverter());
  }

  public static @NotNull Function<File, MetricGraph> createFileToMetricGraphConverter() {
    return file -> {
      try (var reader = new ValidatedMetricGraphReader(file).getReader()) {
        return reader.read();
      } catch (IOException e) {
        throw new ConversionException(e);
      }
    };
  }

  public static @NotNull Function<ConvertedTimedArcPetriNet, File> createTimedArcPetriNetToFileConverter() {
    return petriNet -> {
      var writer = new TimedArcPetriNetNetworkWriter(
          petriNet.getNetwork(),
          List.of(petriNet.getTemplate()),
          Collections.emptyList(),
          petriNet.getNetwork().constants()
      );
      try {
        File file = File.createTempFile("convertedTapn", ".tapn");
        writer.savePNML(file);
        return file;
      } catch (TransformerException | IOException | ParserConfigurationException e) {
        throw new ConversionException(e);
      }
    };
  }

  public static @NotNull Function<File, TimedArcPetriNet> createFileToTimedArcPetriNetConverter() {
    return file -> {
      ModelLoader loader = new ModelLoader();
      try {
        var load = loader.load(file);
        var network = load.network();
        var net = ContainerUtil.first(network.allTemplates());
        if (net == null) {
          throw new RuntimeException("Network must have at least one template");
        }
        return net;
      } catch (Exception e) {
        throw new ConversionException(e);
      }
    };
  }

  private ConvertersFactory() {
  }
}
