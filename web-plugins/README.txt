Version 1.0

This plugin family is implemented to harvest content objects from any web sources. 

Currently you can use the plugins  in combination of the CrawlerContentProvider and the derived ContentProvider.
A combination of the BaseContentProvider and the derived ContentProvider is also available to scale the crawler by an external job.
You can use a Servelet to start the external Crawler jobs.

You can reuse the html-plugin for different sources by declaration and configuration in the iqser-config.xml.

The example configuration is considering the following example-pages:

- http://www.festo.com/net/de_de/SupportPortal/default.aspx
- http://www.zolltarifnummern.de/
- http://www.wlw.de/start/wlw_dach/DE/de/index.html

You may have to increase the heap space for the crawler in JBaoss or Eclipse.