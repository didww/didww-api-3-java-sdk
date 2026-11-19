package com.didww.sdk.prices;

import com.didww.sdk.auth.Audience;
import com.didww.sdk.converter.DidwwResourceConverter;
import com.github.jasminb.jsonapi.ResourceConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A price list of a prices service: its paths below {@code /v3}, the audience
 * of its tokens, its lookup filters and its row type. Obtain the services with
 * the price accessors of {@link com.didww.sdk.DidwwClient}.
 *
 * @param <T> the row type of the price list
 */
public final class PriceList<T> {
    /** Voice IN prices. Lookup filters: {@code number} (required), {@code src_country_iso}. */
    public static final PriceList<VoiceInPrice> VOICE_IN = new PriceList<>("", Audience.VOICE_IN,
            list("number"), list("src_country_iso"), list(), VoiceInPrice.class);
    /** Voice OUT prices. Lookup filters: {@code number} (required). */
    public static final PriceList<VoiceOutPrice> VOICE_OUT = new PriceList<>("", Audience.VOICE_OUT,
            list("number"), list(), list(), VoiceOutPrice.class);
    /** SMS OUT A2P prices. Lookup filters: one of {@code dst_number} and {@code dst_country_iso}, or both. */
    public static final PriceList<SmsOutPrice> SMS_OUT_A2P = sms("sms_out/a2p/", SmsOutPrice.class);
    /** SMS OUT P2P prices. Lookup filters: one of {@code dst_number} and {@code dst_country_iso}, or both. */
    public static final PriceList<SmsOutPrice> SMS_OUT_P2P = sms("sms_out/p2p/", SmsOutPrice.class);
    /** SMS OUT OTP prices. Lookup filters: one of {@code dst_number} and {@code dst_country_iso}, or both. */
    public static final PriceList<SmsOutPrice> SMS_OUT_OTP = sms("sms_out/otp/", SmsOutPrice.class);
    /** SMS IN prices. Lookup filters: one of {@code dst_number} and {@code dst_country_iso}, or both. */
    public static final PriceList<SmsInPrice> SMS_IN = sms("sms_in/", SmsInPrice.class);

    private static final String FILE_NAME = "prices.csv.gz";
    private static final String LOOKUP_PATH = "lookup/prices";

    private final String prefix;
    private final Audience audience;
    private final List<String> required;
    private final List<String> optional;
    private final List<String> anyOf;
    private final Class<T> rowClass;
    // One converter per row class: every row type is "prices", and a converter
    // maps a type to the last class registered for it.
    private final ResourceConverter converter;

    private PriceList(String prefix, Audience audience, List<String> required, List<String> optional,
                      List<String> anyOf, Class<T> rowClass) {
        this.prefix = prefix;
        this.audience = audience;
        this.required = required;
        this.optional = optional;
        this.anyOf = anyOf;
        this.rowClass = rowClass;
        this.converter = new ResourceConverter(DidwwResourceConverter.createObjectMapper(), rowClass);
    }

    private static <R> PriceList<R> sms(String prefix, Class<R> rowClass) {
        return new PriceList<>(prefix, Audience.SMS, list(), list(), list("dst_number", "dst_country_iso"), rowClass);
    }

    private static List<String> list(String... names) {
        return Collections.unmodifiableList(Arrays.asList(names));
    }

    /**
     * Returns the path of the price file below {@code /v3}, for example
     * {@code sms_out/a2p/prices.csv.gz}.
     *
     * @return the file path
     */
    public String getFilePath() {
        return prefix + FILE_NAME;
    }

    /**
     * Returns the path of the lookup below {@code /v3}, for example
     * {@code sms_out/a2p/lookup/prices}.
     *
     * @return the lookup path
     */
    public String getLookupPath() {
        return prefix + LOOKUP_PATH;
    }

    Audience getAudience() {
        return audience;
    }

    List<String> getRequired() {
        return required;
    }

    List<String> getAnyOf() {
        return anyOf;
    }

    List<String> allowed() {
        List<String> allowed = new ArrayList<>(required);
        allowed.addAll(optional);
        allowed.addAll(anyOf);
        return allowed;
    }

    Class<T> getRowClass() {
        return rowClass;
    }

    ResourceConverter getConverter() {
        return converter;
    }

    @Override
    public String toString() {
        return "PriceList{" + getLookupPath() + ", audience=" + audience + "}";
    }
}
