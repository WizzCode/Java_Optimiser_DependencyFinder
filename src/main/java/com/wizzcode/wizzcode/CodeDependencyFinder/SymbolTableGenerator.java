package com.wizzcode.wizzcode.CodeDependencyFinder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
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
import com.wizzcode.wizzcode.utils.JavaParserUtils;

import java.util.ArrayList;

public class SymbolTableGenerator {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;
    public JavaParser parser;
    public CombinedTypeSolver combinedTypeSolver;
    public JavaSymbolSolver symbolSolver;
    public CompilationUnit compilationUnit;
    ProgramAttributes paObj = new ProgramAttributes();
    JavaParserUtils jpObj = new JavaParserUtils();

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

    private void addToAttributeArray(String name){
        if (!name.equals("") && !paObj.attribute_array.contains(name)) {
            paObj.attribute_array.add(name);
        }
    }

    private void addToRightArray(String name){
        if (!name.equals("") && !paObj.right.contains(name)) {
            paObj.right.add(name);
        }
    }

    private void addToDependenceDict(String dependentNode, String currentNode){
        ArrayList<String> dependents = new ArrayList<String>();
        if (paObj.dependence_dict.containsKey(dependentNode)) {
            dependents = paObj.dependence_dict.get(dependentNode);
        }
        if (!currentNode.equals("") && !dependents.contains(currentNode))
            dependents.add(currentNode);
        paObj.dependence_dict.put(dependentNode, dependents);
    }
    
    private ArrayList<String> evalBinaryExprAttributes(String className, String methodName, ArrayList<Node> subExprList, ArrayList<String> dependents){
       String name;

       for (int i = 0; i < subExprList.size(); i++) {

            name = jpObj.getQualifiedName(subExprList.get(i));
            if (!name.contains(".") && !name.equals(""))
                name = className + "." + methodName + "." + name;
            if (!name.equals(""))
                System.out.println("Qualified Name: " + name);
            addToRightArray(name);
            if (!name.equals("") && !dependents.contains(name))
                dependents.add(name);

        }
       return dependents;
    }

