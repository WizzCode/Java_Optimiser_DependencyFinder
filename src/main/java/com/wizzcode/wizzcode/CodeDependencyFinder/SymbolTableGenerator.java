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
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.wizzcode.wizzcode.utils.JavaParserUtils;
import com.wizzcode.wizzcode.utils.MethodOverrideDetector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SymbolTableGenerator {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;
    public JavaParser parser;
    public CombinedTypeSolver combinedTypeSolver;
    public JavaSymbolSolver symbolSolver;
    public CompilationUnit compilationUnit;
    ProgramAttributes paObj = new ProgramAttributes();
    JavaParserUtils jpObj = new JavaParserUtils();
    Map<String, Map<String, String>> graphNodes = new LinkedHashMap<>();
    Map<Integer, Map<String, String>> IdgraphNodes = new LinkedHashMap<>();

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
    
    private void addNode(String name, String level, String type, String over, String thread){
        int id;
        if (!name.equals("") && !graphNodes.containsKey(name)) {
        id = graphNodes.size();
        Map<String, String> node = new LinkedHashMap<>();
        node.put("name",name);
        node.put("level",level);
        node.put("primaryType",type);
        node.put("overriding",over);
        node.put("thread",thread);
        int index = graphNodes.size();
        graphNodes.put(name,node);
        }
       
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
                addNode(argName, "4", "variable", "n", "n");
            }
            else addNode(argName, "2", "classVariable", "n", "n");
            
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
            addNode(qualifiedNameRHS, "2", "classVariable", "n", "n");
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
                    subExprName = parentName + "." + subExprName;
                    addNode(subExprName, "4", "variable", "n", "n");
                }
                else addNode(subExprName, "2", "classVariable", "n", "n");
                
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
            addNode(qualifiedNameRHS, "2", "classVariable", "n", "n");
            addToRightArray(qualifiedNameRHS);
            addToDependenceDict(qualifiedNameLHS, qualifiedNameRHS, dependencySources);
        }
    }

    public ProgramAttributes findAttributes() {
        
        //adding all class declarations to attribute array
        List<ClassOrInterfaceDeclaration> classList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration cls : classList) {
            String className = cls.getNameAsString();
            addToAttributeArray(className);
            addNode(className, "1", "class", "n", "n");
        }
        
        
        //List of Overriding Methods
        VoidVisitor<List<MethodDeclaration>> methodOverrideVisitor = new MethodOverrideDetector(compilationUnit);
        List <MethodDeclaration> overriders_collector = new ArrayList<>();
        methodOverrideVisitor.visit(compilationUnit,overriders_collector);
        List<String> overridingMethodsList = new ArrayList<String>();
        for (MethodDeclaration method : overriders_collector) {
            String methodQualifiedName = jpObj.getQualifiedName(method);
            overridingMethodsList.add(methodQualifiedName);
        }
        
         //adding all method declarations to attribute array
        List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);
        for (MethodDeclaration method : methods) {
            String methodQualifiedName = jpObj.getQualifiedName(method);
            String access = method.getAccessSpecifier().asString();
            if(access.isEmpty()) access ="default";
            String overFlag;
            addToAttributeArray(methodQualifiedName);
            if(overridingMethodsList.contains(methodQualifiedName)) overFlag = "y";
            else overFlag = "n";
            addNode(methodQualifiedName, "3", access + "Method", overFlag, "n");
        }

        //---------------------LOOP FOR CLASSES---------------------
        for (ClassOrInterfaceDeclaration classOrInterface : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {

            //className
            String classOrInterfaceName = classOrInterface.getNameAsString();
            addToAttributeArray(classOrInterfaceName);
            addNode(classOrInterfaceName, "1", "class", "n", "n");
            //Extended classes
            List<ClassOrInterfaceType> extendedTypes = classOrInterface.getExtendedTypes();
            for(int e =0;e<extendedTypes.size();e++){
                String extendedClassName = extendedTypes.get(e).getNameAsString();
                if (paObj.attribute_array.contains(extendedClassName)){
                addToRightArray(extendedClassName);
                addToDependenceDict(classOrInterfaceName,extendedClassName);
                }
            }
            
            
            //---------------------LOOP FOR CLASS VARIABLES---------------------
            for (FieldDeclaration fieldDeclaration : classOrInterface.getFields()) {
                //fieldDeclaration type contains entire line of code where a class variable has been declared

                if (fieldDeclaration.getVariables().size() > 0) {
                    VariableDeclarator fieldDeclarationAsVD = fieldDeclaration.getVariable(0);
                    String fieldName = classOrInterfaceName + "." + fieldDeclarationAsVD.getName();

                    addToAttributeArray(fieldName);
                    addNode(fieldName, "2", "classVariable", "n", "n");
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
                    addNode(parameterVariableName, "4", "variable", "n", "n");
                    addToRightArray(parameterVariableName);
                    addToDependenceDict(methodName, parameterVariableName); //method depends on the variable
//                System.out.println(jpObj.getQualifiedName(pr.getNameAsExpression()));
                }

                //DECLARATIONS INSIDE METHOD
                method.getBody().ifPresent(blockStatement -> {
                    for (VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
                        String lhsDeclarationName = methodName + "." + variable.getNameAsString();

                        addToAttributeArray(lhsDeclarationName);
                        addNode(lhsDeclarationName, "4", "variable", "n", "n");
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
                            addNode(lhsVariableName, "4", "variable", "n", "n");
                        }
                        else addNode(lhsVariableName, "2", "classVariable", "n", "n");

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

        //---------------------THREADS---------------------
        detectThread();

        int id = 0;
        for (Map.Entry<String, Map<String,String>> entry : graphNodes.entrySet()) {
            IdgraphNodes.put(id,  entry.getValue());
            id++;
        }
//        for (Map.Entry<Integer, Map<String,String>> entry : IdgraphNodes.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//        System.out.println(IdgraphNodes.size());
        
        return paObj;
        
        
    }
    
    
    //send hashmap to dependency.java
    public Map<Integer, Map<String, String>> getGraphNodes(){
        return IdgraphNodes;
    }
    
    private void detectThread(){
        // Traverse the AST and identify classes that extend or implement Thread or Runnable
        List<ClassOrInterfaceDeclaration> classOrInterfaceDeclns = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classOrInterfaceDecln : classOrInterfaceDeclns) {
            if (classOrInterfaceDecln.isAbstract()) {
                continue;
            }

            Boolean extendsThread = classOrInterfaceDecln.getExtendedTypes().stream().anyMatch(t -> t.getNameAsString().equals("Thread"));
            Boolean implementsRunnable = classOrInterfaceDecln.getImplementedTypes().stream().anyMatch(t -> t.getNameAsString().equals("Runnable"));

            if(extendsThread || implementsRunnable){
                // Check if the class overrides the run() method
                List<MethodDeclaration> runMethodList = classOrInterfaceDecln.getMethodsByName("run");
                if (runMethodList.size() > 0) {
                    String methodQualifiedName;
                    for(MethodDeclaration runMethod: runMethodList){
                        methodQualifiedName = jpObj.getQualifiedName(runMethod);
                        Map<String, String> threadnode = new LinkedHashMap<>();
                        threadnode = graphNodes.get(methodQualifiedName);
                        threadnode.replace("thread","y");
                        graphNodes.replace(methodQualifiedName, threadnode);
                    }
                }

                // Check for explicit calls to start() method
                for (MethodDeclaration methodDecln : classOrInterfaceDecln.getMethods()) {
                    String methodQualifiedName = jpObj.getQualifiedName(methodDecln);
                    List<MethodCallExpr> startCalls = methodDecln.findAll(MethodCallExpr.class, m -> m.getName().getIdentifier().equals("start"));
                    if (startCalls.size() > 0) {
                        Map<String, String> threadnode = new LinkedHashMap<>();
                        threadnode = graphNodes.get(methodQualifiedName);
                        threadnode.replace("thread","y");
                        graphNodes.replace(methodQualifiedName, threadnode);
                    }
                }
            }

//             Check for synchronized blocks or methods
//            List<SynchronizedStmt> syncStmts = classDecl.findAll(SynchronizedStmt.class);
//            if (syncStmts.size() > 0) {
//                System.out.println("Class " + classDecl.getName() + " has synchronized blocks or methods");
//            }
        }
    }

    public static boolean isStringOnlyAlphabet(String str) {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}