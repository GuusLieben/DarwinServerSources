package com.darwinreforged.server.core.resources;

public enum ServerDependencies {
    REFLECTIONS(
            "org.reflections",
            "https://repo1.maven.org/maven2/org/reflections/reflections/0.9.11/reflections-0.9.11.jar",
            "0.9.11"),
    JACKSON_CORE(
            "com.fasterxml.jackson.core",
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.9.8/jackson-databind-2.9.8.jar",
            "2.9.8"),
    JACKSON_DATAFORMAT_YAML(
            "com.fasterxml.jackson.dataformat.yaml",
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/dataformat/jackson-dataformat-yaml/2.9.8/jackson-dataformat-yaml-2.9.8.jar",
            "2.9.8"
    ),
    JACKSON_DATATYPE_JSR310(
            "com.fasterxml.jackson.datatype.jsr310",
            "https://repo1.maven.org/maven2/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.9.8/jackson-datatype-jsr310-2.9.8.jar",
            "2.9.8"
    ),
    APACHE_COMMONS_COLLECTIONS(
            "org.apache.commons.collections4",
            "https://repo1.maven.org/maven2/org/apache/commons/commons-collections4/4.1/commons-collections4-4.1.jar",
            "4.1"
    ),
    ORMLITE_CORE(
            "com.j256.ormlite.core",
            "https://repo1.maven.org/maven2/com/j256/ormlite/ormlite-core/5.1/ormlite-core-5.1.jar",
            "5.1"
    ),
    ORMLITE_JDBC(
            "com.j256.ormlite.jdbc",
            "https://repo1.maven.org/maven2/com/j256/ormlite/ormlite-jdbc/5.1/ormlite-jdbc-5.1.jar",
            "5.1"
    );

    private final String basePackage;
    private final String url;
    private final String version;

    ServerDependencies(String basePackage, String url, String version) {
        this.basePackage = basePackage;
        this.url = url;
        this.version = version;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public String getBaseFile() {
        String[] urlSplit = getUrl().split("/");
        String fileName = urlSplit[urlSplit.length - 1];
        return fileName.endsWith(".jar") ? (fileName) : (getBasePackage() + ".jar");
    }
}
