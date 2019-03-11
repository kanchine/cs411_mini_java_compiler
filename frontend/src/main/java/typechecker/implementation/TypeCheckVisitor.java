package typechecker.implementation;

import ast.AST;
import ast.And;
import ast.ArrayAssign;
import ast.ArrayLength;
import ast.ArrayLookup;
import ast.Assign;
import ast.Block;
import ast.BlockType;
import ast.BooleanLiteral;
import ast.BooleanType;
import ast.Call;
import ast.ClassDecl;
import ast.ClassType;
import ast.Conditional;
import ast.Expression;
import ast.FunctionDecl;
import ast.FunctionType;
import ast.IdentifierExp;
import ast.If;
import ast.IntArrayType;
import ast.IntegerLiteral;
import ast.IntegerType;
import ast.LessThan;
import ast.MainClass;
import ast.MethodDecl;
import ast.MethodType;
import ast.Minus;
import ast.NewArray;
import ast.NewObject;
import ast.NodeList;
import ast.Not;
import ast.ObjectType;
import ast.Plus;
import ast.Print;
import ast.Program;
import ast.This;
import ast.Times;
import ast.Type;
import ast.UnknownType;
import ast.VarDecl;
import ast.While;
import typechecker.ErrorReport;
import util.ImpTable;
import visitor.Visitor;

import java.util.ArrayList;
import java.util.List;


/**
 * This class implements Phase 2 of the Type Checker. This phase
 * assumes that we have already constructed the program's symbol table in
 * Phase1.
 * <p>
 * Phase 2 checks for the use of undefined identifiers and type errors.
 * <p>
 * Visitors may return a Type as a result. Generally, only visiting
 * an expression or a type actually returns a type.
 * <p>
 * Visiting other nodes just returns null.
 *
 * @author kdvolder
 */
public class TypeCheckVisitor implements Visitor<Type> {

    /**
     * The place to send error messages to.
     */
    private ErrorReport errors;

    /**
     * The symbol table from Phase 1.
     */
    private ImpTable<Type> variables;
    private ImpTable<Type> classFields = null;                     // Symbol table for class fields
    private ImpTable<Type> classMethods = null;                    // Symbol table for class methods
    private ImpTable<Type> methodScope = null;                     // Symbol table for a particular method
    private ImpTable<Type> blockScope = null;                      // Symbol table for a particular block
    private String className = null;


    public TypeCheckVisitor(ImpTable<Type> variables, ErrorReport errors) {
        this.variables = variables;
        this.errors = errors;
    }

    //// Helpers /////////////////////

    /**
     * Check whether the type of a particular expression is as expected.
     */
    private void check(Expression exp, Type expected) {
        Type actual = exp.accept(this);
        if (!assignableFrom(expected, actual))
            errors.typeError(exp, expected, actual);
    }

    /**
     * Check whether two types in an expression are the same
     */
    private void check(Expression exp, Type t1, Type t2) {
        if (!t1.equals(t2))
            errors.typeError(exp, t1, t2);
    }

    private boolean assignableFrom(Type varType, Type valueType) {
        if (varType instanceof ClassType && valueType instanceof ClassType) {
            String varTypeName = ((ClassType) varType).name;
            String valueTypeName = ((ClassType) valueType).name;

            for (String ptr = valueTypeName; ptr != null; ) {
                if (ptr.equals(varTypeName)) {
                    return true;
                }

                ClassType currType = (ClassType) variables.lookup(ptr);
                ptr = currType.superName;
            }
            return false;
        } else {
            return varType.equals(valueType);
        }
    }

    ///////// Visitor implementation //////////////////////////////////////

    @Override
    public <T extends AST> Type visit(NodeList<T> ns) {
        for (int i = 0; i < ns.size(); i++) {
            ns.elementAt(i).accept(this);
        }
        return null;
    }

    @Override
    public Type visit(Program n) {
        //		variables = applyInheritance(variables);
        n.mainClass.accept(this);
        n.classes.accept(this);
        return null;
    }

    @Override
    public Type visit(BooleanType n) {
        return n;
    }

    @Override
    public Type visit(IntegerType n) {
        return n;
    }

    @Override
    public Type visit(UnknownType n) {
        return n;
    }

    /**
     * Can't use check, because print allows either Integer or Boolean types
     */
    @Override
    public Type visit(Print n) {
        Type actual = n.exp.accept(this);
        if (!assignableFrom(new IntegerType(), actual) && !assignableFrom(new BooleanType(), actual)) {
            List<Type> l = new ArrayList<Type>();
            l.add(new IntegerType());
            l.add(new BooleanType());
            errors.typeError(n.exp, l, actual);
        }
        return null;
    }

    @Override
    public Type visit(Assign n) {
        check(n.value, lookup(n.name));
        return null;
    }

    @Override
    public Type visit(LessThan n) {
        check(n.e1, new IntegerType());
        check(n.e2, new IntegerType());
        n.setType(new BooleanType());
        return n.getType();
    }

    @Override
    public Type visit(Conditional n) {
        check(n.e1, new BooleanType());
        Type t2 = n.e2.accept(this);
        Type t3 = n.e3.accept(this);
        check(n, t2, t3);
        return t2;
    }

    @Override
    public Type visit(MethodType n) {
        return n;
    }

