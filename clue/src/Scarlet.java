/*
 * Scarlet.java
 *
 */

import java.util.*;

/** 
 * This class represents a player who knows how to play the game of Clue. 
 *
 * @author Roxanne Canosa
 * @author T.J. Borrelli
 *
 */

public class Scarlet extends CluePlayer {  
    
    private int curr_row = 19;
    private int curr_column = 5;
    private HashMap<String,String> myCards = new HashMap<String,String>();
    private DetectiveNotes myNotes = new DetectiveNotes();
    
    /**
     *  This method implements a strategy for this player. A strategy consists
     *  of (1) finding a square to move to, (2) moving to that square and 
     *  making a suggestion if the move ends up in a room, and (3) trying to 
     *  prove the solution by using the suggestion.  
     *
     *  @return                 An accusation, to check if it is a winner. 
     */
    public ArrayList<String> strategy() {
                
        Square square = findSquareSmart(curr_row, curr_column, myNotes);
        System.out.println("Scarlet is moving to: " + square.getRow() + " " + square.getColumn());
        String[] suggestion = moveSmart(curr_row, curr_column, square.getRow(), 
                                   square.getColumn(),"Red","Scarlet",myNotes);
        ArrayList<String> accusation = null;
        if (suggestion!=null) accusation = prove(suggestion, myNotes, 
                                           Clue.SCARLET, Clue.MUSTARD);
        curr_row = square.getRow();
        curr_column = square.getColumn();
        System.out.println("Returning " + accusation);
        return accusation;
    }

    /**
     *  Set this player's cards.
     *
     *  @param     cards    This player's cards: key -> card, value -> type
     *
     */
    public void setCards(HashMap<String,String> cards) {
        myCards = cards;
    }

    /**
     *  Return this player's cards.
     *
     *  @return             This player's cards: key -> card, value -> type
     */
    public HashMap<String,String> getCards() {
        return myCards;
    }

    /**
     *  Print out this player's cards.
     *
     */
    public void printCards() {
        System.out.print("Miss Scarlet's Cards: ");
        Iterator iter = myCards.keySet().iterator();
        
        while (iter.hasNext()) 
            System.out.print((String)iter.next() + ", ");
            
        System.out.println();
    }

    /**
     *  Set this player's detective notes upon learning some information.
     *
     *  @param     card     The card that caused the change
     *  @param     type     The type of the card - suspect, weapon, or room
     *
     */
    public void setNotes(String card, String type) {
        setNotes(myNotes,card,type);
    }

    /**
     *  Return this player's detective notes.
     *
     *  @return             This player's detective notes
     */
    public DetectiveNotes getNotes() {
        return myNotes;
    }

    /**
     *  Delete this player's detective notes and cards.
     *
     */
    public void clear() {
	curr_row = 19;
	curr_column = 5;
        myCards.clear();
        myNotes.clear();
    }

    /**
     *  Print out this player's detective notes.
     *
     */
    public void printNotes() {
        System.out.println("Miss Scarlet's Detective Notes: ");
        System.out.println(myNotes);   
    }
}
