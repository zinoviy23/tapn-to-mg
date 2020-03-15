package com.github.zinoviy23;

import dk.aau.cs.io.LoadedModel;
import dk.aau.cs.io.TapnXmlLoader;
import dk.aau.cs.io.TimedArcPetriNetNetworkWriter;
import dk.aau.cs.model.tapn.*;
import dk.aau.cs.util.FormatException;
import org.junit.Test;
import pipe.dataLayer.TAPNQuery;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.*;

public class TapnFilesTest {
    @Test
    public void readFileWebserver() throws FormatException {
        InputStream resource = getClass().getClassLoader().getResourceAsStream("Example nets/webserver.tapn");
        assertNotNull(resource);

        assertWebserver(resource);
    }

    @Test
    public void writeFileWebserver() throws FormatException, ParserConfigurationException, TransformerException, IOException {
        TapnXmlLoader loader = new TapnXmlLoader();
        InputStream resource = getClass().getClassLoader().getResourceAsStream("Example nets/webserver.tapn");
        assertNotNull(resource);

        LoadedModel model = loader.load(resource);
        TimedArcPetriNetNetwork network = model.network();

        TimedArcPetriNetNetworkWriter writer = new TimedArcPetriNetNetworkWriter(network, model.templates(), Collections.<TAPNQuery>emptyList(), network.constants());
        File file = new File("./myFile");
        writer.savePNML(file);

        try (InputStream stream = new FileInputStream(file)) {
            assertWebserver(stream);
        }

        assertTrue(file.delete());
    }

    private void assertWebserver(InputStream resource) throws FormatException {
        TapnXmlLoader loader = new TapnXmlLoader();
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
