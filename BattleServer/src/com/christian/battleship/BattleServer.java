/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian.battleship;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author christian
 */
public class BattleServer {

    private static final int CLIENT_TIMEOUT      =    30000;
    private static final int ERROR_CODE          =    -1;
    private static final int WAITING_CODE        =     0;
    private static final int ALL_OPPONENTS_READY =     1;
    
    private static final int PORT                = 17000;
    
    private static ArrayList<GameThread> games;
    private static ServerSocket          serverSocket;
    private static boolean               listening;
    
    static {
        
        games = new ArrayList<GameThread>();
        serverSocket = null;
        listening = true;
        
    }
    
    private static void startAdminThread() {
        
       new AdminThread().start();
    }
    
    private static void runServer() throws IOException {

        listening = true;
        
        try {
            serverSocket = new ServerSocket(PORT);
            
        }
        catch(IOException e) {
            System.out.println("Could not listen on port 17000");
            System.exit(-1);
        }
        
        Socket socketOne = null;
        Socket socketTwo = null;
        DataOutputStream socketOneOut = null;
        DataOutputStream socketTwoOut = null;
        
        while(listening) {
            
            System.out.println("Listening on port " + PORT);
            //accept a connection from a client
            socketOne = serverSocket.accept();
            
            System.out.println("\nConnected to client:");
            System.out.println(socketOne.getInetAddress());
            System.out.println(socketOne.getPort());
            
            //we'll want to communicate with the connected client
            socketOneOut = new DataOutputStream(socketOne.getOutputStream());
            socketOneOut.writeInt(WAITING_CODE);
            
            //set a timeout for the opponent to connect
            serverSocket.setSoTimeout(CLIENT_TIMEOUT); //only 
            
            try {
                //wait for a connection from the opponent
                socketTwo = serverSocket.accept();
                socketTwoOut = new DataOutputStream(socketTwo.getOutputStream());
                
                System.out.println("\nConnected to client:");
                System.out.println(socketTwo.getInetAddress());
                System.out.println(socketTwo.getPort());
               
                
                socketOneOut.writeInt(ALL_OPPONENTS_READY);
                socketTwoOut.writeInt(ALL_OPPONENTS_READY);
                
                
                //since we have two clients, we can start a game
                GameThread newGame = new GameThread(games.size(), socketOne, socketTwo);   
                games.add(newGame);
                newGame.start();
                System.out.println("Started a game.Going back to listening now");
                
            }
            catch(SocketTimeoutException e) {
                
                //send message to client that connection to opponent timed out
                socketOneOut.writeInt(ERROR_CODE);
                socketOneOut.writeUTF("Timed out waiting for opponent.\n");
                socketOneOut.close();
                if(socketOne != null)
                    socketOne.close(); //close the socket
            }
            
            serverSocket.setSoTimeout(0);
        }
        
        serverSocket.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        
        startAdminThread();
        
        try {
            runServer();
        }
        catch(IOException e) {
            if(e instanceof SocketException)
                System.out.println("Connection to server was closed. Goodbye");
        }
        
        
    }
    
    private static void kill(Scanner in) {
        
        if(games.isEmpty())
            System.out.println("No games are running.");
        else {
            int gameId = -1;
            if(in.hasNextInt())
                gameId = in.nextInt();
            
            if(gameId >= 0 && gameId < games.size())
                killGame(gameId);
            else
                System.out.println("Could not kill game thread with id " + gameId);
        }
    }
    
    public static void killGame(int i)  {
                                
        try { 
            (games.get(i)).kill();
            (games.get(i)).join();
            games.remove(i);
        }
        catch(InterruptedException e) { 
            
        }
        catch(IOException e) {
            System.out.println(e);
        }
    }
    
    public static void ls() {
        System.out.println(games.size() + " game(s) currently running");
        
        for(GameThread game : games) {
            System.out.println(game);
        }
    }
    
    private static class AdminThread extends Thread {
        
        @Override
        public void run() {
            Scanner in = new Scanner(System.in);
            String input = "";
            
            System.out.print("BattleShipServer@localhost $ ");
            if(in.hasNextLine())
                input = in.nextLine();
            
            while(!"exit".equals(input)) {
                
                if(input.equals("ls"))
                    ls();
                else if(input.equals("kill")) {
                    ls();
                    System.out.println("Enter id of game to kill: ");
                    kill(in);
                }
                
                System.out.print(" BattleShipServer@localhost $ ");
                if(in.hasNextLine())
                    input = in.nextLine();
            }
            
            try {
                serverSocket.close();
            }
            catch(IOException e) {
                e.toString();
            }
        }
    }
}
