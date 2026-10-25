package com.didww.examples;

import com.didww.sdk.DidwwClient;
import com.didww.sdk.exception.DidwwPricesException;
import com.didww.sdk.exception.PricesErrorKind;
import com.didww.sdk.prices.PriceLookup;
import com.didww.sdk.prices.PricesService;
import com.didww.sdk.prices.SmsInPrice;
import com.didww.sdk.prices.SmsOutPrice;
import com.didww.sdk.prices.VoiceInPrice;
import com.didww.sdk.prices.VoiceOutPrice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Looks up Voice IN, Voice OUT, SMS OUT and SMS IN prices (2026-10-22) for one number.
 *
 * Prices are served by separate hosts (api-vin, api-vout and api-sms); the SDK obtains a token for each of
 * them on first use. A read-scoped client ({@code DidwwClient.builder().scope(DidwwScope.READ)}) is enough
 * for prices. The account needs a price plan of the service, otherwise the call fails with a
 * DidwwPricesException of kind PRICE_PLAN_NOT_ASSIGNED.
 *
 * Usage: DIDWW_API_KEY=xxx ./gradlew runExample -PexampleClass=com.didww.examples.PricesLookupExample
 *            --args="[number]"
 */
public class PricesLookupExample {

    public static void main(String[] args) {
        DidwwClient client = ExampleClientFactory.fromEnv();
        String number = args.length > 0 ? args[0] : "18005550123";

        show("Voice IN", client.voiceInPrices(), filters("number", number));
        show("Voice IN, calls from the US", client.voiceInPrices(),
                filters("number", number, "src_country_iso", "US"));
        show("Voice OUT", client.voiceOutPrices(), filters("number", number));
        show("SMS OUT A2P", client.smsOutA2pPrices(), filters("dst_number", number));
        show("SMS OUT P2P, whole country", client.smsOutP2pPrices(), filters("dst_country_iso", "US"));
        show("SMS OUT OTP", client.smsOutOtpPrices(), filters("dst_number", number, "dst_country_iso", "US"));
        show("SMS IN", client.smsInPrices(), filters("dst_number", number));

        // Filters are checked before any request is sent: Voice OUT takes number only.
        try {
            client.voiceOutPrices().lookup(filters("number", number, "src_country_iso", "US"));
        } catch (IllegalArgumentException e) {
            System.out.println("\nRejected locally: " + e.getMessage());
        }
    }

    private static Map<String, String> filters(String... namesAndValues) {
        Map<String, String> filters = new LinkedHashMap<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            filters.put(namesAndValues[i], namesAndValues[i + 1]);
        }
        return filters;
    }

    private static <T> void show(String title, PricesService<T> prices, Map<String, String> filters) {
        System.out.println("\n=== " + title + " ===");
        PriceLookup<T> result;
        try {
            result = prices.lookup(filters);
        } catch (DidwwPricesException e) {
            if (e.getKind() != PricesErrorKind.PRICE_PLAN_NOT_ASSIGNED && e.getKind() != PricesErrorKind.PRICE_NOT_FOUND) {
                throw e;
            }
            System.out.println("  " + e.getKind() + ": " + e.getMessage());
            return;
        }

        System.out.println(result.getTotalRecords() + " matching price(s)");
        for (T row : result.getRows()) {
            System.out.println("  " + describe(row));
        }
    }

    // Prices and fees are BigDecimal values in USD; missing values print as "null".
    private static String describe(Object row) {
        if (row instanceof VoiceInPrice) {
            VoiceInPrice p = (VoiceInPrice) row;
            return p.getCountry() + " " + p.getDestination() + " " + p.getSourceType() + " from " + p.getSrcCountryName()
                    + ": rate " + p.getRate() + ", connection fee " + p.getConnectionFee()
                    + ", intervals " + p.getInitialInterval() + "/" + p.getNextInterval()
                    + ", reject calls " + p.getRejectCalls();
        }
        if (row instanceof VoiceOutPrice) {
            VoiceOutPrice p = (VoiceOutPrice) row;
            return p.getCategory() + " " + p.getCountry() + " " + p.getNetwork() + " " + p.getPrefix()
                    + ": rate " + p.getRate() + ", connection fee " + p.getConnectionFee()
                    + ", increments " + p.getInitialIncrement() + "/" + p.getRecurringIncrement()
                    + ", next rate " + p.getNextRate() + " from " + p.getEffectiveAt();
        }
        if (row instanceof SmsOutPrice) {
            SmsOutPrice p = (SmsOutPrice) row;
            return p.getCountry() + " " + p.getMccName() + " " + p.getMncName()
                    + " (MCC " + p.getMcc() + ", MNC " + p.getMnc() + ") "
                    + p.getSourcePrefix() + " -> " + p.getDestinationPrefix()
                    + ": price " + p.getPrice() + ", reject SMS " + p.getRejectSms();
        }
        if (row instanceof SmsInPrice) {
            SmsInPrice p = (SmsInPrice) row;
            return p.getCountry() + " " + p.getSourcePrefix() + " -> " + p.getDestinationPrefix()
                    + ": price " + p.getPrice();
        }
        return String.valueOf(row);
    }
}
