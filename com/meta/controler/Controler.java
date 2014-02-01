package com.meta.controler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import com.meta.controler.P2P.P2PControler;
import com.meta.modele.Model;
import com.meta.plugin.PluginTCPControler;
import com.meta.plugin.PluginWebServiceControler;

import djondb.LibraryException;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
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
/**
 * 
 * @author Thomas LAVOCAT
 * 
 */
public class Controler {
	
	private Properties 		properties 				= 	null;
	private Model 			model 					=	null;
	private String 			pluginsropertiesFile	=	null;//TODO
	private P2PControler 	p2pControler 			= 	null;
	private ArrayList<String>							lstPluginsNames			=	null;
	private HashMap<String, String>						configuration			=	null;
	private HashMap<String, PluginTCPControler> 		mapTCPControler			=	null;
	private HashMap<String, PluginWebServiceControler>	mapWebServiceControler	=	null;
	
	/**
	 * 
	 * @throws LibraryException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	 public Controler() 
		 throws 	LibraryException, 
		 			IOException, 
		 			URISyntaxException
	{
		 //TODO initialize configuration
		 this.model = new Model();
		 this.properties = new Properties();
		 FileInputStream input = new FileInputStream(
				 					new File(pluginsropertiesFile));
		 this.p2pControler = new P2PControler(Integer.parseInt(configuration.get("port")));
		 properties.load(input);
		 pluginInitialisation();
	 }
	 
	 /**
	  * TODO
	  */
	private void pluginInitialisation() {
		lstPluginsNames			= new ArrayList<String>();
		mapTCPControler 		= new HashMap<String, PluginTCPControler>();
		mapWebServiceControler	= new HashMap<String, PluginWebServiceControler>();
		
		Enumeration<Object> keys = properties.keys();
		while(keys.hasMoreElements()){
			String key = ((String) keys.nextElement());
			//TODO split sur les points et prendre le dernier élément
			if(key.contains(".name")){
				//plugin founded
				lstPluginsNames.add(properties.getProperty(key));
				//load TCP class
				String strTCPClass = properties.getProperty(key+".TCPClass");
				//load web service class
				String strWSClass  = properties.getProperty(key+"WSClass");
				
				try {
					Class clazzTCP 	= Class.forName(strTCPClass);
					Class clazzWS	= Class.forName(strWSClass);
					
					//load TCPControler
					PluginTCPControler tcpControler = 
							(PluginTCPControler)   clazzTCP.newInstance();
					//load webServiceControler
					PluginWebServiceControler webServiceControler = 
							(PluginWebServiceControler) clazzWS.newInstance();
					
					//Set parameters
					tcpControler.setP2pControler(p2pControler);
					tcpControler.setModel(model);
					//Set parameters
					webServiceControler.setModel(model);
					webServiceControler.setTcpControler(tcpControler);
					//init TCP and WS parts
					tcpControler.init();
					webServiceControler.init();
					
				} catch (ClassNotFoundException e) {
					System.out.println("The plugin "+key+" is not available");
					mapTCPControler.remove(key);
					mapWebServiceControler.remove(key);
					lstPluginsNames.remove(key);
				} catch (InstantiationException e) {
					System.out.println(
							"Error during instanciation of th plugin : "+key);
					mapTCPControler.remove(key);
					mapWebServiceControler.remove(key);
					lstPluginsNames.remove(key);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @return the model
	 */
	public Model getModel() {
		return model;
	}
}
