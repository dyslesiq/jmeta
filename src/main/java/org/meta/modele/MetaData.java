package org.meta.modele;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import djondb.BSONArrayObj;
import djondb.BSONObj;

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
 * This class correspond to a Metadata. A MetaData is describe by a list
 * of properties. like {name:subtitles, value:vostfr} and a list of results.
 * 
 * This class extends Searchable.
 */
public class MetaData extends Searchable {

	private ArrayList<Data> 			linkedData = null;
	private ArrayList<MetaProperty>	 	properties = null;
	private ArrayList<String> tmpLinkedData;
	
	/**
	 * needed for java Reflection
	 */
	public MetaData(){
		super();
	}
	
	/**
	 * Create a MetaData -> use in case of creation
	 * @param hashCode hash of this MetaData
	 * @param linkedData every data linked to this metaData
	 */
	public MetaData(
			String 					hashCode, 	
			ArrayList<Data> 		linkedData,
			ArrayList<MetaProperty> properties
		) {
		super(hashCode);
		this.setLinkedData(linkedData);
		this.setProperties(properties);
	}

	/**
	 * Create a MetaData -> use in case of data base loading
	 * @param hashCode hash of this MetaData
	 * @param linkedData every data linked to this metaData
	 */
	public MetaData(
			String 					hashCode, 	
			ArrayList<Data> 		linkedData,
			ArrayList<MetaProperty> properties,
			BSONObj 				json
		) {
		super(hashCode, json);
		this.setLinkedData(linkedData);
		this.setProperties(properties);
	}
	
	/**
	 * 
	 * @return every data linked to this metaData
	 */
	public ArrayList<Data> getLinkedData() {
		return linkedData;
	}

	/**
	 * set linked data
	 * @param linkedData
	 */
	public void setLinkedData(ArrayList<Data> linkedData) {
		this.linkedData = linkedData;
	}

	/**
	 * @return the properties
	 */
	public ArrayList<MetaProperty> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(ArrayList<MetaProperty> properties) {
		this.properties = properties;
	}

	public BSONObj toJson() {
		BSONObj json = super.toJson();
		//foreach linked data, get his hash and put it in the json
		BSONArrayObj bsonLinkedData = new BSONArrayObj();
		for (int i=0; i<linkedData.size(); i++) {
			Data data = linkedData.get(i);
			BSONObj bsonData = new BSONObj();
			bsonData.add("data", data.getHashCode());
			bsonLinkedData.add(bsonData);
		}
		json.add("linkedData", bsonLinkedData);
		//foreach properties, get her value and name and put it in the json
		BSONArrayObj bsonProperties = new BSONArrayObj();
		for (int i=0; i< properties.size(); i++) {
			MetaProperty property = properties.get(i);
			BSONObj bsonProperty = new BSONObj();
			bsonProperty.add("name", property.getName());
			bsonProperty.add("value", property.getValue());
			bsonProperties.add(bsonProperty);
		}
		json.add("properties", bsonProperties);
		return json;
	}
	
	@Override
	public ArrayList<Searchable> getChildsToCreate() {
		//Foreach linked data, check if it has to be created
		ArrayList<Searchable> lstChildsToCreate =  super.getChildsToCreate();
		for(Data data : linkedData)
			if(data.haveToCreate())
				lstChildsToCreate.add(data);
		return lstChildsToCreate;
	}

	@Override
	protected void fillFragment(LinkedHashMap<String, byte[]> fragment) {
		//write every properties
		fragment.put("_nbProperties", (properties.size()+"").getBytes());
		for (int i = 0; i < properties.size();i++) {
			MetaProperty property = properties.get(i);
			fragment.put("_i"+i+"_property_name", property.getValue().getBytes());
			fragment.put("_i"+i+"_property_value", property.getName().getBytes());
		}
		//write every data's hash
		fragment.put("_nbLinkedData", (linkedData.size()+"").getBytes());
		for (int i = 0; i < linkedData.size();i++) {
			Data data = linkedData.get(i);
			fragment.put("_i"+i+"_data", data.getHashCode().getBytes());
		}
	}

	@Override
	protected void decodefragment(LinkedHashMap<String, byte[]> fragment) {
		//when this method is called in a metaData, her state is no more a real
		//MetaData but a temporary metaData, it means, it only represent what's 
		//over the network, so source = null ans result = null
		linkedData = null;
		//but not properties
		properties = new ArrayList<MetaProperty>();
		//and the Search cannot be write or updated in database
		updateDB   = false;
		createInDb = false; 
		
		//extract all linkedDatas and delete it from the fragment too
		int nbProperties = Integer.parseInt(new String(fragment.get("nbMetaData")));
		for(int i=0; i<nbProperties; i++){
			String name = new String(fragment.get("_i"+i+"_property_name"));
			String value = new String(fragment.get("_i"+i+"_property_value"));
			MetaProperty property = new MetaProperty(name, value);
			fragment.remove("_i"+i+"_property_name");
			fragment.remove("_i"+i+"_property_value");
			properties.add(property);
		}
		
		//extract all linkedDatas and delete it from the fragment too
		int nbLinkedData = Integer.parseInt(new String(fragment.get("nbMetaData")));
		tmpLinkedData  	 = new ArrayList<String>();
		for(int i=0; i<nbLinkedData; i++){
			String data = new String(fragment.get("_i"+i+"_data"));
			fragment.remove("_i"+i+"_data");
			tmpLinkedData.add(data);
		}
	}

	public ArrayList<String> getTmpLinkedData() {
		return tmpLinkedData;
	}
}
