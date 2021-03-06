/*
 * Copyright (c) 2020 Adam Gaj
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.adgadev.jplusone.test.domain.commerce.session;

import com.adgadev.jplusone.core.registry.OperationNodeView;
import com.adgadev.jplusone.core.registry.RootNodeView;
import com.adgadev.jplusone.core.registry.SessionNodeView;
import com.adgadev.jplusone.test.matchers.JPlusOneMatchers;
import com.adgadev.jplusone.test.matchers.frame.FrameExtractSpecification;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@TestMethodOrder(OrderAnnotation.class)
class OuterSessionServiceTest {

    private static final Long MANUFACTURER_ID = 1L;

    @Autowired
    private OuterSessionService service;

    @Autowired
    private RootNodeView rootNode;

    private RootNodeAssertionWrapper rootNodeWrapper;

    @BeforeEach
    public void setup() {
        rootNodeWrapper = new RootNodeAssertionWrapper(rootNode);
    }

    @Test
    void shouldCheckIfEntityManagerOpenInTx() {
        // when
        service.checkIfEntityManagerOpenInTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(1));
        SessionNodeView sessionNode = rootNodeWrapper.getFirstNewSession();
        assertThat(sessionNode.getOperations(), hasSize(equalTo(0)));
    }

    @Test
    void shouldCheckIfEntityManagerOpenNoTx() {
        // when
        service.checkIfEntityManagerOpenNoTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(0));
    }

    @Test
    void shouldCreateQueryInTx() {
        // when
        service.createQueryInTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(1));
        SessionNodeView sessionNode = rootNodeWrapper.getFirstNewSession();
        assertThat(sessionNode.getOperations(), hasSize(equalTo(0)));
    }

    @Test
    void shouldCreateQueryNoTx() {
        // when
        service.createQueryNoTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(0));
    }

    @Test
    void shouldFetchDataOuterTx() {
        // when
        service.fetchDataOuterTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(1));

        SessionNodeView sessionNode = rootNodeWrapper.getFirstNewSession();
        assertThat(sessionNode.getOperations(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(sessionNode.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterTx")
        )));

        OperationNodeView operationNodeView1 = sessionNode.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));
    }

    @Test
    void shouldFetchDataInnerNewTx() {
        // when
        service.fetchDataInnerNewTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(2));

        SessionNodeView sessionNode1 = rootNodeWrapper.getNewSession(0);
        SessionNodeView sessionNode2 = rootNodeWrapper.getNewSession(1);
        assertThat(sessionNode1.getOperations(), hasSize(equalTo(1)));
        assertThat(sessionNode2.getOperations(), hasSize(equalTo(0)));

        MatcherAssert.assertThat(sessionNode1.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataInnerNewTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        assertThat(sessionNode2.getSessionFrameStack(), nullValue()); // TODO: sessions without operations / frame stack should not be added - investigate

        OperationNodeView operationNodeView1 = sessionNode1.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));
    }

    @Test
    void shouldFetchDataOuterTxInnerTx() {
        // when
        service.fetchDataOuterTxInnerTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(1));

        SessionNodeView sessionNode = rootNodeWrapper.getFirstNewSession();
        assertThat(sessionNode.getOperations(), hasSize(equalTo(2)));
        MatcherAssert.assertThat(sessionNode.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterTxInnerTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerTx")
        )));

        OperationNodeView operationNodeView1 = sessionNode.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));

        OperationNodeView operationNodeView2 = sessionNode.getOperations().get(1);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInExistingTransaction"),
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInExistingTransaction")
        )));
    }

    @Test
    void shouldFetchDataOuterTxInnerNewTx() {
        // when
        service.fetchDataOuterTxInnerNewTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(2));

        SessionNodeView sessionNode1 = rootNodeWrapper.getNewSession(0);
        SessionNodeView sessionNode2 = rootNodeWrapper.getNewSession(1); // session ordered by session close date
        assertThat(sessionNode1.getOperations(), hasSize(equalTo(1)));
        assertThat(sessionNode2.getOperations(), hasSize(equalTo(1)));

        MatcherAssert.assertThat(sessionNode1.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterTxInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        MatcherAssert.assertThat(sessionNode2.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterTxInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTx")
        )));

        OperationNodeView operationNodeView1 = sessionNode1.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        OperationNodeView operationNodeView2 = sessionNode2.getOperations().get(0);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));
    }

    @Test
    void shouldFetchDataOuterNoTxInnerTx() {
        // when
        service.fetchDataOuterNoTxInnerTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(2));

        SessionNodeView sessionNode1 = rootNodeWrapper.getNewSession(0);
        SessionNodeView sessionNode2 = rootNodeWrapper.getNewSession(1);
        assertThat(sessionNode1.getOperations(), hasSize(equalTo(1)));
        assertThat(sessionNode2.getOperations(), hasSize(equalTo(1)));

        MatcherAssert.assertThat(sessionNode1.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterNoTxInnerTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));

        MatcherAssert.assertThat(sessionNode2.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterNoTxInnerTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInExistingTransaction")
        )));

        OperationNodeView operationNodeView1 = sessionNode1.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.allFrameCallMatcher(FrameExtractSpecification.notAppMethodCallFrame()));

        OperationNodeView operationNodeView2 = sessionNode2.getOperations().get(0);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInExistingTransaction")
        )));
    }

    @Test
    void shouldFetchDataOuterNoTxInnerNewTx() {
        // when
        service.fetchDataOuterNoTxInnerNewTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(2));

        SessionNodeView sessionNode1 = rootNodeWrapper.getNewSession(0);
        SessionNodeView sessionNode2 = rootNodeWrapper.getNewSession(1);
        assertThat(sessionNode1.getOperations(), hasSize(equalTo(1)));
        assertThat(sessionNode2.getOperations(), hasSize(equalTo(1)));

        MatcherAssert.assertThat(sessionNode1.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterNoTxInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerNewTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerNewTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));

        MatcherAssert.assertThat(sessionNode2.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterNoTxInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerNewTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterNoTxInnerNewTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        OperationNodeView operationNodeView1 = sessionNode1.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.allFrameCallMatcher(FrameExtractSpecification.notAppMethodCallFrame()));

        OperationNodeView operationNodeView2 = sessionNode2.getOperations().get(0);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));
    }

    @Test
    void shouldFetchDataInnerTxOuterTx() {
        // when
        service.fetchDataInnerTxOuterTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(1));

        SessionNodeView sessionNode = rootNodeWrapper.getFirstNewSession();
        assertThat(sessionNode.getOperations(), hasSize(equalTo(2)));
        MatcherAssert.assertThat(sessionNode.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataInnerTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataInnerTxOuterTx")
        )));

        OperationNodeView operationNodeView2 = sessionNode.getOperations().get(0);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataInnerTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInExistingTransaction"),
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInExistingTransaction")
        )));

        OperationNodeView operationNodeView1 = sessionNode.getOperations().get(1);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataInnerTxOuterTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));
    }

    @Test
    void shouldFetchDataInnerNewTxOuterTx() {
        // when
        service.fetchDataInnerNewTxOuterTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(2));

        SessionNodeView sessionNode1 = rootNodeWrapper.getNewSession(0);
        SessionNodeView sessionNode2 = rootNodeWrapper.getNewSession(1); // session ordered by session close date
        assertThat(sessionNode1.getOperations(), hasSize(equalTo(1)));
        assertThat(sessionNode2.getOperations(), hasSize(equalTo(1)));

        MatcherAssert.assertThat(sessionNode1.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataInnerNewTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataInnerNewTxOuterTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataInnerNewTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        MatcherAssert.assertThat(sessionNode2.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataInnerNewTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataInnerNewTxOuterTx")
        )));

        OperationNodeView operationNodeView1 = sessionNode1.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        OperationNodeView operationNodeView2 = sessionNode2.getOperations().get(0);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataInnerNewTxOuterTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));
    }

    @Test
    void shouldFetchDataOuterTxInnerNewTxOuterTx() {
        // when
        service.fetchDataOuterTxInnerNewTxOuterTx();

        // then
        assertThat(rootNodeWrapper.getNewSessionsAmount(), equalTo(2));

        SessionNodeView sessionNode1 = rootNodeWrapper.getNewSession(0);
        SessionNodeView sessionNode2 = rootNodeWrapper.getNewSession(1); // session ordered by session close date
        assertThat(sessionNode1.getOperations(), hasSize(equalTo(1)));
        assertThat(sessionNode2.getOperations(), hasSize(equalTo(2)));

        MatcherAssert.assertThat(sessionNode1.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterTxInnerNewTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTxOuterTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        MatcherAssert.assertThat(sessionNode2.getSessionFrameStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionServiceTest.class, "shouldFetchDataOuterTxInnerNewTxOuterTx"),
                FrameExtractSpecification.anyProxyMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTxOuterTx")
        )));

        OperationNodeView operationNodeView1 = sessionNode1.getOperations().get(0);
        assertThat(operationNodeView1.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView1.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(InnerSessionService.class, "fetchDataInNewTransaction")
        )));

        OperationNodeView operationNodeView2 = sessionNode2.getOperations().get(0);
        assertThat(operationNodeView2.getStatements(), hasSize(equalTo(1)));
        MatcherAssert.assertThat(operationNodeView2.getCallFramesStack(), JPlusOneMatchers.frameCallSequenceMatcher(List.of(
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchDataOuterTxInnerNewTxOuterTx"),
                FrameExtractSpecification.anyAppMethodCallFrame(OuterSessionService.class, "fetchData")
        )));
    }

    private static class RootNodeAssertionWrapper {

        private final RootNodeView rootNode;

        private final int initialSessionAmount;

        RootNodeAssertionWrapper(RootNodeView rootNode) {
            this.rootNode = rootNode;
            this.initialSessionAmount = rootNode.getSessions().size();
        }

        int getNewSessionsAmount() {
            return rootNode.getSessions().size() - initialSessionAmount;
        }

        SessionNodeView getFirstNewSession() {
            return getNewSession(0);
        }

        SessionNodeView getNewSession(int sessionOrdinal) {
            return rootNode.getSessions().stream()
                    .skip(initialSessionAmount + sessionOrdinal)
                    .findFirst().get();
        }
    }
}