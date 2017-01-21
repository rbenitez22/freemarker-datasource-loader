/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker.sql;

import com.iamcodepoet.freemarker.TemplateSource;
import java.util.Date;
import java.util.Locale;


public class JdbcTemplateSource implements TemplateSource
{
    private long id;
    private String name;
    private Locale locale;
    private String source;
    private Date dateCreated;
    private Date lastModified;

    public JdbcTemplateSource()
    {
        this.locale = Locale.getDefault();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void setSource(String source)
    {
        this.source = source;
    }

    @Override
    public String getSource()
    {
        return this.source;
    }

    @Override
    public void setLastModified(Date date)
    {
        if(date == null){this.lastModified = null;}
        else
        {
            this.lastModified = new Date(date.getTime());
        }
    }

    @Override
    public Date getLastModified()
    {
        if(lastModified == null){return null;}
        return new Date(lastModified.getTime());
    }

    @Override
    public void setDateCreated(Date date)
    {
        if(date == null)
        {
            this.dateCreated = null;
        }
        else
        {
            this.dateCreated = new Date(date.getTime());
        }
            
    }

    @Override
    public Date getDateCreated()
    {
        if(this.dateCreated == null){return null;}
        return new Date(dateCreated.getTime());
    }

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}
