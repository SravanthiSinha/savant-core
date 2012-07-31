/*
 * Copyright (c) 2001-2011, Inversoft, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.savantbuild.config.groovy;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.google.inject.Inject;

/**
 * This class is a Groovy AST transformation that Savant uses to convert local Script variables to fields inside the
 * Script class.
 *
 * @author Brian Pontarelli
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class SavantASTTransformation implements ASTTransformation {
  /**
   * This assumes that the file being compiled is a script file that only has one class, the Script.
   *
   * @param nodes  Not used.
   * @param source The source that is modified.
   */
  @Override
  public void visit(ASTNode[] nodes, SourceUnit source) {
    ModuleNode ast = source.getAST();
    ClassNode mainClass = ast.getClasses().get(0);

    List<MethodNode> methods = mainClass.getMethods();
    for (MethodNode method : methods) {
      if (!method.getName().equals("run") && !mainClass.getSuperClass().getName().equals("")) {
        continue;
      }

      BlockStatement block = (BlockStatement) method.getCode();
      List<Statement> statements = block.getStatements();
      for (Iterator<Statement> i = statements.iterator(); i.hasNext(); ) {
        Statement statement = i.next();
        ExpressionStatement exprStmt = (ExpressionStatement) statement;
        Expression expr = exprStmt.getExpression();
        if (expr instanceof DeclarationExpression) {
          DeclarationExpression decl = (DeclarationExpression) expr;
          VariableExpression var = decl.getVariableExpression();

          // If the local variable doesn't have the inject annotation, skip it
          if (var.getAnnotations(new ClassNode("Inject", 1, null)).isEmpty()) {
            continue;
          }

          // Grab the local variable and add an identical field. Then remove the local variable
          ClassNode type = var.getType();
          String name = var.getName();

          FieldNode field = new FieldNode(name, 1, type, mainClass, null);
          field.addAnnotation(new AnnotationNode(new ClassNode(Inject.class)));
          mainClass.addField(field);

          i.remove();
        }
      }
    }
  }
}
