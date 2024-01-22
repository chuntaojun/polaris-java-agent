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

package cn.polarismesh.agent.plugin.dubbo2.common;

public interface ClassNames {

    String CONFIG_MANAGER_NAME = "org.apache.dubbo.config.context.ConfigManager";

    String REGISTRY_PROTOCOL_NAME = "org.apache.dubbo.registry.integration.RegistryProtocol";

    String ABSTRACT_EXPORTER_NAME = "org.apache.dubbo.rpc.protocol.AbstractExporter";

    String URL_NAME = "org.apache.dubbo.common.URL";

    String ROUTER_CHAIN_NAME = "org.apache.dubbo.rpc.cluster.RouterChain";

    String CLUSTER_INVOKER_NAME = "org.apache.dubbo.rpc.cluster.support.AbstractClusterInvoker";

    String DIRECTORY_NAME = "org.apache.dubbo.rpc.cluster.directory.AbstractDirectory";

    String EXTENSION_LOADER_NAME = "org.apache.dubbo.common.extension.ExtensionLoader";

    String INVOCATION_NAME = "org.apache.dubbo.rpc.Invocation";

    String REGISTRY_FACTORY_NAME = "org.apache.dubbo.registry.RegistryFactory";

    String RPC_INVOKER_NAME = "org.apache.dubbo.rpc.Invoker";

    String REGISTRY_DIRECTORY_NAME = "org.apache.dubbo.registry.integration.RegistryDirectory";

}
