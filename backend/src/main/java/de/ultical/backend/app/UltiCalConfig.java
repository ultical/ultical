/**
 * Copyright (C) 2015-2016 ultical contributors
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 * * Apache License, version 2.0
 * * Apache Software License, version 1.0
 * * Mozilla Public License, versions 1.0, 1.1 and 2.0
 * * Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU Affero General Public License
 * version 3 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the  GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.ultical.backend.app;

import javax.validation.constraints.NotNull;

import com.google.common.cache.CacheBuilderSpec;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class UltiCalConfig extends Configuration {

    @Data
    public static class MailConfig {
        private String smtpHost;
        private String smtpUser;
        private String smtpPassword;
        private String smtpPort;
        private String smtpSender;
    }

    @Data
    public static class DebugMode {
        private boolean enabled = false;
        private String mailCatcher = "";
    }

    @Data
    public static class JobsConfig {
        private boolean dfvMvSyncEnabled = false;
    }

    @Data
    public static class ReCaptcha {
        private String url;
        private String secret;
    }

    private DfvApiConfig dfvApi;

    private DebugMode debugMode;

    private ReCaptcha reCaptcha;

    private JobsConfig jobs;

    @NotNull
    private DataSourceFactory database;

    private CacheBuilderSpec authenticationCache = CacheBuilderSpec.parse("maximumSize = 1000");

    @NotNull
    private MailConfig mail;

    private String frontendUrl;

    private boolean corsFilterEnabled = false;
}
