/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    public Reader getReader(Object object, String encoding) throws IOException
    {
        assertValidTemplateSourceParameter(object);
        
        return new StringReader(((TemplateSource)object).getSource());
    }

    @Override
    public void closeTemplateSource(Object object) throws IOException
    {
       //do nothing
    }

}
