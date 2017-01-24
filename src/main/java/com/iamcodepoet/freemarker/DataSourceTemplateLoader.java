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
import com.iamcodepoet.freemarker.sql.TemplateSourceDao;
import com.iamcodepoet.freemarker.util.Experimental;
import freemarker.cache.TemplateLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import javax.sql.DataSource;

/**
 *A {@link  TemplateLoader} implementation that loads data from a database table, via a {@link  DataSource}
 * The database work is performed by the helper (DAO) class {@link TemplateSourceDao}.
 * 
 * @author Roberto C. Benitez
 */
public class DataSourceTemplateLoader implements TemplateLoader
{
    private final JdbcMetaData metadata;
    
    private final DataSource dataSource;

    public DataSourceTemplateLoader(DataSource dataSource,JdbcMetaData metada)
    {
        this.dataSource = dataSource;
        this.metadata=metada;
    }

    public DataSourceTemplateLoader(DataSource dataSource,String templateTableName)
    {
        this.dataSource=  dataSource;
        metadata = new JdbcMetaData(templateTableName);
        
    }
    
    @Experimental(description = "This implementation is somewhat of a hack. "
                                + "queryByName actually returns a TemplateSource (which extends TemplateName)--consider return just a name instead?"
                                 + "HOWEVER? per TemplateLoader javadoc, perhaps should just return TemplateSource..???")
    @Override public TemplateName findTemplateSource(String localizedTemplateName) throws IOException
    {
        if(localizedTemplateName == null || localizedTemplateName.isEmpty())
        {
            throw new IOException("Cannot find template with a NULL or EMPTY name");
        }
        
        TemplateName name;
        
        try (TemplateSourceDao dao= new TemplateSourceDao(dataSource.getConnection(),metadata))
        {
            name = dao.getTemplateNameForLocalizedName(localizedTemplateName);
        }
        catch (Exception e) 
        {
            Throwable root=(e.getCause() == null)?e:e.getCause();
            throw new IOException(root.getMessage(), root);
        }
        
        return name;
    }

    @Override public long getLastModified(Object object)
    {
        assertValidTemplateNameParameter(object);
        if(object instanceof TemplateSource)
        {
            
            Date date= ((TemplateSource)object).getLastModified();
            return (date == null)? -1 : date.getTime();
        }
        else
        {
            try
            {
                TemplateSource source = loadSourceFromDatabse(object);
                return source.getLastModified().getTime();
            }
            catch(IOException e)
            {
                return -1; //throw exception instead?
            }
        }
            
    }

    private TemplateSource loadSourceFromDatabse(Object object) throws IOException
    {
        TemplateSource source;
        try (TemplateSourceDao dao = new TemplateSourceDao(dataSource.getConnection(), metadata))
        {
            source= dao.queryByName((TemplateName)object);
        }
        catch (Exception e)
        {
            Throwable cause = (e.getCause() == null)?e : e.getCause();
            throw new IOException(cause.getMessage(), cause);
        }
        return source;
    }

    private void assertValidTemplateNameParameter(Object object) throws IllegalArgumentException, NullPointerException
    {
        if(object == null)
        {
            throw new NullPointerException("Template Name parameter is null");
        }
        
        if(object instanceof TemplateName == false)
        {
            String string=String.format("IllegalArgument. Expected '%s', Got '%s'",TemplateName.class,object);
            throw new IllegalArgumentException(string);
        }
    }

    @Override
    public Reader getReader(Object object, String encoding) throws IOException
    {
        assertValidTemplateNameParameter(object);
        if(object instanceof TemplateSource)
        {
            return new StringReader(((TemplateSource)object).getSource());
        }
        else
        {
            TemplateSource source = loadSourceFromDatabse(object);
            
            return new StringReader(source.getSource());
            
        }
    }

    @Override
    public void closeTemplateSource(Object object) throws IOException
    {
       //do nothing
    }

}
