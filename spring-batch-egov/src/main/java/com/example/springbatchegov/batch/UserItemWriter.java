package com.example.springbatchegov.batch;

import com.example.springbatchegov.domain.User;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class UserItemWriter implements ItemWriter<User> {

    private final JdbcTemplate jdbcTemplate;

    public UserItemWriter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void write(Chunk<? extends User> chunk) {
        // batchUpdate: PreparedStatement.addBatch() 내부 사용 → 벌크 INSERT
        jdbcTemplate.batchUpdate(
                "INSERT INTO users (name, email, department, created_at) VALUES (?, ?, ?, ?)",
                chunk.getItems(),
                chunk.size(),
                (ps, user) -> {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getEmail());
                    ps.setString(3, user.getDepartment());
                    ps.setObject(4, user.getCreatedAt());
                }
        );
        System.out.println("[WRITE] " + chunk.size() + "건 저장 완료");
    }
}
