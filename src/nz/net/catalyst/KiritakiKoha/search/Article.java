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

class Article extends Object {
	public long articleId;
	public long feedId;
	public String title;
	public String isbn;
	public String description;
	public URL url;
}

