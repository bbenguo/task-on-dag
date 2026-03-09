package com.mude.tod;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlowTest {

    @Test
    public void testFlow() {
        Flow flow = makeFlow();
        Assert.isTrue(flow.validate(), "任务流初始化失败");

        TestContext context = new TestContext();
        FlowResult execute = flow.execute(context, Flow.TaskDirection.ROOT_FIRST);
        Assert.isTrue(execute.isSuccess(), "任务执行失败");
        Assert.isTrue("R:a".equals(context.get("a")), "任务执行失败");
        Assert.isTrue("R:d".equals(context.get("d")), "任务执行失败");

        context = new TestContext();
        execute = flow.execute(context, Flow.TaskDirection.LEAF_FIRST);
        Assert.isTrue(execute.isSuccess(), "任务执行失败");
        Assert.isTrue("R:a".equals(context.get("a")), "任务执行失败");
        Assert.isTrue("R:d".equals(context.get("d")), "任务执行失败");
    }

    @Test
    public void testAsyncExecuteFlow() throws InterruptedException {
        Flow flow = makeFlow();
        Assert.isTrue(flow.validate(), "任务流初始化失败");

        ExecutorService executorService = Executors.newFixedThreadPool(6);
        Flow.Callback callback = (context, result) -> {
            Assert.isTrue(result.isSuccess(), "任务执行失败");
            Assert.isTrue("R:a".equals(context.get("a")), "任务执行失败");
            Assert.isTrue("R:d".equals(context.get("d")), "任务执行失败");
        };
        flow.asyncExecute(new TestContext(), executorService, Flow.TaskDirection.ROOT_FIRST, callback);
        Thread.sleep(2000L);
    }

    /**
     * 构造简单任务树
     *       a
     *     /  \
     *    b    c
     *          \
     *           d
     */
    private Flow makeFlow() {
        Flow flow = new Flow("simple-flow");
        TestTask a = new TestTask("a");
        TestTask b = new TestTask("b");
        TestTask c = new TestTask("c");
        TestTask d = new TestTask("d");

        flow.addTask(a, b, c);
        flow.addTask(c, d);
        return flow;
    }

    @Test
    public void testComplexFlow() {
        Flow flow = makeComplexFlow();
        Assert.isTrue(flow.validate(), "任务流初始化失败");

        TestContext context = new TestContext();
        FlowResult execute = flow.execute(context, Flow.TaskDirection.ROOT_FIRST);
        Assert.isTrue(execute.isSuccess(), "任务执行失败");
        Assert.isTrue("R:a".equals(context.get("a")), "任务执行失败");
        Assert.isTrue("R:g".equals(context.get("g")), "任务执行失败");
        Assert.isTrue("R:h".equals(context.get("h")), "任务执行失败");

        context = new TestContext();
        execute = flow.execute(context, Flow.TaskDirection.LEAF_FIRST);
        Assert.isTrue(execute.isSuccess(), "任务执行失败");
        Assert.isTrue("R:a".equals(context.get("a")), "任务执行失败");
        Assert.isTrue("R:g".equals(context.get("g")), "任务执行失败");
        Assert.isTrue("R:h".equals(context.get("h")), "任务执行失败");
    }

    @Test
    public void testAsyncComplexExecuteFlow() throws InterruptedException {
        Flow flow = makeComplexFlow();
        Assert.isTrue(flow.validate(), "任务流初始化失败");

        ExecutorService executorService = Executors.newFixedThreadPool(6);
        Flow.Callback callback = (context, result) -> {
            Assert.isTrue(result.isSuccess(), "任务执行失败");
            Assert.isTrue("R:a".equals(context.get("a")), "任务执行失败");
            Assert.isTrue("R:g".equals(context.get("g")), "任务执行失败");
            Assert.isTrue("R:h".equals(context.get("h")), "任务执行失败");
        };
        flow.asyncExecute(new TestContext(), executorService, Flow.TaskDirection.LEAF_FIRST, callback);
        Thread.sleep(2000L);
    }



    /**
     * 构造复杂任务树
     *       a      h
     *     /   \
     *    b     c
     *  / | \    \
     * e  f  g    d
     *
     */
    private Flow makeComplexFlow() {
        Flow flow = new Flow("complex-flow");
        TestTask a = new TestTask("a");
        TestTask b = new TestTask("b");
        TestTask c = new TestTask("c");
        TestTask d = new TestTask("d");
        TestTask e = new TestTask("e");
        TestTask f = new TestTask("f");
        TestTask g = new TestTask("g");
        TestTask h = new TestTask("h");

        flow.addTask(a, b, c);
        flow.addTask(b, e, f, g);
        flow.addTask(c, d);
        flow.addTask(h);
        return flow;
    }
}
