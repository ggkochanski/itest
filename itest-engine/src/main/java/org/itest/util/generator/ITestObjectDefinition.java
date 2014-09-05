package org.itest.util.generator;

public class ITestObjectDefinition {
    private final Class<?> clazz;

    private final String use;

    private final String transform;

    public ITestObjectDefinition(Class<?> clazz, String use, String transform) {
        this.clazz = clazz;
        this.use = use;
        this.transform = transform;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getUse() {
        return use;
    }

    public String getTransform() {
        return transform;
    }
}
