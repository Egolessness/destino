/*
 */

/*
 */

package org.egolessness.destino.mandatory.model;

import javax.annotation.Nonnull;

/**
 * synchronizer data.
 *
 * @author zsmjwk@outlook.com (wangkang)
 */
public class VersionKey implements Comparable<VersionKey> {

    private final long version;

    private final String key;

    public VersionKey(long version, String key) {
        this.version = version;
        this.key = key;
    }

    public long getVersion() {
        return version;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int compareTo(@Nonnull VersionKey o) {
        int compared = Long.compare(version, o.version);
        if (compared != 0) {
            return compared;
        }
        if (null == key) {
            return -1;
        }
        if (null == o.key) {
            return 1;
        }
        return key.compareTo(o.key);
    }
}