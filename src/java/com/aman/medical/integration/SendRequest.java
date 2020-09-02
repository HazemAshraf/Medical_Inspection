/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aman.medical.integration;

import com.aman.medical.db.CRUD;
import com.aman.medical.db.getcon;
import com.aman.medical.log.LoggingActions;
import com.aman.medical.log.LoggingActions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Hazem Ashraf
 */
public class SendRequest extends CRUD {

    private String REQUEST_URL;
    private String REQUEST_ID;
    private String PARAMS;

    public SendRequest(String URL, String requestID, String PARAMS) {
        this.REQUEST_URL = URL;
        this.REQUEST_ID = requestID;
        this.PARAMS = PARAMS;
    }

    public SendRequest() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getURL() {
        return REQUEST_URL;
    }

    public void setURL(String URL) {
        this.REQUEST_URL = URL;
    }

    public String getRequestID() {
        return REQUEST_ID;
    }

    public void setRequestID(String requestID) {
        this.REQUEST_ID = requestID;
    }

    public String getPARAMS() {
        return PARAMS;
    }

    public void setPARAMS(String PARAMS) {
        this.PARAMS = PARAMS;
    }

    @Override
    public String toString() {
        return "SendRequest{" + "URL=" + REQUEST_URL + ", requestID=" + REQUEST_ID + ", PARAMS=" + PARAMS + '}';
    }

    public void sendGET(String GET_URL, String TrackID) throws IOException {
        URL obj = new URL(GET_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //set request header
        con.setRequestMethod("GET");
        con.setRequestProperty("version", "1.0");
        con.setRequestProperty("category", "Request");
        con.setRequestProperty("Service", "TIT_Medical_Results");
        con.setRequestProperty("Timestamp", String.valueOf(System.currentTimeMillis()));
        con.setRequestProperty("TrackID", TrackID);

        int responseCode = con.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
        } else {

        }

    }

    public int sendPOST() throws IOException, SQLException, ClassNotFoundException {

        //intilize variables 
        Connection Con = null;
       // Statement stmt = null;

        getcon c = new getcon();

        URL obj = new URL(REQUEST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //set request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = PARAMS.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                if (response.toString().contains("\"error_Message\":200")) {
                    //write to database that this succes notified request
                    Con = c.myconnection();
                    // stmt = Con.createStatement();
                    LoggingActions log = new LoggingActions();
                    Map<String, String> params = new HashMap<>();
                    params.put("response", response.toString());
                    params.put("requestID", REQUEST_ID);
                    log.setCON(Con);
                    log.setTABLE_NAME("mi.log_success");
                    log.setPARAMS(params);
                    int saved = log.save();
                    //  int updated = stmt.executeUpdate("insert into mi.log_success (response,requestID) values ('" + response.toString() + "' , '" + REQUEST_ID + "')");
                    params = new HashMap<>();
                    params.put("notified", "1");
                    int updated = update(Con, "mi.clients_data", "WHERE requestID = '" + REQUEST_ID + "'", params);
                    // int updated1 = stmt.executeUpdate("update mi.clients_data set notified = 1 where requestID = '" + REQUEST_ID + "'");
                    // stmt.close();
                    Con.close();
                    return 0;
                } else {
                    int codeReturn = 1;
                    if (response.toString().contains("\"errorCode\":-4")) {
                        codeReturn = -4;
                    }
                    //write to database that this faild notified request
                    Con = c.myconnection();
                    // stmt = Con.createStatement();
                    LoggingActions log = new LoggingActions();
                    Map<String, String> params = new HashMap<>();
                    params.put("response", response.toString());
                    params.put("requestID", REQUEST_ID);
                    log.setCON(Con);
                    log.setTABLE_NAME("mi.log_faild");
                    log.setPARAMS(params);
                    int saved = log.save();
                    // int updated = stmt.executeUpdate("insert into mi.log_faild (response,requestID) values ('" + response.toString() + "' , '" + REQUEST_ID + "')");
                    params = new HashMap<>();
                    params.put("notified", "0");
                    int updated = update(Con, "mi.clients_data", "WHERE requestID = '" + REQUEST_ID + "'", params);
                    // int updated1 = stmt.executeUpdate("update mi.clients_data set notified = 0 where requestID = '" + REQUEST_ID + "'");
                    // stmt.close();
                    Con.close();
                    return codeReturn;
                }

            }
        } else {
            //write to database that this faild notified request
            Con = c.myconnection();
            //stmt = Con.createStatement();
            LoggingActions log = new LoggingActions();
            Map<String, String> params = new HashMap<>();
            params.put("response", "NO JSON COME FROM OTHER SIDE");
            params.put("requestID", REQUEST_ID);
            log.setCON(Con);
            log.setTABLE_NAME("mi.log_faild");
            log.setPARAMS(params);
            int saved = log.save();
            //int updated = stmt.executeUpdate("insert into mi.log_faild (response,requestID) values ('NO JSON COME FROM OTHER SIDE' , '" + REQUEST_ID + "')");
            params = new HashMap<>();
            params.put("notified", "-1");
            int updated = update(Con, "mi.clients_data", "WHERE requestID = '" + REQUEST_ID + "'", params);
            // int updated1 = stmt.executeUpdate("update mi.clients_data set notified = -1 where requestID = '" + REQUEST_ID + "'");
        //    stmt.close();
            Con.close();
            return -1;
        }

    }
}
