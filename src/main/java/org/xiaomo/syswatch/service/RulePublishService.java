package org.xiaomo.syswatch.service;

public interface RulePublishService {

    void publishAll();
    void publishByResourceType(String resourceType);

}