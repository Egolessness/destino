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

package org.egolessness.destino.core.storage.sql;

import org.egolessness.destino.common.enumeration.Mark;
import org.egolessness.destino.common.utils.PredicateUtils;
import org.egolessness.destino.core.Loggers;
import org.egolessness.destino.core.enumration.Errors;
import org.egolessness.destino.core.exception.SnapshotException;
import org.egolessness.destino.core.fixedness.Processor;
import org.egolessness.destino.core.fixedness.SnapshotOperation;
import org.egolessness.destino.core.message.ConsistencyDomain;
import org.egolessness.destino.core.storage.SnapshotProcessorAware;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.*;

import javax.sql.PooledConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class H2Context {

    private final static String username = "destino";

    private final static String password = "destino";

    private final static String schemaFilePath = "META-INF/h2_schema.sql";

    private final static String backupFileName = "backup.sql";

    private final SnapshotProcessorAware snapshotProcessorAware = buildSnapshotProcessorAware();

    private final SnapshotOperation snapshotOperation = buildSnapshotOperation();

    private final ReentrantLock lock = new ReentrantLock();

    private final ConsistencyDomain domain;

    private final JdbcDataSource dataSource;

    private final String dbDir;

    private final PooledConnection connectionPool;

    private Connection connection;

    private List<Processor> beforeLoadProcessors;

    private List<Processor> afterLoadProcessors;

    private List<Processor> beforeSaveProcessors;

    private List<Processor> afterSaveProcessors;

    public H2Context(ConsistencyDomain domain, String dbDir) throws SQLException, IOException {
        this.domain = domain;
        this.dbDir = dbDir;
        this.dataSource = createDataSource();
        this.connectionPool = buildConnectionPool();
        this.initData();
    }

    public Connection getConnection() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            return this.connection;
        }
        lock.lock();
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.connection = this.connectionPool.getConnection();
            }
        } catch (Exception e) {
            return null;
        } finally {
            lock.unlock();
        }
        return this.connection;
    }

    private String getDatabaseName() {
        return domain.name().toLowerCase();
    }

    private JdbcDataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUser(username);
        dataSource.setPassword(password);
        long cacheSize = Runtime.getRuntime().maxMemory() / 20;
        dataSource.setUrl("jdbc:h2:" + Paths.get(dbDir, getDatabaseName()) + ";CACHE_SIZE=" + cacheSize);
        dataSource.setLoginTimeout(5);
        return dataSource;
    }

    private PooledConnection buildConnectionPool() throws SQLException {
        try {
            return this.dataSource.getPooledConnection();
        } catch (SQLException e) {
            Loggers.STORAGE.warn("H2database init failed, is recovering.", e);
        }
        Recover.execute(dbDir, getDatabaseName());
        return this.dataSource.getPooledConnection();
    }

    private void initData() throws IOException, SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();
        for (String sql : this.loadSchema()) {
            try {
                statement.execute(sql);
            } catch (SQLException e) {
                Loggers.STORAGE.warn("H2database execute sql failed.", e);
            }
        }
        connection.commit();
    }

    private List<String> loadSchema() throws IOException {
        List<String> schema = new ArrayList<>();
        StringBuilder sqlContent = new StringBuilder();

        Enumeration<URL> resources = ClassLoader.getSystemResources(schemaFilePath);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            sqlContent.append(parse(url));
        }

        String[] split = Mark.SEMICOLON.split(sqlContent.toString());
        for (String seg : split) {
            if (PredicateUtils.isNotEmpty(seg) && !seg.startsWith("--")) {
                schema.add(seg);
            }
        }
        return schema;
    }

    private String parse(URL url) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = url.openStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            return reader.lines().collect(Collectors.joining());
        } catch (Exception e) {
            try {
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException ignored) {
            }
        }
        return PredicateUtils.emptyString();
    }

    private SnapshotProcessorAware buildSnapshotProcessorAware() {
        return new SnapshotProcessorAware() {
            @Override
            public boolean isLoaded() {
                return true;
            }
            @Override
            public synchronized void addBeforeLoadProcessor(Processor processor) {
                if (beforeLoadProcessors == null) beforeLoadProcessors = new ArrayList<>(2);
                beforeLoadProcessors.add(processor);
            }
            @Override
            public synchronized void addAfterLoadProcessor(Processor processor) {
                if (afterLoadProcessors == null) afterLoadProcessors = new ArrayList<>(2);
                afterLoadProcessors.add(processor);
            }
            @Override
            public synchronized void addBeforeSaveProcessor(Processor processor) {
                if (beforeSaveProcessors == null) beforeSaveProcessors = new ArrayList<>(2);
                beforeSaveProcessors.add(processor);
            }

            @Override
            public synchronized void addAfterSaveProcessor(Processor processor) {
                if (afterSaveProcessors == null) afterSaveProcessors = new ArrayList<>(2);
                afterSaveProcessors.add(processor);
            }
        };
    }

    private SnapshotOperation buildSnapshotOperation() {

        return new SnapshotOperation() {

            @Override
            public String snapshotSource() {
                return dbDir;
            }

            @Override
            public void snapshotSave(String path) throws SnapshotException {
                try {
                    executeProcessors(beforeSaveProcessors);
                    String fileName = Paths.get(path, backupFileName).toString();
                    Script.process(getConnection(), fileName, PredicateUtils.emptyString(), PredicateUtils.emptyString());
                } catch (Exception e) {
                    throw new SnapshotException(Errors.SNAPSHOT_SAVE_FAIL, e.getMessage());
                } finally {
                    executeProcessors(afterSaveProcessors);
                }
            }

            @Override
            public void snapshotLoad(String path) throws SnapshotException {
                String fileName = Paths.get(path, backupFileName).toString();
                try (FileReader fileReader = new FileReader(fileName)) {
                    executeProcessors(beforeLoadProcessors);
                    Connection connection = getConnection();
                    Statement statement = connection.createStatement();
                    statement.execute("DROP ALL OBJECTS;");
                    RunScript.execute(getConnection(), fileReader);
                    connection.commit();
                } catch (Exception e) {
                    throw new SnapshotException(Errors.SNAPSHOT_LOAD_FAIL, e.getMessage());
                } finally {
                    executeProcessors(afterLoadProcessors);
                }
            }

        };
    }

    private void executeProcessors(Collection<Processor> processors) {
        if (processors == null) {
            return;
        }
        for (Processor processor : processors) {
            processor.process();
        }
    }

    public String getDbDir() {
        return dbDir;
    }

    public SnapshotProcessorAware getSnapshotProcessorAware() {
        return snapshotProcessorAware;
    }

    public SnapshotOperation getSnapshotOperation() {
        return snapshotOperation;
    }

}
