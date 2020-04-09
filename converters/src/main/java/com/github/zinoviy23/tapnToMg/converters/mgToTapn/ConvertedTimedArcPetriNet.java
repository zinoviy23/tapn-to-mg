package com.github.zinoviy23.tapnToMg.converters.mgToTapn;

import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import org.jetbrains.annotations.NotNull;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Template;

public final class ConvertedTimedArcPetriNet {
    private final TimedArcPetriNetNetwork network;
    private final Template template;

    public ConvertedTimedArcPetriNet(@NotNull TimedArcPetriNetNetwork network,
                                     @NotNull TimedArcPetriNet petriNet,
                                     @NotNull DataLayer dataLayer) {
        this.network = network;
        this.template = new Template(petriNet, dataLayer,  null);
    }

    public TimedArcPetriNetNetwork getNetwork() {
        return network;
    }

    public Template getTemplate() {
        return template;
    }
}
