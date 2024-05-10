package com.zsh.spider.client;

import com.zsh.spider.pojo.dto.SpiderDTO;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 代理client
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpiderClient {

    private final OkHttpClient okHttpClient;

    /**
     * get方式获取内容
     */
    @Retry(name = "retry-backend")
    @SneakyThrows
    public String getContent(String url, String cookie) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        requestBuilder.header("Pragma", "no-cache");
        requestBuilder.header("Cookie", cookie);
        requestBuilder.get();
        Response response = requestContent(requestBuilder.build());
        return decodeContent(response);
    }

    /**
     * spider获取内容
     */
    @SneakyThrows
    public String getSpiderContent(SpiderDTO dto) {
        Request.Builder requestBuilder = new Request.Builder().url(dto.getUrl());
        requestBuilder.header("Pragma", "no-cache");
        requestBuilder.get();
        Response response = requestContent(requestBuilder.build());
        String realUrl = response.request().url().toString();
        if (!dto.getUrl().equals(realUrl)) {
            // url不相等, 修改url为重定向url
            dto.setUrl(realUrl);
        }
        return decodeContent(response);
    }

    @SneakyThrows
    private String decodeContent(Response response) {
        byte[] bytes = response.body().bytes();
        // 使用http响应头编码解析
        Charset charset = getCharsetFromHeader(response);
        if (charset != null) {
            return new String(bytes, charset);
        }
        String content = new String(bytes, Charset.defaultCharset());
        // 字符串可能为乱码
        Document document = Jsoup.parse(content);
        // 选择所有的meta标签
        Elements metaTags = document.select("meta");
        Charset realCharset = getCharsetFromDocument(metaTags);
        if (realCharset.name().equals(Charset.defaultCharset().name())) {
            return content;
        }
        return new String(bytes, realCharset);
    }

    /**
     * 从document中获取真实编码,
     * 未获取到则返回 UTF-8
     */
    private Charset getCharsetFromDocument(Elements metaTags) {
        for (Element metaTag : metaTags) {
            String charsetAttr = metaTag.attr("charset");
            String contentAttr = metaTag.attr("content");
            try {
                if (!StringUtils.isBlank(charsetAttr) && Charset.isSupported(charsetAttr)) {
                    return Charset.forName(charsetAttr);
                }
                int index = contentAttr.indexOf("charset=") + 8;
                // 防止越界
                index = index > contentAttr.length() -1 ? 0 : index;
                String charsetStr = contentAttr.substring(index).trim();
                return Charset.forName(charsetStr);
            } catch (Exception ignored) {

            }
        }
        return Charset.forName("utf-8");
        //return Charset.forName("gb2312");
    }

    @Nullable
    private Charset getCharsetFromHeader(Response response) {
        String contentType = response.header("Content-Type");
        String[] contentTypeParts = contentType.split(";");
        for (String part : contentTypeParts) {
            part = part.trim();
            if (part.startsWith("charset=")) {
                String charsetStr = part.substring("charset=".length());
                try {
                    return Charset.forName(charsetStr);
                } catch (Exception ignored) {

                }
            }
        }
        return null;
    }

    private Response requestContent(Request request) throws IOException {
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException(response.toString());
        }
        return response;
    }
}
