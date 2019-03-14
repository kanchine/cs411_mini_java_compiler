package test.translate.typechecker;

import ast.BooleanType;
import ast.IntegerType;
import ast.ObjectType;
import ast.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import parser.ParseException;
import typechecker.ErrorMessage;
import typechecker.TypeChecker;
import typechecker.TypeCheckerException;
import util.SampleCode;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static parser.Parser.parseExp;

/**
 * The difficulty in writing tests for this unit of work is that we should,
 * if at all possible try to not make the testing code be dependant on the
 * Expression type checker returning specific error messages.
 * <p>
 * To try to still have reasonably specific tests that specify relatively
 * precisely what type of error a specific program ought to raise we will:
 * <ul>
 * <li>Provide you with a class ErrorReport that you should use to create
 * error reports.
 * <li>Tests will only inspect the first error in the report.
 * <li>Tests will be written to avoid ambiguities into what is the "first"
 * error as much as possible.
 * </ul>
 *
 * @author kdvolder
 */
public class TypeCheckTest {

    //////////////////////////////////////////////////////////////////////////////////////////
    // Preliminary check....

    /**
     * This test parses and typechecks all the book sample programs. These should
     * type check without any errors.
     * <p>
     * By itself this is not a very good test. E.g. an implementation which does nothing
     * at all will already pass the test!
     */
    @Test
    public void testSampleCode() throws Exception {
        File[] sampleFiles = SampleCode.sampleFiles("java");
        for (File sampleFile : sampleFiles) {
            System.out.println("parsing: " + sampleFile);
            accept(sampleFile);
        }
    }

    ///////////////////////// Helpers /////////////////////////////////////////

    private ErrorMessage typeError(String exp, Type expected, Type actual)
	throws ParseException {
        return ErrorMessage.typeError(parseExp(exp), expected, actual);
    }

    private void accept(File file) throws TypeCheckerException, Exception {
        TypeChecker.parseAndCheck(file);
    }

    private void accept(String string) throws TypeCheckerException, Exception {
        TypeChecker.parseAndCheck(string);
    }

    /**
     * Mostly what we want to do in this set of unit tests is see
     * whether the checker produces the right kind of error
     * reports. This is a helper method to do just that.
     */
    private void expect(ErrorMessage expect, String input) throws Exception {
        try {
            TypeChecker.parseAndCheck(input);
            Assertions.fail(
	      "A TypeCheckerException should have been raised but was not.");
        } catch (TypeCheckerException e) {
            Assertions.assertEquals(expect, e.getFirstMessage());
        }
    }

    // a helper method that expect input program to produce some type error
    // but doesn't care what the error is
    private void expectError(String input) {
        boolean typeCheckError = false;
        try{
            TypeChecker.parseAndCheck(input);
        } catch (TypeCheckerException e){
            typeCheckError = true;
        } catch (Exception e) {
            typeCheckError = false;
            System.out.println(e);
        }

        assertTrue(typeCheckError);
    }


    private static final String defaultMainClass = mainClass("{}"); // used in composing test program for convenience
    private static String mainClass(String stm) {
        return
                "class Main { \n" +
                        "   public static void main(String[] args) {\n" +
                        "      "+ stm +"\n"+
                        "   }\n" +
                        "}\n";
    }


    ////// Jerry's tests //////

    // --------------------------------------------------
    // Test group 1: duplicate identifier definitions in the same scope

    @Test
    public void duplicateClassName() throws Exception {
        // Duplicate with the Main class name
        expect( ErrorMessage.duplicateDefinition("Main"),
                defaultMainClass +
                        "class Main {}");

        // Duplicate class name
        expect( ErrorMessage.duplicateDefinition("Other"),
                defaultMainClass +
                        "class Other {}\n" +
                        "class Other {}");
    }

