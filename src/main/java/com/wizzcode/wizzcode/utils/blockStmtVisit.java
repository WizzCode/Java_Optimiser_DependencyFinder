package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class blockStmtVisit extends VoidVisitorAdapter<Void> {
    @Override
    public void visit(IfStmt n, Void arg) {
        Statement then = n.getThenStmt();
        
        System.out.println(n +"\n");
        if (then instanceof BlockStmt) {
            BlockStmt blockThenStmt = then.asBlockStmt();
            super.visit(blockThenStmt, arg); // <-- visit statements in thenStmt
           
          if(blockThenStmt.getStatements().size()==0)
          {
              System.out.println("empty if encountered! Avoid empty if \n");
          }
           // System.out.println(blockThenStmt.getStatements());
        }
//        super.visit(n, arg); // <-- visit nested ifStmt
    }
    
     public void visit(ForStmt n, Void arg) {
        Statement then = n.getBody();
        
       
        
        if (then instanceof BlockStmt) {
            BlockStmt blockThenStmt = then.asBlockStmt();
            super.visit(blockThenStmt, arg); // <-- visit statements in thenStmt
            
           // System.out.println(blockThenStmt.getStatements());
        }

    }
}
