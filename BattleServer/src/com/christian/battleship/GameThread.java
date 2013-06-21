/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian.battleship;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private DataInputStream         sockOneIn;
    private DataInputStream         sockTwoIn;
    
    private DataOutputStream        sockOneOut;
    private DataOutputStream        sockTwoOut;
    
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
        sockOneOut.writeUTF(GOODBYE);
        sockTwoOut.writeUTF(GOODBYE);
        playing = false;
        cleanup();
    }
    
    @Override 
    public void run()  {
        
        try {
            
            sockOneIn = new DataInputStream(playerOneSocket.getInputStream());

            sockTwoIn = new DataInputStream(playerTwoSocket.getInputStream());

            sockOneOut = new DataOutputStream(playerOneSocket.getOutputStream());
            sockTwoOut = new DataOutputStream(playerTwoSocket.getOutputStream());
            
            //exchange of host names
            String host1 = sockOneIn.readUTF();
            String host2 = sockTwoIn.readUTF();
            sockOneOut.writeUTF(host2);
            sockTwoOut.writeUTF(host1);
            
            /* at this point both clients should be setting up their boards,
             * so the server should wait for READY messages on both sockets.
             */
            
            System.out.println(sockOneIn.readUTF());
            System.out.println(sockTwoIn.readUTF());
                              
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
    
    private void processOneTurn(DataInputStream offenseIn, DataOutputStream offenseOut,
                                DataInputStream defenseIn, DataOutputStream defenseOut) {
        
        try {
            //get a guess from the attacking player
            int x = offenseIn.readInt();
            int y = offenseIn.readInt();
            
            //send the guess to the opponent
            defenseOut.writeUTF(ATTACK);
            defenseOut.writeInt(x);
            defenseOut.writeInt(y);
            
            //get the result from the opponent
            int result = defenseIn.readInt();
            
            //send the result back to the attacking player, with the original guess
            offenseOut.writeUTF(RESULT);
            offenseOut.writeInt(result);
            offenseOut.writeInt(x);
            offenseOut.writeInt(y);
            
            //wait for the attacking player to finish processing the result
            //and then let the opponent know it can now take a turn
            if(offenseIn.readUTF().equals(CLIENT_READY))
                defenseOut.writeUTF(CLIENT_READY);
            
            //if attacking player won the game, the server will try and 
            //set up a new game
            
            if(result == ALL_SHIPS_SUNK) {
                
                //make sure both players want to play again
                String p1reply = offenseIn.readLine();
                String p2reply = defenseIn.readLine();
                
                if(p1reply.equals(PLAY_AGAIN) && p2reply.equals(PLAY_AGAIN)) {
                    offenseOut.writeUTF(PLAY_AGAIN);
                    defenseOut.writeUTF(PLAY_AGAIN);
                    offenseOut.writeUTF(DEFENSE);
                    defenseOut.writeUTF(OFFENSE);
                    System.out.println(offenseIn.readUTF());
                    System.out.println(defenseIn.readUTF());
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
