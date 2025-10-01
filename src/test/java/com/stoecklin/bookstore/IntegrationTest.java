package com.stoecklin.bookstore;

import com.stoecklin.bookstore.config.AsyncSyncConfiguration;
import com.stoecklin.bookstore.config.EmbeddedElasticsearch;
import com.stoecklin.bookstore.config.EmbeddedSQL;
import com.stoecklin.bookstore.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { BookstoreApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@EmbeddedElasticsearch
@EmbeddedSQL
public @interface IntegrationTest {
}