    @Test
    public void duplicateFields() throws Exception {
        // If names are diff it should be ok:
        accept(defaultMainClass +
                "class MyClass {\n" +
                "   int x;\n" +
                "   int y;\n" +
                "   int z;\n" +
                "}");
        // Same names in diff classes should be ok
        accept(defaultMainClass +
                "class MyClass {\n" +
                "   int x;\n" +
                "}\n" +
                "class OtherClass {\n" +
                "   int x;\n" +
                "}\n" +
                "");
        // Same names in same class
        expect( ErrorMessage.duplicateDefinition("same"),
                defaultMainClass +
                        "class MyClass {\n" +
                        "   int same;\n" +
                        "   int same;\n" +
                        "}");
        // Same names in same class = bad even if not consecutive locations.
        expect( ErrorMessage.duplicateDefinition("same"),
                defaultMainClass +
                        "class MyClass {\n" +
                        "   int same;\n" +
                        "   int diff;\n" +
                        "   int same;\n" +
                        "}");
    }

    @Test
    public void sameMethodAndFieldName() throws Exception {
        accept(defaultMainClass +
                "class MyClass {\n" +
                "   int same;\n" +
                "   public int same() { return 0; }\n" +
                "}");
    }

    @Test
    public void duplicateMethods() throws Exception {
        expect( ErrorMessage.duplicateDefinition("same"),
                defaultMainClass +
                        "class MyClass {\n" +
                        "   public int same() { return 1; }\n" +
                        "   public int same() { return 0; }\n" +
                        "}");
        expect( ErrorMessage.duplicateDefinition("same"),
                defaultMainClass +
                        "class MyClass {\n" +
                        "   public int same() { return 1; }\n" +
                        "   public int diff() { return 1; }\n" +
                        "   public int same() { return 0; }\n" +
                        "}");
        // Overloading is not supported
        expect( ErrorMessage.duplicateDefinition("same"),
                defaultMainClass +
                        "class MyClass {\n" +
                        "   public int same() { return 1; }\n" +
                        "   public int same(int x) { return x; }\n" +
                        "}");
    }

    @Test public void sameMethodAndLocal() throws Exception {
        // Methods fields and locals are in different name spaces
        accept( defaultMainClass +
                "class MyClass {\n" +
                "   int foo;\n" +
                "   public int foo(int foo) { return foo; }\n" +
                "}");
    }

    @Test
    public void sameLocalNameAndParamName() throws Exception {
        expect( ErrorMessage.duplicateDefinition("same"),
                defaultMainClass +
                        "class MyClass {\n" +
                        "   public int foo(int same) { int same; return same; }\n" +
                        "}");
    }
    @Test
    // Same name between field and local or between field and parameter
    public void sameLocalNameAndFieldName() throws Exception {
        accept(defaultMainClass +
                "class MyClass {\n" +
                "   int x;\n" +
                "   int y;\n" +
                "   public int foo(int x) { int y; return x; }\n" +
                "}");
    }

    // --------------------------------------------------
    // Test group 2: Undefined Types

    @Test
    public void goodFieldType() throws Exception {
        accept(defaultMainClass +
                "class Foo {\n" +
                "   int i;\n" +
                "   Foo foo;\n" +
                "   Bar bar;\n" +
                "}\n" +
                "class Bar {\n" +
                "   Foo foo;\n" +
                "   Bar bar;\n" +
                "}");
    }
    @Test
    public void badFieldType() throws Exception {
        expect(ErrorMessage.undefinedId("Bar"),
                defaultMainClass +
                        "class Foo {\n" +
                        "   Bar f;\n" +
                        "}");
    }

    @Test
    public void goodReturnType() throws Exception {
        accept(defaultMainClass +
                "class Foo {\n" +
                "   public Bar getBar() { return bar; }\n" +
                "}\n" +
                "class Bar {\n" +
                "   public Foo getFoo() { return foo; }\n" +
                "}");
    }
    
