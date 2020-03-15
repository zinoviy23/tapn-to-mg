/*
 * Export class
 *
 * Created on 27-Feb-2004
 *
 */
package pipe.gui;

import dk.aau.cs.TCTL.CTLParsing.TAPAALCTLQueryParser;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.TCTL.TCTLPathPlaceHolder;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.print.DocFlavor;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;

import dk.aau.cs.TCTL.visitors.CTLQueryVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.gui.TabContent;
import dk.aau.cs.io.PNMLWriter;
import dk.aau.cs.model.tapn.NetworkMarking;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.TAPNComposer;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.NetWriter;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.GuiFrame.GUIMode;
import pipe.gui.graphicElements.PetriNetObject;
import pipe.gui.widgets.filebrowser.FileBrowser;

/**
 * Class for exporting things to other formats, as well as printing.
 * 
 * @author Maxim
 */
public class Export {

	public static final int PNG = 1;
	public static final int POSTSCRIPT = 2;
	public static final int PRINTER = 3;
	public static final int TIKZ = 5;
	public static final int PNML = 6;
	public static final int QUERY = 7;	

	private static void toPnml(DrawingSurfaceImpl g, String filename) 
			throws NullPointerException, DOMException, TransformerConfigurationException, 
			IOException, ParserConfigurationException, TransformerException {
		TabContent currentTab = CreateGui.getCurrentTab();
		NetworkMarking currentMarking = null;
		if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)){
			currentMarking = currentTab.network().marking();
			currentTab.network().setMarking(CreateGui.getAnimator().getInitialMarking());
		}

		NetWriter tapnWriter = new PNMLWriter(
				currentTab.network(),
				currentTab.getGuiModels()
				);

		tapnWriter.savePNML(new File(filename));

		if(CreateGui.getApp().getGUIMode().equals(GUIMode.animation)){
			currentTab.network().setMarking(currentMarking);
		}
	}
	
	private static void toQueryXML(String filename){
		toQueryXML(CreateGui.getCurrentTab().network(), filename, CreateGui.getCurrentTab().queries());
		
	}
	
	public static void toQueryXML(TimedArcPetriNetNetwork network, String filename, Iterable<TAPNQuery> queries){
		try{
	            ITAPNComposer composer = new TAPNComposer(new MessengerImpl(), true);
	            NameMapping mapping = composer.transformModel(network).value2();
	            Iterator<TAPNQuery> queryIterator = queries.iterator();
	            PrintStream queryStream = new PrintStream(filename);
	            CTLQueryVisitor XMLVisitor = new CTLQueryVisitor();
	            
	            while(queryIterator.hasNext()){
	                TAPNQuery clonedQuery = queryIterator.next().copy();

                            // Attempt to parse and possibly transform the string query using the manual edit parser
                            TCTLAbstractProperty newProperty;
                            try {
                                newProperty = TAPAALCTLQueryParser.parse(clonedQuery.getProperty().toString());
                            } catch (Throwable ex) {
                                newProperty = clonedQuery == null ? new TCTLPathPlaceHolder() : clonedQuery.getProperty();
                            }
                            newProperty.accept(new RenameAllPlacesVisitor(mapping), null);
                            newProperty.accept(new RenameAllTransitionsVisitor(mapping), null);
                            XMLVisitor.buildXMLQuery(newProperty, clonedQuery.getName());
	            }
	            queryStream.print(XMLVisitor.getFormatted());
				
				queryStream.close();
		} catch(FileNotFoundException e) {
			System.err.append("An error occurred while exporting the queries to XML.");
		}
	}

	public static void toPostScript(Object g, String filename)
			throws PrintException, IOException {
		// Input document type
		DocFlavor flavour = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
		// Output stream MIME type
		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();

		// Look up a print service factory that can handle this job
		StreamPrintServiceFactory[] factories = StreamPrintServiceFactory
				.lookupStreamPrintServiceFactories(flavour, psMimeType);
		if (factories.length == 0) {
			throw new RuntimeException(
					"No suitable factory found for export to PS");
		}

		FileOutputStream f = new FileOutputStream(filename);
		// Get a print service from the factory, create a print job and print
		factories[0].getPrintService(f).createPrintJob().print(
				new SimpleDoc(g, flavour, null),
				new HashPrintRequestAttributeSet());
		f.close();
	}

	public static void toPNG(DrawingSurfaceImpl g, String filename) throws IOException {
		Iterator<ImageWriter> i = ImageIO.getImageWritersBySuffix("png");
		if (!i.hasNext()) {
			throw new RuntimeException("No ImageIO exporters can handle PNG");
		}

		File f = new File(filename);
		BufferedImage img = new BufferedImage(g.getPreferredSize().width, g.getPreferredSize().height, BufferedImage.TYPE_3BYTE_BGR);
		g.print(img.getGraphics());
		Rectangle r = g.calculateBoundingRectangle();
		BufferedImage croppedImg = img.getSubimage(r.x, r.y, r.width, r.height);
		ImageIO.write(croppedImg, "png", f);
	}

	private static void toPrinter(DrawingSurfaceImpl g) throws PrintException {
		PrinterJob pjob = PrinterJob.getPrinterJob(); 
		PageFormat pf = pjob.defaultPage(); 
		pjob.setPrintable(g, pf);
		if(pjob.printDialog()){
			try {
				pjob.print();
			} catch (PrinterException e) {
				throw new PrintException(e);
			} 
		}
	}

	public static void exportGuiView(DrawingSurfaceImpl g, int format,
			DataLayer model) {
		if (g.getComponentCount() == 0) {
			return;
		}

		String filename = null;
		if (CreateGui.getCurrentTab().getFile() != null) {
			filename = CreateGui.getCurrentTab().getFile().getAbsolutePath();
			// change file extension
			int dotpos = filename.lastIndexOf('.');
			if (dotpos > filename.lastIndexOf(System.getProperty("file.separator"))) {
				// dot is for extension
				filename = filename.substring(0, dotpos + 1);
				switch (format) {
				case PNG:
					filename += "png";
					break;
				case POSTSCRIPT:
					filename += "ps";
					break;
				case TIKZ:
					filename += "tex";
					break;
				case PNML:
					filename += "pnml";
					break;
				case QUERY:
					filename = filename.substring(0, dotpos);
					filename += "-queries.xml";
					break;
				}
				
			}
		}

		boolean gridEnabled = Grid.isEnabled();
		setupViewForExport(g, gridEnabled);

		try {
			switch (format) {
			case PNG:
				filename = FileBrowser.constructor("PNG image", "png", filename).saveFile();
				if (filename != null) {
					toPNG(g, filename);
				}
				break;
			case POSTSCRIPT:
				filename = FileBrowser.constructor("PostScript file", "ps", filename)
				.saveFile();
				if (filename != null) {
					toPostScript(g, filename);
				}
				break;
			case PRINTER:
				toPrinter(g);
				break;
			case TIKZ:
				Object[] possibilities = { "Only the TikZ figure",
				"Full compilable LaTex including your figure" };
				String figureOptions = (String) JOptionPane.showInputDialog(
						CreateGui.getApp(),
						"Choose how you would like your TikZ figure outputted: \n",
						"Export to TikZ", JOptionPane.PLAIN_MESSAGE,
						null, possibilities, "Only the TikZ figure");
				TikZExporter.TikZOutputOption tikZOption = TikZExporter.TikZOutputOption.FIGURE_ONLY;
				if (figureOptions == null)
					break;

				if (figureOptions == possibilities[0])
					tikZOption = TikZExporter.TikZOutputOption.FIGURE_ONLY;
				if (figureOptions == possibilities[1])
					tikZOption = TikZExporter.TikZOutputOption.FULL_LATEX;

				filename = FileBrowser.constructor("TikZ figure", "tex", filename).saveFile();
				if (filename != null) {
					TikZExporter output = new TikZExporter(model, filename, tikZOption);
					output.ExportToTikZ();
				}
				break;
			case PNML:
				filename = FileBrowser.constructor("PNML file", "pnml", filename)
				.saveFile();
				if (filename != null) {
					toPnml(g, filename);
				}
				break;
			case QUERY:
				filename = FileBrowser.constructor("Query XML file", "xml", filename).saveFile(CreateGui.getAppGui().getCurrentTabName().replaceAll(".tapn", "-queries"));
				if (filename != null) {
					toQueryXML(filename);
				}
				break;
			}
		} catch (NullPointerException e) {
		} catch (Exception e) {
			// There was some problem with the action
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There were errors performing the requested action:\n" + e,
					"Error", JOptionPane.ERROR_MESSAGE);
		}

		resetViewAfterExport(g, gridEnabled);

		return;
	}

	private static void resetViewAfterExport(DrawingSurfaceImpl g,
			boolean gridEnabled) {
		if (gridEnabled) {
			Grid.enableGrid();
		}
		g.repaint();
	}

	private static void setupViewForExport(DrawingSurfaceImpl g, boolean gridEnabled) {
		// Stuff to make it export properly
		g.updatePreferredSize();
		if (gridEnabled) {
			Grid.disableGrid();
		}
	}
}
