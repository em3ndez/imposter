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

package io.gatehill.imposter.openapi.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

/**
 * Generates Imposter configuration for the OpenAPI plugin.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ConfigGenerator {
    private static final Logger LOGGER = LogManager.getLogger(ConfigGenerator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static Path writeImposterConfig(List<Path> specificationFiles) throws IOException {
        final Path configDir = Files.createTempDirectory("imposter");
        specificationFiles.forEach(spec -> {
            try {
                // copy spec into place
                Files.copy(spec, configDir.resolve(spec.getFileName()));

                // write config file
                final Path configFile = configDir.resolve(spec.getFileName() + "-config.json");
                try (final FileOutputStream out = new FileOutputStream(configFile.toFile())) {
                    MAPPER.writeValue(out, new HashMap<String, Object>() {{
                        put("plugin", "openapi");
                        put("specFile", spec.getFileName().toString());
                    }});
                }
                LOGGER.debug("Wrote Imposter configuration file: {}", configFile);

            } catch (IOException e) {
                throw new RuntimeException(String.format("Error generating configuration for specification file %s in %s", spec, configDir), e);
            }
        });
        return configDir;
    }
}
