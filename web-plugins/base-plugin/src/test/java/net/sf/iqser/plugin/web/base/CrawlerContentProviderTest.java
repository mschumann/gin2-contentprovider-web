package net.sf.iqser.plugin.web.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.sf.iqser.plugin.web.test.MockCrawlerContentProvider;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.iqser.core.model.Content;
import com.iqser.gin.developer.test.plugin.provider.ContentProviderTestCase;

/**
 * A general test case for the Crawler Content Provider
 * 
 * @author Järg Wurzer
 *
 */
public class CrawlerContentProviderTest extends ContentProviderTestCase {
		
	/** The Logger */
	private static Logger logger = Logger.getLogger( CrawlerContentProviderTest.class );

	@Test
	public void testDoSynchonization() throws Exception {
		logger.debug("testDoSynchronization() called");
		
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
						
		// Using an implementation of CrawlerContentProvider
		CrawlerContentProvider provider = new MockCrawlerContentProvider();
		
		Properties initParams = new Properties();
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		initParams.setProperty("start-server", "http://www.designerfashion.de");
		initParams.setProperty("start-path", "/Seiten/index_svg.html");
		initParams.setProperty("server-filter", "http://www.designerfashion.de");
		initParams.setProperty("path-filter", "/Seiten/");
		initParams.setProperty("maxdepth-filter", "1");
		initParams.setProperty("item-filter", "(.*\\.html$)||(.*\\.htm$)");
		
		provider.setInitParams(initParams);
		provider.setName("net.sf.iqser.plugin.web.crawler");
		provider.init();
	
		Connection conn = null;
			
		try {
			conn = DriverManager.getConnection(
					provider.getInitParams().getProperty("protocol", "jdbc:mysql:") + 
					"//" + provider.getInitParams().getProperty("database") + 
					"?user=" + provider.getInitParams().getProperty("username") +
					"&password=" + provider.getInitParams().getProperty("password"));
			
			Statement stmt = conn.createStatement();;
			stmt.execute("DELETE FROM pages");	
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
		Content content1 = new Content();
		content1.setType("Web Page");
		content1.setContentUrl("http://www.designerfashion.de/Seiten/index_svg.html");
		content1.setProvider(provider.getName());
		Content content2 = new Content();
		content2.setType("Web Page");
		content2.setContentUrl("http://www.designerfashion.de/Seiten/matt-lehitka.html");
		content2.setProvider(provider.getName());		
		Content content3 = new Content();
		content3.setType("Web Page");
		content3.setContentUrl("http://www.designerfashion.de/Seiten/uebersicht.html");
		content3.setProvider(provider.getName());		
		
		// expectations
		expectsAddContent(content1);
		expectsAddContent(content2);
		expectsAddContent(content3);
		
		// initialize the test
		prepare(); 
		
		// execute the method(s) under test
		provider.doSynchronization();
		
		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM pages");
			assertTrue(rs.first());
			assertEquals(rs.getInt("Count(url)"), 3);
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

	@Test
	public void testDoHousekeeping() throws Exception {
		logger.debug("testDoHousekeeping() called");
		
		// Using an implementation of CrawlerContentProvider
		CrawlerContentProvider provider = new MockCrawlerContentProvider();
		
		Properties initParams = new Properties();
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		initParams.setProperty("start-server", "http://www.designerfashion.de");
		initParams.setProperty("start-path", "/Seiten/index_svg.html");
		initParams.setProperty("server-filter", "http://www.designerfashion.de");
		initParams.setProperty("path-filter", "/Seiten/");
		initParams.setProperty("maxdepth-filter", "1");
		initParams.setProperty("item-filter", "(.*\\.html$)||(.*\\.htm$)");
		
		provider.setInitParams(initParams);
		provider.setName("net.sf.iqser.plugin.web.crawler");
		provider.init();
		
		Connection conn = null;

		try {
			conn = DriverManager.getConnection(
					provider.getInitParams().getProperty("protocol", "jdbc:mysql:") + 
					"//" + provider.getInitParams().getProperty("database") + 
					"?user=" + provider.getInitParams().getProperty("username") +
					"&password=" + provider.getInitParams().getProperty("password"));
			
			Statement stmt = conn.createStatement();;
			stmt.execute("DELETE FROM pages");	
			
			stmt.executeUpdate("INSERT INTO pages VALUES " + 
					"(DEFAULT, 'http://www.wurzer.org/no-page.html', 1, 'net.sf.iqser.plugin.web.crawler', " 
					+ 0 + ", " + String.valueOf(System.currentTimeMillis()) + ")");
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		} 

		expectsRemoveContent("net.sf.iqser.plugin.web.crawler", "http://www.wurzer.org/no-page.html");
		
		// initialize the test
		prepare(); 
		provider.init();		
		
		// execute the method(s) under test
		provider.doHousekeeping();
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

	@Test
	public void testParse() {
		logger.debug("testParse() called");
		
		// Not yet implemented
	}

}
