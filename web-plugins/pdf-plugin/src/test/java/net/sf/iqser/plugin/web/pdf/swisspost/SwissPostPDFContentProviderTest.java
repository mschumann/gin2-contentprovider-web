package net.sf.iqser.plugin.web.pdf.swisspost;

import java.util.Properties;

import junit.framework.TestCase;

import net.sf.iqser.plugin.web.pdf.PDFContentProvider;

import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.model.Content;

/**
 * A general test case for the PDF Content Provider
 * 
 * @author Joerg Wurzer
 *
 */
public class SwissPostPDFContentProviderTest extends TestCase {
	
	/** Content provider to test */
	private PDFContentProvider provider = null;

	protected void setUp() throws Exception {
		super.setUp();
		
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/html-plugin/src/test/res/log4j.properties");
		
		Properties initParams = new Properties(); 
		initParams.setProperty("database", System.getProperty("database","localhost/crawler"));
		initParams.setProperty("username", System.getProperty("username", "root"));
		initParams.setProperty("password", System.getProperty("password", "master"));
		
		provider = new PDFContentProvider();
		provider.setId("net.sf.iqser.plugin.web.pdf");
		provider.setType("PDF Document");
		provider.setInitParams(initParams);
		provider.init();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetContentString() {
		Content c = provider.getContent("http://www.admin.ch/ch/e/rs/8/814.012.en.pdf");
		
		assertNotNull(c);
		assertTrue(!c.getFulltext().isEmpty());
		assertTrue(c.getModificationDate() < System.currentTimeMillis());
		assertTrue(c.getAttributes().size() > 0);
		assertEquals("Microsoft Word - 814.012.en.doc", c.getAttributeByName("Title").getValue());
		assertEquals("brabk", c.getAttributeByName("Author").getValue());
		assertEquals("PScript5.dll Version 5.2.2", c.getAttributeByName("Creator").getValue());
		assertNull(c.getAttributeByName("Keywords"));
		assertTrue(c.getFulltext().contains("English is not an official language of the Swiss Confederation."));
		assertTrue(c.getAttributeByName("Author").isKey());
		assertTrue(!c.getAttributeByName("Producer").isKey());
	}	

}
