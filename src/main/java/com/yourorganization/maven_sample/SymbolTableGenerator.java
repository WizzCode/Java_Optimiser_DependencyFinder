package com.yourorganization.maven_sample;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Some code that uses JavaParser.
 */
public class SymbolTableGenerator {
    //    public static void main(String[] args) {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;

    public void parseInputCode(String filename) {
        // JavaParser has a minimal logging class that normally logs nothing.
        // Let's ask it to write to standard out:
        Log.setAdapter(new Log.StandardOutStandardErrorAdapter());

        // SourceRoot is a tool that read and writes Java files from packages on a certain root directory.
        // In this case the root directory is found by taking the root from the current Maven module,
        // with src/main/resources appended.
        sourceRoot = new SourceRoot(CodeGenerationUtils.mavenModuleRoot(SymbolTableGenerator.class).resolve("src/main/resources"));

        // Our sample is in the root of this directory, so no package name.
        cu = sourceRoot.parse("", filename);
        Log.info("Compilation Unit generated");

        cu.accept(new ModifierVisitor<Void>() {
            /**
             * For every if-statement, see if it has a comparison using "!=".
             * Change it to "==" and switch the "then" and "else" statements around.
             */
            @Override
            public Visitable visit(IfStmt n, Void arg) {
                // Figure out what to get and what to cast simply by looking at the AST in a debugger! 
                n.getCondition().ifBinaryExpr(binaryExpr -> {
                    if (binaryExpr.getOperator() == BinaryExpr.Operator.NOT_EQUALS && n.getElseStmt().isPresent()) {
                        /* It's a good idea to clone nodes that you move around.
                            JavaParser (or you) might get confused about who their parent is!
                        */
                        Statement thenStmt = n.getThenStmt().clone();
                        Statement elseStmt = n.getElseStmt().get().clone();
                        n.setThenStmt(elseStmt);
                        n.setElseStmt(thenStmt);
                        binaryExpr.setOperator(BinaryExpr.Operator.EQUALS);
                    }
                });
                return super.visit(n, arg);
            }
        }, null);

    }

    public VariableAttributes variableDependencyInput() {

        VariableAttributes obj1= new VariableAttributes();
        for (TypeDeclaration typeDec : cu.getTypes()) {
            List<BodyDeclaration> members = typeDec.getMembers();
            if (members != null) {
                for (BodyDeclaration member : members) {
                    if (member.isMethodDeclaration()) {
                        MethodDeclaration function = (MethodDeclaration) member;
                        System.out.println("Method name: " + function.getNameAsString());

                    }
                }
            }
        }
        cu.findAll(VariableDeclarator.class).forEach(variable -> {
            String rightSide;
            System.out.println("Name " + variable.getNameAsString());
            obj1.variable_array.add(variable.getNameAsString());//adding to variable array


            System.out.println("Type " + variable.getTypeAsString());
//            System.out.println("Right side "+variable.getInitializer());
            if (variable.getInitializer().isPresent()) {
                rightSide = variable.getInitializer().get().toString();
                ArrayList<String> dependents = new ArrayList<String>();
                for (String val: rightSide.split(" "))
                {
                    if (isStringOnlyAlphabet(val))
                    {
                        if(!dependents.contains(val))
                        {
                            dependents.add(val);
                        }
                        if(!obj1.right.add(val))
                        {
                            obj1.right.add(val);
                        }
                       // this could add the same variable appearing twice ,
                        //two times
                    }
                }
                if(!dependents.isEmpty()) obj1.dependence_dict.put(variable.getNameAsString(),dependents);
                    // printing the final value.;

                System.out.println("Right side " + rightSide); // a
                System.out.println("Initialiser Type " + variable.getInitializer().get().getClass().getName());
            }
        });
        // This saves all the files we just read to an output directory.  
        sourceRoot.saveAll(
                // The path of the Maven module/project which contains the LogicPositivizer class.
                CodeGenerationUtils.mavenModuleRoot(SymbolTableGenerator.class)
                        // appended with a path to "output"
                        .resolve(Paths.get("output")));

      return obj1;
    }
    public static boolean isStringOnlyAlphabet(String str)
    {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}
