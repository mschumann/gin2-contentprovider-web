package net.sf.iqser.plugin.web.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import net.sf.iqser.plugin.web.base.CrawlerContentProvider;

import org.apache.log4j.Logger;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;
import com.iqser.core.model.Parameter;

/**
 * A PDF Content Provider of the iQser Web Content Provider Family
 * 
 * @author Joerg Wurzer
 *
 */
public class PDFContentProvider extends CrawlerContentProvider {
	
	/** Logger */
	private static  Logger logger = Logger.getLogger( PDFContentProvider.class );

	@Override
	public Content createContent(String url) {
		logger.debug("createContent(String) called for " + url);
		
		Content c = new Content();
		
		c.setContentUrl(url);
		c.setProvider(getName());
		c.setType(getInitParams().getProperty("Type", "PDF Document"));
		
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
	public Content createContent(InputStream in) {
		logger.debug("createContent(InputStream) called");
		
		Content c = new Content();

		// Setting technical meta data
		c.setProvider(getName());
		c.setType(getInitParams().getProperty("Type", "PDF Document"));
		
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
	public Collection<String> getActions(Content arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void performAction(String arg0, Collection<Parameter> arg1,
			Content arg2) {
		// TODO Auto-generated method stub
		
	}

	private void createContentAttributes(Content c, PDDocument doc) {
		logger.debug("createContentAttributes(Condent, PDDocument) called");
		
		PDDocumentInformation info = doc.getDocumentInformation();
		
		if ((info.getTitle() != null) && !info.getTitle().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Title", "Title").toUpperCase(), 
					info.getTitle(), Attribute.ATTRIBUTE_TYPE_TEXT, true) );
		if ((info.getAuthor() != null) && !info.getAuthor().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Author", "Author").toUpperCase(),
					info.getAuthor(), Attribute.ATTRIBUTE_TYPE_TEXT, true) );
		if ((info.getSubject() != null) && !info.getSubject().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Subject", "Subject").toUpperCase(),
					info.getSubject(), Attribute.ATTRIBUTE_TYPE_TEXT, true) );
		if ((info.getKeywords() != null) && !info.getKeywords().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Keywords", "Keywords").toUpperCase(),
					info.getKeywords(), Attribute.ATTRIBUTE_TYPE_TEXT, true));
		if ((info.getCreator() != null) && !info.getCreator().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Creator", "Creator").toUpperCase(),
					info.getCreator(), Attribute.ATTRIBUTE_TYPE_TEXT, false) );
		if ((info.getProducer() != null) && !info.getProducer().isEmpty())
			c.addAttribute(new Attribute(getInitParams().getProperty("Producer", "Producer").toUpperCase(),
					info.getProducer(), Attribute.ATTRIBUTE_TYPE_TEXT, false) );
		try {
			c.addAttribute(new Attribute(getInitParams().getProperty("Created", "Created").toUpperCase(),
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
