package com.trecapps.pictures.repos;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories (
		entityManagerFactoryRef = "pictureEntityManagerFactory",
        transactionManagerRef = "pictureTransactionManager",
        basePackages = {"com.trecapps.pictures.repos"})
public class PictureDataSourceConfiguration
{
	@Bean(name = "pictureDataSourceProperties")
	@ConfigurationProperties("trec.datasource-picture")
	public DataSourceProperties primaryDataSourceProperties()
	{
        return new DataSourceProperties();
    }

	@Bean(name = "pictureDataSource")
	@ConfigurationProperties("trec.datasource-picture.configuration")
	public DataSource primaryDataSource(@Qualifier("pictureDataSourceProperties") DataSourceProperties primaryDataSourceProperties) {
		DataSource ds = primaryDataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		return ds;
	}

	@Bean(name = "pictureEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean  primaryEntityManagerFactory(
			EntityManagerFactoryBuilder  primaryEntityManagerFactoryBuilder,
			@Qualifier("pictureDataSource") DataSource primaryDataSource) {
		
		Map<String, String> primaryJpaProperties = new HashMap<>();
        primaryJpaProperties.put("hibernate.dialect", System.getenv("DB_DIALECT"));
        primaryJpaProperties.put("hibernate.hbm2ddl.auto", "update");
        primaryJpaProperties.put("hibernate.enable_lazy_load_no_trans", "true");

        LocalContainerEntityManagerFactoryBean ret = primaryEntityManagerFactoryBuilder
			.dataSource(primaryDataSource)
			.packages("com.trecapps.pictures.models")
			.persistenceUnit("pictureDataSource")
			.properties(primaryJpaProperties)
			.build();

        return ret;
	}

    @Bean(name = "pictureTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("pictureEntityManagerFactory") EntityManagerFactory primaryEntityManagerFactory) {

        return new JpaTransactionManager(primaryEntityManagerFactory);
    }
}