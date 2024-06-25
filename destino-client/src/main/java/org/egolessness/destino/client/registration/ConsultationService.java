package org.egolessness.destino.client.registration;

import org.egolessness.destino.client.registration.collector.Service;
import org.egolessness.destino.client.registration.selector.InstanceSelector;
import org.egolessness.destino.common.exception.DestinoException;
import org.egolessness.destino.common.fixedness.Cancellable;
import org.egolessness.destino.common.fixedness.Listener;
import org.egolessness.destino.common.fixedness.Lucermaire;
import org.egolessness.destino.common.model.Page;
import org.egolessness.destino.common.model.Pageable;

import java.util.List;

/**
 * consultation service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface ConsultationService extends Lucermaire {

    InstanceSelector selectInstances(String serviceName, String[] clusters) throws DestinoException;

    InstanceSelector selectInstances(String namespace, String serviceName, String[] clusters) throws DestinoException;

    InstanceSelector selectInstances(String namespace, String groupName, String serviceName) throws DestinoException;

    InstanceSelector selectInstances(String namespace, String groupName, String serviceName, String[] clusters)
            throws DestinoException;

    InstanceSelector subscribeService(String serviceName, String[] clusters) throws DestinoException;

    InstanceSelector subscribeService(String groupName, String serviceName, String[] clusters) throws DestinoException;

    InstanceSelector subscribeService(String namespace, String groupName, String serviceName) throws DestinoException;

    InstanceSelector subscribeService(String namespace, String groupName, String serviceName, String[] clusters)
            throws DestinoException;

    Cancellable subscribeService(Listener<Service> listener, String namespace, String groupName, String serviceName,
                                 String[] clusters) throws DestinoException;

    void unsubscribeService(String namespace, String serviceName) throws DestinoException;

    void unsubscribeService(String namespace, String serviceName, String[] clusters) throws DestinoException;

    void unsubscribeService(String namespace, String groupName, String serviceName) throws DestinoException;

    void unsubscribeService(String namespace, String groupName, String serviceName, String[] clusters)
            throws DestinoException;

    Page<String> queryServices(Pageable pageable) throws DestinoException;

    Page<String> queryServices(String groupName, Pageable pageable) throws DestinoException;

    Page<String> queryServices(String namespace, String groupName, Pageable pageable) throws DestinoException;

    List<Service> getSubscribeServices();
}
