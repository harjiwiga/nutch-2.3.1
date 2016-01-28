package org.apache.nutch.store.readable.utils;

import org.apache.gora.persistency.Persistent;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.gora.util.GoraException;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.storage.Host;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;

/**
 * Created by harji on 1/20/16.
 */
public class StorageUtilsPlugin extends StorageUtils{

    public static <K, V extends Persistent> DataStore<K, V> createWebStore(Configuration conf, Class<K> keyClass, Class<V> persistentClass, String crawlId) throws ClassNotFoundException, GoraException {

        crawlId = (crawlId==null)?conf.get(Nutch.CRAWL_ID_KEY, ""):crawlId;
        String schemaPrefix = "";
        if (!crawlId.isEmpty()) {
            schemaPrefix = crawlId + "_";
        }

        String schema;
        if (WebPage.class.equals(persistentClass)) {
            schema = conf.get("storage.schema.webpage", "webpage");
            conf.set("preferred.schema.name", schemaPrefix + "webpage");
        } else if (Host.class.equals(persistentClass)) {
            schema = conf.get("storage.schema.host", "host");
            conf.set("preferred.schema.name", schemaPrefix + "host");
        } else {
            throw new UnsupportedOperationException("Unable to create store for class " + persistentClass);
        }

        Class<? extends DataStore<K, V>> dataStoreClass =
                (Class<? extends DataStore<K, V>>) StorageUtils.getDataStoreClass(conf);
        return DataStoreFactory.createDataStore(dataStoreClass,
                keyClass, persistentClass, conf, schema);
    }
}
