package roles;

import actions.RoleInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import translator.Translator;
public abstract class RoleControl {

    public String roleName; // The player's role name
    public String nightAction; // What the player chooses to be their night action
    public String nightResult; // What the player sees as the result for their night action(if it worked)
    static String mafKiller = "No one"; // The name of the Mafia Killer that will go out to kill
    static int mafTarget; // The chosen target of the Mafia that is marked for death
    static int finMafTarget; // The actual target of the Mafia that will be attacked
    int playerNum; // The number of the player
    int target; // Used to keep track of player targets
    int target2; // Used for multi target roles
    public int witched = 0; // Indicator for which target is the player forced to visit(if Witched)
    static int vetNum = 0; // Indicator for the Veteran's player number(if he exists)
    public static int visitingVamp = 0; // Indicator for which Vampire is visiting
    public static int visitingMD = 0; // Indicator for which MD(Janitor/Forger) is following the MK
    boolean jailed = false; // If the player is Jailed
    boolean blocked = false; // If the player is roleblocked
    boolean immune; // Used to keep track of if people vested/self-healed
    static boolean ignited = false; // Used to keep track if an Arsonist ignited or not
    static boolean mkJailed = true; // If there is one MK, this tells if he is jailed or not
    public boolean forged = false; // Indicator if this player was forged or not
    public boolean cleaned = false; // Indicator if this player was cleaned or not
    public boolean bitten = false; // Indicator if this player was bitten by a Vampire or not
    static boolean alert = false; // Used to keep track if the Veteran alerted or not
    static boolean gfBlock = true; // Indicator if the godfather is restricted or not
    static boolean mfBlock = true; // Indicator if the mafioso is restricted or not
    public ArrayList<String> activity; // Influence from other roles that they are notified of(non-lethal)
    public ArrayList<String> attackers; // Players that attacked and killed the player
    ArrayList<Integer> DocSubs = new ArrayList<>(); // Tracks Doctor subscribers
    ArrayList<Integer> BGSubs = new ArrayList<>(); // Tracks Bodyguard subscribers
    ArrayList<Integer> visits = new ArrayList<>(); // Tracks all visitors to this player
    public static ArrayList<Integer> mafVisits = new ArrayList<>(); // Tracks all players visited by the Mafia
    public static ArrayList<Integer> mafia; // Indicator for all the mafia in the role list
    public static ArrayList<Integer> vampires; // Indicator for all the vampires in the role list
    static Random randomizer = new Random(); // Used for random generation of numbers
    static HashMap<Integer, Integer> switches; // Tracks which targets were transported
    public static HashMap<Integer, RoleControl> players; // Tracker for all the players

    /**
     * Used only by special classes (MafiaKillers and ArsoIgnite)
     */
    public RoleControl(String name) {
        roleName = name;
        resetStaticVariables();
    }

    public RoleControl(String name, int num) {
        roleName = name;
        playerNum = num;
        nightAction = "";
        nightResult = "";
        activity = new ArrayList<>();
        attackers = new ArrayList<>();
        resetStaticVariables();
    }

    private void resetStaticVariables() {

        RoleControl.mafKiller = "No one";
        RoleControl.vetNum = 0;
        RoleControl.visitingMD = 0;
        RoleControl.visitingVamp = 0;
        RoleControl.ignited = false;
        RoleControl.mkJailed = true;
        RoleControl.alert = false;
        RoleControl.mfBlock = true;
        RoleControl.gfBlock = true;
        RoleControl.mafVisits = new ArrayList<>();
        RoleControl.switches = new HashMap<>();

    }

    /**
     * Returns a valid target for a town member that was chosen by the player from all valid targets.
     * A valid target is everyone in the town excluding the number of the player,
     * as they cannot target themselves unless the role allows it.
     * In those cases, the number -1 is passed in.
     *
     * @param player The player number
     * @return The target number
     */
    public int validTownTarget(int player) {
        ArrayList<Integer> options = new ArrayList<>();
        for (int i = 1; i < players.size()+1; i++) {
            if (player != i){
                options.add(i);
            }
        }
        return PlayerChoice(options);
    }

