package org.delcom.ifs25022pbo2026springbootcashflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DotEnvTest {

    private static final String PLAIN = "CASHFLOW_TEST_PLAIN";
    private static final String DOUBLE = "CASHFLOW_TEST_DOUBLE";
    private static final String SINGLE = "CASHFLOW_TEST_SINGLE";

    @AfterEach
    void clearProperties() {
        System.clearProperty(PLAIN);
        System.clearProperty(DOUBLE);
        System.clearProperty(SINGLE);
        System.clearProperty("CASHFLOW_TEST_MISMATCH_D");
        System.clearProperty("CASHFLOW_TEST_MISMATCH_S");
        System.clearProperty("CASHFLOW_TEST_SHORT");
        System.clearProperty("CASHFLOW_TEST_EMPTY");
    }

    @Test
    void loadReadsEnvFile(@TempDir Path tempDir) throws Exception {
        Path env = tempDir.resolve(".env");
        Files.writeString(env, "# komentar\n"
                + PLAIN + "=9090\n"
                + "\n"
                + DOUBLE + "=\"7070\"\n"
                + SINGLE + "='6060'\n"
                + "=tanpa-kunci\n"
                + "TANPA_SAMA_DENGAN\n");

        DotEnv.load(env);

        assertEquals("9090", System.getProperty(PLAIN));
        assertEquals("7070", System.getProperty(DOUBLE));
        assertEquals("6060", System.getProperty(SINGLE));
    }

    @Test
    void loadIgnoresMissingFile(@TempDir Path tempDir) throws Exception {
        DotEnv.load(tempDir.resolve("tidak-ada.env"));

        assertNull(System.getProperty(PLAIN));
    }

    @Test
    void loadDoesNotOverrideExistingSystemProperty(@TempDir Path tempDir) throws Exception {
        System.setProperty(PLAIN, "1111");
        Path env = tempDir.resolve(".env");
        Files.writeString(env, PLAIN + "=2222\n");

        DotEnv.load(env);

        assertEquals("1111", System.getProperty(PLAIN));
    }

    @Test
    void loadDefaultLooksForDotEnvInWorkingDirectory() throws Exception {
        DotEnv.load();
    }

    @Test
    void applyLineSkipsBlankCommentAndLinesWithoutKey() {
        DotEnv.applyLine("   ");
        DotEnv.applyLine("# hanya komentar");
        DotEnv.applyLine("=tanpa-kunci");
        DotEnv.applyLine(" =tanpa-kunci");
        DotEnv.applyLine("TANPA_SAMA_DENGAN");

        assertNull(System.getProperty("TANPA_SAMA_DENGAN"));
    }

    @Test
    void applyLineOnlyStripsMatchingQuotes() {
        DotEnv.applyLine("CASHFLOW_TEST_MISMATCH_D=\"abc'");
        DotEnv.applyLine("CASHFLOW_TEST_MISMATCH_S='abc\"");
        DotEnv.applyLine("CASHFLOW_TEST_SHORT=\"");
        DotEnv.applyLine("CASHFLOW_TEST_EMPTY=\"\"");

        assertEquals("\"abc'", System.getProperty("CASHFLOW_TEST_MISMATCH_D"));
        assertEquals("'abc\"", System.getProperty("CASHFLOW_TEST_MISMATCH_S"));
        assertEquals("\"", System.getProperty("CASHFLOW_TEST_SHORT"));
        assertEquals("", System.getProperty("CASHFLOW_TEST_EMPTY"));
    }

    @Test
    void applyLineDoesNotOverrideExistingEnvironmentVariable() {
        String existingKey = System.getenv().keySet().iterator().next();
        String before = System.getProperty(existingKey);

        DotEnv.applyLine(existingKey + "=should-not-win");

        assertEquals(before, System.getProperty(existingKey));
    }

    @Test
    void privateConstructorIsCovered() throws Exception {
        Constructor<DotEnv> constructor = DotEnv.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
