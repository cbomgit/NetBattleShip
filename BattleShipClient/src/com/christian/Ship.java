/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.christian;

/**
 *
 * @author Christian
 */
class Ship {

   private int hitsLeft;
   private int size;
   
   public static final int AIRCRAFT_CARRIER = 5;
   public static final int DESTROYER = 4;
   public static final int BATTLESHIP = 3;
   public static final int SUBMARINE = 3;
   public static final int PATROL_BOAT = 2;

   public static final int VERTICAL = 0;
   public static final int HORIZONTAL = 1;

   Ship(int shipLives){

       hitsLeft = size = shipLives;
   }

   public int size(){
       return size;
   }
   
   boolean takeDamage(){

       if(--hitsLeft == 0)
          return true;
       
       return false;
   }

   public static String shipName(int size){

      if(size == AIRCRAFT_CARRIER)
          return "Aircraft Carrier";
      else if(size == DESTROYER)
          return "Destroyer";
      else if(size == SUBMARINE)
          return "Submarine or Battleship";
      else
          return "Patrol Boat";
   }
}