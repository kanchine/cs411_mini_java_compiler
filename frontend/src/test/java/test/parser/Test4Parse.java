package test.parser;

import ast.AST;
import org.junit.*;
import org.junit.jupiter.api.*;
import parser.Parser;
import util.TranscriptWriter;

import java.io.File;
import java.io.IOException;


/**
 * The tests in this class correspond more or less to the work in Chapter 4.
 * <p>
 * We simply run the same tests as the Test3Parse tests (done this by inheriting all
 * the tests from Test3Parse via the extends clause). But we modify the accept method
 * to also check the output of pretty printing the parse tree against a (supposedly correct)
 * test transcript.
 * <p>
 * See also {@link util.TranscriptWriter} for more
 * information on the Transcript.
 *
 * @author kdvolder
 */
public class Test4Parse extends Test3Parse {

    /**
     * If verifyTranscript is true then the output of the test run will be
     * compared against the "TestParse4.log" file. Errors will be thrown if
     * the actual output doesn't match the logfile contents.
     * <p>
     * If verifyTranscript is false, the output will not be compared to the
     * logfile.
     * <p>
     * This is useful if you are debugging individual tests, because running only
     * some but not all tests will produce transcript verification errors
     * since skipping tests will cause some output to go "missing".
     * <p>
     * Note: in "noverify" mode, a test transcript is written to the file
     * "Test4Parse-log.tmp". This logfile is rewritten on
     * each testrun.
     *
     */

    private static TranscriptWriter transcript;

    private static int testNumber = 0;

    @BeforeAll
    public static void openTranscript() throws IOException {
        File tempLog = new File("Test4Parse-log.tmp");
        if (tempLog.exists())
            tempLog.delete();
        transcript = new TranscriptWriter(tempLog);
    }

    @AfterAll
    public static void closeTranscript() throws IOException {
        transcript.close();
    }

    @BeforeEach
    public void markBegin() throws IOException {
        transcript.mark("### BEG # TEST " + ++testNumber + " ###");
    }

    @AfterEach
    public void markEnd() throws IOException {
        transcript.mark("### END # TEST " + testNumber + " ###");
    }

    @Override
    protected void accept(String input) throws Exception {
        transcript.println("---------------");
        transcript.println(input);
        AST result = Parser.parse(input);
        verifyAST(result);
    }

    @Override
    protected void accept(File input) throws Exception {
        transcript.println("---------------");
        transcript.println("File: " + input);
        AST result = Parser.parse(input);
        verifyAST(result);
    }

    private void verifyAST(AST result) throws Exception {
        transcript.println("vvvvvvvvvvvvvvv");
        String pretty = Parser.unparse(result);
        transcript.println(pretty); // this is automatically verified against transcript log if it exists.
        //As an extra safety against possible human error in verifying that the
        //captured transcript output is actually correct, we also double check that the
        //the pretty printed output can be parsed again and results in the same parse tree.
        AST parsedAgain = Parser.parse(pretty);
        Assertions.assertEquals(pretty, Parser.unparse(parsedAgain));
    }

}
