/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker.sql;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
    
    public TemplateSourceDaoTest()
    {
    }
    
    @BeforeClass public static void init()
    {
        DbUrl = Paths.get(URL_PREFIX,new File("").getAbsolutePath(), DB_NAME).toString();
        metadata = new JdbcMetaData(TABLE_NAME);
        
        try(Connection conn = getConnection())
        {
            createTableIfNotExists(conn);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
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
        try (TemplateSourceDao dao= new TemplateSourceDao(getConnection(), metadata))
        {
            JdbcTemplateSource templateSource = new JdbcTemplateSource();
            
            templateSource.setName("Cover Letter Template");
            templateSource.setSource("Dear ${hiringManager},\n\t You hire me.  You hire me now!");
            
            dao.insert(templateSource);
            
        }
        
    }
    
    @Test
    public void testQuery() throws Exception
    {
        try (TemplateSourceDao dao= new TemplateSourceDao(getConnection(), metadata))
        {
            
            dao.query()
                    .forEach(e->System.out.printf("[%s] -> %s\n",e.getName(),e.getSource()));
        }
        
    }
    
    private static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(DbUrl);
    }
    
}
