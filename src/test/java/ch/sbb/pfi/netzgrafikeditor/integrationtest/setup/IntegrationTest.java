package ch.sbb.pfi.netzgrafikeditor.integrationtest.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.ControllableNowProvider;
import ch.sbb.pfi.netzgrafikeditor.integrationtest.helper.JsonHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfiguration.class)
public abstract class IntegrationTest {

    @Autowired protected TestDataService testDataService;

    @Autowired protected ObjectMapper objectMapper;

    @Autowired protected MockMvc mockMvc;

    @Autowired protected DSLContext context;

    @Autowired protected ControllableNowProvider controllableNowProvider;

    @Autowired protected JsonHandler jsonHandler;

    @SneakyThrows
    @BeforeEach
    protected void setup() {}

    @AfterEach
    protected void deleteAllData() {
        testDataService.deleteAllData();
    }

    protected ResultMatcher jsonResponse(Object expected) {
        return result -> {
            this.jsonHandler
                    .createMatcher(result.getResponse().getContentAsString(StandardCharsets.UTF_8))
                    .assertEqualsIgnoringOrder(expected);

            assertEquals(
                    MediaType.APPLICATION_JSON_VALUE,
                    result.getResponse().getContentType(),
                    "Response Content-Type is not " + MediaType.APPLICATION_JSON_VALUE);
        };
    }

    protected ResultMatcher jsonResponse(
            ThrowingConsumer<JsonHandler.JsonMatcher> jsonMatcherConsumer) {
        return result -> {
            assertEquals(
                    MediaType.APPLICATION_JSON_VALUE,
                    result.getResponse().getContentType(),
                    "Response Content-Type is not " + MediaType.APPLICATION_JSON_VALUE);
            jsonMatcherConsumer.accept(
                    this.jsonHandler.createMatcher(
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8)));
        };
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T value) throws Exception;
    }
}
