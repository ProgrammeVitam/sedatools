package fr.gouv.vitam.tools.mailextractlib.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailExtractProgressLoggerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailExtractProgressLoggerTest.class);

    private static final int CALLS = 20_000;

    private static final Duration MAX_DURATION = Duration.ofSeconds(2);

    private ListAppender<ILoggingEvent> appender;
    private ch.qos.logback.classic.Logger logger;

    @BeforeEach
    void setUp() {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MailExtractProgressLoggerTest.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        Thread.interrupted();
    }

    @Test
    void shouldNotBeSlowedDownWhenDoProgressLogIsFilteredByLevel() throws InterruptedException {
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(LOGGER, MailExtractProgressLogger.MESSAGE_GROUP);

        long start = System.nanoTime();
        for (int i = 0; i < CALLS; i++) {
            MailExtractProgressLogger.doProgressLog(mepl, MailExtractProgressLogger.MESSAGE_DETAILS, "filtered", null);
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(elapsed).isLessThan(MAX_DURATION);
        assertThat(appender.list).isEmpty();
    }

    @Test
    void shouldNotBeSlowedDownWhenDoProgressLogOneMoreCountedObjectIsFilteredByLevel() throws InterruptedException {
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(LOGGER, MailExtractProgressLogger.MESSAGE_GROUP);

        long start = System.nanoTime();
        for (int i = 0; i < CALLS; i++) {
            MailExtractProgressLogger.doProgressLogOneMoreCountedObject(
                mepl,
                MailExtractProgressLogger.MESSAGE_DETAILS,
                "filtered %count"
            );
        }
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        assertThat(elapsed).isLessThan(MAX_DURATION);
        assertThat(appender.list).isEmpty();
    }

    @Test
    void shouldPropagateInterruptionOnDoProgressLogEvenWhenFilteredByLevel() {
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(LOGGER, MailExtractProgressLogger.MESSAGE_GROUP);
        Thread.currentThread().interrupt();

        assertThatThrownBy(
            () ->
                MailExtractProgressLogger.doProgressLog(
                    mepl,
                    MailExtractProgressLogger.MESSAGE_DETAILS,
                    "filtered",
                    null
                )
        ).isInstanceOf(InterruptedException.class);
        assertThat(Thread.currentThread().isInterrupted()).isFalse();
    }

    @Test
    void shouldPropagateInterruptionOnDoProgressLogWhenLogged() {
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(
            LOGGER,
            MailExtractProgressLogger.MESSAGE_DETAILS
        );
        Thread.currentThread().interrupt();

        assertThatThrownBy(
            () -> MailExtractProgressLogger.doProgressLog(mepl, MailExtractProgressLogger.MESSAGE, "logged", null)
        ).isInstanceOf(InterruptedException.class);
    }

    @Test
    void shouldPropagateInterruptionOnDoProgressLogOneMoreCountedObjectWhenStepReached() {
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(
            LOGGER,
            MailExtractProgressLogger.MESSAGE,
            (count, log) -> {},
            1
        );
        Thread.currentThread().interrupt();

        assertThatThrownBy(
            () ->
                MailExtractProgressLogger.doProgressLogOneMoreCountedObject(
                    mepl,
                    MailExtractProgressLogger.MESSAGE,
                    "message %count"
                )
        ).isInstanceOf(InterruptedException.class);
    }

    @Test
    void shouldNotThrowOnDoProgressLogWhenNotInterrupted() {
        MailExtractProgressLogger mepl = new MailExtractProgressLogger(
            LOGGER,
            MailExtractProgressLogger.MESSAGE_DETAILS
        );

        assertThatCode(
            () -> MailExtractProgressLogger.doProgressLog(mepl, MailExtractProgressLogger.MESSAGE, "logged", null)
        ).doesNotThrowAnyException();
    }

    @Test
    void shouldDoNothingAndKeepInterruptFlagOnDoProgressLogWithNullLogger() {
        Thread.currentThread().interrupt();

        assertThatCode(
            () -> MailExtractProgressLogger.doProgressLog(null, MailExtractProgressLogger.MESSAGE, "ignored", null)
        ).doesNotThrowAnyException();
        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }
}
