package com.wizzcode.wizzcode.CodeDependencyFinder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.CodeGenerationUtils;
import com.github.javaparser.utils.Log;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.Node;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.wizzcode.wizzcode.utils.JavaSymbolSolverUtils;

import java.util.ArrayList;

public class SymbolTableGenerator {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;
    public JavaParser parser;
    public CombinedTypeSolver combinedTypeSolver;
    public JavaSymbolSolver symbolSolver;
    public CompilationUnit compilationUnit;

    public void symbolsolverparsing(String path) throws FileNotFoundException {
        System.out.println("Creating Compilation Unit");
        combinedTypeSolver = new CombinedTypeSolver();
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        ParseResult<CompilationUnit> parseResultcompilationUnit;
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add((new JavaParserTypeSolver("src/main/java")));
        combinedTypeSolver.add((new JavaParserTypeSolver("src/test/java")));
        symbolSolver = new JavaSymbolSolver(this.combinedTypeSolver);
        parserConfiguration.setSymbolResolver(this.symbolSolver);
        parser = new JavaParser(parserConfiguration);
        // FileInputStream in = new
        // FileInputStream("src/main/resources/SampleProgramNew.java");
        FileInputStream in = new FileInputStream(path);
        parseResultcompilationUnit = this.parser.parse(in);
        compilationUnit = parseResultcompilationUnit.getResult().get();
    }

