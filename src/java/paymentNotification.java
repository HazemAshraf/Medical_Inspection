/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import Entity.Paymentnotify;
import Entity.Vehicleinspection;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 *
 * @author User
 */
@WebServlet(name = "paymentNotification", urlPatterns = {"/paymentNotification"})
public class paymentNotification extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, JRException, ParseException {
        //response.setContentType("text/html;charset=UTF-8");
//        try (PrintWriter out = response.getWriter()) {
        /* TODO output your page here. You may use following sample code. */
//                   JasperReport report = JasperCompileManager.compileReport("C:/User/user/Desktop/testPDF.jrxml");
//                JasperPrint print = JasperFillManager.fillReport(report,null);
//                JasperExportManager.exportReportToPdfFile(print,"C:/User/user/Desktop/Test.pdf");
        Map properties = new HashMap();
        properties.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/mi?zeroDateTimeBehavior=convertToNull&characterEncoding=UTF8&jdbcCompliantTruncation=false&serverTimezone=UTC");
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("Medical_InspPU", properties);

        EntityManager entityManager = factory.createEntityManager();
        // response.setContentType("text/html;charset=UTF-8");
        // try (PrintWriter out = response.getWriter()) {
        JsonObject rcvd;
        JsonObject obj1 = new JsonObject();

        InputStreamReader reader = new InputStreamReader(request.getInputStream(), "UTF-8");
        JsonReader json = new JsonReader(reader);

        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(json);
        rcvd = (JsonObject) root;
        HashMap params = new HashMap();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA " + rcvd.toString());
        System.out.println("hi boysssssssss  : " + rcvd.get("ServiceID"));