    public ProgramAttributes attributes() {

//        JavaParserUtils jpObj = new JavaParserUtils();
//        ProgramAttributes paObj = new ProgramAttributes();

        System.out.println("-------------------------------");

        for (ClassOrInterfaceDeclaration classOrInterface : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {

            String classOrInterfaceName = classOrInterface.getNameAsString();
            System.out.println("Class: " + classOrInterfaceName);

            addToAttributeArray(classOrInterfaceName);

            System.out.println("Class Variables: ");
            for (FieldDeclaration ff : classOrInterface.getFields()) {
                System.out.println("Qualified Name: " + classOrInterfaceName + "." + ff.getVariable(0).getName());
                ArrayList<String> dependents = new ArrayList<String>();
                String qname = classOrInterfaceName + "." + ff.getVariable(0).getName();
                if (paObj.dependence_dict.containsKey(qname)) {
                    dependents = paObj.dependence_dict.get(qname);
                }
                addToAttributeArray(qname);

                if (ff.getVariable(0).getInitializer().isPresent()) {

                    Node parentNode = ff.getVariable(0).getInitializer().get();
                    String name = "";
                    if (parentNode instanceof MethodCallExpr) {
                        name = jpObj.getQualifiedName(parentNode);
                        if (!name.equals(""))
                            System.out.println("Qualified Name: " + name);

                    }

                    else if (parentNode instanceof ObjectCreationExpr) {
                        name = ff.getVariable(0).getType().toString();
                        System.out.println(
                                "Qualified name of class whose obj is created: " + ff.getVariable(0).getType());
                        String cname = classOrInterface.getNameAsString();

                        addToDependenceDict(cname, name);

                    }

                    else if (parentNode instanceof BinaryExpr) {
                        ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                        dependents = evalBinaryExprAttributes("","",subExprList,dependents);

                    } else {
                        name = jpObj.getQualifiedName(parentNode);
                        if (!name.equals(""))
                            System.out.println("Qualified Name: " + name);
                    }

                    addToAttributeArray(name);
                    addToRightArray(name);
                    if (!dependents.contains(name) && !name.equals(""))
                        dependents.add(name);
                    String temp = classOrInterface.getNameAsString() + "." + ff.getVariable(0).getName();
                    if (!dependents.isEmpty())
                        paObj.dependence_dict.put(temp, dependents);
                }

            }

            for (MethodDeclaration method : classOrInterface.getMethods()) {

                System.out.println("Method: ");
                String methodName = classOrInterface.getNameAsString() + "." + method.getNameAsString();
                System.out.println("Qualified Name: " + methodName);
                addToAttributeArray(methodName);
                System.out.println("Method Variables");

                method.getBody().ifPresent(blockStatement -> {

                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {

                        ArrayList<String> dependents = new ArrayList<String>();
                        String lhsDecname = methodName + "." + variable.getNameAsString();
                        if (paObj.dependence_dict.containsKey(lhsDecname)) {
                            dependents = paObj.dependence_dict.get(lhsDecname);
                        }

                        if (variable.getInitializer().isPresent()) {

                            addToAttributeArray(lhsDecname);
                            if (!lhsDecname.equals(""))
                                System.out.println("Qualified name: " + lhsDecname);

                            Node parentNode = variable.getInitializer().get();
                            String name = "";

                            if (parentNode instanceof MethodCallExpr) {
                                String qname = classOrInterface.getNameAsString() + "." + method.getNameAsString();

                                name = jpObj.getQualifiedName(parentNode);
                                addToDependenceDict(qname, name);
                                if (!name.equals(""))
                                    System.out.println("Qualified Name: " + name);
                            }

                            else if (parentNode instanceof ObjectCreationExpr) {
                                name = variable.getTypeAsString();
                                System.out
                                        .println("Qualified name of class whose obj is created: " + variable.getType());
                                String cname = classOrInterface.getNameAsString();
                                addToDependenceDict(cname, name);

                            } else if (parentNode instanceof BinaryExpr) {
                                ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                                dependents = evalBinaryExprAttributes(
                                        classOrInterface.getNameAsString(),
                                        method.getNameAsString(),
                                                 subExprList, dependents );
                                

                            } else {
                                name = jpObj.getQualifiedName(parentNode);
                                if (!name.equals(""))
                                    System.out.println("Qualified Name: " + name);
                            }
                            addToAttributeArray(name);
                            addToRightArray(name);
                            if (!dependents.contains(name) && !name.equals(""))
                                dependents.add(name);
                            if (!dependents.isEmpty())
                                paObj.dependence_dict.put(lhsDecname, dependents);
                        }

                        for (NameExpr nameExp : blockStatement.findAll(NameExpr.class)) {
                            dependents = new ArrayList<String>();
                            if (nameExp.getNameAsString().equals(variable.getNameAsString())) {

                                Node parentNode = nameExp.getParentNode().get();

                                if (parentNode instanceof MethodCallExpr) {
                                    String qname = classOrInterface.getNameAsString() + "." + method.getNameAsString();
                                    String name = jpObj.getQualifiedName(parentNode);

                                    addToDependenceDict(qname, name);
                                    if (!name.equals(""))
                                        System.out.println("Qualified Name: " + name);
                                    addToAttributeArray(name);
                                    addToRightArray(name);
                                }

                                else if (parentNode instanceof ObjectCreationExpr) {
                                    System.out.println(
                                            "Qualified name of class whose obj is created: " + variable.getType());
                                    String objVariableName = variable.getType().toString();
                                    addToAttributeArray(objVariableName);
                                    String cname = classOrInterface.getNameAsString();
                                    addToDependenceDict(cname, objVariableName);


                                } else if (parentNode instanceof AssignExpr) {
                                    String name = "";

                                    Expression left = ((AssignExpr) parentNode).getTarget();
                                    Expression right = ((AssignExpr) parentNode).getValue();
                                    System.out.println("LHS: " + left);
                                    String leftstr = jpObj.getQualifiedName(left);
                                    if (!leftstr.contains(".") && !leftstr.equals(""))
                                        leftstr = classOrInterface.getNameAsString() + "." + method.getNameAsString() + "." + leftstr;
                                    System.out.println("LHS Qualified Name: " + leftstr);

                                    System.out.println("RHS: " + right);
                                    if (right instanceof BinaryExpr) {
                                        
                                        ArrayList<Node> subExprList = new ArrayList<>(right.getChildNodes());
                                       dependents = evalBinaryExprAttributes(
                                        classOrInterface.getNameAsString(),
                                        method.getNameAsString(),
                                                 subExprList, dependents );
                                    }
                                    
                                    addToAttributeArray(leftstr);
                                    if (!dependents.isEmpty())
                                        paObj.dependence_dict.put(leftstr, dependents);

                                } else {
                                    String name = jpObj.getQualifiedName(parentNode);
                                    if (!name.equals(""))
                                        System.out.println("Qualified Name: " + name);
                                    addToAttributeArray(name);
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
