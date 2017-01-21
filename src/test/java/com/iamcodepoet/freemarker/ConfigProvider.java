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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 *
 * @author Roberto C. Benitez
 */
public  class ConfigProvider
{

    private final String basePath;

    public ConfigProvider()
    {
        String home=System.getProperty("user.home");
        basePath = Paths.get(home, "Programming","sandbox-config").toString();
    }

    public ConfigProvider(String basePath)
    {
        this.basePath = basePath;
    }

    public String getBasePath()
    {
        return basePath;
    }
    
    public Properties getConfig(String name) throws IOException
    {
        if(name == null || name.isEmpty())
        {
            throw new NullPointerException("name parameter is NULL");
        }
        
        String fileName=(name.endsWith(".properties"))?name : name + ".properties";
        File propsFile=Paths.get(basePath, fileName).toFile();
        
        Properties props = new Properties();
        try(InputStream is = new FileInputStream(propsFile))
        {
            props.load(is);
        }
        
        return props;
        
    }
    
    public void saveConfig(Properties props, String name) throws IOException
    {
        String fileName=(name.endsWith(".properties"))?name : name + ".properties";
        File propsFile=Paths.get(basePath, fileName).toFile();
        
        try(OutputStream os= new FileOutputStream(propsFile))
        {
            props.store(os,"");
        }
    }
        
    
    public Connection getDatabaseConnection(String configName, String urlPrefix) throws SQLException
    {
        Properties props;
        try {
            props = getConfig(configName);
        }
        catch (IOException e) {
            throw new SQLException("Unable to create connection.  Unable to load database configuration file", e);
        }
        String host = props.getProperty("host");
        String port = props.getProperty("port");
        String db = props.getProperty("databaseName");
        String user = props.getProperty("user");
        String passord = props.getProperty("password");
        String url = String.format("%s://%s:%s/%s", urlPrefix, host, port, db);
        return DriverManager.getConnection(url, user, passord);
    }
    
}
