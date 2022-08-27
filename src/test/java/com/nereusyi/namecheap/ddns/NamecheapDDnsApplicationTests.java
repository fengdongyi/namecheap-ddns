package com.nereusyi.namecheap.ddns;

import com.nereusyi.namecheap.ddns.task.NamecheapDDnsTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NamecheapDDnsApplicationTests {

    @Autowired
    private NamecheapDDnsTask namecheapDDnsTask;

    @Test
    void contextLoads() {
    }

    @Test
    public void executeTaskTest() {
        namecheapDDnsTask.execute();
    }

}
