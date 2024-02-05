package ch.sbb.pfi.netzgrafikeditor.integrationtest.helper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class JsonHandler {
    private final ObjectMapper om;

    public JsonMatcher createMatcher(String jsonString) throws JsonProcessingException {
        return createMatcher(this.om.readTree(jsonString));
    }

    public JsonMatcher createMatcher(JsonNode actual) {
        return new JsonMatcher(actual, this::toJsonNode);
    }

    public JsonAccessor createAccessor(String jsonString) throws JsonProcessingException {
        return createAccessor(this.om.readTree(jsonString));
    }

    public JsonAccessor createAccessor(JsonNode actual) {
        return new JsonAccessor(actual);
    }

    private JsonNode toJsonNode(Object obj) {
        try {
            String expectedJsonString = om.writeValueAsString(obj);
            return om.readTree(expectedJsonString);
        } catch (JsonProcessingException e) {
            Assertions.fail("Can not serialize expected object to JSON nodes");
            return null; // never called, assertion will throw runtime exception
        }
    }

    @RequiredArgsConstructor
    public static class JsonAccessor {
        private final JsonNode json;

        public Optional<Integer> getIntegerByPath(String path) {
            return Optional.of(this.json.at(path))
                    .filter(jsonNode -> !jsonNode.isMissingNode())
                    .map(JsonNode::asInt);
        }
    }

    @RequiredArgsConstructor
    public static class JsonMatcher {
        private final JsonNode actual;
        private final Function<Object, JsonNode> jsonizer;

        private final Set<String> ignoredPaths = new HashSet<>();

        public JsonMatcher ignorePath(String path) {
            this.ignoredPaths.add(path);
            return this;
        }

        public JsonMatcher assertEquals(Object expected) {
            return this.assertEquals(jsonizer.apply(expected));
        }

        public JsonMatcher assertEquals(JsonNode expected) {
            var patch = JsonDiff.asJson(actual, expected);

            var changeCount =
                    StreamSupport.stream(patch.spliterator(), false)
                            .filter(
                                    jsonNode ->
                                            !this.ignoredPaths.contains(
                                                    jsonNode.get("path").textValue()))
                            .count();

            if (changeCount > 0) {
                assertThat(actual.toPrettyString())
                        .as("Differences:\n %s", patch.toPrettyString())
                        .isEqualTo(expected.toPrettyString())
                        .withFailMessage("JSON response does not matches expected result");
            }

            return this;
        }

        public JsonMatcher assertEqualsIgnoringOrder(Object expected) {
            return this.assertEqualsIgnoringOrder(jsonizer.apply(expected));
        }

        public JsonMatcher assertEqualsIgnoringOrder(JsonNode expected) {
            var patch = JsonDiff.asJson(actual, expected);

            var changeCount =
                    StreamSupport.stream(patch.spliterator(), false)
                            .filter(jsonNode -> !jsonNode.get("op").textValue().equals("move"))
                            .filter(
                                    jsonNode ->
                                            !this.ignoredPaths.contains(
                                                    jsonNode.get("path").textValue()))
                            .count();

            if (changeCount > 0) {
                assertThat(actual.toPrettyString())
                        .as("Differences:\n %s", patch.toPrettyString())
                        .isEqualTo(expected.toPrettyString())
                        .withFailMessage("JSON response does not matches expected result");
            }

            return this;
        }
    }
}
