package site.wanjiahao.gulimall.ware.config;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

//@EnableConfigurationProperties(DataSourceProperties.class)
public class SeataConfig {

    @Bean
    public DataSource dataSource(DataSourceProperties properties) {
        DruidDataSource dataSource = properties.initializeDataSourceBuilder().type(DruidDataSource.class).build();
        return new DataSourceProxy(dataSource);
    }
}
