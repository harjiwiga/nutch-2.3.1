package org.apache.nutch.store.readable.orm;

import org.apache.nutch.storage.ParseStatus;
import org.apache.nutch.storage.ProtocolStatus;
import org.apache.nutch.storage.WebPage;

/**
 * Created by harji on 1/20/16.
 */

//org.apache.gora.persistency.impl.PersistentBase
public class ReadableWebPage extends WebPage  implements org.apache.avro.specific.SpecificRecord, org.apache.gora.persistency.Persistent {

    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"WebPage\",\"namespace\":\"org.apache.nutch.storage\",\"fields\":[{\"name\":\"__g__dirty\",\"type\":\"bytes\",\"doc\":\"Bytes used to represent weather or not a field is dirty.\",\"default\":\"AAAAAA==\"},{\"name\":\"baseUrl\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"status\",\"type\":\"int\",\"default\":0},{\"name\":\"fetchTime\",\"type\":\"long\",\"default\":0},{\"name\":\"prevFetchTime\",\"type\":\"long\",\"default\":0},{\"name\":\"fetchInterval\",\"type\":\"int\",\"default\":0},{\"name\":\"retriesSinceFetch\",\"type\":\"int\",\"default\":0},{\"name\":\"modifiedTime\",\"type\":\"long\",\"default\":0},{\"name\":\"prevModifiedTime\",\"type\":\"long\",\"default\":0},{\"name\":\"protocolStatus\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"ProtocolStatus\",\"fields\":[{\"name\":\"__g__dirty\",\"type\":\"bytes\",\"doc\":\"Bytes used to represent weather or not a field is dirty.\",\"default\":\"AA==\"},{\"name\":\"code\",\"type\":\"int\",\"default\":0},{\"name\":\"args\",\"type\":{\"type\":\"array\",\"items\":\"string\"},\"default\":[]},{\"name\":\"lastModified\",\"type\":\"long\",\"default\":0}]}],\"default\":null},{\"name\":\"content\",\"type\":[\"null\",\"bytes\"],\"default\":null},{\"name\":\"contentType\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"prevSignature\",\"type\":[\"null\",\"bytes\"],\"default\":null},{\"name\":\"signature\",\"type\":[\"null\",\"bytes\"],\"default\":null},{\"name\":\"title\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"text\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"parseStatus\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"ParseStatus\",\"fields\":[{\"name\":\"__g__dirty\",\"type\":\"bytes\",\"doc\":\"Bytes used to represent weather or not a field is dirty.\",\"default\":\"AA==\"},{\"name\":\"majorCode\",\"type\":\"int\",\"default\":0},{\"name\":\"minorCode\",\"type\":\"int\",\"default\":0},{\"name\":\"args\",\"type\":{\"type\":\"array\",\"items\":\"string\"},\"default\":[]}]}],\"default\":null},{\"name\":\"score\",\"type\":\"float\",\"default\":0},{\"name\":\"reprUrl\",\"type\":[\"null\",\"string\"],\"default\":null},{\"name\":\"headers\",\"type\":{\"type\":\"map\",\"values\":[\"null\",\"string\"]},\"default\":{}},{\"name\":\"outlinks\",\"type\":{\"type\":\"map\",\"values\":[\"null\",\"string\"]},\"default\":{}},{\"name\":\"inlinks\",\"type\":{\"type\":\"map\",\"values\":[\"null\",\"string\"]},\"default\":{}},{\"name\":\"markers\",\"type\":{\"type\":\"map\",\"values\":[\"null\",\"string\"]},\"default\":{}},{\"name\":\"metadata\",\"type\":{\"type\":\"map\",\"values\":[\"null\",\"bytes\"]},\"default\":{}},{\"name\":\"batchId\",\"type\":[\"null\",\"string\"],\"default\":null}]}");

    public static final Tombstone TOMBSTONE = new Tombstone();
    @Override
    public WebPage.Tombstone getTombstone() {
        return TOMBSTONE;
    }

    @Override
    public WebPage newInstance() {
        return newBuilder().build();
    }

