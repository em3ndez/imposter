/*
 * Copyright (c) 2016-2021.
 *
 * This file is part of Imposter.
 *
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as
 * defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, "Sell" means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software: Imposter
 *
 * License: GNU Lesser General Public License version 3
 *
 * Licensor: Peter Cornish
 *
 * Imposter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Imposter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Imposter.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.gatehill.imposter.plugin.rest;

import io.gatehill.imposter.ImposterConfig;
import io.gatehill.imposter.plugin.PluginInfo;
import io.gatehill.imposter.plugin.ScriptedPlugin;
import io.gatehill.imposter.plugin.config.ConfiguredPlugin;
import io.gatehill.imposter.plugin.config.ContentTypedConfig;
import io.gatehill.imposter.plugin.config.resource.ResourceConfig;
import io.gatehill.imposter.plugin.rest.config.ResourceConfigType;
import io.gatehill.imposter.plugin.rest.config.RestPluginConfig;
import io.gatehill.imposter.plugin.rest.config.RestPluginResourceConfig;
import io.gatehill.imposter.service.ResourceService;
import io.gatehill.imposter.service.ResponseService;
import io.gatehill.imposter.util.FileUtil;
import io.gatehill.imposter.util.HttpUtil;
import io.gatehill.imposter.util.ResourceUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.gatehill.imposter.util.HttpUtil.CONTENT_TYPE_JSON;
import static java.util.Optional.ofNullable;

/**
 * Plugin for simple RESTful APIs.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@PluginInfo("rest")
public class RestPluginImpl<C extends RestPluginConfig> extends ConfiguredPlugin<C> implements ScriptedPlugin<C> {
    private static final Logger LOGGER = LogManager.getLogger(RestPluginImpl.class);

    /**
     * Example: <pre>/anything/:id/something</pre>
     */
    private static final Pattern PARAM_MATCHER = Pattern.compile(".*:(.+).*");

    @Inject
    private ImposterConfig imposterConfig;

    @Inject
    private ResourceService resourceService;

    @Inject
    private ResponseService responseService;

    private List<C> configs;

    @SuppressWarnings("unchecked")
    @Override
    protected Class<C> getConfigClass() {
        return (Class<C>) RestPluginConfig.class;
    }

    @Override
    protected void configurePlugin(List<C> configs) {
        this.configs = configs;
    }

    @Override
    public void configureRoutes(Router router) {
        configs.forEach(config -> {
            // add root handler
            // TODO consider changing this to config.getPath() if non-null
            addObjectHandler(router, "", config, config);

            // add child resource handlers
            ofNullable(config.getResources()).ifPresent(resources -> resources.forEach(resource ->
                    addResourceHandler(router, config, resource)));
        });
    }

    private void addResourceHandler(Router router, C rootConfig, RestPluginResourceConfig resourceConfig) {
        final ResourceConfigType resourceType = ofNullable(resourceConfig.getType())
                .orElse(ResourceConfigType.OBJECT);

        switch (resourceType) {
            case OBJECT:
                addObjectHandler(router, rootConfig, resourceConfig);
                break;

            case ARRAY:
                addArrayHandler(router, rootConfig, resourceConfig);
                break;
        }
    }

    private void addObjectHandler(Router router, C pluginConfig, ContentTypedConfig resourceConfig) {
        addObjectHandler(router, pluginConfig.getPath(), pluginConfig, resourceConfig);
    }

    private void addObjectHandler(Router router, String rootPath, C pluginConfig, ContentTypedConfig resourceConfig) {
        final String qualifiedPath = buildQualifiedPath(rootPath, resourceConfig);
        final HttpMethod method = ResourceUtil.convertMethodToVertx(resourceConfig);
        LOGGER.debug("Adding {} object handler: {}", method, qualifiedPath);

        router.route(method, qualifiedPath).handler(resourceService.handleRoute(imposterConfig, pluginConfig, vertx, routingContext -> {
            // script should fire first
            scriptHandler(pluginConfig, resourceConfig, routingContext, getInjector(), responseBehaviour -> {
                LOGGER.info("Handling {} object request for: {}", method, routingContext.request().absoluteURI());
                responseService.sendResponse(pluginConfig, resourceConfig, routingContext, responseBehaviour);
            });
        }));
    }

    private void addArrayHandler(Router router, C pluginConfig, RestPluginResourceConfig resourceConfig) {
        final String resourcePath = resourceConfig.getPath();
        final String qualifiedPath = buildQualifiedPath(pluginConfig.getPath(), resourceConfig);
        final HttpMethod method = ResourceUtil.convertMethodToVertx(resourceConfig);
        LOGGER.debug("Adding {} array handler: {}", method, qualifiedPath);

        // validate path includes parameter
        final Matcher matcher = PARAM_MATCHER.matcher(resourcePath);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Resource '%s' does not contain a field ID parameter",
                    resourcePath));
        }

        router.route(method, qualifiedPath).handler(resourceService.handleRoute(imposterConfig, pluginConfig, vertx, routingContext -> {
            // script should fire first
            scriptHandler(pluginConfig, resourceConfig, routingContext, getInjector(), responseBehaviour -> {
                LOGGER.info("Handling {} array request for: {}", method, routingContext.request().absoluteURI());

                // get the first param in the path
                final String idFieldName = matcher.group(1);
                final String idField = routingContext.request().getParam(idFieldName);

                // find row
                final Optional<JsonObject> result = FileUtil.findRow(idFieldName, idField,
                        responseService.loadResponseAsJsonArray(pluginConfig, responseBehaviour));

                final HttpServerResponse response = routingContext.response();

                if (result.isPresent()) {
                    LOGGER.info("Returning single row for {}:{}", idFieldName, idField);
                    response.setStatusCode(HttpUtil.HTTP_OK)
                            .putHeader(HttpUtil.CONTENT_TYPE, CONTENT_TYPE_JSON)
                            .end(result.get().encodePrettily());
                } else {
                    // no such record
                    LOGGER.error("No row found for {}:{}", idFieldName, idField);
                    response.setStatusCode(HttpUtil.HTTP_NOT_FOUND)
                            .end();
                }
            });
        }));
    }

    private String buildQualifiedPath(String rootPath, ResourceConfig resourceConfig) {
        final String qualifiedPath = ofNullable(rootPath).orElse("") + ofNullable(resourceConfig.getPath()).orElse("");
        return qualifiedPath.startsWith("/") ? qualifiedPath : "/" + qualifiedPath;
    }
}
