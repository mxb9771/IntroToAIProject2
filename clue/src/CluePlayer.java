/*
 * CluePlayer.java
 */

import java.util.*;

/** 
 * This class contains player strategies for the game of Clue. 
 *
 * @author     Matthew Bollinger
 *
 */

class GameException extends Exception {

    public GameException(String message){
        super(message);
    }

}

public class CluePlayer {

    //WORLD DATA FOR SMART PLAYER TO USE
    private String[] allSuspects = {"Scarlet","Mustard","White","Peacock",
            "Green","Plum"};
    private String[] allWeapons = {"Rope","Revolver","Wrench","Pipe",
            "Candlestick","Knife"};
    private boolean rushLounge = true;
    private final int[] doorLocations =
            {
                    4,3,//KitchenDoor
                    4,6,//BallroomDoors
                    5,7,
                    5,10,
                    4,11,
                    5,14,//ConservatoryDoor
                    12,3,//DinningRoomDoors
                    9,4,
                    7,14,//BilliardRoomDoor
                    9,16,
                    12,15,//LibraryDoor
                    13,14,
                  //16,4,//LoungeDoor (commented out because we should have ran rush Lounge
                    16,11,//HallwayDoors
                    15,9,
                    17,14,//StudyDoor
            };


    /**
     *  Find a random square on the board for a player to move to.
     *
     *  @return                  The square that the player ends up on  
     */
    public Square findSquareRand() {

        int row = 0, col = 0;
        boolean valid = false;
        
        while (!valid) {
            col = (int)(Math.random()*(Clue.board.WIDTH)) + 1;
            row = (int)(Math.random()*(Clue.board.HEIGHT)) + 1;
            if (col >= 0 && col < Clue.board.WIDTH && 
                row >= 2 && row < Clue.board.HEIGHT)
                valid = true;
        }  
        return new Square(row, col);
    }

    /**
     *  Find a square on the board for a player to move to by rolling the 
     *  die and chosing a random direction. The square that is chosen must
     *  be legally accessible to the player (i.e., the player must adhere to 
     *  the rules of the game to be there).
     *
     *  @param    c_row          The current row of this player
     *  @param    c_col          The current column of this player
     *
     *  @return                  The square that this player ends up on
     */
    public Square findSquareRandDir(int c_row, int c_col) {

        int moveSize = Clue.die;
        int new_row = c_row;
        int new_col = c_col;
        int moves = 0;
        int tries = 0;
        Map<Integer, Integer> visited = new TreeMap<>();
        while (moves < moveSize) {
            int currentSpot = Integer.parseInt("" + c_row + c_col);
            visited.put(currentSpot, 1);
            tries++;
            if (tries == 969){
                System.out.println("here");
            }
            if (tries > 1000) try {
                throw new GameException("too many random move tries!");
            } catch (GameException e) {
                e.printStackTrace();
            }
            char direction = randomDirection();
            int[] spaceType;
            switch (direction) {
                case 'n' -> spaceType = getSpaceType(new_row - 1, new_col);
                case 'e' -> spaceType = getSpaceType(new_row, new_col + 1);
                case 's' -> spaceType = getSpaceType(new_row + 1, new_col);
                case 'w' -> spaceType = getSpaceType(new_row, new_col - 1);
                default -> {
                    continue;
                }
            }
            if (spaceType[0] == 0){ //valid move
                int testPos = Integer.parseInt("" + spaceType[1] + spaceType[2]);
                if (visited.containsKey(testPos)) continue;
                moves += 1;
                new_row = spaceType[1];
                new_col = spaceType[2];
            }
            else if(spaceType[0] == 1){ //doorway hit
                return new Square(spaceType[1], spaceType[2]);
            }
        }
        return new Square(new_row, new_col);
    }


    /*

     */
    private int[] getSpaceType(int row, int col) {
        //-1 invalid space, 0 valid move, 1 room/doorway;
        if (col >= 0 && col < Board.WIDTH &&
                row >= 2 && row < Board.HEIGHT) {
            String room = Clue.board.getRoom(row, col);
            String color = Clue.board.getColor(row, col);
            if(color.equals("None") && room.equals(" ")){
                return new int[]{0, row, col};
            }
            if (room.equals("Center")){
                return new int[] {-1, row, col};
            }
            if (color.equals("Gray") && Clue.board.isHallwayDoor(row, col)) {
                return new int[] {1, row, col};
            }
        }
        return new int[]{-1, row, col};
    }


    /*

     */
    private char randomDirection(){
        char[] directions = {'n', 'e', 's', 'w'};
        return directions[(int)(Math.random() * 4)];
    }


