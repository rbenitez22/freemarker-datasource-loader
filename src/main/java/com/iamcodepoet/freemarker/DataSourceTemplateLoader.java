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
import freemarker.cache.TemplateLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

/**
 *
 * @author Roberto C. Benitez
 */
public class DataSourceTemplateLoader implements TemplateLoader
{
    public final static String LOCALIZED_NAME_REGEX="_[a-z]{2}(_([a-zA-Z]{2}){1,2})?_[A-Z]{2}$";
    
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
    
    @Override public TemplateSource findTemplateSource(String localizedTemplateName) throws IOException
    {
        if(localizedTemplateName == null || localizedTemplateName.isEmpty())
        {
            throw new IOException("Cannot find template with a NULL or EMPTY name");
        }
        
        Object[] parts =splitLocalizedName(localizedTemplateName);
        String name=(String)parts[0];
        Locale locale=(Locale)parts[1];
        
        TemplateSource source;
        
        try (TemplateSourceDao dao= new TemplateSourceDao(dataSource.getConnection(),metadata))
        {
            source = dao.queryByName(name,locale);
        }
        catch (Exception e) 
        {
            Throwable root=(e.getCause() == null)?e:e.getCause();
            throw new IOException(root.getMessage(), root);
        }
        
        return source;
    }

    private Object[] splitLocalizedName(String localizedTemplateName)
    {
        Object[] tokens= new Object[2];
        
        //defaults
        tokens[0] = localizedTemplateName;
        tokens[1] = Locale.getDefault();
        
        if(isLocalizedName(localizedTemplateName))
        {
            
            Pattern pattern= Pattern.compile(LOCALIZED_NAME_REGEX);
            Matcher matcher = pattern.matcher(localizedTemplateName);
            if(matcher.find())
            {
                String name=localizedTemplateName.substring(0, matcher.start());
                String languageString=matcher.group(0);
                String languageTag = languageString.substring(1).replace("_", "-");
                Locale locale=Locale.forLanguageTag(languageTag);
                tokens[0]= name;
                tokens[1]= locale;
            }
        }
        
        return tokens;
    }
    
    private boolean isLocalizedName(String name)
    {
        if(name == null || name.isEmpty()){return false;}
        return name.matches(".*"+LOCALIZED_NAME_REGEX);
    }
    

    @Override
    public long getLastModified(Object object)
    {
        assertValidTemplateSourceParameter(object);
        
        Date date= ((TemplateSource)object).getLastModified();
        return (date == null)? -1 : date.getTime();
            
    }

    private void assertValidTemplateSourceParameter(Object object) throws IllegalArgumentException, NullPointerException
    {
        if(object == null)
        {
            throw new NullPointerException("Template parameter is null");
        }
        
        if(object instanceof TemplateSource == false)
        {
            String string=String.format("IllegalArgument. Expected '%s', Got '%s'",TemplateSource.class,object);
            throw new IllegalArgumentException(string);
        }
    }

    @Override
    public Reader getReader(Object source, String encoding) throws IOException
    {
        assertValidTemplateSourceParameter(source);
        
        return new StringReader(((TemplateSource)source).getSource());
    }

    @Override
    public void closeTemplateSource(Object object) throws IOException
    {
       //do nothing
    }

}
