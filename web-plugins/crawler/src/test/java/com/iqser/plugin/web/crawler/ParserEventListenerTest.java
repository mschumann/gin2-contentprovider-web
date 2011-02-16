package com.iqser.plugin.web.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.PropertyConfigurator;

import com.torunski.crawler.Crawler;
import com.torunski.crawler.core.ICrawler;
import com.torunski.crawler.events.ParserEvent;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.parser.PageData;

import junit.framework.TestCase;

/**
 * A test case for the iQser specific Parser Event Listener.
 * 
 * @author Jšrg Wurzer
 *
 */
public class ParserEventListenerTest extends TestCase {
	
	/** Listener to test */
	private ParserEventListener pListener = null;
	
	/** Crawler for testing */
	private ICrawler crawler = null;
	
	/** The database connection to cache */
	private Connection conn = null;

	protected void setUp() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
		
		super.setUp();
		
		crawler = new Crawler();
		pListener = new ParserEventListener("(.*\\.html$)||(.*\\.htm$)", "com.iqser.plugin.web.html");		
		pListener.setDatabaseConnection("jdbc:mysql:", "localhost/crawler", "root", "master");
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/crawler?user=root&password=master");
			Statement stmt = conn.createStatement();
			
			// Enable the database to write testadata
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");;
			stmt.executeUpdate("DELETE FROM documents");		
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}

	}

	protected void tearDown() throws Exception {
		pListener.destroy();
		super.tearDown();
	}

	public void testParse() {
		
		Link link1 = new Link("http://www.some-server.com/page1.html");
		Link link2 = new Link("http://www.some-server.com/page.pdf");
		Collection<Link> outLinks = new ArrayList();
		outLinks.add(new Link("http//www.some-server.com/page2.html"));
		
		PageData page1 = new MockPageData(link1);
		PageData page2 = new MockPageData(link2);
		
		ParserEvent pEvent1 = new ParserEvent(crawler, link1, page1, outLinks);
		ParserEvent pEvent2 = new ParserEvent(crawler, link2, page2, outLinks);
		
		pListener.parse(pEvent1);
		pListener.parse(pEvent2);
		pListener.parse(pEvent1);
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/crawler?user=root&password=master");
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT Count(url) FROM documents");
			
			assertTrue(rs.next());
			assertEquals(1, rs.getInt("Count(url)"));
			
			rs = stmt.executeQuery("SELECT url FROM documents");
			assertTrue(rs.next());
			assertEquals("http://www.some-server.com/page1.html", rs.getString("url"));
		
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

}
