/**
 * Delivery configurations of an {@link com.didww.sdk.resource.SmsTrunk} (API 2026-10-22).
 *
 * <p>The configuration is sent as a whole object: null fields are left out of a request, and attributes
 * left out keep their stored values. On update the configuration type must match the stored one.
 *
 * <p>So the typed classes cannot clear a nullable attribute, such as the {@code body_type} and {@code body}
 * of an {@code http_in} configuration switched to {@code get}, or the {@code allowed_ips} of an
 * {@code http_out} one. To send explicit nulls, use a {@link GenericSmsTrunkConfiguration} of the same type,
 * whose null attribute values are sent as {@code null}:
 * <pre>{@code
 * ObjectNode attributes = JsonNodeFactory.instance.objectNode();
 * attributes.put("http_method", "get");
 * attributes.putNull("body_type");
 * attributes.putNull("body");
 * trunk.setConfiguration(new GenericSmsTrunkConfiguration("http_in", attributes));
 * }</pre>
 *
 * <p>An unknown configuration type is read as a {@link GenericSmsTrunkConfiguration} and written back
 * unchanged, numbers included. Changes made inside a configuration object are not tracked: call
 * {@code trunk.setConfiguration(configuration)} again to send it in the next update.
 */
package com.didww.sdk.resource.smstrunkconfiguration;
