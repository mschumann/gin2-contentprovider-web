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

import net.sf.iqser.plugin.web.test.MockBaseContentProvider;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Test;

import com.iqser.core.model.Content;
import com.iqser.gin.developer.test.plugin.provider.ContentProviderTestCase;

/**
 * A general test case for the Base Content Provider
 * 
 * @author Jörg Wurzer
 *
 */
public class BaseContentProviderTest extends ContentProviderTestCase {
	
	/** The Logger */
	private static Logger logger = Logger.getLogger( BaseContentProviderTest.class );

	@Test
	public void testDoSynchonization() throws Exception {
		logger.debug("testDoSynchronization() called");

		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
						
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
		initParams.setProperty("link-filter", "(.*\\.html$)||(.*\\.htm$)");
		
		// Using an implementation of BaseContentProvider
		MockBaseContentProvider provider = new MockBaseContentProvider();
		provider.setInitParams(initParams);
		provider.setName("net.sf.iqser.plugin.web.base");
		provider.init();

		// Prepare test data
		Content content1 = new Content();
		content1.setType("Mock Content");
		content1.setContentUrl("http://www.designerfashion.de/Seiten/index_svg.html");
		content1.setProvider(provider.getName());
		Content content2 = new Content();
		content2.setType("Mock Content");
		content2.setContentUrl("http://www.designerfashion.de/Seiten/matt-lehitka.html");
		content2.setProvider(provider.getName());		
		Content content3 = new Content();
		content3.setType("Mock Content");
		content3.setContentUrl("http://www.designerfashion.de/Seiten/uebersicht.html");
		content3.setProvider(provider.getName());
		
		Connection conn = null;
		
		try {
			conn = DriverManager.getConnection(
					provider.getInitParams().getProperty("protocol", "jdbc:mysql:") + 
					"//" + provider.getInitParams().getProperty("database") + 
					"?user=" + provider.getInitParams().getProperty("username") +
					"&password=" + provider.getInitParams().getProperty("password"));
			
			Statement stmt = conn.createStatement();
			
			// Insert new test data to simulate a crawler job
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");
			
			stmt.executeUpdate("DELETE FROM documents");
			stmt.executeUpdate("DELETE FROM providers");
			
			stmt.executeUpdate("INSERT INTO documents VALUES(" +
					"DEFAULT, '" + content1.getContentUrl() + "', 123456789, " +
					"'" + content1.getProvider() + "', " + (System.currentTimeMillis() + 1000) + ")");	
			stmt.executeUpdate("INSERT INTO documents VALUES(" +
					"DEFAULT, '" + content2.getContentUrl() + "', 123456789, " +
					"'" + content2.getProvider() + "', " + (System.currentTimeMillis() + 1000) + ")");	
			stmt.executeUpdate("INSERT INTO documents VALUES(" +
					"DEFAULT, '" + content3.getContentUrl() + "', 123456789, " +
					"'" + content3.getProvider() + "', " + (System.currentTimeMillis() + 1000) + ")");	
			stmt.close();
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
		// Expectations
		expectsIsExistingContent(content1.getProvider(), content1.getContentUrl(), false);
		expectsIsExistingContent(content2.getProvider(), content2.getContentUrl(), false);
		expectsIsExistingContent(content3.getProvider(), content3.getContentUrl(), false);
		expectsAddContent(content1);
		expectsAddContent(content2);
		expectsAddContent(content3);
		
		// initialize the test
		prepare(); 
		provider.init();

		// execute the method(s) under test
		provider.doSynchronization();
		
		if (conn != null) {
			try {
				Statement stmt = conn.createStatement();;
				ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM documents");
				assertTrue(rs.first());
				assertEquals(3, rs.getInt("Count(url)"));
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				fail("Could not establish database connection - " + e.getMessage());
			}
		}
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

	@Test
	public void testDoHousekeeping() throws Exception {
		logger.debug("tstDoHousekeeping() called");
		
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
		initParams.setProperty("link-filter", "(.*\\.html$)||(.*\\.htm$)");
		
		// Using an implementation of BaseContentProvider
		MockBaseContentProvider provider = new MockBaseContentProvider();
		provider.setInitParams(initParams);
		provider.setName("net.sf.iqser.plugin.web.base");
		
		// Prepare test data
		Content content1 = new Content();
		content1.setType(initParams.getProperty("type"));
		content1.setContentUrl("http://www.designerfashion.de/nothing.html");
		content1.setProvider(provider.getName());
		Content content2 = new Content();
		content2.setType(initParams.getProperty("type"));
		content2.setContentUrl("http://www.designerfashion.de/nothingelse.html");
		content2.setProvider(provider.getName());		
		Content content3 = new Content();
		content3.setType(initParams.getProperty("type"));
		content3.setContentUrl("http://www.designerfashion.de/nothingatall.html");
		content3.setProvider(provider.getName());
				
		Connection conn = null;
		
		try {
			conn = DriverManager.getConnection(
					provider.getInitParams().getProperty("protocol", "jdbc:mysql:") + 
					"//" + provider.getInitParams().getProperty("database") + 
					"?user=" + provider.getInitParams().getProperty("username") +
					"&password=" + provider.getInitParams().getProperty("password"));
			
			Statement stmt = conn.createStatement();
			
			// Insert new test data to simulate a crawler job
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");
			
			stmt.executeUpdate("DELETE FROM documents");
			stmt.executeUpdate("DELETE FROM providers");
			
			stmt.executeUpdate("INSERT INTO documents VALUES(" +
					"DEFAULT, '" + content1.getContentUrl() + "', 123456789, " +
					"'" + content1.getProvider() + "', " + 1 + ")");	
			
			stmt.executeUpdate("INSERT INTO documents VALUES(" +
					"DEFAULT, '" + content2.getContentUrl() + "', 123456789, " +
					"'" + content2.getProvider() + "', " + 1 + ")");	

			stmt.executeUpdate("INSERT INTO documents VALUES(" +
					"DEFAULT, '" + content3.getContentUrl() + "', 123456789, " +
					"'" + content3.getProvider() + "', " + 1 + ")");	
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
		expectsRemoveContent(content1.getProvider(), content1.getContentUrl());
		expectsRemoveContent(content2.getProvider(), content2.getContentUrl());
		expectsRemoveContent(content3.getProvider(), content3.getContentUrl());
		
		// Start test
		prepare();
		provider.init();
		provider.doHousekeeping();
		
		try {
			Statement stmt = conn.createStatement();;
			ResultSet rs = stmt.executeQuery("SELECT COUNT(url) FROM documents");
			assertTrue(rs.first());
			assertEquals(0, rs.getInt("Count(url)"));	
		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}
}
