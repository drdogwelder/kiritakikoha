package nz.net.catalyst.KiritakiKoha.search;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nz.net.catalyst.KiritakiKoha.GlobalResources;
import nz.net.catalyst.KiritakiKoha.log.LogConfig;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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

	// Feed and Article objects to use for temporary storage
	private Article currentArticle = new Article();
	private Feed currentFeed = new Feed();
	
	// Number of articles added so far
    List<Article> articles = new ArrayList<Article>();

	// Number of articles to download
	private static final int ARTICLES_LIMIT = 500;

	// The possible values for targetFlag
	private static final int TARGET_FEED = 0;
	private static final int TARGET_ARTICLES = 5;

	// A flag to know if looking for Articles or Feed name
	private int targetFlag;
	
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

		if (currentArticle.url != null && currentArticle.title != null) {
			
			Log.d(TAG, "TARGET_ARTICLE: ID = " + currentArticle.articleId + ", Title=" + currentArticle.title + 
								"URL=" + currentArticle.url);
			
			//Message msg =  mHandler.obtainMessage(GlobalResources.ITEM_FOUND, currentArticle.title);
			//msg.arg1 = articlesAdded;
			//mHandler.sendMessage(msg);		
			//if (VERBOSE) Log.v(TAG, "RSSHandler triggered item found");
							
			Article a = new Article();
			a = currentArticle.clone();
			articles.add(a);

			// Lets check if we've hit our limit on number of articles
			if (articles.size() >= ARTICLES_LIMIT)
				throw new SAXException();

			currentArticle.title = null;
			currentArticle.url = null;

		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		super.characters(ch, start, length);
		String chars = new String(ch, start, length);

		try {
			// If not in item, then title/link refers to feed
			if (!inItem) {
				if (inTitle)
					currentFeed.title = chars;
			} else {
				if (inLink)
					currentArticle.url = new URL(chars);
				else if (inTitle)
					currentArticle.title = chars.trim();
				else if (inDescription)
					currentArticle.description = chars.trim();
				else if (inISBN)
					currentArticle.isbn = chars.trim();
				else if ( chars.trim().length() > 0 )
					Log.d(TAG, "Unwanted chars: " + chars.trim());
				
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "characters" + e.toString());
		}

	}

	public List<Article> getItems(Context ctx, URL url) throws IOException {
		try {
			targetFlag = TARGET_ARTICLES;

			currentFeed.url = url;

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
		return articles;
	}
}
