package com.ry.cbms.decision.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;


/**
 * @Author maoYang
 * @Date 2019/5/27 15:32
 * @Description TODO
 */
@Slf4j
public class HttpUtil2 {


    /**
     * <p>发送GET请求
     *
     * @param url GET请求地址
     * @return 与当前请求对应的响应内容字节数组
     */
    public static byte[] doGet(String url) {

        return doGet(url, null, null, 0);
    }

    /**
     * <p>发送GET请求
     *
     * @param url       GET请求地址
     * @param headerMap GET请求头参数容器
     * @return 与当前请求对应的响应内容字节数组
     */
    public static byte[] doGet(String url, Map headerMap) {

        return doGet(url, headerMap, null, 0);
    }

    /**
     * <p>发送GET请求
     *
     * @param url       GET请求地址
     * @param proxyUrl  代理服务器地址
     * @param proxyPort 代理服务器端口号
     * @return 与当前请求对应的响应内容字节数组
     */
    public static byte[] doGet(String url, String proxyUrl, int proxyPort) {

        return doGet(url, null, proxyUrl, proxyPort);
    }

    /**
     * <p>发送GET请求
     *
     * @param url       GET请求地址
     * @param headerMap GET请求头参数容器
     * @param proxyUrl  代理服务器地址
     * @param proxyPort 代理服务器端口号
     * @return 与当前请求对应的响应内容字节数组
     */
    public static byte[] doGet(String url, Map headerMap, String proxyUrl, int proxyPort) {

        byte[] content = null;
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);

        if (headerMap != null) {

            //头部请求信息
            if (headerMap != null) {

                Iterator iterator = headerMap.entrySet().iterator();
                while (iterator.hasNext()) {

                    Map.Entry entry = (Map.Entry) iterator.next();
                    getMethod.addRequestHeader(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }

        if (!StringUtils.isEmpty(proxyUrl)) {

            httpClient.getHostConfiguration().setProxy(proxyUrl, proxyPort);
        }

        //设置成了默认的恢复策略，在发生异常时候将自动重试3次，在这里你也可以设置成自定义的恢复策略
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 10000);
        //postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER , new DefaultHttpMethodRetryHandler());
        InputStream inputStream = null;
        try {

            if (httpClient.executeMethod(getMethod) == HttpStatus.SC_OK) {
                //读取内容
                inputStream = getMethod.getResponseBodyAsStream();
                content = IOUtils.toByteArray(inputStream);
            } else {
                log.error("Method failed: {}", getMethod.getStatusLine());
            }
        } catch (IOException ex) {
            log.error(ex.toString());
        } finally {
            IOUtils.closeQuietly(inputStream);
            getMethod.releaseConnection();
        }
        return content;
    }

    /**
     *
     * @param url
     * @param token
     * @param nameValuePairList
     * @return
     */
    public static JSONObject doGet(String url, String token, List<NameValuePair> nameValuePairList) {
        // 获取连接客户端工具
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String entityStr = null;
        CloseableHttpResponse response = null;

        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            if (null != nameValuePairList && nameValuePairList.size() > 0) {
                uriBuilder.setParameters(nameValuePairList);
            }
            // 根据带参数的URI对象构建GET请求对象
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            // 浏览器表示
            httpGet.addHeader("access_token", token);
            // 传输的类型
            // httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
            // 执行请求
            response = httpClient.execute(httpGet);
            // 获得响应的实体对象
            HttpEntity entity = response.getEntity();
            // 使用Apache提供的工具类进行转换成字符串
            entityStr = EntityUtils.toString(entity, "UTF-8");
        } catch (URISyntaxException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        if(null==entityStr){
            return null;
        }
        return JSONObject.parseObject(entityStr);
    }



    /**
     *
     * @param url
     * @param token
     * @return
     */
    public static JSONObject doGetFor(String url, String token) {
        // 获取连接客户端工具
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String entityStr = null;
        CloseableHttpResponse response = null;

        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            // 根据带参数的URI对象构建GET请求对象
            HttpGet httpGet = new HttpGet(uriBuilder.build());

            // 浏览器表示
            httpGet.addHeader("token", token);
            // 传输的类型
            // httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
            // 执行请求
            response = httpClient.execute(httpGet);
            // 获得响应的实体对象
            HttpEntity entity = response.getEntity();
            // 使用Apache提供的工具类进行转换成字符串
            entityStr = EntityUtils.toString(entity, "UTF-8");
        } catch (URISyntaxException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        if(null==entityStr){
            return null;
        }
        return JSONObject.parseObject(entityStr);
    }
}
