/*
 * Copyright (c) 2023 by Kang Wang. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.egolessness.destino.common.model.request;

import org.egolessness.destino.common.annotation.Body;
import org.egolessness.destino.common.annotation.Http;
import org.egolessness.destino.common.enumeration.HttpMethod;
import org.egolessness.destino.common.model.ExecutionFeedback;

import java.io.Serializable;
import java.util.List;

/**
 * request of feedback scheduled executed result
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
@Body
@Http(value = "/api/execution/feedback", method = HttpMethod.POST)
public class ExecutionFeedbackRequest implements Serializable {

    private static final long serialVersionUID = 4907512977630544232L;

    private List<ExecutionFeedback> feedbackList;

    public ExecutionFeedbackRequest() {
    }

    public ExecutionFeedbackRequest(List<ExecutionFeedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

    public List<ExecutionFeedback> getFeedbackList() {
        return feedbackList;
    }

    public void setFeedbackList(List<ExecutionFeedback> feedbackList) {
        this.feedbackList = feedbackList;
    }

}