    @Test
    public void badReturnType() throws Exception {
        expect(ErrorMessage.undefinedId("Ghost"),
                defaultMainClass +
                        "class Foo {\n" +
                        "   Foo foo;\n" +
                        "   public Foo getFoo() { return foo; }\n" +
                        "   public Ghost getGhost() { return foo; }\n" +
                        "   public int getZero() { return 0; }\n" +
                        "}");

        expect(typeError("false", new IntegerType(), new BooleanType()),
                defaultMainClass +
                        "class Foo {\n" +
                        "   public int test() { return false; }\n" +
                        "}");
    }

    @Test
    public void goodParamType() throws Exception {
        accept(	defaultMainClass+
                "class Foo {\n" +
                "   public Foo getFoo(Foo foo) { return foo; }\n" +
                "}\n");
    }

    @Test
    public void badParamType() throws Exception {
        expect(ErrorMessage.undefinedId("Ghost"),
                defaultMainClass+
                        "class Foo {\n" +
                        "   Foo foo;\n" +
                        "   public Foo getFoo() { return foo; }\n" +
                        "   public Foo getGhost(Ghost ghost) { return foo; }\n" +
                        "   public int getZero() { return 0; }\n" +
                        "}");
    }

    @Test
    public void goodLocalType() throws Exception {
        accept(defaultMainClass +
                "class Foo {\n" +
                "   public Foo getFoo(Foo foo1) { Foo foo2; return foo2; }\n" +
                "}\n");
    }

    @Test
    public void badLocalType() throws Exception {
        expect(ErrorMessage.undefinedId("Ghost"),
                defaultMainClass +
                        "class Foo {\n" +
                        "   public Foo getFoo(Foo foo) { Ghost ghost; int a; int b; return foo; }\n" +
                        "}");
    }


    // --------------------------------------------------
    // Test group 3: statements

    @Test
    public void badPrint() throws Exception {
        // In minijava, println can only print integer. see textbook page 484
        expect( typeError("true", new IntegerType(), new BooleanType()),
                "class Main {\n" +
                        "   public static void main(String[] args) {\n" +
                        "      System.out.println(true);\n" +
                        "   }\n" +
                        "}");
        expect( typeError("boolValue", new IntegerType(), new BooleanType()),
                defaultMainClass+
                        "class Classy {\n" +
                        "   public int foo(boolean boolValue) {\n" +
                        "      System.out.println(boolValue);\n" +
                        "      return 0;\n" +
                        "   }\n" +
                        "}");
    }

