package com.minecraft.core.bukkit.api.shape.manager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ImgurUtil {

    private static final Pattern IMGUR_PAGE = Pattern.compile("(?:https?://)?(?:www\\.)?imgur\\.com/(?:gallery/|a/)?([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMGUR_DIRECT = Pattern.compile("(?:https?://)?i\\.imgur\\.com/([A-Za-z0-9]+)(?:\\.[a-zA-Z]+)?", Pattern.CASE_INSENSITIVE);

    private ImgurUtil() {
    }

    public static String normalize(String raw) {
        if (raw == null)
            return null;

        String url = raw.trim();
        if (url.isEmpty())
            return url;

        Matcher direct = IMGUR_DIRECT.matcher(url);
        if (direct.find())
            return "https://i.imgur.com/" + direct.group(1) + ".png";

        Matcher page = IMGUR_PAGE.matcher(url);
        if (page.find())
            return "https://i.imgur.com/" + page.group(1) + ".png";

        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "https://" + url;

        return url;
    }

    public static boolean looksLikeImageUrl(String url) {
        if (url == null || url.isEmpty())
            return false;
        String lower = url.toLowerCase();
        return lower.contains("imgur.com")
                || lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif");
    }
}
