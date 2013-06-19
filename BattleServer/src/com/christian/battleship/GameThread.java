/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian.battleship;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author christian
 */
class GameThread extends Thread {
    
    private int     threadId;
    private boolean playing;
    
    
    //this threads socekt
    private Socket                  playerOneSocket;
    private Socket                  playerTwoSocket;
    
    //io streams for each socket
    private BufferedReader          sockOneIn;
    private BufferedReader          sockTwoIn;
    
    private PrintWriter             sockOneOut;
    private PrintWriter             sockTwoOut;
    
    private static final String     CLIENT_READY      =  "Ready to play";
    private static final String     ATTACK            =  "Attack";
    private static final String     RESULT            =  "Result";
    
    private static final String     GOODBYE           =  "Goodbye";
    private static final String     PLAY_AGAIN        =  "Play again?";
    private static final String     OFFENSE           =  "Offense";
    private static final String     DEFENSE           =  "Defense";
    
    private static final int        ALL_SHIPS_SUNK    =  -4;
    
    public GameThread(int id, Socket socketOne, Socket socketTwo) {
       
       super("Game Thread");
       
       threadId = id;
       
       playerOneSocket = socketOne;
       playerTwoSocket = socketTwo;
       playing = true;
    }
    
    public void kill() throws IOException{
        //send goodbye messages to the clients and kill the game
        sockOneOut.println(GOODBYE);
        sockTwoOut.println(GOODBYE);
        playing = false;
        cleanup();
    }
    
    @Override 
    public void run()  {
        
        try {
            
            sockOneIn = new BufferedReader(
                        new InputStreamReader(playerOneSocket.getInputStream()));

            sockTwoIn = new BufferedReader(
                        new InputStreamReader(playerTwoSocket.getInputStream()));

            sockOneOut = new PrintWriter(playerOneSocket.getOutputStream(), true);
            sockTwoOut = new PrintWriter(playerTwoSocket.getOutputStream(), true);
            
            //exchange of host names
            String host1 = sockOneIn.readLine();
            String host2 = sockTwoIn.readLine();
            sockOneOut.println(host2);
            sockTwoOut.println(host1);
            
            /* at this point both clients should be setting up their boards,
             * so the server should wait for READY messages on both sockets.
             */
            
            System.out.println(sockOneIn.readLine());
            System.out.println(sockTwoIn.readLine());
                              
        /*now run the game loop */
        
        while(playing) {
            
            //player one takes a turn
            processOneTurn(sockOneIn, sockOneOut, sockTwoIn, sockTwoOut);

            /* if the game is over for any reason here, exit the loop to avoid
             * querying player two's socket streams
             */
            
            if(!playing)
                break;
            
            //player two takes a turn
            processOneTurn(sockTwoIn, sockTwoOut, sockOneIn, sockOneOut);
        }
        
        BattleServer.killGame(threadId);
        
        }
        catch(IOException e) {
            
            System.out.println("Game thread " + threadId + " threw exception");
            System.out.println(e);
            
        }
    }
    
    private void processOneTurn(BufferedReader offenseIn, PrintWriter offenseOut,
                                BufferedReader defenseIn, PrintWriter defenseOut) {
        
        try {
            //get a guess from the attacking player
            int x = Integer.parseInt(offenseIn.readLine());
            int y = Integer.parseInt(offenseIn.readLine());
            
            //send the guess to the opponent
            defenseOut.println(ATTACK);
            defenseOut.println(x);
            defenseOut.println(y);
            
            //get the result from the opponent
            int result = Integer.parseInt(defenseIn.readLine());
            
            //send the result back to the attacking player, with the original guess
            offenseOut.println(RESULT);
            offenseOut.println(result);
            offenseOut.println(x);
            offenseOut.println(y);
            
            //wait for the attacking player to finish processing the result
            //and then let the opponent know it can now take a turn
            if(offenseIn.readLine().equals(CLIENT_READY))
                defenseOut.println(CLIENT_READY);
            
            //if attacking player won the game, the server will try and 
            //set up a new game
            
            if(result == ALL_SHIPS_SUNK) {
                
                //make sure both players want to play again
                String p1reply = offenseIn.readLine();
                String p2reply = defenseIn.readLine();
                
                if(p1reply.equals(PLAY_AGAIN) && p2reply.equals(PLAY_AGAIN)) {
                    offenseOut.println(PLAY_AGAIN);
                    defenseOut.println(PLAY_AGAIN);
                    offenseOut.println(DEFENSE);
                    defenseOut.println(OFFENSE);
                    System.out.println(offenseIn.readLine());
                    System.out.println(defenseIn.readLine());
                }
                else 
                    playing = false;
                
            }
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }
    
    private void cleanup() throws IOException{
        
            sockOneIn.close();
            sockTwoIn.close();
            sockOneOut.close();
            sockTwoOut.close();
            playerOneSocket.close();
            playerTwoSocket.close();
            
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("Game id: ").append(threadId);
        sb.append("\nPlayer one connected @: ");
        sb.append(playerOneSocket.getInetAddress()).append(":");
        sb.append(playerOneSocket.getPort());
        sb.append("\nPlayer two connected @: ");
        sb.append(playerTwoSocket.getInetAddress()).append(":");
        sb.append(playerTwoSocket.getPort()).append("\n");
        
        return sb.toString();
    }
}
