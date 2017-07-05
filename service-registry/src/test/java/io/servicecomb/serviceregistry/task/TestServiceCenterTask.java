/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.serviceregistry.task;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.eventbus.EventBus;

import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.client.ServiceRegistryClient;
import io.servicecomb.serviceregistry.config.ServiceRegistryConfig;
import io.servicecomb.serviceregistry.task.event.ShutdownEvent;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

public class TestServiceCenterTask {
    @Test
    public void testLifeCycle() {
        EventBus eventBus = new EventBus();

        ServiceCenterTask serviceCenterTask = new ServiceCenterTask(eventBus, ServiceRegistryConfig.INSTANCE);
        serviceCenterTask.init();

        eventBus.post(new ShutdownEvent());
        Assert.assertFalse(Deencapsulation.getField(serviceCenterTask, "running"));
    }

    @Test
    public void testCalcSleepInterval(@Mocked ServiceRegistryClient srClient,
            @Mocked Microservice microservice, @Mocked MicroserviceInstanceHeartbeatTask heartbeatTask,
            @Mocked MicroserviceInstanceRegisterTask registerTask, @Mocked MicroserviceServiceCenterTask centerTask) {
        EventBus eventBus = new EventBus();

        ServiceCenterTask serviceCenterTask = new ServiceCenterTask(eventBus, ServiceRegistryConfig.INSTANCE);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(30, serviceCenterTask.getInterval());

        serviceCenterTask.addMicroserviceTask(centerTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(1, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(2, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(3, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(10, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(20, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(30, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(40, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(50, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(60, serviceCenterTask.getInterval());
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(60, serviceCenterTask.getInterval());

        new Expectations() {
            {
                heartbeatTask.isNeedRegisterInstance();
                result = false;
            }
        };
        eventBus.post(heartbeatTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(60, serviceCenterTask.getInterval());

        new Expectations() {
            {
                heartbeatTask.isNeedRegisterInstance();
                result = true;
            }
        };
        eventBus.post(heartbeatTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(1, serviceCenterTask.getInterval());

        new Expectations() {
            {
                registerTask.isRegistered();
                result = true;
            }
        };
        eventBus.post(registerTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(30, serviceCenterTask.getInterval());

        new Expectations() {
            {
                registerTask.isRegistered();
                result = false;
            }
        };
        eventBus.post(registerTask);
        serviceCenterTask.calcSleepInterval();
        Assert.assertEquals(1, serviceCenterTask.getInterval());
    }
}