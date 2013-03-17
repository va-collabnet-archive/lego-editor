/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.va.legoEdit.drools.manager;
/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import gov.va.legoEdit.drools.definitions.IsKindOfEvaluatorDefinition;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.EvaluatorOption;
import org.drools.conf.ConsequenceExceptionHandlerOption;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.ConsequenceExceptionHandler;
//import org.ihtsdo.tk.drools.IsGbMemberTypeOfEvaluatorDefinition;
//import org.ihtsdo.tk.drools.IsMemberOfEvaluatorDefinition;
//import org.ihtsdo.tk.drools.IsMemberOfWithTypeEvaluatorDefinition;
//import org.ihtsdo.tk.drools.IsMissingDescForDialectEvaluatorDefinition;
//import org.ihtsdo.tk.drools.IsParentMemberOfEvaluatorDefinition;
//import org.ihtsdo.tk.drools.IsSynonymMemberTypeOfEvaluatorDefinition;
//import org.ihtsdo.tk.drools.IsUsMemberTypeOfEvaluatorDefinition;
//import org.ihtsdo.tk.drools.SatisfiesConstraintEvaluatorDefinition;

/**
 *
 * @author kec
 */
public class DroolsExecutionManager {

    private KnowledgeBase kbase;
    private final Set<File> kbFiles;
    private final File drlPkgFile;
    private final KnowledgeBaseConfiguration kBaseConfig;
    public static String drools_dialect_java_compiler;

    public static enum ExtraEvaluators {

        IS_KIND_OF, SAFISFIES_CONSTRAINT,
        IS_MEMBER_OF, IS_PARENT_MEMBER_OF,
        IS_MISSING_DESC_FOR, IS_GB_MEMBER_TYPE_OF,
        IS_US_MEMBER_TYPE_OF, IS_SYNONYM_MEMBER_TYPE_OF,
        IS_MEMBER_OF_WITH_TYPE;
    }
    private boolean failed = false;
    Collection<KnowledgePackage> kpkgs = null;
    private static ConcurrentHashMap<String, Semaphore> packageLocks = new ConcurrentHashMap<String, Semaphore>();

    public DroolsExecutionManager(Set<File> kbFiles, String kbKey) throws IOException {
        this(EnumSet.allOf(ExtraEvaluators.class),
                kbFiles, kbKey);
    }

    public DroolsExecutionManager(EnumSet<ExtraEvaluators> extraEvaluators,
            Set<File> kbFiles, String kbKey) throws IOException {

        boolean buildKb = false;
        File ruleDirectory = new File("rules");
        ruleDirectory.mkdirs();
        this.kbFiles = kbFiles;
        kbKey = sanatizeKey(kbKey);
        drlPkgFile = new File(ruleDirectory, kbKey + ".kpkgs");
        int tries = 0;
        
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        for (File f : this.kbFiles) {
            if (!drlPkgFile.exists() || drlPkgFile.lastModified() < f.lastModified()) {
                lock.writeLock().lock();
                if (!drlPkgFile.exists() || drlPkgFile.lastModified() < f.lastModified()) {
                    buildKb = true;
                    break;
                } else {
                    lock.writeLock().unlock();
                }
            }
        }

        if (buildKb) {
            loadKnowledgePackages(kbFiles, extraEvaluators, drlPkgFile);
            lock.writeLock().unlock();
        } else {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(drlPkgFile));
            try {
                kpkgs = (Collection<KnowledgePackage>) in.readObject();
            } catch(Exception e){
                if(tries < 3){
                    lock.writeLock().lock();
                    loadKnowledgePackages(kbFiles, extraEvaluators, drlPkgFile);
                    lock.writeLock().unlock(); 
                    tries++;
                }else{
                    if(e instanceof ClassNotFoundException){
                    throw new IOException(e);
                    }else if(e instanceof EOFException){
                        System.out.println("@@@@ End of file exception: " + drlPkgFile.toString());
                    }
                }
            }finally {
                in.close();
            }
        }

        kBaseConfig =
                KnowledgeBaseFactory.newKnowledgeBaseConfiguration();

        Class<? extends ConsequenceExceptionHandler> exHandlerClass =
                DroolsExceptionHandler.class;

        ConsequenceExceptionHandlerOption cehOption =
                ConsequenceExceptionHandlerOption.get(exHandlerClass);

        kBaseConfig.setOption(cehOption);
        if (drools_dialect_java_compiler != null) {
            kBaseConfig.setProperty("drools.dialect.java.compiler", drools_dialect_java_compiler);
        }

