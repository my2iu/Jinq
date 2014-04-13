package ch.epfl.labos.iu.orm.tools;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class EntityGenerator
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      assert(args.length >= 2);
      
      String filename = args[0];
      String outputPath = args[1];
      
      // Get the list of entities
      String entityListString = getXSLTResultString(filename, "shared/entity_list.xslt");
      String [] entityList = entityListString.trim().split("\\s+");
      
      // Get package where entities are to be stuck
      String packageName = getXSLTResultString(filename, "shared/entity_package.xslt");
      packageName = packageName.trim();
      
      // Actually generate entity Java files now
      String variant = "sql/";
      for (String entity : entityList)
      {
         createEntity(entity, packageName, filename, outputPath, variant);
      }
      
      // Generate some other miscellaneous stuff
      createEntityManager(packageName, filename, outputPath, variant);
      createDBManager(packageName, filename, outputPath, variant);
   }
   
   static void createEntityManager(String packageName, String descriptionFile, String outputPath, String variant)
   {
      outputPath = outputPath + "/" + packageName.replace('.', '/');

      HashMap<String, String> params = new HashMap<String, String>();
      params.put("package", packageName);
      
      applyXSLT(outputPath + "/EntityManager.java", descriptionFile, variant + "create_entity_manager.xslt", params);      
   }

   static void createDBManager(String packageName, String descriptionFile, String outputPath, String variant)
   {
      outputPath = outputPath + "/" + packageName.replace('.', '/');

      HashMap<String, String> params = new HashMap<String, String>();
      params.put("package", packageName);
      
      applyXSLT(outputPath + "/DBManager.java", descriptionFile, variant + "create_db_manager.xslt", params);      
   }

   static void createEntity(String entity, String packageName, String descriptionFile, String outputPath, String variant)
   {
      outputPath = outputPath + "/" + packageName.replace('.', '/');

      HashMap<String, String> params = new HashMap<String, String>();
      params.put("entity", entity);
      params.put("package", packageName);
      
      applyXSLT(outputPath + "/" + entity + ".java", descriptionFile, variant + "create_entity.xslt", params);      
   }

   // Applies an XSLT stylesheet to some file and returns the resulting string
   static public String getXSLTResultString(String xmlFile, String xsltFile) {
        StringWriter stringStream = new StringWriter();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        
        try {
           Document document = factory.newDocumentBuilder().parse(new File(xmlFile));
           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           transformerFactory.setURIResolver(new ClassLoaderURIResolver());
           Transformer transformer = transformerFactory
              .newTransformer(new StreamSource(getResource(xsltFile), ClassLoader.getSystemResource(xsltFile).toString()));
           transformer.transform(new DOMSource(document), new StreamResult(stringStream));
        } catch(Exception e) 
        {
           e.printStackTrace();
        }        return stringStream.toString();
    }
   
   static class ClassLoaderURIResolver implements URIResolver
   {
      @Override
      public Source resolve(String href, String base)
            throws TransformerException
      {
         if (href.startsWith("../")) href = href.substring(3);
         return new StreamSource(getResource(href));
      }
   }
   
   static private InputStream getResource(String file)
   {
      return ClassLoader.getSystemResourceAsStream(file);
   }

   // Applies an XSLT stylesheet to some file and write the output to some other file
   static public void applyXSLT(String destFile, String xmlFile, String xsltFile, Map params)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        
        try {
           Document document = factory.newDocumentBuilder().parse(new File(xmlFile));
           TransformerFactory transformerFactory = TransformerFactory.newInstance();
           transformerFactory.setURIResolver(new ClassLoaderURIResolver());
           Transformer transformer = transformerFactory
              .newTransformer(new StreamSource(getResource(xsltFile), ClassLoader.getSystemResource(xsltFile).toString()));

           if (params != null) {
              for (Iterator it = params.entrySet().iterator(); it.hasNext();) {
                  Map.Entry e = (Map.Entry)it.next();
                  transformer.setParameter((String)e.getKey(), (String)e.getValue());
              }
           }
           
           transformer.transform(new DOMSource(document), new StreamResult(new File(destFile)));
        } catch(Exception e) 
        {
           e.printStackTrace();
        }
    }

}
