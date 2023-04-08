package com.wizzcode.wizzcode.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.wizzcode.wizzcode.CodeDependencyFinder.SymbolTableGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodOverrideDetector extends VoidVisitorAdapter<List<MethodDeclaration>> {

public String path;

CompilationUnit cu;
public MethodOverrideDetector (CompilationUnit compilationUnit) {
        cu = compilationUnit;
    }

     @Override
    public void visit(MethodDeclaration md,List<MethodDeclaration> collector) {
        
        // Check if the method is abstract or final
        if (md.isAbstract() || md.isFinal()) {
            System.out.println("Method " + md.getName() + " cannot be overridden");
        } else {
            // Check if the method is overridden in any subclass
            boolean overridden = false;
            ResolvedClassDeclaration classDecl = md.resolve().declaringType().asClass();
            List<ResolvedReferenceType> ancestors = new ArrayList<>();
            ancestors.addAll(classDecl.getAllInterfaces());
            ResolvedReferenceType superClass = classDecl.getSuperClass().orElse(null);
            if (superClass != null) {
                ancestors.add(superClass);
            }
            for (ResolvedReferenceType ancestor : ancestors) {
                String ancestorName = ancestor.getQualifiedName();
                
                // Skip non-reference types
                if (ancestor.isReferenceType()) {
                    
                    ResolvedReferenceType ancestorRefType = ancestor.asReferenceType();
                   
                     // Find the superclass of method
                   
                    System.out.println("SuperClass: "+ancestorName);

                   Optional<ClassOrInterfaceDeclaration> supClassOpt = cu.findFirst(ClassOrInterfaceDeclaration.class,
                node -> node.getNameAsString().equals(ancestorName));
             

                    //check all methods of superclass 
                    if (supClassOpt.isPresent()) {
                        ClassOrInterfaceDeclaration supClass = supClassOpt.get();
                        overridden = supClass.getMethods().stream()
                                .anyMatch(m -> m.getNameAsString().equals(md.getNameAsString())
                                        && m.getParameters().equals(md.getParameters()));
                        if (overridden) {
                            System.out.println("Method " + md.getName() + " overrides method of superclass " + supClass.getName());
                            collector.add(md);
                            break;
                        }
                    }
                }
                 
            }
            if (!overridden) {
                System.out.println("Method " + md.getName() + " is not overridden");
            }
        }
        System.out.println("------------------------");
        super.visit(md,collector);
    }
}
