package net.sf.iqser.plugin.web.base;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.zip.Adler32;

import org.apache.log4j.Logger;
import org.htmlparser.util.NodeList;

import com.iqser.core.exception.IQserException;
import com.iqser.core.plugin.AbstractContentProvider;
import com.torunski.crawler.Crawler;
import com.torunski.crawler.events.IParserEventListener;
import com.torunski.crawler.events.ParserEvent;
import com.torunski.crawler.filter.LinkFilterUtil;
import com.torunski.crawler.filter.RegularExpressionFilter;
import com.torunski.crawler.filter.ServerFilter;
import com.torunski.crawler.link.Link;
import com.torunski.crawler.model.MaxDepthModel;
import com.torunski.crawler.parser.htmlparser.SimpleHtmlParser;

/**
 * A base content provider for the iQser GIN Platform to crawle and parse web content 
 * and store links in a database.
 * A content provider has to be implemented for specific content like html pages.
 * 
 * @author Joerg Wurzer
 *
 */
public abstract class CrawlerContentProvider extends AbstractContentProvider implements IParserEventListener {

	/** Version ID */
	private static final long serialVersionUID = 1L;
	
	/** Default link filter */
	private static final String DEFAULT_REGEX = "(.*\\.htm$)||(.*\\.html$)||(.*\\.php\\.*$)||(.*\\.jsp\\.*$)";
	
	/** Logger */
    private static Logger logger = Logger.getLogger( CrawlerContentProvider.class );
	
    /** Crawler */
	private Crawler crawler;
	
	/** Crawler start time */
	private long crawlerstart;
	
    /** database connection */
    private Connection conn;

	/**
	 * Method sets up the crawler and creates tables in the database to persist 
	 * harwested links to parse for the implemented content provider.
	 */
	public void init() {
		logger.debug("init() called");
		
		// Setup the crawler
		crawler = new Crawler();
		
		if (getInitParams().getProperty("server-filter") != null)
			crawler.setLinkFilter(
					LinkFilterUtil.and(
							new ServerFilter(
									getInitParams().getProperty("server-filter") + 
									getInitParams().getProperty("path-filter", "/") ),
							new RegularExpressionFilter(
									getInitParams().getProperty("link-filter", DEFAULT_REGEX) )
							)
					);
		
		crawler.setModel(
				new MaxDepthModel(Integer.valueOf(getInitParams().getProperty("maxdepth-filter", "2"))) );
		
		SimpleHtmlParser parser = new SimpleHtmlParser();
		
		crawler.setParser(parser);
		crawler.addParserListener(this); 
		
		try {
			conn = DriverManager.getConnection(
					getInitParams().getProperty("protocol", "jdbc:mysql:") + 
					"//" + getInitParams().getProperty("database") + 
					"?user=" + getInitParams().getProperty("username") +
					"&password=" + getInitParams().getProperty("password"));
		} catch (SQLException e) {
			logger.error("Could not establish database connection - " + e.getMessage());
		}
		
		// Creating a table if it does't exist
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();;
			stmt.execute("CREATE TABLE IF NOT EXISTS documents" +
					"(id INT AUTO_INCREMENT KEY, url VARCHAR(255) NOT NULL, " +
					"checksum BIGINT NOT NULL, provider VARCHAR(255) NOT NULL, " +
					"checked BIGINT NOT NULL)");;
		} catch (SQLException e) {
			logger.debug("Could't create table - " + e.getMessage());
		}
		