        kbase = KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
        if (kpkgs != null) {
            kbase.addKnowledgePackages(kpkgs);
        }
    }

    private static String sanatizeKey(String kbKey) {
        if (kbKey.contains(":")) {
            kbKey = kbKey.replace(':', '.');
        }
        if (kbKey.contains("/")) {
            kbKey = kbKey.replace('/', '.');
        }
        if (kbKey.contains("*")) {
            kbKey = kbKey.replace('*', '.');
        }
        if (kbKey.contains("\\")) {
            kbKey = kbKey.replace('\\', '.');
        }
        return kbKey;
    }

    public final void loadKnowledgePackages(Set<File> kbFiles, EnumSet<ExtraEvaluators> extraEvaluators, File drlPkgFile) throws RuntimeException, IOException {
        System.out.println("Writing: " + drlPkgFile.getCanonicalPath());
        HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
        for (File f : kbFiles) {
            resources.put(ResourceFactory.newFileResource(f), ResourceType.DRL);
        }
        Properties props = new Properties();
        if (drools_dialect_java_compiler != null) {
            props.setProperty("drools.dialect.java.compiler", drools_dialect_java_compiler);
        }
        KnowledgeBuilderConfiguration builderConfig =
                KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(props, null);

        if (extraEvaluators.contains(ExtraEvaluators.IS_KIND_OF)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsKindOfEvaluatorDefinition.IS_KIND_OF.getOperatorString(),
                    new IsKindOfEvaluatorDefinition()));
        }
        /*
        if (extraEvaluators.contains(ExtraEvaluators.SAFISFIES_CONSTRAINT)) {
            builderConfig.setOption(EvaluatorOption.get(
                    SatisfiesConstraintEvaluatorDefinition.SATISFIES_CONSTRAINT.getOperatorString(),
                    new SatisfiesConstraintEvaluatorDefinition()));
        }
        if (extraEvaluators.contains(ExtraEvaluators.IS_MEMBER_OF)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsMemberOfEvaluatorDefinition.IS_MEMBER_OF.getOperatorString(),
                    new IsMemberOfEvaluatorDefinition()));
        }
        if (extraEvaluators.contains(ExtraEvaluators.IS_MEMBER_OF_WITH_TYPE)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsMemberOfWithTypeEvaluatorDefinition.IS_MEMBER_OF_WITH_TYPE.getOperatorString(),
                    new IsMemberOfWithTypeEvaluatorDefinition()));
        }
        if (extraEvaluators.contains(ExtraEvaluators.IS_PARENT_MEMBER_OF)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsParentMemberOfEvaluatorDefinition.IS_PARENT_MEMBER_OF.getOperatorString(),
                    new IsParentMemberOfEvaluatorDefinition()));
        }
        if (extraEvaluators.contains(ExtraEvaluators.IS_MISSING_DESC_FOR)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsMissingDescForDialectEvaluatorDefinition.IS_MISSING_DESC_FOR.getOperatorString(),
                    new IsMissingDescForDialectEvaluatorDefinition()));
        }

        if (extraEvaluators.contains(ExtraEvaluators.IS_GB_MEMBER_TYPE_OF)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsGbMemberTypeOfEvaluatorDefinition.IS_GB_MEMBER_TYPE_OF.getOperatorString(),
                    new IsGbMemberTypeOfEvaluatorDefinition()));
        }

        if (extraEvaluators.contains(ExtraEvaluators.IS_US_MEMBER_TYPE_OF)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsUsMemberTypeOfEvaluatorDefinition.IS_US_MEMBER_TYPE_OF.getOperatorString(),
                    new IsUsMemberTypeOfEvaluatorDefinition()));
        }

        if (extraEvaluators.contains(ExtraEvaluators.IS_SYNONYM_MEMBER_TYPE_OF)) {
            builderConfig.setOption(EvaluatorOption.get(
                    IsSynonymMemberTypeOfEvaluatorDefinition.IS_SYNONYM_MEMBER_TYPE_OF.getOperatorString(),
                    new IsSynonymMemberTypeOfEvaluatorDefinition()));
        }
        */

        //        if (kBaseConfig != null) {
        //            kBaseConfig.setProperty("drools.dialect.java.compiler", "JANINO");
        //        }
        kbase = KnowledgeBaseFactory.newKnowledgeBase(kBaseConfig);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase, builderConfig);
        for (Resource resource : resources.keySet()) {
            synchronized (this) {
                kbuilder.add(resource, resources.get(resource)); 
            }
        }
        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        kpkgs = kbuilder.getKnowledgePackages();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(drlPkgFile));
        out.writeObject(kpkgs);
        out.close();
    }
    private static ConcurrentHashMap<String, DroolsExecutionManager> managerMap =
            new ConcurrentHashMap<String, DroolsExecutionManager>();

    public static void setup(String kbKey, Set<File> kbFiles) throws IOException {
        kbKey = sanatizeKey(kbKey);
        if (!managerMap.containsKey(kbKey)) {
            managerMap.put(kbKey, new DroolsExecutionManager(kbFiles, kbKey));
        }

    }

    public static boolean fireAllRules(String kbKey, Set<File> kbFiles,
            Map<String, Object> globals, Collection<Object> facts, boolean useLogger)
            throws IOException, DroolsException {
        kbKey = sanatizeKey(kbKey);
        KnowledgeRuntimeLogger logger = null;
        DroolsExecutionManager mgr = managerMap.get(kbKey);
        try {
            if (mgr == null) {
                mgr = new DroolsExecutionManager(kbFiles, kbKey);
                managerMap.put(kbKey, mgr);
            }
            if (mgr.failed) {
                return false;
            }

            StatefulKnowledgeSession ksession = mgr.kbase.newStatefulKnowledgeSession();
            try {
                if (useLogger) {
                    logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
                }
                for (Map.Entry<String, Object> e : globals.entrySet()) {
                    ksession.setGlobal(e.getKey(), e.getValue());
                }
                for (Object fact : facts) {
                    ksession.insert(fact);
                }
                ksession.fireAllRules();
            } finally {
                ksession.dispose();
            }
        } catch (Throwable ex) {
            if (mgr != null) {
                mgr.failed = true;
            }
            if (ex instanceof IOException) {
                IOException ioe = (IOException) ex;
                throw ioe;
            }
            if (ex instanceof DroolsException) {
                DroolsException de = (DroolsException) ex;
                throw de;
            }
            throw new IOException(ex);
        } finally {
            if (logger != null) {
                logger.close();
            }
        }
        return true;
    }

    public static void resetFailures() {
        for (DroolsExecutionManager mgr : managerMap.values()) {
            mgr.failed = false;
        }
    }
}
