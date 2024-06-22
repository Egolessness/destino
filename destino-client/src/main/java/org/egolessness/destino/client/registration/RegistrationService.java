package org.egolessness.destino.client.registration;

import org.egolessness.destino.client.registration.message.RegistrationInfo;
import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.ServiceInstance;

import java.util.Collection;

/**
 * registration service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface RegistrationService extends Lucermaire {

    void register(String serviceName, String ip, int port) throws DestinoException;

    void register(String serviceName, String ip, int port, String cluster) throws DestinoException;

    void register(String groupName, String serviceName, String ip, int port) throws DestinoException;

    void register(String groupName, String serviceName, String ip, int port, String cluster)
            throws DestinoException;

    void register(String namespace, String groupName, String serviceName, String ip, int port)
            throws DestinoException;

    void register(String namespace, String groupName, String serviceName, String ip, int port,
                  String cluster) throws DestinoException;

    void register(String serviceName, String ip, int port, Collection<Scheduled<String, String>> jobs)
            throws DestinoException;

    void register(String serviceName, String ip, int port, String cluster,
                  Collection<Scheduled<String, String>> jobs) throws DestinoException;

    void register(String groupName, String serviceName, String ip, int port,
                  Collection<Scheduled<String, String>> jobs) throws DestinoException;

    void register(String groupName, String serviceName, String ip, int port, String cluster,
                  Collection<Scheduled<String, String>> jobs) throws DestinoException;

    void register(String namespace, String groupName, String serviceName, String ip, int port,
                  Collection<Scheduled<String, String>> jobs) throws DestinoException;

    void register(String namespace, String groupName, String serviceName, String ip, int port,
                  String cluster, Collection<Scheduled<String, String>> jobs) throws DestinoException;

    void register(String namespace, String groupName, String serviceName, RegistrationInfo registrationInfo)
            throws DestinoException;

    void deregister(String serviceName, String ip, int port) throws DestinoException;

    void deregister(String serviceName, String ip, int port, String cluster) throws DestinoException;

    void deregister(String groupName, String serviceName, String ip, int port) throws DestinoException;

    void deregister(String groupName, String serviceName, String ip, int port, String cluster)
            throws DestinoException;

    void deregister(String namespace, String groupName, String serviceName, String ip, int port)
            throws DestinoException;

    void deregister(String namespace, String groupName, String serviceName, String ip, int port, String cluster)
            throws DestinoException;

    void deregister(String namespace, String groupName, String serviceName, ServiceInstance instance)
            throws DestinoException;

    void update(String namespace, String groupName, String serviceName, RegistrationInfo registrationInfo)
            throws DestinoException;
}
