package com.sfdc.http.client;

import com.ning.http.client.Cookie;
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author psrinivasan
 *         Date: 8/28/12
 *         Time: 6:29 PM
 */
public class NingResponse implements com.ning.http.client.Response {

    private static final Logger LOGGER = LoggerFactory.getLogger(NingResponse.class);
    private com.ning.http.client.Response response;

    public NingResponse(com.ning.http.client.Response response) {
        this.response = response;
    }

    @Override
    public int getStatusCode() {
        return response.getStatusCode();
    }

    @Override
    public String getStatusText() {
        return response.getStatusText();
    }

    @Override
    public byte[] getResponseBodyAsBytes() throws IOException {
        return response.getResponseBodyAsBytes();
    }

    @Override
    public InputStream getResponseBodyAsStream() throws IOException {
        return response.getResponseBodyAsStream();
    }

    @Override
    public String getResponseBodyExcerpt(int i, String s) throws IOException {
        return response.getResponseBodyExcerpt(i, s);
    }

    @Override
    public String getResponseBody(String s) throws IOException {
        return response.getResponseBody(s);
    }

    @Override
    public String getResponseBodyExcerpt(int i) throws IOException {
        return response.getResponseBodyExcerpt(i);
    }

    @Override
    public String getResponseBody() throws IOException {
        return response.getResponseBody();
    }

    @Override
    public URI getUri() throws MalformedURLException {
        return response.getUri();
    }

    @Override
    public String getContentType() {
        return response.getContentType();
    }

    @Override
    public String getHeader(String s) {
        return response.getHeader(s);
    }

    @Override
    public List<String> getHeaders(String s) {
        return response.getHeaders(s);
    }

    @Override
    public FluentCaseInsensitiveStringsMap getHeaders() {
        return response.getHeaders();
    }

    @Override
    public boolean isRedirected() {
        return response.isRedirected();
    }

    @Override
    public List<Cookie> getCookies() {
        return response.getCookies();
    }

    @Override
    public boolean hasResponseStatus() {
        return response.hasResponseStatus();
    }

    @Override
    public boolean hasResponseHeaders() {
        return response.hasResponseHeaders();
    }

    @Override
    public boolean hasResponseBody() {
        return response.hasResponseBody();
    }

    public String getClientId() throws Exception {
        return getBayeuxTokenValue("clientId");
    }

    public ArrayList<String> getChannels() throws Exception {
        return getBayeuxTokenValueAllMatches("channel");
    }

    /**
     * This will return a value only if the response is a valid subscribe call, else it will throw an exception.
     *
     * @return
     * @throws Exception
     */
    public String getSubscription() throws Exception {
        return getBayeuxTokenValue("subscription");
    }

    public boolean getBayeuxSuccessResponseField() throws Exception {
        return Boolean.parseBoolean(getBayeuxTokenValue("successful"));

    }

    public String getBayeuxError() throws Exception {
        return getBayeuxTokenValue("error");
    }

    /**
     * Looks for strings like clientId in JSON arrays like
     * [{"channel":"/meta/connect","clientId":"1ljvke3c5bpf3nv818c1ayqzg","advice":{"reconnect":"retry","interval":0,"timeout":110000},"successful":true}]
     *
     * @param rootNode
     * @param searchString
     * @return This method returns only the first match!
     */
    public String findTokeninJSonArray(JsonNode rootNode, String searchString) throws Exception {
        for (int i = 0; i < rootNode.size(); i++) {
            Iterator<String> itr = rootNode.get(i).getFieldNames();
            while (itr.hasNext()) {
                String field = itr.next();
                //System.out.println("field: " + field);
                if (field.equalsIgnoreCase(searchString)) {
                    return (rootNode.get(i).path(searchString).asText());
                }
            }
        }
        throw new Exception("could not find(or parse) " + searchString + " field in Bayeux response.  Response is " + rootNode.asText());
    }

    public ArrayList<String> findTokeninJSonArrayAllMatches(JsonNode rootNode, String searchString) throws Exception {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < rootNode.size(); i++) {
            Iterator<String> itr = rootNode.get(i).getFieldNames();
            while (itr.hasNext()) {
                String field = itr.next();
                //System.out.println("field: " + field);
                //System.out.println(" is value field? : " + rootNode.get(i).path(field).isValueNode());
                //System.out.println(" field as text: " + rootNode.get(i).path(field).asText());
                //System.out.println(" field as ... : " + rootNode.get(i).path(field).toString());
                if (field.equalsIgnoreCase(searchString)) {
                    list.add(rootNode.get(i).path(field).asText());
                }
            }
        }
        if (list.size() == 0) {
            throw new Exception("could not find(or parse) " + searchString + " field in Bayeux response.  Response is " + rootNode.asText());
        }
        return list;
    }

    /*
     *  findTokeninJSonArrayAllNodes, findTokeninJSonArrayAllMatches, and findTokeninJSonArray only look through the first level
     *  nodes.  There seems to be no point in going (recursively) through the entire tree given the nature of the bayeux json responses.
    */
    public ArrayList<JsonNode> findTokeninJSonArrayAllNodes(JsonNode rootNode, String searchString) throws Exception {
        ArrayList<JsonNode> list = new ArrayList<JsonNode>();
        for (int i = 0; i < rootNode.size(); i++) {
            Iterator<String> itr = rootNode.get(i).getFieldNames();
            while (itr.hasNext()) {
                String field = itr.next();
                //System.out.println("field: " + field);
                //System.out.println(" is value field? : " + rootNode.get(i).path(field).isValueNode());
                //System.out.println(" field as text: " + rootNode.get(i).path(field).asText());
                //System.out.println(" field as ... : " + rootNode.get(i).path(field).toString());
                if (field.equalsIgnoreCase(searchString)) {
                    list.add(rootNode.get(i).path(field));
                }
            }
        }
        if (list.size() == 0) {
            throw new Exception("could not find(or parse) " + searchString + " field in Bayeux response.  Response is " + rootNode.asText());
        }
        return list;
    }

    public String getBayeuxTokenValue(String token) throws Exception {
        String responseBody = null;
        try {
            responseBody = response.getResponseBody();
            if ((responseBody == null) || (responseBody == "")) {
                LOGGER.error("Received Empty Response Body!");
                throw new Exception("Received Empty Response Body");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return findTokeninJSonArray(rootNode, token);
    }

    public ArrayList<String> getBayeuxTokenValueAllMatches(String token) throws Exception {
        String responseBody = null;
        try {
            responseBody = response.getResponseBody();
            if ((responseBody == null) || (responseBody == "")) {
                LOGGER.error("Received Empty Response Body!");
                throw new Exception("Received Empty Response Body");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return findTokeninJSonArrayAllMatches(rootNode, token);
    }

    public HashMap<String, String> parseEventData(String data) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        System.out.println(data);
        try {
            node = mapper.readTree(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (int i = 0; i < node.size(); i++) {
//            Iterator<String> itr = node.get(i).getFieldNames();
//            while (itr.hasNext()) {
//                System.out.println("array[" + i + "] field: " + node.get(i).path(itr.next()).toString());
//            }
//
//
//        }
        return null;
    }
}