    @Test
    public void badPrintObject() throws Exception {
        expect(
                typeError("theC", new IntegerType(), new ObjectType("C")),
                defaultMainClass +
                        "class C {\n" +
                        "    public int foo(C theC) {\n" +
                        "        System.out.println(theC);\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void goodPrintLiteral() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public boolean f(int x) {\n" +
                "        System.out.println(12345);\n" +
                "        return true;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void goodPrintVariable() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(int x) {\n" +
                "        System.out.println(x);\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void ifValid() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(boolean b, int x, int y) {\n" +
                "        if (b) {\n" +
                "            System.out.println(x + y);\n" +
                "        } else {\n" +
                "            System.out.println(x * y);\n" +
                "        }\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void nestedIfValid() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(boolean b, boolean c, int x, int y) {\n" +
                "        if (b) {\n" +
                "            if (c) {\n" +
                "                System.out.println(x + y);\n" +
                "            } else {\n" +
                "                System.out.println(x * y);\n" +
                "            }\n" +
                "        } else {\n" +
                "            if (c) {\n" +
                "                System.out.println(x * y);\n" +
                "            } else {\n" +
                "                System.out.println(x + y);\n" +
                "            }\n" +
                "        }\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void longIfValid() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(int x) {\n" +
                "        if (x < 0) {\n" +
                "            System.out.println(x);\n" +
                "        } else if (x < 10) {\n" +
                "            System.out.println(x + 1);\n" +
                "        } else if (x < 20) {\n" +
                "            System.out.println(x + 2);\n" +
                "        } else if (x < 30) {\n" +
                "            System.out.println(x + 3);\n" +
                "        } else {\n" +
                "            System.out.println(x + 4);\n" +
                "        }\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void emptyIfValid() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(int x) {\n" +
                "        if (x < 0) {\n" +
                "        } else {\n" +
                "        }\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void ifIntegerCondition() throws Exception {
        expect(
                typeError("cond", new BooleanType(), new IntegerType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(int cond, int x, int y) {\n" +
                        "        if (cond) {\n" +
                        "            System.out.println(x + y);\n" +
                        "        } else {\n" +
                        "            System.out.println(x * y);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void ifIntegerLiteralCondition() throws Exception {
        expect(
                typeError("100", new BooleanType(), new IntegerType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(int x, int y) {\n" +
                        "        if (100) {\n" +
                        "            System.out.println(x + y);\n" +
                        "        } else {\n" +
                        "            System.out.println(x * y);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void ifObjectCondition() throws Exception {
        expect(
                typeError("cond", new BooleanType(), new ObjectType("C")),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(C cond, int x, int y) {\n" +
                        "        if (cond) {\n" +
                        "            System.out.println(x + y);\n" +
                        "        } else {\n" +
                        "            System.out.println(x * y);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void ifLeftBranchError() throws Exception {
        expect(
                typeError("false", new IntegerType(), new BooleanType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(boolean cond, int x, int y) {\n" +
                        "        if (cond) {\n" +
                        "            System.out.println(false);\n" +
                        "        } else {\n" +
                        "            System.out.println(0);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void ifRightBranchError() throws Exception {
        expect(
                typeError("false", new IntegerType(), new BooleanType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(boolean cond, int x, int y) {\n" +
                        "        if (cond) {\n" +
                        "            System.out.println(0);\n" +
                        "        } else {\n" +
                        "            System.out.println(false);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void nestedIfWrong() throws Exception {
        expect(
                typeError("false", new IntegerType(), new BooleanType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(boolean cond, int x, int y) {\n" +
                        "        if (cond) {\n" +
                        "            if (!cond) {\n" +
                        "                System.out.println(0);\n" +
                        "            } else {\n" +
                        "                System.out.println(false);\n" +
                        "            }\n" +
                        "        } else {\n" +
                        "            System.out.println(0);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void whileValid() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(boolean b, int x, int y) {\n" +
                "        while (b) {\n" +
                "            System.out.println(x + y);\n" +
                "        }\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void nestedWhileValid() throws Exception {
        accept(defaultMainClass +
                "class C {\n" +
                "    public int f(boolean b, int x, int y) {\n" +
                "        while (b) {\n" +
                "            while (true) {\n" +
                "                System.out.println(x + y);\n" +
                "            }\n" +
                "            System.out.println(x + y);\n" +
                "        }\n" +
                "        return 0;\n" +
                "    }\n" +
                "}"
        );
    }

    @Test
    public void whileIntegerCondition() throws Exception {
        expect(
                typeError("cond", new BooleanType(), new IntegerType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(int cond, int x, int y) {\n" +
                        "        while (cond) {\n" +
                        "            System.out.println(x + y);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void whileIntegerLiteralCondition() throws Exception {
        expect(
                typeError("100", new BooleanType(), new IntegerType()),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(int x, int y) {\n" +
                        "        while (100) {\n" +
                        "            System.out.println(x + y);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    @Test
    public void whileObjectCondition() throws Exception {
        expect(
                typeError("cond", new BooleanType(), new ObjectType("C")),
                defaultMainClass +
                        "class C {\n" +
                        "    public int f(C cond, int x, int y) {\n" +
                        "        while (cond) {\n" +
                        "            System.out.println(x + y);\n" +
                        "        }\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
    }

    // --------------------------------------------------
    // Test group 4: expressions
    // TODO: need more failure tests
}
