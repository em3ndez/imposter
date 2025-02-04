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

package io.gatehill.imposter.plugin.openapi.util;

import io.vertx.ext.web.RoutingContext;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public final class ValidationReportUtil {
    private ValidationReportUtil() {
    }

    public static void sendValidationReport(RoutingContext routingContext, String reportMessages) {
        if (routingContext.parsedHeaders().accept().stream().anyMatch(a -> a.rawValue().equals("text/html"))) {
            routingContext.response().putHeader("Content-Type", "text/html")
                    .end(buildResponseReportHtml(reportMessages));
        } else {
            routingContext.response().putHeader("Content-Type", "text/plain")
                    .end(buildResponseReportPlain(reportMessages));
        }
    }

    private static String buildResponseReportPlain(String reportMessages) {
        return "Request validation failed:\n" + reportMessages + "\n";
    }

    private static String buildResponseReportHtml(String reportMessages) {
        return "<html>\n" +
                "<head><title>Invalid request</title></head>\n" +
                "<body><h1>Request validation failed</h1><br/><pre>" + reportMessages + "</pre></body>\n" +
                "</html>\n";
    }
}
