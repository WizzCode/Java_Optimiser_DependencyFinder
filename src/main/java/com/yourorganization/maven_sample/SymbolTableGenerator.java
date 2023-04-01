package com.yourorganization.maven_sample;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.ExpressionStmt;
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


import java.util.ArrayList;
import java.util.List;



public class SymbolTableGenerator {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;
    public JavaParser parser;
    public CombinedTypeSolver combinedTypeSolver;
    public JavaSymbolSolver symbolSolver;
    public CompilationUnit compilationUnit;
    
    public void symbolsolverparsing(String path) throws FileNotFoundException{
        
         
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
//        FileInputStream in = new FileInputStream("src/main/resources/SampleProgramNew.java");
        FileInputStream in = new FileInputStream(path);
        parseResultcompilationUnit = this.parser.parse(in);
        compilationUnit = parseResultcompilationUnit.getResult().get();
        
        
    }
    
    
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
       System.out.println(compilationUnit);
        VariableAttributes obj1= new VariableAttributes();
        for (TypeDeclaration typeDec : compilationUnit.getTypes()) {
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
        compilationUnit.findAll(VariableDeclarator.class).forEach(variable -> {
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


      return obj1;
    }
    
    
    
 public ProgramAttributes attributes(){

        JavaSymbolSolverUtils ssObj = new JavaSymbolSolverUtils();
        ProgramAttributes paObj= new ProgramAttributes();
        
        System.out.println("-------------------------------");
        
        for (ClassOrInterfaceDeclaration cd : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
           
            System.out.println("Class: "+cd.getNameAsString()); 
           
            if(!paObj.attribute_array.contains(cd.getNameAsString()) )paObj.attribute_array.add(cd.getNameAsString()); //class

  System.out.println("Class Variables: ");
            for(FieldDeclaration ff:cd.getFields())
    {
       System.out.println("Qualified Name: " +cd.getNameAsString()+"."+ff.getVariable(0).getName());
        ArrayList<String> dependents = new ArrayList<String>();
        String qname = cd.getNameAsString()+"."+ff.getVariable(0).getName();
        if(paObj.dependence_dict.containsKey(qname)){
            dependents = paObj.dependence_dict.get(qname);
        }

        if(!paObj.attribute_array.contains(cd.getNameAsString()+"."+ff.getVariable(0).getName()) )paObj.attribute_array.add(cd.getNameAsString()+"."+ff.getVariable(0).getName()); // class variable
       if(ff.getVariable(0).getInitializer().isPresent()){
                    
                    Node parentNode = ff.getVariable(0).getInitializer().get();
                    String name ="";
                     if (parentNode instanceof MethodCallExpr) {
                            name = ssObj.getQualifiedName(parentNode);
                            if(!name.equals("")) System.out.println("Qualified Name: " + name);

                     }
                     
                     else if(parentNode instanceof ObjectCreationExpr){
                         name = ff.getVariable(0).getType().toString();
                         System.out.println("Qualified name of class whose obj is created: "+ ff.getVariable(0).getType());
                         String cname = cd.getNameAsString();
                         ArrayList<String> classdependents = new ArrayList<String>();
                         
                         if(paObj.dependence_dict.containsKey(cname)){
                             classdependents = paObj.dependence_dict.get(cname);
                         }
                            if(!name.equals("")&&!classdependents.contains(name)) classdependents.add(name);
                            paObj.dependence_dict.put(cname, classdependents);

                     }
                     
                     else if(parentNode instanceof BinaryExpr){
                         name = "";
                        
                        ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                         for(int i =0;i<subExprList.size();i++){
                            name = ssObj.getQualifiedName(subExprList.get(i));
                            if(!name.equals("")) System.out.println("Qualified Name: " +name);
                             if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name);
                             if(!name.equals("") && !paObj.right.contains(name) ) paObj.right.add(name);
                             if(!dependents.contains(name) && !name.equals("")) dependents.add(name);
                         }
                    }
                     else{
                         name = ssObj.getQualifiedName(parentNode);
                         if(!name.equals("")) System.out.println("Qualified Name: " +name);
                     }

                     
                    if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name);
                    if(!name.equals("") && !paObj.right.contains(name) ) paObj.right.add(name);
                    if(!dependents.contains(name) && !name.equals("")) dependents.add(name);
                    String temp = cd.getNameAsString()+"."+ff.getVariable(0).getName();
                    if(!dependents.isEmpty()) paObj.dependence_dict.put(temp,dependents);
                }
        
    }
       
            
    
    for (MethodDeclaration method : cd.getMethods()) {

        System.out.println("Method: ");
        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+method.getNameAsString()); 
        if(!paObj.attribute_array.contains(cd.getNameAsString()+"."+method.getNameAsString()) )paObj.attribute_array.add(cd.getNameAsString()+"."+method.getNameAsString()); 
        System.out.println("Method Variables");
        
       
        
        method.getBody().ifPresent(blockStatement -> {
            
                    
            for( VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {

                ArrayList<String> dependents = new ArrayList<String>();
                String lhsDecname = cd.getNameAsString()+"."+method.getNameAsString() + "."+variable.getNameAsString();
                if(paObj.dependence_dict.containsKey(lhsDecname)){
                    dependents = paObj.dependence_dict.get(lhsDecname);
                }

                if(variable.getInitializer().isPresent()){
                   

                    if(!lhsDecname .equals("") && !paObj.attribute_array.contains(lhsDecname ) ) paObj.attribute_array.add(lhsDecname );
                    if(!lhsDecname.equals("")) System.out.println("Qualified name: "+ lhsDecname);
                    
                    Node parentNode = variable.getInitializer().get();
                    String name ="";
                    
                     if (parentNode instanceof MethodCallExpr) {
                         String qname = cd.getNameAsString()+"."+method.getNameAsString();
                         ArrayList<String> methoddependents = new ArrayList<String>();
                         name = ssObj.getQualifiedName(parentNode);
                         if(paObj.dependence_dict.containsKey(qname)){
                             methoddependents = paObj.dependence_dict.get(qname);
                         }
                            if(!name.equals("")&&!methoddependents.contains(name)) methoddependents.add(name);
                            paObj.dependence_dict.put(qname, methoddependents);
                            if(!name.equals("")) System.out.println("Qualified Name: " + name);
                        }    

                     else if(parentNode instanceof ObjectCreationExpr){
                         name = variable.getTypeAsString();
                         System.out.println("Qualified name of class whose obj is created: "+ variable.getType());
                         String cname = cd.getNameAsString();
                         ArrayList<String> classdependents = new ArrayList<String>();
                         
                         if(paObj.dependence_dict.containsKey(cname)){
                             classdependents = paObj.dependence_dict.get(cname);
                         }
                            if(!name.equals("")&&!classdependents.contains(name)) classdependents.add(name);
                            paObj.dependence_dict.put(cname, classdependents);

                     }
                     else if(parentNode instanceof BinaryExpr){
                         name = "";
                                
                                ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                                 for(int i =0;i<subExprList.size();i++){
                                     
                                     name = ssObj.getQualifiedName(subExprList.get(i));
                                     if(!name.contains(".") && !name.equals("")) name=cd.getNameAsString()+"."+method.getNameAsString() + "."+name;
                                     if(!name.equals("")) System.out.println("Qualified Name: " +name);
                                     if(!name.equals("") && !paObj.right.contains(name) ) paObj.right.add(name);
                                     if(!name.equals("") && !dependents.contains(name)) dependents.add(name);

                                 }

                            }
                     else{
                         name = ssObj.getQualifiedName(parentNode);
                         if(!name.equals("")) System.out.println("Qualified Name: " +name);
                     }
                     if(!name .equals("") && !paObj.attribute_array.contains(name ) ) paObj.attribute_array.add(name );
                    if(!name.equals("") && !paObj.right.contains(name) ) paObj.right.add(name);
                    if(!dependents.contains(name) && !name.equals("")) dependents.add(name);
                    if(!dependents.isEmpty()) paObj.dependence_dict.put(lhsDecname,dependents);
                }


                for (NameExpr nameExp : blockStatement.findAll(NameExpr.class)) {
                    dependents = new ArrayList<String>();
                    if (nameExp.getNameAsString().equals(variable.getNameAsString())) {
//                        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+method.getNameAsString()+"."+nameExp.getNameAsString());
                        Node parentNode = nameExp.getParentNode().get();
//                        System.out.println("Variable used in Expression: "+parentNode);
//                        System.out.println("Expression type: "+parentNode.getClass());
                        if (parentNode instanceof MethodCallExpr) {
                            String qname = cd.getNameAsString()+"."+method.getNameAsString();
                            ArrayList<String> methoddependents = new ArrayList<String>();
                            String name = ssObj.getQualifiedName(parentNode);
                            if(paObj.dependence_dict.containsKey(qname)){
                                methoddependents = paObj.dependence_dict.get(qname);
                            }
                            if(!methoddependents.contains(name)) methoddependents.add(name);
                            paObj.dependence_dict.put(qname, methoddependents);
                            if(!name.equals("")) System.out.println("Qualified Name: " + name);
                            if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name);
                            if(!name.equals("") && !paObj.right.contains(name) ) paObj.right.add(name);
                        }
                        
                        else if(parentNode instanceof ObjectCreationExpr){
                         System.out.println("Qualified name of class whose obj is created: "+ variable.getType());
                         if(!variable.getType().toString().equals("") && !paObj.attribute_array.contains(variable.getType().toString()) ) paObj.attribute_array.add(variable.getType().toString());
                         String cname = cd.getNameAsString();
                         String name = variable.getType().toString();
                         ArrayList<String> classdependents = new ArrayList<String>();
                         
                         if(paObj.dependence_dict.containsKey(cname)){
                             classdependents = paObj.dependence_dict.get(cname);
                         }
                            if(!name.equals("")&&!classdependents.contains(name)) classdependents.add(name);
                            paObj.dependence_dict.put(cname, classdependents);
                     }
                        else if (parentNode instanceof AssignExpr) {
                            String name = "";
                             
                            Expression left = ((AssignExpr)parentNode).getTarget();  
                            Expression right = ((AssignExpr)parentNode).getValue();
                            System.out.println("LHS: "+left);
                            String leftstr = ssObj.getQualifiedName(left);
                            if(!leftstr.contains(".")&& !leftstr.equals("")) leftstr=cd.getNameAsString()+"."+method.getNameAsString() + "."+leftstr;
                            System.out.println("LHS Qualified Name: " + leftstr);

                            System.out.println("RHS: "+right); 
                            if(right instanceof BinaryExpr){ 
//                               
                                ArrayList<Node> subExprList = new ArrayList<>(right.getChildNodes());
                                for(int i =0;i<subExprList.size();i++){
                                    name = ssObj.getQualifiedName(subExprList.get(i));
                                    if(!name.contains(".") && !name.equals("")) name=cd.getNameAsString()+"."+method.getNameAsString() + "."+name;
                                    if(!name.equals("") && !paObj.right.contains(name) ) paObj.right.add(name);
                                    if(!name.equals("") && !dependents.contains(name)) dependents.add(name);
                                    
                                    if(!name.equals("")) System.out.println("RHS Qualified Name: " +name);
                                }
                            }
                            if(!leftstr.equals("") && !paObj.attribute_array.contains(leftstr) ) paObj.attribute_array.add(leftstr);
                            if(!dependents.isEmpty()) paObj.dependence_dict.put(leftstr,dependents);

                        }
                        else{
                         String name = ssObj.getQualifiedName(parentNode);
                         if(!name.equals("")) System.out.println("Qualified Name: " +name);
                         if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name);
                     }
                                           
                    }
                    
                   
                }
                
               
                
            }
        });
    }
}
        System.out.println("-------------Attribute array------------------");
        
        


        
       for(int i=0;i<paObj.attribute_array.size();i++) System.out.println(paObj.attribute_array.get(i));



     System.out.println("-------------Right array------------------");
     for(int i=0;i<paObj.right.size();i++) System.out.println(paObj.right.get(i));


     System.out.println("-------------Dep dict------------------");

     for (String name: paObj.dependence_dict.keySet()) {
         String key = name.toString();
         ArrayList<String> value = paObj.dependence_dict.get(name);
         System.out.println(key + " " + value);
     }
     
     return paObj;

 }
    
    public static boolean isStringOnlyAlphabet(String str)
    {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}
