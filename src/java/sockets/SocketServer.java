/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sockets;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Basic;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author c0587637
 */
@ServerEndpoint("/sample")
@ApplicationScoped
public class SocketServer {
    
    
    @Inject
    private MessageController msgCtrl;
    
    @OnOpen
    public void connected(Session session) throws IOException {
        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (JsonObject m : msgCtrl.getMessages()) {
            arr.add(m);
        }
        String output = arr.build().toString();
        Basic basic = session.getBasicRemote();
        basic.sendText(output);
    }
    
    @OnMessage
    public void receiveMessage(String message, Session session) throws IOException {

        // Add the person to the list of people
        if (!msgCtrl.getPeople().contains(session)) {
            msgCtrl.addSession(session);
        }

        // Save the message to the list of messages
        JsonObject json = Json.createReader(new StringReader(message)).readObject();
        msgCtrl.add(json);

        // Broadcast the message out to all connected people
        for (Session person : msgCtrl.getPeople()) {
            Basic basic = person.getBasicRemote();
            String output = Json.createArrayBuilder().add(json).build().toString();
            basic.sendText(output);
        }
    }
}
