/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker;

import com.iamcodepoet.freemarker.sql.JdbcTemplateSource;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Roberto C. Benitez
 */
public class SqLiteDao implements  AutoCloseable
{
    public final static String URL_PREFIX="jdbc:sqlite://";
    public final static String DB_NAME="template-loader.db";
    public final static String TABLE_NAME="templates";
    
    private final Connection connection;
    private final String identQuoteString;

    public SqLiteDao(String path) throws SQLException
    {
        this.connection = createConnection(path);
        identQuoteString = connection.getMetaData().getIdentifierQuoteString();
        createTableIfNotExists(connection);
    }
    
    private static Connection createConnection(String path) throws SQLException
    {
        String url=getJdbcUrl(path);
        return DriverManager.getConnection(url);
    }

    public static String getJdbcUrl(String path)
    {
        String url=Paths.get(URL_PREFIX,path,DB_NAME).toString();
        return url;
    }
        
    
    private void createTableIfNotExists(Connection conn) throws SQLException
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

    @Override
    public void close() throws Exception
    {
        if(!(connection == null || connection.isClosed()))
        {
            connection.close();
        }
    }
    
    public void update(JdbcTemplateSource source) throws SQLException
    {
        String table= quote(TABLE_NAME);
        String sql=String.format("UPDATE TABLE %s SET templateName = ?,templateSource = ?,lastModified = ?) WHERE id=?", table);
        try(PreparedStatement stmt = connection.prepareStatement(sql))
        {
            java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
            
            stmt.setString(1, source.getName());
            stmt.setString(2, source.getSource());
            stmt.setDate(3, now);
            stmt.setLong(4, source.getId());
            stmt.executeUpdate();
        }
    }
     
    public void insert(JdbcTemplateSource source) throws SQLException
    {
        String table= quote(TABLE_NAME);
        String sql=String.format("INSERT INTO %s(id,templateName,templateSource,dateCreated,lastModified) VALUES(?,?,?,?,?)", table);
        try(PreparedStatement stmt = connection.prepareStatement(sql))
        {
            java.sql.Date now = new java.sql.Date(System.currentTimeMillis());
            
            stmt.setLong(1, source.getId());
            stmt.setString(2, source.getName());
            stmt.setString(3, source.getSource());
            stmt.setDate(4, now);
            stmt.setDate(5, now);
            stmt.executeUpdate();
        }
            
    }
    
    public long getCount()
    {
        long count;
        try(Statement stmt = connection.createStatement();
                ResultSet rst=stmt.executeQuery("SELECT COUNT(*) FROM " + quote(TABLE_NAME) ))
        {
            if(rst.next())
            {
                count=rst.getLong(1);
            }
            else
            {
                count=-1;
            }
        }
        catch (Exception e) 
        {
            count = -1;
        }
        
        return count;
    }
    
    private String quote(String string)
    {
        return identQuoteString + string + identQuoteString;
    }
    
    
    private String getQuoteString() throws SQLException
    {
        String qs=connection.getMetaData().getIdentifierQuoteString();
        return qs;
    }
        
    
}
