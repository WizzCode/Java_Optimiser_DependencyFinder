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
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.lang.ProcessBuilder.Redirect.Type;





import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Some code that uses JavaParser.
 */
public class SymbolTableGenerator {
    //    public static void main(String[] args) {
    public CompilationUnit cu;
    public SourceRoot sourceRoot;
    public JavaParser parser;
    public CombinedTypeSolver combinedTypeSolver;
    public JavaSymbolSolver symbolSolver;
    public CompilationUnit compilationUnit;
    
    public void symbolsolverparsing() throws FileNotFoundException{
        
         
        System.out.println("Entered function");
        combinedTypeSolver = new CombinedTypeSolver();
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        ParseResult<CompilationUnit> parseResultcompilationUnit;
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add((new JavaParserTypeSolver("src/main/java")));
        combinedTypeSolver.add((new JavaParserTypeSolver("src/test/java")));
        symbolSolver = new JavaSymbolSolver(this.combinedTypeSolver);
        parserConfiguration.setSymbolResolver(this.symbolSolver);
        parser = new JavaParser(parserConfiguration);
        FileInputStream in = new FileInputStream("src/main/resources/SampleProgramNew.java");
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
    
    
    
 public void attributes(){

        JavaSymbolSolverUtils ssObj = new JavaSymbolSolverUtils();
        ProgramAttributes paObj= new ProgramAttributes();
        
        System.out.println("-------------------------------");
        
        for (ClassOrInterfaceDeclaration cd : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
            System.out.println("Class "+cd.getNameAsString()); 
            System.out.println("Class Variables");
            if(!paObj.attribute_array.contains(cd.getNameAsString()) )paObj.attribute_array.add(cd.getNameAsString()); //class
            for(FieldDeclaration ff:cd.getFields()) 
    {
       System.out.println("Qualified Name: " +cd.getNameAsString()+"."+ff.getVariable(0).getName());
       if(!paObj.attribute_array.contains(cd.getNameAsString()+"."+ff.getVariable(0).getName()) )paObj.attribute_array.add(cd.getNameAsString()+"."+ff.getVariable(0).getName()); // class variable
       if(ff.getVariable(0).getInitializer().isPresent()){
                    
                    Node parentNode = ff.getVariable(0).getInitializer().get();
                    String name ="";
                     if (parentNode instanceof MethodCallExpr) {
                            name = ssObj.getQualifiedName(parentNode);
                            System.out.println("Method Qualified Name: " + name);
                     }
                     
                     else if(parentNode instanceof ObjectCreationExpr){
                         System.out.println("Qualified name of class whose obj is created: "+ ff.getVariable(0).getType());
                     }
                     
                     else if(parentNode instanceof BinaryExpr){
                         name = "";
                        System.out.println(parentNode.getChildNodes().getClass());
                        ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                         for(int i =0;i<subExprList.size();i++){
                            name = ssObj.getQualifiedName(subExprList.get(i));
                            System.out.println(name);
                        }
                    }
                     else{
                         name = ssObj.getQualifiedName(parentNode);
                         System.out.println(name);
                     }
                     
                    if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name); 
                }
        
    }
       
            
    
    for (MethodDeclaration method : cd.getMethods()) {
        System.out.println("Method");
        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+method.getNameAsString()); 
        if(!paObj.attribute_array.contains(cd.getNameAsString()+"."+method.getNameAsString()) )paObj.attribute_array.add(cd.getNameAsString()+"."+method.getNameAsString()); 
        System.out.println("Method Variables");
        
       
        
        method.getBody().ifPresent(blockStatement -> {
            
                    
            for( VariableDeclarator variable : blockStatement.findAll(VariableDeclarator.class)) {
             
                if(variable.getInitializer().isPresent()){
                   
                    String lhsDecname = cd.getNameAsString()+"."+method.getNameAsString() + "."+variable.getNameAsString();
                    if(!lhsDecname .equals("") && !paObj.attribute_array.contains(lhsDecname ) ) paObj.attribute_array.add(lhsDecname );
                    System.out.println("Qualified name LHS: "+ lhsDecname);
                    
                    Node parentNode = variable.getInitializer().get();
                    String name ="";
                    
                     if (parentNode instanceof MethodCallExpr) {
                            name = ssObj.getQualifiedName(parentNode);
                            System.out.println("Method Qualified Name: " + name);
                        }    

                     else if(parentNode instanceof ObjectCreationExpr){
                         System.out.println("Qualified name of class whose obj is created: "+ variable.getType());
                     }
                     else if(parentNode instanceof BinaryExpr){
                         name = "";
                                System.out.println(parentNode.getChildNodes().getClass());
                                ArrayList<Node> subExprList = new ArrayList<>(parentNode.getChildNodes());
                                 for(int i =0;i<subExprList.size();i++){
                                     System.out.println(subExprList.get(i).getClass());
                                     name = ssObj.getQualifiedName(subExprList.get(i));
                                     if(!name.contains(".") && !name.equals("")) name=cd.getNameAsString()+"."+method.getNameAsString() + "."+name;
                                     System.out.println(name);
                                }
                             
                            }
                     else{
                         name = ssObj.getQualifiedName(parentNode);
                         System.out.println(name);
                     }
                     if(!name .equals("") && !paObj.attribute_array.contains(name ) ) paObj.attribute_array.add(name );
                }
                for (NameExpr nameExp : blockStatement.findAll(NameExpr.class)) {
                    
                    if (nameExp.getNameAsString().equals(variable.getNameAsString())) {                       
//                        System.out.println("Qualified Name: " +cd.getNameAsString()+"."+method.getNameAsString()+"."+nameExp.getNameAsString());
                        Node parentNode = nameExp.getParentNode().get();
                        System.out.println("Variable used in Expression: "+parentNode);
//                        System.out.println("Expression type: "+parentNode.getClass());
                        if (parentNode instanceof MethodCallExpr) {
                            String name = ssObj.getQualifiedName(parentNode);
                            System.out.println("Method Qualified Name: " + name);
                            if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name);
                        }
                        
                        else if(parentNode instanceof ObjectCreationExpr){
                         System.out.println("Qualified name of class whose obj is created: "+ variable.getType());
                         if(!variable.getType().toString().equals("") && !paObj.attribute_array.contains(variable.getType().toString()) ) paObj.attribute_array.add(variable.getType().toString());
                     }
                        else if (parentNode instanceof AssignExpr) {
                            String name = "";
                             
                            Expression left = ((AssignExpr)parentNode).getTarget();  
                            Expression right = ((AssignExpr)parentNode).getValue();
                            System.out.println("LHS: "+left);
                            name = ssObj.getQualifiedName(left);
                            if(!name.contains(".")&& !name.equals("")) name=cd.getNameAsString()+"."+method.getNameAsString() + "."+name;
                            System.out.println("Left Qualified Name: " + name);

                            System.out.println("RHS: "+right); 
                            if(right instanceof BinaryExpr){ 
//                               
                                ArrayList<Node> subExprList = new ArrayList<>(right.getChildNodes());
                                for(int i =0;i<subExprList.size();i++){
                                    name = ssObj.getQualifiedName(subExprList.get(i));
                                     if(!name.contains(".") && !name.equals("")) name=cd.getNameAsString()+"."+method.getNameAsString() + "."+name;
                                    System.out.println(name);
                                }
                            }
                            if(!name.equals("") && !paObj.attribute_array.contains(name) ) paObj.attribute_array.add(name);
                        }
                        else{
                         String name = ssObj.getQualifiedName(parentNode);
                         System.out.println(name);
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
        
         
    }
    
    public static boolean isStringOnlyAlphabet(String str)
    {

        return ((str != null) && (!str.equals(""))
                && (str.matches("^[a-zA-Z]*$")));
    }
}
