package com.iqser.plugin.web.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

/**
 * A generic test case of the Crawler Manager.
 * 
 * @author Jšrg Wurzer
 *
 */
public class CrawlerManagerTest extends TestCase {

	/** Crawler Manager to test */
	private CrawlerManager manager = null;
	
	/** Database connection for crwaled documents and registered provider */
	private Connection conn = null;
	
	protected void setUp() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/base-plugin/src/test/res/log4j.properties");
		
		super.setUp();
		
		manager = CrawlerManager.getInstance();
		manager.setDatabaseConnection("jdbc:mysql:", "localhost/crawler", "root", "master");
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/crawler?user=root&password=master");
			Statement stmt = conn.createStatement();
			
			
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");
			stmt.executeUpdate("DELETE FROM documents");
			
			stmt.execute("CREATE TABLE IF NOT EXISTS providers" +
					"(id INT AUTO_INCREMENT KEY, provider VARCHAR(255) NOT NULL, " +
					"startserver VARCHAR(255) NOT NULL, startpath VARCHAR(255) NOT NULL, " +
					"serverfilter VARCHAR(255) NOT NULL, pathfilter VARCHAR(255) NOT NULL, " +
					"linkfilter VARCHAR(255) NOT NULL, maxdepth INT NOT NULL, " +
					"lastcrawlerstart BIGINT NOT NULL, lastsyncstart BIGINT NOT NULL)");
			stmt.executeUpdate("DELETE FROM providers");
			
			// Creates test data
			stmt.executeUpdate("INSERT INTO providers VALUES(" +
					"DEFAULT, 'com.iqser.plugin.web.html', 'http://www.designerfashion.de', " +
					"'/Seiten/index_svg.html', 'http://www.designerfashion.de', '/Seiten/', " +
					"'(.*\\.html$)||(.*\\.htm$)', '(.*\\.html$)||(.*\\.htm$)', " +
					"2, 1234567890, 123456789)");	
			stmt.executeUpdate("INSERT INTO providers VALUES(" +
					"DEFAULT, 'com.iqser.plugin.web.movie', " +
					"'http://cms.live-im-web.tv', " +
					"'/nw143/article.php?article_file=1108323420.txt&showtopic=Referenzen', " +
					"'none', '/', '(.*\\.html$)||(.*\\.htm$)', '(.*\\.mp4$)||(.*\\.wmv$)', " +
					"1, 1234567890, 123456789)");

		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testStartAllCrawler() {
		manager.startAllCrawler();
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/crawler?user=root&password=master");
			Statement stmt = conn.createStatement();
			
			ResultSet rs = stmt.executeQuery(
					"SELECT Count(url) FROM documents WHERE provider='com.iqser.plugin.web.html'");
			assertTrue(rs.first());
			assertTrue(rs.getInt("Count(url)") > 0);
			
			rs = stmt.executeQuery(
					"SELECT url FROM documents WHERE provider='com.iqser.plugin.web.html'");	
			while (rs.next()) {
				assertTrue(rs.getString("url").endsWith(".html"));
			}
			
			rs = stmt.executeQuery(
					"SELECT Count(url) FROM documents WHERE provider='com.iqser.plugin.web.movie'");
			assertTrue(rs.first());
			assertTrue(rs.getInt("Count(url)") > 0);
		
			rs = stmt.executeQuery(
					"SELECT url FROM documents WHERE provider='com.iqser.plugin.web.movie'");
		
			while (rs.next()) {
				assertTrue(rs.getString("url").endsWith(".mp4") || rs.getString("url").endsWith(".wmv"));
			}

		} catch (SQLException e) {
			fail("Could not establish database connection - " + e.getMessage());
		}
	}

	public void testStopAllCrawler() {
		manager.stopAllCrawler();
		assertTrue(manager.getCrawler() == null);
	}

	public void testAreRunning() {
		boolean areRunning = manager.areRunning();
		assertTrue(!areRunning);
	}

}
