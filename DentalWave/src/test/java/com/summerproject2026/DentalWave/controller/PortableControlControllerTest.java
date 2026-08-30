package com.summerproject2026.DentalWave.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class PortableControlControllerTest {

    @Test
    void statusIdentifiesThePortableApplication() {
        PortableControlController controller = new PortableControlController(
                mock(ConfigurableApplicationContext.class), "strong-control-token");

        assertEquals("DentalWave", controller.status().get("application"));
        assertEquals("ready", controller.status().get("status"));
    }

    @Test
    void rejectsShutdownWithoutTheExactControlToken() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        PortableControlController controller = new PortableControlController(
                context, "strong-control-token");

        assertEquals(HttpStatus.FORBIDDEN, controller.shutdown(null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, controller.shutdown("wrong-token").getStatusCode());
        verify(context, never()).close();
    }

    @Test
    void exactControlTokenTriggersApplicationSpecificShutdown() {
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        PortableControlController controller = new PortableControlController(
                context, "strong-control-token");

        assertEquals(HttpStatus.OK,
                controller.shutdown("strong-control-token").getStatusCode());
        verify(context, timeout(1500)).close();
    }
}
