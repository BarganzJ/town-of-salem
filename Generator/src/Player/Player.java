/*
 * Course: CS 1011
 *Fall 2023
 * Roby Velez
 * Cameron Konicek
 * 1/24/2024
 */
package Player;

import java.util.ArrayList;

public abstract class Player {
    Boolean dead = false;

    /**
     * sends a message in chat
     * @param s the message to be sent
     * @param channel the player number to send the message to, or -1 for all players
     */
    public abstract void sendChat(String s, int channel);

    /**
     * displays a message in chat
     * @param s the message to be displayed
     */
    public abstract void receiveChat(String s);

    /**
     * displays a prompt to the player, and gets an input
     * @param prompt the prompt to be displayed
     * @return the player's response
     */
    public abstract int ReadPrompt(String prompt);
    public void kill(){
        boolean dead = true;
    }
    /**
     * generates a pre-written prompt containing the passed in options and sends it to the player
     * to be used for both roleActions AND voting
     * @param options the options to be included in the prompt
     * @param actionType the type of action the request was made for
     * @return the selection made by the player
     */
    public int request(ArrayList<Integer> options, String actionType){
        return 0;
    }
}
