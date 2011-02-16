package com.iqser.plugin.web.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import com.iqser.core.event.Event;
import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.plugin.web.base.CrawlerContentProvider;

/**
 * A PDF Content Provider of the iQser Web Content Provider Family
 * 
 * @author Jšrg Wurzer
 *
 */
public class PDFContentProvider extends CrawlerContentProvider {

	/** Serial ID */
	private static final long serialVersionUID = 6068337672539646013L;
	
	/** Logger */
	private static  Logger logger = Logger.getLogger( PDFContentProvider.class );

	@Override
	public Content getContent(String url) {
		logger.debug("getContent(String) called for " + url);
		
		Content c = new Content();
		
		c.setContentUrl(url);
		c.setProvider(getId());
		c.setType(getType());
		
		PDDocument doc;
		try {
			doc = PDDocument.load(new URL(url));
			
			if (doc.isEncrypted()) {
				logger.warn("Document " + url + " is encrypted");
			} else {
				createContentAttributes(c, doc);
			}
		} catch (IOException e) {
			logger.error("Couldn't read document - " + e.getMessage());
			return null;
		}
		
		return c;
	}

	@Override
	public Content getContent(InputStream in) {
		logger.debug("getContent(InputStream) called");
		
		Content c = new Content();

		// Setting techincal meta data
		c.setProvider(getId());
		c.setType(getType());
		
		PDDocument doc;
		
		try {
			doc = PDDocument.load(in);
			
			if (doc.isEncrypted()) {
				logger.warn("Document is encrypted");
			} else {
				createContentAttributes(c, doc);
			}
		} catch (IOException e) {
			logger.error("Couldn't read document - " + e.getMessage());
			return null;
		}
		
		return c;
	}

	@Override
	public byte[] getBinaryData(Content c) {
		logger.debug("getBinaryData(Content) is called for " + c.getContentUrl());
		
		try {
			PDDocument doc = PDDocument.load(new URL(c.getContentUrl()));
			
			if (doc.isEncrypted()) {
				logger.warn("Document " + c.getContentUrl() + " is encrypted");
			} else {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				doc.save(out);
				return out.toByteArray();
			}
		} catch (IOException e) {
			logger.error("Couldn't read document - " + e.getMessage());
		} catch (COSVisitorException e) {
			logger.error("Couldn't write document - " + e.getMessage());
		}
		
		
		return null;
	}

	@Override
	public Collection getActions(Content arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performAction(String arg0, Content arg1) {
		// TODO Auto-generated method stub
	
	}

	@Override
	public void onChangeEvent(Event arg0) {
		// TODO Auto-generated method stub

	}
	
	private void createContentAttributes(Content c, PDDocument doc) {
		logger.debug("createContentAttributes(Condent, PDDocument) called");
		
		PDDocumentInformation info = doc.getDocumentInformation();
		
		if ((info.getTitle() != null) && !info.getTitle().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Title", "Title"), 
					info.getTitle(), Attribute.ATTRIBUTE_TYPE_TEXT, true) );
		if ((info.getAuthor() != null) && !info.getAuthor().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Author", "Author"),
					info.getAuthor(), Attribute.ATTRIBUTE_TYPE_TEXT, true) );
		if ((info.getSubject() != null) && !info.getSubject().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Subject", "Subject"),
					info.getSubject(), Attribute.ATTRIBUTE_TYPE_TEXT, true) );
		if ((info.getKeywords() != null) && !info.getKeywords().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Keywords", "Keywords"),
					info.getKeywords(), Attribute.ATTRIBUTE_TYPE_TEXT, true));
		if ((info.getCreator() != null) && !info.getCreator().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Creator", "Creator"),
					info.getCreator(), Attribute.ATTRIBUTE_TYPE_TEXT, false) );
		if ((info.getProducer() != null) && !info.getProducer().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Producer", "Producer"),
					info.getProducer(), Attribute.ATTRIBUTE_TYPE_TEXT, false) );
		try {
			c.addAttribute(new Attribute(getInitParams().getProperty("Created", "Created"),
					String.valueOf(info.getCreationDate().getTimeInMillis()), 
					Attribute.ATTRIBUTE_TYPE_DATE, false) );
		} catch (IOException e) {
			logger.error("Coutldn't read creation date - " + e.getMessage());
		}
		
		try {
			c.setModificationDate(info.getModificationDate().getTimeInMillis());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Coutldn't read creation date - " + e.getMessage());
		}
		
		PDFTextStripper stripper;
		try {
			stripper = new PDFTextStripper();
			c.setFulltext(stripper.getText(doc));
		} catch (IOException e) {
			logger.error("Coutldn't extract text from pdf document - " + e.getMessage());
		}

	}

}