    /**
     * TODO: make a request method in translator and call that instead
     * sends the list of all valid options to the request method anf validates the output
     * @param options a list of all valid options
     * @return the chosen valid option
     */
    private static int PlayerChoice(ArrayList<Integer>options){
        Scanner in = new Scanner(System.in);
        int target = 0;
        while (target == 0) {
            System.out.println("Your options are "+options);
            System.out.println("enter one of the shown numbers");
            if (in.hasNextInt()) {
                int input = in.nextInt();
                in.nextLine();
                for (Integer option : options) {
                    if (input == option) {
                        target = input;
                        break;
                    }
                }
            } else if (in.hasNext()){
                in.nextLine();
            }

        }
        return target;
    }

    /**
     * Returns a valid target for the mafia. A valid mafia target is everyone
     * in the town that does not happen to be a member of the mafia.
     *
     * @return The target number
     */
    public int validMafTarget() {
        ArrayList<Integer> options = new ArrayList<>();
        for (int i = 1; i < players.size()+1; i++) {
            if (!mafia.contains(i)){
                options.add(i);
            }
        }
        return PlayerChoice(options);
    }

    /**
     * Returns a valid target for the vampires. A valid vampire target is everyone
     * in the town that do not happen to already be a vampire.
     *
     * @return The target number
     */
    public int validVampTarget() {

        ArrayList<Integer> options = new ArrayList<>();
        for (int i = 1; i < players.size()+1; i++) {
            if (!vampires.contains(i)){
                options.add(i);
            }
        }
        return PlayerChoice(options);
    }

    /**
     * Returns a valid target for the mafia that is not marked for death by the mafia.
     * A valid mafia target is everyone in the town that does not happen to be a member of the mafia.
     * This method should only be called once MafiaKillers has been processed
     *
     * @return The target number
     */
    public int validNonDeathMafTarget() {
        ArrayList<Integer> options = new ArrayList<>();
        for (int i = 1; i < players.size()+1; i++) {
            if (!(mafia.contains(i))||target == mafTarget){
                options.add(i);
            }
        }
        /*
         * Special case: Role list is only 6 players, with 5 mafia. Attempting to run PlayerChoice
         * will present 0 options. Following if prevents this,
         * and forces the role to simply visit the target marked for death instead.
         */
        if (options.isEmpty()){
            return mafTarget;
        }
        return PlayerChoice(options);
    }

    /**
     * Process if the player being visited is a Veteran on alert.
     * If it is, send the appropriate data to the visitor and the Veteran.
     * The function assumes that the role class currently in the player variable
     * is the visiting player
     *
     * @param num The number of the person being visited
     * @return If the target was shot(and killed) by the Veteran
     */
    public boolean checkVetVisit(int num) {

        if (num == vetNum && alert) {
            if (DocSubs.isEmpty()) {
                attackers.add("Veteran");
                players.get(vetNum).activity.add("VetShot");
                return true;
            } else {
                activity.add("DocSave");
                notifyDoctors();
            }
        }
        return false;

    }

    /**
     * Given a number representing the player number that the current player wishes
     * to visit, this function will return back the number of the player that will
     * actually be visited.
     * It is assumed that the player variable stores the visitor's player profile.
     *
     * @param num The target that the visitor wants to visit
     * @return The actual target that will be visited
     */
    public int checkTargetSwitch(int num) {

        int target = num;
        if (witched != 0) {
            target = witched;
        }
        if (switches.containsKey(target)) {
            return (switches.get(target));
        }
        return(target);

    }

    /**
     * Lookouts ONLY
     * Returns the Visits of the player that they watched that night
     *
     * @return The visits of the watched player
     */
    public ArrayList<Integer> getLookoutVisits() {
        return players.get(target).visits;
    }

    /**
     * Used by ActivityPrint for the Lookout and the Spy
     * Returns the name of the player under the given number slot
     *
     * @param num The player numbers
     * @return The role name
     */
    public String getPlayerName(int num) {
        return players.get(num).roleName;
    }


