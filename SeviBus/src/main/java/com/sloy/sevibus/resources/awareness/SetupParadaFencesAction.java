package com.sloy.sevibus.resources.awareness;

import android.support.annotation.NonNull;

import com.sloy.sevibus.resources.awareness.model.ParadaVisualization;
import com.sloy.sevibus.resources.awareness.model.ParadaVisualizationDataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

public class SetupParadaFencesAction {

    private final static Integer[] DAYS_OF_WEEK = {
            DateTimeConstants.MONDAY,
            DateTimeConstants.TUESDAY,
            DateTimeConstants.WEDNESDAY,
            DateTimeConstants.THURSDAY,
            DateTimeConstants.FRIDAY
    };

    private final ParadaVisualizationDataSource paradaVisualizationDataSource;

    public SetupParadaFencesAction(ParadaVisualizationDataSource paradaVisualizationDataSource) {
        this.paradaVisualizationDataSource = paradaVisualizationDataSource;
    }

    public Observable<Void> setupFences() {
        Observable<ParadaVisualization> validVisualizationsObservable = paradaVisualizationDataSource.obtainVisualizations()
                .filter(this::isWithinValidDate);
        List<ParadaVisualization> visualizations = validVisualizationsObservable.toList().toBlocking().single();

        for (Integer day : DAYS_OF_WEEK) {
            // Filtra los del día D
            List<ParadaVisualization> dayVisualizations = filterByDay(visualizations, day);
            // Filtra si menos de 2 valores
            //TODO what to do? break?
            // Agrupa por parada
            Map<Integer, List<ParadaVisualization>> groupedByParada = groupByParada(dayVisualizations);
            // Por cada parada:
            for (Integer parada : groupedByParada.keySet()) {
                List<ParadaVisualization> visualizationsFromParada = groupedByParada.get(parada);
                List<Long> timestamps = getTimestamps(visualizationsFromParada);
                List<Long> normalizedTimestamps = normalizeToSameDay(timestamps);
                Long firstVisualization = findFirst(normalizedTimestamps);
                // set fence at this time
                setFenceAt(day, parada, firstVisualization);
            }

        }

        return null;
    }

    private void setFenceAt(Integer day, Integer parada, Long timeOfDay) {

    }

    private List<Long> normalizeToSameDay(List<Long> timestamps) {
        List<Long> normalized = new ArrayList<>(timestamps.size());
        for (Long timestamp : timestamps) {
            DateTime absoluteDate = new DateTime(timestamp);
            int millis = absoluteDate.millisOfDay().get();
            normalized.add((long) millis);
        }
        return normalized;
    }

    private List<Long> getTimestamps(List<ParadaVisualization> visualizations) {
        List<Long> timestamps = new ArrayList<>(visualizations.size());
        for (ParadaVisualization visualization : visualizations) {
            timestamps.add(visualization.getTimestamp());
        }
        return timestamps;
    }

    private Long findFirst(List<Long> timestamps) {
        Long min = Long.MAX_VALUE;
        for (Long timestamp : timestamps) {
            if (timestamp < min) {
                min = timestamp;
            }
        }
        return min;
    }

    private Map<Integer, List<ParadaVisualization>> groupByParada(List<ParadaVisualization> visualizations) {
        Map<Integer, List<ParadaVisualization>> map = new HashMap<>();
        for (ParadaVisualization visualization : visualizations) {
            if (map.get(visualization.getParadaNumero()) != null) {
                map.get(visualization.getParadaNumero()).add(visualization);
            } else {
                List<ParadaVisualization> listByParada = new ArrayList<>();
                listByParada.add(visualization);
                map.put(visualization.getParadaNumero(), listByParada);
            }
        }
        return map;
    }

    private List<ParadaVisualization> filterByDay(List<ParadaVisualization> input, int day) {
        List<ParadaVisualization> output = new ArrayList<>();
        for (ParadaVisualization visualization : input) {
            DateTime timestamp = new DateTime(visualization.getTimestamp());
            if (timestamp.dayOfWeek().get() == day) {
                output.add(visualization);
            }
        }
        return output;
    }

    @NonNull
    private Func1<List<ParadaVisualization>, Boolean> hasAtLeast2Values() {
        return list -> list.size() > 2;
    }

    @NonNull
    private Func1<ParadaVisualization, Boolean> isFromWeekDay(Integer day) {
        return paradaVisualization -> {
            DateTime timestamp = new DateTime(paradaVisualization.getTimestamp());
            return timestamp.dayOfWeek().get() == day;
        };
    }


    private Boolean isWithinValidDate(ParadaVisualization paradaVisualization) {
        DateTime timestamp = new DateTime(paradaVisualization.getTimestamp());
        DateTime minimumValidDate = getMinimumValidDate();
        return timestamp.isAfter(minimumValidDate);
    }

    private DateTime getMinimumValidDate() {
        DateTime now = DateTime.now();
        return now.minusWeeks(4);
    }
}
