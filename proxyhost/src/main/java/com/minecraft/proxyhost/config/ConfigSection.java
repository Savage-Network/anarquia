package com.minecraft.proxyhost.config;

import java.util.Map;

/**
 * Wrapper minimalista sobre {@link Map} para acesso tipado a config carregada
 * via SnakeYAML. Suporta paths em ponto ({@code "database.host"}).
 */
public final class ConfigSection {

    private final Map<String, Object> root;

    public ConfigSection(Map<String, Object> root) {
        this.root = root;
    }

    @SuppressWarnings("unchecked")
    private Object resolve(String path) {
        String[] parts = path.split("\\.");
        Object cur = root;
        for (String part : parts) {
            if (!(cur instanceof Map<?, ?> map)) return null;
            cur = ((Map<String, Object>) map).get(part);
            if (cur == null) return null;
        }
        return cur;
    }

    public String getString(String path, String def) {
        Object v = resolve(path);
        return v == null ? def : v.toString();
    }

    public int getInt(String path, int def) {
        Object v = resolve(path);
        if (v == null) return def;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return def; }
    }

    public long getLong(String path, long def) {
        Object v = resolve(path);
        if (v == null) return def;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return def; }
    }

    public boolean getBoolean(String path, boolean def) {
        Object v = resolve(path);
        if (v == null) return def;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }
}