    /**
     * Subscribe a player(who is a Doctor) to the player of this class
     *
     * @param Doctor The Doctor's player number
     */
    public void subscribeDoctor(int Doctor) {
        DocSubs.add(Doctor);
    }

    /**
     * Subscribe a player(who is a Bodyguard) to the player of this class
     *
     * @param Bodyguard The Bodyguard's player number
     */
    public void subscribeBG(int Bodyguard) {
        BGSubs.add(Bodyguard);
    }

    /**
     * Notify all Doctors subscribed to this player that he was attacked
     */
    public void notifyDoctors() {
        for (int Doctor: DocSubs) {
            players.get(Doctor).updateDoctor();
        }
    }

    /**
     * Notify one of the Bodyguards subscribed to this player that he is being attacked,
     * and then remove that Bodyguard from the subscribers list as he had fulfilled his duty
     */
    public void notifyBG() {
        if (!BGSubs.isEmpty()) {
            players.get(BGSubs.get(0)).updateBG();
            BGSubs.remove(0);
        }
    }

    /**
     * Update the Doctor that his target was attacked
     */
    public void updateDoctor() {
        activity.add("DocAtt");
    }

    /**
     * Update the Bodyguard that his target was targeted, and process his fate
     * accordingly
     */
    public void updateBG() {
        if (!this.DocSubs.isEmpty()) {
            notifyDoctors();
            activity.add("DocSave");
        } else {
            attackers.add("BGAttack");
        }
    }

    /**
     * General attack method for killing roles.
     * TODO: store death state permanently
     */
    public void lethalAttack(int num, String AttackerName) {

        /*
         * Logic:
         * If in Jail, protectives do nothing. Notify attacker of immunity and target of failed attempt, exit
         * If immune by RoleControl variable, notify attacker of immunity, do a case below, then exit
         *      If target is Doc: Inform target that the attack failed due to immunity
         *      Else: Inform target that the attack failed due to a vest
         * If they Visited an alerted Veteran, process counter-attack and notify Veteran of the event, exit
         * If a doctor(s) are protecting, notify them of attack, notify target of heal, preserve them
         * If a bodyguard(s) are protecting, choose first, process counter-attack, inform target of save, exit
         * If immune by RoleInfo variable, inform attacker of immunity and target of the failed attack, preserve them
         * If not preserved, add the attackers name to the list of people who killed the target.
         */
        boolean dead = true;
        if (players.get(num).jailed) {
            players.get(num).activity.add("JailSave");
            activity.add("NightImmune");
            return;
        }
        if (players.get(num).immune) {
            if (players.get(num).roleName.equals("Doctor")) {
                players.get(num).activity.add("ImmuneSave");
            } else {
                players.get(num).activity.add("VestSave");
            }
            activity.add("NightImmune");
            return;
        }
        if (players.get(num).roleName.equals("Veteran") && alert) {
            players.get(num).activity.add("VetAtt");
            if (!DocSubs.isEmpty()) {
                notifyDoctors();
                activity.add("DocSave");
            } else {
                attackers.add("Veteran");
            }
            return;
        }
        if (!players.get(num).DocSubs.isEmpty()) {
            dead = false;
            players.get(num).activity.add("DocSave");
            players.get(num).notifyDoctors();
        }
        if (!players.get(num).BGSubs.isEmpty()) {
            players.get(num).activity.add("BGSave");
            players.get(num).notifyBG();
            if (!DocSubs.isEmpty()) {
                activity.add("DocvsBG");
                notifyDoctors();
            } else {
                attackers.add("Bodyguard");
            }
            return;
        }
        if (RoleInfo.NightImmune.contains(players.get(num).roleName) && dead) {
            players.get(num).activity.add("ImmuneSave");
            activity.add("NightImmune");
            dead = false;
        }
        if (dead) {
            players.get(num).attackers.add(AttackerName);
        }

    }

    /**
     * Processes the main function of the role
     *
     */
    abstract public void Process();

}