    public ProgramAttributes attributes() {

        JavaSymbolSolverUtils ssObj = new JavaSymbolSolverUtils();
        ProgramAttributes paObj = new ProgramAttributes();

        System.out.println("-------------------------------");

        for (ClassOrInterfaceDeclaration classOrInterface : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {

            String classOrInterfaceName = classOrInterface.getNameAsString();
            System.out.println("Class: " + classOrInterfaceName);

            if (!paObj.attribute_array.contains(classOrInterfaceName)) {
                paObj.attribute_array.add(classOrInterface.getNameAsString()); // class
            }

            System.out.println("Class Variables: ");
            for (FieldDeclaration ff : classOrInterface.getFields()) {
                System.out.println("Qualified Name: " + classOrInterface.getNameAsString() + "." + ff.getVariable(0).getName());
                ArrayList<String> dependents = new ArrayList<String>();
                String qname = classOrInterface.getNameAsString() + "." + ff.getVariable(0).getName();
                if (paObj.dependence_dict.containsKey(qname)) {
                    dependents = paObj.dependence_dict.get(qname);
                }

                if (!paObj.attribute_array.contains(classOrInterface.getNameAsString() + "." + ff.getVariable(0).getName()))
                    paObj.attribute_array.add(classOrInterface.getNameAsString() + "." + ff.getVariable(0).getName()); // class
                                                                                                         // variable
                if (ff.getVariable(0).getInitializer().isPresent()) {

                    Node parentNode = ff.getVariable(0).getInitializer().get();
                    String name = "";
                    if (parentNode instanceof MethodCallExpr) {
                        name = ssObj.getQualifiedName(parentNode);
                        if (!name.equals(""))
                            System.out.println("Qualified Name: " + name);

                    }

                    else if (parentNode instanceof ObjectCreationExpr) {
                        name = ff.getVariable(0).getType().toString();
                        System.out.println(
                                "Qualified name of class whose obj is created: " + ff.getVariable(0).getType());
                        String cname = classOrInterface.getNameAsString();
                        ArrayList<String> classdependents = new ArrayList<String>();

                        if (paObj.dependence_dict.containsKey(cname)) {
                            classdependents = paObj.dependence_dict.get(cname);
                        }
                        if (!name.equals("") && !classdependents.contains(name))
                            classdependents.add(name);
                        paObj.dependence_dict.put(cname, classdependents);

                    }

                    else if (parentNode instanceof BinaryExpr) {
                        name = "";

                        ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                        for (int i = 0; i < subExprList.size(); i++) {
                            name = ssObj.getQualifiedName(subExprList.get(i));
                            if (!name.equals(""))
                                System.out.println("Qualified Name: " + name);
                            if (!name.equals("") && !paObj.attribute_array.contains(name))
                                paObj.attribute_array.add(name);
                            if (!name.equals("") && !paObj.right.contains(name))
                                paObj.right.add(name);
                            if (!dependents.contains(name) && !name.equals(""))
                                dependents.add(name);
                        }
                    } else {
                        name = ssObj.getQualifiedName(parentNode);
                        if (!name.equals(""))
                            System.out.println("Qualified Name: " + name);
                    }

                    if (!name.equals("") && !paObj.attribute_array.contains(name))
                        paObj.attribute_array.add(name);
                    if (!name.equals("") && !paObj.right.contains(name))
                        paObj.right.add(name);
                    if (!dependents.contains(name) && !name.equals(""))
                        dependents.add(name);
                    String temp = classOrInterface.getNameAsString() + "." + ff.getVariable(0).getName();
                    if (!dependents.isEmpty())
                        paObj.dependence_dict.put(temp, dependents);
                }

            }

            for (MethodDeclaration method : classOrInterface.getMethods()) {

                System.out.println("Method: ");
                System.out.println("Qualified Name: " + classOrInterface.getNameAsString() + "." + method.getNameAsString());
                if (!paObj.attribute_array.contains(classOrInterface.getNameAsString() + "." + method.getNameAsString()))
                    paObj.attribute_array.add(classOrInterface.getNameAsString() + "." + method.getNameAsString());
                System.out.println("Method Variables");

                method.getBody().ifPresent(blockStatement -> {

                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {

                        ArrayList<String> dependents = new ArrayList<String>();
                        String lhsDecname = classOrInterface.getNameAsString() + "." + method.getNameAsString() + "."
                                + variable.getNameAsString();
                        if (paObj.dependence_dict.containsKey(lhsDecname)) {
                            dependents = paObj.dependence_dict.get(lhsDecname);
                        }

                        if (variable.getInitializer().isPresent()) {

                            if (!lhsDecname.equals("") && !paObj.attribute_array.contains(lhsDecname))
                                paObj.attribute_array.add(lhsDecname);
                            if (!lhsDecname.equals(""))
                                System.out.println("Qualified name: " + lhsDecname);

                            Node parentNode = variable.getInitializer().get();
                            String name = "";

                            if (parentNode instanceof MethodCallExpr) {
                                String qname = classOrInterface.getNameAsString() + "." + method.getNameAsString();
                                ArrayList<String> methoddependents = new ArrayList<String>();
                                name = ssObj.getQualifiedName(parentNode);
                                if (paObj.dependence_dict.containsKey(qname)) {
                                    methoddependents = paObj.dependence_dict.get(qname);
                                }
                                if (!name.equals("") && !methoddependents.contains(name))
                                    methoddependents.add(name);
                                paObj.dependence_dict.put(qname, methoddependents);
                                if (!name.equals(""))
                                    System.out.println("Qualified Name: " + name);
                            }

                            else if (parentNode instanceof ObjectCreationExpr) {
                                name = variable.getTypeAsString();
                                System.out
                                        .println("Qualified name of class whose obj is created: " + variable.getType());
                                String cname = classOrInterface.getNameAsString();
                                ArrayList<String> classdependents = new ArrayList<String>();

                                if (paObj.dependence_dict.containsKey(cname)) {
                                    classdependents = paObj.dependence_dict.get(cname);
                                }
                                if (!name.equals("") && !classdependents.contains(name))
                                    classdependents.add(name);
                                paObj.dependence_dict.put(cname, classdependents);

                            } else if (parentNode instanceof BinaryExpr) {
                                name = "";

                                ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                                for (int i = 0; i < subExprList.size(); i++) {

                                    name = ssObj.getQualifiedName(subExprList.get(i));
                                    if (!name.contains(".") && !name.equals(""))
                                        name = classOrInterface.getNameAsString() + "." + method.getNameAsString() + "." + name;
                                    if (!name.equals(""))
                                        System.out.println("Qualified Name: " + name);
                                    if (!name.equals("") && !paObj.right.contains(name))
                                        paObj.right.add(name);
                                    if (!name.equals("") && !dependents.contains(name))
                                        dependents.add(name);

                                }

                            } else {
                                name = ssObj.getQualifiedName(parentNode);
                                if (!name.equals(""))
                                    System.out.println("Qualified Name: " + name);
                            }
                            if (!name.equals("") && !paObj.attribute_array.contains(name))
                                paObj.attribute_array.add(name);
                            if (!name.equals("") && !paObj.right.contains(name))
                                paObj.right.add(name);
                            if (!dependents.contains(name) && !name.equals(""))
                                dependents.add(name);
                            if (!dependents.isEmpty())
                                paObj.dependence_dict.put(lhsDecname, dependents);
                        }

                        for (NameExpr nameExp : blockStatement.findAll(NameExpr.class)) {
                            dependents = new ArrayList<String>();
                            if (nameExp.getNameAsString().equals(variable.getNameAsString())) {
                                // System.out.println("Qualified Name: "
                                // +classOrInterface.getNameAsString()+"."+method.getNameAsString()+"."+nameExp.getNameAsString());
                                Node parentNode = nameExp.getParentNode().get();
                                // System.out.println("Variable used in Expression: "+parentNode);
                                // System.out.println("Expression type: "+parentNode.getClass());
                                if (parentNode instanceof MethodCallExpr) {
                                    String qname = classOrInterface.getNameAsString() + "." + method.getNameAsString();
                                    ArrayList<String> methoddependents = new ArrayList<String>();
                                    String name = ssObj.getQualifiedName(parentNode);
                                    if (paObj.dependence_dict.containsKey(qname)) {
                                        methoddependents = paObj.dependence_dict.get(qname);
                                    }
                                    if (!methoddependents.contains(name))
                                        methoddependents.add(name);
                                    paObj.dependence_dict.put(qname, methoddependents);
                                    if (!name.equals(""))
                                        System.out.println("Qualified Name: " + name);
                                    if (!name.equals("") && !paObj.attribute_array.contains(name))
                                        paObj.attribute_array.add(name);
                                    if (!name.equals("") && !paObj.right.contains(name))
                                        paObj.right.add(name);
                                }

                                else if (parentNode instanceof ObjectCreationExpr) {
                                    System.out.println(
                                            "Qualified name of class whose obj is created: " + variable.getType());
                                    if (!variable.getType().toString().equals("")
                                            && !paObj.attribute_array.contains(variable.getType().toString()))
                                        paObj.attribute_array.add(variable.getType().toString());
                                    String cname = classOrInterface.getNameAsString();
                                    String name = variable.getType().toString();
                                    ArrayList<String> classdependents = new ArrayList<String>();

                                    if (paObj.dependence_dict.containsKey(cname)) {
                                        classdependents = paObj.dependence_dict.get(cname);
                                    }
                                    if (!name.equals("") && !classdependents.contains(name))
                                        classdependents.add(name);
                                    paObj.dependence_dict.put(cname, classdependents);
                                } else if (parentNode instanceof AssignExpr) {
                                    String name = "";

                                    Expression left = ((AssignExpr) parentNode).getTarget();
                                    Expression right = ((AssignExpr) parentNode).getValue();
                                    System.out.println("LHS: " + left);
                                    String leftstr = ssObj.getQualifiedName(left);
                                    if (!leftstr.contains(".") && !leftstr.equals(""))
                                        leftstr = classOrInterface.getNameAsString() + "." + method.getNameAsString() + "." + leftstr;
                                    System.out.println("LHS Qualified Name: " + leftstr);

                                    System.out.println("RHS: " + right);
                                    if (right instanceof BinaryExpr) {
                                        //
                                        ArrayList<Node> subExprList = new ArrayList<>(right.getChildNodes());
                                        for (int i = 0; i < subExprList.size(); i++) {
                                            name = ssObj.getQualifiedName(subExprList.get(i));
                                            if (!name.contains(".") && !name.equals(""))
                                                name = classOrInterface.getNameAsString() + "." + method.getNameAsString() + "."
                                                        + name;
                                            if (!name.equals("") && !paObj.right.contains(name))
                                                paObj.right.add(name);
                                            if (!name.equals("") && !dependents.contains(name))
                                                dependents.add(name);

                                            if (!name.equals(""))
                                                System.out.println("RHS Qualified Name: " + name);
                                        }
                                    }
                                    if (!leftstr.equals("") && !paObj.attribute_array.contains(leftstr))
                                        paObj.attribute_array.add(leftstr);
                                    if (!dependents.isEmpty())
                                        paObj.dependence_dict.put(leftstr, dependents);

                                } else {
                                    String name = ssObj.getQualifiedName(parentNode);
                                    if (!name.equals(""))
                                        System.out.println("Qualified Name: " + name);
                                    if (!name.equals("") && !paObj.attribute_array.contains(name))
                                        paObj.attribute_array.add(name);
                                }

                            }

                        }

                    }
                });
            }
        }
        System.out.println("-------------Attribute array------------------");

        for (int i = 0; i < paObj.attribute_array.size(); i++)
            System.out.println(paObj.attribute_array.get(i));

        System.out.println("-------------Right array------------------");
        for (int i = 0; i < paObj.right.size(); i++)
            System.out.println(paObj.right.get(i));

        System.out.println("-------------Dep dict------------------");

        for (String name : paObj.dependence_dict.keySet()) {
            String key = name.toString();
            ArrayList<String> value = paObj.dependence_dict.get(name);
            System.out.println(key + " " + value);
        }

        return paObj;

    }

    public static boolean isStringOnlyAlphabet(String str) {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}
