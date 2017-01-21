/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker.sql;

import com.iamcodepoet.freemarker.TemplateSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Roberto C. Benitez
 */
public class TemplateSourceDao implements AutoCloseable
{
    private final java.sql.Connection connection;
    private final JdbcMetaData metadata;
    private String identQuoteString;

    public TemplateSourceDao(Connection connection, JdbcMetaData metadata)
    {
        this.connection = connection;
        this.metadata = metadata;
    }
    
    private String getLanguageTag(Locale locale)
    {
        return String.format("%s-%s", locale.getLanguage(), locale.getCountry());
    }

    public TemplateSource queryByName(String localizedTemplateName, Locale locale) throws SQLException, IOException
    {
        TemplateSource source;
        
        String languageTag = getLanguageTag(locale);
        String nameColumn = quote(metadata.getNameColumn());
        String localeColumn = quote(metadata.getLocaleColumn());
        String where = String.format(" WHERE %s = ? AND %s= ?", nameColumn, localeColumn);
        String sql = getSelectSql() + " " + where;
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) 
        {
            stmt.setString(1, localizedTemplateName);
            stmt.setString(2, languageTag);
            try (final ResultSet rst = stmt.executeQuery()) 
            {
                if (rst.next()) {
                    source = createSourceFromResultSet(rst);
                }
                else 
                {
                    throw new IOException("Template not found. " + localizedTemplateName);
                }
            }
        }
        return source;
    }

    private String quote(String string) throws SQLException
    {
        if(identQuoteString == null)
        {
            identQuoteString = connection.getMetaData().getIdentifierQuoteString();
        }
        
        return identQuoteString + string + identQuoteString;
    }
    
    public String getFullTableName() throws SQLException
    {
        String name;
        if (metadata.getSchemaName() == null || metadata.getSchemaName().isEmpty()) 
        {
            name = quote(metadata.getTableName());
        }
        else 
        {
            name = quote(metadata.getSchemaName()) + "." + quote(metadata.getTableName());
        }
        
        return name;
    }

    private long getCount() throws SQLException
    {
        long count;
        try (final Statement stmt = connection.createStatement();
                final ResultSet rst = stmt.executeQuery("SELECT COUNT(*) FROM " + getFullTableName())) 
        {
            if (rst.next()) 
            {
                count = rst.getLong(1);
            }
            else 
            {
                count = -1;
            }
        }
        return count;
    }

    private String getSelectSql() throws SQLException
    {
        String table = getFullTableName();
        return String.format("SELECT * FROM %s", table);
    }

    public List<TemplateSource> query() throws SQLException
    {
        List<TemplateSource> sources = new ArrayList<>();
        String sql = getSelectSql();
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) 
        {
            try (final ResultSet rst = stmt.executeQuery()) 
            {
                while (rst.next()) 
                {
                    TemplateSource source = createSourceFromResultSet(rst);
                    sources.add(source);
                }
            }
        }
        return sources;
    }

    private TemplateSource createSourceFromResultSet(final ResultSet rst) throws SQLException
    {
        TemplateSource source = new JdbcTemplateSource();
        source.setName(rst.getString(metadata.getNameColumn()));
        source.setSource(rst.getString(metadata.getSourceColumn()));
        source.setDateCreated(rst.getDate(metadata.getDateCreatedColumn()));
        source.setLastModified(rst.getDate(metadata.getLastModifiedColumn()));
        Locale locale;
        String languageTag = rst.getString(metadata.getLocaleColumn());
        if (languageTag == null || languageTag.isEmpty()) {
            locale = Locale.getDefault();
        }
        else {
            locale = Locale.forLanguageTag(languageTag);
        }
        source.setLocale(locale);
        return source;
    }

    public int delete(String templateName, Locale locale) throws SQLException
    {
        int count;
        String nameCol = quote(metadata.getNameColumn());
        String localeCol = quote(metadata.getLocaleColumn());
        String table = quote(metadata.getTableName());
        
        String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s= ?", table, nameCol, localeCol);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, templateName);
            stmt.setString(2, locale.toLanguageTag());
            count = stmt.executeUpdate();
        }
        
        return count;
    }

    public void insert(JdbcTemplateSource source) throws SQLException
    {
        if (source.getName() == null || source.getName().isEmpty()) 
        {
            throw new SQLException("Template Source Name is missing. Cannot insert record");
        }
        
        if (exists(source.getName(), source.getLocale())) 
        {
            String msg = String.format("Template '%s (%s)' already exists", source.getName(), source.getLocale().toLanguageTag());
            throw new SQLException(msg);
        }
            
        String table = getFullTableName();
        String idColumn = quote(metadata.getIdColumn());
        String nameColmn = quote(metadata.getNameColumn());
        String localeColumn = quote(metadata.getLocaleColumn());
        String sourceColumn = quote(metadata.getSourceColumn());
        String createColumn = quote(metadata.getDateCreatedColumn());
        String modColumn = quote(metadata.getLastModifiedColumn());
        
        String sql = String.format("INSERT INTO %s(%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?)", table, idColumn, nameColmn, localeColumn, sourceColumn, createColumn, modColumn);
        
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) 
        {
            Date now = new Date(System.currentTimeMillis());
            if (source.getId() < 1) 
            {
                long id = getCount() + 1;
                if (id < 1) 
                {
                    throw new SQLException("Missing Template Source ID, and failed to generate one");
                }
            }
            
            stmt.setLong(1, source.getId());
            stmt.setString(2, source.getName());
            String languageTag = source.getLocale().toLanguageTag();
            stmt.setString(3, languageTag);
            stmt.setString(4, source.getSource());
            stmt.setDate(5, now);
            stmt.setDate(6, now);
            stmt.executeUpdate();
        }
    }


    public void update(JdbcTemplateSource source) throws SQLException
    {
        String table = getFullTableName();
        String idColumn = quote(metadata.getIdColumn());
        String nameColmn = quote(metadata.getNameColumn());
        String localeColumn = quote(metadata.getLocaleColumn());
        String sourceColumn = quote(metadata.getSourceColumn());
        String modColumn = quote(metadata.getLastModifiedColumn());
        
        String sql = String.format("UPDATE TABLE %s SET %s = ? %s= ?,%s = ?,%s =? WHERE %s", table, nameColmn, localeColumn, sourceColumn, modColumn, idColumn);
        
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            Date now = new Date(System.currentTimeMillis());
            stmt.setString(1, source.getName());
            String languageTag = source.getLocale().toLanguageTag();
            stmt.setString(2, languageTag);
            stmt.setString(3, source.getSource());
            stmt.setDate(4, now);
            stmt.setLong(5, source.getId());
            stmt.executeUpdate();
        }
    }

    private boolean exists(String templateName, Locale locale) throws SQLException
    {
        try 
        {
            TemplateSource src = queryByName(templateName, locale);
            return src != null;
        }
        catch (IOException e) 
        {
            return false;
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
    
    
}
