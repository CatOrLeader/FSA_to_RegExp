// Mukhutdinov Artur

/*
 * Main
 *
 * Ver. 1.0.0
 *
 * Program which implements the FSA to RegExp converter.
 * Data stores and outputs in the files' 'input.txt' and 'result.txt'.
 */

import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Main class of the program with the general functionality.
 *
 * @author Artur Mukhutdinov, CS-03 BS1 Innopolis University
 * @version 1.0.0
 */
public class Main {
    /**
     * Scanner from input file ("fsa.txt")
     */
    private static BufferedReader reader = null;
    /**
     * Writer for output file ("result.txt")
     */
    private static BufferedWriter writer = null;
    /**
     * The static variable which provides the most general and complicated tests for FSA validation
     */
    private static final Checker CHECKER = new Checker();
    /**
     * The static variable which provides a finished report of the FSA validation if there are no runtime errors
     */
    private static final ReportFormation REPORT = new ReportFormation();
    /**
     * Array of possible states in FSA.
     */
    private static final ArrayList<State> STATES = new ArrayList<>();
    /**
     * Array of possible transition token in FSA.
     */
    private static final ArrayList<Transition> ALPHA = new ArrayList<>();
    /**
     * An initial state of FSA. In this particular case, according to the task, only one initial state is allowed.
     */
    private static State initialState = null;
    /**
     * Array of possible final states in FSA.
     */
    private static final ArrayList<State> FINAL_STATES = new ArrayList<>();

