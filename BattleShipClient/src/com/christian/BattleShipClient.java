/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian;

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.SwingWorker;

/**
 *
 * @author christian
 */
public class BattleShipClient implements ClientInitListener {

    private static final int ERROR_CODE          =    -1;
    private static final int WAITING_CODE        =     0;
    private static final int ALL_OPPONENTS_READY =     1;
        
    public static void main(String[] args) {
        
        /* Create and display the form */
        BattleShipClient client = new BattleShipClient();
        showNetworkSettingsDialog(client);
               
    }

    @Override
    public void initClient(final String hostName, final String serverAddr, 
                           final int serverPort) {
    
        /* first step is to show the gui */
        final GameBoard gameBoard = new GameBoard(Controller.DEFAULT_GRID_SIZE);
        gameBoard.setVisible(true);
        gameBoard.toConsole("Hello " + hostName);
        gameBoard.toConsole("Connecting to server...");
        
        SwingWorker socketWorker = new SwingWorker<Socket, Integer>() {

            @Override
            protected Socket doInBackground() throws Exception {
                     
                Socket socket = null;
                try {
                    //attempt to connect to the server
                    socket = new Socket(serverAddr, serverPort);
                    
                    gameBoard.toConsole("Connection successful!!");
                    gameBoard.toConsole("Waiting for opponent...");
                    
                    /* Open an input stream to read messages from the server.
                     * If we are the first of two players attempting a connection
                     * then we will get two messages from the server - one to 
                     * let us know the server is waiting for an opponent and 
                     * another to let us know two clients are connected. There
                     * is also the possibility that the 
                     */
                    
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    
                    int messageCode = in.readInt();
                    String controlFlag = Controller.GO_SECOND;
                    
                    if(messageCode == WAITING_CODE) {
                        gameBoard.toConsole("Waiting for opponent...");
                        messageCode = in.readInt();
                        controlFlag = Controller.GO_FIRST;
                    }

                    if(messageCode == ALL_OPPONENTS_READY) {
                        
                        new Controller(hostName, controlFlag, socket, gameBoard);
                    }
                    else if(messageCode == ERROR_CODE){
                        //get the error message
                        gameBoard.toConsole(in.readUTF());
                    }
                    
                }
                catch(UnknownHostException e) {
                    gameBoard.toConsole(e.toString());
                }
                catch(IOException e) {
                    System.out.println(e);
                }
                
                return socket;
            }
            
        };
        
        socketWorker.execute();
        
    }
    
    
    
    public static void showNetworkSettingsDialog(final ClientInitListener listener) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new NetworkSettingsDialog(listener).setVisible(true);
            }
        });
    }
    
}

