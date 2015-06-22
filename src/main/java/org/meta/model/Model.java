package org.meta.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kyotocabinet.DB;
import org.bson.BSON;
import org.bson.BSONDecoder;
import org.bson.BSONEncoder;
import org.bson.BSONObject;
import org.bson.BasicBSONDecoder;
import org.bson.BasicBSONEncoder;
import org.bson.types.BasicBSONList;
import org.meta.common.MetHash;
import org.meta.common.MetaProperties;
import org.meta.model.exceptions.ModelException;

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
 * @author Nicolas MICHON, Thomas LAVOCAT
 *
 */
public class Model {

    private static Model instance;
    private static final String DEFAULT_DATABASE_FILE = "db/jmeta.kch";
    DB kyotoDB;
    ModelFactory factory;

    /**
     * Instanciate a new model. Init the dataBaseConnection.
     *
     * @throws org.meta.model.exceptions.ModelException
     */
    private Model() throws ModelException {
        initDataBase();
        factory = new ModelFactory();
    }

    @Override
    protected void finalize() {
        kyotoDB.close();
    }

    /**
     * Singleton instance getter.
     *
     * @return The model Instance.
     */
    public synchronized static Model getInstance() {
        if (instance == null) {
            try {
                instance = new Model();
            } catch (ModelException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        return instance;
    }

    /**
     *
     * @return The model factory.
     */
    public ModelFactory getFactory() {
        return this.factory;
    }

    /**
     * Initialize data base connection
     *
     * @throws LibraryException
     */
    private void initDataBase() throws ModelException {
        String databaseFile = MetaProperties.getProperty("database_path", DEFAULT_DATABASE_FILE);
        kyotoDB = new DB();
        if (!kyotoDB.open(databaseFile, DB.OREADER | DB.OWRITER | DB.OCREATE | DB.MSET)) {
            throw new ModelException("Unable to start kyoto cabinet with database file : " + databaseFile);
        }
    }

    /**
     *
     * @param hash
     * @return a search pointed by his hash. Return null if not found or if the
     * hash is not pointed a Search object
     */
    public Search getSearch(MetHash hash) {
        Search search = null;
        try {
            //Try to load the search in the data base
            search = (Search) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return search;
    }

    /**
     *
     * @param hash
     * @return a MetaData pointed by his hash or null if the hash is pointed on
     * nothing or if the hash is pointed on a non MetaData object
     */
    public MetaData getMetaData(MetHash hash) {
        MetaData metaData = null;
        try {
            metaData = (MetaData) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return metaData;
    }

    /**
     *
     * @param hash
     * @return a DataString object or null if the hash does not exists
     */
    public DataString getDataString(MetHash hash) {
        DataString data = null;

        try {
            data = (DataString) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     *
     * @param hash
     * @return a DataString object or null if the hash does not exists
     */
    public DataFile getDataFile(MetHash hash) {
        DataFile data = null;

        try {
            data = (DataFile) load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     *
     * @param hash
     * @return the Search linked to the hash or null if not found
     */
    public Searchable getSearchable(MetHash hash) {
        Searchable foundedObject = null;
        try {
            foundedObject = load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return foundedObject;
    }

    /**
     * Recursive synchronized method Load.
     *
     * @param hash
     * @return a searchale object if found or null if not.
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Searchable load(byte[] hash) throws
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        byte[] serializedData = kyotoDB.get(hash);
        if (serializedData == null) {
            return null;
        }
        BSONObject bsonObject = null;
        try {
            bsonObject = BSON.decode(serializedData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Searchable searchable = null;

        String typeName = bsonObject.get("type").toString();
        ModelType type = ModelType.valueOf(typeName);
        searchable = factory.getInstance(type);
        //Set the hash code
        searchable.setHash(new MetHash(hash));
        /*
         * Now we're looking for what is this object ? And extract the
         * good one.
         */
        if (searchable instanceof Search) {
            extractSearch(searchable, bsonObject);
        } else if (searchable instanceof MetaData) {
            extractMetaData(searchable, bsonObject);
        } else if (searchable instanceof DataFile) {
            extractDataFile(searchable, bsonObject);
        } else if (searchable instanceof DataString) {
            extractDataString(searchable, bsonObject);
        }
        searchable.setState(Searchable.ObjectState.UP_TO_DATE);
        //Freshly out from db : up to date
        return searchable;
    }

    /**
     *
     * @param searchable
     */
    private void extractDataString(Searchable searchable, BSONObject bsonObject) {
        DataString data = (DataString) searchable;
        data.setString(bsonObject.get("string").toString());
    }

    /**
     * Extract a data from Searchable object
     *
     * @param searchable
     * @param jsonSearcheable
     */
    private void extractDataFile(Searchable searchable, BSONObject bsonObject) {
        DataFile data = (DataFile) searchable;
        String filePath = bsonObject.get("file").toString();
        File file = new File(filePath);
        data.setFile(file);
    }

    /**
     * Extract a metadata from a searchale object
     *
     * @param searchable
     * @param jsonSearcheable
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private void extractMetaData(Searchable searchable, BSONObject bsonObject)
            throws
            ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        MetaData metaData = (MetaData) searchable;
        List<Data> linkedData = new ArrayList<Data>();
        BasicBSONList bsonLinkedData = (BasicBSONList) bsonObject.get("linkedData");
        for (String key : bsonLinkedData.keySet()) {
            MetHash hash = new MetHash(bsonLinkedData.get(key).toString());
            Data toAdd = (Data) load(hash.toByteArray());
            linkedData.add(toAdd);
        }
        BasicBSONList bsonProperties = (BasicBSONList) bsonObject.get("properties");
        BSONObject tmp;
        List<MetaProperty> properties = new ArrayList<MetaProperty>();
        for (String key : bsonProperties.keySet()) {
            tmp = (BSONObject) bsonProperties.get(key);
            MetaProperty toAdd = new MetaProperty(tmp.get("name").toString(), tmp.get("value").toString());
            properties.add(toAdd);
        }
        metaData.setLinkedData(linkedData);
        metaData.setProperties(properties);
    }

    /**
     * Extract a search from a searchable object
     *
     * @param searchable
     * @param jsonSearcheable
     * @param hashSource
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessExceptionn
     */
    private void extractSearch(Searchable searchable, BSONObject bsonObject)
            throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException {
        Search search = (Search) searchable;
        //load the source from her hash
        MetHash hash = new MetHash(bsonObject.get("source").toString());
        Searchable source = load(hash.toByteArray());
        List<MetaData> results = new ArrayList<MetaData>();
        BasicBSONList bsonResultsList = (BasicBSONList) bsonObject.get("results");
        for (String key : bsonResultsList.keySet()) {
            hash = new MetHash(bsonResultsList.get(key).toString());
            Searchable result = load(hash.toByteArray());
            if (result != null) {
                results.add((MetaData) result);
            }
        }
        search.setSource(source);
        search.setResults(results);
    }

    /**
     *
     * @param hash
     * @return A searchable object, or null if not found.
     */
    public Searchable get(MetHash hash) {
        Searchable ret = null;
        try {
            ret = load(hash.toByteArray());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    /**
     * 
     * @param startTx
     * @return 
     */
    private boolean startTransaction(boolean startTx) {
        if (!startTx) {
            return true;
        }
        if (!kyotoDB.begin_transaction(false)) {
            System.err.println("KYOTO FAILED TO START TX! " + kyotoDB.error());
            return false;
        }
        return true;
    }

    /**
     * 
     * @param commit if true commits the current transaction
     * if false rollback.
     * 
     * @return true on success, false otherwise
     */
    private boolean commitTransaction(boolean commit) {
        return kyotoDB.end_transaction(commit);
    }

    /**
     *
     * @see Model.set
     *
     * @param searchable The object to set to db.
     *
     * @param startTx If true starts a transaction for all writes to db.
     *
     * @return true on success, false otherwise
     *
     * If false is returned, the database remains untouched.
     */
    private boolean set(Searchable searchable, boolean startTx) {
        boolean status = startTransaction(startTx);
        //Based on the object's type, redirects to specific set method.
        switch (ModelType.fromClass(searchable.getClass())) {
            case DATAFILE:
            case DATASTRING:
                //Datas have no childs, nothing to do here
                break;
            case METADATA:
                status = this.setMetaData((MetaData) searchable);
                break;
            case SEARCH:
                status = this.setSearch((Search) searchable);
                break;
            default:
                return false;
        }
        //Finally encode the object itself and put it to db
        BSONObject bsonObject = searchable.getBson();
        byte[] data = null;
        try {
            data = BSON.encode(bsonObject);
            status = status && data != null;
        } catch (Exception e) {
            System.err.println("ERROR ENCODE BSON object : " + bsonObject.toString());
            e.printStackTrace();
            status = false;
        }
        if (status) {
            byte[] key = searchable.getHash().toByteArray();
            byte[] existingData = kyotoDB.get(key);
            if (existingData == null || !Arrays.equals(data, existingData)) {
                status = status && kyotoDB.set(key, data);
            }
        }
        if (startTx) {
            status = commitTransaction(status) && status;
        }
        return status;
    }

    /**
     * Creates or updates a searchable object in database. All children of given
     * object are also created/updated.
     *
     * @param searchable The object to create / update
     *
     * @return true on success, false otherwise
     */
    public boolean set(Searchable searchable) {
        if (searchable == null) {
            return false;
        }
        return set(searchable, true);
    }

    /**
     * Calls the 'set' method for each of the search's children.
     *
     * @param search A Search object.
     *
     */
    private boolean setSearch(Search search) {
        boolean status = true;

        if (search.getSource().getState() != Searchable.ObjectState.UP_TO_DATE) {
            status = status && this.set(search.getSource(), false);
        }
        for (MetaData metaData : search.getResults()) {
            if (metaData.getState() != Searchable.ObjectState.UP_TO_DATE) {
                status = status && this.set(metaData, false);
            }
        }
        return status;
    }

    /**
     * Calls the 'set' method for each of the metaData's children.
     *
     * @param metaData The MetaData object
     */
    private boolean setMetaData(MetaData metaData) {
        boolean status = true;

        for (Data data : metaData.getLinkedData()) {
            if (data.getState() != Searchable.ObjectState.UP_TO_DATE) {
                status = status && this.set(data, false);
            }
        }
        return status;
    }

    /**
     * Delete an object in DB
     *
     * @param searchable the object to remove from db
     * @return true on success, false otherwise
     */
    public boolean remove(Searchable searchable) {
        return remove(searchable.getHash());
    }

    /**
     * Delete an object in DB
     *
     * @param hash The hash to remove from db
     * @return true on success, false otherwise
     */
    public boolean remove(MetHash hash) {
        startTransaction(true);
        boolean status = kyotoDB.remove(hash.toByteArray());
        return commitTransaction(status) && status;
    }

	public MetHash hash(File file) {
		// TODO Auto-generated method stub
		return null;
	}
}