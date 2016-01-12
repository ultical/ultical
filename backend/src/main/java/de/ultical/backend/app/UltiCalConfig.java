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

    private DfvApiConfig dfvApi;

    @NotNull
    private DataSourceFactory database;

    private CacheBuilderSpec authenticationCache = CacheBuilderSpec.parse("maximumSize = 1000");

    @NotNull
    private MailConfig mail;
}
