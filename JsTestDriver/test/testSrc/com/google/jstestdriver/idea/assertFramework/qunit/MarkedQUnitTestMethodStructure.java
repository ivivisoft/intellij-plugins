package com.google.jstestdriver.idea.assertFramework.qunit;

import com.google.jstestdriver.idea.JsTestDriverTestUtils;
import com.google.jstestdriver.idea.assertFramework.Annotation;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MarkedQUnitTestMethodStructure {

  private static final String KEY_ID = "id";
  private static final String KEY_NAME = "name";

  private final String myId;
  private final int myTestCaseId;
  private final String myName;
  private final int myStartPosition;
  private PsiElement myPsiElement;

  public MarkedQUnitTestMethodStructure(Annotation startAnnotation) {
    String id = getIdAndValidate(startAnnotation);
    String[] idComponents = id.split(Pattern.quote("_"));
    if (idComponents.length != 2) {
      throw new RuntimeException("Malformed test id: " + id);
    }
    myId = id;
    myTestCaseId = Integer.parseInt(idComponents[0]);
    myName = startAnnotation.getValue(KEY_NAME);
    myStartPosition = startAnnotation.getTextRange().getEndOffset();
  }

  public String getId() {
    return myId;
  }

  public int getTestCaseId() {
    return myTestCaseId;
  }

  public String getName() {
    return myName;
  }

  public void endEncountered(TextRange endAnnotationTextRange, JSFile jsFile) {
    if (myPsiElement != null) {
      throw new RuntimeException("End annotation is already encountered");
    }
    int endPosition = endAnnotationTextRange.getStartOffset();
    myPsiElement = JsTestDriverTestUtils.findExactPsiElement(jsFile, TextRange.create(myStartPosition, endPosition));
  }

  public void validateBuiltStructure() {
    JSCallExpression jsCallExpression = CastUtils.tryCast(myPsiElement, JSCallExpression.class);
    if (jsCallExpression == null) {
      Assert.fail("Unable to find underlying " + JSCallExpression.class + " for " + this);
    }
  }

  public JSCallExpression getCallExpression() {
    return (JSCallExpression) myPsiElement;
  }

  @NotNull
  public static String getIdAndValidate(Annotation annotation) {
    String id = annotation.getValue(KEY_ID);
    String name = annotation.getValue(KEY_NAME);
    if (id == null || name == null) {
      throw new RuntimeException(KEY_ID + " and " + KEY_NAME + " should be specified, " + annotation);
    }
    return id;
  }

}
