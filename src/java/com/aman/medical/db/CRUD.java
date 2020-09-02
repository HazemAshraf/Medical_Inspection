/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aman.medical.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author Hazem Ashraf
 */
public class CRUD {

    public int insert(Connection con, String table, Map<String, String> params) throws SQLException {
        StringBuilder sql = new StringBuilder();
        StringBuilder fields = new StringBuilder();
        StringBuilder values = new StringBuilder();

        sql.append("INSERT INTO ").append(table).append(" (");
        String sep = "";
        for (String key : params.keySet()) {
            fields.append(sep).append("`").append(key).append("`");
            String val = (String) params.get(key);
            if (val == null) {
                val = "NULL";
            } else {
                val = StringEscapeUtils.escapeSql((String) params.get(key));
            }
            if (val.toLowerCase().equals("null") || val.equals("CURRENT_TIMESTAMP()")) {
                values.append(sep).append(val);
            } else {
                values.append(sep).append("'").append(val).append("'");
            }

            sep = ", ";
        }
        sql.append(fields).append(") VALUES (").append(values).append(")");

        Statement stmt = null;
        try {
            // con = Database.getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(sql.toString(), 1);
            ResultSet auto = stmt.getGeneratedKeys();
            if (auto != null && auto.first()) {
                return auto.getInt(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(CRUD.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            stmt.close();
           // con.close();

        }

        return -1;
    }
    
    
      public int update(Connection con ,String table, String id, Map<String, String> params) throws SQLException {
    StringBuilder sql = new StringBuilder();
    
    sql.append("UPDATE ").append(table).append(" SET ");
    String sep = "";
    for (String key : params.keySet()) {
      String val = (String)params.get(key);
      if (val == null) {
        val = "NULL";
      } else {
        val = StringEscapeUtils.escapeSql((String)params.get(key));
      } 
      if (val.toLowerCase().equals("null") || val.equals("CURRENT_TIMESTAMP()")) {
        sql.append(sep).append("`").append(key).append("`= ").append(val).append(" ");
      } else {
        sql.append(sep).append("`").append(key).append("`='").append(val).append("'");
      } 
      sep = ", ";
    } 
    if (id.contains("WHERE") && id.contains("=")) {
      sql.append(" " + id);
    } else {
      sql.append(" WHERE id=").append(id);
    } 
    
   // con = null;
    Statement stmt = null;
    try {
      //con = Database.getConnection();
      stmt = con.createStatement();
      stmt.executeUpdate(sql.toString());
      if (stmt.getUpdateCount() > 0) {
          
          return stmt.getUpdateCount();
//        if (id.contains("WHERE") && id.contains("=")) {
//          return 0;
//        }
//        return Integer.valueOf(id).intValue();
      }
    
    } catch (SQLException ex) {
      Logger.getLogger(CRUD.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      
          stmt.close();
           // con.close();

    } 
 return -1;
  }

}
