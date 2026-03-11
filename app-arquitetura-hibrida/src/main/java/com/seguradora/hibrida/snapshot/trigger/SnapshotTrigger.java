package com.seguradora.hibrida.snapshot.trigger;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.snapshot.SnapshotProperties;
import com.seguradora.hibrida.snapshot.SnapshotStore;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Componente responsável por detectar quando um snapshot deve ser criado.
 *
 * <p>O SnapshotTrigger analisa o estado do aggregate e decide se um novo
 * snapshot deve ser criado baseado em:
 * <ul>
 *   <li>Número de eventos desde o último snapshot (threshold configurável)</li>
 *   <li>Tempo decorrido desde o último snapshot</li>
 *   <li>Tamanho estimado do aggregate</li>
 *   <li>Política de snapshot definida nas propriedades</li>
 * </ul>
 *
 * <p>Por padrão, cria snapshot a cada 50 eventos, mas este valor é configurável
 * via application.yml através da propriedade {@code snapshot.threshold}.
 *
 * <p>Exemplo de uso:
 * <pre>{@code
 * // Verificar se deve criar snapshot
 * if (snapshotTrigger.shouldTriggerSnapshot(aggregate)) {
 *     snapshotTrigger.createSnapshot(aggregate);
 * }
 * }</pre>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotTrigger {

    private final SnapshotStore snapshotStore;
    private final SnapshotProperties snapshotProperties;

    /**
     * Verifica se deve disparar criação de snapshot para um aggregate.
     *
     * <p>Analisa múltiplos critérios:
     * <ul>
     *   <li>Número de eventos desde último snapshot vs threshold configurado</li>
     *   <li>Se aggregate nunca teve snapshot e atingiu threshold mínimo</li>
     *   <li>Configuração global de snapshots habilitada/desabilitada</li>
     * </ul>
     *
     * @param aggregate Aggregate root a ser analisado
     * @return true se deve criar snapshot, false caso contrário
     */
    public boolean shouldTriggerSnapshot(AggregateRoot aggregate) {
        if (!snapshotProperties.isEnabled()) {
            log.trace("Snapshot system is disabled, not triggering for aggregate {}", aggregate.getId());
            return false;
        }

        if (aggregate == null || aggregate.getId() == null) {
            log.warn("Cannot trigger snapshot for null aggregate or aggregate without ID");
            return false;
        }

        try {
            String aggregateId = aggregate.getId();
            long currentVersion = aggregate.getVersion();

            boolean shouldTrigger = snapshotStore.shouldCreateSnapshot(aggregateId, currentVersion);

            if (shouldTrigger) {
                log.debug("Snapshot trigger activated for aggregate {} at version {} (threshold: {})",
                         aggregateId, currentVersion, snapshotProperties.getSnapshotThreshold());
            }

            return shouldTrigger;

        } catch (Exception e) {
            log.error("Error evaluating snapshot trigger for aggregate {}: {}",
                     aggregate.getId(), e.getMessage(), e);
            return false; // Em caso de erro, não disparar snapshot
        }
    }

    /**
     * Cria snapshot de um aggregate.
     *
     * <p>Extrai o estado atual do aggregate e persiste como snapshot.
     * A operação é assíncrona e não bloqueia a thread chamadora.
     *
     * @param aggregate Aggregate root a ter snapshot criado
     */
    public void createSnapshot(AggregateRoot aggregate) {
        if (aggregate == null || aggregate.getId() == null) {
            log.warn("Cannot create snapshot for null aggregate or aggregate without ID");
            return;
        }

        try {
            log.debug("Creating snapshot for aggregate {} at version {}",
                     aggregate.getId(), aggregate.getVersion());

            // Criar snapshot com os dados do aggregate
            Map<String, Object> snapshotData = new HashMap<>();
            snapshotData.put("aggregate", aggregate);
            snapshotData.put("id", aggregate.getId());
            snapshotData.put("version", aggregate.getVersion());
            snapshotData.put("type", aggregate.getClass().getName());

            AggregateSnapshot snapshot = new AggregateSnapshot(
                aggregate.getId(),
                aggregate.getClass().getSimpleName(),
                aggregate.getVersion(),
                snapshotData
            );

            // Salvamento assíncrono
            snapshotStore.saveSnapshot(snapshot);

            log.info("Snapshot creation triggered for aggregate {} at version {}",
                    aggregate.getId(), aggregate.getVersion());

        } catch (Exception e) {
            log.error("Failed to create snapshot for aggregate {} at version {}: {}",
                     aggregate.getId(), aggregate.getVersion(), e.getMessage(), e);
            // Não propagar exceção - falha de snapshot não deve afetar operação principal
        }
    }

    /**
     * Verifica se deve disparar snapshot e cria se necessário.
     *
     * <p>Método de conveniência que combina {@link #shouldTriggerSnapshot(AggregateRoot)}
     * e {@link #createSnapshot(AggregateRoot)} em uma única chamada.
     *
     * @param aggregate Aggregate root a ser processado
     * @return true se snapshot foi criado, false caso contrário
     */
    public boolean tryCreateSnapshot(AggregateRoot aggregate) {
        if (shouldTriggerSnapshot(aggregate)) {
            createSnapshot(aggregate);
            return true;
        }
        return false;
    }

    /**
     * Força criação de snapshot independente do threshold.
     *
     * <p>Útil para situações especiais como:
     * <ul>
     *   <li>Migração de dados</li>
     *   <li>Operações administrativas</li>
     *   <li>Testes</li>
     * </ul>
     *
     * @param aggregate Aggregate root a ter snapshot forçado
     */
    public void forceSnapshot(AggregateRoot aggregate) {
        log.info("Forcing snapshot creation for aggregate {} at version {} (bypassing threshold)",
                aggregate.getId(), aggregate.getVersion());
        createSnapshot(aggregate);
    }

    /**
     * Retorna o threshold configurado para criação de snapshots.
     *
     * @return Número de eventos necessários para disparar novo snapshot
     */
    public int getConfiguredThreshold() {
        return snapshotProperties.getSnapshotThreshold();
    }

    /**
     * Verifica se o sistema de snapshots está habilitado.
     *
     * @return true se habilitado, false caso contrário
     */
    public boolean isSnapshotSystemEnabled() {
        return snapshotProperties.isEnabled();
    }
}
