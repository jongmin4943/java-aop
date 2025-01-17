package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter pss) throws DataAccessException {
        final var conn = DataSourceUtils.getConnection(dataSource);
        try (final var pstmt = conn.prepareStatement(sql)) {
            pss.setParameters(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(final String sql, final Object... parameters) {
        update(sql, createPreparedStatementSetter(parameters));
    }

    public void update(final PreparedStatementCreator psc, final KeyHolder holder) {
        final var conn = DataSourceUtils.getConnection(dataSource);
        try {
            final var ps = psc.createPreparedStatement(conn);
            ps.executeUpdate();

            final var rs = ps.getGeneratedKeys();
            if (rs.next()) {
                holder.setId(rs.getLong(1));
            }
            rs.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rm, final PreparedStatementSetter pss) {
        List<T> list = query(sql, rm, pss);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rm, final Object... parameters) {
        return queryForObject(sql, rm, createPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(final String sql,
                             final RowMapper<T> rm,
                             final PreparedStatementSetter pss) throws DataAccessException {
        final var conn = DataSourceUtils.getConnection(dataSource);
        ResultSet rs = null;
        try (final var pstmt = conn.prepareStatement(sql)) {
            pss.setParameters(pstmt);
            rs = pstmt.executeQuery();

            List<T> list = new ArrayList<T>();
            while (rs.next()) {
                list.add(rm.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rm, final Object... parameters) {
        return query(sql, rm, createPreparedStatementSetter(parameters));
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }
}
