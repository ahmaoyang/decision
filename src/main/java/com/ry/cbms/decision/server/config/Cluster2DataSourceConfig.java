/*
package com.ry.cbms.decision.server.config;

*/
/**
 * @ClassName Cluster2DataSourceConfig
 * @Description TODO
 * @Author XTH.TOT
 * @Date 2019/7/18 16:25
 * @Version 1.0
 **//*

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;


@Configuration
@MapperScan(basePackages = "com.ry.cbms.decision.server.cbmsdao",sqlSessionTemplateRef = "cluster2SqlSessionTemplate")
public class Cluster2DataSourceConfig {

    */
/**
     * 创建数据源
     *@return DataSource
     *//*

    @Bean(name = "cluster2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cluster2")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    */
/**
     * 创建工厂
     *@param dataSource
     *@throws Exception
     *@return SqlSessionFactory
     *//*

    @Bean(name = "cluster2SqlSessionFactory")
    public SqlSessionFactory masterSqlSessionFactory(@Qualifier("cluster2DataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mybatis-mappers-cbms/*.xml"));
        bean.setTypeAliasesPackage("com.ry.cbms.decision.server.cbmsmodel");
        return bean.getObject();
    }

    */
/**
     * 创建事务
     *@param dataSource
     *@return DataSourceTransactionManager
     *//*

    @Bean(name = "cluster2TransactionManager")
    public DataSourceTransactionManager masterDataSourceTransactionManager(@Qualifier("clusterDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    */
/**
     * 创建模板
     *@param sqlSessionFactory
     *@return SqlSessionTemplate
     *//*

    @Bean(name = "cluster2SqlSessionTemplate")
    public SqlSessionTemplate masterSqlSessionTemplate(@Qualifier("cluster2SqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
*/