        String a = rcvd.get("ServiceID").getAsString();
        String PayedElements = rcvd.get("PayedElements").getAsString();
        if (a.equals("TIT_Medical_payment")) {
            System.out.println("aaaaaaaaaaaaa");
            Paymentnotify obj = mapper.readValue(rcvd.toString(), Paymentnotify.class);
            obj.setTotalAmount("200");
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa " + obj + "                " + obj.getTotalAmount());
            Random random = new Random();
            obj.setPaymentNumber(Integer.toString(random.nextInt()));
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            System.out.println("aaaaaaaaaaaaa " + obj);

            System.out.println("bbbbbbbbb " + obj.getTimeStamp());

            String output = obj.getTimeStamp().substring(0, 10);

            obj.setDate(output);
            List<Paymentnotify> payNotifyByDate = entityManager.createNamedQuery("Paymentnotify.findByDateAndTraffiUnit", Paymentnotify.class).setParameter("date", obj.getDate()).
                    setParameter("trafficUnit", obj.getTrafficUnit()).getResultList();
            if (payNotifyByDate.size() < 1) {
                obj.setQueueNumber("1");
            } else {
                List<Integer> l = new ArrayList<>();
                payNotifyByDate.stream().forEach(i -> {
                    l.add(Integer.parseInt(i.getQueueNumber()));
                });
                Collections.sort(l, Collections.reverseOrder());
                System.out.println("aaaaaaaaaaaaaaaaaaaa list  " + l);
                System.out.println("sadfasfafs " + l.get(0));
                int value = l.get(0) + 1;
                System.out.println("queeeeeeeee " + value);
                obj.setQueueNumber(Integer.toString(value));
            }

            Date d = dateFormat.parse(obj.getTimeStamp());
            System.out.println("dddddddddddddddddddddd " + d);
            //Paymentnotify p = new Paymentnotify();
            List<Paymentnotify> listp = entityManager.createNamedQuery("Paymentnotify.findByRequestID", Paymentnotify.class).setParameter("requestID", obj.getRequestID()).getResultList();
            // System.out.println("aaaaaaaaaaaaaaaaaaaaa "+p);
            try {
                if (listp.size() > 0) {

                    obj1.addProperty("RequestID", obj.getRequestID());
                    obj1.addProperty("Confirmed", "true");
                    obj1.addProperty("TimeStamp", obj.getTimeStamp());

                    obj1.addProperty("PaymentNumber", obj.getPaymentNumber());
                    obj1.addProperty("queueNumber", obj.getQueueNumber());

                } else {
                    obj.setStatusCode("200 OK");
                    entityManager.getTransaction().begin();
                    entityManager.persist(obj);
                    entityManager.getTransaction().commit();

                    obj1.addProperty("RequestID", obj.getRequestID());
                    obj1.addProperty("Confirmed", "true");
                    obj1.addProperty("TimeStamp", obj.getTimeStamp());

                    obj1.addProperty("PaymentNumber", obj.getPaymentNumber());
                    obj1.addProperty("queueNumber", obj.getQueueNumber());
                }

                params.put("receiptType", "مايكنة الكشف الطبي");
                params.put("ApplicantName", obj.getApplicantName());
                params.put("TimeStamp", obj.getTimeStamp());
                params.put("requestID", obj.getRequestID());
                params.put("TrafficUnit", obj.getTrafficUnit());
                params.put("paymentNumber", obj.getPaymentNumber());
                params.put("QueueNumverNumber", obj.getQueueNumber());
                params.put("totalAmount", obj.getTotalAmount());

                String jasperName = "";
                if ((PayedElements.contains("Medical") && PayedElements.contains("E-Exam")) || PayedElements.isEmpty()||PayedElements.equals("") || PayedElements == null || PayedElements.contains("[]")) {
                    System.out.println("NEWWWWWWWWWWW");
                    jasperName = "viPolicy2_3_qoute_reciept.jasper";
                    
                } else if (PayedElements.contains("Medical")) {
                    jasperName = "viPolicy2_3_qoute_reciept_old.jasper";
                    System.out.println("OLDDDDDDDDDDDDDDDDDDDDDDDDD");
                }

                String fileName = "medical_reciept_" + obj.getRequestID();
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                        InputStream is = classloader.getResourceAsStream(jasperName);
              //  InputStream is = classloader.getResourceAsStream("viPolicy2_3_qoute_reciept.jasper");
                // System.out.println("input stream " + is.read() + "   " + is.toString());
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
                //  JasperReport jasperReport = JasperCompileManager.compileReport("testPDF.jrxml");

//                    System.out.println("aaaaaaaaaaaaaaaaa");
//                JasperReport report = JasperCompileManager.compileReport("C:/User/user/Desktop/testPDF.jrxml");
//                    System.out.println("bbbbbbbbbbbbbbb");
//                JasperPrint print = JasperFillManager.fillReport(report,null);
//                    System.out.println("cccccccccc");
//                JasperExportManager.exportReportToPdfFile(print,"C:/User/user/Desktop/Test.pdf");
//                    System.out.println("ddddddddddddddddddd");
                JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
              //  String receiptPath = "C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\path\\to\\receipt\\" + fileName + ".pdf";
                   String receiptPath = "C:\\Users\\User\\Desktop\\apache-tomcat-8.5.5\\webapps\\path\\to\\receipt\\" + fileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(print, receiptPath);

                String IP = "";
                // System.out.println("IP of client is : "+request.getRemoteAddr() + " " +request.getRemoteHost());
                if (request.getRemoteAddr().toString().contains("192.168.235.55") || request.getRemoteAddr().toString().contains("192.168.235.51")) {
                    IP = "192.168.235.76";
                } else {
                    IP = "192.168.250.138";
                }
                String receiptPathResp = "http://" + IP + ":8080/receipt/" + fileName + ".pdf";

                // byte[] byteStream = JasperRunManager.runReportToPdf(jasperReport, params, new JREmptyDataSource());
                // OutputStream outStream = response.getOutputStream();
//                    response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//                    response.setHeader("Content-Disposition","attachment; filename=" + fileName + ".pdf");
//                    response.setHeader("Content-Stream", byteStream.toString());
                // response.setContentType("application/pdf");
                // response.setContentLength(byteStream.length);
                // out.print("asdasdasdas");
                PrintWriter out = response.getWriter();
                obj1.addProperty("receiptURL", receiptPathResp);
                out.write(obj1.toString());
            } catch (Exception e) {
                obj.setStatusCode("500 Internal server error");
                entityManager.getTransaction().begin();
                entityManager.persist(obj);
                entityManager.getTransaction().commit();
                response.setStatus(500);
            }

        } else if (a.equals("TIT_Vehicle_Inspection_payment")) {
            Vehicleinspection obj = mapper.readValue(rcvd.toString(), Vehicleinspection.class);
            obj.setTotalAmount(rcvd.get("TotalAmount").getAsString());
            obj.setCancel(0);
            obj.setUsedcancel(0);
            System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa " + obj + "                " + obj.getTotalAmount());
            Random random = new Random();
            obj.setPaymentNumber(Integer.toString(random.nextInt()));
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            System.out.println("aaaaaaaaaaaaa " + obj);

            System.out.println("bbbbbbbbb " + obj.getTimeStamp());

            String output = obj.getTimeStamp().substring(0, 10);

            obj.setDate(output);
            List<Vehicleinspection> payNotifyByDate = entityManager.createNamedQuery("Vehicleinspection.findByDateAndTraffiUnit", Vehicleinspection.class).setParameter("date", obj.getDate()).
                    setParameter("trafficUnit", obj.getTrafficUnit()).getResultList();
            if (payNotifyByDate.size() < 1) {
                obj.setQueueNumber("1");
            } else {
                List<Integer> l = new ArrayList<>();
                payNotifyByDate.stream().forEach(i -> {
                    l.add(Integer.parseInt(i.getQueueNumber()));
                });
                Collections.sort(l, Collections.reverseOrder());
                System.out.println("aaaaaaaaaaaaaaaaaaaa list  " + l);
                System.out.println("sadfasfafs " + l.get(0));
                int value = l.get(0) + 1;
                System.out.println("queeeeeeeee " + value);
                obj.setQueueNumber(Integer.toString(value));
            }

            Date d = dateFormat.parse(obj.getTimeStamp());
            System.out.println("dddddddddddddddddddddd " + d);

            //Paymentnotify p = new Paymentnotify();
            List<Vehicleinspection> listp = entityManager.createNamedQuery("Vehicleinspection.findByRequestID", Vehicleinspection.class).setParameter("requestID", obj.getRequestID()).getResultList();
            // System.out.println("aaaaaaaaaaaaaaaaaaaaa "+p);
            try {
                if (listp.size() > 0) {

                    obj1.addProperty("RequestID", obj.getRequestID());
                    obj1.addProperty("Confirmed", "true");
                    obj1.addProperty("TimeStamp", obj.getTimeStamp());

                    obj1.addProperty("PaymentNumber", obj.getPaymentNumber());
                    obj1.addProperty("queueNumber", obj.getQueueNumber());

                } else {
                    obj.setStatusCode("200 OK");
                    entityManager.getTransaction().begin();
                    entityManager.persist(obj);
                    entityManager.getTransaction().commit();

                    obj1.addProperty("RequestID", obj.getRequestID());
                    obj1.addProperty("Confirmed", "true");
                    obj1.addProperty("TimeStamp", obj.getTimeStamp());

                    obj1.addProperty("PaymentNumber", obj.getPaymentNumber());
                    obj1.addProperty("queueNumber", obj.getQueueNumber());
                }

                params.put("receiptType", "خدمة الفحص الفني للمركبات");
                params.put("ApplicantName", obj.getApplicantName());
                params.put("TimeStamp", obj.getTimeStamp());
                params.put("requestID", obj.getRequestID());
                params.put("paymentNumber", obj.getPaymentNumber());
                params.put("TrafficUnit", obj.getTrafficUnit());
                params.put("totalAmount", obj.getTotalAmount());
                String fileName = "vehicle_insp_reciept_" + obj.getRequestID();
                ClassLoader classloader = Thread.currentThread().getContextClassLoader();
                InputStream is = classloader.getResourceAsStream("viPolicy2_3_vehicle_reciept.jasper");
                // System.out.println("input stream " + is.read() + "   " + is.toString());
                JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
                //  JasperReport jasperReport = JasperCompileManager.compileReport("testPDF.jrxml");

//                    System.out.println("aaaaaaaaaaaaaaaaa");
//                JasperReport report = JasperCompileManager.compileReport("C:/User/user/Desktop/testPDF.jrxml");
//                    System.out.println("bbbbbbbbbbbbbbb");
//                JasperPrint print = JasperFillManager.fillReport(report,null);
//                    System.out.println("cccccccccc");
//                JasperExportManager.exportReportToPdfFile(print,"C:/User/user/Desktop/Test.pdf");
//                    System.out.println("ddddddddddddddddddd");
                JasperPrint print = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
                String receiptPath = "C:\\Program Files\\Apache Software Foundation\\Tomcat 8.5\\webapps\\path\\to\\receipt\\" + fileName + ".pdf";
                //   String receiptPath = "C:\\Users\\User\\Desktop\\apache-tomcat-8.5.5\\webapps\\path\\to\\receipt\\" + fileName + ".pdf";
                JasperExportManager.exportReportToPdfFile(print, receiptPath);
                String IP = "";
                if (request.getRemoteAddr().toString().contains("192.168.235.50") || request.getRemoteAddr().toString().contains("192.168.245.50")) {
                    IP = "192.168.235.76";
                } else {
                    IP = "192.168.250.138";
                }
                String receiptPathResp = "http://" + IP + ":8080/receipt/" + fileName + ".pdf";
                // byte[] byteStream = JasperRunManager.runReportToPdf(jasperReport, params, new JREmptyDataSource());
                // OutputStream outStream = response.getOutputStream();

//                    response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
//                    response.setHeader("Content-Disposition","attachment; filename=" + fileName + ".pdf");
//                    response.setHeader("Content-Stream", byteStream.toString());
                // response.setContentType("application/pdf");
                // response.setContentLength(byteStream.length);
                // out.print("asdasdasdas");
                PrintWriter out = response.getWriter();
                obj1.addProperty("receiptURL", receiptPathResp);
                out.write(obj1.toString());
            } catch (Exception e) {
                obj.setStatusCode("500 Internal server error");
                entityManager.getTransaction().begin();
                entityManager.persist(obj);
                entityManager.getTransaction().commit();
                response.setStatus(500);
            }
        }

//                
//                try {
//                   // System.out.println("bytestream is " + byteStream.length +"     "  +  byteStream.toString());
//                    outStream.write(byteStream);        //,0,byteStream.length);
//               } catch(Exception e2){
//                    response.setStatus(500);
//                }
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
        } catch (JRException ex) {
            Logger.getLogger(paymentNotification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(paymentNotification.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (JRException ex) {
            Logger.getLogger(paymentNotification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(paymentNotification.class.getName()).log(Level.SEVERE, null, ex);
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