    /**
     * The main method provide something like "collection" of the major methods of the entire program
     *
     * @param args canonical parameter for java entry point
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    public static void main(String[] args) throws IOException {
        scanFiles();

        makeFormattedInput();

        // Checking FSA for disjoint
        try {
            if (CHECKER.isDisjoint(STATES, initialState)) {
                throw new DisjointStatesException();
            }
        } catch (DisjointStatesException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }

        // Complete a report
        markWarnings();
        REPORT.markCompleteness(CHECKER.isComplete(STATES, ALPHA));

        writer.write(REPORT.toString());

        reader.close();
        writer.close();
    }

    /**
     * Scan files input ("fsa.txt") and output ("result.txt"). Output file will be created again
     *
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    private static void scanFiles() throws IOException {
        try {
            reader = new BufferedReader(new FileReader("fsa.txt"));
            writer = new BufferedWriter(new FileWriter("result.txt"));
        } catch (IOException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse entire set of all possible states
     *
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    private static void scanStates() throws IOException {
        try {
            String tempString = reader.readLine();
            String[] stateNames = tempString.substring(8, tempString.length() - 1).split(",");

            // If nothing was appeared
            if (tempString.substring(8, tempString.length() - 1).length() == 0) {
                throw new InputMalformedException();
            }

            for (String stateName : stateNames) {
                if (!CHECKER.isStateNameCorrect(stateName)) {
                    throw new InputMalformedException();
                }

                STATES.add(new State(stateName));
            }

        } catch (IOException | InputMalformedException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse entire set of all possible transition tokens
     *
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    private static void scanAlpha() throws IOException {
        try {
            String tempString = reader.readLine();
            String[] transitionNames = tempString.substring(7, tempString.length() - 1).split(",");

            // If nothing was appeared
            if (tempString.substring(7, tempString.length() - 1).length() == 0) {
                throw new InputMalformedException();
            }

            for (String transitionName : transitionNames) {
                if (!CHECKER.isTransitionNameCorrect(transitionName)) {
                    throw new InputMalformedException();
                }

                ALPHA.add(new Transition(transitionName));
            }

        } catch (IOException | InputMalformedException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse initial state of FSA
     *
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    private static void scanInitialState() throws IOException {
        try {
            String stateName = reader.readLine();
            stateName = stateName.substring(9, stateName.length() - 1);
            String[] tempString = stateName.split(",");

            // If nothing was appeared
            if (stateName.length() == 0) {
                throw new InitialStateNotDefinedException();
            }

            // If more than one initial state
            if (tempString.length > 1) {
                throw new InputMalformedException();
            }

            initialState = getState(stateName);

            // If initialState not in the possible states set
            if (initialState == null) {
                throw new IncorrectStateException(stateName);
            }

        } catch (IOException | InputMalformedException | InitialStateNotDefinedException | IncorrectStateException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse entire set of all possible final states
     *
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    private static void scanFinalStates() throws IOException {
        try {
            String tempString = reader.readLine();
            String[] stateNames = tempString.substring(8, tempString.length() - 1).split(",");

            // If nothing was appeared
            if (tempString.substring(8, tempString.length() - 1).length() == 0) {
                return;
            }

            for (String stateName : stateNames) {
                State tempState = getState(stateName);

                // If particular state is not belong to the possible states set
                if (tempState == null) {
                    throw new IncorrectStateException(stateName);
                }

                FINAL_STATES.add(tempState);
            }

        } catch (IOException | IncorrectStateException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse the entire set of all possible transitions. Transitions in this implementation presented as
     * links from state_1 --> (possible_states_from_state_1)
     *
     * @throws IOException throws when input file ("fsa.txt") does not exist
     */
    private static void scanTransitions() throws IOException {
        try {
            String tempString = reader.readLine();
            String[] transitions = tempString.substring(7, tempString.length() - 1).split(",");

            // If nothing was appeared
            if (tempString.substring(7, tempString.length() - 1).length() == 0) {
                return;
            }

            for (String transition : transitions) {
                // Transition split by separator = ">"
                String[] transitionSplit = transition.split(">");

                State sourceState = getState(transitionSplit[0]);
                // If state_source from transition does not belong set of possible states
                if (sourceState == null) {
                    throw new IncorrectStateException(transitionSplit[0]);
                }

                Transition trans = getTransition(transitionSplit[1]);
                // If transition token from transition does not belong set of possible transitions
                if (trans == null) {
                    throw new TransitionIsNotPresentedException(transitionSplit[1]);
                }

                State destState = getState(transitionSplit[2]);
                // If state_dest from transition does not belong set of possible states
                if (destState == null) {
                    throw new IncorrectStateException(transitionSplit[2]);
                }

                sourceState.addPossibleTransition(destState, trans);
            }
        } catch (IOException | IncorrectStateException | TransitionIsNotPresentedException e) {
            writer.write(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Get state by its name from set of all possible states
     *
     * @param stateName name of the state needed state
     * @return State if presented in set of states; Otherwise, null
     */
    private static State getState(String stateName) {
        for (State state : STATES) {
            if (state.getName().equals(stateName)) {
                return state;
            }
        }
        return null;
    }

    /**
     * Get transition token by its name from set of all possible transitions
     *
     * @param transitionName name of the state needed transition token
     * @return Transition if presented in set of transitions; Otherwise, null
     */
    private static Transition getTransition(String transitionName) {
        for (Transition transition : ALPHA) {
            if (transition.name().equals((transitionName))) {
                return transition;
            }
        }
        return null;
    }

    /**
     * The common driver for all parsing methods of the program.
     * Parsing provided sequentially by task's conditions.
     */
    private static void makeFormattedInput() {
        try {
            scanStates();
            scanAlpha();
            scanInitialState();
            scanFinalStates();
            scanTransitions();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Mark appearing warnings in final report
     */
    private static void markWarnings() {
        // If no final states
        if (FINAL_STATES.size() == 0) {
            REPORT.markWarning(1);
        }

        // If some states are not reachable from the initial state, but connected with other states somehow (!disjoint)
        if (!CHECKER.areAllStatesReachable(STATES, initialState)) {
            REPORT.markWarning(2);
        }

        // If there is more than one transition with the same transition token from particular state
        if (!CHECKER.isDeterministic(STATES)) {
            REPORT.markWarning(3);
        }
    }
}

/**
 * The most essential class of the program. Implement all significant tests for the FSA validation
 */
class Checker {
    private static final int A_ASCII = 65;
    private static final int Z_ASCII = 90;
    private static final int a_ASCII = 97;
    private static final int z_ASCII = 122;
    private static final int UNDERSCORE_ASCII = 95;
    private static final int ASCII_0 = 48;
    private static final int ASCII_9 = 57;
    /**
     * Array of states for checking if the FSA disjoint
     */
    private static final ArrayList<State> REACHED_STATES = new ArrayList<>();
    /**
     * Array of States which is the same as the original FSA States but without directions;
     * (e.g. 1 --> 2 now 1 <--> 2)
     */
    private static final ArrayList<State> UNDIRECTED_STATES = new ArrayList<>();
    /**
     * HashMap which shows how many states can be visited from the initial state
     */
    private static final HashMap<State, Boolean> IS_VISITED_FROM_INITIAL_STATE = new HashMap<>();

    /**
     * Check if the state name is correct according to task's condition
     *
     * @param name current state name for checking
     * @return true - if name is correct; Otherwise, false
     */
    public boolean isStateNameCorrect(String name) {
        for (Character c : name.toCharArray()) {
            if (!(isLetter(c) || isDigit(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the transition token name is correct according to task's condition
     *
     * @param name current transition token name for checking
     * @return true - if name is correct; Otherwise, false
     */
    public boolean isTransitionNameCorrect(String name) {
        for (Character c : name.toCharArray()) {
            if (!(isLetter(c) || isDigit(c) || c == UNDERSCORE_ASCII)) {
                return false;
            }
        }
        return true;
    }

    private boolean isLetter(Character c) {
        return (A_ASCII <= c && c <= Z_ASCII) || (a_ASCII <= c && c <= z_ASCII);
    }

    private boolean isDigit(Character c) {
        return (ASCII_0 <= c && c <= ASCII_9);
    }

    /**
     * Check if states are disjoint somehow
     *
     * @param states       original possible states
     * @param initialState original initial state
     * @return true - if states are disjoint; Otherwise, false
     */
    public boolean isDisjoint(ArrayList<State> states, State initialState) {
        createStatesUndirected(states);
        State initialStateInUndirectedGraph = UNDIRECTED_STATES.get(UNDIRECTED_STATES.indexOf(getState(initialState)));
        getAllPossibleReachedStates(initialStateInUndirectedGraph);
        return REACHED_STATES.size() != states.size();
    }

    /**
     * Create an undirected set of states from origin set of states.
     * Undirected set will be saved in static variable UNDIRECTED_STATES.
     *
     * @param states original set of all possible states
     */
    private void createStatesUndirected(ArrayList<State> states) {
        // Copy all states from the original set to undirectedStatesSet
        for (State state : states) {
            State newState = new State(state.getName());
            UNDIRECTED_STATES.add(newState);
        }

        // Implement old transitions between states in the new field on undirectedStatesSet.
        // (Transitions were obtained from the original states transitions)
        for (State state : states) {
            for (State stateInner : state.getPossibleStatesToMove()) {
                UNDIRECTED_STATES.get(UNDIRECTED_STATES.indexOf(getState(state))).addPossibleTransition(
                        UNDIRECTED_STATES.get(UNDIRECTED_STATES.indexOf(getState(stateInner))), null
                );
            }
        }

        // Implement addition transition from [tail_state] to [head_state].
        // (e.g. [original set = (1: 2, 3; 2: ; 3: ); new set = (1: 2, 3; 2: 1 ; 3: 1)])
        for (State state : UNDIRECTED_STATES) {
            ArrayList<State> tempList = (ArrayList<State>) state.getPossibleStatesToMove().clone();
            for (State innerState : tempList) {
                innerState.addPossibleTransition(state, null);
            }
        }

        // Delete duplicates of states in every array of possible transitions from each state
        for (State state : UNDIRECTED_STATES) {
            state.setPossibleStatesToMove(new ArrayList<>(
                    state.getPossibleStatesToMove().stream().distinct().toList()
            ));
        }
    }

    /**
     * Get state from the undirected set of states
     *
     * @param state needed state
     * @return State - if state is found; Otherwise, false
     */
    private State getState(State state) {
        for (State stateUnd : UNDIRECTED_STATES) {
            if (stateUnd.getName().equals(state.getName())) {
                return stateUnd;
            }
        }
        return null;
    }

    /**
     * Fill REACHED_STATE by true if state connected with the other states
     *
     * @param initialState initial state for starting process of iteration over all states
     */
    private void getAllPossibleReachedStates(State initialState) {
        REACHED_STATES.add(initialState);
        for (State state : initialState.getPossibleStatesToMove()) {
            if (!REACHED_STATES.contains(state)) {
                getAllPossibleReachedStates(state);
            }
        }
    }

    /**
     * Check if all states are reachable from the initial state
     *
     * @param states       original set of all possible states
     * @param initialState original initial state of FSA
     * @return true - if all states are accessible from the initial state; Otherwise, false
     */
    public boolean areAllStatesReachable(ArrayList<State> states, State initialState) {
        // Fill the HashMap with false - we are didn't appear in this places now
        for (State state : states) {
            IS_VISITED_FROM_INITIAL_STATE.put(state, false);
        }

        IS_VISITED_FROM_INITIAL_STATE.put(initialState, true);

        makeMove(initialState);

        return !IS_VISITED_FROM_INITIAL_STATE.containsValue(false);
    }

    /**
     * Support step for iteration over all possible moves from the initial state
     *
     * @param state state from which we check every possible transition to another states
     */
    private void makeMove(State state) {
        for (State tempState : state.getPossibleStatesToMove()) {
            if (IS_VISITED_FROM_INITIAL_STATE.get(tempState)) {
                continue;
            }
            IS_VISITED_FROM_INITIAL_STATE.put(tempState, true);
            makeMove(tempState);
        }
    }

    /**
     * Check if there is more than one transition with the same transition token for some state
     *
     * @param states original set of possible states
     * @return true - if there is no more than one transition with the same transition token for each token;
     * Otherwise, false
     */
    public boolean isDeterministic(ArrayList<State> states) {
        for (State state : states) {
            int possibleTransitionsNumber = state.getTransitions().size();
            int distinctPossibleTransitionsNumber = state.getTransitions().stream().distinct().toList().size();
            if (possibleTransitionsNumber > distinctPossibleTransitionsNumber) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checking if the FSA is complete
     *
     * @param states      original set of possible states
     * @param transitions original set of possible transition tokens
     * @return true - if FSA is complete; Otherwise, false
     */
    public boolean isComplete(ArrayList<State> states, ArrayList<Transition> transitions) {
        int transitionsCount = 0;

        for (State state : states) {
            transitionsCount += state.getTransitions().stream().distinct().toList().size();
        }

        // Check if the number of transitions equal to needed number of transitions of FSA to be complete
        return transitionsCount == states.size() * transitions.size();
    }
}

/**
 * Class State implements node of the FSA with the name. Name of states cannot repeat
 */
class State {
    /**
     * All possible states which can be reached from this state
     */
    private ArrayList<State> possibleStatesToMove = new ArrayList<>();
    /**
     * All transition tokens which can be used from this state
     */
    private final ArrayList<Transition> transitions = new ArrayList<>();
    private final String name;

    State(String name) {
        this.name = name;
    }

    public ArrayList<State> getPossibleStatesToMove() {
        return possibleStatesToMove;
    }

    public void setPossibleStatesToMove(ArrayList<State> possibleStatesToMove) {
        this.possibleStatesToMove = possibleStatesToMove;
    }

    public ArrayList<Transition> getTransitions() {
        return transitions;
    }

    public String getName() {
        return name;
    }

    public void addPossibleTransition(State destState, Transition transition) {
        possibleStatesToMove.add(destState);
        transitions.add(transition);
    }
}

/**
 * Transition token
 *
 * @param name of the transition token
 */
record Transition(String name) {
}

class IncorrectStateException extends Exception {
    private final String stateName;

    IncorrectStateException(String stateName) {
        this.stateName = stateName;
    }

    public String toString() {
        return "Error:\nE1: A state '" + stateName + "' is not in the set of states\n";
    }
}

class DisjointStatesException extends Exception {
    @Override
    public String toString() {
        return "Error:\nE2: Some states are disjoint\n";
    }
}

class TransitionIsNotPresentedException extends Exception {
    private final String transitionName;

    TransitionIsNotPresentedException(String transitionName) {
        this.transitionName = transitionName;
    }

    public String toString() {
        return "Error:\nE3: A transition '" + transitionName + "' is not represented in the alphabet\n";
    }
}

class InitialStateNotDefinedException extends Exception {
    @Override
    public String toString() {
        return "Error:\nE4: Initial state is not defined\n";
    }
}

class InputMalformedException extends Exception {
    @Override
    public String toString() {
        return "Error:\nE5: Input file is malformed\n";
    }
}

class WarningDoesNotExistException extends Exception {
    @Override
    public String toString() {
        return "Error:\nE6: Warning with this number does not exist\n";
    }
}

/**
 * Class which represent the final report for the FSA
 */
class ReportFormation {
    private static final String W1 = "W1: Accepting state is not defined";
    private static final String W2 = "W2: Some states are not reachable from the initial state";
    private static final String W3 = "W3: FSA is nondeterministic";
    private static final String[] warningsMessages = {W1, W2, W3};
    private static final boolean[] warningsAppearance = {false, false, false};
    private static boolean completeness;

    /**
     * Mark that warning should be in the final output
     *
     * @param warningNumber integer number of warning
     */
    public void markWarning(int warningNumber) {
        try {
            if (!(1 <= warningNumber && warningNumber <= 3)) {
                throw new WarningDoesNotExistException();
            }

            warningsAppearance[warningNumber - 1] = true;
        } catch (WarningDoesNotExistException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Mark if the FSA is complete
     *
     * @param isComplete FSA condition
     */
    public void markCompleteness(boolean isComplete) {
        completeness = isComplete;
    }

    @Override
    public String toString() {
        StringBuilder outputString = new StringBuilder("FSA is ");

        outputString.append(completeness ? "complete\n" : "incomplete\n");

        boolean areWarningsAppear = false;
        StringBuilder warningMessagesText = new StringBuilder("Warning:\n");
        for (int i = 0; i < 3; i++) {
            if (warningsAppearance[i]) {
                areWarningsAppear = true;
                warningMessagesText.append(warningsMessages[i]).append("\n");
            }
        }

        if (areWarningsAppear) {
            outputString.append(warningMessagesText);
        }

        return outputString.toString();
    }
}
