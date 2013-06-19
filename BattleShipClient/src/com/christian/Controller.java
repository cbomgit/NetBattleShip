package com.christian;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Christian
 */
class Controller {
          
    private GameBoard               board;
    private User                    player;
    
    
    //socket and io streams needed to talk to server
    private Socket                  server;
    private PrintWriter             out;
    private BufferedReader          in;
    private SocketWorker            socketWorker;
    
    private String                  controlFlag;
       
    private static final String     CLIENT_READY      =  "Ready to play";    
    
    private static final String     ATTACK            =  "Attack";
    private static final String     RESULT            =  "Result";
    
    private static final String     GOODBYE           =  "Goodbye";
    private static final String     PLAY_AGAIN        =  "Play again?";
    
    public  static final String     GO_FIRST          =  "Go first";
    public  static final String     GO_SECOND         =  "Go second";
    
    public  static final int        DEFAULT_GRID_SIZE = 11;
    
    public Controller(String hostName, String control, Socket socket, GameBoard b){
        
        player = new User(hostName, DEFAULT_GRID_SIZE);
        board = b;                       
        board.addListenerToShipGrid(new BoardConfigController());
        controlFlag = control;
        
        server = socket;
        
        try {
            out = new PrintWriter(server.getOutputStream(), true);
            in  = new BufferedReader(
                        new InputStreamReader(server.getInputStream()));
            
            //exchange user names 
            out.println(player.getUserName());
            String opponent = in.readLine();
            
            board.setOpponentName(opponent);
            board.toConsole(opponent + " wants to play.");
        }
        catch(IOException e) {
            System.out.println("Could not open Socket IO stream");
            System.out.println(e.toString());
        }
        
        
    }
    
    private class MainGameController extends MouseAdapter{
        
        
        @Override
        public void mouseClicked(MouseEvent event) {
            
            final Cell target = board.getCoordinatesOfMouseClick(event);

            /* must verify that user has not clicked on a square that has 
             * already been marked as a hit or a miss
             */
            if(player.verifyNewTarget(target)){
                
                /*first disable the board so that the user doesn't overload
                * the server with requests
                */
                
                board.toggleBoard(false);
                
                //send the guess to the opponent. 
                out.println(target.x);
                out.println(target.y);
                
                //the socketinputthread should handle the rest
            }
        }
    }

    private class BoardConfigController extends MouseAdapter{
        
        /* the purpose of this listener is to allow the user to allocate 
         * his ships to his own board. As the user moves the mouse around
         * on the board, the listener will display a preview of the ship in
         * both directions. The user will left click for a vertical
         * ship and right for a horizontal ship. 
         */

        private boolean verticalShipPreviewSet;
        private boolean horizontalShipPreviewSet;
        private int     whichShip;
        
        public BoardConfigController() {
            verticalShipPreviewSet = horizontalShipPreviewSet = false;
            whichShip = 0;
        }
        
