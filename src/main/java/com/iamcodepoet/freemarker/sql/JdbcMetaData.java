/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker.sql;

/**
 *
 * @author Roberto C. Benitez
 */
public class JdbcMetaData
{
    private String schemaName;
    private String tableName;
    private String idColumn;
    private String nameColumn;
    private String localeColumn;
    private String sourceColumn;
    private String dateCreatedColumn;
    private String lastModifiedColumn;

    public JdbcMetaData(String tableName)
    {
        this("",tableName);
       
    }

    public JdbcMetaData(String schemaName, String tableName)
    {
        this.schemaName = schemaName;
        this.tableName = tableName;
         this.idColumn="id";
        this.nameColumn="templateName";
        this.sourceColumn="templateSource";
        this.dateCreatedColumn="dateCreated";
        this.lastModifiedColumn="lastModified";
        this.localeColumn="locale";
        
    }

    public String getTableName()
    {
        return tableName;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public String getIdColumn()
    {
        return idColumn;
    }

    public void setIdColumn(String idColumn)
    {
        this.idColumn = idColumn;
    }

    public String getNameColumn()
    {
        return nameColumn;
    }

    public void setNameColumn(String nameColumn)
    {
        this.nameColumn = nameColumn;
    }

    public String getSourceColumn()
    {
        return sourceColumn;
    }

    public void setSourceColumn(String sourceColumn)
    {
        this.sourceColumn = sourceColumn;
    }

    public String getDateCreatedColumn()
    {
        return dateCreatedColumn;
    }

    public void setDateCreatedColumn(String dateCreatedColumn)
    {
        this.dateCreatedColumn = dateCreatedColumn;
    }

    public String getLastModifiedColumn()
    {
        return lastModifiedColumn;
    }

    public void setLastModifiedColumn(String lastModifiedColumn)
    {
        this.lastModifiedColumn = lastModifiedColumn;
    }

    public String getSchemaName()
    {
        return schemaName;
    }

    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public String getLocaleColumn()
    {
        return localeColumn;
    }

    public void setLocaleColumn(String localeColumn)
    {
        this.localeColumn = localeColumn;
    }

}