		finally {    
		    if (stmt != null) {
		        try {
		            stmt.close();
		        } catch (SQLException sqlEx) { } // ignore
		        
		        stmt = null;
		    }
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.iqser.core.plugin.AbstractContentProvider#destroy()
	 */
	public void destroy() {
		logger.debug("destroy() called");
		
		try {
			conn.close();
		} catch (SQLException e) {
			logger.error("Could not close the database connection - " + e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.iqser.core.plugin.AbstractContentProvider#getContentUrls()
	 */
	public Collection getContentUrls() {
		// Not yet implemented decrepated method
		return null;
	}

	/**
	 * Starts the crawler to harwest web links determined by the configuration. 
	 * Content objects are added and updatet in the implementes event listener of the crawler.
	 */
	public void doSynchonization() {
		logger.debug("doSynchonization() called");
		
		crawlerstart = System.currentTimeMillis();
		
		try {
			crawler.start(
					getInitParams().getProperty("start-server"), 
					getInitParams().getProperty("start-path", "/") );
		} catch (Exception e) {
			logger.error("Couldn't start crawler - " + e.getMessage());
		}
		
		logger.debug("Crawler has finished");
	}

	/*
	 * The implementation first limits the scope by links, which haven't been checked 
	 * by the current crawler job. Then each link is checked, wether it exists. 
	 */
	public void doHousekeeping() {
		logger.debug("doHousekeeping() called");
		
		try {
			URL url = new URL(getInitParams().getProperty("test-url", "http://www.iqser.com"));
			url.openConnection().connect();
		} catch (IOException ioe) {
			logger.error("Couldn't open connection for test url - " + ioe.getMessage());
			return;
		}
		
		Statement stmt1 = null;
		Statement stmt2 = null;
		ResultSet rs   = null;
		
		try {
			stmt1 = conn.createStatement();
			rs = stmt1.executeQuery("SELECT * FROM documents WHERE checked<" + crawlerstart +
					" AND provider='" + getId() + "'");
			
			while (rs.next()) {
				
				String s = rs.getString("url");
				
				try {
					URL url = new URL(s);
					url.openConnection().connect();
					logger.warn("Not checked by the crawler but still available web content");
				} catch (IOException ioe) {
					logger.error("Couldn't open connection for " + s + " - " + ioe.getMessage());
					
					stmt2 = conn.createStatement();
					stmt2.executeUpdate("DELETE FROM documents WHERE url='" + s + "'" +
							" AND provider='" + getId() + "'");
					
					try {
						this.removeContent(rs.getString("url"));
					} catch (IQserException iqe) {
						logger.error("Couldn't delete object " + s + " - " + iqe.getMessage());
					}
				}
			}	
		} catch (SQLException sqle) {
			logger.error("Couldn't perform sql query or update - " + sqle.getMessage());
		} 
		
		finally {
		    if (rs != null) {
		        try {
		            rs.close();
		        } catch (SQLException sqlEx) { } // ignore
		        
		        rs = null;
		    }
		    
		    if (stmt1 != null) {
		        try {
		            stmt1.close();
		        } catch (SQLException sqlEx) { } // ignore
		        
		        stmt1 = null;
		    }
		    
		    if (stmt2 != null) {
		        try {
		            stmt2.close();
		        } catch (SQLException sqlEx) { } // ignore
		        
		        stmt2 = null;
		    }
		}
		
		logger.debug("Housekeeping has finished");

	}
	
	/**
	 * Implementation of IParserEventListener includes the logic to add, update or delete
	 * content for the index and analysis process of the core engine.
	 */
	public void parse(ParserEvent event) {
		logger.debug("parse() called for link " + event.getLink().getURI());
		
		if (event.getCrawler().equals(crawler) && isContentLink(event.getLink())) {
			
			logger.debug("Content " + event.getLink().getURI() + " matched");
			
			Statement stmt = null;
			ResultSet rs   = null;
			
			long checksum1 = 0;
						
			try {
				URL url = new URL( event.getLink().getURI() );
				checksum1 = url.openConnection().getLastModified();
			} catch (MalformedURLException mfe) {
				logger.error("Malformed url " + event.getLink().getURI() + " - " + mfe.getMessage());
				return;
			} catch (IOException ioe) {
				logger.error("Couldn't read " + event.getLink().getURI() + " - " + ioe.getMessage());
				return;
			}
			
			if ((checksum1 == 0) || (checksum1 == 1)) {
				Adler32 adler32 = new Adler32();
				NodeList nodes = (NodeList)event.getPageData().getData();			
				adler32.update(nodes.toHtml().getBytes());
				checksum1 = adler32.getValue();
			}
			
			try {
				stmt = conn.createStatement();
				rs = stmt.executeQuery("SELECT * FROM documents WHERE url='" + event.getLink().getURI() + "'");
				
				// Has the source already be parsed?
				if (rs.first()) {
					long checksum2 = rs.getLong("checksum");
			
					// Has the source been modified?
					if (checksum1 == checksum2) {
						stmt.executeUpdate("UPDATE documents SET checked=" + 
								String.valueOf(System.currentTimeMillis()) +
								" WHERE id=" + rs.getString("id"));
					} else {
						stmt.executeUpdate("UPDATE documents SET checksum=" + checksum1 +
								", checked=" + String.valueOf(System.currentTimeMillis()) +
								" WHERE id=" + rs.getString("id"));
						this.updateContent(getContent(event.getLink().getURI()));
					}
					
				} else {
					stmt.executeUpdate("INSERT INTO documents VALUES " + 
							"(DEFAULT, '" + event.getLink().getURI() + "', " + checksum1 +
							", '" + getId() + "', " + String.valueOf(System.currentTimeMillis()) + ")");
					this.addContent(getContent(event.getLink().getURI()));
				}
				
			} catch (SQLException e) {
				logger.error("Could't perform database query or update - " + e.getMessage());
			} catch (IQserException e) {
				logger.error("Couldn't perform repository update - " + e.getLocalizedMessage());
			}
			
			finally {
			    if (rs != null) {
			        try {
			            rs.close();
			        } catch (SQLException sqlEx) { } // ignore
			        
			        rs = null;
			    }
			    
			    if (stmt != null) {
			        try {
			            stmt.close();
			        } catch (SQLException sqlEx) { } // ignore
			        
			        stmt = null;
			    }
			}
		}
	}
	
	/**
	 * Checks wether a link has to be parsed by the provider. 
	 * This is determined by the initial parameters in the confugraiton of the plugin.
	 */
	private boolean isContentLink(Link link) {
		if (link.getURI().matches(getInitParams().getProperty("item-filter", DEFAULT_REGEX)))
			return true;
		else
			return false;
	}

}