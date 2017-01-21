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
