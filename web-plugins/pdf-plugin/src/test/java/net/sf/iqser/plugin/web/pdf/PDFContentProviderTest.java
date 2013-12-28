package net.sf.iqser.plugin.web.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import net.sf.iqser.plugin.web.pdf.PDFContentProvider;

import org.apache.log4j.PropertyConfigurator;

import com.iqser.core.model.Content;
import com.iqser.gin.developer.test.plugin.provider.ContentProviderTestCase;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * A general test case for the PDF Content Provider
 * 
 * @author Joerg Wurzer
 *
 */
public class PDFContentProviderTest extends ContentProviderTestCase {

	@Test
	public void testCreateContentString() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/html-plugin/src/test/res/log4j.properties");
		
		Properties initParams = new Properties(); 
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		
		PDFContentProvider provider = new PDFContentProvider();
		provider.setName("net.sf.iqser.plugin.web.pdf");
		provider.setInitParams(initParams);
		
		String contentUrl = "http://www.wurzer.org/" +
				"Homepage/Publikationen/Eintrage/2009/10/7_Wissen_dynamisch_organisieren_files/" +
				"KnowTech%202009%20-%20Wissen%20dynamisch%20organisieren.pdf";		
		
		// initialize the test
		prepare(); 
		provider.init();		
		
		// execute the method(s) under test
		Content c = provider.createContent(contentUrl);
				
		assertNotNull(c);
		assertTrue(!c.getFulltext().isEmpty());
		assertTrue(c.getModificationDate() < System.currentTimeMillis());
		assertTrue(c.getAttributes().size() > 0);
		assertEquals("KnowTech 2009 - Wissen dynamisch organisieren", c.getAttributeByName("Title").getValue());
		assertEquals("Jörg Wurzer", c.getAttributeByName("Author").getValue());
		assertEquals("Pages", c.getAttributeByName("Creator").getValue());
		assertNull(c.getAttributeByName("Keywords"));
		assertTrue(c.getFulltext().startsWith("Wissen dynamisch organisieren"));
		assertTrue(c.getAttributeByName("Author").isKey());
		assertTrue(!c.getAttributeByName("Producer").isKey());
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

	@Test
	public void testGetContentInputStream() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/html-plugin/src/test/res/log4j.properties");
		
		Properties initParams = new Properties(); 
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		
		PDFContentProvider provider = new PDFContentProvider();
		provider.setName("net.sf.iqser.plugin.web.pdf");
		provider.setInitParams(initParams);
		
		URL url;
		try {
			url = new URL("http://www.wurzer.org/" +
					"Homepage/Publikationen/Eintrage/2009/10/7_Wissen_dynamisch_organisieren_files/" +
					"KnowTech%202009%20-%20Wissen%20dynamisch%20organisieren.pdf");
			InputStream in = url.openStream();
			
			// initialize the test
			prepare(); 
			provider.init();	
			
			// execute the method(s) under test
			Content c = provider.createContent(in);
			
			assertNotNull(c);
			assertTrue(!c.getFulltext().isEmpty());
			assertTrue(c.getModificationDate() < System.currentTimeMillis());
			assertTrue(c.getAttributes().size() > 0);
			assertEquals("KnowTech 2009 - Wissen dynamisch organisieren", c.getAttributeByName("Title").getValue());
			assertEquals("Jörg Wurzer", c.getAttributeByName("Author").getValue());
			assertEquals("Pages", c.getAttributeByName("Creator").getValue());
			assertNull(c.getAttributeByName("Keywords"));
			assertTrue(c.getFulltext().startsWith("Wissen dynamisch organisieren"));
			assertTrue(c.getAttributeByName("Author").isKey());
			assertTrue(!c.getAttributeByName("Producer").isKey());	
			
			// destroy the plug-in
			provider.destroy();
		
			// verify if your expectations were met
			verify(); 
		} catch (MalformedURLException e) {
			fail("Malformed url - " + e.getMessage());
		} catch (IOException e) {
			fail("Couldn't read file - " + e.getMessage());
		}
	}

	@Test
	public void testGetBinaryData() throws Exception {
		PropertyConfigurator.configure(
				System.getProperty("user.dir") + "/html-plugin/src/test/res/log4j.properties");
		
		Properties initParams = new Properties(); 
		initParams.setProperty("database", "localhost/crawler");
		initParams.setProperty("username", "root");
		initParams.setProperty("password", "master");
		
		PDFContentProvider provider = new PDFContentProvider();
		provider.setName("net.sf.iqser.plugin.web.pdf");
		provider.setInitParams(initParams);

		// initialize the test
		prepare(); 
		provider.init();	
		
		// execute the method(s) under test
		Content c = new Content();
		c.setContentUrl("http://www.wurzer.org/" +
					"Homepage/Publikationen/Eintrage/2009/10/7_Wissen_dynamisch_organisieren_files/" +
					"KnowTech%202009%20-%20Wissen%20dynamisch%20organisieren.pdf");
		byte[] byteArr = provider.getBinaryData(c);
		assertNotNull(byteArr);
		
		// destroy the plug-in
		provider.destroy();
	
		// verify if your expectations were met
		verify(); 
	}

}
