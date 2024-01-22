/*
 * Tencent is pleased to support the open source community by making Polaris available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package cn.polarismesh.agent.plugin.dubbo2;

import cn.polarismesh.agent.core.common.exception.InstrumentException;
import cn.polarismesh.agent.core.extension.AgentPlugin;
import cn.polarismesh.agent.core.extension.PluginContext;
import cn.polarismesh.agent.core.extension.instrument.InstrumentClass;
import cn.polarismesh.agent.core.extension.instrument.InstrumentMethod;
import cn.polarismesh.agent.core.extension.instrument.Instrumentor;
import cn.polarismesh.agent.core.extension.transform.TransformCallback;
import cn.polarismesh.agent.core.extension.transform.TransformOperations;
import cn.polarismesh.agent.plugin.dubbo2.common.ClassNames;
import cn.polarismesh.agent.plugin.dubbo2.common.Constant;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.config.DubboConfigInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.metadatareport.DubboMetadataCenterInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.registry.DubboRegistryDirectoryInterceptor;
import cn.polarismesh.agent.plugin.dubbo2.interceptor.registry.DubboRegistryFactoryInterceptor;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

public class MainPlugin implements AgentPlugin {
    @Override
    public void init(PluginContext context) {
        System.setProperty(Constant.AGENT_CONF_PATH, context.getAgentDirPath());
        TransformOperations operations = context.getTransformOperations();
        addPolarisTransformers(operations);
    }

    private void addPolarisTransformers(TransformOperations operations) {
        operations.transform(ClassNames.CONFIG_MANAGER_NAME, ConfigManagerTransform.class);
        operations.transform(ClassNames.REGISTRY_PROTOCOL_NAME, RegistryProtocolTransform.class);
        operations.transform(ClassNames.REGISTRY_DIRECTORY_NAME, RegistryDirectoryTransform.class);
        operations.transform(ClassNames.ABSTRACT_EXPORTER_NAME, ExporterTransform.class);
        operations.transform(ClassNames.URL_NAME, UrlConstructorTransform.class);
        operations.transform(ClassNames.CLUSTER_INVOKER_NAME, ClusterInvokerTransform.class);
        operations.transform(ClassNames.DIRECTORY_NAME, DirectoryTransform.class);
        operations.transform(ClassNames.EXTENSION_LOADER_NAME, ExtensionLoaderTransform.class);
    }

    public static class ConfigManagerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod1 = target.getDeclaredMethod("getConfigCenters");
            if (invokeMethod1 != null) {
                invokeMethod1.addInterceptor(DubboConfigInterceptor.class);
            }
            InstrumentMethod invokeMethod2 = target.getDeclaredMethod("getMetadataConfigs");
            if (invokeMethod2 != null) {
                invokeMethod2.addInterceptor(DubboMetadataCenterInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class RegistryProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("setRegistryFactory", ClassNames.REGISTRY_FACTORY_NAME);
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboRegistryFactoryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class RegistryDirectoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("toInvokers", List.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboRegistryDirectoryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ExporterTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod constructor = target.getConstructor(ClassNames.RPC_INVOKER_NAME);
            if (constructor != null) {
                constructor.addInterceptor(DubboExporterInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class UrlConstructorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer)
                throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, protectionDomain, classFileBuffer);

            String[] paramTypes = new String[]{String.class.getCanonicalName(), String.class.getCanonicalName(),
                    String.class.getCanonicalName(), String.class.getCanonicalName(),
                    int.class.getCanonicalName(), String.class.getCanonicalName(),
                    Map.class.getCanonicalName()};

            InstrumentMethod constructor = target.getConstructor(paramTypes);
            if (constructor != null) {
                constructor.addInterceptor(DubboUrlInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ClusterInvokerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", ClassNames.INVOCATION_NAME);
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboInvokeInterceptor.class);
            }
            return target.toBytecode();
        }

    }

    public static class DirectoryTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("setRouterChain", ClassNames.ROUTER_CHAIN_NAME);
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboAbstractDirectoryInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class ExtensionLoaderTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className,
                                    Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
                throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target
                    .getDeclaredMethod("createExtension", String.class.getCanonicalName(), boolean.class.getCanonicalName());
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(DubboExtensionLoaderInterceptor.class);
            }
            return target.toBytecode();
        }
    }
}
