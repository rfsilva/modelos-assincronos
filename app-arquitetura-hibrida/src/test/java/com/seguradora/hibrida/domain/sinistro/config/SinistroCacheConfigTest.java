package com.seguradora.hibrida.domain.sinistro.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroCacheConfig Tests")
class SinistroCacheConfigTest {

    private SinistroCacheConfig config;

    @BeforeEach
    void setUp() {
        config = new SinistroCacheConfig();
    }

    @Test
    @DisplayName("constantes de nome de cache devem ter valores corretos")
    void cacheNameConstantsShouldBeCorrect() {
        assertThat(SinistroCacheConfig.CACHE_DASHBOARD).isEqualTo("sinistro-dashboard");
        assertThat(SinistroCacheConfig.CACHE_LIST).isEqualTo("sinistro-list");
        assertThat(SinistroCacheConfig.CACHE_DETAIL).isEqualTo("sinistro-detail");
        assertThat(SinistroCacheConfig.CACHE_ANALYTICS).isEqualTo("sinistro-analytics");
        assertThat(SinistroCacheConfig.CACHE_QUERY).isEqualTo("sinistro-query");
    }

    @Test
    @DisplayName("cacheManager deve criar CacheManager com os caches configurados")
    void cacheManagerShouldCreateWithAllCaches() {
        CacheManager cacheManager = config.cacheManager();

        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager.getCacheNames()).containsExactlyInAnyOrder(
                SinistroCacheConfig.CACHE_DASHBOARD,
                SinistroCacheConfig.CACHE_LIST,
                SinistroCacheConfig.CACHE_DETAIL,
                SinistroCacheConfig.CACHE_ANALYTICS,
                SinistroCacheConfig.CACHE_QUERY
        );
    }

    @Test
    @DisplayName("dashboardCache deve criar cache não nulo")
    void dashboardCacheShouldCreateNonNull() {
        Cache<String, Object> cache = config.dashboardCache();
        assertThat(cache).isNotNull();
    }

    @Test
    @DisplayName("dashboardCache deve suportar put e get")
    void dashboardCacheShouldSupportPutAndGet() {
        Cache<String, Object> cache = config.dashboardCache();
        cache.put("key1", "value1");
        assertThat(cache.getIfPresent("key1")).isEqualTo("value1");
    }

    @Test
    @DisplayName("listCache deve criar cache não nulo")
    void listCacheShouldCreateNonNull() {
        Cache<String, Object> cache = config.listCache();
        assertThat(cache).isNotNull();
    }

    @Test
    @DisplayName("detailCache deve criar cache não nulo")
    void detailCacheShouldCreateNonNull() {
        Cache<String, Object> cache = config.detailCache();
        assertThat(cache).isNotNull();
    }

    @Test
    @DisplayName("analyticsCache deve criar cache não nulo")
    void analyticsCacheShouldCreateNonNull() {
        Cache<String, Object> cache = config.analyticsCache();
        assertThat(cache).isNotNull();
    }

    @Test
    @DisplayName("cacheWarmer deve ser criado com caches válidos")
    void cacheWarmerShouldBeCreatedWithValidCaches() {
        Cache<String, Object> dashboard = config.dashboardCache();
        Cache<String, Object> list = config.listCache();
        Cache<String, Object> analytics = config.analyticsCache();

        SinistroCacheConfig.SinistroCacheWarmer warmer =
                config.cacheWarmer(dashboard, list, analytics);

        assertThat(warmer).isNotNull();
    }

    @Test
    @DisplayName("SinistroCacheWarmer.invalidateAll deve limpar os caches")
    void cacheWarmerInvalidateAllShouldClearCaches() {
        Cache<String, Object> dashboard = config.dashboardCache();
        Cache<String, Object> list = config.listCache();
        Cache<String, Object> analytics = config.analyticsCache();

        dashboard.put("k1", "v1");
        list.put("k2", "v2");
        analytics.put("k3", "v3");

        SinistroCacheConfig.SinistroCacheWarmer warmer =
                new SinistroCacheConfig.SinistroCacheWarmer(dashboard, list, analytics);

        warmer.invalidateAll();

        assertThat(dashboard.getIfPresent("k1")).isNull();
        assertThat(list.getIfPresent("k2")).isNull();
        assertThat(analytics.getIfPresent("k3")).isNull();
    }

    @Test
    @DisplayName("SinistroCacheWarmer.getStatistics deve retornar estatísticas dos 3 caches")
    void cacheWarmerGetStatisticsShouldReturnStats() {
        Cache<String, Object> dashboard = config.dashboardCache();
        Cache<String, Object> list = config.listCache();
        Cache<String, Object> analytics = config.analyticsCache();

        SinistroCacheConfig.SinistroCacheWarmer warmer =
                new SinistroCacheConfig.SinistroCacheWarmer(dashboard, list, analytics);

        SinistroCacheConfig.CacheStatistics stats = warmer.getStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats.getAll()).containsKeys("dashboard", "list", "analytics");
    }

    @Test
    @DisplayName("CacheStatistics.addCacheStats deve armazenar por nome")
    void cacheStatisticsAddShouldStoreByName() {
        SinistroCacheConfig.CacheStatistics stats = new SinistroCacheConfig.CacheStatistics();
        CacheStats cs = CacheStats.of(10, 2, 0, 0, 0, 0, 0);

        stats.addCacheStats("test", cs);

        assertThat(stats.get("test")).isEqualTo(cs);
    }

    @Test
    @DisplayName("CacheStatistics.getTotalHitRate deve retornar 0.0 para stats vazio")
    void cacheStatisticsHitRateShouldReturn0ForEmpty() {
        SinistroCacheConfig.CacheStatistics stats = new SinistroCacheConfig.CacheStatistics();
        assertThat(stats.getTotalHitRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("CacheStatistics.getTotalHitRate deve calcular corretamente")
    void cacheStatisticsHitRateShouldCalculate() {
        SinistroCacheConfig.CacheStatistics stats = new SinistroCacheConfig.CacheStatistics();
        // hits=8, misses=2, totalRequests=10
        stats.addCacheStats("c1", CacheStats.of(8, 2, 0, 0, 0, 0, 0));

        assertThat(stats.getTotalHitRate()).isEqualTo(0.8);
    }

    @Test
    @DisplayName("CacheStatistics.toString deve conter informações dos caches")
    void cacheStatisticsToStringShouldContainInfo() {
        SinistroCacheConfig.CacheStatistics stats = new SinistroCacheConfig.CacheStatistics();
        stats.addCacheStats("dashboard", CacheStats.of(5, 3, 0, 0, 0, 0, 0));

        String str = stats.toString();

        assertThat(str).contains("dashboard");
        assertThat(str).contains("CacheStatistics");
    }

    @Test
    @DisplayName("logCacheStats não deve lançar exceções")
    void logCacheStatsShouldNotThrow() {
        config.logCacheStats();
    }
}
