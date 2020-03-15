package com.github.zinoviy23;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.FormatException;
import org.junit.Test;
import pipe.gui.CreateGui;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class OpenTapnFile {
    @Test
    public void readFileWebserver() throws FormatException {
        TapnXmlLoader loader = new TapnXmlLoader();
        InputStream resource = getClass().getClassLoader().getResourceAsStream("Example nets/webserver.tapn");
        assertNotNull(resource);

        CreateGui.init();

        LoadedModel model = loader.load(resource);
        TimedArcPetriNetNetwork network = model.network();
        TimedArcPetriNet webServer = network.getTAPNByName("WebServer");
        assertNotNull(webServer);

        TimedPlace userA = webServer.getPlaceByName("UserA");
        TimedPlace userB = webServer.getPlaceByName("UserB");
        TimedTransition drop = webServer.getTransitionByName("Drop");
        assertNotNull(userA);
        assertNotNull(userB);
        assertNotNull(drop);
    }
}
