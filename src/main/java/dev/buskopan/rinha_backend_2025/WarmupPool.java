package dev.buskopan.rinha_backend_2025;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;

@Component
public class WarmupPool implements InitializingBean {

    private final DataSource ds;

    public WarmupPool(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try (Connection con = ds.getConnection()) {
            System.out.println("Warmup Hikari pool");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
