/*
 */

/*
 */

package org.egolessness.destino.mandatory.model;

import org.egolessness.destino.mandatory.message.VsData;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * synchronizer data.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class MandatorySyncData {

    private final MandatorySyncRecorder recorder;

    private final List<VsData> firstDataList = new ArrayList<>();

    private final TreeMap<VersionKey, VsData> appendDataMap = new TreeMap<>();

    private final List<VsData> undertakeDataList = new ArrayList<>();

    public MandatorySyncData() {
        this.recorder = new MandatorySyncRecorder();
    }

    public MandatorySyncData(MandatorySyncRecorder recorder) {
        this.recorder = recorder;
    }

    public MandatorySyncRecorder getRecorder() {
        return recorder;
    }

    public List<VsData> getFirstDataList() {
        return firstDataList;
    }

    public TreeMap<VersionKey, VsData> getAppendDataMap() {
        return appendDataMap;
    }

    public List<VsData> getUndertakeDataList() {
        return undertakeDataList;
    }
}