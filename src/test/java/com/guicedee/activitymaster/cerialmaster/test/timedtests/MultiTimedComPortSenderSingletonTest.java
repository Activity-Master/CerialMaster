package com.guicedee.activitymaster.cerialmaster.test.timedtests;

import com.guicedee.activitymaster.cerialmaster.client.MultiTimedComPortSender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MultiTimedComPortSenderSingletonTest {

    @Test
    public void singleton_static_field_and_getter_return_same_instance() {
        MultiTimedComPortSender a = MultiTimedComPortSender.INSTANCE;
        MultiTimedComPortSender b = MultiTimedComPortSender.getInstance();
        assertNotNull(a);
        assertSame(a, b, "INSTANCE and getInstance() should refer to the same singleton instance");

        // New instances should not be the same as the singleton (backward compatibility retained)
        MultiTimedComPortSender c = new MultiTimedComPortSender();
        assertNotSame(a, c, "Explicitly constructed instances are not the singleton; use getInstance/INSTANCE for singleton");
    }
}
