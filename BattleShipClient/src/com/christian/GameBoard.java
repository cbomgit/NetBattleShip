/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
/**
 *
 * @author Christian
 */
class GameBoard extends JFrame{
   
    private static final String SETUP_INSTRUCTION = "Left-Click for vertical ship.\n"
            + "Right-Click for horizontal ship.";
    
    private GridButton [][] shipGrid; //display User's ships
    private GridButton [][] hitMissGrid;//display users guesses
    private JTextArea       console;
    private int             gridSize;
    private JScrollPane     consoleScrollPane;
    
    public GameBoard(int theSize){
        
        gridSize = theSize;        
        createGameBoard();
        layoutTheBoard();
    }
    
    //disables the users ship grid and enables the opponent grid
    //listener is removed from ship grid and a new listener is registered to the 
    //shot grid
    public void setMainController(MouseListener old, MouseListener newListener){
                
        for(int y = 0; y < gridSize; y++)
           for(int x = 0; x < gridSize; x++){

              shipGrid[x][y].removeMouseListener(old);
              shipGrid[x][y].setEnabled(false);
              hitMissGrid[x][y].setEnabled(true);
              hitMissGrid[x][y].addMouseListener(newListener);
           }
    }
    
    public void toggleBoard(boolean toggleVal) {
        
        for(int y = 0; y < gridSize; y++)
            for(int x = 0; x < gridSize; x++) 
                hitMissGrid[x][y].setEnabled(toggleVal);
    }
    
    //cretes the objects for an empty game board
    private void createGameBoard(){
        
        shipGrid = new GridButton[gridSize][gridSize];
        hitMissGrid = new GridButton[gridSize][gridSize];
        
        for(int y = 0; y < gridSize; y++)
            for(int x = 0; x < gridSize; x++){
                shipGrid[x][y] = new GridButton(x, y);
                shipGrid[x][y].setEnabled(true);
                shipGrid[x][y].setBackground(Color.BLACK);
                hitMissGrid[x][y] = new GridButton(x, y);
                hitMissGrid[x][y].setEnabled(false);
                hitMissGrid[x][y].setBackground(Color.BLACK);
            }
        
        createConsoleArea();
    }
    
    private void createConsoleArea() {
        
        consoleScrollPane = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();

        console.setEditable(false);
        console.setBackground(new java.awt.Color(0, 0, 0));
        console.setColumns(100);
        console.setFont(new java.awt.Font("Monospaced", 0, 13)); // NOI18N
        console.setForeground(new java.awt.Color(0, 255, 0));
        console.setRows(8);
        consoleScrollPane.setViewportView(console);
        consoleScrollPane.setAutoscrolls(true);
        consoleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        consoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
    
    //lays out the board        setResizable(false);

    private void layoutTheBoard(){
        
        JPanel shipGridPanel = new JPanel();
        shipGridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        shipGridPanel.setLayout(new GridLayout(gridSize, gridSize, 0, 0));
        
        JPanel shotGridPanel = new JPanel();
        shotGridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        shotGridPanel.setLayout(new GridLayout(gridSize, gridSize, 0, 0));
        
        for(int y = 0; y < 11; y++)
             for(int x = 0; x < 11; x++){
                shipGridPanel.add(shipGrid[x][y]);
                shotGridPanel.add(hitMissGrid[x][y]);
            }
       
        
        //panel where all sub-panels will be placed
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(shipGridPanel, BorderLayout.EAST);
        mainPanel.add(shotGridPanel, BorderLayout.WEST);
        mainPanel.add(consoleScrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
        setTitle("BattleShip");
        setSize(1100, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    //method to register a mouselistener with each grid button
    //each button contains a reference to the same listener
    public void addListenerToShipGrid(MouseListener m){
        for(int y = 0; y < gridSize; y++)
            for(int x = 0; x < gridSize; x++)
                shipGrid[x][y].addMouseListener(m);
    }
    
    /*methods for highlighting, setting and removing a ship from the users 
     * ship grid
     */
    public void paintVerticalShip(Cell ship, int size) {
        
        for(int i = 0; i < size; i++)
            shipGrid[ship.x][ship.y + i].setBackground(Color.GRAY);
    }
    
    public void paintHorizontalShip(Cell ship, int size) {
        
        for(int i = 0; i < size; i++)
            shipGrid[ship.x + i][ship.y].setBackground(Color.GRAY);
    }
    
    public void unpaintVerticalShip(Cell ship, int size){
        
        for(int i = 0; i < size; i++)
            shipGrid[ship.x][ship.y + i].setBackground(Color.BLACK);
    }
    
    public void unpaintHorizontalShip(Cell ship, int size){
        for(int i = 0; i < size; i++)
            shipGrid[ship.x + i][ship.y].setBackground(Color.BLACK);
    }

    //refresh view to indicate result of opponent guesses
    public void updateHitMissGrid(int result, Cell target) {
        
        if(result == User.MISS){
            hitMissGrid[target.x][target.y].setBackground(Color.WHITE);
            toConsole("You missed at " + target.x + ", " + target.y);
        }
        else{
            hitMissGrid[target.x][target.y].setBackground(Color.RED);
            if(result == Player.HIT)
                toConsole("You damaged CPU's ship at " + target.x + ", " + target.y);
            else
                toConsole("You sunk CPU's " + Ship.shipName(result));
        }
        
        
    }
    
    //refresh view to indicate result of opponent guesses
    public void updateShipGrid(int result, Cell target) {
        if(result == User.MISS){
            shipGrid[target.x][target.y].setBackground(Color.WHITE);
            toConsole("CPU missed at " + target.x + ", " + target.y);
        }
        else{
            shipGrid[target.x][target.y].setBackground(Color.RED);
            if(result == Player.HIT)
                toConsole("CPU damaged your ship at " + target.x + ", " + target.y);
            else
                toConsole("CPU sunk your " + Ship.shipName(result));
        }
        
        
        
    }
    
    public void toConsole(String output) {
        console.append(output + "\n");
    }

    //mouse event represents a mouse click on a grid. 
    //returns (x, y) location of click
    public Cell getCoordinatesOfMouseClick(MouseEvent e) {
        
        GridButton b = null;
        
        if(e.getComponent() instanceof GridButton)
            b = (GridButton) e.getComponent();
        
        return new Cell(b.x, b.y);
    }

    //erases the board
    public void clearTheBoard() {
        for(int y = 0; y < gridSize; y++)
            for(int x = 0; x < gridSize; x++){
                shipGrid[x][y].setBackground(Color.BLACK);
                hitMissGrid[x][y].setBackground(Color.BLACK);
            }
    }

   public void setBoardConfigController(MouseListener newListener) {
       
      MouseListener [] listeners = getMouseListeners();
      
      for(int y = 0; y < gridSize; y++)
         for(int x = 0; x < gridSize; x++){
             
            for(int i = 0; i < listeners.length; i++) 
                hitMissGrid[x][y].removeMouseListener(listeners[i]);
            
            hitMissGrid[x][y].setEnabled(false);
            shipGrid[x][y].setEnabled(true);
            shipGrid[x][y].addMouseListener(newListener);
         }   
      
      toConsole(SETUP_INSTRUCTION);
   }
    
    private static class GridButton extends JButton {
    
        public int x;
        public int y;
    
        GridButton(int a, int b){

            super();
            x = a;
            y = b;

        }
    }
}

