/* 
 * Copyright (C) 2017 Roberto C. Benitez
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.iamcodepoet.freemarker;

import com.iamcodepoet.freemarker.sql.JdbcMetaData;
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
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.junit.Test;
import org.junit.BeforeClass;
import org.postgresql.ds.PGSimpleDataSource;
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
    private static ConfigProvider config;
    
    public DataSourceTemplateLoaderTest()
    {
    }
    
    @BeforeClass public static final void init()
    {
        File file= new File("");
        try
        {
            jdbcUrl = Paths.get(URL_PREFIX,file.getAbsolutePath(), DB_NAME).toString();
            config = new ConfigProvider();
            
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Test public void testLoader() throws MalformedTemplateNameException, ParseException, IOException, TemplateException, SQLException
    {
        
            
            javax.sql.DataSource ds= createPostgreSqlDataSource(); //createSqLiteDataSource();
            JdbcMetaData meta  = new JdbcMetaData("public", TABLE_NAME);
            DataSourceTemplateLoader loader= new DataSourceTemplateLoader(ds,meta);

            
            Version version = new Version(2, 3, 23);
            Configuration config= new Configuration(version);
            config.setTemplateLoader(loader);
            
            testTemplate1(config);
            
            testTemplate2(config);
      
    }

    private boolean testTemplate2(Configuration config) throws TemplateException, IOException
    {
        try
        {
            Template template2 = config.getTemplate("Cover Letter Template");
            HashMap<String,Object> model = new HashMap<>();
            model.put("hiringManager", "Miss Ada Byron");

            System.out.printf("--%s\n",template2.getName());
            StringWriter writer2 = new StringWriter();
            template2.process(model, writer2);
            System.out.printf("Cover Letter: %s\n\t",writer2.toString());
            
            return true;
        }
         catch(IOException | TemplateException e)
        {
            Throwable root= (e.getCause() == null)?e : e.getCause();
            System.out.println(root.getMessage());
            return false;
        }
    }

    private boolean testTemplate1(Configuration config) throws TemplateException, IOException
    {
        try
        {
            Template template1=config.getTemplate("Profound Message");

            Map<String,String> model= new HashMap<>();
            model.put("noun1", "Pinguins");
            model.put("verb", "waddle");
            model.put("noun2", "aid");
            model.put("noun3", "Linux distro");

            StringWriter writer1= new  StringWriter();

            template1.process(model, writer1);

            System.out.println("Message test: " + writer1.toString());
            
            return true;
        }
        catch(IOException | TemplateException e)
        {
            Throwable root= (e.getCause() == null)?e : e.getCause();
            System.out.println(root.getMessage());
            return false;
        }
    }

    private SQLiteDataSource createSqLiteDataSource()
    {
        SQLiteDataSource ds= new SQLiteDataSource();
        ds.setUrl(jdbcUrl);
        return ds;
    }
    
    private DataSource createPostgreSqlDataSource() throws IOException
    {
        PGSimpleDataSource ds=  new PGSimpleDataSource();
        
        Properties props = config.getConfig("postgresql-sandbox");
        
        ds.setServerName(props.getProperty("host"));
        ds.setDatabaseName(props.getProperty("databaseName"));
        ds.setUser(props.getProperty("user"));
        ds.setPassword(props.getProperty("password"));
        
        return ds;
    }

   
}
