package com.vladmihalcea.book.high_performance_java_persistence.jdbc.caching;

import com.vladmihalcea.book.high_performance_java_persistence.jdbc.batch.providers.BatchEntityProvider;
import com.vladmihalcea.hibernate.masterclass.laboratory.util.DataSourceProviderIntegrationTest;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.Test;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * StatementCacheTest - Test Statement cache
 *
 * @author Vlad Mihalcea
 */
public class StatementCacheWithPoolableAPITest extends DataSourceProviderIntegrationTest {

    public static final String INSERT_POST = "insert into Post (title, version, id) values (?, ?, ?)";

    public static final String INSERT_POST_COMMENT = "insert into PostComment (post_id, review, version, id) values (?, ?, ?, ?)";

    private BatchEntityProvider entityProvider = new BatchEntityProvider();

    public StatementCacheWithPoolableAPITest(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    @Parameterized.Parameters
    public static Collection<DataSourceProvider[]> rdbmsDataSourceProvider() {
        List<DataSourceProvider[]> providers = new ArrayList<>();
        providers.add(new DataSourceProvider[]{
            new OracleDataSourceProvider() {
                @Override
                public DataSource dataSource() {
                    OracleDataSource dataSource = (OracleDataSource) super.dataSource();
                    try {
                        Properties connectionProperties = dataSource.getConnectionProperties();
                        if(connectionProperties == null) {
                            connectionProperties = new Properties();
                        }
                        connectionProperties.put("oracle.jdbc.implicitStatementCacheSize", "5");
                        dataSource.setConnectionProperties(connectionProperties);
                    } catch (SQLException e) {
                        fail(e.getMessage());
                    }
                    return dataSource;
                }
            }
        });
        /*providers.add(new DataSourceProvider[]{new JTDSDataSourceProvider() {
            @Override
            public DataSource dataSource() {
                JtdsDataSource dataSource = (JtdsDataSource) super.dataSource();
                dataSource.setMaxStatements(5);
                return dataSource;
            }
        }});*/
        providers.add(new DataSourceProvider[]{new PostgreSQLDataSourceProvider()});
        //providers.add(new DataSourceProvider[]{new MySQLDataSourceProvider()});
        return providers;
    }

    @Override
    protected Class<?>[] entities() {
        return entityProvider.entities();
    }

    @Override
    public void init() {
        super.init();
        doInConnection(connection -> {
            try (
                    PreparedStatement postStatement = connection.prepareStatement(INSERT_POST);
                    PreparedStatement postCommentStatement = connection.prepareStatement(INSERT_POST_COMMENT);
            ) {
                int postCount = getPostCount();
                int postCommentCount = getPostCommentCount();

                int index;

                for (int i = 0; i < postCount; i++) {
                    index = 0;
                    postStatement.setString(++index, String.format("Post no. %1$d", i));
                    postStatement.setInt(++index, 0);
                    postStatement.setLong(++index, i);
                    postStatement.executeUpdate();
                }

                for (int i = 0; i < postCount; i++) {
                    for (int j = 0; j < postCommentCount; j++) {
                        index = 0;
                        postCommentStatement.setLong(++index, i);
                        postCommentStatement.setString(++index, String.format("Post comment %1$d", j));
                        postCommentStatement.setInt(++index, (int) (Math.random() * 1000));
                        postCommentStatement.setLong(++index, (postCommentCount * i) + j);
                        postCommentStatement.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                fail(e.getMessage());
            }
        });
    }

    @Test
    public void testStatementCaching() {
        selectWhenCaching(true);
    }

    @Test
    public void testStatementWithoutCaching() {
        selectWhenCaching(false);
    }

    private void selectWhenCaching(boolean caching) {
        long startNanos = System.nanoTime();
        doInConnection(connection -> {
            for (int i = 0; i < getStatementCount(); i++) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "select p.title, pc.review " +
                                "from post p left join postcomment pc on p.id = pc.post_id " +
                                "where EXISTS ( " +
                                "   select 1 from postcomment where version = ? and id > p.id " +
                                ")"
                )) {
                    statement.setPoolable(caching);
                    statement.setInt(1, i);
                    statement.execute();
                } catch (SQLException e) {
                    fail(e.getMessage());
                }
            }
        });
        LOGGER.info("{} when caching Statements is {} took {} millis",
                getClass().getSimpleName(),
                caching,
                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos));
    }

    protected int getPostCount() {
        return 1000;
    }

    protected int getPostCommentCount() {
        return 5;
    }

    protected int getStatementCount() {
        return 10 * 1000;
    }

    @Override
    protected boolean proxyDataSource() {
        return false;
    }
}
