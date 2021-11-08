package com.backbase.account.mock.util;

import static com.google.common.base.CaseFormat.*;
import static java.lang.Thread.currentThread;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;

/**
 * Object creation utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntityUtil {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setVisibility(new VisibilityChecker.Std(Visibility.ANY))
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setSerializationInclusion(Include.NON_NULL)
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .enable(JsonParser.Feature.ALLOW_TRAILING_COMMA)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static final String RES_ARRANGEMENT_PRESENTATION = "arrangement-presentation";
    public static final String RES_ARRANGEMENT_PRESENTATION_MASKED = "arrangement-presentation-masked";
    public static final String RES_ARRANGEMENT_PRESENTATION_MASKED_COMPAT = "arrangement-presentation-masked-compat";

    public static final String RES_TRANSLATION = "translation";
    public static final String RES_PRODUCT_ITEM = "product-item";

    private static Map<Class<?>, AtomicInteger> lastTestInstance = new IdentityHashMap<>();
    private static final Map<Object, Integer> instances = new IdentityHashMap<>();

    /**
     * Get the <code>{number}</code> that has been used to generate an object.
     */
    public static int getTestInstance(Object o) {
        return instances.computeIfAbsent(o, k -> {
            throw new IllegalArgumentException("No such test instance " + o);
        });
    }

    /**
     * Create an instance of type <code>type</code> using a JSON template.
     * <p>
     * The template is looked in the root of the classpath by using the simple name of the type (e.g.
     * <code>ClassSimpleName.json</code>). If the resource is found, then the instance is deserialised
     * from it; otherwise the type is simply instantiated.
     * </p>
     * <p>
     * The JSON template may contain the pattern <code>{number}</code> that is replaced with the value
     * of the <code>instance</code> parameter.
     * </p>
     * <p>
     * The instance is passed to the customiser provided by the third argument.
     * </p>
     *
     * @param type the type of the object to be created.
     * @param instance an instance identifier.
     * @param cust consumer of the created instance used for customisation.
     */
    public static <T> T buildTestInstance(Class<T> type, int instance, Consumer<T> cust) {
        return buildTestInstance(type.getSimpleName(), type, instance, cust);
    }

    /**
     * Use a specified resource name to create an object instance, see
     * {@link #buildTestInstance(Class, int, Consumer)}.
     *
     * @param resName the name of the JSON resource
     * @param type the type of the object to be created.
     * @param instance an instance identifier.
     * @param cust consumer of the created instance used for customisation.
     */
    @SneakyThrows
    public static <T> T buildTestInstance(String resName, Class<T> type, int instance, Consumer<T> cust) {
        final String json = readJsonTemplate(resName, instance);
        final T obj = readValue(type, json);

        cust.accept(obj);

        doWithFields(obj.getClass(), f -> injectEnum(obj, f, instance), f -> f.getType().isEnum());

        instances.put(obj, instance);

        return obj;
    }

    @SneakyThrows
    public static String readJsonTemplate(String resource, int instance) {
        final ClassLoader cld = currentThread().getContextClassLoader();
        final URL url = Stream.of(
            cld.getResource(resource + ".json"),
            cld.getResource(caseAlternative(resource) + ".json"))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseThrow(() -> new FileNotFoundException("Cannot find resource " + resource));

        return IOUtils.toString(url, StandardCharsets.UTF_8)
            .replace("{number}", Integer.toString(instance));
    }

    private static String caseAlternative(String resource) {
        return resource.indexOf('-') < 0
            ? UPPER_CAMEL.to(LOWER_HYPHEN, resource.replaceAll("((Client)?Dto|Entity)$", ""))
            : LOWER_HYPHEN.to(UPPER_CAMEL, resource);
    }

    @SneakyThrows
    public static <T extends JsonNode> T readNodeTemplate(String resource, int instance) {
        return (T) OBJECT_MAPPER.readTree(readJsonTemplate(resource, instance));
    }

    /**
     * Create an instance of type <code>type</code> with a default customizer.
     *
     * @see EntityUtil#buildTestInstance(Class, int, Consumer)
     */
    public static <T> T buildTestInstance(Class<T> type, int instance) {
        return buildTestInstance(type, instance, p -> {
        });
    }

    @SneakyThrows
    public static <T> T buildTestInstance(String resName, Class<T> type, int instance) {
        return buildTestInstance(resName, type, instance, p -> {
        });
    }

    public static <T> T newTestInstance(Class<T> type) {
        return buildTestInstance(type, nextTestInstance(type), p -> {
        });
    }

    public static <T> T newTestInstance(Class<T> type, Consumer<T> cust) {
        return buildTestInstance(type.getSimpleName(), type, nextTestInstance(type), cust);
    }

    public static <T> T newTestInstance(String resName, Class<T> type) {
        return buildTestInstance(resName, type, nextTestInstance(type), p -> {
        });
    }

    public static <T> T newTestInstance(String resName, Class<T> type, Consumer<T> cust) {
        return buildTestInstance(type, nextTestInstance(type));
    }

    @SneakyThrows
    public static <T> T injectId(T t, Object id) {
        writeField(t, "id", id, true);

        return t;
    }

    @SneakyThrows
    public static <T> T injectRandomId(T t) {
        return injectId(t, UUID.randomUUID().toString());
    }

    public static void resetTestInstance() {
        instances.clear();
        lastTestInstance.clear();
    }

    private static int nextTestInstance(Class<?> type) {
        return lastTestInstance.computeIfAbsent(type, t -> new AtomicInteger()).incrementAndGet();
    }

    private static <T> T readValue(Class<T> type, final String json) throws JsonProcessingException {
        try {
            final T obj = type.getConstructor().newInstance();

            OBJECT_MAPPER.readerForUpdating(obj).readValue(json);

            return obj;
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException
            | NoSuchMethodException | SecurityException e) {
            // the class may not be directly instantiable, but Jackson should be able to handle it
            return OBJECT_MAPPER.readValue(json, type);
        }
    }

    @SneakyThrows
    private static void injectEnum(Object o, Field f, int instance) {
        final Object[] values = f.getType().getEnumConstants();

        makeAccessible(f);

        f.set(o, values[instance % values.length]);
    }
}
