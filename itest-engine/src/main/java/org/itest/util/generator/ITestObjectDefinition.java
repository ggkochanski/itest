package org.itest.util.generator;

public class ITestObjectDefinition {
    private final Class<?> clazz;

    private final String useName;

    private final String transform;

    public ITestObjectDefinition(Class<?> clazz, String use, String transform) {
        this.clazz = clazz;
        this.useName = use;
        this.transform = transform;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getUseName() {
        return useName;
    }

    public String getTransform() {
        return transform;
    }
}
