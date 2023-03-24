package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class IfStmtVisitor extends VoidVisitorAdapter<List<Expression>>{
    int flag;
    
    
    @Override
 public void visit(IfStmt ifStmt, List<Expression> collector) {
         

        Optimiser obj = new Optimiser();
        flag = obj.flag;
       //System.out.println("reached here");
        if(flag==2){
             Statement then = ifStmt.getThenStmt();
            
             if (then instanceof BlockStmt) {
            BlockStmt blockThenStmt = then.asBlockStmt();
             
          if(blockThenStmt.getStatements().size()==0)
          {
              System.out.println("\nEmpty if encountered! Avoid empty if");
              System.out.println(ifStmt.getBegin());
              System.out.println(ifStmt);
                  
              
          }
                  
        }
        }
        
        collector.add(ifStmt.getCondition());
        //System.out.println(collector.size());
        super.visit(ifStmt, collector);
//blockStmtVisit b_obj = new blockStmtVisit();
//b_obj.visit(ifStmt,null);


        
        
         }

}
