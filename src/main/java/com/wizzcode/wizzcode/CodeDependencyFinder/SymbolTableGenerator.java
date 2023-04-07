package com.wizzcode.wizzcode.CodeDependencyFinder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.Node;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.wizzcode.wizzcode.utils.JavaParserUtils;

import java.util.ArrayList;
import java.util.List;

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
        ArrayList<String> dependencySources = new ArrayList<String>();
        //dependence dict: key depends on list of values

        if (paObj.dependence_dict.containsKey(dependentNode)) {
            dependencySources = paObj.dependence_dict.get(dependentNode);
        }
        if (!currentNode.equals("") && !dependencySources.contains(currentNode)) {
            dependencySources.add(currentNode);
        }
        paObj.dependence_dict.put(dependentNode, dependencySources);
    }

    private void addToDependenceDict(String dependentNode, String currentNode, ArrayList<String> dependencySources) {
        if (!currentNode.equals("") && !dependencySources.contains(currentNode)) {
            dependencySources.add(currentNode);
        }
        paObj.dependence_dict.put(dependentNode, dependencySources);
    }

    private ArrayList<String> evalBinaryExprAttributes(String methodName, ArrayList<Node> subExprList, ArrayList<String> dependencySources) {
        String name;
        for (int i = 0; i < subExprList.size(); i++) {
            name = jpObj.getQualifiedName(subExprList.get(i));
            if (!name.contains(".") && !name.equals("")){
                name = methodName + "." + name;
            }
            addToAttributeArray(name);
            addToRightArray(name);
            if (!name.equals("") && !dependencySources.contains(name)){
                dependencySources.add(name);
            }
        }
        return dependencySources;
    }

    private void findDependenciesInRHS(Expression expr, String name, String parentName, ArrayList<String> dependencySources) {
        String qualifiedName = "";
        if (expr instanceof MethodCallExpr) {
            qualifiedName = jpObj.getQualifiedName(expr);
            addToAttributeArray(qualifiedName);
            addToRightArray(qualifiedName);
            addToDependenceDict(name, qualifiedName, dependencySources);
        } else if (expr instanceof ObjectCreationExpr) {
            qualifiedName = jpObj.getQualifiedName(expr);
            addToAttributeArray(qualifiedName);
            addToRightArray(qualifiedName);
            addToDependenceDict(name, qualifiedName, dependencySources);
        } else if (expr instanceof BinaryExpr) {
            ArrayList<Node> subExprList = new ArrayList<>(expr.getChildNodes());
            dependencySources = evalBinaryExprAttributes(parentName, subExprList, dependencySources);
            paObj.dependence_dict.put(name, dependencySources);
        } else {
            qualifiedName = jpObj.getQualifiedName(expr);
            addToAttributeArray(qualifiedName);
            addToRightArray(qualifiedName);
            addToDependenceDict(name, qualifiedName, dependencySources);
        }
    }

    public ProgramAttributes findAttributes() {
        //adding all method declarations to attribute array
        List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);
        for (MethodDeclaration method : methods) {
            String methodQualifiedName = jpObj.getQualifiedName(method);
            addToAttributeArray(methodQualifiedName);
        }

        //---------------------LOOP FOR CLASSES---------------------
        for (ClassOrInterfaceDeclaration classOrInterface : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {

            //className
            String classOrInterfaceName = classOrInterface.getNameAsString();
            addToAttributeArray(classOrInterfaceName);

            //---------------------LOOP FOR CLASS VARIABLES---------------------
            for (FieldDeclaration fieldDeclaration : classOrInterface.getFields()) {
                //fieldDeclaration type contains entire line of code where a class variable has been declared

                if (fieldDeclaration.getVariables().size() > 0) {
                    VariableDeclarator fieldDeclarationAsVD = fieldDeclaration.getVariable(0);
                    String fieldName = classOrInterfaceName + "." + fieldDeclarationAsVD.getName();

                    addToAttributeArray(fieldName);
                    addToDependenceDict(classOrInterfaceName, fieldName); //class depends on the field

                    if (fieldDeclarationAsVD.getInitializer().isPresent()) {
                        Expression fieldInitializerExpr = fieldDeclarationAsVD.getInitializer().get();
                        ArrayList<String> dependencySources = new ArrayList<String>();
                        if (paObj.dependence_dict.containsKey(fieldName)) {
                            dependencySources = paObj.dependence_dict.get(fieldName);
                        }
                        findDependenciesInRHS(fieldInitializerExpr, fieldName, classOrInterfaceName, dependencySources);
                    }
                }
            }

            //---------------------LOOP FOR METHODS---------------------
            for (MethodDeclaration method : classOrInterface.getMethods()) {
                String methodName = jpObj.getQualifiedName(method);
                addToDependenceDict(classOrInterfaceName, methodName);

                //DECLARATIONS INSIDE METHOD
                method.getBody().ifPresent(blockStatement -> {
                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
                        String lhsDeclarationName = methodName + "." + variable.getNameAsString();

                        addToAttributeArray(lhsDeclarationName);
                        addToDependenceDict(methodName, lhsDeclarationName); //method depends on the variable

                        if (variable.getInitializer().isPresent()) {
                            Expression rightExpr = variable.getInitializer().get();
                            ArrayList<String> dependencySources = new ArrayList<String>();
                            if (paObj.dependence_dict.containsKey(lhsDeclarationName)) {
                                dependencySources = paObj.dependence_dict.get(lhsDeclarationName);
                            }
                            findDependenciesInRHS(rightExpr, lhsDeclarationName, methodName, dependencySources);
                        }
                    }

                    //ASSIGNMENTS INSIDE METHOD
                    for (AssignExpr assignExpr : blockStatement.findAll(AssignExpr.class)) {
                        Expression lhsVariable = assignExpr.getTarget();
                        String lhsVariableName = jpObj.getQualifiedName(lhsVariable);
                        if (!lhsVariableName.contains(".") && !lhsVariableName.equals("")) {
                            lhsVariableName = methodName + "." + lhsVariableName;
                        }

                        addToAttributeArray(lhsVariableName);
                        addToDependenceDict(methodName, lhsVariableName); //method depends on the variable

                        Expression right = (assignExpr).getValue();
                        ArrayList<String> dependencySources = new ArrayList<String>();
                        if (paObj.dependence_dict.containsKey(lhsVariableName)) {
                            dependencySources = paObj.dependence_dict.get(lhsVariableName);
                        }
                        findDependenciesInRHS(right, lhsVariableName, methodName, dependencySources);
                    }

                    //METHOD CALLS INSIDE METHOD
                    for (MethodCallExpr methodCallExpr : blockStatement.findAll(MethodCallExpr.class)) {
                        String methodCallName = jpObj.getQualifiedName(methodCallExpr);

//                        addToAttributeArray(methodCallName);
                        if (paObj.attribute_array.contains(methodCallName)) {
                            addToRightArray(methodCallName);
                            addToDependenceDict(methodName, methodCallName); //Method depends on called method

                            if (methodCallExpr.isAssignExpr()) {
                                Expression lhsVariable = methodCallExpr.toAssignExpr().get().getTarget();
                                String lhsVariableName = jpObj.getQualifiedName(lhsVariable);

                                addToAttributeArray(lhsVariableName);
                                addToDependenceDict(methodName, lhsVariableName); //method depends on the variable
                                addToDependenceDict(lhsVariableName, methodCallName);
                            }
                        }
                    }
                });
            }
        }
        return paObj;
    }

    public static boolean isStringOnlyAlphabet(String str) {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}