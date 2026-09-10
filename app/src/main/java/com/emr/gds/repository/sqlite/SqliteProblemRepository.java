package com.emr.gds.repository.sqlite;

import com.emr.gds.core.db.DbPaths;
import com.emr.gds.repository.ProblemRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite-backed repository for the persistent problem list.
 */
public class SqliteProblemRepository implements ProblemRepository {

    private final Path dbFile;

    public SqliteProblemRepository() {
        this(DbPaths.resolveDbPath("prolist.db"));
    }

    public SqliteProblemRepository(Path dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void init() throws SQLException {
        ensureDirectories();
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS problems (id INTEGER PRIMARY KEY AUTOINCREMENT, problem_text TEXT NOT NULL UNIQUE)");
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM problems")) {
                if (rs.next() && rs.getInt("count") == 0) {
                    stmt.execute("INSERT INTO problems (problem_text) VALUES ('Hypercholesterolemia [F/U]')");
                    stmt.execute("INSERT INTO problems (problem_text) VALUES ('Prediabetes (FBS 108 mg/dL)')");
                    stmt.execute("INSERT INTO problems (problem_text) VALUES ('Thyroid nodule (small)')");
                }
            }
        }
    }

    @Override
    public List<String> findAll() throws SQLException {
        ensureDirectories();
        List<String> results = new ArrayList<>();
        String sql = "SELECT problem_text FROM problems ORDER BY problem_text COLLATE NOCASE";
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(rs.getString("problem_text"));
            }
        }
        return results;
    }

    @Override
    public void insert(String problemText) throws SQLException {
        ensureDirectories();
        String sql = "INSERT INTO problems(problem_text) VALUES(?)";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, problemText);
            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean delete(String problemText) throws SQLException {
        ensureDirectories();
        String sql = "DELETE FROM problems WHERE problem_text = ?";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, problemText);
            return pstmt.executeUpdate() > 0;
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
