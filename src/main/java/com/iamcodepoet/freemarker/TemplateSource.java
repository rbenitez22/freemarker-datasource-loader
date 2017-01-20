/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iamcodepoet.freemarker;

import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Roberto C. Benitez
 */
public interface TemplateSource
{
    void setName(String name);
    String getName();
    
    void setSource(String source);
    String getSource();
    
    void setLastModified(Date date);
    Date getLastModified();
    
    void setDateCreated(Date date);
    Date getDateCreated();
    
    void setLocale(Locale locale);
    Locale getLocale();
}
