package nz.net.catalyst.KiritakiKoha.search;

import java.net.URL;

/*
 *      <item> 
       <title>Conquerors of time : </title> 
       <isbn>0719555175</isbn> 
       <link>http://opac.koha.workbuffer.org/cgi-bin/koha/opac-detail.pl?biblionumber=2470</link> 
       <description><![CDATA[
 
 
 
 
 
	   <p>By Fishlock, Trevor,. 
	   London : J. Murray, 2004
                        . xiii, 444 p., [16] p. of plates :
                        
                         24 cm.. 
                         0719555175 </p><p> 
 
<a href="http://opac.koha.workbuffer.org/cgi-bin/koha/opac-reserve.pl?biblionumber=2470">Place Hold on <i>Conquerors of time :</i></a></p> 
 
						]]></description> 
       <guid>http://opac.koha.workbuffer.org/cgi-bin/koha/opac-detail.pl?biblionumber=2470</guid> 
     </item> 
 */

class Record extends Object {
	//private long articleId;
	//private long feedId;
	private String title;
	private String isbn;
	private String description;
	private URL url;
	
	public Record clone() {
		Record a = new Record();
		a.title = this.title;
		a.isbn = this.isbn;
		a.description = this.description;
		a.url = this.url;
		return a;
	}
	
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public URL getURL() {
		return url;
	}
	public String getISBN() {
		return isbn;
	}
	public void setTitle(String t) {
		title = t;
	}
	public void setDescription(String d) {
		description= d;
	}
	public void setURL(URL u) {
		url = u;
	}
	public void setISBN(String i) {
		isbn = i;
	}
	public String getGroup() {
		// In the meantime just set the article ID, i.e force no grouping
		return this.title;
	}
}

