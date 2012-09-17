package org.jboss.tools.maven.accessrules.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.embedder.ArtifactKey;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.IRuntimeType;
import org.eclipse.wst.server.core.IRuntimeWorkingCopy;

public class AccessRulesProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

	private IRuntime mockRuntime = new IRuntime() {
		
		public IStatus validate(IProgressMonitor arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public Object loadAdapter(Class arg0, IProgressMonitor arg1) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public boolean isWorkingCopy() {
			// TODO Auto-generated method stub
			return false;
		}
		
		public boolean isStub() {
			// TODO Auto-generated method stub
			return false;
		}
		
		public boolean isReadOnly() {
			// TODO Auto-generated method stub
			return false;
		}
		
		public IRuntimeType getRuntimeType() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public IPath getLocation() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public Object getAdapter(Class arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public void delete() throws CoreException {
			// TODO Auto-generated method stub
			
		}
		
		public IRuntimeWorkingCopy createWorkingCopy() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	@Override
	public void configure(ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		// ignore
	}

	public void configureClasspath(IMavenProjectFacade facade,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {

		IProject project = facade.getProject();
		
		Set<IRuntime> runtimes = getTargetedJBossRuntimes(project);
		
		if (runtimes == null || runtimes.isEmpty()) {
			return;
		}
		
		List<IClasspathEntryDescriptor> entries = classpath.getEntryDescriptors();

		for (IRuntime runtime : runtimes) {

			Map<String, List<IAccessRule>> runtimeAccessRules = getAccessRules(runtime);
			if (runtimeAccessRules == null || runtimeAccessRules.isEmpty()) {
				continue;
			}

			for (IClasspathEntryDescriptor e : entries) {
				//Don't use e.getGroupId()+":"+e.getGroupId(); //values are null
				ArtifactKey aKey  = e.getArtifactKey();
				if (aKey == null) {
					continue;
				}
				String key = aKey.getGroupId()+":"+aKey.getArtifactId();
				List<IAccessRule> artifactAccessRules = runtimeAccessRules.get(key);
				if (artifactAccessRules != null && !artifactAccessRules.isEmpty()) {
					mergeAccessRules(e, artifactAccessRules);
				}
			}
		}
	}

	private void mergeAccessRules(IClasspathEntryDescriptor e,	List<IAccessRule> artifactAccessRules) {
		List<IAccessRule> existingRules = e.getAccessRules();
		for (IAccessRule ar : artifactAccessRules) {
			if (existingRules == null || !existingRules.contains(ar)) {
				e.addAccessRule(ar);
			}
		}
	}

	private Map<String, List<IAccessRule>> getAccessRules(IRuntime runtime) {
		Map<String, List<IAccessRule>> rulesMap = new HashMap<String, List<IAccessRule>>();
		//TODO get access rules from somewhere
		String key = "org.jboss.modules:jboss-modules";
		List<IAccessRule> rules = new ArrayList<IAccessRule>();
		rules.add(JavaCore.newAccessRule(new Path("org/jboss/modules/**"), IAccessRule.K_NON_ACCESSIBLE));
		rulesMap.put(key, rules);
		return rulesMap;
	}
	private Set<IRuntime> getTargetedJBossRuntimes(IProject project) {
		return Collections.singleton(mockRuntime);
	}

	public void configureRawClasspath(ProjectConfigurationRequest request,
			IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
	}

}
