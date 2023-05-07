// Mukhutdinov Artur

/*
 * Main
 *
 * Ver. 1.0.0
 *
 * Program which implements the FSA to RegExp converter.
 * Data stores and outputs in the files' 'input.txt' and 'result.txt'. Moreover, the final
 * report output in the console too.
 */

import java.io.*;
import java.util.ArrayList;

/**
 * The Main class of the program with the general functionality.
 *
 * @author Artur Mukhutdinov, CS-03 BS1 Innopolis University
 * @version 1.0.0
 */
public class Main {
    /**
     * Scanner from input file ("input.txt")
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
    private static final ArrayList<State> ACCEPTING_STATES = new ArrayList<>();
    /**
     * Class with the implementation of Kleene's algorithm. Works for different FSAs.
     */
    private static final KleeneAlgorithm algorithmImplementor = new KleeneAlgorithm();

    /**
     * The main method provide something like "collection" of the major methods of the entire program
     *
     * @param args canonical parameter for java entry point
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    public static void main(String[] args) throws IOException {
        scanFiles();

        makeFormattedInput();

        // Checking FSA for disjoint and
        try {
            if (!CHECKER.isDeterministic(STATES)) {
                throw new FSANondeterministicException();
            }

            if (CHECKER.isDisjoint(STATES, initialState)) {
                throw new DisjointStatesException();
            }
        } catch (DisjointStatesException | FSANondeterministicException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }

        // Making FSA
        FSA fsa = new FSA(STATES, ALPHA, initialState, ACCEPTING_STATES);

        // Output final regExp in the file
        writer.write(algorithmImplementor.getFinalRegExp(fsa) + "\n");
        reader.close();
        writer.close();

        // Output final regExp in the console
        BufferedReader output = new BufferedReader(new FileReader("result.txt"));
        System.out.println(output.readLine());

        // Complete a report
        output.close();
    }

    /**
     * Scan files input ("input.txt") and output ("result.txt"). Output file will be created again
     *
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    private static void scanFiles() throws IOException {
        try {
            reader = new BufferedReader(new FileReader("input.txt"));
            writer = new BufferedWriter(new FileWriter("result.txt"));
        } catch (IOException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse entire set of all possible states
     *
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    private static void scanStates() throws IOException {
        try {
            String tempString = reader.readLine();

            // If line is incorrect
            if (tempString.length() < 7) {
                throw new InputMalformedException();
            }

            // If keyword is incorrect
            if (!tempString.startsWith("states=")) {
                throw new InputMalformedException();
            }

            // If nothing was appeared
            if (tempString.substring(8, tempString.length() - 1).length() == 0) {
                throw new InputMalformedException();
            }

            String[] stateNames = tempString.substring(8, tempString.length() - 1).split(",");

            for (String stateName : stateNames) {
                if (!CHECKER.isStateNameCorrect(stateName)) {
                    throw new InputMalformedException();
                }

                STATES.add(new State(stateName));
            }

        } catch (IOException | InputMalformedException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse entire set of all possible transition tokens
     *
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    private static void scanAlpha() throws IOException {
        try {
            String tempString = reader.readLine();

            // If line is incorrect
            if (tempString.length() < 6) {
                throw new InputMalformedException();
            }

            // If keyword is incorrect
            if (!tempString.startsWith("alpha=")) {
                throw new InputMalformedException();
            }

            // If nothing was appeared
            if (tempString.substring(7, tempString.length() - 1).length() == 0) {
                throw new InputMalformedException();
            }

            String[] transitionNames = tempString.substring(7, tempString.length() - 1).split(",");

            for (String transitionName : transitionNames) {
                if (!CHECKER.isTransitionNameCorrect(transitionName)) {
                    throw new InputMalformedException();
                }

                ALPHA.add(new Transition(transitionName));
            }

        } catch (IOException | InputMalformedException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse initial state of FSA
     *
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    private static void scanInitialState() throws IOException {
        try {
            String tempString = reader.readLine();

            // If line is incorrect
            if (tempString.length() < 9) {
                throw new InputMalformedException();
            }

            String stateName = tempString.substring(9, tempString.length() - 1);
            String[] tempString0 = stateName.split(",");

            // If keyword is incorrect
            if (!tempString.startsWith("initial=")) {
                throw new InputMalformedException();
            }

            // If nothing was appeared
            if (stateName.length() == 0) {
                throw new InitialStateNotDefinedException();
            }

            // If more than one initial state
            if (tempString0.length > 1) {
                throw new InputMalformedException();
            }

            initialState = getState(stateName);

            // If initialState not in the possible states set
            if (initialState == null) {
                throw new IncorrectStateException(stateName);
            }

        } catch (IOException | InputMalformedException | InitialStateNotDefinedException | IncorrectStateException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse entire set of all possible final states
     *
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    private static void scanAcceptingStates() throws IOException {
        try {
            String tempString = reader.readLine();

            // If line is incorrect
            if (tempString.length() < 11) {
                throw new InputMalformedException();
            }

            String[] stateNames = tempString.substring(11, tempString.length() - 1).split(",");

            // If keyword is incorrect
            if (!tempString.startsWith("accepting=")) {
                throw new InputMalformedException();
            }

            // If nothing was appeared
            if (tempString.substring(11, tempString.length() - 1).length() == 0) {
                throw new SetOfAcceptingStatesEmptyException();
            }

            for (String stateName : stateNames) {
                State tempState = getState(stateName);

                // If particular state is not belong to the possible states set
                if (tempState == null) {
                    throw new IncorrectStateException(stateName);
                }

                ACCEPTING_STATES.add(tempState);
            }

        } catch (IOException | IncorrectStateException | SetOfAcceptingStatesEmptyException
                | InputMalformedException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
            reader.close();
            writer.close();
            System.exit(0);
        }
    }

    /**
     * Parse the entire set of all possible transitions. Transitions in this implementation presented as
     * links from state_1 --> (possible_states_from_state_1)
     *
     * @throws IOException throws when input file ("input.txt") does not exist
     */
    private static void scanTransitions() throws IOException {
        try {
            String tempString = reader.readLine();

            // If line is incorrect
            if (tempString.length() < 7) {
                throw new InputMalformedException();
            }

            String[] transitions = tempString.substring(7, tempString.length() - 1).split(",");

            // If keyword is incorrect
            if (!tempString.startsWith("trans=")) {
                throw new InputMalformedException();
            }

            // If nothing was appeared
            if (tempString.substring(7, tempString.length() - 1).length() == 0) {
                return;
            }

            for (String transition : transitions) {
                // Transition split by separator = ">"
                String[] transitionSplit = transition.split(">");

                // Incorrect transition regex
                if (transitionSplit.length != 3) {
                    throw new InputMalformedException();
                }

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
        } catch (IOException | IncorrectStateException | TransitionIsNotPresentedException
                | InputMalformedException e) {
            writer.write(e.toString());
            System.out.println(e.toString());
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
            scanAcceptingStates();
            scanTransitions();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
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
     * Check if there is more than one transition with the same transition token for some state
     *
     * @param states original set of possible states
     * @return true - if there is no more than one transition with the same transition token for each token;
     * Otherwise, false
     */
    public boolean isDeterministic(ArrayList<State> states) {
        for (State state : states) {
            ArrayList<Transition> allTransitions = new ArrayList<>();

            for (ArrayList<Transition> transitionArrayList : state.getTransitions()) {
                allTransitions.addAll(transitionArrayList);
            }

            int possibleTransitions = allTransitions.size();
            int distinctPossibleTransitions = allTransitions.stream().distinct().toList().size();

            if (distinctPossibleTransitions != possibleTransitions) {
                return false;
            }
        }

        return true;
    }
}

/**
 * Class, which represents the simple model of FSA.
 */
class FSA {
    private final ArrayList<State> states;
    private final ArrayList<Transition> alpha;
    private State initialState;
    private final ArrayList<State> acceptingStates;

    public FSA(ArrayList<State> states, ArrayList<Transition> alpha, State initialState, ArrayList<State> acceptingStates) {
        this.states = states;
        this.alpha = alpha;
        this.initialState = initialState;
        this.acceptingStates = acceptingStates;
    }

    public ArrayList<State> getStates() {
        return states;
    }

    public ArrayList<Transition> getAlpha() {
        return alpha;
    }

    public State getInitialState() {
        return initialState;
    }

    public void setInitialState(State initialState) {
        this.initialState = initialState;
    }

    public ArrayList<State> getAcceptingStates() {
        return acceptingStates;
    }
}

/**
 * Class, which represents the Kleene's Algorithm for understanding the RegExp, which
 * accepted by given FSA.
 */
class KleeneAlgorithm {
    private static final String EMPTY_SET = "{}";
    private static final String EPSILON = "eps";

    /**
     * Get final accepted by given FSA RegExp without reducing terms.
     * @param fsa for which will be found RegExp
     * @return string, represents the possible regExp, accepted by FSA.
     */
    public String getFinalRegExp(FSA fsa) {
        ArrayList<ArrayList<String>> steps = createEmptyStateArrays(fsa);

        initialStep(fsa, steps);
        for (int k = 0; k < fsa.getStates().size(); k++) {
            steps = makeStep(fsa, steps, k);
        }

        ArrayList<Integer> indexesOfAcceptingStates = new ArrayList<>();
        for (State finalState : fsa.getAcceptingStates()) {
            indexesOfAcceptingStates.add(fsa.getStates().indexOf(finalState));
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Integer index : indexesOfAcceptingStates) {
            stringBuilder.append("(");
            stringBuilder.append(steps.get(fsa.getStates().indexOf(fsa.getInitialState())).get(index)).append(")");
            stringBuilder.append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }

    private ArrayList<ArrayList<String>> createEmptyStateArrays(FSA fsa) {
        ArrayList<ArrayList<String>> steps = new ArrayList<>();
        for (int i = 0; i < fsa.getStates().size(); i++) {
            steps.add(new ArrayList<>());
            for (int j = 0; j < fsa.getStates().size(); j++) {
                steps.get(i).add(EMPTY_SET);
            }
        }
        return steps;
    }

    private ArrayList<ArrayList<String>> makeStep(FSA fsa, ArrayList<ArrayList<String>> steps, int k) {
        ArrayList<ArrayList<String>> newArr = new ArrayList<>();

        for (int i = 0; i < fsa.getStates().size(); i++) {
            newArr.add(new ArrayList<>());
            for (int j = 0; j < fsa.getStates().size(); j++) {
                newArr.get(i).add(formatRegExp(steps, i, j, k));
            }
        }

        return newArr;
    }

    private String formatRegExp(ArrayList<ArrayList<String>> steps, int i, int j, int k) {
        return "(" + steps.get(i).get(k) + ")" +
                "(" + steps.get(k).get(k) + ")" +
                "*" +
                "(" + steps.get(k).get(j) + ")" +
                "|" +
                "(" + steps.get(i).get(j) + ")";
    }


    private void initialStep(FSA fsa, ArrayList<ArrayList<String>> steps) {
        for (int i = 0; i < fsa.getStates().size(); i++) {
            for (int j = 0; j < fsa.getStates().size(); j++) {
                steps.get(i).set(j, getRegExp(fsa, i, j));
            }
        }
    }

    private String getRegExp(FSA fsa, int i, int j) {
        ArrayList<String> tokens = new ArrayList<>(findTransitions(fsa, i, j));

        tokens.sort(String::compareTo);
        StringBuilder stringBuilder = new StringBuilder();

        for (String token : tokens) {
            stringBuilder.append(token).append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        if (i == j) {
            if (tokens.get(0).equals(EMPTY_SET)) {
                return EPSILON;
            }
            stringBuilder.append("|").append(EPSILON);
        }

        return stringBuilder.toString();
    }

    private ArrayList<String> findTransitions(FSA fsa, int i, int j) {
        State source = fsa.getStates().get(i);
        State dest = fsa.getStates().get(j);

        ArrayList<String> trans = new ArrayList<>();

        if (!source.getPossibleStatesToMove().contains(dest)) {
            trans.add(EMPTY_SET);
            return trans;
        }

        for (Transition transition : source.getTransitions().get(source.getPossibleStatesToMove().indexOf(dest))) {
            trans.add(transition.name());
        }

        return trans;
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
    private final ArrayList<ArrayList<Transition>> transitions = new ArrayList<>();
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

    public ArrayList<ArrayList<Transition>> getTransitions() {
        return transitions;
    }

    public String getName() {
        return name;
    }

    public void addPossibleTransition(State destState, Transition transition) {
        if (possibleStatesToMove.contains(destState)) {
            transitions.get(possibleStatesToMove.indexOf(destState)).add(transition);
            return;
        }

        possibleStatesToMove.add(destState);
        transitions.add(new ArrayList<>());
        transitions.get(transitions.size() - 1).add(transition);
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
        return "E4: A state '" + stateName + "' is not in the set of states\n";
    }
}

class DisjointStatesException extends Exception {
    @Override
    public String toString() {
        return "E6: Some states are disjoint\n";
    }
}

class TransitionIsNotPresentedException extends Exception {
    private final String transitionName;

    TransitionIsNotPresentedException(String transitionName) {
        this.transitionName = transitionName;
    }

    public String toString() {
        return "E5: A transition '" + transitionName + "' is not represented in the alphabet\n";
    }
}

class InitialStateNotDefinedException extends Exception {
    @Override
    public String toString() {
        return "E2: Initial state is not defined\n";
    }
}

class InputMalformedException extends Exception {
    @Override
    public String toString() {
        return "E1: Input file is malformed\n";
    }
}

class SetOfAcceptingStatesEmptyException extends Exception {
    @Override
    public String toString() {
        return "E3: Set of accepting states is empty\n";
    }
}

class FSANondeterministicException extends Exception {
    @Override
    public String toString() {
        return "E7: FSA is nondeterministic\n";
    }
}
