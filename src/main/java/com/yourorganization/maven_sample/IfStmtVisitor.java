package com.yourorganization.maven_sample;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.github.javaparser.StaticJavaParser.parse;

public class IfStmtVisitor extends VoidVisitorAdapter<List<String>>{
    @Override
 public void visit(IfStmt ifStmt, List<String> collector) {
         
//         super.visit(ifStmt, collector);

        //Statement then = ifStmt.getThenStmt();
blockStmtVisit b_obj = new blockStmtVisit();
b_obj.visit(ifStmt,null);
         collector.add(ifStmt.getThenStmt().toString());
        
        // System.out.println("If Statement body: "+ifStmt.getThenStmt());
         }

}
