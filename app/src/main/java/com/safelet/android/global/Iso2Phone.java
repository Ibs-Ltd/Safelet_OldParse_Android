/**
 * Created by Badea Mihai Bogdan on Oct 17, 2014
 * Copyright (c) 2014 XLTeam. All rights reserved.
 */
package com.safelet.android.global;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class is used at keeping all phone prefixes with corresponding country codes
 *
 * @author Mihai Badea
 */
final class Iso2Phone {

    private static final Map<String, String> COUNTRY_2PHONE = new HashMap<String, String>();

    static {
        COUNTRY_2PHONE.put("AF", "+93");
        COUNTRY_2PHONE.put("AL", "+355");
        COUNTRY_2PHONE.put("DZ", "+213");
        COUNTRY_2PHONE.put("AD", "+376");
        COUNTRY_2PHONE.put("AO", "+244");
        COUNTRY_2PHONE.put("AG", "+1-268");
        COUNTRY_2PHONE.put("AR", "+54");
        COUNTRY_2PHONE.put("AM", "+374");
        COUNTRY_2PHONE.put("AU", "+61");
        COUNTRY_2PHONE.put("AT", "+43");
        COUNTRY_2PHONE.put("AZ", "+994");
        COUNTRY_2PHONE.put("BS", "+1-242");
        COUNTRY_2PHONE.put("BH", "+973");
        COUNTRY_2PHONE.put("BD", "+880");
        COUNTRY_2PHONE.put("BB", "+1-246");
        COUNTRY_2PHONE.put("BY", "+375");
        COUNTRY_2PHONE.put("BE", "+32");
        COUNTRY_2PHONE.put("BZ", "+501");
        COUNTRY_2PHONE.put("BJ", "+229");
        COUNTRY_2PHONE.put("BT", "+975");
        COUNTRY_2PHONE.put("BO", "+591");
        COUNTRY_2PHONE.put("BA", "+387");
        COUNTRY_2PHONE.put("BW", "+267");
        COUNTRY_2PHONE.put("BR", "+55");
        COUNTRY_2PHONE.put("BN", "+673");
        COUNTRY_2PHONE.put("BG", "+359");
        COUNTRY_2PHONE.put("BF", "+226");
        COUNTRY_2PHONE.put("BI", "+257");
        COUNTRY_2PHONE.put("KH", "+855");
        COUNTRY_2PHONE.put("CM", "+237");
        COUNTRY_2PHONE.put("CA", "+1");
        COUNTRY_2PHONE.put("CV", "+238");
        COUNTRY_2PHONE.put("CF", "+236");
        COUNTRY_2PHONE.put("TD", "+235");
        COUNTRY_2PHONE.put("CL", "+56");
        COUNTRY_2PHONE.put("CN", "+86");
        COUNTRY_2PHONE.put("CO", "+57");
        COUNTRY_2PHONE.put("KM", "+269");
        COUNTRY_2PHONE.put("CD", "+243");
        COUNTRY_2PHONE.put("CG", "+242");
        COUNTRY_2PHONE.put("CR", "+506");
        COUNTRY_2PHONE.put("CI", "+225");
        COUNTRY_2PHONE.put("HR", "+385");
        COUNTRY_2PHONE.put("CU", "+53");
        COUNTRY_2PHONE.put("CY", "+357");
        COUNTRY_2PHONE.put("CZ", "+420");
        COUNTRY_2PHONE.put("DK", "+45");
        COUNTRY_2PHONE.put("DJ", "+253");
        COUNTRY_2PHONE.put("DM", "+1-767");
        COUNTRY_2PHONE.put("DO", "+1-809and1-829");
        COUNTRY_2PHONE.put("EC", "+593");
        COUNTRY_2PHONE.put("EG", "+20");
        COUNTRY_2PHONE.put("SV", "+503");
        COUNTRY_2PHONE.put("GQ", "+240");
        COUNTRY_2PHONE.put("ER", "+291");
        COUNTRY_2PHONE.put("EE", "+372");
        COUNTRY_2PHONE.put("ET", "+251");
        COUNTRY_2PHONE.put("FJ", "+679");
        COUNTRY_2PHONE.put("FI", "+358");
        COUNTRY_2PHONE.put("FR", "+33");
        COUNTRY_2PHONE.put("GA", "+241");
        COUNTRY_2PHONE.put("GM", "+220");
        COUNTRY_2PHONE.put("GE", "+995");
        COUNTRY_2PHONE.put("DE", "+49");
        COUNTRY_2PHONE.put("GH", "+233");
        COUNTRY_2PHONE.put("GR", "+30");
        COUNTRY_2PHONE.put("GD", "+1-473");
        COUNTRY_2PHONE.put("GT", "+502");
        COUNTRY_2PHONE.put("GN", "+224");
        COUNTRY_2PHONE.put("GW", "+245");
        COUNTRY_2PHONE.put("GY", "+592");
        COUNTRY_2PHONE.put("HT", "+509");
        COUNTRY_2PHONE.put("HN", "+504");
        COUNTRY_2PHONE.put("HU", "+36");
        COUNTRY_2PHONE.put("IS", "+354");
        COUNTRY_2PHONE.put("IN", "+91");
        COUNTRY_2PHONE.put("ID", "+62");
        COUNTRY_2PHONE.put("IR", "+98");
        COUNTRY_2PHONE.put("IQ", "+964");
        COUNTRY_2PHONE.put("IE", "+353");
        COUNTRY_2PHONE.put("IL", "+972");
        COUNTRY_2PHONE.put("IT", "+39");
        COUNTRY_2PHONE.put("JM", "+1-876");
        COUNTRY_2PHONE.put("JP", "+81");
        COUNTRY_2PHONE.put("JO", "+962");
        COUNTRY_2PHONE.put("KZ", "+7");
        COUNTRY_2PHONE.put("KE", "+254");
        COUNTRY_2PHONE.put("KI", "+686");
        COUNTRY_2PHONE.put("KP", "+850");
        COUNTRY_2PHONE.put("KR", "+82");
        COUNTRY_2PHONE.put("KW", "+965");
        COUNTRY_2PHONE.put("KG", "+996");
        COUNTRY_2PHONE.put("LA", "+856");
        COUNTRY_2PHONE.put("LV", "+371");
        COUNTRY_2PHONE.put("LB", "+961");
        COUNTRY_2PHONE.put("LS", "+266");
        COUNTRY_2PHONE.put("LR", "+231");
        COUNTRY_2PHONE.put("LY", "+218");
        COUNTRY_2PHONE.put("LI", "+423");
        COUNTRY_2PHONE.put("LT", "+370");
        COUNTRY_2PHONE.put("LU", "+352");
        COUNTRY_2PHONE.put("MK", "+389");
        COUNTRY_2PHONE.put("MG", "+261");
        COUNTRY_2PHONE.put("MW", "+265");
        COUNTRY_2PHONE.put("MY", "+60");
        COUNTRY_2PHONE.put("MV", "+960");
        COUNTRY_2PHONE.put("ML", "+223");
        COUNTRY_2PHONE.put("MT", "+356");
        COUNTRY_2PHONE.put("MH", "+692");
        COUNTRY_2PHONE.put("MR", "+222");
        COUNTRY_2PHONE.put("MU", "+230");
        COUNTRY_2PHONE.put("MX", "+52");
        COUNTRY_2PHONE.put("FM", "+691");
        COUNTRY_2PHONE.put("MD", "+373");
        COUNTRY_2PHONE.put("MC", "+377");
        COUNTRY_2PHONE.put("MN", "+976");
        COUNTRY_2PHONE.put("ME", "+382");
        COUNTRY_2PHONE.put("MA", "+212");
        COUNTRY_2PHONE.put("MZ", "+258");
        COUNTRY_2PHONE.put("MM", "+95");
        COUNTRY_2PHONE.put("NA", "+264");
        COUNTRY_2PHONE.put("NR", "+674");
        COUNTRY_2PHONE.put("NP", "+977");
        COUNTRY_2PHONE.put("NL", "+31");
        COUNTRY_2PHONE.put("NZ", "+64");
        COUNTRY_2PHONE.put("NI", "+505");
        COUNTRY_2PHONE.put("NE", "+227");
        COUNTRY_2PHONE.put("NG", "+234");
        COUNTRY_2PHONE.put("NO", "+47");
        COUNTRY_2PHONE.put("OM", "+968");
        COUNTRY_2PHONE.put("PK", "+92");
        COUNTRY_2PHONE.put("PW", "+680");
        COUNTRY_2PHONE.put("PA", "+507");
        COUNTRY_2PHONE.put("PG", "+675");
        COUNTRY_2PHONE.put("PY", "+595");
        COUNTRY_2PHONE.put("PE", "+51");
        COUNTRY_2PHONE.put("PH", "+63");
        COUNTRY_2PHONE.put("PL", "+48");
        COUNTRY_2PHONE.put("PT", "+351");
        COUNTRY_2PHONE.put("QA", "+974");
        COUNTRY_2PHONE.put("RO", "+40");
        COUNTRY_2PHONE.put("RU", "+7");
        COUNTRY_2PHONE.put("RW", "+250");
        COUNTRY_2PHONE.put("KN", "+1-869");
        COUNTRY_2PHONE.put("LC", "+1-758");
        COUNTRY_2PHONE.put("VC", "+1-784");
        COUNTRY_2PHONE.put("WS", "+685");
        COUNTRY_2PHONE.put("SM", "+378");
        COUNTRY_2PHONE.put("ST", "+239");
        COUNTRY_2PHONE.put("SA", "+966");
        COUNTRY_2PHONE.put("SN", "+221");
        COUNTRY_2PHONE.put("RS", "+381");
        COUNTRY_2PHONE.put("SC", "+248");
        COUNTRY_2PHONE.put("SL", "+232");
        COUNTRY_2PHONE.put("SG", "+65");
        COUNTRY_2PHONE.put("SK", "+421");
        COUNTRY_2PHONE.put("SI", "+386");
        COUNTRY_2PHONE.put("SB", "+677");
        COUNTRY_2PHONE.put("SO", "+252");
        COUNTRY_2PHONE.put("ZA", "+27");
        COUNTRY_2PHONE.put("ES", "+34");
        COUNTRY_2PHONE.put("LK", "+94");
        COUNTRY_2PHONE.put("SD", "+249");
        COUNTRY_2PHONE.put("SR", "+597");
        COUNTRY_2PHONE.put("SZ", "+268");
        COUNTRY_2PHONE.put("SE", "+46");
        COUNTRY_2PHONE.put("CH", "+41");
        COUNTRY_2PHONE.put("SY", "+963");
        COUNTRY_2PHONE.put("TJ", "+992");
        COUNTRY_2PHONE.put("TZ", "+255");
        COUNTRY_2PHONE.put("TH", "+66");
        COUNTRY_2PHONE.put("TL", "+670");
        COUNTRY_2PHONE.put("TG", "+228");
        COUNTRY_2PHONE.put("TO", "+676");
        COUNTRY_2PHONE.put("TT", "+1-868");
        COUNTRY_2PHONE.put("TN", "+216");
        COUNTRY_2PHONE.put("TR", "+90");
        COUNTRY_2PHONE.put("TM", "+993");
        COUNTRY_2PHONE.put("TV", "+688");
        COUNTRY_2PHONE.put("UG", "+256");
        COUNTRY_2PHONE.put("UA", "+380");
        COUNTRY_2PHONE.put("AE", "+971");
        COUNTRY_2PHONE.put("GB", "+44");
        COUNTRY_2PHONE.put("US", "+1");
        COUNTRY_2PHONE.put("UY", "+598");
        COUNTRY_2PHONE.put("UZ", "+998");
        COUNTRY_2PHONE.put("VU", "+678");
        COUNTRY_2PHONE.put("VA", "+379");
        COUNTRY_2PHONE.put("VE", "+58");
        COUNTRY_2PHONE.put("VN", "+84");
        COUNTRY_2PHONE.put("YE", "+967");
        COUNTRY_2PHONE.put("ZM", "+260");
        COUNTRY_2PHONE.put("ZW", "+263");
        COUNTRY_2PHONE.put("GE", "+995");
        COUNTRY_2PHONE.put("TW", "+886");
        COUNTRY_2PHONE.put("AZ", "+374-97");
        COUNTRY_2PHONE.put("CY", "+90-392");
        COUNTRY_2PHONE.put("MD", "+373-533");
        COUNTRY_2PHONE.put("SO", "+252");
        COUNTRY_2PHONE.put("GE", "+995");
        COUNTRY_2PHONE.put("CX", "+61");
        COUNTRY_2PHONE.put("CC", "+61");
        COUNTRY_2PHONE.put("NF", "+672");
        COUNTRY_2PHONE.put("NC", "+687");
        COUNTRY_2PHONE.put("PF", "+689");
        COUNTRY_2PHONE.put("YT", "+262");
        COUNTRY_2PHONE.put("GP", "+590");
        COUNTRY_2PHONE.put("GP", "+590");
        COUNTRY_2PHONE.put("PM", "+508");
        COUNTRY_2PHONE.put("WF", "+681");
        COUNTRY_2PHONE.put("CK", "+682");
        COUNTRY_2PHONE.put("NU", "+683");
        COUNTRY_2PHONE.put("TK", "+690");
        COUNTRY_2PHONE.put("GG", "+44");
        COUNTRY_2PHONE.put("IM", "+44");
        COUNTRY_2PHONE.put("JE", "+44");
        COUNTRY_2PHONE.put("AI", "+1-264");
        COUNTRY_2PHONE.put("BM", "+1-441");
        COUNTRY_2PHONE.put("IO", "+246");
        COUNTRY_2PHONE.put("", "+357");
        COUNTRY_2PHONE.put("VG", "+1-284");
        COUNTRY_2PHONE.put("KY", "+1-345");
        COUNTRY_2PHONE.put("FK", "+500");
        COUNTRY_2PHONE.put("GI", "+350");
        COUNTRY_2PHONE.put("MS", "+1-664");
        COUNTRY_2PHONE.put("SH", "+290");
        COUNTRY_2PHONE.put("TC", "+1-649");
        COUNTRY_2PHONE.put("MP", "+1-670");
        COUNTRY_2PHONE.put("PR", "+1-787and1-939");
        COUNTRY_2PHONE.put("AS", "+1-684");
        COUNTRY_2PHONE.put("GU", "+1-671");
        COUNTRY_2PHONE.put("VI", "+1-340");
        COUNTRY_2PHONE.put("HK", "+852");
        COUNTRY_2PHONE.put("MO", "+853");
        COUNTRY_2PHONE.put("FO", "+298");
        COUNTRY_2PHONE.put("GL", "+299");
        COUNTRY_2PHONE.put("GF", "+594");
        COUNTRY_2PHONE.put("GP", "+590");
        COUNTRY_2PHONE.put("MQ", "+596");
        COUNTRY_2PHONE.put("RE", "+262");
        COUNTRY_2PHONE.put("AX", "+358-18");
        COUNTRY_2PHONE.put("AW", "+297");
        COUNTRY_2PHONE.put("AN", "+599");
        COUNTRY_2PHONE.put("SJ", "+47");
        COUNTRY_2PHONE.put("AC", "+247");
        COUNTRY_2PHONE.put("TA", "+290");
        COUNTRY_2PHONE.put("CS", "+381");
        COUNTRY_2PHONE.put("PS", "+970");
        COUNTRY_2PHONE.put("EH", "+212");
    }

    private Iso2Phone() {
        throw new UnsupportedOperationException();
    }

    /**
     * Retrieves phone prefix based on the country code
     *
     * @param code Country code
     * @return Phone prefix
     */
    public static String getPhone(String code) {
        return COUNTRY_2PHONE.get(code.toUpperCase(Locale.getDefault()));
    }

    /**
     * Gets all data
     *
     * @return All country and phone prefixes available
     */
    public static Map<String, String> getAll() {
        return COUNTRY_2PHONE;
    }
}
