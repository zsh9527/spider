package com.zsh.spider.config;

import lombok.AllArgsConstructor;
import okhttp3.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * OkHttpClient 配置
 */
@Configuration
@AllArgsConstructor
public class OkHttpClientConfig {

    private final HttpProp httpProp;
    private final Environment env;

    /**
     * 配置okHttpClient
     */
    @Bean(name = "okHttpClient")
    public OkHttpClient okHttpClient() {
        // 最大请求数
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(httpProp.getMaxRequest());
        dispatcher.setMaxRequestsPerHost(httpProp.getMaxPerHostRequest());
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (Arrays.stream(env.getActiveProfiles()).allMatch("dev"::equals)) {
            // 开发环境自动配置代理
            configProxy(builder);
        }
        configProxy(builder);
        return builder.retryOnConnectionFailure(true)
            .addInterceptor(new GzipInterceptor())
            .connectTimeout(httpProp.getConnectTimeOut(), TimeUnit.SECONDS)
            .writeTimeout(httpProp.getReadTimeOut(),TimeUnit.SECONDS)
            .readTimeout(httpProp.getWriteTimeOut(),TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(httpProp.getMaxConnection(), 5, TimeUnit.MINUTES))
            .dispatcher(dispatcher)
            .build();
    }

    /**
     * 调试使用, 配置代理
     */
    private void configProxy(OkHttpClient.Builder builder) {
        try (Socket ignored1 = new Socket("127.0.0.1", 1080)) {
            // 如果连接成功，说明该端口是打开的
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080));
            builder.proxy((proxy)).sslSocketFactory(sslSocketFactory(), x509TrustManager());
        } catch (Exception ignored) {

        }
    }

    private X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private SSLSocketFactory sslSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] {x509TrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * okhttpclient 拦截器
     */
    class GzipInterceptor implements Interceptor {

        /**
         * 自动添加请求头(接受编码内容为utf-8), 自动解压缩gzip数据
         */
        @Override
        public Response intercept(Chain chain) throws IOException {

            Request request = chain.request();
            request = request.newBuilder()
                .header("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36")
                .header("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,image/*,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9; charset=UTF-8")
                .header("Accept-Encoding", "gzip")
                .header("Accept-Language", "zh-CN,zh-TW;q=0.9,zh;q=0.8,en-US;q=0.7,en;q=0.6")
                .build();

            Response response = chain.proceed(request);
            if (response.isSuccessful()) {
                String encoding = response.header("Content-Encoding");
                if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                    GZIPInputStream gzipInputStream = new GZIPInputStream(response.body().byteStream());
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = gzipInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }
                    byteArrayOutputStream.close();
                    response = response.newBuilder()
                        .body(ResponseBody.create(byteArrayOutputStream.toByteArray(), response.body().contentType()))
                        .build();
                }
            }
            return response;
        }
    }
}
