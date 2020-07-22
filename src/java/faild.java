/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//import com.google.common.base.Strings;
import com.aman.medical.db.getcon;
import java.io.BufferedReader;
import java.io.FileWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 *
 * @author win7
 */
@WebServlet(urlPatterns = {"/retry"})
public class faild extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
 private static int sendPOST(String POST_URL, String POST_PARAMS, String requestID) throws IOException, SQLException, ClassNotFoundException {
        
        
        
        Connection Con = null;
            Statement stmt = null;

            getcon c = new getcon();
            Con = c.myconnection();

            stmt = Con.createStatement();
            
            
        System.out.println("JSON is " + POST_PARAMS);
        FileWriter file = new FileWriter("E:\\Biometrics\\log_request.txt");
        file.write(new Timestamp(System.currentTimeMillis()).toString() + " POST /API/oculist " + POST_PARAMS);
        file.close();

        URL obj = new URL(POST_URL);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //set request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);
//        con.setRequestProperty("Accept-Charset", "UTF-8");
//        con.setRequestProperty("Content-Type", "application/json");
        // For POST only - START
//		con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = POST_PARAMS.getBytes("utf-8");
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
                System.out.println(response.toString());
                FileWriter file1 = new FileWriter("E:\\Biometrics\\log_response.txt");
                file1.write(new Timestamp(System.currentTimeMillis()).toString() + " POST /drvintegration/API/MedicalCheckup/NotifyResults " + response.toString());
                file1.close();
//                String txt = "\n" + new Timestamp(System.currentTimeMillis()).toString() + " POST /drvintegration_test/API/MedicalCheckup/NotifyResults " + response.toString();
////                      
//                Files.write(Paths.get("E:\\Biometrics\\log_response.txt"), txt.getBytes(), StandardOpenOption.APPEND);
                if (response.toString().contains("200")) {
                    //write to database that this succes notified request
                    
                       int updated = stmt.executeUpdate("insert into mi.log_success_retry (response,requestID) values ('" + response.toString() + "' , '" + requestID + "')");
              
                    return 0;
                } else {
                    //write to database that this faild notified request
                         int updated = stmt.executeUpdate("insert into mi.log_faild_retry (response,requestID) values ('" + response.toString() + "' , '" + requestID + "')");
                    return 1;
                }

            }
        } else {
            //write to database that this faild notified request
                   int updated = stmt.executeUpdate("insert into mi.log_faild_retry (response,requestID) values ('NO JSON COME FROM OTHER SIDE' , '" + requestID + "')");
                    
            return -1;
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, ClassNotFoundException, JRException, InterruptedException {
        Connection Con = null;
        Statement stmt = null , stmt1 = null , stmt2=null;
        try {
                response.setContentType("text/html;charset=UTF-8");
              
                
                getcon c = new getcon();
                
                 Con = c.myconnection();
                
                String sql = "select * from mi.retry , mi.clients_data where mi.retry.requestID1 = mi.clients_data.requestID";
                 stmt = Con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                String photo64="";
                while(rs.next()){
                //fetch this request ID 
                 Blob b = rs.getBlob("photo");
                byte[] ba = b.getBytes(1, (int) b.length());
                photo64 = new String(ba);
                
                
                String json = "{\"header\": {\"version\": \"1.0\",\"category\": \"request\",\"service\": \" TIT_Medical_Results \",\"timestamp\": \"03-09-2018 13:19\",\"tid\": \"594f2c57-e0d6-4311-87ffac491c4337dd\"},\"body\": {\"RequestID\": " + rs.getString("requestID") + ",\"MedicalCheckupID\": \"" + rs.getString("MedicalCheckupID") + "\",\"MedicalCheckupDate\": \"" + rs.getTimestamp("request_date").toString() + "\",\"MedicalCheckupResults\": 2,\"MedicalCheckupPhoto\": \"" + photo64 + "\",\"BloodGroup\": \""+rs.getString("blood_group")+"\",\"BioPath\": \"\",\"MedicalConditions\": []}}";

                     
                        Thread.sleep(3000);

                        int res = sendPOST("http://192.168.235.54/drvintegration/API/MedicalCheckup/NotifyResults", json, rs.getString("requestID"));
//                        int count = 0;
//                        while(res == 1 || res == -1){
//                            if(count == 4) break;
//                            count++;
//                          res = sendPOST("http://192.168.235.50/drvintegration/API/MedicalCheckup/NotifyResults", json, rs.getString("requestID"));
//                        }
                }
               
               
         PrintWriter out = response.getWriter();
          out.println ("done");
          
            
            } catch(SQLException ex){
              Logger.getLogger(ConfirmData.class.getName()).log(Level.SEVERE, null, ex);
              System.err.println(ex.toString());
            }
            finally{
            try {
                Con.close();
                stmt.close();
               
            } catch (SQLException ex) {
                Logger.getLogger(ConfirmData.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println(ex.toString());
            }
            }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConfirmData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JRException ex) {
            Logger.getLogger(ConfirmData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
         Logger.getLogger(faild.class.getName()).log(Level.SEVERE, null, ex);
     }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConfirmData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JRException ex) {
            Logger.getLogger(ConfirmData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
         Logger.getLogger(faild.class.getName()).log(Level.SEVERE, null, ex);
     }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
