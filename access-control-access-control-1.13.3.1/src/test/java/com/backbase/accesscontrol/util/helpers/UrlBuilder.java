package com.backbase.accesscontrol.util.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import joptsimple.internal.Strings;

public class UrlBuilder {

    private String url;
    private List<String> pathParams = new ArrayList<>();
    private List<String[]> queryParams = new ArrayList<>();

    public UrlBuilder(String url) {
        this.url = url;
    }

    public UrlBuilder addPathParameter(String param) {
        pathParams.add(param);
        return this;
    }

    public UrlBuilder addQueryParameter(String name, String value) {
        String[] pair = new String[]{name, value};
        queryParams.add(pair);
        return this;
    }

    public UrlBuilder addQueryParameter(String name, Collection<String> value) throws UnsupportedEncodingException {
        String items = Strings.join(value, ",");
        String[] pair = new String[]{name, URLEncoder.encode(items, StandardCharsets.UTF_8.toString())};
        queryParams.add(pair);
        return this;
    }

    private String getFirstParam() {
        if (pathParams.isEmpty()) {
            return "";
        }
        String param = pathParams.remove(0);
        return param;
    }

    private String createPath() {
        String[] parts = url.split("\\{(.*?)\\}");
        return Arrays.stream(parts).map(s -> s + getFirstParam()).collect(Collectors.joining());
    }

    private String createQuery() {
        String query = queryParams.stream().map(s -> "&".concat(s[0]).concat("=").concat(s[1]))
            .collect(Collectors.joining());
        if (query.isEmpty()) {
            return "";
        }
        return "?".concat(query.substring(1));
    }

    public String build() {
        return createPath() + createQuery();
    }

}
