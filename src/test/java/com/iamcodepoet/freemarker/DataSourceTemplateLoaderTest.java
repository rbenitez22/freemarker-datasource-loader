/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker;

import com.iamcodepoet.freemarker.sql.JdbcTemplateSource;
import freemarker.cache.TemplateLoader;
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
    
    @AfterClass public final static void afterclass()
    {
        
    }
    
    //@Test
    public void testLocale()
    {
        String name="Test 1";
        String regex="_[a-z]{2}(_([a-zA-Z]{2}){1,2})?_[A-Z]{2}$";
        for(Locale locale : Locale.getAvailableLocales())
        {
            if(locale.getLanguage().isEmpty() || locale.getCountry().isEmpty()){continue;}
            String localeString="_" + locale.getLanguage() + "_" + locale.getCountry();
            String localizedName=name + localeString;
            boolean matches=localizedName.matches(".*"+regex);
            System.out.printf("Locale: %s, String: %s; Ends With Locale string: %s\n",locale,localizedName,matches);
            
            
            Pattern pattern= Pattern.compile(regex);
            Matcher matcher = pattern.matcher(localizedName);
            if(matcher.find())
            {
                String matchedLocaleString=matcher.group();
                String deLocalizedName=localizedName.substring(0,matcher.start());
                Locale locale2=Locale.forLanguageTag(matchedLocaleString.substring(1).replace("_", "-"));
                System.out.printf("Matched Locale String: %s, De-Localized Name: %s, Build Locale: Lang(%s), Country(%s)\n",matchedLocaleString,deLocalizedName,locale2.getLanguage(),locale2.getCountry());
                
            }
             
            
          //  assert matches == true;
            
            
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
            
            Template template=config.getTemplate("Profound Message");
            
            Map<String,String> model= new HashMap<>();
            model.put("noun1", "Pinguins");
            model.put("verb", "waddle");
            model.put("noun2", "aid");
            model.put("noun3", "Linux distro");
            
            StringWriter writer= new  StringWriter();
            
            template.process(model, writer);
            
            System.out.println("Msg: " + writer.toString());
            assert true;
      
    }

   
}
