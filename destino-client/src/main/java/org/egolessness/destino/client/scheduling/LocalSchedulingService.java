package org.egolessness.destino.client.scheduling;

import org.egolessness.destino.client.scheduling.functional.Scheduled;
import org.egolessness.destino.common.fixedness.Lucermaire;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * scheduled service.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public interface LocalSchedulingService extends Lucermaire {

    @Nullable
    Scheduled<String, String> parseJobForInterface(@Nonnull Object instance);

    @Nullable
    Scheduled<String, String> parseJob(@Nonnull Object instance, @Nonnull Method method, @Nullable String jobName);

    List<Scheduled<String, String>> parseJobs(Object... objs);

    Collection<Scheduled<String, String>> loadJobs();

    void addJobs(Object... objs);

    void addJobs(Collection<Scheduled<String, String>> jobs);

    void removeJobs(String... jobNames);

    void removeJobs(Collection<Scheduled<String, String>> jobs);

    void cancelExecution(long schedulerId);

    void cancelExecution(long schedulerId, long executionTime);

}
