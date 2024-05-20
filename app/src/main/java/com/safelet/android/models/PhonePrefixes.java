package com.safelet.android.models;


/**
 * Data model for storing phone prefixs
 */
public class PhonePrefixes {

    //        public static final int TYPE_DATE = 0;
    public static final int TYPE_LETTER = 1;
    public String prefix;
    public String countryName;
    public int type;

    public PhonePrefixes(String prefix, String country) {
        this.prefix = prefix;
        this.countryName = country;
    }

}
