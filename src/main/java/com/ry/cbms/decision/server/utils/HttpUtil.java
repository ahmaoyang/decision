package com.ry.cbms.decision.server.utils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by maoYang on 2019/5/12.
 */
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    public static <T> ResponseEntity<T> httpExchange(String url, HttpEntity<String> requestEntity, HttpMethod httpMethod, Class<T> responseType) {
        StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(m);

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 设置超时
       // requestFactory.setConnectTimeout(1000);
        //requestFactory.setReadTimeout(1000);
        RestTemplate rest = new RestTemplate(requestFactory);
        ResponseEntity<T> response;
        try {
            response = rest.exchange(url, httpMethod, requestEntity, responseType);
            return response;
        } catch (Exception e) {
            log.error("请求失败：url:{},httpMethod:{},requestEntity:{},error:{}", url, httpMethod, requestEntity, e);
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
//            return response;
        }
    }


    public static String httpPostForString(String url, String requestBody, String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Accept-Charset", "UTF-8");
        headers.add("Content-Type", "application/json;charset=UTF-8");
        if (StringUtils.isEmpty(token)) {
            headers.add("access_token", token);
        }
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = httpExchange(url, requestEntity, HttpMethod.POST, String.class);
        return response.getBody();
    }

    public static String httpGetForString(String executUrl) {
        StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(m);
        RestTemplate rest = new RestTemplate(converters);
        return rest.getForObject(executUrl, String.class);
    }

    public static String httpGetForString(String executUrl, Map<String, Object> param) {
        StringBuilder url = new StringBuilder(executUrl);
        url.append("?");
        for (Map.Entry<String, Object> entry : param.entrySet()) {
            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return httpGetForString(url.toString());
    }

    public static String httpPostWithForm(String url, Map<String, Object> params) throws IOException {
        List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            pairList.add(new BasicNameValuePair(param.getKey(), param.getValue().toString()));
        }
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));
        String respContent = null;
        HttpClient _httpClient = HttpClients.createDefault();
        HttpResponse resp = _httpClient.execute(httpPost);
        org.apache.http.HttpEntity he = resp.getEntity();
        respContent = EntityUtils.toString(he, "UTF-8");
        return respContent;
    }




}