//    @Override
//    public Persistent newInstance() {
//        return null;
//    }

    /** Enum containing all data bean's fields. */
    public static enum Field {
        __G__DIRTY(0, "__g__dirty"),
        BASE_URL(1, "baseUrl"),
        STATUS(2, "status"),
        FETCH_TIME(3, "fetchTime"),
        PREV_FETCH_TIME(4, "prevFetchTime"),
        FETCH_INTERVAL(5, "fetchInterval"),
        RETRIES_SINCE_FETCH(6, "retriesSinceFetch"),
        MODIFIED_TIME(7, "modifiedTime"),
        PREV_MODIFIED_TIME(8, "prevModifiedTime"),
        PROTOCOL_STATUS(9, "protocolStatus"),
        CONTENT(10, "content"),
        CONTENT_TYPE(11, "contentType"),
        PREV_SIGNATURE(12, "prevSignature"),
        SIGNATURE(13, "signature"),
        TITLE(14, "title"),
        TEXT(15, "text"),
        PARSE_STATUS(16, "parseStatus"),
        SCORE(17, "score"),
        REPR_URL(18, "reprUrl"),
        HEADERS(19, "headers"),
        OUTLINKS(20, "outlinks"),
        INLINKS(21, "inlinks"),
        MARKERS(22, "markers"),
        METADATA(23, "metadata"),
        BATCH_ID(24, "batchId"),
        READBL(25, "readable"),
        ;
        /**
         * Field's index.
         */
        private int index;

        /**
         * Field's name.
         */
        private String name;

        /**
         * Field's constructor
         * @param index field's index.
         * @param name field's name.
         */
        Field(int index, String name) {this.index=index;this.name=name;}

        /**
         * Gets field's index.
         * @return int field's index.
         */
        public int getIndex() {return index;}

        /**
         * Gets field's name.
         * @return String field's name.
         */
        public String getName() {return name;}

        /**
         * Gets field's attributes to string.
         * @return String field's attributes to string.
         */
        public String toString() {return name;}
    };

    public static final String[] _ALL_FIELDS = {
            "__g__dirty",
            "baseUrl",
            "status",
            "fetchTime",
            "prevFetchTime",
            "fetchInterval",
            "retriesSinceFetch",
            "modifiedTime",
            "prevModifiedTime",
            "protocolStatus",
            "content",
            "contentType",
            "prevSignature",
            "signature",
            "title",
            "text",
            "parseStatus",
            "score",
            "reprUrl",
            "headers",
            "outlinks",
            "inlinks",
            "markers",
            "metadata",
            "batchId",
            "readable",
    };

    /**
     * Gets the total field count.
     * @return int field count
     */
    public int getFieldsCount() {
        return WebPage._ALL_FIELDS.length;
    }


    /** Bytes used to represent weather or not a field is dirty. */
    private java.nio.ByteBuffer __g__dirty = java.nio.ByteBuffer.wrap(new byte[4]);
    private CharSequence baseUrl;
    private int status;
    private long fetchTime;
    private long prevFetchTime;
    private int fetchInterval;
    private int retriesSinceFetch;
    private long modifiedTime;
    private long prevModifiedTime;
    private ProtocolStatus protocolStatus;
    private java.nio.ByteBuffer content;
    private CharSequence contentType;
    private java.nio.ByteBuffer prevSignature;
    private java.nio.ByteBuffer signature;
    private CharSequence title;
    private CharSequence text;
    private ParseStatus parseStatus;
    private float score;
    private CharSequence reprUrl;
    private java.util.Map<CharSequence,CharSequence> headers;
    private java.util.Map<CharSequence,CharSequence> outlinks;
    private java.util.Map<CharSequence,CharSequence> inlinks;
    private java.util.Map<CharSequence,CharSequence> markers;
    private java.util.Map<CharSequence,java.nio.ByteBuffer> metadata;
    private CharSequence batchId;
    private String readAble;
    public org.apache.avro.Schema getSchema() { return SCHEMA$; }
    // Used by DatumWriter.  Applications should not call.
    public Object get(int field$) {
        switch (field$) {
            case 0: return __g__dirty;
            case 1: return baseUrl;
            case 2: return status;
            case 3: return fetchTime;
            case 4: return prevFetchTime;
            case 5: return fetchInterval;
            case 6: return retriesSinceFetch;
            case 7: return modifiedTime;
            case 8: return prevModifiedTime;
            case 9: return protocolStatus;
            case 10: return content;
            case 11: return contentType;
            case 12: return prevSignature;
            case 13: return signature;
            case 14: return title;
            case 15: return text;
            case 16: return parseStatus;
            case 17: return score;
            case 18: return reprUrl;
            case 19: return headers;
            case 20: return outlinks;
            case 21: return inlinks;
            case 22: return markers;
            case 23: return metadata;
            case 24: return batchId;
            case 25: return readAble;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value="unchecked")
    public void put(int field$, Object value) {
        switch (field$) {
            case 0: __g__dirty = (java.nio.ByteBuffer)(value); break;
            case 1: baseUrl = (CharSequence)(value); break;
            case 2: status = (Integer)(value); break;
            case 3: fetchTime = (Long)(value); break;
            case 4: prevFetchTime = (Long)(value); break;
            case 5: fetchInterval = (Integer)(value); break;
            case 6: retriesSinceFetch = (Integer)(value); break;
            case 7: modifiedTime = (Long)(value); break;
            case 8: prevModifiedTime = (Long)(value); break;
            case 9: protocolStatus = (ProtocolStatus)(value); break;
            case 10: content = (java.nio.ByteBuffer)(value); break;
            case 11: contentType = (CharSequence)(value); break;
            case 12: prevSignature = (java.nio.ByteBuffer)(value); break;
            case 13: signature = (java.nio.ByteBuffer)(value); break;
            case 14: title = (CharSequence)(value); break;
            case 15: text = (CharSequence)(value); break;
            case 16: parseStatus = (ParseStatus)(value); break;
            case 17: score = (Float)(value); break;
            case 18: reprUrl = (CharSequence)(value); break;
            case 19: headers = (java.util.Map<CharSequence,CharSequence>)((value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)value)); break;
            case 20: outlinks = (java.util.Map<CharSequence,CharSequence>)((value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)value)); break;
            case 21: inlinks = (java.util.Map<CharSequence,CharSequence>)((value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)value)); break;
            case 22: markers = (java.util.Map<CharSequence,CharSequence>)((value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)value)); break;
            case 23: metadata = (java.util.Map<CharSequence,java.nio.ByteBuffer>)((value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper((java.util.Map)value)); break;
            case 24: batchId = (CharSequence)(value); break;
            default: throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'baseUrl' field.
     */
    public CharSequence getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the value of the 'baseUrl' field.
     * @param value the value to set.
     */
    public void setBaseUrl(CharSequence value) {
        this.baseUrl = value;
        setDirty(1);
    }

    /**
     * Checks the dirty status of the 'baseUrl' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isBaseUrlDirty(CharSequence value) {
        return isDirty(1);
    }

    /**
     * Gets the value of the 'status' field.
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * Sets the value of the 'status' field.
     * @param value the value to set.
     */
    public void setStatus(Integer value) {
        this.status = value;
        setDirty(2);
    }

    /**
     * Checks the dirty status of the 'status' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isStatusDirty(Integer value) {
        return isDirty(2);
    }

    /**
     * Gets the value of the 'fetchTime' field.
     */
    public Long getFetchTime() {
        return fetchTime;
    }

    /**
     * Sets the value of the 'fetchTime' field.
     * @param value the value to set.
     */
    public void setFetchTime(Long value) {
        this.fetchTime = value;
        setDirty(3);
    }

    /**
     * Checks the dirty status of the 'fetchTime' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isFetchTimeDirty(Long value) {
        return isDirty(3);
    }

    /**
     * Gets the value of the 'prevFetchTime' field.
     */
    public Long getPrevFetchTime() {
        return prevFetchTime;
    }

    /**
     * Sets the value of the 'prevFetchTime' field.
     * @param value the value to set.
     */
    public void setPrevFetchTime(Long value) {
        this.prevFetchTime = value;
        setDirty(4);
    }

    /**
     * Checks the dirty status of the 'prevFetchTime' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isPrevFetchTimeDirty(Long value) {
        return isDirty(4);
    }

    /**
     * Gets the value of the 'fetchInterval' field.
     */
    public Integer getFetchInterval() {
        return fetchInterval;
    }

    /**
     * Sets the value of the 'fetchInterval' field.
     * @param value the value to set.
     */
    public void setFetchInterval(Integer value) {
        this.fetchInterval = value;
        setDirty(5);
    }

    /**
     * Checks the dirty status of the 'fetchInterval' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isFetchIntervalDirty(Integer value) {
        return isDirty(5);
    }

    /**
     * Gets the value of the 'retriesSinceFetch' field.
     */
    public Integer getRetriesSinceFetch() {
        return retriesSinceFetch;
    }

    /**
     * Sets the value of the 'retriesSinceFetch' field.
     * @param value the value to set.
     */
    public void setRetriesSinceFetch(Integer value) {
        this.retriesSinceFetch = value;
        setDirty(6);
    }

    /**
     * Checks the dirty status of the 'retriesSinceFetch' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isRetriesSinceFetchDirty(Integer value) {
        return isDirty(6);
    }

    /**
     * Gets the value of the 'modifiedTime' field.
     */
    public Long getModifiedTime() {
        return modifiedTime;
    }

    /**
     * Sets the value of the 'modifiedTime' field.
     * @param value the value to set.
     */
    public void setModifiedTime(Long value) {
        this.modifiedTime = value;
        setDirty(7);
    }

    /**
     * Checks the dirty status of the 'modifiedTime' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isModifiedTimeDirty(Long value) {
        return isDirty(7);
    }

    /**
     * Gets the value of the 'prevModifiedTime' field.
     */
    public Long getPrevModifiedTime() {
        return prevModifiedTime;
    }

    /**
     * Sets the value of the 'prevModifiedTime' field.
     * @param value the value to set.
     */
    public void setPrevModifiedTime(Long value) {
        this.prevModifiedTime = value;
        setDirty(8);
    }

    /**
     * Checks the dirty status of the 'prevModifiedTime' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isPrevModifiedTimeDirty(Long value) {
        return isDirty(8);
    }

    /**
     * Gets the value of the 'protocolStatus' field.
     */
    public ProtocolStatus getProtocolStatus() {
        return protocolStatus;
    }

    /**
     * Sets the value of the 'protocolStatus' field.
     * @param value the value to set.
     */
    public void setProtocolStatus(ProtocolStatus value) {
        this.protocolStatus = value;
        setDirty(9);
    }

    /**
     * Checks the dirty status of the 'protocolStatus' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isProtocolStatusDirty(ProtocolStatus value) {
        return isDirty(9);
    }

    /**
     * Gets the value of the 'content' field.
     */
    public java.nio.ByteBuffer getContent() {
        return content;
    }

    /**
     * Sets the value of the 'content' field.
     * @param value the value to set.
     */
    public void setContent(java.nio.ByteBuffer value) {
        this.content = value;
        setDirty(10);
    }

    /**
     * Checks the dirty status of the 'content' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isContentDirty(java.nio.ByteBuffer value) {
        return isDirty(10);
    }

    /**
     * Gets the value of the 'contentType' field.
     */
    public CharSequence getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the 'contentType' field.
     * @param value the value to set.
     */
    public void setContentType(CharSequence value) {
        this.contentType = value;
        setDirty(11);
    }

    /**
     * Checks the dirty status of the 'contentType' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isContentTypeDirty(CharSequence value) {
        return isDirty(11);
    }

    /**
     * Gets the value of the 'prevSignature' field.
     */
    public java.nio.ByteBuffer getPrevSignature() {
        return prevSignature;
    }

    /**
     * Sets the value of the 'prevSignature' field.
     * @param value the value to set.
     */
    public void setPrevSignature(java.nio.ByteBuffer value) {
        this.prevSignature = value;
        setDirty(12);
    }

    /**
     * Checks the dirty status of the 'prevSignature' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isPrevSignatureDirty(java.nio.ByteBuffer value) {
        return isDirty(12);
    }

    /**
     * Gets the value of the 'signature' field.
     */
    public java.nio.ByteBuffer getSignature() {
        return signature;
    }

    /**
     * Sets the value of the 'signature' field.
     * @param value the value to set.
     */
    public void setSignature(java.nio.ByteBuffer value) {
        this.signature = value;
        setDirty(13);
    }

    /**
     * Checks the dirty status of the 'signature' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isSignatureDirty(java.nio.ByteBuffer value) {
        return isDirty(13);
    }

    /**
     * Gets the value of the 'title' field.
     */
    public CharSequence getTitle() {
        return title;
    }

    /**
     * Sets the value of the 'title' field.
     * @param value the value to set.
     */
    public void setTitle(CharSequence value) {
        this.title = value;
        setDirty(14);
    }

    /**
     * Checks the dirty status of the 'title' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isTitleDirty(CharSequence value) {
        return isDirty(14);
    }

    /**
     * Gets the value of the 'text' field.
     */
    public CharSequence getText() {
        return text;
    }

    /**
     * Sets the value of the 'text' field.
     * @param value the value to set.
     */
    public void setText(CharSequence value) {
        this.text = value;
        setDirty(15);
    }

    /**
     * Checks the dirty status of the 'text' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isTextDirty(CharSequence value) {
        return isDirty(15);
    }

    /**
     * Gets the value of the 'parseStatus' field.
     */
    public ParseStatus getParseStatus() {
        return parseStatus;
    }

    /**
     * Sets the value of the 'parseStatus' field.
     * @param value the value to set.
     */
    public void setParseStatus(ParseStatus value) {
        this.parseStatus = value;
        setDirty(16);
    }

    /**
     * Checks the dirty status of the 'parseStatus' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isParseStatusDirty(ParseStatus value) {
        return isDirty(16);
    }

    /**
     * Gets the value of the 'score' field.
     */
    public Float getScore() {
        return score;
    }

    /**
     * Sets the value of the 'score' field.
     * @param value the value to set.
     */
    public void setScore(Float value) {
        this.score = value;
        setDirty(17);
    }

    /**
     * Checks the dirty status of the 'score' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isScoreDirty(Float value) {
        return isDirty(17);
    }

    /**
     * Gets the value of the 'reprUrl' field.
     */
    public CharSequence getReprUrl() {
        return reprUrl;
    }

    /**
     * Sets the value of the 'reprUrl' field.
     * @param value the value to set.
     */
    public void setReprUrl(CharSequence value) {
        this.reprUrl = value;
        setDirty(18);
    }

    /**
     * Checks the dirty status of the 'reprUrl' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isReprUrlDirty(CharSequence value) {
        return isDirty(18);
    }

    /**
     * Gets the value of the 'headers' field.
     */
    public java.util.Map<CharSequence,CharSequence> getHeaders() {
        return headers;
    }

    /**
     * Sets the value of the 'headers' field.
     * @param value the value to set.
     */
    public void setHeaders(java.util.Map<CharSequence,CharSequence> value) {
        this.headers = (value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper(value);
        setDirty(19);
    }

    /**
     * Checks the dirty status of the 'headers' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isHeadersDirty(java.util.Map<CharSequence,CharSequence> value) {
        return isDirty(19);
    }

    /**
     * Gets the value of the 'outlinks' field.
     */
    public java.util.Map<CharSequence,CharSequence> getOutlinks() {
        return outlinks;
    }

    /**
     * Sets the value of the 'outlinks' field.
     * @param value the value to set.
     */
    public void setOutlinks(java.util.Map<CharSequence,CharSequence> value) {
        this.outlinks = (value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper(value);
        setDirty(20);
    }

    /**
     * Checks the dirty status of the 'outlinks' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isOutlinksDirty(java.util.Map<CharSequence,CharSequence> value) {
        return isDirty(20);
    }

    /**
     * Gets the value of the 'inlinks' field.
     */
    public java.util.Map<CharSequence,CharSequence> getInlinks() {
        return inlinks;
    }

    /**
     * Sets the value of the 'inlinks' field.
     * @param value the value to set.
     */
    public void setInlinks(java.util.Map<CharSequence,CharSequence> value) {
        this.inlinks = (value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper(value);
        setDirty(21);
    }

    /**
     * Checks the dirty status of the 'inlinks' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isInlinksDirty(java.util.Map<CharSequence,CharSequence> value) {
        return isDirty(21);
    }

    /**
     * Gets the value of the 'markers' field.
     */
    public java.util.Map<CharSequence,CharSequence> getMarkers() {
        return markers;
    }

    /**
     * Sets the value of the 'markers' field.
     * @param value the value to set.
     */
    public void setMarkers(java.util.Map<CharSequence,CharSequence> value) {
        this.markers = (value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper(value);
        setDirty(22);
    }

    /**
     * Checks the dirty status of the 'markers' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isMarkersDirty(java.util.Map<CharSequence,CharSequence> value) {
        return isDirty(22);
    }

    /**
     * Gets the value of the 'metadata' field.
     */
    public java.util.Map<CharSequence,java.nio.ByteBuffer> getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the 'metadata' field.
     * @param value the value to set.
     */
    public void setMetadata(java.util.Map<CharSequence,java.nio.ByteBuffer> value) {
        this.metadata = (value instanceof org.apache.gora.persistency.Dirtyable) ? value : new org.apache.gora.persistency.impl.DirtyMapWrapper(value);
        setDirty(23);
    }

    /**
     * Checks the dirty status of the 'metadata' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isMetadataDirty(java.util.Map<CharSequence,java.nio.ByteBuffer> value) {
        return isDirty(23);
    }

    /**
     * Gets the value of the 'batchId' field.
     */
    public CharSequence getBatchId() {
        return batchId;
    }

    /**
     * Sets the value of the 'batchId' field.
     * @param value the value to set.
     */
    public void setBatchId(CharSequence value) {
        this.batchId = value;
        setDirty(24);
    }

    /**
     * Checks the dirty status of the 'batchId' field. A field is dirty if it represents a change that has not yet been written to the database.
     * @param value the value to set.
     */
    public boolean isBatchIdDirty(CharSequence value) {
        return isDirty(24);
    }

    /** Creates a new WebPage RecordBuilder */

    /** Creates a new WebPage RecordBuilder */
//    public static Builder newBuilder() {
//        return new Builder();
//    }
//
//    /** Creates a new WebPage RecordBuilder by copying an existing Builder */
//    public static Builder newBuilder(Builder other) {
//        return new Builder(other);
//    }
//
//    /** Creates a new WebPage RecordBuilder by copying an existing WebPage instance */
//    public static Builder newBuilder(WebPage other) {
//        return new Builder(other);
//    }

    public static Builder newBuilder(){

        return WebPage.newBuilder();
    }

    public static Builder newBuilder(Builder other){

        return WebPage.newBuilder(other);
    }

    public static Builder newBuilder(ReadableWebPage other){

        return ReadableWebPage.newBuilder(other);
    }

    private static java.nio.ByteBuffer deepCopyToWriteOnlyBuffer(
            java.nio.ByteBuffer input) {
        java.nio.ByteBuffer copy = java.nio.ByteBuffer.allocate(input.capacity());
        int position = input.position();
        input.reset();
        int mark = input.position();
        int limit = input.limit();
        input.rewind();
        input.limit(input.capacity());
        copy.put(input);
        input.rewind();
        copy.rewind();
        input.position(mark);
        input.mark();
        copy.position(mark);
        copy.mark();
        input.position(position);
        copy.position(position);
        input.limit(limit);
        copy.limit(limit);
        return copy.asReadOnlyBuffer();
    }

}
