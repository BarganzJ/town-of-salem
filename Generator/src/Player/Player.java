/*
 * Course: CS 1011
 *Fall 2023
 * Roby Velez
 * Cameron Konicek
 * 1/24/2024
 */
package Player;

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
    public static void kill(){
        boolean dead = true;
    }
}
