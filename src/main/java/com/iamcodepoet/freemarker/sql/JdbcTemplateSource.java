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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


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

    public String getName()
    {
        return this.name;
    }

    public String getLocalizedName()
    {
        if(locale == null){locale = Locale.getDefault();}
        
        return String.format("%s_%s_%s", name,locale.getLanguage(),locale.getCountry());
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

    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
    
    public TemplateName getTemplateName()
    {
        return new TemplateName(name, locale);
    }

    @Override
    public String toString()
    {
        return getLocalizedName();
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.locale);
        hash = 97 * hash + Objects.hashCode(this.source);
        hash = 97 * hash + Objects.hashCode(this.dateCreated);
        hash = 97 * hash + Objects.hashCode(this.lastModified);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JdbcTemplateSource other = (JdbcTemplateSource) obj;
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.locale, other.locale)) {
            return false;
        }
        if (!Objects.equals(this.dateCreated, other.dateCreated)) {
            return false;
        }
        if (!Objects.equals(this.lastModified, other.lastModified)) {
            return false;
        }
        return true;
    }
    
    
}
