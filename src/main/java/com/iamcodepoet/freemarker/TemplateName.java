/*
 * Copyright 2017 Roberto C. Benitez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iamcodepoet.freemarker;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TemplateName 
{
    public final static String LOCALIZED_NAME_REGEX="_[a-z]{2}(_([a-zA-Z]{2}){1,2})?_[A-Z]{2}$";
    
    private final String name;
    private final Locale locale;
    
    public TemplateName(String name, Locale locale)
    {
        this.name = name;
        this.locale = locale;
    }

    public String getName()
    {
        return name;
    }

    public String getLocalizedName()
    {
        return String.format("%s_%s_%s",name,locale.getLanguage(),locale.getCountry());
    }

    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.locale);
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
        final TemplateName other = (TemplateName) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.locale, other.locale)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return getLocalizedName();
    }
    
    
    
    public static TemplateName from(String name, Locale locale)
    {
        return new TemplateName(name, locale);
    }
    
    public static TemplateName fromLocalizedName(String localizedName)
    {
        
        if(localizedName == null)
        {
            throw new NullPointerException("name parameter is null");
        }
        
        if(localizedName.isEmpty()){throw new IllegalArgumentException("Invalid name parameter");}
        
        String name = localizedName;
        Locale locale = Locale.getDefault();
        
        boolean localized=localizedName.matches(".*" + LOCALIZED_NAME_REGEX);
        
        
        if(localized)
        {
            Pattern pattern= Pattern.compile(LOCALIZED_NAME_REGEX);
            Matcher matcher = pattern.matcher(localizedName);
            if(matcher.find())
            {
                name=localizedName.substring(0, matcher.start());
                String languageString=matcher.group(0);
                String languageTag = languageString.substring(1).replace("_", "-");
                locale=Locale.forLanguageTag(languageTag);
            }
        }
        
        return new TemplateName(name, locale);
    }
    
}
