package com.zzj.eventobserves;

import com.zzj.event.ConfEvent;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class StartUpService {

    @Inject
    Event<ConfEvent> event;

    //监听程序启动事件
    public void onStart(@Observes StartupEvent ev) {
        event.fire(new ConfEvent());
    }
}
