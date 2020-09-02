/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aman.medical.log;

import com.aman.medical.db.CRUD;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hazem Ashraf
 */
public class LoggingActions extends CRUD {

    private String TABLE_NAME;
    private Map<String, String> PARAMS;
    private Connection CON;
    // private CRUD crud;

    public LoggingActions() {
        CON = null;
        TABLE_NAME = "";
        PARAMS = new HashMap<>();
        //   crud = new CRUD();
    }

    public Connection getCON() {
        return CON;
    }

    public void setCON(Connection CON) {
        this.CON = CON;
    }
//
//    public CRUD getCrud() {
//        return crud;
//    }
//
//    public void setCrud(CRUD crud) {
//        this.crud = crud;
//    }

    public String getTABLE_NAME() {
        return TABLE_NAME;
    }

    public void setTABLE_NAME(String TABLE_NAME) {
        this.TABLE_NAME = TABLE_NAME;
    }

    public Map<String, String> getPARAMS() {
        return PARAMS;
    }

    public void setPARAMS(Map<String, String> PARAMS) {
        this.PARAMS.putAll(PARAMS);
    }

    public int save() throws SQLException {
        int res = insert(CON, TABLE_NAME, PARAMS);
        return res;
    }

}
