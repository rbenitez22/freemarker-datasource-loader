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

import com.iamcodepoet.freemarker.TemplateName;
import com.iamcodepoet.freemarker.TemplateSource;
import com.iamcodepoet.freemarker.util.Experimental;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *A brief implementation of a Data Access Object (DAO) for template sources.
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
    
    /**
     * This method performs a name search using the {@link #queryByName(com.iamcodepoet.freemarker.TemplateName)} method.
     * Essentially, this methods serves to confirm that named template exists in the database.
     * @param localizedName
     * @return 
     * @throws java.sql.SQLException throw by driver should any error occur, such as invalid table/column names.
     * @throws java.io.IOException thrown if the named template does not exist
     */
    @Experimental
    public TemplateName getTemplateNameForLocalizedName(String localizedName) throws SQLException, IOException
    {
        TemplateName searchName=TemplateName.fromLocalizedName(localizedName);
        
        return queryByName(searchName).getTemplateName();
    }
    
    /**
     * Query a template by Name (and {@link Locale}).  Whilst Freemarker passes a 
     * localized template name, it (may) be best to store/handle the template name and locale information separately.
     * As such, this method takes a name and {@link Locale} to perform a query.  The expectation is that a database record 
     * may have many records with the same template name (but different locales).  Furthermore, it is expected (nay, required), that the {@link Locale} information
     * be stored as a <a href="https://tools.ietf.org/html/bcp47">well-formed IETF BCP 47 language tag</a>--this is provided by Java via {@link  Locale#toLanguageTag()}
     * @param name
     * @return {@link TemplateSource}
     * @throws SQLException throw by driver should any error occur, such as invalid table/column names.
     * @throws IOException throw if named template is not found.
     */
    public JdbcTemplateSource queryByName(TemplateName name) throws SQLException, IOException
    {
        JdbcTemplateSource source;
        
        String languageTag = name.getLocale().toLanguageTag();
        String nameColumn = quoteIdentifierName(metadata.getNameColumn());
        String localeColumn = quoteIdentifierName(metadata.getLocaleColumn());
        String where = String.format(" WHERE %s = ? AND %s= ?", nameColumn, localeColumn);
        String sql = getSelectSql() + " " + where;
        
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) 
        {
            stmt.setString(1, name.getName());
            stmt.setString(2, languageTag);
            try (final ResultSet rst = stmt.executeQuery()) 
            {
                if (rst.next()) {
                    source = createSourceFromResultSet(rst);
                }
                else 
                {
                    throw new IOException("Template not found. " + name.getLocalizedName());
                }
            }
        }
        return source;
    }

    /**
     * Quote an identifier name with the appropriate Identifier Quote String, as
     * specified by the database.
     * @param identifierName
     * @return quoted String
     * @throws SQLException 
     */
    private String quoteIdentifierName(String identifierName) throws SQLException
    {
        if(identQuoteString == null)
        {
            identQuoteString = connection.getMetaData().getIdentifierQuoteString();
        }
        
        return identQuoteString + identifierName + identQuoteString;
    }
    
    /**
     * Get a properly quoted table name--that includes the schema if supplied (e.g. "public"."templates")
     * @return table name
     * @throws SQLException 
     */
    public String getFullTableName() throws SQLException
    {
        String name;
        if (metadata.getSchemaName() == null || metadata.getSchemaName().isEmpty()) 
        {
            name = quoteIdentifierName(metadata.getTableName());
        }
        else 
        {
            name = quoteIdentifierName(metadata.getSchemaName()) + "." + quoteIdentifierName(metadata.getTableName());
        }
        
        return name;
    }

    /**
     * This method should help generate a primary value/sequence if the user has not provided one.
     * A Sequence generator may be best--with implementations for common database sequence generators.
     * @return row count in the templates table
     * @throws SQLException 
     */
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

    /**
     * Query all template sources from the database.
     * @return List of template sources
     * @throws SQLException 
     */
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

    /**
     * Create a new {@link JdbcTemplateSource} from the current {@link ResultSet} row.
     * @param rst
     * @return {@link TemplateSource}
     * @throws SQLException 
     */
    private JdbcTemplateSource createSourceFromResultSet(final ResultSet rst) throws SQLException
    {
        JdbcTemplateSource source = new JdbcTemplateSource();
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

    /**
     * Delete a template source matching this name and locale.
     * @param templateName
     * @param locale
     * @return count of rows deleted
     * @throws SQLException 
     */
    public int delete(String templateName, Locale locale) throws SQLException
    {
        int count;
        String nameCol = quoteIdentifierName(metadata.getNameColumn());
        String localeCol = quoteIdentifierName(metadata.getLocaleColumn());
        String table = getFullTableName();
        
        String sql = String.format("DELETE FROM %s WHERE %s = ? AND %s= ?", table, nameCol, localeCol);
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, templateName);
            stmt.setString(2, locale.toLanguageTag());
            count = stmt.executeUpdate();
        }
        
        return count;
    }
    
    public int delete(JdbcTemplateSource source) throws SQLException
    {
        String idCol=quoteIdentifierName(metadata.getIdColumn());
        String table = getFullTableName();
        
        String sql = String.format("DELETE FROM %s WHERE %s = ? ", table, idCol);
        int count;
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, source.getId());
            
            count = stmt.executeUpdate();
        }
        
        return count;
    }

    /**
     * Insert a new template source record.  
     * @param source
     * @throws SQLException if the name or locale fields are null; a template
     * this name and locale are present in the database; or {@link SQLException} throw by the driver,
     * such as incorrectly specified values in the {@link JdbcMetaData} supplied to the DAO instance.
     */
    public void insert(JdbcTemplateSource source) throws SQLException
    {
        assertCanInsert(source);
            
        String table = getFullTableName();

        String sql=createInsertStatement(table,metadata.getIdColumn(),metadata.getNameColumn()
                                        ,metadata.getLocaleColumn(),metadata.getSourceColumn()
                                        ,metadata.getDateCreatedColumn(),metadata.getLastModifiedColumn());
        
        try (final PreparedStatement stmt = connection.prepareStatement(sql)) 
        {
            Timestamp now= new Timestamp(System.currentTimeMillis());
            if (source.getId() < 1) 
            {
                long id = getCount() + 1;
                if (id < 1) 
                {
                    throw new SQLException("Missing Template Source ID, and failed to generate one");
                }
                source.setId(id);
            }
            
            source.setDateCreated(now);
            source.setLastModified(now);
            
            stmt.setLong(1, source.getId());
            stmt.setString(2, source.getName());
            String languageTag = source.getLocale().toLanguageTag();
            stmt.setString(3, languageTag);
            stmt.setString(4, source.getSource());
            stmt.setTimestamp(5, now);
            stmt.setTimestamp(6, now);
            stmt.executeUpdate();
        }
    }
    
    private String createInsertStatement(String tableName,String ... columns) throws SQLException
    {
        StringBuilder names = new StringBuilder();
        StringBuilder vars= new StringBuilder();
        
        for(String column : columns)
        {
            String quoted=quoteIdentifierName(column);
            
            if(names.length() > 0)
            {
                names.append(",");
                vars.append(",");
            }
            names.append(quoted);
            vars.append("?");
        }
            
        return String.format("INSERT INTO %s (%s) VALUES(%s)", tableName,names.toString(),vars.toString());
        
    }

    private void assertCanInsert(JdbcTemplateSource source) throws SQLException
    {
        if (source.getName() == null || source.getName().isEmpty())
        {
            throw new SQLException("Template Source Name is missing. Cannot insert record");
        }
        
        if(source.getLocale() == null)
        {
            throw new SQLException("Locale field is null");
        }
        
        if (exists(source.getTemplateName()))
        {
            String msg = String.format("Template '%s (%s)' already exists", source.getName(), source.getLocale().toLanguageTag());
            throw new SQLException(msg);
        }
    }


    /**
     * Update this template source.
     * @param source
     * @throws SQLException 
     */
    public void update(JdbcTemplateSource source) throws SQLException
    {
        String table = getFullTableName();
        String idColumn = quoteIdentifierName(metadata.getIdColumn());
        String nameColmn = quoteIdentifierName(metadata.getNameColumn());
        String localeColumn = quoteIdentifierName(metadata.getLocaleColumn());
        String sourceColumn = quoteIdentifierName(metadata.getSourceColumn());
        String modColumn = quoteIdentifierName(metadata.getLastModifiedColumn());
        
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

    /**
     * Check whether a template source record with this name and locale already exists.
     * @param templateName
     * @param locale
     * @return
     * @throws SQLException 
     */
    private boolean exists(TemplateName name) throws SQLException
    {
        try 
        {
            TemplateSource src = queryByName(name);
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
