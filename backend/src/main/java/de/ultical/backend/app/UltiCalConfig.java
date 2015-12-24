package de.ultical.backend.app;

import javax.validation.constraints.NotNull;

import com.google.common.cache.CacheBuilderSpec;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import lombok.*;

@Data
@ToString
@EqualsAndHashCode(callSuper = false)
public class UltiCalConfig extends Configuration {
    private DfvApiConfig dfvApi;

    @NotNull
    private DataSourceFactory database;

    private CacheBuilderSpec authenticationCache = CacheBuilderSpec.parse("maximuSize:1000");
}
