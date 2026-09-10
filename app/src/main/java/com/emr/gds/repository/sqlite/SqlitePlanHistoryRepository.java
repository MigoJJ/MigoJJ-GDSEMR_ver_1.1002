package com.emr.gds.repository.sqlite;

import com.emr.gds.core.db.DbPaths;
import com.emr.gds.domain.PlanHistoryEntry;
import com.emr.gds.repository.PlanHistoryRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite-backed repository for plan history.
 */
public class SqlitePlanHistoryRepository implements PlanHistoryRepository {

    private final Path dbFile;

    public SqlitePlanHistoryRepository() {
        this(DbPaths.resolveDbPath("plan_history.db"));
    }

    public SqlitePlanHistoryRepository(Path dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void init() throws SQLException {
        ensureDirectories();
        try (Connection c = openConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS plan_history (id INTEGER PRIMARY KEY, created_at TEXT NOT NULL, section TEXT, content TEXT, patient_id TEXT, encounter_date TEXT)");
        }
    }

    @Override
    public void save(PlanHistoryEntry entry) throws SQLException {
        ensureDirectories();
        String sql = "INSERT INTO plan_history (created_at, section, content, patient_id, encounter_date) VALUES (?,?,?,?,?)";
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entry.createdAt().toString());
            ps.setString(2, entry.section());
            ps.setString(3, entry.content());
            ps.setString(4, entry.patientId());
            ps.setString(5, entry.encounterDate());
            ps.executeUpdate();
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
    }

    private void ensureDirectories() throws SQLException {
        try {
            Path parent = dbFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception e) {
            throw new SQLException("Failed to create database directories", e);
        }
    }
}
