package com.igot.cb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("IntegrationFrameworkApplication Tests")
class IntegrationFrameworkApplicationTest {

    @Test
    @DisplayName("main - with args - invokes SpringApplication.run with correct class and args")
    void main_withArgs_invokesSpringApplicationRunWithCorrectClassAndArgs() {
        try (MockedStatic<SpringApplication> mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp
                    .when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            String[] args = {"--server.port=8080"};
            IntegrationFrameworkApplication.main(args);

            mockedSpringApp.verify(
                    () -> SpringApplication.run(IntegrationFrameworkApplication.class, args),
                    times(1)
            );
        }
    }

    @Test
    @DisplayName("main - with empty args - invokes SpringApplication.run successfully")
    void main_withEmptyArgs_invokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp
                    .when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            String[] args = new String[]{};
            IntegrationFrameworkApplication.main(args);

            mockedSpringApp.verify(
                    () -> SpringApplication.run(IntegrationFrameworkApplication.class, args),
                    times(1)
            );
        }
    }

    @Test
    @DisplayName("main - SpringApplication.run is called exactly once per invocation")
    void main_springApplicationRunCalledExactlyOnce() {
        try (MockedStatic<SpringApplication> mockedSpringApp = mockStatic(SpringApplication.class)) {
            mockedSpringApp
                    .when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            IntegrationFrameworkApplication.main(new String[]{});

            mockedSpringApp.verify(
                    () -> SpringApplication.run(any(Class.class), any(String[].class)),
                    times(1)
            );
        }
    }
}

