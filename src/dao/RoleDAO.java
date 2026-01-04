package dao;

import config.DatabaseConnection;
import model.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

    public List<Role> getAllRoles() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT id_role, nama_role, keterangan FROM roles ORDER BY id_role";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Role role = new Role();
                role.setIdRole(rs.getLong("id_role"));
                role.setNamaRole(rs.getString("nama_role"));
                role.setKeterangan(rs.getString("keterangan"));
                list.add(role);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}