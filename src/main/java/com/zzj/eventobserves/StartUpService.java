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

    //TODO
/*

    private static final Logger LOGGER = Logger.getLogger("StartUpService");

    void onLoadRule(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");

        //加载默认的限制策略

        FlowRule rule2 = new FlowRule("homeList")
                .setCount(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .as(FlowRule.class);

        FlowRule rule3 = new FlowRule("rightSide")
                .setCount(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .as(FlowRule.class);

        FlowRuleManager.loadRules(Arrays.asList(rule2, rule3));

    }*/


}
