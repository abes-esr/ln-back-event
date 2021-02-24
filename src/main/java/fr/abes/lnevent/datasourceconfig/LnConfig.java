/*
package fr.abes.lnevent.datasourceconfig;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@EnableJpaRepositories(
        basePackages = "fr.abes.lnevent.repository",
        entityManagerFactoryRef = "lnEntityManagerFactory",
        transactionManagerRef = "lnTransactionManager"
)
public class LnConfig
{
    @Autowired
    private Environment env;

    @Primary
    @Bean(name= "lnDb")
    @ConfigurationProperties(prefix="ln.datasource")
    public DataSourceProperties sujetsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name= "lnDataSource")
    public DataSource lnDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(env.getProperty("ln.datasource.driver-class-name"));
        config.setJdbcUrl(env.getProperty("ln.datasource.url"));
        config.setUsername(env.getProperty("ln.datasource.username"));
        config.setPassword(env.getProperty("ln.datasource.password"));
        //Frequently used
        config.setAutoCommit(false);
        config.setConnectionTimeout(60000);
        config.setIdleTimeout(65000);
        config.setMaxLifetime(85000);
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(2);
        config.getMetricsTrackerFactory();
        config.getMetricRegistry();
        config.getHealthCheckProperties();
        config.setPoolName("poolLnEvent");
        //Infrequently used
        config.setInitializationFailTimeout(0);
        //config.setIsolateInternalQueries(true);
        config.setAllowPoolSuspension(true);
        config.setLeakDetectionThreshold(80000);
        config.setValidationTimeout(4500);
        config.setConnectionTestQuery("SELECT 1 FROM DUAL");

        config.addDataSourceProperty("implicitCachingEnabled", "true"); //spec oracle
        config.addDataSourceProperty("maxStatements", "250"); //spec oracle

        return new HikariDataSource(config);

    }


    @Primary
    @Bean(name= "lnTransactionManager")
    public PlatformTransactionManager lnTransactionManager()
    {
        EntityManagerFactory factory = lnEntityManagerFactory().getObject();
        return new JpaTransactionManager(factory);
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean lnEntityManagerFactory()
    {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(lnDataSource());
        factory.setPackagesToScan(new String[]{"fr.abes.lnevent.entities"});
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.show-sql", env.getProperty("spring.jpa.show-sql"));
        jpaProperties.put("javax.persistence.schema-generation.create-source", "metadata");
        jpaProperties.put("javax.persistence.schema-generation.scripts.action", "drop-and-create");
        jpaProperties.put("javax.persistence.schema-generation.scripts.create-target", "./create.sql");
        jpaProperties.put("javax.persistence.schema-generation.scripts.drop-target", "./drop.sql");

        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Primary
    @Bean
    public DataSourceInitializer lnDataSourceInitializer()
    {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(lnDataSource());
        dataSourceInitializer.setEnabled(env.getProperty("ln.datasource.initialize", Boolean.class, false));
        return dataSourceInitializer;
    }



}*/
