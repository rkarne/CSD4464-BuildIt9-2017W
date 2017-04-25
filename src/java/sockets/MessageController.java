/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.Session;

/**
 *
 * @author c0587637
 */
@ApplicationScoped
public class MessageController {

    private List<Session> people = new ArrayList<>();
    private List<JsonObject> messages = new ArrayList<>();

    private Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            String jdbc = "jdbc:derby://localhost:1527/Messages";
            String username = "username";
            String password = "password";
            conn = DriverManager.getConnection(jdbc, username, password);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    
    public MessageController() {
        try {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Message");
            while (rs.next()) {
                JsonObject json = Json.createObjectBuilder()
                        .add("nickname", rs.getString("nickname"))
                        .add("timestamp", rs.getTimestamp("timestamp").toString())
                        .add("message", rs.getString("message"))
                        .build();
                messages.add(json);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<JsonObject> getMessages() {
        return messages;
    }

    public void setMessages(List<JsonObject> messages) {
        this.messages = messages;
    }

    public void add(JsonObject j) {
        try {
            String nickname = j.getString("nickname");
            String message = j.getString("message");
            String timestamp = j.getString("timestamp");
            
            // http://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
            Date date = javax.xml.bind.DatatypeConverter.parseDateTime(timestamp).getTime();

            Connection conn = getConnection();
            String sql = "INSERT INTO Message (Nickname, Message, Timestamp) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);
            pstmt.setString(2, message);
            pstmt.setTimestamp(3, new java.sql.Timestamp(date.getTime()));
            pstmt.executeUpdate();
            
            messages.add(j);
        } catch (SQLException ex) {
            Logger.getLogger(MessageController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Session> getPeople() {
        return people;
    }

    public void setPeople(List<Session> people) {
        this.people = people;
    }

    public void addSession(Session s) {
        this.people.add(s);
    }
}
