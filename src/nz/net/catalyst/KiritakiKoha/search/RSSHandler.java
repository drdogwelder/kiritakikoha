package nz.net.catalyst.KiritakiKoha.search;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nz.net.catalyst.KiritakiKoha.Record;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Log;

public class RSSHandler extends DefaultHandler {
	static final String TAG = LogConfig.getLogTag(RSSHandler.class);
	// whether DEBUG level logging is enabled (whether globally, or explicitly
	// for this log tag)
	static final boolean DEBUG = LogConfig.isDebug(TAG);
	// whether VERBOSE level logging is enabled
	static final boolean VERBOSE = LogConfig.VERBOSE;

	// Used to define what elements we are currently in
	private boolean inItem = false;
	private boolean inTitle = false;
	private boolean inLink = false;
	private boolean inDescription = false;
	private boolean inISBN = false;

	// Feed and Record objects to use for temporary storage
	private Record currentRecord = new Record();

	// Number of Records added so far
    ArrayList<Record> Records = new ArrayList<Record>();

	// Number of Records to download
	private static final int RECORDS_LIMIT = 500;

	// The possible values for targetFlag
	// A flag to know if looking for Records or Feed name
	//private int targetFlag = 5;
	
	public void unparsedEntityDecl (String name, String publicId, String systemId, String notationName) {
		Log.d(TAG, "unparsedEntityDecl: " + name);
	}	
	public void skippedEntity (String name) {
		Log.d(TAG, "skippedEntity: " + name);		
	}
	public void startElement(String uri, String name, String qName,
			Attributes atts) {
		if (name.trim().equals("title"))
			inTitle = true;
		else if (name.trim().equals("item"))
			inItem = true;
		else if (name.trim().equals("description"))
			inDescription = true;
		else if (name.trim().equals("isbn"))
			inISBN = true;
		else if (name.trim().equals("guid"))
			inLink = true;
	}

	public void endElement(String uri, String name, String qName)
			throws SAXException {

		if (name.trim().equals("title"))
			inTitle = false;
		else if (name.trim().equals("item"))
			inItem = false;
		else if (name.trim().equals("description"))
			inDescription = false;
		else if (name.trim().equals("isbn"))
			inISBN = false;
		else if (name.trim().equals("guid"))
			inLink = false;

		if (currentRecord.getURL() != null && currentRecord.getTitle() != null) {
			Record a = new Record();
			a = currentRecord.clone();
			Records.add(a);

			// Lets check if we've hit our limit on number of Records
			if (Records.size() >= RECORDS_LIMIT)
				throw new SAXException();

			currentRecord = new Record();
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		String chars = new String(ch, start, length);

		try {
			// If not in item, then title/link refers to feed
			if (inItem) {
				if (inLink)
					currentRecord.setURL(new URL(chars));
				else if (inTitle) {
					if ( currentRecord.getTitle() == null ) 
						currentRecord.setTitle(chars.trim());
					else 
						currentRecord.setTitle((currentRecord.getTitle() + " " + chars.trim()).trim());
				}
				else if (inDescription) {
					if ( currentRecord.getDescription() == null )
						currentRecord.setDescription(chars.trim());
					else
						currentRecord.setDescription((currentRecord.getDescription() + " " + chars.trim()).trim());
				}
				else if (inISBN) {
					if ( currentRecord.getISBN() == null )
						currentRecord.setISBN(chars.trim());
					else
						currentRecord.setISBN((currentRecord.getISBN() + " " + chars.trim()).trim());
				}
				//else if ( chars.trim().length() > 0 )
				//	Log.d(TAG, "Unwanted chars: " + chars.trim());
				
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "characters" + e.toString());
		} catch (StringIndexOutOfBoundsException e) {
			Log.e(TAG, "characters" + e.toString());
		}
	}

	public ArrayList<Record> getItems(Context ctx, URL url) throws IOException {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			xr.parse(new InputSource(url.openStream()));
			
		} catch (IOException e) {
			Log.e(TAG, "getItems: IOException: " + e.toString());
			throw new IOException("Connection failed.");
		} catch (SAXException e) {
			Log.e(TAG, "getItems: SAXException: " + e.toString());
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "getItems: ParserConfigurationException: " + e.toString());
		}
		return Records;
	}
}
