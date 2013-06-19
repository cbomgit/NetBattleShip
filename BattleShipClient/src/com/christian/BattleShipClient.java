/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    
    private Socket           socket;
    
    public static void main(String[] args) {
        
        /* Create and display the form */
        BattleShipClient client = new BattleShipClient();
        showNetworkSettingsDialog(client);
               
    }

    @Override
    public void initClient(final String serverAddr, final int serverPort) {
    
        /* first step is to show the gui */
        final GameBoard gameBoard = new GameBoard(11);
        gameBoard.setVisible(true);
        gameBoard.toConsole("Connecting to server...");
        
        SwingWorker socketWorker = new SwingWorker<Socket, Integer>() {

            @Override
            protected Socket doInBackground() throws Exception {
                                
                try {
                    //attempt to connect to the server
                    Socket socket = new Socket(serverAddr, serverPort);
                    
                    gameBoard.toConsole("Connection successful!!");
                    gameBoard.toConsole("Waiting for opponent...");
                    
                    /* Open an input stream to read messages from the server.
                     * If we are the first of two players attempting a connection
                     * then we will get two messages from the server - one to 
                     * let us know the server is waiting for an opponent and 
                     * another to let us know two clients are connected. There
                     * is also the possibility that the 
                     */
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader
                            (socket.getInputStream()));
                    
                    int messageCode = Integer.parseInt(in.readLine());
                    String controlFlag = Controller.DEFENSE;
                    
                    if(messageCode == WAITING_CODE) {
                        gameBoard.toConsole("Waiting for opponent...");
                        messageCode = Integer.parseInt(in.readLine());
                        controlFlag = Controller.OFFENSE;
                    }

                    if(messageCode == ALL_OPPONENTS_READY) {
                        new Controller(controlFlag, socket, gameBoard, 11);
                        gameBoard.toConsole("All opponents ready!!!");
                    }
                    else if(messageCode == ERROR_CODE){
                        //get the error message
                        gameBoard.toConsole(in.readLine());
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
