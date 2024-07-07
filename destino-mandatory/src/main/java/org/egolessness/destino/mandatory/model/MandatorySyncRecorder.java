/*
 */

/*
 */

package org.egolessness.destino.mandatory.model;

import org.egolessness.destino.mandatory.message.VbKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * synchronizer status recorder.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatorySyncRecorder {

    private final AtomicInteger failCounter;

    private long localFirstTime;

    private VersionKey localAppendFlag;

    private long undertakeFirstTime;

    private VersionKey undertakeAppendFlag;

    private List<VbKey> removingKeys;

    public MandatorySyncRecorder() {
        this.failCounter = new AtomicInteger();
        this.localAppendFlag = new VersionKey(0, null);
        this.undertakeAppendFlag = new VersionKey(0, null);
        this.removingKeys = new ArrayList<>();
    }

    public MandatorySyncRecorder(MandatorySyncRecorder source) {
        this.failCounter = source.failCounter;
        this.localFirstTime = source.localFirstTime;
        this.localAppendFlag = source.localAppendFlag;
        this.undertakeFirstTime = source.undertakeFirstTime;
        this.undertakeAppendFlag = source.undertakeAppendFlag;
        this.removingKeys = source.removingKeys;
    }

    public AtomicInteger getFailCounter() {
        return failCounter;
    }

    public long getLocalFirstTime() {
        return localFirstTime;
    }

    public void setLocalFirstTime(long localFirstTime) {
        this.localFirstTime = localFirstTime;
    }

    public VersionKey getLocalAppendFlag() {
        return localAppendFlag;
    }

    public void setLocalAppendFlag(VersionKey localAppendFlag) {
        this.localAppendFlag = localAppendFlag;
    }

    public long getUndertakeFirstTime() {
        return undertakeFirstTime;
    }

    public void setUndertakeFirstTime(long undertakeFirstTime) {
        this.undertakeFirstTime = undertakeFirstTime;
    }

    public VersionKey getUndertakeAppendFlag() {
        return undertakeAppendFlag;
    }

    public void setUndertakeAppendFlag(VersionKey undertakeAppendFlag) {
        this.undertakeAppendFlag = undertakeAppendFlag;
    }

    public List<VbKey> getRemovingKeys() {
        return removingKeys;
    }

    public void setRemovingKeys(List<VbKey> removingKeys) {
        this.removingKeys = removingKeys;
    }
}