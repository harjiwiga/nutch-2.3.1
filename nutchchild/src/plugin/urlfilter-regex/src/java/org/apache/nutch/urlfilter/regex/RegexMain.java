package org.apache.nutch.urlfilter.regex;


import org.apache.gora.persistency.Persistent;
import org.apache.gora.store.DataStore;
import org.apache.gora.store.DataStoreFactory;
import org.apache.gora.util.GoraException;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.storage.Host;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.apache.nutch.util.NutchConfiguration;

import java.io.FileReader;
import java.io.IOException;
import java.util.regex.PatternSyntaxException;

/**
 * Created by harji on 1/15/16.
 */
public class RegexMain {
    public static void main(String[]args) throws PatternSyntaxException, IOException, ClassNotFoundException {

        Configuration localConf = NutchConfiguration.create();
//        localConf.set("storage.data.store.class", "org.apache.gora.memory.store.HBaseStore");

        DataStore<String, WebPage> store = createWebStore(localConf,
                String.class, WebPage.class,"TestCrawl");

//        store.schemaExists();

//        readWrite("single_id", store);
        System.out.println("Done.");

        WebPage camera = store.get("com.tokopedia.www:https/xiaomi-mobile/xiaomi-yi-action-camera-paket-komplit-16gb");
//        String schemaName = store.getSchemaName();
        String filename = "/home/harji/Workspace/Project/BigData/Project-Src/crawlers/nutch-2.x/runtime/local/conf/regex-urlfilter.txt";
        String url_exclude = "https://www.tokopedia.com/(()p/";
        String url_include = "https://www.tokopedia.com/sagalayaa/kogan-action-camera-12mp-white-wifi?key=eyJvYiI6IjEiLCJwYWdlIjoiMSIsInNxIjoiIn0&pos=2";

        RegexURLFilter regexFilter = new RegexURLFilter(new FileReader(filename));
        System.out.println("FILTER INCLUDE:"+regexFilter.filter(url_include));
        System.out.println("FILTER EXCLUDE:"+regexFilter.filter(url_exclude));
    }

    public static <K, V extends Persistent> DataStore<K, V> createWebStore(Configuration conf, Class<K> keyClass, Class<V> persistentClass,String crawlId) throws ClassNotFoundException, GoraException {

//        String crawlId = conf.get(Nutch.CRAWL_ID_KEY, "");
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
