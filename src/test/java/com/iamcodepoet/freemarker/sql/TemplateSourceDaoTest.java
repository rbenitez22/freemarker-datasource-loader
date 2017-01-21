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
package com.iamcodepoet.freemarker.sql;

import com.iamcodepoet.freemarker.ConfigProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.junit.Test;
import org.junit.BeforeClass;

/**
 *
 * @author Roberto C. Benitez
 */
public class TemplateSourceDaoTest
{
    public final static String URL_PREFIX="jdbc:sqlite://";
    public final static String DB_NAME="template-loader.db";
    public final static String TABLE_NAME="templates";
    
    private static String DbUrl;
    private static JdbcMetaData metadata;
    
    private static ConfigProvider config;
    
    public TemplateSourceDaoTest()
    {
    }
    
    @BeforeClass public static void init()
    {
        config = new ConfigProvider();
        
        DbUrl = Paths.get(URL_PREFIX,new File("").getAbsolutePath(), DB_NAME).toString();
        metadata = new JdbcMetaData(TABLE_NAME);
        
        try(Connection conn = getSqLiteConnection())
        {
            createTableIfNotExists(conn);
        }
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
    }
    
     private static void createTableIfNotExists(Connection conn) throws SQLException
    {
        String sql="CREATE TABLE IF NOT EXISTS templates\n" +
                    "(\n" +
                    "	id INTEGER PRIMARY KEY,\n" +
                    "	templateName TEXT,\n" +
                    "	templateSource TEXT,\n" +
                    "	dateCreated INTEGER,\n" +
                    "	lastModified INTEGER,\n" +
                    "	locale TEXT\n" +
                    ")";
        
        try(Statement stmt=conn.createStatement())
        {
            stmt.execute(sql);
        }
    }
     
    @Test
    public void testInsert() throws Exception
    {
        try (TemplateSourceDao dao= new TemplateSourceDao(getPgSqlConnection(), metadata))
        {
            JdbcTemplateSource templateSource = new JdbcTemplateSource();
            
            templateSource.setName("Employment Termination Letter Template");
            templateSource.setSource("All those coming in on Friday, please reply--not so fast ${terminatedEmployee}");
            
            dao.insert(templateSource);
            
        }
        
    }
    
    @Test
    public void testQuery() throws Exception
    {
        try (TemplateSourceDao dao= new TemplateSourceDao(getPgSqlConnection(), metadata))
        {
            
            dao.query()
                    .forEach(e->System.out.printf("[%s] -> %s\n",e.getName(),e.getSource()));
        }
        
    }
    
    
    private static Connection getPgSqlConnection() throws SQLException
    {
        String urlPrefix="jdbc:postgresql";
        String configName="postgresql-sandbox.properties";
        
        return config.getDatabaseConnection(configName, urlPrefix);
    }

    private static Connection getSqLiteConnection() throws SQLException
    {
        return DriverManager.getConnection(DbUrl);
    }
    
}
