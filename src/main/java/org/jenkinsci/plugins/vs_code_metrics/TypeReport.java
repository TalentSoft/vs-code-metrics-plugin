package org.jenkinsci.plugins.vs_code_metrics;

import hudson.model.Run;

import org.jenkinsci.plugins.vs_code_metrics.bean.Type;

public final class TypeReport extends AbstractReport {

   /**
    *
    * @param run
    * @param result
    * @param tokens
    */
   public TypeReport(Run<?, ?> run, Type result, String... tokens) {
       super(run, result.getName(), result);
       setBuildTokens(getName(), tokens);
       setDepthOfInheritance(false);
       setChildUrlLink(false);
   }

   @Override
   public Object getReport(String token) {
       return this;
   }

}
