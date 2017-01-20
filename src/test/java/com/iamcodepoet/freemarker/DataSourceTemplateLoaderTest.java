/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.BeforeClass;
import org.sqlite.SQLiteDataSource;

/**
 *
 * @author Roberto C. Benitez
 */
public class DataSourceTemplateLoaderTest
{
    public final static String URL_PREFIX="jdbc:sqlite://";
    public final static String DB_NAME="template-loader.db";
    public final static String TABLE_NAME="templates";
    
    private static String jdbcUrl;
    
    public DataSourceTemplateLoaderTest()
    {
    }
    
    @BeforeClass public static final void init()
    {
        File file= new File("");
        try
        {
            jdbcUrl = Paths.get(URL_PREFIX,file.getAbsolutePath(), DB_NAME).toString();
            
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
     
    @Test public void testLoader() throws MalformedTemplateNameException, ParseException, IOException, TemplateException, SQLException
    {
        
            
            SQLiteDataSource ds= new SQLiteDataSource();
            ds.setUrl(jdbcUrl);
            DataSourceTemplateLoader loader= new DataSourceTemplateLoader(ds,TABLE_NAME);

            
            Version version = new Version(2, 3, 23);
            Configuration config= new Configuration(version);
            config.setTemplateLoader(loader);
            
            Template template1=config.getTemplate("Profound Message");
            
            Map<String,String> model= new HashMap<>();
            model.put("noun1", "Pinguins");
            model.put("verb", "waddle");
            model.put("noun2", "aid");
            model.put("noun3", "Linux distro");
            
            StringWriter writer1= new  StringWriter();
            
            template1.process(model, writer1);
            
            System.out.println("Message test: " + writer1.toString());
            
            Template template2 = config.getTemplate("Cover Letter Template");
            model = new HashMap<>();
            model.put("hiringManager", "Miss Ada Byron");
            
            System.out.printf("--%s\n",template2.getName());
            StringWriter writer2 = new StringWriter();
            template2.process(model, writer2);
            System.out.printf("Cover Letter: %s\n\t",writer2.toString());
      
    }

   
}
