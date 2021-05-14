package tqs.assignment.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import tqs.assignment.component.Cache;

@Configuration
@ConditionalOnClass(Cache.class)
@PropertySource("classpath:application.yml")
public class CacheConfiguration {
    @Value("${cache-collector.ttl}")
    private int ttl;

    @Value("${cache-collector.interval}")
    private int collectorInterval;

    @Bean
    @ConditionalOnMissingBean(Cache.class)
    public Cache cache(){
        return Cache.getOrCreate(ttl, collectorInterval);
    }
}
