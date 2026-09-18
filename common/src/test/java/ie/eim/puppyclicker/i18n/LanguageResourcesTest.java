package ie.eim.puppyclicker.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class LanguageResourcesTest {
    private static final String LANGUAGE_RESOURCE_DIRECTORY = "assets/puppyclicker/lang";
    private static final Pattern FORMAT_SPECIFIER = Pattern.compile("%(?:\\d+\\$)?([a-zA-Z%])");
    private static final Pattern PUPPYCLICKER_TRANSLATION_KEY = Pattern.compile(
            "\\\"((?:item|message|screen|tooltip)\\.puppyclicker\\.[^\\\"]+"
                    + "|key\\.(?:categories\\.puppyclicker|category\\.puppyclicker\\.[^\\\"]+"
                    + "|puppyclicker\\.[^\\\"]+)"
                    + "|puppyclicker\\.configuration\\.[^\\\"]+)\\\"");
    private static final Type LANGUAGE_MAP_TYPE = new TypeToken<Map<String, String>>() { }.getType();

    @Test
    void everyLocaleIsCompleteAndPreservesFormatArguments() throws Exception {
        Path languageDirectory = languageDirectory();
        Map<String, String> source = readLanguageFile(languageDirectory.resolve("en_us.json"));

        assertFalse(source.isEmpty(), "en_us.json must contain translation keys");

        try (Stream<Path> entries = Files.list(languageDirectory)) {
            List<Path> languageFiles = entries
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();

            assertFalse(languageFiles.isEmpty(), "At least en_us.json must be present");
            for (Path languageFile : languageFiles) {
                assertLanguageMatchesSource(source, languageFile);
            }
        }
    }

    @Test
    void everyTranslationKeyReferencedBySourceExistsInEnglish() throws Exception {
        Map<String, String> source = readLanguageFile(languageDirectory().resolve("en_us.json"));
        Path repositoryRoot = repositoryRoot();
        Set<String> referencedKeys = new TreeSet<>();

        for (Path sourceRoot : List.of(
                repositoryRoot.resolve("common/src/main/java"),
                repositoryRoot.resolve("platforms"))) {
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                for (Path sourceFile : files
                        .filter(path -> path.getFileName().toString().endsWith(".java"))
                        .toList()) {
                    Matcher matcher = PUPPYCLICKER_TRANSLATION_KEY.matcher(
                            Files.readString(sourceFile, StandardCharsets.UTF_8));
                    while (matcher.find()) {
                        referencedKeys.add(matcher.group(1));
                    }
                }
            }
        }

        Set<String> missing = new TreeSet<>(referencedKeys);
        missing.removeAll(source.keySet());
        assertEquals(Set.of(), missing, "Java source references missing US English keys");
    }

    private static Path languageDirectory() throws URISyntaxException {
        URL resource = LanguageResourcesTest.class.getClassLoader().getResource(LANGUAGE_RESOURCE_DIRECTORY);
        assertNotNull(resource, "Missing " + LANGUAGE_RESOURCE_DIRECTORY);
        return Path.of(resource.toURI());
    }

    private static Path repositoryRoot() {
        Path candidate = Path.of("").toAbsolutePath();
        while (candidate != null) {
            if (Files.isRegularFile(candidate.resolve("settings.gradle"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new IllegalStateException("Could not locate repository root from the test working directory");
    }

    private static Map<String, String> readLanguageFile(Path languageFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(languageFile, StandardCharsets.UTF_8)) {
            Map<String, String> translations = new Gson().fromJson(reader, LANGUAGE_MAP_TYPE);
            assertNotNull(translations, languageFile.getFileName() + " must contain a JSON object");
            return translations;
        }
    }

    private static void assertLanguageMatchesSource(Map<String, String> source, Path languageFile)
            throws IOException {
        Map<String, String> translations = readLanguageFile(languageFile);
        Set<String> missing = new TreeSet<>(source.keySet());
        missing.removeAll(translations.keySet());
        Set<String> unexpected = new TreeSet<>(translations.keySet());
        unexpected.removeAll(source.keySet());

        assertEquals(Set.of(), missing, languageFile.getFileName() + " is missing translation keys");
        assertEquals(Set.of(), unexpected, languageFile.getFileName() + " has unknown translation keys");

        for (Map.Entry<String, String> entry : source.entrySet()) {
            String key = entry.getKey();
            String translatedValue = translations.get(key);
            assertFalse(translatedValue == null || translatedValue.isBlank(),
                    languageFile.getFileName() + " has a blank value for " + key);
            assertEquals(
                    formatArguments(entry.getValue()),
                    formatArguments(translatedValue),
                    languageFile.getFileName() + " changes the format arguments for " + key);
        }
    }

    private static List<String> formatArguments(String value) {
        Matcher matcher = FORMAT_SPECIFIER.matcher(value);
        Stream.Builder<String> arguments = Stream.builder();
        while (matcher.find()) {
            String conversion = matcher.group(1);
            if (!conversion.equals("%")) {
                arguments.add(conversion.toLowerCase());
            }
        }
        return arguments.build().sorted().toList();
    }
}
