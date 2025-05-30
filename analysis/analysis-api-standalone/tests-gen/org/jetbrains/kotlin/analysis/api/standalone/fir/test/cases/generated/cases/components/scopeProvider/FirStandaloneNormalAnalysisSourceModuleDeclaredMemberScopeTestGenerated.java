/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.standalone.fir.test.cases.generated.cases.components.scopeProvider;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analysis.api.standalone.fir.test.configurators.AnalysisApiFirStandaloneModeTestConfiguratorFactory;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfiguratorFactoryData;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.TestModuleKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.FrontendKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisSessionMode;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiMode;
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.scopeProvider.AbstractDeclaredMemberScopeTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope")
@TestDataPath("$PROJECT_ROOT")
public class FirStandaloneNormalAnalysisSourceModuleDeclaredMemberScopeTestGenerated extends AbstractDeclaredMemberScopeTest {
  @NotNull
  @Override
  public AnalysisApiTestConfigurator getConfigurator() {
    return AnalysisApiFirStandaloneModeTestConfiguratorFactory.INSTANCE.createConfigurator(
      new AnalysisApiTestConfiguratorFactoryData(
        FrontendKind.Fir,
        TestModuleKind.Source,
        AnalysisSessionMode.Normal,
        AnalysisApiMode.Standalone
      )
    );
  }

  @Test
  public void testAllFilesPresentInDeclaredMemberScope() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope"), Pattern.compile("^(.+)\\.kt$"), null, true);
  }

  @Test
  @TestMetadata("annotatedProperties.kt")
  public void testAnnotatedProperties() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/annotatedProperties.kt");
  }

  @Test
  @TestMetadata("annotatedPropertiesCompiled.kt")
  public void testAnnotatedPropertiesCompiled() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/annotatedPropertiesCompiled.kt");
  }

  @Test
  @TestMetadata("annotatedPropertiesCompiledK1.kt")
  public void testAnnotatedPropertiesCompiledK1() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/annotatedPropertiesCompiledK1.kt");
  }

  @Test
  @TestMetadata("annotatedReturnType.kt")
  public void testAnnotatedReturnType() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/annotatedReturnType.kt");
  }

  @Test
  @TestMetadata("annotatedReturnTypeCompiled.kt")
  public void testAnnotatedReturnTypeCompiled() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/annotatedReturnTypeCompiled.kt");
  }

  @Test
  @TestMetadata("class.kt")
  public void testClass() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/class.kt");
  }

  @Test
  @TestMetadata("delegateInterface.kt")
  public void testDelegateInterface() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/delegateInterface.kt");
  }

  @Test
  @TestMetadata("delegateInterfaceLibrary.kt")
  public void testDelegateInterfaceLibrary() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/delegateInterfaceLibrary.kt");
  }

  @Test
  @TestMetadata("enumClass.kt")
  public void testEnumClass() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumClass.kt");
  }

  @Test
  @TestMetadata("enumClassWithAbstractMembers.kt")
  public void testEnumClassWithAbstractMembers() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumClassWithAbstractMembers.kt");
  }

  @Test
  @TestMetadata("enumClassWithFinalMembers.kt")
  public void testEnumClassWithFinalMembers() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumClassWithFinalMembers.kt");
  }

  @Test
  @TestMetadata("enumEntryInitializer.kt")
  public void testEnumEntryInitializer() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumEntryInitializer.kt");
  }

  @Test
  @TestMetadata("enumEntryInitializerWithEmptyBody.kt")
  public void testEnumEntryInitializerWithEmptyBody() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumEntryInitializerWithEmptyBody.kt");
  }

  @Test
  @TestMetadata("enumEntryInitializerWithFinalEnumMember.kt")
  public void testEnumEntryInitializerWithFinalEnumMember() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumEntryInitializerWithFinalEnumMember.kt");
  }

  @Test
  @TestMetadata("enumEntryInitializerWithOverriddenMember.kt")
  public void testEnumEntryInitializerWithOverriddenMember() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/enumEntryInitializerWithOverriddenMember.kt");
  }

  @Test
  @TestMetadata("innerClass.kt")
  public void testInnerClass() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/innerClass.kt");
  }

  @Test
  @TestMetadata("javaClass.kt")
  public void testJavaClass() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/javaClass.kt");
  }

  @Test
  @TestMetadata("javaDeclaredEnhancementScope.kt")
  public void testJavaDeclaredEnhancementScope() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/javaDeclaredEnhancementScope.kt");
  }

  @Test
  @TestMetadata("javaDeclaredInheritList.kt")
  public void testJavaDeclaredInheritList() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/javaDeclaredInheritList.kt");
  }

  @Test
  @TestMetadata("javaInnerClassConstructor.kt")
  public void testJavaInnerClassConstructor() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/javaInnerClassConstructor.kt");
  }

  @Test
  @TestMetadata("properties.kt")
  public void testProperties() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/properties.kt");
  }

  @Test
  @TestMetadata("propertiesCompiled.kt")
  public void testPropertiesCompiled() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/propertiesCompiled.kt");
  }

  @Test
  @TestMetadata("substitutionOverrideOfKotlinInJava.kt")
  public void testSubstitutionOverrideOfKotlinInJava() {
    runTest("analysis/analysis-api/testData/components/scopeProvider/declaredMemberScope/substitutionOverrideOfKotlinInJava.kt");
  }
}
