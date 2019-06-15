package ch.so.arp.nplvalidator.geoserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPUtils {
    private static final Logger log = LoggerFactory.getLogger(HTTPUtils.class);

    /**
     * PUTs a String representing an XML document to the given URL. <BR>
     * Basic auth is used if both username and pw are not null.
     * 
     * @param url      The URL where to connect to.
     * @param content  The XML content to be sent as a String.
     * @param username Basic auth credential. No basic auth if null.
     * @param pw       Basic auth credential. No basic auth if null.
     * @return The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public static String putXml(String url, String content, String username, String pw) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, "application/xml");
        return put(url, content, headers, username, pw);
    }

    /**
     * PUTs a String to the given URL. <BR>
     * Basic auth is used if both username and pw are not null.
     * 
     * @param url      The URL where to connect to.
     * @param content  The content to be sent as a String.
     * @param headers  The header to be sent.
     * @param username Basic auth credential. No basic auth if null.
     * @param pw       Basic auth credential. No basic auth if null.
     * @return The HTTP response as a String if the HTTP response code was 200 (OK).
     * @throws MalformedURLException
     * @return the HTTP response or <TT>null</TT> on errors.
     */
    public static String put(String url, String content, Map<String, String> headers, String username, String pw) {
        try {
            HttpPut httpPut = new HttpPut(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                httpPut.setHeader(header.getKey(), header.getValue());
            }
            return send(httpPut, new StringEntity(content), username, pw);
        } catch (UnsupportedEncodingException ex) {
            log.error("Cannot PUT " + url, ex);
            return null;
        }
    }

    /**
     * Send an HTTP request (PUT or POST) to a server. <BR>
     * Basic auth is used if both username and pw are not null.
     * <P>
     * Only
     * <UL>
     * <LI>200: OK</LI>
     * <LI>201: ACCEPTED</LI>
     * <LI>202: CREATED</LI>
     * </UL>
     * are accepted as successful codes; in these cases the response string will be
     * returned.
     * 
     * @return the HTTP response or <TT>null</TT> on errors.
     * @throws IOException
     * @throws ClientProtocolException
     */
    private static String send(HttpEntityEnclosingRequestBase httpMethod, StringEntity requestEntity, String username,
            String pw) {
        int timeout = 5;
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout * 1000)
            .setConnectionRequestTimeout(timeout * 1000)
            .setSocketTimeout(timeout * 1000).build();

        try {
            CloseableHttpClient httpclient;
            if (username != null && pw != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(httpMethod.getURI().getHost(), httpMethod.getURI().getPort()), 
                    new UsernamePasswordCredentials(username, pw));
                httpclient = HttpClients.custom().setDefaultRequestConfig(config).setDefaultCredentialsProvider(credentialsProvider).build();
            } else {
                httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
            } 
    
            if (requestEntity != null) {
                httpMethod.setEntity(requestEntity);
            }
            
            CloseableHttpResponse response = httpclient.execute(httpMethod);
            int status = response.getStatusLine().getStatusCode();
    
            switch (status) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_CREATED:
                case HttpURLConnection.HTTP_ACCEPTED:


            }
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            
            // ResponseHandler<String> responseHandler = response -> {
            //     int status = response.getStatusLine().getStatusCode();
            //     if (status >= 200 && status < 300) {
            //         HttpEntity entity = response.getEntity();
            //         return entity != null ? EntityUtils.toString(entity) : null;
            //     } else {
            //         throw new ClientProtocolException("Unexpected response status: " + status);
            //     }
            // };    
        } catch (ClientProtocolException e) {
            log.error("Couldn't connect to [" + httpMethod.getURI().toString() + "]");
            return null;
        } catch (IOException e) {
            log.error("Couldn't connect to [" + httpMethod.getURI().toString() + "]");
            return null;
        }

        return null;

    }



}
