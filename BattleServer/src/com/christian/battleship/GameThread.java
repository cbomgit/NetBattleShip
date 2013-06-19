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
    
    public void kill() {
        //send goodbye messages to the clients and kill the game
        sockOneOut.println(GOODBYE);
        sockTwoOut.println(GOODBYE);
        playing = false;
        try {
            cleanup();
            
        }
        catch(IOException e){
            System.out.println(e);
        }
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
            /* at this point both clients should be setting up their boards,
             * so the server should wait for READY messages on both sockets.
             */
            
            System.out.println(sockOneIn.readLine());
            System.out.println(sockTwoIn.readLine());
            
        }
        catch(IOException e) {
            
            System.out.println("Game thread " + threadId + " threw exception");
            System.out.println(e);
            
        }
        
       
                        
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
    
    private void processOneTurn(BufferedReader offenseIn, PrintWriter offenseOut,
                                BufferedReader defenseIn, PrintWriter defenseOut) {
        
        try {
            //get a guess from the attacking player
            int x = Integer.parseInt(offenseIn.readLine());
            int y = Integer.parseInt(offenseIn.readLine());
            
                                                                                        System.out.println("Recieved guess: " + x + ", " + y);
            //send the guess to the opponent
            defenseOut.println(ATTACK);
            defenseOut.println(x);
            defenseOut.println(y);
            
            //get the result from the opponent
            int result = Integer.parseInt(defenseIn.readLine());
                                                                                        System.out.println("Sending back result: " + result);
            //send the result back to the attacking player, with the original guess
            offenseOut.println(RESULT);
            offenseOut.println(result);
            offenseOut.println(x);
            offenseOut.println(y);
            
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
                    waitForClientToBeReady(offenseIn);
                    waitForClientToBeReady(defenseIn);
                }
                else {
                    playing = false;
                }
            }
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }

    private void waitForClientToBeReady(BufferedReader sockIn) throws IOException {
                
            //the next incoming message should be the client ready message
        sockIn.readLine();
        
    }
    
    private void cleanup() throws IOException{
        
            sockOneIn.close();
            sockTwoIn.close();
            sockOneOut.close();
            sockTwoOut.close();
            playerOneSocket.close();
            playerTwoSocket.close();
            
    }
}
