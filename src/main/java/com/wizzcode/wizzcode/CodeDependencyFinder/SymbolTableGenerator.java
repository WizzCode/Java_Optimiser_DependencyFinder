package com.wizzcode.wizzcode.CodeDependencyFinder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.Node;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
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

    //DEPENDENCY BETWEEN METHOD AND METHOD CALL
    //parent = qualified name of node that contains methodCall
    //lhsVariableName = qualified name of LHS if assignment, else pass null
    private void findDependenciesForMethodCallExpr(MethodCallExpr methodCallExpr, String parent, String lhsVariableName){
        String methodCallName = jpObj.getQualifiedName(methodCallExpr);

        //if methodCallName is not present in attribute array, the method belongs to an imported class, hence no need to find dependency with it
        if (paObj.attribute_array.contains(methodCallName)) {
            addToRightArray(methodCallName);
            addToDependenceDict(parent, methodCallName);
            if (lhsVariableName!=null) {
                addToDependenceDict(lhsVariableName, methodCallName);
            }
        }
    }

    //DEPENDENCY BETWEEN NODE FROM WHERE METHOD IS CALLED AND THE METHOD CALL ARGUMENTS
    //parent = qualified name of node that contains methodCall
    //lhsVariableName = qualified name of LHS if assignment, else pass null
    private void findDependenciesForMethodCallArgs(MethodCallExpr methodCallExpr, String parent, String lhsVariableName){
        for(Expression expr: methodCallExpr.getArguments()){
            String argName = jpObj.getQualifiedName(expr);
            if (!argName.contains(".") && !argName.equals("")){
                argName = parent + "." + argName;
            }
            addToAttributeArray(argName);
            addToRightArray(argName);
            addToDependenceDict(parent, argName);
            if (lhsVariableName!=null) {
                addToDependenceDict(lhsVariableName, argName);
            }
        }
    }

    //parentName = Node that contains the Expression expr
    //dependencySources = list of nodes on which qualifiedNameLHS depends
    private void findDependenciesInRHS(Expression expr, String qualifiedNameLHS, String parentName, ArrayList<String> dependencySources) {
        String qualifiedNameRHS = "";
        if (expr instanceof MethodCallExpr) {
            findDependenciesForMethodCallExpr((MethodCallExpr)expr, parentName, qualifiedNameLHS);
            findDependenciesForMethodCallArgs((MethodCallExpr)expr, parentName, qualifiedNameLHS);
        } else if (expr instanceof ObjectCreationExpr) {
            qualifiedNameRHS = jpObj.getQualifiedName(expr);
            addToAttributeArray(qualifiedNameRHS);
            addToRightArray(qualifiedNameRHS);
            addToDependenceDict(qualifiedNameLHS, qualifiedNameRHS, dependencySources);
        } else if (expr instanceof BinaryExpr) {
            ArrayList<Node> subExprList = new ArrayList<>(expr.getChildNodes());
//            dependencySources = evalBinaryExprAttributes(parentName, subExprList, dependencySources);

            String subExprName;
            Node subExpr;
            for (int i = 0; i < subExprList.size(); i++) {
                subExpr = subExprList.get(i);
                subExprName = jpObj.getQualifiedName(subExpr);
                if (!subExprName.contains(".") && !subExprName.equals("")){
                    subExprName = subExprName + "." + subExprName;
                }
                addToAttributeArray(subExprName);
                addToRightArray(subExprName);

                if (subExpr instanceof MethodCallExpr) {
                    findDependenciesForMethodCallExpr((MethodCallExpr)subExpr, parentName, qualifiedNameLHS);
                    findDependenciesForMethodCallArgs((MethodCallExpr)subExpr, parentName, qualifiedNameLHS);
                }

                if (!subExprName.equals("") && !dependencySources.contains(subExprName)){
                    dependencySources.add(subExprName);
                }
            }
            paObj.dependence_dict.put(qualifiedNameLHS, dependencySources);
        }
        else {
            qualifiedNameRHS = jpObj.getQualifiedName(expr);
            addToAttributeArray(qualifiedNameRHS);
            addToRightArray(qualifiedNameRHS);
            addToDependenceDict(qualifiedNameLHS, qualifiedNameRHS, dependencySources);
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

                //DECLARATIONS IN PARAMETER
                NodeList<Parameter> methodParameters = method.getParameters();
                for(Parameter pr:methodParameters){
                    String parameterVariableName = methodName + "." + pr.getNameAsExpression();
                    addToAttributeArray(parameterVariableName);
                    addToRightArray(parameterVariableName);
                    addToDependenceDict(methodName, parameterVariableName); //method depends on the variable
//                System.out.println(jpObj.getQualifiedName(pr.getNameAsExpression()));
                }

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
                        findDependenciesForMethodCallExpr(methodCallExpr, methodName, null); //method depends on the called method
                        findDependenciesForMethodCallArgs(methodCallExpr, methodName, null);
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