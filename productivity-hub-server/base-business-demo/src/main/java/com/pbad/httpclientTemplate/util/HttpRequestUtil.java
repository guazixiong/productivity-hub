package com.pbad.httpclientTemplate.util;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestUtil {

    /**
     * <p>执行一个HTTP POST请求</p>
     *
     * @param url     请求的URL地址
     * @param params  请求的查询参数,可以为null
     * @param charset 字符集
     * @param data    xml格式的数据
     * @return 返回请求响应的HTML
     * @throws Exception
     */
    public static String getDoPostResponse(String url, Map<String, String> params, String charset, String data, int timeout) throws Exception {

//        HttpClient client = new HttpClient();
//        HttpConnectionManagerParams httpManager = client.getHttpConnectionManager().getParams();
//        httpManager.setConnectionTimeout(timeout);
//        httpManager.setSoTimeout(timeout);
//        DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(2, false);
//        client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
//        client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
//
//        PostMethod method = new PostMethod(url);
//        StringBuffer sb = new StringBuffer();
//
//        if (data != null) {
//            RequestEntity entity = new StringRequestEntity(data, "application/x-www-form-urlencoded", "UTF-8");
//            method.setRequestEntity(entity);
//        }
//        if (params != null) {
//            //设置Http Post参数
//            NameValuePair[] da = null;
//            da = new NameValuePair[params.size()];
//            int i = 0;
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                da[i] = new NameValuePair(entry.getKey(), entry.getValue());
//                i++;
//            }
//            i = 0;
//            System.out.println("data=" + da.toString());
//            method.setRequestBody(da);
//        }
//        try {
//            client.executeMethod(method);
//            if (method.getStatusCode() == HttpStatus.SC_OK) {
//                BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
//                String str = "";
//                while ((str = reader.readLine()) != null) {
//                    sb.append(str);
//                }
//            }
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            method.releaseConnection();
//        }
//        return sb.toString();

        return null;
    }

    public static String httpPostWithjson(String reqUrl, String jsonStr) throws IOException {
        String resp = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(reqUrl); //url地址
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonStr.getBytes("UTF-8"));
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String lines;
                StringBuffer sbf = new StringBuffer();
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes(), "utf-8");
                    sbf.append(lines);
                }
                System.out.println("返回来的报文：" + sbf.toString());
                resp = sbf.toString();
                return resp;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 图片上传接口，未校验文件名称，商户进件需要用到
     *
     * @param url
     * @param paramMap
     * @param file
     * @return
     * @throws Exception
     */
    public static String sendPostFile(String url, Map<String, String> paramMap, File file) throws Exception {
//        HttpPost httpPost = new HttpPost(url);
//        //如果返回403 增加一下代码模拟浏览器
//        //httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:82.0) Gecko/20100101 Firefox/82.0");;
//
////		CloseableHttpClient client = HttpClientBuilder.create().build();
//        CloseableHttpClient client = HttpClients.createDefault();
//        MultipartEntity entity = new MultipartEntity();
//        entity.addPart("file", new FileBody(file)); // 添加文件部分
//        // 添加其他参数
//        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
//            entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
//        }
//        httpPost.setEntity(entity);
//        HttpResponse httpResponse = client.execute(httpPost);
//        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            System.out.println(httpResponse.getStatusLine().getStatusCode());
//        }
//        HttpEntity resEntity = httpResponse.getEntity();
//        return resEntity == null ? "" : EntityUtils.toString(resEntity, "utf-8");
        return null;
    }


    /**
     * 文件上传 校验了文件名称，支付宝实名认证文件上传用到
     *
     * @param url
     * @param paramMap
     * @param file
     * @param fileName
     * @return
     * @throws Exception
     */
    public static String sendPostFileAndName(String url, Map<String, String> paramMap, File file, String fileName) throws Exception {
//        if (StringUtils.isEmpty(fileName)) {
//            throw new Exception("文件名称不能为空");
//        }
//        HttpPost httpPost = new HttpPost(url);
//        //如果返回403 增加一下代码模拟浏览器
//        //httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:82.0) Gecko/20100101 Firefox/82.0");;
//
//        CloseableHttpClient client = HttpClients.createDefault();
//
//        MultipartEntity entity = new MultipartEntity();
//        entity.addPart("file", new FileBody(file, ContentType.DEFAULT_BINARY, fileName)); // 添加文件部分
//
//        // 添加其他参数
//        for (Map.Entry<String, String> entry : paramMap.entrySet()) {
//            entity.addPart(entry.getKey(), new StringBody(entry.getValue(), Charset.forName("UTF-8")));
//        }
//
//        httpPost.setEntity(entity);
//
//        HttpResponse httpResponse = client.execute(httpPost);
//        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            System.out.println(httpResponse.getStatusLine().getStatusCode());
//        }
//
//        HttpEntity resEntity = httpResponse.getEntity();
//        return resEntity == null ? "" : EntityUtils.toString(resEntity, "utf-8");
        return null;
    }

    public static void main(String[] args) {
        try {
            String url = "http://example.com/upload";
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("param1", "value1");
            paramMap.put("param2", "value2");
            File file = new File("path/to/your/file.txt");
            String fileName = "example.txt";

            String response = sendPostFileAndName(url, paramMap, file, fileName);
            System.out.println("Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