        @Override
         public void mouseClicked(MouseEvent e) {
            
            Cell target = board.getCoordinatesOfMouseClick(e);
            int shipSize = player.getShipSize(whichShip);
            
            int direction = e.getButton() == MouseEvent.BUTTON1 ? 
                    Ship.VERTICAL : Ship.HORIZONTAL;
            
            if(direction == Ship.VERTICAL && verticalShipPreviewSet){
                
                if(horizontalShipPreviewSet)
                    board.unpaintHorizontalShip
                        (new Cell(target.x + 1, target.y), shipSize - 1);
                player.setVerticalShip(target, whichShip);
                
                board.toConsole("You set your " + Ship.shipName(player.getShipSize(whichShip)));
                whichShip++;
                verticalShipPreviewSet = horizontalShipPreviewSet = false;
                
                
            }
            else if(direction == Ship.HORIZONTAL && horizontalShipPreviewSet){
                
                if(verticalShipPreviewSet)
                    board.unpaintVerticalShip
                        (new Cell(target.x, target.y + 1), shipSize - 1);
                player.setHorizontalShip(target, whichShip);
                
                board.toConsole("You set your " + Ship.shipName(player.getShipSize(whichShip)));
                whichShip++;
                verticalShipPreviewSet = horizontalShipPreviewSet =false;

            }
            

            //gives user option to reconfigure ships or begin play
            if(whichShip == 5){
               
                int option = giveOptionToReconfigureShips();
                
                if(option == JOptionPane.YES_OPTION) {
                    whichShip = 0;
                    board.setMainController(this, new MainGameController());
                    
                    //let the server know we are ready
                    out.println(CLIENT_READY);
                    (socketWorker = new SocketWorker()).execute();
                    
                    
                    if(controlFlag.equals(GO_SECOND)) {
                        board.toConsole("Waiting for opponent to go first...");
                        board.toggleBoard(false);
                    }
                    else
                        board.toConsole("You go first.");
                }
                else{
                    board.clearTheBoard();
                    player.clearShipGrid();
                    whichShip = 0;
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            
            Cell target = board.getCoordinatesOfMouseClick(e);
            
            int shipSize = player.getShipSize(whichShip);

            if(player.canSetVerticalShip(target, shipSize)){
                board.paintVerticalShip(target, shipSize);
                verticalShipPreviewSet = true;
            }
            if(player.canSetHorizontalShip(target, shipSize)){
                board.paintHorizontalShip(target, shipSize);
                horizontalShipPreviewSet = true;
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Cell target = board.getCoordinatesOfMouseClick(e);
            
            int shipSize = player.getShipSize(whichShip);

            if(verticalShipPreviewSet)
                board.unpaintVerticalShip(target, shipSize);

            if(horizontalShipPreviewSet)
                board.unpaintHorizontalShip(target, shipSize);

            horizontalShipPreviewSet = verticalShipPreviewSet = false;
            
        }
        
        //show menu that prompts user to confirm placement of ships or restart set
        //up
        private int giveOptionToReconfigureShips(){

           Object [] options = {"Go to War!", "Re-deploy"};
           
           return JOptionPane.showOptionDialog(null, 
                                               "Deployment complete?", 
                                               "Set-up menu", 
                                               JOptionPane.YES_NO_OPTION, 
                                               JOptionPane.QUESTION_MESSAGE, 
                                               null, options, null);
        }
        
    }
    
    private class SocketWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            boolean playing = true;
            
            while(playing) {
            
            try {
                
                
                String messageType = in.readLine();
                System.out.println(messageType);
                
                if(messageType.equals(ATTACK)) {
                    
                    Cell target = new Cell(Integer.parseInt(in.readLine()),
                                           Integer.parseInt(in.readLine()));                    
                                         
                    int result = player.opponentGuessedHere(target);
                    board.updateShipGrid(result, target);
                    
                    out.println(result);
                                        
                    if(result == Player.ALL_SHIPS_SUNK)
                        promptForSecondGame(false);
                }
                else if(messageType.equals(RESULT)) {
                    
                    int result = Integer.parseInt(in.readLine());
                    
                    Cell target = new Cell(Integer.parseInt(in.readLine()),
                                           Integer.parseInt(in.readLine()));
                    
                    player.processResult(result, target);
                    board.updateHitMissGrid(result, target);
                    
                    /* let the server know we are done processing our results.
                     * Opponent can now take a turn.
                     */
                    out.println(CLIENT_READY);
                    
                    if(result == Player.ALL_SHIPS_SUNK)
                        promptForSecondGame(true);
                }
                else if(messageType.equals(GOODBYE)) {
                    cleanUpAndExit();
                }
                else if(messageType.equals(PLAY_AGAIN)) {
                    wantsToPlayAgain();
                }
                else if(messageType.equals(CLIENT_READY))
                    board.toggleBoard(true);
                
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
            return null;
        }
        
        
    }
    
    //game is over.User can decide to ext app or begin a new game
    public void promptForSecondGame(boolean whoWon) throws IOException{
       
      String message = whoWon ? "You win!!" : "You Lose!";
      Object [] options = {"Play Again", "Quit"};

      int option = JOptionPane.showOptionDialog(null, message, "Game Over", 
                                                JOptionPane.YES_NO_OPTION, 
                                                JOptionPane.QUESTION_MESSAGE,
                                                null, options, null);

      if(option == JOptionPane.YES_OPTION) {
          out.println(PLAY_AGAIN);
          board.toConsole("Waiting for opponent to confirm second game...");
      }
      else
          sendGoodByeMessage();
     
    }
    
    private void wantsToPlayAgain() {
        board.toConsole("Starting another game.");
        board.clearTheBoard();
        board.setBoardConfigController(new BoardConfigController());
        player = new User(player.getUserName(), player.getGridSize());
    }
    
    private void sendGoodByeMessage() {
        
        out.println(GOODBYE);
        board.toConsole("A player closed the connecttion. Goodbye.");
        cleanUpAndExit();
    }
    
    private void cleanUpAndExit() {
        
        try {
            
            in.close();
            out.close();
            server.close();
            board.dispose();
            
            BattleShipClient.showNetworkSettingsDialog(new BattleShipClient());
        }
        catch(IOException e) {
            System.out.println(e.toString());
        }
    }
}
