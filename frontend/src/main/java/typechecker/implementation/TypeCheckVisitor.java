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

import java.lang.reflect.Method;
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
    private String currentClassName = null;


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
//    private void check(Expression exp, Type t1, Type t2) {
//        if (!t1.equals(t2))
//            errors.typeError(exp, t1, t2);
//    }

    private void check(Expression exp, Type t1, Type t2) {
        if (!assignableFrom(t1, t2))
            errors.typeError(exp, t1, t2);
    }

    // input may be null. need null check to avoid NullPointerException
    private boolean assignableFrom(Type varType, Type valueType) {
        if (varType == valueType) {
            return true;
        } else if (varType == null || valueType == null) {
            return false;
        } else if (varType instanceof ObjectType && valueType instanceof ObjectType) {
            String varTypeName = ((ObjectType) varType).name;
            String valueTypeName = ((ObjectType) valueType).name;

            while (valueTypeName != null) {
                if (valueTypeName.equals(varTypeName)) {
                    return true;
                }

                ClassType valueClassType = (ClassType) variables.lookup(valueTypeName);
                if (valueClassType == null) {
                    return false;
                }
                valueTypeName = valueClassType.superName;
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

        // reset context variables
        currentClassName = null;

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
        // Check variable referencing first
        Type type = lookup(n.name);
        Type valueType = n.value.accept(this);
        if (type == null) {
            errors.undefinedId(n.name);
            return null;
        }

        check(n.value, valueType, type);
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
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);

        check(n.e1, e1Type, new IntegerType());
        check(n.e2, e2Type, new IntegerType());
        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(Minus n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);

        check(n.e1, e1Type, new IntegerType());
        check(n.e2, e2Type, new IntegerType());
        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(Times n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);

        check(n.e1, e1Type, new IntegerType());
        check(n.e2, e2Type, new IntegerType());
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

        if (type == null) {
            errors.undefinedId(n.name);
            type = new UnknownType();
        }
        n.setType(type);
        return n.getType();
    }

    @Override
    public Type visit(Not n) {
        Type eType = n.e.accept(this);
        check(n.e, eType, new BooleanType());
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
        return null;
    }

    @Override
    public Type visit(Call n) {
        // check the following:
        // 1. the speicified method name exsits for the specifier receiver (look up current class and parent class)
        // 2. argument list of Call matches the parameter list of method declaration found in step 1

        Type rType = n.receiver.accept(this);
        String name;
        if (rType instanceof ObjectType)
            name = ((ObjectType) rType).name;
        else {
            errors.typeError(n.receiver, new ObjectType(""), rType);
            n.setType(new UnknownType());
            return n.getType();
        }

        Type type = lookupMethods(n, name);

        if (type == null) {
            // method name is not found
            errors.undefinedId(n.name);
            n.setType(new UnknownType());
            return n.getType();
        }

        MethodType methodType = (MethodType) type;

        n.setType(methodType.returnType);

        if (methodType.formals.size() != n.rands.size()) {
            errors.wrongNumberOfArguments(methodType.formals.size(), n.rands.size());
        } else {
            for (int i = 0; i < methodType.formals.size(); ++i) {
                Type expectedType = methodType.formals.elementAt(i).type;
                check(n.rands.elementAt(i), expectedType);
            }
        }

        return n.getType();
    }

    @Override
    public Type visit(FunctionType n) {
        return n;
    }

    @Override
    public Type visit(MainClass n) {
        currentClassName = n.className;
        n.statement.accept(this);
        currentClassName = null;
        return null;
    }

    @Override
    public Type visit(ClassDecl n) {
        classFields = n.classType.locals;
        classMethods = n.classType.methods;
        currentClassName = n.name;

        n.vars.accept(this);
        n.methods.accept(this);

        classFields = null;
        classMethods = null;
        currentClassName = null;

        return null;
    }

    @Override
    public Type visit(MethodDecl n) {
        // TODO need to correctly reject overloading  (same method name but different parameter list)
        //  1. reject overloading in current class (This part is done in the BuildSymbolTableVisitor, we prevent user to
        //  define methods of same name in the current class)
        //  2. reject overloading between current and parent class
        ClassType currClassType = (ClassType) variables.lookup(className);
        currClassType = (ClassType) variables.lookup(currClassType.superName);
        while (currClassType != null) {
            MethodType methodType = (MethodType) currClassType.methods.lookup(n.name);
            if (methodType != null) {
                if (methodType.formals.size() != n.methodType.formals.size() || !methodType.returnType.equals(n.methodType.returnType)) {
                    errors.duplicateDefinition(n.name);
                    return null;
                }

                for (int idx = 0; idx < methodType.formals.size(); ++idx) {
                    if (methodType.formals.elementAt(idx).type.equals(n.methodType.formals.elementAt(idx))) {
                        errors.duplicateDefinition(n.name);
                        return null;
                    }
                }
            }

            currClassType = (ClassType) variables.lookup(currClassType.superName);
        }

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
        Type valueType = n.value.accept(this);
        Type type = lookup(n.name);
        if (type == null) {
            errors.undefinedId(n.name);
            return null;
        }

        check(n.value, new IntArrayType(), type);
        check(n.value, new IntegerType(), valueType);
        return null;
    }

    @Override
    public Type visit(And n) {
        Type e1Type = n.e1.accept(this);
        Type e2Type = n.e2.accept(this);

        check(n.e1, e1Type, new BooleanType());
        check(n.e2, e2Type, new BooleanType());

        n.setType(new BooleanType());
        return n.getType();
    }

    @Override
    public Type visit(ArrayLookup n) {
        Type arrayType = n.array.accept(this);
        Type indexType = n.index.accept(this);

        check(n.array, arrayType, new IntArrayType());
        check(n.index, indexType, new IntegerType());

        n.setType(new IntegerType());
        return n.getType();
    }

    @Override
    public Type visit(ArrayLength n) {
        Type arrayType = n.array.accept(this);

        check(n.array, arrayType, new IntArrayType());

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
        // obsolete code
        // ClassType classType = (ClassType) variables.lookup(currentClassName);
        // n.setType(new ObjectType(classType.name));

        ObjectType type = new ObjectType(currentClassName);
        n.setType(type);
        return n.getType();
    }

    @Override
    public Type visit(NewArray n) {
        Type sizeType = n.size.accept(this);
        check(n.size, sizeType, new IntegerType());
        n.setType(new IntArrayType());
        return n.getType();
    }

    @Override
    public Type visit(NewObject n) {
        if (variables.lookup(n.typeName) == null)
            errors.undefinedId(n.typeName);
        return new ObjectType(n.typeName);
    }

    // lookup a variable name in block, method, or class scope
    private Type lookup(String name) {
        // lookup order: block -> method -> class scope (only lookup the next scope if not found in previous scope)
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


    private Type lookupMethods(Call n, String receiverTypeName) {
        ClassType receiverType = (ClassType) variables.lookup(receiverTypeName);

        // check whether the receiver class exists
        while (receiverType != null) {
            // check whether the receiver class has this method name
            Type methodType = receiverType.methods.lookup(n.name);
            if (methodType != null) {
                // found the method name
                return methodType;
            }

            // If not found, check whether the receiver's parent class has this method name in the next iteration
            receiverType = (ClassType) variables.lookup(receiverType.superName);
        }

        return null;
    }

}
