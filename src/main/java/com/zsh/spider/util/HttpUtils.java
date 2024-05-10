package com.zsh.spider.util;

import com.zsh.spider.pojo.dto.SearchDTO;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * url解析工具类
 */
public class HttpUtils {

    private static Pattern httpPattern = getUrlPattern(1);
    private static Pattern[] datePatterns = {
        Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}"),
        Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日")
    };

    /**
     * 获取子级link
     */
    public static List<String> getChildLink(Document document, SearchDTO search, String url) {
        String parent = getUrlPrefix(url);
        Elements linkElements = document.getElementsByTag("a");
        Set<String> hrefs = new HashSet<>();
        Matcher matcher = httpPattern.matcher(document.text());
        while (matcher.find()) {
            hrefs.add(matcher.group());
        }
        String finalParent = parent;
        var htmlLinks = linkElements.stream()
            .map(
                obj -> obj.attr("href")
            ).map(
                obj -> {
                    if (obj.startsWith("//")) {
                        return "http:" + obj;
                    } else if (obj.startsWith("/")) {
                        return finalParent + obj;
                    } else {
                        return obj;
                    }
                }
            ).map(obj -> {
                Matcher matcher2 = httpPattern.matcher(obj);
                if (matcher2.find()) {
                    return matcher2.group();
                } else {
                    return null;
                }
            })
            .toList();
        hrefs.addAll(htmlLinks);
        return getLegalHref(search, hrefs);
    }

    /**
     * 获取子级link -- 只获取a标签
     */
    public static List<String> getChildLinkByA(Document document, SearchDTO search, String url) {
        String parent = getUrlPrefix(url);
        Elements linkElements = document.getElementsByTag("a");
        String finalParent = parent;
        var hrefs = linkElements.stream()
            .filter(
                // 标签必须和搜索内容相关
                obj -> SimilarUtils.isRelatedLink(obj.text() + obj.getElementsByAttribute("title"), search)
            )
            .map(
                obj -> obj.attr("href")
            ).map(
                obj -> {
                    if (obj.startsWith("//")) {
                        return "http:" + obj;
                    } else if (obj.startsWith("/")) {
                        return finalParent + obj;
                    } else {
                        return obj;
                    }
                }
            ).map(obj -> {
                Matcher matcher2 = httpPattern.matcher(obj);
                if (matcher2.find()) {
                    return matcher2.group();
                } else {
                    return null;
                }
            })
            .collect(Collectors.toSet());
        return getLegalHref(search, hrefs);
    }

    @NotNull
    private static String getUrlPrefix(String url) {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        String parent = scheme + "://" + host;
        // 如果端口未明确指定，则使用默认端口
        if (uri.getPort() != -1) {
            parent += ":" + uri.getPort();
        }
        return parent;
    }

    /**
     * 获取合法的链接
     */
    @NotNull
    private static List<String> getLegalHref(SearchDTO search, Set<String> hrefs) {
        return hrefs.stream()
            .filter(obj -> !StringUtils.isEmpty(obj))
            .filter(obj -> search.getLimitSite() == null ||
                obj.contains(search.getLimitSite()))
            .filter(obj ->
                search.getIgnoreSpiderSite().stream()
                    .noneMatch(ignoreSite -> obj.contains(ignoreSite) &&
                        obj.indexOf(ignoreSite) < 20 + ignoreSite.length())
            )
            .toList();
    }

    /**
     * 解析重定向URL
     *
     * @return 无重定向URL返回null
     */
    public static String parseRedirectUrl(Document document) {
        Elements metaTags = document.select("meta[http-equiv=refresh]");
        for (Element meta : metaTags) {
            var redirectContent = meta.attr("content");
            var arr = redirectContent.split(";", 2);
            if (arr.length == 2 && Integer.parseInt(arr[0].trim()) < 3) {
                var urlContent = arr[1].replace("'", " ").replace("\"", " ");
                Matcher matcher = httpPattern.matcher(urlContent);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return null;
    }

    /**
     * 解析时间
     */
    public static Instant parseTime(String content) {
        for (Pattern pattern : datePatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return parseDateToInstant(matcher.group());
            }
        }
        return null;
    }

    private static Instant parseDateToInstant(String dateStr) {
        // 定义多个可能的日期格式
        String[] dateFormats = {"yyyy-M-d", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy年M月d日"};

        // 尝试使用多个日期格式进行解析
        for (String dateFormat : dateFormats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
                LocalDate localDate = LocalDate.parse(dateStr, formatter);
                return localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException e) {
                // 如果解析失败，尝试下一个格式
            }
        }
        return null;
    }

    /**
     * regex1 和 regex2数据都可以
     */
    public static Pattern getUrlPattern(int type) {
        String regex1 = "http(s?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:(0-9)*)*(\\/?)([a-zA-Z0-9\\-\\.\\?\\,\\'\\/\\\\&%\\+\\$#_=]*)?";
        String regex2 = "^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        if (type == 1) {
            return Pattern.compile(regex1, Pattern.DOTALL);
        } else {
            return Pattern.compile(regex2, Pattern.DOTALL);
        }
    }
}