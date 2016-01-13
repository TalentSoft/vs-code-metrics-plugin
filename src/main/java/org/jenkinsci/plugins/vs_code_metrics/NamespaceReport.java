package org.jenkinsci.plugins.vs_code_metrics;

import hudson.model.Run;

import org.jenkinsci.plugins.vs_code_metrics.bean.Namespace;
import org.jenkinsci.plugins.vs_code_metrics.bean.Type;

public final class NamespaceReport extends AbstractReport {

   /**
    *
    * @param run
    * @param result
    * @param tokens
    */
   public NamespaceReport(Run<?, ?> run, Namespace result, String... tokens) {
       super(run, result.getName(), result);
       setBuildTokens(getName(), tokens);
   }

   @Override
   public Object getReport(String token) {
       if ((getChildren() != null) && getChildren().containsKey(token))
           return new TypeReport(getBuild(), (Type)getChildren().get(token), getBuildTokens());
       else
           return this;
   }

}
