import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GrammarSolverTest {

    private GrammarSolver solver(String inputFile) throws FileNotFoundException {
        List<String> grammar = new ArrayList<>();
        Scanner input = new Scanner(new File(inputFile));
        while (input.hasNextLine()) {
            String next = input.nextLine().trim();
            if (next.length() > 0) {
                grammar.add(next);
            }
        }
        return new GrammarSolver(grammar);
    }

    // No empty grammars in grammar constructor
    @Test
    void emptyGrammarSolver() {
        assertThrows(IllegalArgumentException.class, () -> new GrammarSolver(null));
        assertThrows(IllegalArgumentException.class, () -> new GrammarSolver(List.of()));
    }

    // No duplicates in grammar constructor
    @Test
    void duplicateGrammarSolver() {
        List<String> s = new ArrayList<>();
        s.add("<s>::=<np> <vp>");
        s.add("<s>::=<np> <vp>");
        assertThrows(IllegalArgumentException.class, () -> new GrammarSolver(s));
    }

    // Contains(): is a non-term -> true, false otherwise
    @ParameterizedTest
    @ValueSource(strings = {"sentence.txt", "sentence_spaces.txt"})
    void contains(String source) throws FileNotFoundException {

        GrammarSolver solver = this.solver(source);
        // Contains given symbol
        assertTrue(solver.contains("<s>"));
        // case-sensitive
        assertFalse(solver.contains("<S>"));
        // Does not contain given symbol
        assertFalse(solver.contains("green"));
        // Empty symbols should throw exceptions
        assertThrows(IllegalArgumentException.class, () -> solver.contains(null));
        assertThrows(IllegalArgumentException.class, () -> solver.contains(""));
    }


    @ParameterizedTest
    @ValueSource(strings = {"sentence.txt", "sentence_spaces.txt"})
    void generate() throws FileNotFoundException {
        GrammarSolver solver = this.solver("sentence.txt");
        // Not a non-term in the grammar
        assertThrows(IllegalArgumentException.class, () -> solver.generate("this_is_not_a_symbol"));
        // No intermediate spaces of > 1
        String generated = solver.generate("<s>");
        assertEquals(generated, generated.trim());
        for (int i = 0; i < 10; i++) {
            assertTrue(noMoreThanOneSpace(solver.generate("<s>")));
        }
        // No leading or trailing spaces
        assertEquals(generated, generated.trim());
        // Is it random?
        String genCase = solver.generate("<s>");
        int counter = 0;
        for (int j = 0; j < 10; j++) {
            if (solver.generate("<s>").equals(genCase)) {
                counter++;
            }
        }
        assertTrue(counter < 8);

        // Does it work generally?
        // Are they all non-terms?
        Scanner lineScanner = new Scanner(solver.generate("<s>"));
        while (lineScanner.hasNext()) {
            assertFalse(solver.contains(lineScanner.next()));
        }

        GrammarSolver solver2 = solver("sentence4.txt");
        assertEquals("the big dog collapsed",solver2.generate("<s>"));
    }

    // Are there any consecutive runs of more than one space or tabs?
    private boolean noMoreThanOneSpace(String s) {
        int whiteSpaceCounter = 0;
        for (int i = 0; i < s.length(); i++) {
            char currChar = s.charAt(i);
            if (currChar == ' ') {
                whiteSpaceCounter++;
            } else if (currChar == '\t') {
                return false;
            } else {
                whiteSpaceCounter = 0;
            }

            if (whiteSpaceCounter > 1) {
                return false;
            }
        }
        return true;
    }

    @Test
    void getSymbols() throws FileNotFoundException {
        GrammarSolver solver = solver("sentence.txt");
        // KeySets match expected
        assertEquals("[<adj>, <adjp>, <dp>, <iv>, <n>, <np>, <pn>, <s>, <tv>, <vp>]", solver.getSymbols());
    }

    @Test
    void customGrammar() throws FileNotFoundException {
        GrammarSolver solver = solver("grammar.txt");
        String symbols = solver.getSymbols();
        assertTrue(symbols.split(",").length >= 5);
    }
}