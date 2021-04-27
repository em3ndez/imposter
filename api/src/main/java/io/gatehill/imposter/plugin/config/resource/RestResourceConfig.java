package io.gatehill.imposter.plugin.config.resource;

import io.gatehill.imposter.plugin.config.ContentTypedConfig;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class RestResourceConfig extends AbstractResourceConfig implements MethodResourceConfig {
    private ResourceMethod method;

    @Override
    public ResourceMethod getMethod() {
        return method;
    }
}