    /**
     *  Find a square on the board for a player to move to by rolling the 
     *  die and chosing a good direction. The square that is chosen must
     *  be legally accessible to the player (i.e., the player must adhere to 
     *  the rules of the game to be there).
     *
     *  @param    c_row          The current row of this player
     *  @param    c_col          The current column of this player
     *  @param    notes          The Detective Notes of this player 
     *
     *  @return                  The square that this player ends up on
     */
    public Square findSquareSmart(int c_row, int c_col, DetectiveNotes notes) {
        ArrayList<String> rooms = notes.getMyRooms();
        int new_row = c_row;
        int new_col = c_col;
        int moveLength = Clue.die;
        int moves = 0;
        Target nextTarget = null;
        while (moves < moveLength){
            if (rushLounge){ //rush the Lounge (the closest room)
                if (rooms.contains("Lounge")){
                    rushLounge = false;
                    nextTarget = findNextTarget(new_row, new_col, rooms);//rushLounge start done; find next door
                    continue;
                }
                if (new_row > 16){
                    new_row--;
                    moves++;
                }
                else if (new_row == 16) {
                    new_col--;
                    moves++;
                    if (!Clue.board.isLoungeDoor(new_row, new_col)) {
                        try {
                            throw new GameException("STRATEGY RUSH LOUNGE FAILED");
                        } catch (GameException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        rushLounge = false;
                        return new Square(new_row, new_col);
                    }
                }
            }
            else { //now we will use nextTarget to goto next closest room
                if (nextTarget == null || rooms.contains(nextTarget.room)) nextTarget = findNextTarget(new_row, new_col, rooms);
                if (new_row == nextTarget.row && new_col == nextTarget.column) return new Square(new_row, new_col);
                if (moveTowardsRow(new_row, new_col, nextTarget)){
                    if (new_row < nextTarget.row){
                        moves++;
                        new_row++;
                        continue;
                    }
                    else {
                        moves++;
                        new_row--;
                        continue;
                    }
                }
                if (moveTowardsCol(new_row, new_col, nextTarget)){
                    if (new_col < nextTarget.column){
                        moves++;
                        new_col++;
                        continue;
                    }
                    else {
                        moves++;
                        new_col--;
                        continue;
                    }
                }
                else{
                    System.out.println("WARNING SCARLET USING RANDOM MOVE");
                    if(!moveTowardsCol(new_row, new_col, nextTarget) && !moveTowardsRow(new_row, new_col, nextTarget))
                    return findSquareRandDir(c_row, c_col);

                }
            }
        }
        return new Square(new_row, new_col);
    }

    /*
    private booleans says if it's possible to move towards the
    passed target's column
 */
    private boolean moveTowardsCol(int row, int col, Target t){
        if (col < t.column) return getSpaceType(row, col + 1)[0] != -1;
        else if (col > t.column) return getSpaceType(row, col - 1)[0] != -1;
        return false;
    }

    /*
    private booleans says if it's possible to move towards the
    passed target's row
     */
    private boolean moveTowardsRow(int row, int col, Target t){
        if (row < t.row) return getSpaceType(row + 1, col)[0] != -1;
        else if (row > t.row) return getSpaceType(row - 1, col)[0] != -1;
        return false;
    }


    /*
    Returns the location of the next closest door of a room
    NOT in the players current notes when passed the players
    current location
     */
    private Target findNextTarget(int row, int col, List<String> rooms){
        List<Target> targets = new ArrayList<>();
        for(int i = 0; i < doorLocations.length; i+= 2){
            int doorRow = doorLocations[i];
            int doorCol = doorLocations[i + 1];
            int rowDiff = doorRow - row;
            int colDiff = doorCol - col;
            double distance = Math.sqrt((rowDiff*rowDiff) + (colDiff*colDiff));

            Target t = new Target(doorRow, doorCol, distance);
            targets.add(t);
        }
        Target currentTarget = null;
        for (Target t: targets){
            if (currentTarget == null) {
                currentTarget = t;
            }
            else if (t.distance < currentTarget.distance && !rooms.contains(t.room)) currentTarget = t;
        }
        return currentTarget;
    }


    /**
     *  Move to a legal square on the board. If the move lands on a door,
     *  make a suggestion by guessing a random suspect and random weapon.
     *
     *  @param    curr_row        The row of the player before move
     *  @param    curr_column     The column of the player before move
     *  @param    row             Selected row 
     *  @param    column          Selected column 
     *  @param    color           Player color
     *  @param    name            Player name
     *  @param    notes           Player Detective Notes 
     *
     *  @return                   A suggestion -> [name,room,suspect,weapon]
     */
    public String[] moveNaive(int curr_row, int curr_column, 
                         int row, int column, String color, String name, 
                         DetectiveNotes notes) {

	String [] retVal = new String[4];
        String suspect = notes.getRandomSuspect();
	String weapon = notes.getRandomWeapon();
        String room = Clue.board.getRoom(row,column);

        if (Clue.board.isDoor(curr_row,curr_column))
            Clue.board.setColor(curr_row,curr_column,"Gray");
        else
            Clue.board.setColor(curr_row, curr_column, "None");

        if (Clue.board.isDoor(row,column)) {
            retVal[0] = name;
            retVal[1] = room;
            retVal[2] = suspect;
            retVal[3] = weapon;

            if (Clue.gui) {
                System.out.print(name+" suggests that the crime was committed");
                System.out.println(" in the " + room + " by " + suspect +
                               " with the " + weapon);
            }
        }
        else retVal = null;

        Clue.board.setColor(row,column,color);

	return retVal;
    }

    /**
     *  Move to a legal square on the board. If the move lands on a door,
     *  make a good suggestion for the suspect and the weapon. A good
     *  suggestion here is one which does not include any suspects or
     *  weapons that are already in the Detective Notes of this player.
     *
     *  @param    curr_row        The row of the player before move
     *  @param    curr_column     The column of the player before move
     *  @param    row             Selected row 
     *  @param    column          Selected column 
     *  @param    color           Player color
     *  @param    name            Player name
     *  @param    notes           Player Detective Notes 
     *
     *  @return                   A suggestion -> [name,room,suspect,weapon]
     */
    public String[] moveSmart(int curr_row, int curr_column, 
                         int row, int column, String color, String name, 
                         DetectiveNotes notes) {
        String suspect = null;
        String weapon = null;
        String [] retVal = new String[4];

        if (Clue.board.isDoor(curr_row,curr_column))
            Clue.board.setColor(curr_row,curr_column,"Gray");
        else
            Clue.board.setColor(curr_row, curr_column, "None");

        if (Clue.board.isDoor(row,column)) {
            for (int i = 0; i < 6; i++){
                if (suspect == null && !notes.getMySuspects().contains(allSuspects[i])) suspect = allSuspects[i];
                if (weapon == null && !notes.getMyWeapons().contains(allWeapons[i])) weapon = allWeapons[i];
            }
            String room = Clue.board.getRoom(row, column);
            retVal[0] = name;
            retVal[1] = room;
            retVal[2] = suspect;
            retVal[3] = weapon;

            if (Clue.gui) {
                System.out.print(name+" suggests that the crime was committed");
                System.out.println(" in the " + room + " by " + suspect +
                        " with the " + weapon);
            }
        }
        else retVal = null;

        Clue.board.setColor(row,column,color);

        return retVal;
    }
    
    /**
     *  Try to prove a suggestion is false by asking the players, in a
     *  round-robin fashion, to show the suggester one of the suggestions if
     *  the player has that suggested card in their hand. The other players 
     *  know that ONE of the suggestions cannot be in the case file, but they 
     *  do not know which one.
     *
     *  @param  suggestion      A suggestion -> [name,room,suspect,weapon]
     *  @param  notes           The Detective Notes of the current player
     *  @param  player          The current player
     *  @param  next            The next player clockwise around the board
     *  
     *  @return                 An accusation, to check if it is a winner
     *
     */
    public ArrayList<String> prove(String[] suggestion, DetectiveNotes notes,
                         int player, int next) {
        
        String card;
        boolean found = false;
        ArrayList<String> accusation = new ArrayList<String>();

        // Ask the other 5 players to show one of the suggested cards
        // YOUR CODE GOES HERE
        for(int x = 0; x < 5; x++){
            System.out.println( x + " prove accuser wrong");
            int nextPlayer = x + next;
            if (nextPlayer > 5) nextPlayer = nextPlayer - 6;
        // Make an accusation
        if (!found) {
            // Check this player's cards to see if this player has them
            for (int i = 0; i < 3; i++) {
                card = (String) Arrays.asList(
                        (Clue.allCards.get(nextPlayer)).keySet().toArray()).get(i);

                for (int k = 1; k <= 3; k++)
                    if (!found && card.equals(suggestion[k])) {
                        found = true;
                        System.out.println("PROVEN by" + nextPlayer);
                        if (Clue.cardType(card).equals("suspect")) notes.addSuspect(card);
                        else if(Clue.cardType(card).equals("weapon")) notes.addWeapon(card);
                        else if(Clue.cardType(card).equals("room")) notes.addRoom(card);
                        break;
                    }
            }
        }
        }
        // If still not found, I do believe I have won the game!
        for (int i=1; i<4; i++)
            if (!found)
                accusation.add(suggestion[i]);
            else
                accusation.add("None");
        
        return accusation;
    }

    /**
     *  Update this player's detective notes upon learning some information.
     *
     *  @param    notes    The detective notes of this player
     *  @param    card     The card that caused the change
     *  @param    type     The type of the card - suspect, weapon, or room
     *
     */
    public void setNotes(DetectiveNotes notes, String card, String type) {

        if (type.equals("suspect"))
            notes.addSuspect(card);
        else if (type.equals("weapon"))
            notes.addWeapon(card);
        else if (type.equals("room"))
            notes.addRoom(card);
    }
}


class Target{
    public final int row,column;
    public final double distance;
    public String room;
    Target(int row, int column, double distance) {
        this.row = row;
        this.column = column;
        this.distance = distance;
        this.room = Clue.board.getRoom(row, column);
    }
}
