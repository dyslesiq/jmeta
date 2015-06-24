/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 JMeta
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.configuration;

import java.util.Properties;

/**
 * The web service configuration object
 */
public final class WSConfiguration extends BaseConfiguration {

    /**
     * The default web service port.
     */
    public static final Short DEFAULT_WS_PORT = 8080;

    /**
     * The key in configuration file for the web service port.
     */
    public static final String WS_PORT_KEY = "wsPort";

    /**
     * The port the web service stack will listen to.
     */
    private Short wsPort = DEFAULT_WS_PORT;

    /**
     * Empty initialization with default values
     */
    public WSConfiguration() {
    }

    /**
     * Initializes the web service config from properties.
     *
     * @param properties The properties to take configuration from.
     */
    public WSConfiguration(Properties properties) {
        super(properties);
        if (properties != null) {
            initFromProperties();
        }
    }

    @Override
    void initFromProperties() {
        Short port = this.getShort(WS_PORT_KEY);
        if (port != null) {
            this.wsPort = port;
        }
    }

    /**
     * 
     * @return the web service port
     */
    public Short getWsPort() {
        return wsPort;
    }

    /**
     * 
     * @param wsPort the new web service port
     */
    public void setWsPort(Short wsPort) {
        this.wsPort = wsPort;
    }
    
}