
package com.christian;

/**
 *
 * @author Christian
 */

 class User extends Player {
    
   
    User(int theSize){
        super(theSize);
    }
   
    //updates the users hit/miss grid with the results of the 
    //last attempt.
   @Override
    public void processResult(int result, Cell lastAttempt) {
        resultsGrid[lastAttempt.x][lastAttempt.y] = result;
    }
   
   public int getShipSize(int whichShip){
        return fleet[whichShip].size();
   }
   
}