    @Override
    public Type visit(ClassType n) {
        return n;
    }

    @Override
    public Type visit(BlockType n) {
        return n;
    }

    @Override
    public Type visit(Plus n) {
        check(n.e1, new IntegerType());
        check(n.e2, new IntegerType());
        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(Minus n) {
        check(n.e1, new IntegerType());
        check(n.e2, new IntegerType());
        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(Times n) {
        check(n.e1, new IntegerType());
        check(n.e2, new IntegerType());
        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(IntegerLiteral n) {
        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(IdentifierExp n) {
        Type type = lookup(n.name);
        if (type == null)
            type = new UnknownType();
        return type;
    }

    @Override
    public Type visit(Not n) {
        check(n.e, new BooleanType());
        n.setType(new BooleanType());
        return n.getType();
    }

    @Override
    public Type visit(FunctionDecl n) {
        // We don't need to implement this
        throw new Error("Not implemented");
    }

    @Override
    public Type visit(VarDecl n) {
        // Nothing to be done here because type already set in building symbol table phrase
        return null;
    }

    @Override
    public Type visit(Call n) {
        MethodType methodType;
        if (n.receiver instanceof This) {
            // Check the method table in the current class
            Type type = classMethods.lookup(n.name);
            methodType = (MethodType) type;
            n.setType(methodType.returnType);

            if (methodType.formals.size() != n.rands.size())
                errors.wrongNumberOfArguments(methodType.formals.size(), n.rands.size());
            else {
                for (int i = 0; i < methodType.formals.size(); ++i) {
                    Type expectedType = methodType.formals.elementAt(i).type;
                    check(n.rands.elementAt(i), expectedType);
                }
            }

        } else if (n.receiver instanceof IdentifierExp) {
            // retrieve the method table in the corresponding class
            String name = ((IdentifierExp) n.receiver).name;
            Type type = lookup(name);
            ClassType classType = (ClassType) variables.lookup(((ObjectType) type).name);
            methodType = (MethodType) classType.methods.lookup(n.name);
            n.setType(methodType.returnType);

            if (methodType.formals.size() != n.rands.size())
                errors.wrongNumberOfArguments(methodType.formals.size(), n.rands.size());
            else {
                for (int i = 0; i < methodType.formals.size(); ++i) {
                    Type expectedType = methodType.formals.elementAt(i).type;
                    check(n.rands.elementAt(i), expectedType);
                }
            }
        } else {
            n.setType(new UnknownType());
        }

        return n.getType();
    }

    @Override
    public Type visit(FunctionType n) {
        return n;
    }

    @Override
    public Type visit(MainClass n) {
        n.statement.accept(this);
        return null;
    }

    @Override
    public Type visit(ClassDecl n) {
        classFields = n.classType.locals;
        classMethods = n.classType.methods;
        className = n.name;
        n.vars.accept(this);
        n.methods.accept(this);
        classFields = null;
        classMethods = null;
        return null;
    }

    @Override
    public Type visit(MethodDecl n) {
        methodScope = n.methodType.locals;
        n.vars.accept(this);
        n.statements.accept(this);
        check(n.returnExp, n.methodType.returnType);
        methodScope = null;
        return null;
    }

    @Override
    public Type visit(IntArrayType n) {
        return n;
    }

    @Override
    public Type visit(ObjectType n) {
        return n;
    }

    @Override
    public Type visit(Block n) {
        blockScope = n.blockType.locals;
        n.statements.accept(this);
        blockScope = null;
        return null;
    }

    @Override
    public Type visit(If n) {
        n.tst.accept(this);
        n.thn.accept(this);
        n.els.accept(this);

        return null;
    }

    @Override
    public Type visit(While n) {
        n.tst.accept(this);
        n.body.accept(this);
        return null;
    }

    @Override
    public Type visit(ArrayAssign n) {
        check(n.value, lookup(n.name));
        return null;
    }

    @Override
    public Type visit(And n) {
        check(n.e1, new BooleanType());
        check(n.e2, new BooleanType());

        n.setType(new BooleanType());
        return n.getType();
    }

    @Override
    public Type visit(ArrayLookup n) {
        check(n.array, new IntArrayType());
        check(n.index, new IntegerType());

        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(ArrayLength n) {
        check(n.array, new IntArrayType());

        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(BooleanLiteral n) {
        n.setType(new BooleanType());
        return n.getType();
    }

    @Override
    public Type visit(This n) {
        //
        throw new Error("Not implemented");
    }

    @Override
    public Type visit(NewArray n) {
        check(n.size, new IntegerType());
        return null;
    }

    @Override
    public Type visit(NewObject n) {
        return null;
    }

    // lookup a name in local, class, or global scope. This lookup method is private to BuildSymbolTableVisitor
    private Type lookup(String name) {
        // first lookup in local symbol table, if not found, then lookup in global symbol table (variables and functions)
        List<ImpTable<Type>> scopes = new ArrayList<>();
        // set up scopes look up order
        scopes.add(blockScope);
        scopes.add(methodScope);
        scopes.add(classFields);
        Type type = null;  // the type associated with the input name

        for (ImpTable<Type> scope : scopes) {
            if (scope != null) {
                type = scope.lookup(name);
                if (type != null)
                    return type;
            }
        }

        errors.undefinedId(name);

        return null;
    }

}
