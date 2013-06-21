
package com.christian;

/**
 *
 * @author Christian
 */
abstract class Player {

   private   Ship [][] shipGrid;
   private   int       gridSize; // size of the grid
   
   private   int       shipsActive;
   protected Ship []   fleet;//represents users own ships
   protected int  [][] resultsGrid;//keep track of hits/misses/ships sunk
   
   

   public static final int ALL_SHIPS_SUNK = -4;
   public static final int SHIP_SUNK = -3;
   public static final int UNKNOWN = 0;
   public static final int MISS = -1;
   public static final int HIT = -2;

   /**
    * 
    * @param theSize size of the grid
    */
   Player(int theSize){

       gridSize = theSize;
       shipsActive = 5;
       resultsGrid = new int[theSize][theSize];
       shipGrid = new Ship[theSize][theSize];
       fleet = new Ship[5];

       for(int y = 0; y < theSize; y++){
           for(int x = 0; x < theSize; x++){
               resultsGrid[x][y] = UNKNOWN;
               shipGrid[x][y] = null;
           }
       }

       createShips();
   }
   
   public static String resultString(int resultCode) {
       
       switch (resultCode){
           
           case ALL_SHIPS_SUNK : 
               return "All ships sunk";
           case SHIP_SUNK : 
               return "Ship sunk";
           case HIT : 
               return "Hit";
           case MISS :
               return "Miss";
           default :
               return "Default";
       }
   }

   /**
    * create the player's ships.
    */
   private void createShips(){

       fleet[0] = new Ship(Ship.AIRCRAFT_CARRIER);
       fleet[1] = new Ship(Ship.DESTROYER);
       fleet[2] = new Ship(Ship.BATTLESHIP);
       fleet[3] = new Ship(Ship.SUBMARINE);
       fleet[4] = new Ship(Ship.PATROL_BOAT);

   }
   
   /**
    * @return size of the grid 
    */
   public int getGridSize(){
      return gridSize;
   }

   /**
    * Returns the result of an opponents guess.
    * @param x x coordinate of guess.
    * @param y y coordinate of guess.
    * @return result of guess. 
    */
   
   public int opponentGuessedHere(Cell guess) {

        if(shipGrid[guess.x][guess.y] == null)
           return MISS;
        else {
           if(shipGrid[guess.x][guess.y].takeDamage()){
               if(--shipsActive == 0)
                   return ALL_SHIPS_SUNK;
               else
                   return shipGrid[guess.x][guess.y].size();
           }

           return HIT;
        }
   }

   /**
    * clears the ship grid.
    */
   public void clearShipGrid(){
       for(int y = 0; y < gridSize; y++)
            for(int x = 0; x < gridSize; x++)
                shipGrid[x][y] = null;
   }
   
   /**
    * Method to add a ship to User's grid
    * @param target.x row where ship will be placed
    * @param target.y column where ship will be placed
    * @param whichShip index of fleet [] identifies ship to be allocated
    */
   public void setHorizontalShip(Cell target, int whichShip) {

        for (int i = 0; i < fleet[whichShip].size(); i++) {
            shipGrid[target.x + i][target.y] = fleet[whichShip];
        }
    }
   
   /**
    * Same as setHorizontalShip, but for opposite direction
    * @param target.x row where ship will be placed
    * @param target.y column where ship will be place
    * @param whichShip index of fleet [] identifies ship to be allocated
    */
   public void setVerticalShip(Cell target, int whichShip) {

        for (int i = 0; i < fleet[whichShip].size(); i++) 
            shipGrid[target.x][target.y + i] = fleet[whichShip];
        
   }
   
   /**
    * assert that a ship can be place on grid without violating and
    * of the game board constraints (ie not out of bounds, overlapping, etc).
    * @param target.x start x of ship.
    * @param target.y start y of ship.
    * @param size size of ship in question.
    * @return true if ship can be set, otherwise false.
    */
    public boolean canSetHorizontalShip(Cell target, int size) {

        if(target.x + size > gridSize)
            return false;

        for(int i = 0; i < size; i++)
            if(shipGrid[target.x + i][target.y] != null)
                return false;

        return true;
    }

    /**
    * assert that a ship can be place on grid without violating and
    * of the game board constraints (ie not out of bounds, overlapping, etc).
    * @param target.x start x of ship.
    * @param target.y start y of ship.
    * @param size size of ship in question.
    * @return true if ship can be set, otherwise false.
    */
    public boolean canSetVerticalShip(Cell target, int size) {

        if(target.y + size > gridSize)
            return false;

        for(int i = 0; i < size; i++)
            if(shipGrid[target.x][target.y + i] != null)
                return false;

        return true;
    }
    
    /**
     * assert that Player has not guessed at a location already
     * @param x x coordinate of cell in question
     * @param y y coordinate of cell in question
     * @return true if (x, y) is unexplored, otherwise false
     */

    public boolean verifyNewTarget(Cell target){
       return resultsGrid[target.x][target.y] == UNKNOWN;
    }
    
    /**
     * Abstract method to process the result of a guess.
     * @param result result of guess
     * @param x x coordinate of guess
     * @param y y coordinate of guess
     */
    public abstract void processResult(int result, Cell lastAttempt);
}