package com.dk.jpatesting.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Error error = new Error();
    private Pagination pagination = new Pagination();

    @Getter
    @Setter
    public static class Error {
        private boolean includeMethods = true;
    }

    @Getter
    @Setter
    public static class Pagination {
        @Min(1)
        @Max(100)
        private int defaultPageSize = 10;

        @Min(1)
        @Max(100)
        private int maxPageSize = 100;

        private String defaultSortBy = "createdAt";
        private String defaultSortDir = "desc";
    }
}
